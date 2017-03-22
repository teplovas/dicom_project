package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import tools.DicomImage;

public class RenderingLoop {	
	static int shaderProgramInterval;
	
	private static int paletteId = -1;
	private static float scale = 1.0f;
	private static boolean isImageLoad = false;
	
	private static int from;
	private static int to;
	private static boolean isInvert = false;
	private static boolean isRotate = false;
	private static boolean isImageChanged = false;
	private static boolean isMesurements = false;
	
	private static float deltaPosX = 0.0f;
	private static float deltaPosY = 0.0f;
	
	private static float lastX;
	private static float lastY;
	private static boolean isFirstMove = true;
	
	public static int displayWidth;
	public static int displayHeight;
	
	private static int numberOfImages;
	private static int currentImageNumber;
	
	private static Boolean isZoom = null;
	private static DicomImage image;
	
	private static Matrix4f projectionMatrix = null;
	private static Matrix4f viewMatrix = null;
	private static Matrix4f modelMatrix = null;
	private static Vector3f modelPos = null;
	private static Vector3f modelAngle = null;
	private static Vector3f modelScale = null;
	private static Vector3f cameraPos = null;
	private static Matrix4f transfMatrix = null;
	private static FloatBuffer transformMatrix = null;
	private static File fileToSave = null;
	
	public static void init(Canvas canvas) 
	{
		displayWidth = canvas.getWidth();
		displayHeight = canvas.getHeight();
		
		ContextInitialization.init(canvas);
		ImageRender.init(ContextInitialization.getImageShaderProgram(), 
				7, ContextInitialization.getPalettes(), displayWidth, displayHeight);
		MeasurementsRender.init(displayWidth, displayHeight, ContextInitialization.getMeasureShaderProgram());
		AdditionalInfoRender.init(displayHeight);
	}
	
	private static void setupMatrices(int width, int height) {
		modelPos = new Vector3f(0, 0, 0);
		modelAngle = new Vector3f(0, 0, 0);
		modelScale = new Vector3f(1, 1, 1);
		cameraPos = new Vector3f(0, 0, -1);
		
		// Setup projection matrix
		projectionMatrix = new Matrix4f();
		float aspectRatio = (float) width / (float) height;
		float near_plane = 0.1f;
		float far_plane = 100f;

		float y_scale = 1;//Tools.coTangent(Tools.degreesToRadians(fieldOfView / 2f));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = far_plane - near_plane;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((far_plane + near_plane) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * near_plane * far_plane) / frustum_length);
		projectionMatrix.m33 = 0;

		// Setup view matrix
		viewMatrix = new Matrix4f();

		// Setup model matrix
		modelMatrix = new Matrix4f();

		// Create a FloatBuffer with the proper size to store our matrices later
		transformMatrix = BufferUtils.createFloatBuffer(16);
	}
	
	public static void bindImage()
	{
		if(isImageChanged)
		{
			ImageRender.bindImage(image);
			isImageLoad = true;
			isImageChanged = false;
			setupMatrices(image.getWidth(), image.getHeight());
		}
	}
	
	private static void checkMousePressed()
	{
		if(Mouse.isButtonDown(0))
		{
			if(!isFirstMove)
			{
				deltaPosX = Mouse.getX() - lastX;
				deltaPosY = Mouse.getY() - lastY;
			}
			lastX = Mouse.getX();
			lastY = Mouse.getY();
			isFirstMove = false;
		}
		else
		{
			isFirstMove = true;
			deltaPosY = 0f;
			deltaPosX = 0f;
		}
	}
	
	private static void checkMouseWheel()
	{
		if (Mouse.isInsideWindow()) {
			int dWheel = Mouse.getDWheel();
			if (dWheel < 0) {
				changeScale(-0.1f);
			} else if (dWheel > 0) {
				changeScale(0.1f);
			}
		}
	}
	
	public static void changeScale(float scale)
	{
		RenderingLoop.scale += scale;
		if(scale < 0)
		{
			isZoom = false;
		}
		else
		{
			isZoom = true;
		}
	}
	
	
	public static void changePalette(String paletteName)
	{
		paletteId = getPaletteId(paletteName);
	}
	
	public static void notUsePalette()
	{
		paletteId = -1;
	}
	
	private static Integer getPaletteId(String palette)
	{
		switch (palette) {
		case "hotIron": {
			return 0;
		}
		case "pet": {
			return 1;
		}
		case "hotMetalBlue": {
			return 2;
		}
		case "pet20": {
			return 3;
		}
		}
		return 0;
	}
	
