package OpenGLFeatures;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import tools.PaletteLoader;

/** @author teplova.s */
public class MainRender {

	private static Texture texture;

	private static int width = 0;
	private static int height = 0;
	
	private static float from;
	private static float to;

	private static enum State {
		NORMAL, HOT_IRON, HOT_METAL_BLUE, PET, PET20;
	}

	private static State state = State.NORMAL;
	
	/**
	 * Шейдер для применения палитр
	 */
	private static int shaderProgramPalette;
	/**
	 * Шейдер для инвертирования цвета
	 */
	private static int shaderProgramInverse;
	/**
	 * Шейдер для применения границ окна
	 */
	private static int shaderProgramInterval;

	
	public static void createDisplay(int height, int width)
	{
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.setTitle("DICOM");
			Display.create();
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialized correctly. :(");
			Display.destroy();
			System.exit(1);
		}
	}
	
	public static void createTexture(float[] imageBuffer)
	{
		
	}
	
	public static void renderImage(int from, int to, int paletteNumber)
	{
		while (!Display.isCloseRequested()) {
			checkInput();
			glClear(GL_COLOR_BUFFER_BIT);
			texture.bind();
			render(from, to, paletteNumber);		    
			
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		    
//		    int tex = glGenTextures();
//		    glBindTexture(GL_TEXTURE_1D, tex);
//		       
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		 
//		    GL11.glTexImage1D(GL_TEXTURE_1D, 0, ARBTextureRg.GL_R16F, 0, 768, GL11.GL_RED, GL11.GL_FLOAT, getPalette(1));
//		    int paletteTexture = glGetUniformLocation(shaderProgramPalette, "texture2");
//		    glUniform1i(paletteTexture, tex);
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2i(0, 0);
	
				glTexCoord2d(1, 0);
				glVertex2i(width, 0);
	
				glTexCoord2d(1, 1);
				glVertex2i(width, height);
	
				glTexCoord2d(0, 1);
				glVertex2i(0, height);
			glEnd();

			glUseProgram(0);
			Display.update();
			Display.sync(60);
		}
	}
	
	public static void destroyRender()
	{
		
	}
	
	public static void loadAndPrepareShaders(int from, int to, int paletteNumber)
	{
		shaderProgramPalette = glCreateProgram();
		shaderProgramInverse = glCreateProgram();
		shaderProgramInterval = glCreateProgram();
		
		int fragmentShaderPalette = createShader("shaderPalette.frag", true);
		int vertexShaderPalette = createShader("shaderPalette.vert", false);
		
		int fragmentShaderInvert = createShader("shaderInvert.frag", true);
		int vertexShaderInvert = createShader("shaderInvert.vert", false);
		
		int fragmentShaderInterval = createShader("shaderWindow.frag", true);
		int vertexShaderInterval = createShader("shaderWindow.vert", false);
		
		glAttachShader(shaderProgramPalette, vertexShaderPalette);
		glAttachShader(shaderProgramPalette, fragmentShaderPalette);
		
		glAttachShader(shaderProgramInverse, vertexShaderInvert);
		glAttachShader(shaderProgramInverse, fragmentShaderInvert);
		
		glAttachShader(shaderProgramInterval, vertexShaderInterval);
		glAttachShader(shaderProgramInterval, fragmentShaderInterval);
		
		glLinkProgram(shaderProgramPalette);
		glValidateProgram(shaderProgramPalette);
		
		glLinkProgram(shaderProgramInverse);
		glValidateProgram(shaderProgramInverse);
		
		glLinkProgram(shaderProgramInterval);
		glValidateProgram(shaderProgramInterval);
				
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 640, 480, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_TEXTURE_1D);

