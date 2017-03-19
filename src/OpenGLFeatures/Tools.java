package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.Point;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
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
		return screenCoord;
	}
	
	public static Point convertToScreenCoord(Point imageCoord)
	{		
		int x = (imageCoord.getX() + ImageRender.getWShift());
		int y = (imageCoord.getY() + ImageRender.getHShift());
		Vector4f res = Matrix4f.transform(RenderingLoop.getTransformMatrix(), new Vector4f(x, y, 0, 1), null);
		return new Point((int)res.x, (int)res.y);
	}
}
