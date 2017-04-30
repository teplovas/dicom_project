package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import tools.DicomImage;

public class MeasurementsRender {
	
	private final static double MAX_DISTANCE = 3d;
	private final static float DEG2RAD = 3.14159f/180.0f;
	private static List<Measure> measurements = new ArrayList<Measure>();
	private static List<Measure> measurementsToDelete = new ArrayList<Measure>();
	private static int curX = 0;
	private static int curY = 0;
	private static boolean isObjectDrawing = false;
	private static int disWidth;
	private static int disHeight;
	private static TrueTypeFont font;
	private static Float pixelSpacing = 1.f;
	private static Point beginPos = null;
	private static boolean isMousePressed = false;
	private static boolean isMousePosChanged = false;
	
	private static int transformMatrixLocation = 0;
	
	private static int programId;
	private static DicomImage img;
	
	public enum MeasureType
	{
		LINE,
		ELLIPSE,
		NONE,
		;
	}
	
	public enum EllipseParam
	{
		MIN("Min"),
		MAX("Max"),
		MEAN("Mean"),
		AREA("Area"),
		;
		
		private String name;
		private EllipseParam(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
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
	
	public static void bindImage(DicomImage img)
	{
		MeasurementsRender.img = img;
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
		
		if(type != MeasureType.NONE)
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
		if(Mouse.isButtonDown(0))
		{
			isMousePressed = true;
			if(isMousePosChanged)
			{
				if(!isObjectDrawing)
				{
					curX = Mouse.getX();
					curY = disHeight - Mouse.getY();
				}
				if(curX != Mouse.getX() && curY != Mouse.getY())
				drawObject(type, curX, curY, Mouse.getX(), disHeight - Mouse.getY(), false);
				isObjectDrawing = true;
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
				if(isObjectDrawing)
				{
					measurements.add(createMeasure(type, new Point(curX, curY), 
							new Point(Mouse.getX(), disHeight - Mouse.getY())));
				}
				isObjectDrawing = false;
			}
			else
			{
				checkObjectClick();
			}
			isMousePressed = false;
			isMousePosChanged = false;
			beginPos = null;
		}
	}
	
	private static void drawObject(MeasureType type, float fromX, float fromY, float toX, float toY, boolean isSelected)
	{
		if(MeasureType.LINE == type)
		{
			drawLine(fromX, fromY, toX, toY, isSelected);
		}
		if(MeasureType.ELLIPSE == type)
		{
			drawOval(fromX, fromY, toX, toY, isSelected);
		}
	}
	
	private static Measure createMeasure(MeasureType type, Point from, Point to)
	{
		if(MeasureType.LINE == type)
		{
			return MeasureLine.createImgCoordMeasure(from, to);
		}
		if(MeasureType.ELLIPSE == type)
		{
			return MeasureEllipse.createImgCoordMeasure(from, to);
		}
		return null;
	}
	
	private static void checkObjectClick()
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
	
	
	public static void drawLine(float fromX, float fromY, float toX, float toY, boolean isSelected)
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
		if(isSelected)
		{
			glColor3f(1, 1, 0);
		}
		else
		{
			glColor3f(1, 0, 0);
		}
			
		drawCircle(fromX, fromY, Math.abs(fromX - toX), Math.abs(fromY - toY), 360);
		glEnable(GL_TEXTURE_1D);
		glEnable(GL_TEXTURE_2D);
	}
	
