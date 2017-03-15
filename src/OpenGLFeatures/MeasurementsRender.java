package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

public class MeasurementsRender {
	
	private final static double MAX_DISTANCE = 3d; 
	private static List<Measure> measurements = new ArrayList<Measure>();
	private static List<Measure> measurementsToDelete = new ArrayList<Measure>();
	private static int curX = 0;
	private static int curY = 0;
	private static boolean isLineDrawing = false;
	private static int disWidth;
	private static int disHeight;
	private static TrueTypeFont font;
	private static Float pixelSpacing = 1.f;
	private static Point beginPos = null;
	private static boolean isMousePressed = false;
	private static boolean isMousePosChanged = false;
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
	private static Vector3f scaleAddResolution = new Vector3f(0.1f, 0.1f, 0.1f);
	private static Vector3f scaleMinusResolution = new Vector3f(-0.1f, -0.1f, -0.1f);
	private static float rotationDelta = 90f;
	
	private static int programId;
	
	protected static void init(int disWidth, int disHeight, int pId)
	{
		MeasurementsRender.disHeight = disHeight;
		MeasurementsRender.disWidth = disWidth;
		MeasurementsRender.programId = pId;
		
		//initFont();
		
		projectionMatrixLocation = glGetUniformLocation(programId, "projectionMatrix");
		viewMatrixLocation = glGetUniformLocation(programId, "viewMatrix");
		modelMatrixLocation = glGetUniformLocation(programId, "modelMatrix");
		setupMatrices();
	}
	
