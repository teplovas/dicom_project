package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex2f;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Point;
import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

public class MesurementsRender {
	
	private final static double MAX_DISTANCE = 1d; 
	private static List<Mesure> mesurements = new ArrayList<Mesure>();
	private static List<Mesure> mesurementsToDelete = new ArrayList<Mesure>();
	private static int curX = 0;
	private static int curY = 0;
	private static boolean isLineDrawing = false;
	private static int disWidth;
	private static int disHeight;
	private static TrueTypeFont font;
	private static Float pixelSpacing = 1.f;
	private static Point beginPos = null;
	private static Point endPos = null;
	private static boolean isMousePressed = false;
	private static boolean isDrawLine = false;
	
	protected static void init(int disWidth, int disHeight)
	{
		MesurementsRender.disHeight = disHeight;
		MesurementsRender.disWidth = disWidth;
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 14);
		font = new TrueTypeFont(awtFont, true);
	}
	
	protected void setPixelSpacing(Float spacing)
	{
		MesurementsRender.pixelSpacing = spacing;
	}
	
	protected static void renderMesurements(float scale, boolean isMesurements)
	{
		checkKeyPressed();
		for(Mesure m : mesurements)
		{
			printMesureInfo(m, scale);
		}
		
		if(isMesurements)
		{
			doMouseClick();
		}
	}
	
	private static void checkKeyPressed()
	{
		if(Keyboard.isKeyDown(Keyboard.KEY_DELETE))
		{
			mesurements.removeAll(mesurementsToDelete);
			mesurementsToDelete.clear();
		}
	}
	
	
	private static void doMouseClick()
	{
		if(Mouse.isButtonDown(0))
		{
			isMousePressed = true;
			if(isDrawLine)
			{
				if(!isLineDrawing)
				{
					curX = Mouse.getX();
					curY = disHeight - Mouse.getY();
				}
				if(curX != Mouse.getX() && curY != Mouse.getY())
				drawLine(curX, curY, Mouse.getX(), disHeight - Mouse.getY(), true);
				isLineDrawing = true;
			}
			if(beginPos == null)
			{
				beginPos = new Point(Mouse.getX(), Mouse.getY());
			}
			else if(!isDrawLine)
			{
				if(beginPos.getX() != Mouse.getX() || beginPos.getY() != Mouse.getY())
				{
					isDrawLine = true;
				}
			}
		}
		else if(isMousePressed)
		{
			if(isDrawLine)
			{
				if(isLineDrawing)
				{
					mesurements.add(new Mesure(new Point(curX, curY), new Point(Mouse.getX(), disHeight - Mouse.getY())));
				}
				isLineDrawing = false;
			}
			else
			{
				checkLineClick();
			}
			isMousePressed = false;
			isDrawLine = false;
			beginPos = null;
		}
	}
	
	private static void checkLineClick()
	{
		int mouseX = Mouse.getX();
		int mouseY = Mouse.getY();
		for(Mesure m : mesurements)
		{
			if(isMeasureSelected(m, mouseX, mouseY))
			{
				System.out.println("Line Selected");
				if(m.isSelected())
				{
					m.setSelected(false);
					mesurementsToDelete.remove(m);
				}
				else
				{
					m.setSelected(true);
					mesurementsToDelete.add(m);
				}
			}
		}
	}
	
	private static boolean isMeasureSelected(Mesure m, int mouseX, int mouseY)
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
	
	private static void printMesureInfo(Mesure mesure, float scale)
	{
		drawLine(mesure.getPointFrom().getX(), mesure.getPointFrom().getY(), 
				mesure.getPointTo().getX(), mesure.getPointTo().getY(), mesure.isSelected());
		int x = mesure.getPointTo().getX();
		int y = mesure.getPointTo().getY();
		String text = mesure.distance.intValue() + (pixelSpacing != null ? " mm" : " pxl");
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
		
		renderString(text, mesure.pointTo.getX(), mesure.pointTo.getY(), Color.yellow, font);
	}
	
	private static void drawLine(int fromX, int fromY, int toX, int toY, boolean isSelected)
	{
		glLineWidth(1.5f);
		glDisable(GL_TEXTURE_2D);
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
		glEnable(GL_TEXTURE_2D);
	}
	
	private static void renderString(String text, float x, float y, Color color, TrueTypeFont font)
	{
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        //Color.white.bind();
        GL11.glDrawBuffer(GL11.GL_BACK);
		font.drawString(x, y, text, color);
		GL11.glDisable(GL11.GL_BLEND);
	}

	
	private static class Mesure
	{
		private Point pointFrom;
		private Point pointTo;
		private Double distance;
		private boolean isSelected = false;
		
		public Mesure(Point from, Point to) {
			pointFrom = from;
			pointTo = to;
			double diffX = pointFrom.getX() - pointTo.getX();
			diffX = pixelSpacing != null ? pixelSpacing * diffX : diffX;
			double diffY = pointFrom.getY() - pointTo.getY();
			diffY = pixelSpacing != null ? pixelSpacing * diffY : diffY;
			distance = Math.sqrt(diffX * diffX + diffY * diffY);
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
