package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

public class Tools {
	
	public static void renderString(String text, float x, float y, Color color, TrueTypeFont font)
	{
//		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		glEnable(GL_TEXTURE_2D);
        glEnable(GL_TEXTURE_1D);
//		
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        
//        color.bind();
//        GL11.glDrawBuffer(GL11.GL_BACK);
//		font.drawString(x, y, text, color);
//		GL11.glDisable(GL11.GL_BLEND);
		
        //GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		Color.white.bind();
		 
		font.drawString(x, y, text, color);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public static float degreesToRadians(float degrees) {
		return degrees * (float) (Math.PI / 180d);
	}
	
	public static float coTangent(float angle) {
		return (float) (1f / Math.tan(angle));
	}
}
