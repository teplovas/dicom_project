package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glTexImage1D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.awt.Canvas;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import net.sourceforge.fastpng.PNGDecoder;
import tools.DicomImage;

public class RenderingLoop {	
	static int shaderProgramInterval;
	
	private static int paletteId = -1;
		
	private static int[] palettes = new int[4];
	private static boolean closeRequested = false;
	private static float scale = 1.0f;
	private static boolean isImageLoad = false;
	
	private static int fragmentShaderInterval;
	private static int vertexShaderInterval;
	
	private static int from;
	private static int to;
	private static boolean isInvert = false;
	private static boolean isRotate = false;
	private static boolean isImageChanged = false;
	private static boolean isMesurements = false;
	
	private static float centerX = 0.0f;
	private static float centerY = 0.0f;
	
	private static float moveX = 0.0f;
	private static float moveY = 0.0f;
	
	private static float lastX;
	private static float lastY;
	private static boolean isFirstMove = true;
	
	private static int displayWidth;
	private static int displayHeight;
	
	private static int scaleWidth;
	private static int scaleHeight;
	
	private static int numberOfImages;
	private static int currentImageNumber;
	
	private static Float pixelSpacing;
	private static boolean isByte = false;
	private static Boolean isZoom = null;
	private static DicomImage image;
	
	public static void init(Canvas canvas) 
	{
		displayWidth = canvas.getWidth();
		displayHeight = canvas.getHeight();
		ContextInitialization.init(canvas);
		ImageRender.init(ContextInitialization.getShaderProgramInterval(), 
				5, ContextInitialization.getPalettes(), displayWidth, displayHeight);
		MesurementsRender.init(displayWidth, displayHeight);
		AdditionalInfoRender.init(displayHeight);
	}
	
	public static void bindImage()
	{
		if(isImageChanged)
		{
			ImageRender.bindImage(image);
			pixelSpacing = image.getPixelSpacing();
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
				moveFrame((float)(Mouse.getX() - lastX), (float)(lastY - Mouse.getY()));
			}
			lastX = Mouse.getX();
			lastY = Mouse.getY();
			isFirstMove = false;
		}
		else
		{
			isFirstMove = true;
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
	
	private static void moveFrame(float x, float y)
	{
		moveX = x;
		moveY = y;
		centerX += x;
		centerY += y;
	}
	
	private static void rotate() {
		if (isRotate) {
			
			int scalingWidth = (int) (scaleHeight * scale);
			int shiftW = (displayHeight - scalingWidth) / 2;
			
			glTranslatef(displayHeight / 2 - shiftW, displayHeight / 2 - shiftW, 0.0f);
			glRotatef(90.f, 0.0f, 0.0f, 1.0f);
			glTranslatef(-displayHeight / 2 + shiftW, -displayHeight / 2 + shiftW, 0.0f);

			isRotate = false;
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
			//glTranslatef(moveX, moveY, 0);
			//rotate();
			glUseProgram(ContextInitialization.getShaderProgramInterval());
			Util.checkGLError();
			bindImage();
			if (isImageLoad) 
			{
				//Image
				ImageRender.renderImage(from, to, isInvert, paletteId, isZoom, isRotate, moveX, moveY);
				//Measurements
				MesurementsRender.renderMesurements(scale, isMesurements);
				//Additional info
				AdditionalInfoRender.renderInfo(currentImageNumber, numberOfImages);
			}
			isZoom = null;
			isRotate = false;
			moveFrame(0.0f, 0.0f);
			glUseProgram(0);
			Display.sync(60);
			Display.update();
			
		}
		GL11.glDeleteTextures(palettes[0]);
		GL11.glDeleteTextures(palettes[1]);
		GL11.glDeleteTextures(palettes[2]);
		GL11.glDeleteTextures(palettes[3]);
		glDeleteProgram(shaderProgramInterval);
		glDeleteShader(vertexShaderInterval);
		glDeleteShader(fragmentShaderInterval);

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