		while (!Display.isCloseRequested()) {
			checkInput();
			glClear(GL_COLOR_BUFFER_BIT);
			texture.bind();
			render(from, to, paletteNumber);		    
			
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		    
//		    int tex = glGenTextures();
//		    glBindTexture(GL_TEXTURE_1D, tex);
//		       
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		 
//		    GL11.glTexImage1D(GL_TEXTURE_1D, 0, ARBTextureRg.GL_R16F, 0, 768, GL11.GL_RED, GL11.GL_FLOAT, getPalette(1));
//		    int paletteTexture = glGetUniformLocation(shaderProgramPalette, "texture2");
//		    glUniform1i(paletteTexture, tex);
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2i(0, 0);
	
				glTexCoord2d(1, 0);
				glVertex2i(width, 0);
	
				glTexCoord2d(1, 1);
				glVertex2i(width, height);
	
				glTexCoord2d(0, 1);
				glVertex2i(0, height);
			glEnd();

			glUseProgram(0);
			Display.update();
			Display.sync(60);
		}
		glDeleteProgram(shaderProgramPalette);
		glDeleteShader(vertexShaderPalette);
		glDeleteShader(fragmentShaderPalette);
		
		glDeleteProgram(shaderProgramInverse);
		glDeleteShader(vertexShaderInvert);
		glDeleteShader(fragmentShaderInvert);
		
		glDeleteProgram(shaderProgramInterval);
		glDeleteShader(vertexShaderInterval);
		glDeleteShader(fragmentShaderInterval);
		
		Display.destroy();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		try {
			Display.setDisplayMode(new DisplayMode(640, 480));
			Display.setTitle("DICOM");
			Display.create();
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialized correctly. :(");
			Display.destroy();
			System.exit(1);
		}
		
		texture = loadTexture("pic");

		shaderProgramPalette = glCreateProgram();
		shaderProgramInverse = glCreateProgram();
		shaderProgramInterval = glCreateProgram();
		
		int fragmentShaderPalette = createShader("shaderPalette.frag", true);
		int vertexShaderPalette = createShader("shaderPalette.vert", false);
		
		int fragmentShaderInvert = createShader("shaderInvert.frag", true);
		int vertexShaderInvert = createShader("shaderInvert.vert", false);
		
		int fragmentShaderInterval = createShader("shaderWindow.frag", true);
		int vertexShaderInterval = createShader("shaderWindow.vert", false);
		
		glAttachShader(shaderProgramPalette, vertexShaderPalette);
		glAttachShader(shaderProgramPalette, fragmentShaderPalette);
		
		glAttachShader(shaderProgramInverse, vertexShaderInvert);
		glAttachShader(shaderProgramInverse, fragmentShaderInvert);
		
		glAttachShader(shaderProgramInterval, vertexShaderInterval);
		glAttachShader(shaderProgramInterval, fragmentShaderInterval);
		
		glLinkProgram(shaderProgramPalette);
		glValidateProgram(shaderProgramPalette);
		
		glLinkProgram(shaderProgramInverse);
		glValidateProgram(shaderProgramInverse);
		
		glLinkProgram(shaderProgramInterval);
		glValidateProgram(shaderProgramInterval);
				
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 640, 480, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
		glEnable(GL_TEXTURE_1D);

		while (!Display.isCloseRequested()) {
			checkInput();
			glClear(GL_COLOR_BUFFER_BIT);
			texture.bind();
			//render();		    
			
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		    
//		    int tex = glGenTextures();
//		    glBindTexture(GL_TEXTURE_1D, tex);
//		       
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//		 
//		    GL11.glTexImage1D(GL_TEXTURE_1D, 0, ARBTextureRg.GL_R16F, 0, 768, GL11.GL_RED, GL11.GL_FLOAT, getPalette(1));
//		    int paletteTexture = glGetUniformLocation(shaderProgramPalette, "texture2");
//		    glUniform1i(paletteTexture, tex);
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			glBegin(GL_QUADS);
				glTexCoord2d(0, 0);
				glVertex2i(0, 0);
	
				glTexCoord2d(1, 0);
				glVertex2i(width, 0);
	
				glTexCoord2d(1, 1);
				glVertex2i(width, height);
	
				glTexCoord2d(0, 1);
				glVertex2i(0, height);
			glEnd();

			glUseProgram(0);
			Display.update();
			Display.sync(60);
		}
		glDeleteProgram(shaderProgramPalette);
		glDeleteShader(vertexShaderPalette);
		glDeleteShader(fragmentShaderPalette);
		
		glDeleteProgram(shaderProgramInverse);
		glDeleteShader(vertexShaderInvert);
		glDeleteShader(fragmentShaderInvert);
		
		glDeleteProgram(shaderProgramInterval);
		glDeleteShader(vertexShaderInterval);
		glDeleteShader(fragmentShaderInterval);
		
		Display.destroy();
		System.exit(0);
	}
	
