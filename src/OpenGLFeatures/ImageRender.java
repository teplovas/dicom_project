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
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.Util;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

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
	private static boolean isByte;

	private static int projectionMatrixLocation = 0;
	private static int viewMatrixLocation = 0;
	private static int modelMatrixLocation = 0;
	private static Matrix4f projectionMatrix = null;
	private static Matrix4f viewMatrix = null;
	private static Matrix4f modelMatrix = null;
	private static Vector3f modelPos = null;
	private static Vector3f modelAngle = null;
	private static Vector3f modelScale = null;
	private static Vector3f cameraPos = null;
	private static FloatBuffer matrix44Buffer = null;

	protected static void init(int shaderProgramInterval, int imageTextureId, int[] palettes, int disWidth,
			int disHeight) {
		ImageRender.shaderProgramInterval = shaderProgramInterval;
		ImageRender.imageTextureId = imageTextureId;
		ImageRender.palettes = palettes;
		ImageRender.disHeight = disHeight;
		ImageRender.disWidth = disWidth;

		projectionMatrixLocation = glGetUniformLocation(shaderProgramInterval, "projectionMatrix");
		viewMatrixLocation = glGetUniformLocation(shaderProgramInterval, "viewMatrix");
		modelMatrixLocation = glGetUniformLocation(shaderProgramInterval, "modelMatrix");
	}

	private static void setupMatrices() {
		
		modelPos = new Vector3f(0, 0, 0);
		modelAngle = new Vector3f(0, 0, 0);
		modelScale = new Vector3f(1, 1, 1);
		cameraPos = new Vector3f(0, 0, -1);
		
		// Setup projection matrix
		projectionMatrix = new Matrix4f();
		float fieldOfView = 60f;
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
		matrix44Buffer = BufferUtils.createFloatBuffer(16);
	}

	private static void calculateScale() {
		boolean isWGreate = width > disWidth;
		boolean isHGreate = height > disHeight;

		double ratio = (double) width / (double) height;

		if (isWGreate && isHGreate && width > height || isWGreate) {
			scaleWidth = disWidth;
			scaleHeight = (int) ((float) disWidth / ratio);
			return;
		} else if (isWGreate && isHGreate && width < height || isHGreate) {
			scaleHeight = disHeight;
			scaleWidth = (int) ((float) disHeight * ratio);
			return;
		}

		scaleHeight = height;
		scaleWidth = width;
	}

	protected static boolean bindImage(DicomImage img) {
		if (img.getImageBuffer() == null)
			return false;
		ImageRender.width = img.getWidth();
		ImageRender.height = img.getHeight();
		calculateScale();
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture1"), 6);
		Util.checkGLError();
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 6);
		Util.checkGLError();
		glBindTexture(GL_TEXTURE_2D, imageTextureId);
		Util.checkGLError();
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		Util.checkGLError();
		// boolean isByte = false;
		Buffer buffer;// = BufferUtils.createByteBuffer(imageBuffer.length);
		if (img.getImageBuffer()[0] instanceof Byte) {
			isByte = true;
			buffer = BufferUtils.createByteBuffer(img.getImageBuffer().length);
			for (Object o : img.getImageBuffer()) {
				byte by = (byte) o;
				((ByteBuffer) buffer).put((by));
			}
		} else {
			isByte = false;
			buffer = BufferUtils.createShortBuffer(img.getImageBuffer().length);
			for (Object o : img.getImageBuffer()) {
				short by = (short) o;
				((ShortBuffer) buffer).put((by));
			}
		}

		buffer.flip();
		Util.checkGLError();
		if (isByte) {
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R8I, width, height, 0, GL30.GL_RED_INTEGER, GL_BYTE,
					(ByteBuffer) buffer);
		} else {
			GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL30.GL_R32I, width, height, 0, GL30.GL_RED_INTEGER, GL_SHORT,
					(ShortBuffer) buffer);
		}
		setupMatrices();
		return true;
	}

	private static void transform(Boolean isZoom, boolean isRotate, float moveX, float moveY) {
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
		modelPos.y += moveY / 500f;
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

		projectionMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		GL20.glUniformMatrix4(projectionMatrixLocation, false, matrix44Buffer);

		viewMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		GL20.glUniformMatrix4(viewMatrixLocation, false, matrix44Buffer);

		modelMatrix.store(matrix44Buffer);
		matrix44Buffer.flip();
		GL20.glUniformMatrix4(modelMatrixLocation, false, matrix44Buffer);

		// GL20.glUseProgram(0);
	}

	protected static void renderImage(int from, int to, boolean isInvert, int paletteId, Boolean isZoom,
			boolean isRotate, float moveX, float moveY) {
		
		boolean isUsePallete = paletteId >= 0;
		glUseProgram(shaderProgramInterval);
		transform(isZoom, isRotate, moveX, moveY);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "from"), from);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "to"), to);
		Util.checkGLError();
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "width"), width);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "height"), height);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "isUsePalette"), isUsePallete ? 1 : 0);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "isByte"), isByte ? 1 : 0);
		glUniform1i(glGetUniformLocation(shaderProgramInterval, "isInvert"), isInvert ? 1 : 0);
		Util.checkGLError();

		GL13.glActiveTexture(GL13.GL_TEXTURE0 + 6);
		GL11.glBindTexture(GL_TEXTURE_2D, imageTextureId);

		int curPaletteId = isUsePallete ? paletteId : 0;

		glUniform1i(glGetUniformLocation(shaderProgramInterval, "texture2"), palettes[curPaletteId] - 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + curPaletteId);
		glBindTexture(GL_TEXTURE_1D, palettes[curPaletteId]);

		int scalingWidth = (int) (scaleWidth);
		int scalingHeight = (int) (scaleHeight);

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
