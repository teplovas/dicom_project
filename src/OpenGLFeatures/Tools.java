package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

public class Tools {
	
	public static void renderString(String text, float x, float y, Color color, TrueTypeFont font)
	{
		glEnable(GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

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
	
	
	public static Point convertToImageCoord(Point screenCoord)
	{
		float x = 2 * (screenCoord.getX()) / RenderingLoop.displayWidth - 1;
		float y = 2 * (screenCoord.getY()) / RenderingLoop.displayHeight - 1;
		Matrix4f mat = RenderingLoop.getTransformMatrix();
		Matrix4f matInv = Matrix4f.invert(mat, null);
		Vector4f res = Matrix4f.transform(matInv, new Vector4f(x, y, 0, 1), null);
		
		return new Point(res.x, res.y);
	}
	
	public static Point convertToScreenCoord(Point imageCoord)
	{		
		float x = imageCoord.getX();
		float y = imageCoord.getY();
		
		Vector4f res = Matrix4f.transform(RenderingLoop.getTransformMatrix(), new Vector4f(x, y, 0, 1), null);
		x = (res.x + 1) * RenderingLoop.displayWidth/2;
		y = (res.y + 1) * RenderingLoop.displayHeight/2;
		return new Point(x, y);
	}
}
