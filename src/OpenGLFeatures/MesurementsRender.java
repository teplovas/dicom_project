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
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;


public class MesurementsRender {
	
	private static List<Mesure> mesurements = new ArrayList<Mesure>();
	private static int curX = 0;
	private static int curY = 0;
	private static boolean isDrawLine = false;
	private static int disWidth;
	private static int disHeight;
	private static TrueTypeFont font;
	private static Float pixelSpacing = 1.f;
	
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
		for(Mesure m : mesurements)
		{
			printMesureInfo(m, scale);
		}
		
		if(isMesurements)
		{
			if(Mouse.isButtonDown(0))
			{
				if(!isDrawLine)
				{
					curX = Mouse.getX();
					curY = disHeight - Mouse.getY();
				}
				if(curX != Mouse.getX() && curY != Mouse.getY())
				drawLine(curX, curY, Mouse.getX(), disHeight - Mouse.getY(), true);
				isDrawLine = true;
			}
			else
			{
				if(isDrawLine)
				{
					mesurements.add(new Mesure(new Point(curX, curY), new Point(Mouse.getX(), disHeight - Mouse.getY())));
				}
				isDrawLine = false;
			}
		}
	}
	
	private static void printMesureInfo(Mesure mesure, float scale)
	{
		drawLine(mesure.getPointFrom().getX(), mesure.getPointFrom().getY(), 
				mesure.getPointTo().getX(), mesure.getPointTo().getY(), false);
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
	
	private static void drawLine(int fromX, int fromY, int toX, int toY, boolean isb)
	{
		glLineWidth(2);
		glDisable(GL_TEXTURE_2D);
		glBegin(GL_LINES);
			glColor3f(1, 0, 0);
			
			glVertex2f(fromX, fromY);
			glVertex2f(toX, toY);
		glEnd();
		glEnable(GL_TEXTURE_2D);
		if(isb)
			System.out.println(toX + " " + toY);
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
		public void setPointFrom(Point pointFrom) {
			this.pointFrom = pointFrom;
		}
		public Point getPointTo() {
			return pointTo;
		}
		public void setPointTo(Point pointTo) {
			this.pointTo = pointTo;
		}
		public Double getDistance() {
			return distance;
		}
		public void setDistance(Double distance) {
			this.distance = distance;
		}
	}
}
