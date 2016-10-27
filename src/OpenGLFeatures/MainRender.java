package OpenGLFeatures;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.glu.GLU;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.imageio.ImageIO;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import net.sourceforge.fastpng.PNGDecoder;
import tools.PaletteLoader;

/** 
 * @author teplova.s 
 * */
public class MainRender{
	
	static int shaderProgramInterval;
	
	private static int paletteId = 0;
	
	private static int imageTextureId = 0;
	
	private static int[] palettes = new int[4];
	private static boolean closeRequested = false;
	private static float scale = 1.0f;
	private static boolean isImageLoad = false;
	
	private static int fragmentShaderInterval;
	private static int vertexShaderInterval;
	
	private static Object[] imageBuffer;
	private static int width;
	private static int height;
	private static int from;
	private static int to;
	private static boolean isUsePalette = false;
	private static boolean isImageChanged = false;
	
	private static float moveX = 0.0f;
	private static float moveY = 0.0f;
	
	private static float lastX;
	private static float lastY;
	private static boolean isFirstMove = true;
	
	
	public MainRender(Object[] imageBuffer, int width, int height, Canvas canv)
	{
		tmpFunc(imageBuffer, width, height, canv);
	}
	
	public static void main(String[] args){
		int[][] palette  = PaletteLoader.getPalette(1);
		BufferedImage newImage = new BufferedImage(palette.length, 1,
				BufferedImage.TYPE_3BYTE_BGR);

		for(int i = 0; i < palette.length; i++)
		{
			int r = palette[i][0];
			int g = palette[i][1];
			int b = palette[i][2];

			int newRgb = ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8)
					| (b & 0x0ff);
			newImage.setRGB(i, 0, newRgb);
		}
		try {
			ImageIO.write(newImage, "PNG", new File("res/hotIron.PNG"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void initDisplay(Canvas canv) {
		File JGLLib = new File("native/");

		System.setProperty("org.lwjgl.librarypath", JGLLib.getAbsolutePath());
		try {
			Display.setParent(canv);
			Display.setVSyncEnabled(true);
			Display.create();
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialized correctly. :(");
			Display.destroy();
			System.exit(1);
		}
	}
	
	public static void loadShadersAndPallettes()
	{
		shaderProgramInterval = glCreateProgram();
		
		fragmentShaderInterval = createShader("shaderWindow.frag", true);
		vertexShaderInterval = createShader("shaderWindow.vert", false);
		
		glAttachShader(shaderProgramInterval, vertexShaderInterval);
		glAttachShader(shaderProgramInterval, fragmentShaderInterval);
		
		glLinkProgram(shaderProgramInterval);
		glValidateProgram(shaderProgramInterval);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 600, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_TEXTURE_1D);
		glUseProgram(shaderProgramInterval);
		loadPalettes();
	}
	
	private static void loadPalettes() {
		IntBuffer texture_object_handles = BufferUtils.createIntBuffer(5);
		glGenTextures(texture_object_handles);
		imageTextureId = texture_object_handles.get(4);
		for (int i = 0; i < 4; i++) {
			try {
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
				ByteBuffer buf = null;
				PNGDecoder decoder = null;
				InputStream in = null;
				int width, height;
				try {
					in = new FileInputStream("res/" + getPaletteTexture(i + 1) + ".PNG");
					decoder = new PNGDecoder(in);

					width = decoder.getWidth();
					height = decoder.getHeight();

					buf = BufferUtils.createByteBuffer(4 * width * height);
					decoder.decode(buf, width * 4, PNGDecoder.TextureFormat.RGBA);
					buf.flip();
				} finally {
					in.close();
				}
				palettes[i] = texture_object_handles.get(i);

				glBindTexture(GL_TEXTURE_1D, palettes[i]);
				Util.checkGLError();
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				Util.checkGLError();

				// glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
				glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, decoder.getWidth(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
			} catch (Exception e) {
			}
		}
	}
	
	private static void loadImage(Object[] imageBuffer, int width, int height)
	{
		if(imageBuffer == null || !isImageChanged)
			return;
		
//		glMatrixMode(GL_PROJECTION);
//		glLoadIdentity();
//		glOrtho(0, width, height, 0, 1, -1);
//		glMatrixMode(GL_MODELVIEW);
//		glEnable(GL_TEXTURE_2D);
//		glEnable(GL_TEXTURE_1D);
		
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture1"), 4);
		Util.checkGLError();
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
		Util.checkGLError();
		glBindTexture(GL_TEXTURE_2D, imageTextureId);
		Util.checkGLError();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		Util.checkGLError();
		boolean isByte = false;
		Buffer buffer;// = BufferUtils.createByteBuffer(imageBuffer.length);
		if(imageBuffer[0] instanceof Byte)
		{
			isByte = true;
			buffer = BufferUtils.createByteBuffer(imageBuffer.length);
			for(Object o : imageBuffer)
			{
				byte by = (byte)o;
				((ByteBuffer)buffer).put((by));
			}
		}
		else
		{
			buffer = BufferUtils.createShortBuffer(imageBuffer.length);
			for(Object o : imageBuffer)
			{
				short by = (short)o;
				((ShortBuffer)buffer).put((by));
			}
		}
		
		buffer.flip();
		Util.checkGLError();
		if(isByte)
		{
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R32I, width, height, 0, GL30.GL_RED_INTEGER, 
					GL_BYTE, (ByteBuffer)buffer);
		}
		else
		{
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R32I, width, height, 0, GL30.GL_RED_INTEGER, 
					GL_SHORT, (ShortBuffer)buffer);
		}
		isImageLoad = true;
		isImageChanged = false;
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
	
	public static void startRendering() {
		while (!Display.isCloseRequested() && !closeRequested) {
			checkInput();
			checkMousePressed();
			checkMouseWheel();
			glClearColor(0.92549f, 0.917647f, 0.917647f, 0.5f);
			glClear(GL_COLOR_BUFFER_BIT);

			glTranslatef(moveX, moveY, 0);

			glUseProgram(shaderProgramInterval);
			loadImage(imageBuffer, width, height);
			if (isImageLoad) {
				Util.checkGLError();
				// glUniform1f(glGetUniformLocation(shaderProgramInterval,
				// "scale"), 1.0f);
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "from"), from);
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "to"), to);
				Util.checkGLError();
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "width"), width);
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "height"), height);
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "isUsePalette"), isUsePalette ? 1 : 0);
				Util.checkGLError();

				GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
				GL11.glBindTexture(GL_TEXTURE_2D, imageTextureId);

				glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture2"), palettes[paletteId] - 1);
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + paletteId);
				glBindTexture(GL_TEXTURE_1D, palettes[paletteId]);

				int scalingWidth = (int) (width * scale);
				int scalingHeight = (int) (height * scale);

				glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2i(0, 0);

				glTexCoord2d(1, 0);
				glVertex2i(scalingWidth, 0);

				glTexCoord2d(1, 1);
				glVertex2i(scalingWidth, scalingHeight);

				glTexCoord2d(0, 1);
				glVertex2i(0, scalingHeight);
				glEnd();

				// ByteBuffer bytes = BufferUtils.createByteBuffer(width *
				// height * 4);
				// //GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA,
				// GL11.GL_UNSIGNED_BYTE, bytes);
				// glGetTexImage(GL_TEXTURE_2D, 0, GL11.GL_RGBA,
				// GL11.GL_UNSIGNED_BYTE, bytes);
				// ========================================================================
			}
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
	
	public static void moveFrame(float x, float y)
	{
		moveX = x;
		moveY = y;
	}
	
	
	public static void setImageBuffer(Object[] imageBuffer)
	{
		MainRender.imageBuffer = imageBuffer;
		MainRender.isImageChanged = true;
	}
	
	public static void setWidth(int width)
	{
		MainRender.width = width;
	}
	
	public static void setHeight(int height)
	{
		MainRender.height = height;
	}
	
	public static void setFrom(int from)
	{
		MainRender.from = from;
	}
	
	public static void setTo(int to)
	{
		MainRender.to = to;
	}
	
	
	public static void tmpFunc(Object[] imageBuffer, int width, int height, Canvas canv)
	{		
		try {
			Display.setParent(canv);
	        Display.setVSyncEnabled(true);
		Display.create();
	} catch (LWJGLException e) {
		System.err.println("The display wasn't initialized correctly. :(");
		Display.destroy();
		System.exit(1);
	}
	shaderProgramInterval = glCreateProgram();
		
	int fragmentShaderInterval = createShader("shaderWindow.frag", true);
	int vertexShaderInterval = createShader("shaderWindow.vert", false);
	
	glAttachShader(shaderProgramInterval, vertexShaderInterval);
	glAttachShader(shaderProgramInterval, fragmentShaderInterval);
	
	glLinkProgram(shaderProgramInterval);
	glValidateProgram(shaderProgramInterval);
	
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
	glOrtho(0, width, height, 0, 1, -1);
	glMatrixMode(GL_MODELVIEW);
	glEnable(GL_TEXTURE_2D);
	glEnable(GL_TEXTURE_1D);
	glUseProgram(shaderProgramInterval);
	genTexture(imageBuffer, width, height);
	while (!Display.isCloseRequested() && !closeRequested) 
	{
		checkInput();
		glClearColor(1.0f, 1.0f, 1.0f, 1f);
		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(shaderProgramInterval);
		Util.checkGLError();
		//glUniform1f(glGetUniformLocation(shaderProgramInterval, "scale"), 1.0f);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "from"), -128);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "to"), 127);
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "width"), width);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "height"), height);
		Util.checkGLError();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
		GL11.glBindTexture(GL_TEXTURE_2D, imageTextureId);
		
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture2"), palettes[paletteId] - 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + paletteId + 1);
		glBindTexture(GL_TEXTURE_1D, palettes[paletteId]);
		
		//========================================================================
			int scalingWidth = (int)(width * scale);
			int scalingHeight = (int)(height * scale);
			    
		glBegin(GL_QUADS);
			glTexCoord2d(0, 0);
			glVertex2i(0, 0);
		
			glTexCoord2d(1, 0);
			glVertex2i(scalingWidth, 0);
		
			glTexCoord2d(1, 1);
			glVertex2i(scalingWidth, scalingHeight);
		
			glTexCoord2d(0, 1);
			glVertex2i(0, scalingHeight);
		glEnd();
		
		
		
		ByteBuffer bytes = BufferUtils.createByteBuffer(width * height * 4);
	    //GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bytes);
	    glGetTexImage(GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, bytes);
		//========================================================================
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
	
	public static void destroy()
	{
		closeRequested = true;
	}
	
	private static IntBuffer genTexture(Object[] imageBuffer, int width, int height) {
		
		IntBuffer texture_object_handles = BufferUtils.createIntBuffer(5);
		glGenTextures(texture_object_handles);
		try{
		for (int i = 0; i < 5; i++) 
		{
			if(i == 0)
			{
				glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture1"), i);
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
				imageTextureId = texture_object_handles.get(i);
				glBindTexture(GL_TEXTURE_2D, imageTextureId);
				Util.checkGLError();
				glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				Util.checkGLError();
				ByteBuffer buffer = BufferUtils.createByteBuffer(imageBuffer.length);
				for(Object o : imageBuffer)
				{
					byte by = (byte)o;
					buffer.put((by));
				}
				buffer.flip();
				Util.checkGLError();
				glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R32I, width, height, 0, GL30.GL_RED_INTEGER, GL_BYTE, buffer);
			}
			else
			{
				GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
				ByteBuffer buf = null;
				PNGDecoder decoder = null;
				InputStream in = null;
				try {
					in = new FileInputStream("res/" + getPaletteTexture(i) + ".PNG");
				   decoder = new PNGDecoder(in);
				 
				   width = decoder.getWidth();
				   height = decoder.getHeight();
				 
				   buf = BufferUtils.createByteBuffer(4 * width * height);
				   decoder.decode(buf, width*4, PNGDecoder.TextureFormat.RGBA);
				   buf.flip();
				}
				catch(Exception e)
				{}
				finally
				{
					in.close();
				}
				palettes[i - 1] = texture_object_handles.get(i);

				glBindTexture(GL_TEXTURE_1D, palettes[i - 1]);
				Util.checkGLError();
				glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				Util.checkGLError();
				
	//			glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
				glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, decoder.getWidth(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
			}
			Util.checkGLError();
		}
		}
		catch(Exception e)
		{
		}
		return texture_object_handles;
	}
	
	private static String getPaletteTexture(int paletteType)
	{
		switch (paletteType) {
		case 1: {
			return "hotIron";
		}
		case 2: {
			return "pet";
		}
		case 3: {
			return "hotMetalBlue";
		}
		case 4: {
			return "pet20";
		}
		}
		return "hotIron";
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
	
	public static void changePalette(String paletteName)
	{
		paletteId = getPaletteId(paletteName);
		isUsePalette = true;
	}
	
	public static void notUsePalette()
	{
		isUsePalette = false;
	}
	
	public static void changeScale(float scale)
	{
		MainRender.scale += scale;
	}
	
	
	private static void exitOnGLError(String errorMessage) {
        int errorValue = GL11.glGetError();
         
        if (errorValue != GL11.GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);
             
            if (Display.isCreated()) Display.destroy();
            System.exit(-1);
        }
    }

	/**
	 * Создание шейдера
	 * @param shaderName - название
	 * @param isFragment - true, если фрагментный шейдер
	 * @return
	 */
	private static int createShader(String shaderName, boolean isFragment)
	{
		int shader = glCreateShader(isFragment ? GL_FRAGMENT_SHADER : GL_VERTEX_SHADER);
		glShaderSource(shader, createShaderSource("shaders/" + shaderName));
		glCompileShader(shader);
		if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println("Shader " + shaderName + " wasn't able to be compiled correctly.");
			System.err.println(glGetShaderInfoLog(shader, GL_INFO_LOG_LENGTH));
		}
		return shader;
	}
	
	/**
	 * Чтение шейдера из файла
	 * @param filePath - путь к файлу
	 * @return
	 */
	private static StringBuilder createShaderSource(String filePath)
	{
		StringBuilder shaderSource = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append('\n');
			}
		} catch (IOException e) {
			System.err.println("Shader " + filePath + " wasn't loaded properly.");
			e.printStackTrace();
			Display.destroy();
			System.exit(1);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return shaderSource;
	}

	private static void checkInput() {
		switch (paletteId) {
		case 0:
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				paletteId = 2;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				paletteId = 3;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				paletteId = 1;
			}
			break;
		case 1:
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				paletteId = 2;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				paletteId = 3;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				paletteId = 0;
			}
			break;
		case 2:
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				paletteId = 3;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				paletteId = 0;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				paletteId = 1;
			}
		case 3:
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				paletteId = 2;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				paletteId = 0;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
				paletteId = 1;
			}
		}
	}

	private static Texture loadTexture(String fileName) {
		try {
			Texture tex = TextureLoader.getTexture("PNG", new FileInputStream(new File("res/" + fileName + ".png")));
			return tex;

		} catch (FileNotFoundException e) {
			System.err.println("There is no file " + fileName + " in folder 'res'");
		} catch (IOException e) {
			System.err.println("Can't open file " + fileName);
		}
		return null;
	}

}