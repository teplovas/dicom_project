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
	private final static float DEG2RAD = 3.14159f/180.0f;
	private static List<Measure> measurements = new ArrayList<Measure>();
	private static List<Measure> measurementsToDelete = new ArrayList<Measure>();
	private static int curX = 0;
	private static int curY = 0;
	private static boolean isLineDrawing = false;
	private static boolean isOvalDrawing = false;
	private static int disWidth;
	private static int disHeight;
	private static TrueTypeFont font;
	private static Float pixelSpacing = 1.f;
	private static Point beginPos = null;
	private static boolean isMousePressed = false;
	private static boolean isMousePosChanged = false;
	
	private static int transformMatrixLocation = 0;
	
	private static int programId;
	
	public enum MeasureType
	{
		LINE,
		OVAL;
	}	
	
	
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
	
	protected static void renderMeasurements(float scale, MeasureType type, FloatBuffer transformMatrix)
	{
		glUseProgram(programId);
		//GL20.glUniformMatrix4(transformMatrixLocation, false, transformMatrix);
		checkKeyPressed();
		for(Measure m : measurements)
		{
			m.print();
		}
		
		if(type != null)
		{
			doMouseClick(type);
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
	
	
	private static void doMouseClick(MeasureType type)
	{
		if(MeasureType.LINE == type)
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
					measurements.add(MeasureLine.createImgCoordMeasure(new Point(curX, curY), 
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
		//===================================================
		else
		{
			if(Mouse.isButtonDown(0))
			{
				isMousePressed = true;
				if(isMousePosChanged)
				{
					if(!isOvalDrawing)
					{
						curX = Mouse.getX();
						curY = disHeight - Mouse.getY();
					}
					if(curX != Mouse.getX() && curY != Mouse.getY())
					drawOval(curX, curY, Mouse.getX(), disHeight - Mouse.getY(), false);
					isOvalDrawing = true;
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
					if(isOvalDrawing)
					{
						measurements.add(MeasureOval.createImgCoordMeasure(new Point(curX, curY), 
								new Point(Mouse.getX(), disHeight - Mouse.getY())));
					}
					isOvalDrawing = false;
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
	}
	
	private static void checkLineClick()
	{
		int mouseX = Mouse.getX();
		int mouseY = Mouse.getY();
		for(Measure m : measurements)
		{
			if(m.isMeasureSelected(mouseX, mouseY))
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

	
	private static double getDistance(int x0, int y0, float x1, float y1, float x2, float y2)
	{
		return Math.abs((y2 - y1)*x0 - (x2 - x1)*y0 + x2*y1 - y2*x1)/Math.sqrt((y2 - y1)*(y2 - y1) + 
				(x2 - x1)*(x2 - x1));
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
	
	
	private static void drawOval(float fromX, float fromY, float toX, float toY, boolean isSelected)
	{
		glLineWidth(1.5f);
		glDisable(GL11.GL_TEXTURE_2D);
		glDisable(GL11.GL_TEXTURE_1D);

		glBegin(GL_LINE_LOOP);
			if(isSelected)
			{
				glColor3f(1, 1, 0);
			}
			else
			{
				glColor3f(1, 0, 0);
			}
			
			for(int i=0;i<360;i++)
			   {
			      float rad = i*DEG2RAD;
			      glVertex2f((float)Math.cos(rad)*/*Math.abs*/(fromX - toX),
			                  (float)Math.sin(rad)*/*Math.abs*/(fromY - toY));
			   }
		glEnd();
		glEnable(GL_TEXTURE_1D);
		glEnable(GL_TEXTURE_2D);
		//glDisable(GL_LINE_SMOOTH);
	}
	
	
	protected void setPixelSpacing(Float spacing)
	{
		MeasurementsRender.pixelSpacing = spacing;
	}

	
	private abstract static class Measure
	{
		protected Point pointFrom;
		protected Point pointTo;
		private boolean isSelected = false;
		
		public Measure(Point from, Point to) {
			this.pointFrom = from;
			this.pointTo = to;
		}
		
		protected double round(double length)
		{
			return Math.round(length * 100.0) / 100.0;
		}
		
		public Point getPointScreenFrom() {
			return Tools.convertToScreenCoord(pointFrom);
		}
		public Point getPointScreenTo() {
			return Tools.convertToScreenCoord(pointTo);
		}
		
		public abstract void print();
		
		public abstract boolean isMeasureSelected(int mouseX, int mouseY);
	
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
	}
	
	private static class MeasureOval extends Measure
	{
		public MeasureOval(Point from, Point to) {
			super(from, to);
		}
		
		public static MeasureOval createImgCoordMeasure(Point from, Point to)
		{
			return new MeasureOval(Tools.convertToImageCoord(from), Tools.convertToImageCoord(to));
		}

		@Override
		public void print() 
		{
			drawOval(getPointScreenFrom().getX(), getPointScreenFrom().getY(), 
					getPointScreenTo().getX(), getPointScreenTo().getY(), isSelected());
		}

		@Override
		public boolean isMeasureSelected(int mouseX, int mouseY) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	private static class MeasureLine extends Measure
	{
		private Double length;
		
		public MeasureLine(Point from, Point to) {
			super(from, to);
			double diffX = (pointFrom.getX() - pointTo.getX()) * ImageRender.width;
			diffX = pixelSpacing != null ? pixelSpacing * diffX : diffX;
			double diffY = (pointFrom.getY() - pointTo.getY()) * ImageRender.height;
			diffY = pixelSpacing != null ? pixelSpacing * diffY : diffY;
			length = round(Math.sqrt(diffX * diffX + diffY * diffY)/100);
		}
		
		public static MeasureLine createImgCoordMeasure(Point from, Point to)
		{
			return new MeasureLine(Tools.convertToImageCoord(from), Tools.convertToImageCoord(to));
		}
		
		public boolean isMeasureSelected(int mouseX, int mouseY)
		{
			float fromX = getPointScreenFrom().getX();
			float fromY = disHeight - getPointScreenFrom().getY();
			float toX = getPointScreenTo().getX();
			float toY = disHeight - getPointScreenTo().getY();
			System.out.println(mouseX + ":" + mouseY + " " + fromX + ":" + fromY + " "
					+ toX + ":" + toY + " ");
			return getDistance(mouseX, mouseY, fromX, fromY, toX, toY) <= MAX_DISTANCE;
		}
		
		public void print()
		{
			drawLine(getPointScreenFrom().getX(), getPointScreenFrom().getY(), 
					getPointScreenTo().getX(), getPointScreenTo().getY(), isSelected());
			float x = getPointScreenTo().getX();
			float y = getPointScreenTo().getY();
			String text = length + (pixelSpacing != null ? " cm" : " pxl");
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
			Tools.renderString(text, getPointScreenTo().getX(), getPointScreenTo().getY(), Color.yellow, font);
			glUseProgram(programId);
		}
		
		public Point getPointFrom() {
			return pointFrom;
		}
		public Point getPointTo() {
			return pointTo;
		}
	}
}
