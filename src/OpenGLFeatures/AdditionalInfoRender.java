package OpenGLFeatures;

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
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 24);
		font = new TrueTypeFont(awtFont, true);
		
		AdditionalInfoRender.disHeight = disHeight;
	}

	protected static void renderInfo(int currentImageNumber, int numberOfImages)
	{
		renderString(currentImageNumber + "/" + numberOfImages, 20, 30, Color.yellow);
		renderString("X: " + Mouse.getX() + " Y: " + Mouse.getY(), 20, disHeight - 30, Color.yellow);
	}
	
	private static void renderString(String text, float x, float y, Color color)
	{
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        //Color.white.bind();
        GL11.glDrawBuffer(GL11.GL_BACK);
		font.drawString(x, y, text, color);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