	public static void initFont()
	{
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 14);
		font = new TrueTypeFont(awtFont, true);
	}
	
	private static void setupMatrices() {
		modelPos = new Vector3f(0, 0, 0);
		modelAngle = new Vector3f(0, 0, 0);
		modelScale = new Vector3f(1, 1, 1);
		cameraPos = new Vector3f(0, 0, -1);
		// Setup projection matrix
		projectionMatrix = new Matrix4f();
		float fieldOfView = 60f;
		float aspectRatio = (float) 1024 / (float) 1024;
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
	
	private static void transform(Boolean isZoom, boolean isRotate, float moveX, float moveY) {
		// -- Input processing
		if (isZoom != null) {
			Vector3f.add(modelScale, isZoom ? scaleAddResolution : scaleMinusResolution, modelScale);
		}
		modelPos.y += moveY / 500f;
		modelPos.x += moveX / 500f;
		
		if (isRotate) {
			modelAngle.z += rotationDelta;
		}

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
	}
	
	
	protected static void renderMeasurements(float scale, boolean isMesurements, Boolean isZoom, boolean isRotate, 
			float moveX, float moveY)
	{
		glUseProgram(programId);
		transform(isZoom, isRotate, moveX, moveY);
		checkKeyPressed();
		for(Measure m : measurements)
		{
			printMesureInfo(m, scale);
		}
		
		if(isMesurements)
		{
			doMouseClick();
		}
		glUseProgram(0);
	}
	
	private static void checkKeyPressed()
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_DELETE))
		{
			measurements.removeAll(measurementsToDelete);
			measurementsToDelete.clear();
		}
	}
	
	
	private static void doMouseClick()
	{
		if(Mouse.isButtonDown(0))
		{
			isMousePressed = true;
			if(isMousePosChanged)
			{
				if(!isLineDrawing)
				{
					curX = Mouse.getX();
					curY = disHeight - Mouse.getY();
				}
				if(curX != Mouse.getX() && curY != Mouse.getY())
				drawLine(curX, curY, Mouse.getX(), disHeight - Mouse.getY(), false);
				isLineDrawing = true;
			}
			if(beginPos == null)
			{
				beginPos = new Point(Mouse.getX(), Mouse.getY());
			}
			else if(!isMousePosChanged)
			{
				if(beginPos.getX() != Mouse.getX() || beginPos.getY() != Mouse.getY())
				{
					isMousePosChanged = true;
				}
			}
		}
		else if(isMousePressed)
		{
			if(isMousePosChanged)
			{
				if(isLineDrawing)
				{
					measurements.add(new Measure(new Point(curX, curY), new Point(Mouse.getX(), disHeight - Mouse.getY())));
				}
				isLineDrawing = false;
			}
			else
			{
				checkLineClick();
			}
			isMousePressed = false;
			isMousePosChanged = false;
			beginPos = null;
		}
	}
	
	private static void checkLineClick()
	{
		int mouseX = Mouse.getX();
		int mouseY = Mouse.getY();
		for(Measure m : measurements)
		{
			if(isMeasureSelected(m, mouseX, mouseY))
			{
				//System.out.println("Line Selected");
				if(m.isSelected())
				{
					m.setSelected(false);
					measurementsToDelete.remove(m);
				}
				else
				{
					m.setSelected(true);
					measurementsToDelete.add(m);
				}
			}
		}
	}
	
	private static boolean isMeasureSelected(Measure m, int mouseX, int mouseY)
	{
		int fromX = m.pointFrom.getX();
		int fromY = disHeight - m.pointFrom.getY();
		int toX = m.pointTo.getX();
		int toY = disHeight - m.pointTo.getY();
		System.out.println(mouseX + ":" + mouseY + " " + fromX + ":" + fromY + " "
				+ toX + ":" + toY + " ");
//		if (fromX == mouseX) return toX == mouseX;
//		   // if AC is vertical.
//		   if (fromY == mouseY) return toY == mouseY;
		   // match the gradients
//		   return (mouseX - fromX)*(toY - fromY) == 
//				   (mouseY - fromY)*(toX - fromX);
		return getDistance(mouseX, mouseY, fromX, fromY, toX, toY) <= MAX_DISTANCE;
	}
	
	private static double getDistance(int x0, int y0, int x1, int y1, int x2, int y2)
	{
		return Math.abs((y2 - y1)*x0 - (x2 - x1)*y0 + x2*y1 - y2*x1)/Math.sqrt((y2 - y1)*(y2 - y1) + 
				(x2 - x1)*(x2 - x1));
	}
	
	private static void printMesureInfo(Measure mesure, float scale)
	{
		drawLine(mesure.getPointFrom().getX(), mesure.getPointFrom().getY(), 
				mesure.getPointTo().getX(), mesure.getPointTo().getY(), mesure.isSelected());
		int x = mesure.getPointTo().getX();
		int y = mesure.getPointTo().getY();
		String text = mesure.length.intValue() + (pixelSpacing != null ? " mm" : " pxl");
		float w = text.length() * 8f;
		float h = 20f;
		
		glBegin(GL_QUADS);
			glColor3f(135.0f/255.0f, 54.0f/255.0f, 54.0f/255.0f);
			glVertex2f(x, y);
	
			glColor3f(135.0f/255.0f, 54.0f/255.0f, 54.0f/255.0f);
			glVertex2f(x + w, y);
	
			glColor3f(135.0f/255.0f, 54.0f/255.0f, 54.0f/255.0f);
			glVertex2f(x + w, y + h);
	
			glColor3f(135.0f/255.0f, 54.0f/255.0f, 54.0f/255.0f);
			glVertex2f(x, y + h);
		glEnd();
		glUseProgram(0);
		Tools.renderString(text, mesure.pointTo.getX(), mesure.pointTo.getY(), Color.yellow, font);
		glUseProgram(programId);
	}
	
	private static void drawLine(int fromX, int fromY, int toX, int toY, boolean isSelected)
	{
		glLineWidth(1.5f);
		glDisable(GL11.GL_TEXTURE_2D);
		glDisable(GL11.GL_TEXTURE_1D);
//		glEnable(GL_LINE_SMOOTH);               
//		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		glBegin(GL_LINES);
			if(isSelected)
			{
				glColor3f(1, 1, 0);
			}
			else
			{
				glColor3f(1, 0, 0);
			}
			
			glVertex2i(fromX, fromY);
			glVertex2i(toX, toY);
		glEnd();
		glEnable(GL_TEXTURE_1D);
		glEnable(GL_TEXTURE_2D);
		//glDisable(GL_LINE_SMOOTH);
	}
	
	
	protected void setPixelSpacing(Float spacing)
	{
		MeasurementsRender.pixelSpacing = spacing;
	}

	
	private static class Measure
	{
		private Point pointFrom;
		private Point pointTo;
		private Double length;
		private boolean isSelected = false;
		
		public Measure(Point from, Point to) {
			pointFrom = from;
			pointTo = to;
			double diffX = pointFrom.getX() - pointTo.getX();
			diffX = pixelSpacing != null ? pixelSpacing * diffX : diffX;
			double diffY = pointFrom.getY() - pointTo.getY();
			diffY = pixelSpacing != null ? pixelSpacing * diffY : diffY;
			length = Math.sqrt(diffX * diffX + diffY * diffY);
		}
		public Point getPointFrom() {
			return pointFrom;
		}
		public Point getPointTo() {
			return pointTo;
		}
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
	}
}
