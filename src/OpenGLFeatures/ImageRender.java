package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.GL_BYTE;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glVertex2i;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Util;

import tools.DicomImage;

public class ImageRender {
	private static int shaderProgramInterval;
	private static int imageTextureId = 0;
	private static int[] palettes;
	private static int width;
	private static int height;
	private static int disWidth;
	private static int disHeight;
	private static int scaleWidth;
	private static int scaleHeight;
	private static DicomImage image;
	
	
	protected static void init(int shaderProgramInterval, int imageTextureId, int[] palettes,
			int disWidth, int disHeight) 
	{
		ImageRender.shaderProgramInterval = shaderProgramInterval;
		ImageRender.imageTextureId = imageTextureId;
		ImageRender.palettes = palettes;
		ImageRender.disHeight = disHeight;
		ImageRender.disWidth = disWidth;
	}
	
	private static void calculateScale()
	{
		boolean isWGreate = width > disWidth;
		boolean isHGreate = height > disHeight;
		
		double ratio = (double)width / (double)height;
		
		if(isWGreate && isHGreate && width > height || isWGreate)
		{
			scaleWidth = disWidth;
			scaleHeight = (int)((float)disWidth / ratio);
			return;
		}
		else if(isWGreate && isHGreate && width < height || isHGreate)
		{
			scaleHeight = disHeight;
			scaleWidth = (int)((float)disHeight * ratio);
			return;
		}
				
		scaleHeight = height;
		scaleWidth = width;
	}

	protected static boolean bindImage(DicomImage img)
	{
		if(img.getImageBuffer() == null)
			return false;
		ImageRender.width = img.getWidth();
		ImageRender.height = img.getHeight();
		calculateScale();
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture1"), 4);
		Util.checkGLError();
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
		Util.checkGLError();
		glBindTexture(GL_TEXTURE_2D, imageTextureId);
		Util.checkGLError();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		Util.checkGLError();
		//boolean isByte = false;
		Buffer buffer;// = BufferUtils.createByteBuffer(imageBuffer.length);
		boolean isByte = false;
		if(img.getImageBuffer()[0] instanceof Byte)
		{
			isByte = true;
			buffer = BufferUtils.createByteBuffer(img.getImageBuffer().length);
			for(Object o : img.getImageBuffer())
			{
				byte by = (byte)o;
				((ByteBuffer)buffer).put((by));
			}
		}
		else
		{
			isByte = false;
			buffer = BufferUtils.createShortBuffer(img.getImageBuffer().length);
			for(Object o : img.getImageBuffer())
			{
				short by = (short)o;
				((ShortBuffer)buffer).put((by));
			}
		}
		
		buffer.flip();
		Util.checkGLError();
		if(isByte)
		{
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R8I, width, height, 0, GL30.GL_RED_INTEGER, 
					GL_BYTE, (ByteBuffer)buffer);
		}
		else
		{
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R32I, width, height, 0, GL30.GL_RED_INTEGER, 
					GL_SHORT, (ShortBuffer)buffer);
		}
		return true;
	}
	
	protected static void renderImage(int from, int to, boolean isInvert, int paletteId, float scale)
	{
		glUseProgram(shaderProgramInterval);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "from"), from);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "to"), to);
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "width"), width);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "height"), height);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "isUsePalette"), paletteId > 0 ? 1 : 0);
		//glUniform1i(glGetUniformLocation(shaderProgramInterval, "isByte"), isByte ? 1 : 0);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "isInvert"), isInvert ? 1 : 0);
		Util.checkGLError();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
		GL11.glBindTexture(GL_TEXTURE_2D, imageTextureId);

		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture2"), palettes[paletteId] - 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + paletteId);
		glBindTexture(GL_TEXTURE_1D, palettes[paletteId]);

		int scalingWidth = (int) (scaleWidth * scale);
		int scalingHeight = (int) (scaleHeight * scale);
		
		int shiftW = (disWidth - scalingWidth) / 2;
		int shiftH = (disHeight - scalingHeight) / 2;
		
		glEnable(GL_TEXTURE_2D);
		glBegin(GL_QUADS);
		glTexCoord2d(0, 0);
		glVertex2i(shiftW, shiftH);

		glTexCoord2d(1, 0);
		glVertex2i(scalingWidth + shiftW, shiftH);

		glTexCoord2d(1, 1);
		glVertex2i(scalingWidth + shiftW, scalingHeight + shiftH);

		glTexCoord2d(0, 1);
		glVertex2i(shiftW, scalingHeight + shiftH);
		glEnd();
		
		glUseProgram(0);
	}
}