	private static void drawCircle(float x, float y, float r1, float r2, int amountSegments)
	{
		glBegin(GL_LINE_LOOP);
		for(int i = 0; i < amountSegments; i++)
		{
			float angle = 2.0f * 3.1415926f * (float)i / (float)amountSegments;
			float dx = r1 * (float)Math.cos(angle);
			float dy = r2 * (float)Math.sin(angle);
			glVertex2f(x + dx, y + dy);
		}
		glEnd();
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
		
		protected void printInfo(List<String> text)
		{
			float x = getPointScreenTo().getX();
			float y = getPointScreenTo().getY();
			
			int maxLen = text.stream().map(t -> t.length()).max(Integer::compare).get();
			float w = maxLen * 7f;
			float h = text.size() * 20f;
			
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
			
			int counter = 0;
			for(String s : text)
			{
				Tools.renderString(s, getPointScreenTo().getX(), getPointScreenTo().getY() + counter * 20f, 
						Color.yellow, font);
				counter++;
			}
			glUseProgram(programId);
		}
		
		public abstract boolean isMeasureSelected(int mouseX, int mouseY);
	
		public boolean isSelected() {
			return isSelected;
		}
		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}
	}
	
	private static class MeasureEllipse extends Measure
	{
		private Map<EllipseParam, Float> paramsValues;
		public MeasureEllipse(Point from, Point to) {
			super(from, to);
		}
		
		public static MeasureEllipse createImgCoordMeasure(Point from, Point to)
		{
			return new MeasureEllipse(Tools.convertToImageCoord(from), Tools.convertToImageCoord(to));
		}

		@Override
		public void print() 
		{
			float fromX = getPointScreenFrom().getX();
			float fromY = getPointScreenFrom().getY();
			float toX = getPointScreenTo().getX();
			float toY = getPointScreenTo().getY();
			drawOval(fromX, fromY, toX, toY, isSelected());
//			float diffX = Math.abs((fromX - toX)/2);
//			float diffY = Math.abs((fromY - toY)/2);
			float diffX = Math.abs((fromX - toX));
			float diffY = Math.abs((fromY - toY));
			
//			float sqr = (float)round(3.1415926f * Math.abs(diffX) * Math.abs(diffY)/100);
//			sqr = pixelSpacing != null ? pixelSpacing * sqr : sqr;
			//Point center = new Point(fromX + diffX, RenderingLoop.displayHeight - (fromY + diffY));
			Point center = new Point(fromX, /*RenderingLoop.displayHeight - */fromY);
			
			paramsValues = //Tools.calculateEllipseParams(img, center, diffX, diffY); 
					paramsValues == null ? Tools.calculateEllipseParams(img, center, diffX, diffY) :
					paramsValues;
			List<String> infoText = new ArrayList<String>();
			
			for(EllipseParam param : EllipseParam.values())
			{
				if(EllipseParam.AREA.equals(param))
				{
					float sqr = pixelSpacing != null ? pixelSpacing * paramsValues.get(param) : paramsValues.get(param);
					infoText.add(param.getName() + ": " + sqr + " cm2");
				}
				else
				{
					infoText.add(param.getName() + ": " + paramsValues.get(param));
				}
//				infoText.append(System.lineSeparator());
			}
			
			printInfo(infoText);
		}

		@Override
		public boolean isMeasureSelected(int mouseX, int mouseY) {
			float fromX = getPointScreenFrom().getX();
			float fromY = disHeight - getPointScreenFrom().getY();
			float toX = getPointScreenTo().getX();
			float toY = disHeight - getPointScreenTo().getY();
			
			float r1 = Math.abs(fromX - toX);
			float r2 = Math.abs(fromY - toY);
			Point center = new Point(fromX + r1/2f, fromY + r2/2f);
			
			float minR = r1 < r2 ? r1 : r2;
			float diffX = center.getX() - mouseX;
			float diffY = center.getY() - mouseY;
			return Math.sqrt(diffX * diffX + diffY * diffY) - minR <= MAX_DISTANCE;
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
		
		@Override
		public void print()
		{
			drawLine(getPointScreenFrom().getX(), getPointScreenFrom().getY(), 
					getPointScreenTo().getX(), getPointScreenTo().getY(), isSelected());
			String text = length + (pixelSpacing != null ? " cm" : " pxl");
			List<String> info = new ArrayList<String>();
			info.add(text);
			printInfo(info);
		}
		
		public Point getPointFrom() {
			return pointFrom;
		}
		public Point getPointTo() {
			return pointTo;
		}
	}
}
