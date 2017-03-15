package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.awt.Canvas;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.Util;

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
	
	private static int displayWidth;
	private static int displayHeight;
	
	private static int numberOfImages;
	private static int currentImageNumber;
	
	private static Boolean isZoom = null;
	private static DicomImage image;
	
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
	
	public static void bindImage()
	{
		if(isImageChanged)
		{
			ImageRender.bindImage(image);
			isImageLoad = true;
			isImageChanged = false;
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
			glUseProgram(ContextInitialization.getImageShaderProgram());
			Util.checkGLError();
			bindImage();
			if (isImageLoad) 
			{
				//Image
				ImageRender.renderImage(from, to, isInvert, paletteId, isZoom, isRotate, deltaPosX, deltaPosY);
				//Measurements
				MeasurementsRender.renderMeasurements(scale, isMesurements, isZoom, isRotate, deltaPosX, deltaPosY);
				//Additional info
				glUseProgram(0);
				AdditionalInfoRender.renderInfo(currentImageNumber, numberOfImages);
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
}
