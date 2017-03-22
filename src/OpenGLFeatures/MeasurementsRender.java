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
	
	private static int transformMatrixLocation = 0;
	
	private static int programId;
	
	protected static void init(int disWidth, int disHeight, int pId)
	{
		MeasurementsRender.disHeight = disHeight;
		MeasurementsRender.disWidth = disWidth;
		MeasurementsRender.programId = 0;
		
		//transformMatrixLocation = glGetUniformLocation(programId, "transformMatrix");
	}
	
	public static void initFont()
	{
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 14);
		font = new TrueTypeFont(awtFont, true);
	}
	
	protected static void renderMeasurements(float scale, boolean isMesurements, FloatBuffer transformMatrix)
	{
		glUseProgram(programId);
		//GL20.glUniformMatrix4(transformMatrixLocation, false, transformMatrix);
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
					measurements.add(Measure.createImgCoordMeasure(new Point(curX, curY), 
							new Point(Mouse.getX(), disHeight - Mouse.getY())));
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
		float fromX = m.getPointScreenFrom().getX();
		float fromY = disHeight - m.getPointScreenFrom().getY();
		float toX = m.getPointScreenTo().getX();
		float toY = disHeight - m.getPointScreenTo().getY();
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
	
	private static double getDistance(int x0, int y0, float x1, float y1, float x2, float y2)
	{
		return Math.abs((y2 - y1)*x0 - (x2 - x1)*y0 + x2*y1 - y2*x1)/Math.sqrt((y2 - y1)*(y2 - y1) + 
				(x2 - x1)*(x2 - x1));
	}
	
	private static void printMesureInfo(Measure mesure, float scale)
	{
		drawLine(mesure.getPointScreenFrom().getX(), mesure.getPointScreenFrom().getY(), 
				mesure.getPointScreenTo().getX(), mesure.getPointScreenTo().getY(), mesure.isSelected());
		float x = mesure.getPointScreenTo().getX();
		float y = mesure.getPointScreenTo().getY();
		String text = mesure.length + (pixelSpacing != null ? " cm" : " pxl");
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
		Tools.renderString(text, mesure.getPointScreenTo().getX(), mesure.getPointScreenTo().getY(), Color.yellow, font);
		glUseProgram(programId);
	}
	
	private static void drawLine(float fromX, float fromY, float toX, float toY, boolean isSelected)
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
			
			glVertex2f(fromX, fromY);
			glVertex2f(toX, toY);
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
			double diffX = (pointFrom.getX() - pointTo.getX()) * ImageRender.width;
			diffX = pixelSpacing != null ? pixelSpacing * diffX : diffX;
			double diffY = (pointFrom.getY() - pointTo.getY()) * ImageRender.height;
			diffY = pixelSpacing != null ? pixelSpacing * diffY : diffY;
			length = round(Math.sqrt(diffX * diffX + diffY * diffY)/100);
		}
		
		private double round(double length)
		{
			return Math.round(length * 100.0) / 100.0;
		}
		
		public static Measure createImgCoordMeasure(Point from, Point to)
		{
			return new Measure(Tools.convertToImageCoord(from), Tools.convertToImageCoord(to));
		}
		
		public Point getPointScreenFrom() {
			return Tools.convertToScreenCoord(pointFrom);
		}
		public Point getPointScreenTo() {
			return Tools.convertToScreenCoord(pointTo);
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