	/**
	 * Создание шедера
	 * @param shaderName - название
	 * @param isFragment - true, если фрагментный шейдер
	 * @return
	 */
	private static int createShader(String shaderName, boolean isFragment)
	{
		int shader = glCreateShader(isFragment ? GL_FRAGMENT_SHADER : GL_VERTEX_SHADER);
		glShaderSource(shader, createShaderSource("src/OpenGLFeatures/" + shaderName));
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
	
	public static FloatBuffer getPalette(int palette)
	{
		int [][] array = PaletteLoader.getPalette(palette);
		float[] buffer = new float[1024];
		int k = 0;
		for(int i = 0; i < array.length; i++)
		{
			for(int j = 0; j < 3; j++)
			{
				buffer[k] = (float)array[i][j];
				k++;
			}
		}
		// до степени 2
//		for(int i = 0; i < 1024 - array.length * 3; i++)
//		{
//			buffer[k] = 0;
//			k++;
//		}
		FloatBuffer paletteBuffer = BufferUtils.createFloatBuffer(buffer.length);
		for(float f: buffer)
		{
			paletteBuffer.put(f);
		}
		paletteBuffer.rewind();
		return paletteBuffer;
	}

	private static void render(int from, int to, int paletteNumber) {
		switch (state) {
		case NORMAL:
			//glUseProgram(shaderProgramInverse);
			break;
		case HOT_IRON:
			glUseProgram(shaderProgramPalette);
			int loc = glGetUniformLocation(shaderProgramPalette, "palette");
		    glUniform1(loc, getPalette(1));
		    break;
		case HOT_METAL_BLUE:	
			glUseProgram(shaderProgramInterval);
		    int locFrom = glGetUniformLocation(shaderProgramInterval, "from");
		    int locTo = glGetUniformLocation(shaderProgramInterval, "to");
		    glUniform1f(locFrom, (float)from);
		    glUniform1f(locTo, (float)to);
			break;
		case PET:		    
		    loc = glGetUniformLocation(shaderProgramPalette, "palette");
		    glUniform1(loc, getPalette(3));
			break;
		case PET20:		    
		    loc = glGetUniformLocation(shaderProgramPalette, "palette");
		    glUniform1(loc, getPalette(4));
			break;
		}
		
	}

	private static void checkInput() {
		switch (state) {
		case NORMAL:
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				state = State.HOT_IRON;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				state = State.HOT_METAL_BLUE;
			}
			break;
		case HOT_IRON:
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				state = State.NORMAL;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
				state = State.HOT_METAL_BLUE;
			}
			break;
		case HOT_METAL_BLUE:
			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
				state = State.NORMAL;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
				state = State.HOT_IRON;
			}
		}
	}

	private static Texture loadTexture(String fileName) {
		try {
			Texture tex = TextureLoader.getTexture("PNG", new FileInputStream(new File("res/" + fileName + ".png")));
			width = tex.getImageWidth();
			height = tex.getImageHeight();
			return tex;

		} catch (FileNotFoundException e) {
			System.err.println("There is no file " + fileName + " in folder 'res'");
		} catch (IOException e) {
			System.err.println("Can't open file " + fileName);
		}
		return null;
	}
	
//	private static Texture createTexture(String fileName) {
//		try {
//			width = tex.getImageWidth();
//			height = tex.getImageHeight();
//			return tex;
//
//		} catch (FileNotFoundException e) {
//			System.err.println("There is no file " + fileName + " in folder 'res'");
//		} catch (IOException e) {
//			System.err.println("Can't open file " + fileName);
//		}
//		return null;
//	}
}