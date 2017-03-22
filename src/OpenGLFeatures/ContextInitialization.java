package OpenGLFeatures;

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
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glTexImage1D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
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
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Util;

import net.sourceforge.fastpng.PNGDecoder;

public class ContextInitialization {
	
	private static int imageShaderProgram;
	private static int imageFragmentShader;
	private static int imageVertexShader;
	
	private static int measureShaderProgram;
	private static int measureFragmentShader;
	private static int measureVertexShader;
	
	private static int imageTextureId = 0;
	private static int[] palettes = new int[4];
	private static int displayWidth;
	private static int displayHeight;
	
	private static void initDisplay(Canvas canv) {
		File JGLLib = new File("native/");

		System.setProperty("org.lwjgl.librarypath", JGLLib.getAbsolutePath());
		try {
			Display.setParent(canv);
			
			Display.create();
			Display.setVSyncEnabled(true);
			displayHeight = canv.getHeight();
			displayWidth = canv.getWidth();
			System.out.println("width = " + displayWidth + ", height = " + displayHeight);
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialized correctly. :(");
			Display.destroy();
			System.exit(1);
		}
		
		MeasurementsRender.initFont();
		AdditionalInfoRender.initFont();
	}
	
	private static void loadShadersAndPallettes()
	{
		//===============image shaders=================================
		imageShaderProgram = glCreateProgram();
		
		imageFragmentShader = createShader("shaderWindow.frag", true);
		imageVertexShader = createShader("shaderWindow.vert", false);
		
		glAttachShader(imageShaderProgram, imageVertexShader);
		glAttachShader(imageShaderProgram, imageFragmentShader);
		
		GL20.glBindAttribLocation(imageShaderProgram, 0, "in_Position");
		
		glLinkProgram(imageShaderProgram);
		glValidateProgram(imageShaderProgram);
		
		//==============measure shaders=================================
		measureShaderProgram = glCreateProgram();
		
		//measureFragmentShader = createShader("shaderMeasure.frag", true);
		measureVertexShader = createShader("shaderMeasure.vert", false);
		
		glAttachShader(measureShaderProgram, measureVertexShader);
		//glAttachShader(measureShaderProgram, measureFragmentShader);
		
		glLinkProgram(measureShaderProgram);
		glValidateProgram(measureShaderProgram);
		//==============================================================
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, displayWidth, displayHeight, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_TEXTURE_1D);
		glUseProgram(imageShaderProgram);
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
	
	public static int getImageShaderProgram() {
		return imageShaderProgram;
	}
	
	public static int getMeasureShaderProgram() {
		return measureShaderProgram;
	}

	public static int[] getPalettes() {
		return palettes;
	}

	public static void setPalettes(int[] palettes) {
		ContextInitialization.palettes = palettes;
	}
	
	public static void init(Canvas canv)
	{
		initDisplay(canv);
		loadShadersAndPallettes();
	}
	
	public static void destroyContext()
	{
		GL11.glDeleteTextures(palettes[0]);
		GL11.glDeleteTextures(palettes[1]);
		GL11.glDeleteTextures(palettes[2]);
		GL11.glDeleteTextures(palettes[3]);
		glDeleteProgram(imageShaderProgram);
		glDeleteShader(imageFragmentShader);
		glDeleteShader(imageVertexShader);
		glDeleteProgram(measureShaderProgram);
		glDeleteShader(measureFragmentShader);
		glDeleteShader(measureVertexShader);
	}

}
