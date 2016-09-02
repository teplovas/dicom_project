package OpenGLFeatures;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ARBTextureRg;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

import net.sourceforge.fastpng.PNGDecoder;
import net.sourceforge.fastpng.PNGDecoder.TextureFormat;
import tools.PaletteLoader;

///** 
// * @author teplova.s 
// * */
public class MainRender {
	public static void main(String[] args){
		int width = 640;
		int height = 480;
		int shaderProgramInterval;
		try {
		Display.setDisplayMode(new DisplayMode(640, 480));
		Display.setTitle("DICOM");
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
	
//	ByteBuffer buf = ByteBuffer.allocateDirect(64*64);
//	for(int x = 0; x < 64; x++)
//		for(int y = 0; y < 64; y++)
//			buf.put(x + y * 64, (byte)(x ^ y));
//	glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, 64, 64, 0, GL_RED, GL_UNSIGNED_BYTE, buf);
		while (!Display.isCloseRequested()) {
//		checkInput();
		glClearColor(.5f, .5f, 0f, 1f);
		glClear(GL_COLOR_BUFFER_BIT);
		glUseProgram(shaderProgramInterval);
	    int locFrom = glGetUniformLocation(shaderProgramInterval, "from");
	    int locTo = glGetUniformLocation(shaderProgramInterval, "to");
	    glUniform1f(locFrom, .5f);
	    glUniform1f(locTo, .5f);
	    
	    
//		texture.bind();
//		render(0, 150, 1);		    
//		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 640, 480, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_TEXTURE_2D);
//		glEnable(GL_TEXTURE_1D);
		glColor3f(0f, 1f, 0f);
		genTexture();
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
//
		glUseProgram(0);
		Display.update();
		Display.sync(60);
	}
//	glDeleteProgram(shaderProgramPalette);
//	glDeleteShader(vertexShaderPalette);
//	glDeleteShader(fragmentShaderPalette);
//	
//	glDeleteProgram(shaderProgramInverse);
//	glDeleteShader(vertexShaderInvert);
//	glDeleteShader(fragmentShaderInvert);
//	
	glDeleteProgram(shaderProgramInterval);
	glDeleteShader(vertexShaderInterval);
	glDeleteShader(fragmentShaderInterval);
//	
	Display.destroy();
	System.exit(0);
		
	}

	private static void genTexture() {
		IntBuffer texture_object_handles = BufferUtils.createIntBuffer(1);
		glGenTextures(texture_object_handles);

		for (int i = 0; i < 1; ++i) {
			Util.checkGLError();
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
			Util.checkGLError();
			int id = texture_object_handles.get(i);
			glBindTexture(GL_TEXTURE_2D, id);
			Util.checkGLError();
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			Util.checkGLError();
			int size = 128;
			ByteBuffer buffer = BufferUtils.createByteBuffer(size * size);
			for(int k = 0; i < size; i++)
				for(int j = 0; j < size; j++)
					buffer.put(/*j + k*size, */(byte)(178));
			buffer.flip();
			Util.checkGLError();
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, size, size, 0, GL_RED, GL_UNSIGNED_BYTE, buffer);
			Util.checkGLError();
		}
	}
	
