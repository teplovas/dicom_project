package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.awt.Font;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

public class AdditionalInfoRender {
	
	private static TrueTypeFont font;
	private static int disHeight;
	
	protected static void init(int disHeight)
	{	
		AdditionalInfoRender.disHeight = disHeight;
		//initFont();
	}
	
	public static void initFont()
	{
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 24);
		font = new TrueTypeFont(awtFont, true);
	}

	protected static void renderInfo(int currentImageNumber, int numberOfImages)
	{
		Tools.renderString(currentImageNumber + "/" + numberOfImages, 20, 30, Color.yellow, font);
		Tools.renderString("X: " + Mouse.getX() + " Y: " + Mouse.getY(), 20, disHeight - 30, Color.yellow, font);
	}
	
}