	private static FloatBuffer transformMatrix(boolean isMeasure,Boolean isZoom, boolean isRotate, 
			float moveX, float moveY) {
		// -- Input processing
		float rotationDelta = 90f;
		float scaleDelta = 0.1f;
		Vector3f scaleAddResolution = new Vector3f(scaleDelta, scaleDelta, scaleDelta);
		Vector3f scaleMinusResolution = new Vector3f(-scaleDelta, -scaleDelta, -scaleDelta);
		if (isZoom != null) {
			Vector3f.add(modelScale, isZoom ? scaleAddResolution : scaleMinusResolution, modelScale);
		}
		if (isRotate) {
			modelAngle.z += rotationDelta;
		}
//		if(isMeasure)
//		{
//			modelPos.y -= moveY / 500f;
//		}
//		else
		{
			modelPos.y += moveY / 500f;
		}
		modelPos.x += moveX / 500f;

		// -- Update matrices
		// Reset view and model matrices
		viewMatrix = new Matrix4f();
		modelMatrix = new Matrix4f();

		// Translate camera
		Matrix4f.translate(cameraPos, viewMatrix, viewMatrix);

		// Scale, translate and rotate model
		Matrix4f.scale(modelScale, modelMatrix, modelMatrix);
		Matrix4f.translate(modelPos, modelMatrix, modelMatrix);
		Matrix4f.rotate(Tools.degreesToRadians(modelAngle.z), new Vector3f(0, 0, 1), modelMatrix, modelMatrix);
		Matrix4f.rotate(Tools.degreesToRadians(modelAngle.y), new Vector3f(0, 1, 0), modelMatrix, modelMatrix);
		Matrix4f.rotate(Tools.degreesToRadians(modelAngle.x), new Vector3f(1, 0, 0), modelMatrix, modelMatrix);

		transfMatrix = new Matrix4f();
		Matrix4f.mul(projectionMatrix, viewMatrix, transfMatrix);
		Matrix4f.mul(transfMatrix, modelMatrix, transfMatrix);
		
		transfMatrix.store(transformMatrix);
		transformMatrix.flip();
		return transformMatrix;
	}
	
	public static void startRender()
	{
		while (!Display.isCloseRequested()) {
			Util.checkGLError();
			if(!isMesurements)
			{
				checkMousePressed();
			}
			Util.checkGLError();
			checkMouseWheel();
			Util.checkGLError();
			glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
			Util.checkGLError();
			glClear(GL_COLOR_BUFFER_BIT);
			Util.checkGLError();
			bindImage();
			if (isImageLoad) 
			{
				//Image
				ImageRender.renderImage(from, to, isInvert, paletteId, 
						transformMatrix(false, isZoom, isRotate, deltaPosX, deltaPosY));
				transformMatrix.clear();
				//Measurements
				MeasurementsRender.renderMeasurements(scale, isMesurements, 
						transformMatrix(true, isZoom, isRotate, deltaPosX, deltaPosY));
				if(fileToSave != null)
				{
					getPixelDData();
					fileToSave = null;
				}
				//Additional info
				glUseProgram(0);
				AdditionalInfoRender.renderInfo(currentImageNumber, numberOfImages);
				transformMatrix.clear();
			}
			isZoom = null;
			isRotate = false;
			glUseProgram(0);
			Display.sync(60);
			Display.update();
			
		}
		ContextInitialization.destroyContext();

		Display.destroy();
		System.exit(0);
	}
	
	public static void saveScreenToFile(File file)
	{
		fileToSave = file;
	}
	
	public static void getPixelDData()
	{
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = displayWidth;
		int height = displayHeight;
		int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
		
		String format = "PNG"; // Example: "PNG" or "JPG"
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		   
		for(int x = 0; x < width; x++) 
		{
		    for(int y = 0; y < height; y++)
		    {
		        int i = (x + (width * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		   
		try {
		    ImageIO.write(image, format, fileToSave);
		} catch (IOException e) { e.printStackTrace(); }
	}
	

	public static void setScale(float scale) {
		RenderingLoop.scale = scale;
	}

	public static void setFrom(int from) {
		RenderingLoop.from = from;
	}

	public static void setTo(int to) {
		RenderingLoop.to = to;
	}

	public static void setInvert(boolean isInvert) {
		RenderingLoop.isInvert = isInvert;
	}

	public static void setMesurements(boolean isMesurements) {
		RenderingLoop.isMesurements = isMesurements;
	}

	public static void setRotate(boolean isRotate) {
		RenderingLoop.isRotate = isRotate;
	}

	public static void setNumberOfImages(int numberOfImages) {
		RenderingLoop.numberOfImages = numberOfImages;
	}

	public static void setCurrentImageNumber(int currentImageNumber) {
		RenderingLoop.currentImageNumber = currentImageNumber;
	}
	
	public static void loadImage(DicomImage img)
	{
		image = img;
		isImageChanged = true;
	}

	public static Matrix4f getTransformMatrix() {
		return transfMatrix;
	}
}