	//
//	private static Texture texture;
//
//	private static int width = 0;
//	private static int height = 0;
//	
//	private static float from;
//	private static float to;
//	static int texId;
//
//	private static enum State {
//		NORMAL, HOT_IRON, HOT_METAL_BLUE, PET, PET20;
//	}
//
//	private static State state = State.NORMAL;
//	
//	/**
//	 * Шейдер для применения палитр
//	 */
//	private static int shaderProgramPalette;
//	/**
//	 * Шейдер для инвертирования цвета
//	 */
//	private static int shaderProgramInverse;
//	/**
//	 * Шейдер для применения границ окна
//	 */
//	private static int shaderProgramInterval;
//
//	
//	public static void createDisplay(Object[] imageBuffer, int height, int width)
//	{
//		try {
//			Display.setDisplayMode(new DisplayMode(width, height));
//			Display.setTitle("DICOM");
//			Display.create();
//			texture = createTexture(imageBuffer, width, height);
//			//texture = loadTexture("pic");
//		} catch (LWJGLException e) {
//			System.err.println("The display wasn't initialized correctly. :(");
//			Display.destroy();
//			System.exit(1);
//		}
//	}
//	
/////*	public static Texture createTexture(Object[] imageBuffer, int width, int height)
////	{
////		InputStream in = null;
////		ByteBuffer buf = null;
////		try {
////		in = new FileInputStream("res/pic.png");
////		   PNGDecoder decoder = new PNGDecoder(in);
////		 
////		   width = decoder.getWidth();
////		   height = decoder.getHeight();
////		   System.out.println("width="+decoder.getWidth());
////		   System.out.println("height="+decoder.getHeight());
////		 
////		   buf = ByteBuffer.allocateDirect(3*decoder.getWidth()*decoder.getHeight());
////		   decoder.decode(buf, decoder.getWidth()*3, TextureFormat.RGB);
////		   buf.flip();
////		}
////		catch(Exception e)
////		{
////			
////		}
////		finally {
////		   try {
////			in.close();
////		} catch (IOException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////		}
////		
////		/*ByteBuffer buffer = BufferUtils.createByteBuffer(imageBuffer.length);
////		int k = 0;
////		for(Object b : imageBuffer)
////		{
////				//int rgb = ((byte)imageBuffer[k]&0x0ff)<<16|((byte)imageBuffer[k++]&0x0ff)<<8|((byte)imageBuffer[k++]&0x0ff);
////				//buffer.put((byte)((byte)b & 0x0ff));
////				buffer.put((byte)0.1);
////		}
////		buffer.flip();*/
////		/*Util.checkGLError();
////		texId = GL11.glGenTextures();
////		TextureImpl texture = new TextureImpl("", GL_TEXTURE_2D, texId);
////		 GL11.glEnable(GL_TEXTURE_2D);
////
////		 GL11.glBindTexture(GL_TEXTURE_2D, texId);
////		 Util.checkGLError();
////		 texture.setTextureHeight(height);
////		 texture.setTextureWidth(width);
////		 texture.setAlpha(false);
////		 Util.checkGLError();
////		 GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL_NEAREST);
////	     GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_NEAREST);
////	     Util.checkGLError();*/
//////		GL13.glActiveTexture(GL13.GL_TEXTURE0);
//////		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
//////		glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
//////		glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
////		
//////		GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
//////        GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
//// /*       Util.checkGLError();
////        glBindTexture(GL_TEXTURE_2D, texId);
////        Util.checkGLError();
////              glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
////              Util.checkGLError();
////              glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
////              Util.checkGLError();
////              glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
////              Util.checkGLError();
////              glTexParameteri(GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
////              Util.checkGLError();
////                  glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, buf);
////        /*GL11.glTexImage2D(GL_TEXTURE_2D,
////                0,
////                GL_RGB8,
////                texture.getTextureWidth(),
////                texture.getTextureHeight(),
////                0,
////                GL_RGB8,
////                GL_BYTE,
////                buf);*/
////  /*      Util.checkGLError();
////        return texture;
////		 
////		// All RGB bytes are aligned to each other and each component is 1 byte
////		//GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
////		
////		//GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, width, height, 0, GL_RGB8, GL_BYTE, buffer);/*GL11.GL_TEXTURE_2D, 0, ARBTextureRg.GL_RG8, width, height, 0, 
////		//		GL11.GL_RED, GL11.GL_BYTE, buffer);*/
////		//GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
////	}
//
//	
//	public static void destroyRender()
//	{
//		
//	}
//	
////	public static void loadAndPrepareShaders(int from, int to, int paletteNumber)
////	{
////		shaderProgramPalette = glCreateProgram();
////		shaderProgramInverse = glCreateProgram();
////		shaderProgramInterval = glCreateProgram();
////		
////		int fragmentShaderPalette = createShader("shaderPalette.frag", true);
////		int vertexShaderPalette = createShader("shaderPalette.vert", false);
////		
////		int fragmentShaderInvert = createShader("shaderInvert.frag", true);
////		int vertexShaderInvert = createShader("shaderInvert.vert", false);
////		
////		int fragmentShaderInterval = createShader("shaderWindow.frag", true);
////		int vertexShaderInterval = createShader("shaderWindow.vert", false);
////		
////		glAttachShader(shaderProgramPalette, vertexShaderPalette);
////		glAttachShader(shaderProgramPalette, fragmentShaderPalette);
////		
////		glAttachShader(shaderProgramInverse, vertexShaderInvert);
////		glAttachShader(shaderProgramInverse, fragmentShaderInvert);
////		
////		glAttachShader(shaderProgramInterval, vertexShaderInterval);
////		glAttachShader(shaderProgramInterval, fragmentShaderInterval);
////		
////		glLinkProgram(shaderProgramPalette);
////		glValidateProgram(shaderProgramPalette);
////		
////		glLinkProgram(shaderProgramInverse);
////		glValidateProgram(shaderProgramInverse);
////		
////		glLinkProgram(shaderProgramInterval);
////		glValidateProgram(shaderProgramInterval);
////				
////		glMatrixMode(GL_PROJECTION);
////		glLoadIdentity();
////		glOrtho(0, 640, 480, 0, 1, -1);
////		glMatrixMode(GL_MODELVIEW);
////		glEnable(GL_TEXTURE_2D);
////		//glEnable(GL_TEXTURE_1D);
////
////		while (!Display.isCloseRequested()) {
////			checkInput();
////			glClear(GL_COLOR_BUFFER_BIT);
////			Util.checkGLError();
////			//texture.bind();
////			glBindTexture(GL_TEXTURE_2D, texId);
////			Util.checkGLError();
////			render(from, to, paletteNumber);
////			Util.checkGLError();
////			
//////+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++		    
//////		    int tex = glGenTextures();
//////		    glBindTexture(GL_TEXTURE_1D, tex);
//////		       
//////		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//////		    glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//////		 
//////		    GL11.glTexImage1D(GL_TEXTURE_1D, 0, ARBTextureRg.GL_R16F, 0, 768, GL11.GL_RED, GL11.GL_FLOAT, getPalette(1));
//////		    int paletteTexture = glGetUniformLocation(shaderProgramPalette, "texture2");
//////		    glUniform1i(paletteTexture, tex);
//////+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
////			glBegin(GL_QUADS);
////				glTexCoord2d(0, 0);
////				glVertex2i(0, 0);
////	
////				glTexCoord2d(1, 0);
////				glVertex2i(width, 0);
////	
////				glTexCoord2d(1, 1);
////				glVertex2i(width, height);
////	
////				glTexCoord2d(0, 1);
////				glVertex2i(0, height);
////			glEnd();
////
////			glUseProgram(0);
////			Display.update();
////			Display.sync(60);
////		}
////		glDeleteProgram(shaderProgramPalette);
////		glDeleteShader(vertexShaderPalette);
////		glDeleteShader(fragmentShaderPalette);
////		
////		glDeleteProgram(shaderProgramInverse);
////		glDeleteShader(vertexShaderInvert);
////		glDeleteShader(fragmentShaderInvert);
////		
////		glDeleteProgram(shaderProgramInterval);
////		glDeleteShader(vertexShaderInterval);
////		glDeleteShader(fragmentShaderInterval);
////		
////		Display.destroy();
////		System.exit(0);
////	}
//	
//	public static void main(String[] args) {
//		
//		texture = loadTexture("pic");
//
//		shaderProgramPalette = glCreateProgram();
//		shaderProgramInverse = glCreateProgram();
//		shaderProgramInterval = glCreateProgram();
//		
//		int fragmentShaderPalette = createShader("shaderPalette.frag", true);
//		int vertexShaderPalette = createShader("shaderPalette.vert", false);
//		
//		int fragmentShaderInvert = createShader("shaderInvert.frag", true);
//		int vertexShaderInvert = createShader("shaderInvert.vert", false);
//		
//		int fragmentShaderInterval = createShader("shaderWindow.frag", true);
//		int vertexShaderInterval = createShader("shaderWindow.vert", false);
//		
//		glAttachShader(shaderProgramPalette, vertexShaderPalette);
//		glAttachShader(shaderProgramPalette, fragmentShaderPalette);
//		
//		glAttachShader(shaderProgramInverse, vertexShaderInvert);
//		glAttachShader(shaderProgramInverse, fragmentShaderInvert);
//		
//		glAttachShader(shaderProgramInterval, vertexShaderInterval);
//		glAttachShader(shaderProgramInterval, fragmentShaderInterval);
//		
//		glLinkProgram(shaderProgramPalette);
//		glValidateProgram(shaderProgramPalette);
//		
//		glLinkProgram(shaderProgramInverse);
//		glValidateProgram(shaderProgramInverse);
//		
//		glLinkProgram(shaderProgramInterval);
//		glValidateProgram(shaderProgramInterval);
//				
//		glMatrixMode(GL_PROJECTION);
//		glLoadIdentity();
//		glOrtho(0, 640, 480, 0, 1, -1);
//		glMatrixMode(GL_MODELVIEW);
//		glEnable(GL_TEXTURE_2D);
//		glEnable(GL_TEXTURE_1D);
//
//	
	/**
	 * Создание шейдера
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
//	
//	public static FloatBuffer getPalette(int palette)
//	{
//		int [][] array = PaletteLoader.getPalette(palette);
//		float[] buffer = new float[1024];
//		int k = 0;
//		for(int i = 0; i < array.length; i++)
//		{
//			for(int j = 0; j < 3; j++)
//			{
//				buffer[k] = (float)array[i][j];
//				k++;
//			}
//		}
//		// до степени 2
////		for(int i = 0; i < 1024 - array.length * 3; i++)
////		{
////			buffer[k] = 0;
////			k++;
////		}
//		FloatBuffer paletteBuffer = BufferUtils.createFloatBuffer(buffer.length);
//		for(float f: buffer)
//		{
//			paletteBuffer.put(f);
//		}
//		paletteBuffer.rewind();
//		return paletteBuffer;
//	}
//
//	private static void render(int from, int to, int paletteNumber) {
//		switch (state) {
//		case NORMAL:
//			glUseProgram(shaderProgramInterval);
//		    int locFrom = glGetUniformLocation(shaderProgramInterval, "from");
//		    int locTo = glGetUniformLocation(shaderProgramInterval, "to");
//		    glUniform1f(locFrom, (float)from);
//		    glUniform1f(locTo, (float)to);
//			break;
//		case HOT_IRON:
//			glUseProgram(shaderProgramPalette);
//			int loc = glGetUniformLocation(shaderProgramPalette, "palette");
//		    glUniform1(loc, getPalette(1));
//		    break;
//		case HOT_METAL_BLUE:	
//			glUseProgram(shaderProgramInterval);
//		    //int locFrom = glGetUniformLocation(shaderProgramInterval, "from");
//		    //int locTo = glGetUniformLocation(shaderProgramInterval, "to");
//		    //glUniform1f(locFrom, (float)from);
//		    //glUniform1f(locTo, (float)to);
//			break;
//		case PET:		    
//		    loc = glGetUniformLocation(shaderProgramPalette, "palette");
//		    glUniform1(loc, getPalette(3));
//			break;
//		case PET20:		    
//		    loc = glGetUniformLocation(shaderProgramPalette, "palette");
//		    glUniform1(loc, getPalette(4));
//			break;
//		}
//		
//	}
//
//	private static void checkInput() {
//		switch (state) {
//		case NORMAL:
//			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
//				state = State.HOT_IRON;
//			}
//			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
//				state = State.HOT_METAL_BLUE;
//			}
//			break;
//		case HOT_IRON:
//			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
//				state = State.NORMAL;
//			}
//			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
//				state = State.HOT_METAL_BLUE;
//			}
//			break;
//		case HOT_METAL_BLUE:
//			if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
//				state = State.NORMAL;
//			}
//			if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
//				state = State.HOT_IRON;
//			}
//		}
//	}
//
////	private static Texture loadTexture(String fileName) {
////		try {
////			Texture tex = TextureLoader.getTexture("PNG", new FileInputStream(new File("res/" + fileName + ".png")));
////			width = tex.getImageWidth();
////			height = tex.getImageHeight();
////			return tex;
////
////		} catch (FileNotFoundException e) {
////			System.err.println("There is no file " + fileName + " in folder 'res'");
////		} catch (IOException e) {
////			System.err.println("Can't open file " + fileName);
////		}
////		return null;
////	}
//	

}