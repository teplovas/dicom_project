package OpenGLFeatures;

import static org.lwjgl.opengl.GL11.*;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;

import OpenGLFeatures.MeasurementsRender.EllipseParam;
import tools.DicomImage;

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
	
	
	public static float angle( float x1, float y1, float x2, float y2)
	{ 
		float t = (x1 * x2 + y1 * y2)
				/ (float)(Math.sqrt(x1 * x1 + y1 * y1) * Math.sqrt(x2 * x2 + y2 * y2));
		if (t < -1)
			t = -1;
		else if (t > 1)
			t = 1;
		return (float)Math.acos(t);
	}
	
	/**
	 * Находится ли отрезок в эллипсе
	 * @return
	 */
	public static boolean isSegmentInEllipse(Point elCenter, float a, float b, Point segTo)
	{
//		float ang = angle(elCenter.getX() + a, elCenter.getY(), segTo.getX(), segTo.getY());
//		float curRadius = ellipseRadius(a, b, ang);
//		float centerPointDistance = (float)Math.sqrt((elCenter.getX() - segTo.getX())*(elCenter.getX() - segTo.getX())
//				+ (elCenter.getY() - segTo.getY())*(elCenter.getY() - segTo.getY()));
//		return centerPointDistance <= curRadius;
		float res = (((segTo.getX() - elCenter.getX())*(segTo.getX() - elCenter.getX())) / (a*a)) + 
				(((segTo.getY() - elCenter.getY())*(segTo.getY() - elCenter.getY())) / (b*b)); 
		return res < 1;
	}
	
	private static float ellipseRadius(float a, float b, float angle)
	{
		return a*b / (float)Math.sqrt(b*b*Math.cos(angle)*Math.cos(angle) 
				+ a*a*Math.sin(angle)*Math.sin(angle));
	}
	
	public static Map<EllipseParam, Float> calculateEllipseParams(DicomImage img, 
			Point elCenter, float a, float b)
	{
		Map<EllipseParam, Float> result = new HashMap<EllipseParam, Float>();
		float resMin = Float.MAX_VALUE, resMax = Float.MIN_VALUE, resMean = 0, resArea = 0;
		int counter = 0;
		
		Object[] buffer = img.getImageBuffer();
		System.out.println("center: x=" + elCenter.x + ", y=" + elCenter.y);
		System.out.println("a=" + a + ", b=" + b);
		elCenter = new Point(elCenter.getX(), elCenter.getY());		
		for(float y = 0; y < img.getHeight(); y++)
			for(float x = 0; x < img.getWidth(); x++)
			{
				Object elem = buffer[(int)y * img.getWidth() + (int)x]; 
				float val = elem instanceof Byte ? ((Byte)elem).floatValue() : ((Short)elem).floatValue();
				float scrX = 2 * (x)/img.getWidth() - 1;
				float scrY = 2 * (y)/img.getHeight() - 1;
				Point screenCoord = convertToScreenCoord(new Point(scrX, scrY));
				//MeasurementsRender.drawLine(elCenter.x, elCenter.y, scrX, scrY, false);
				//System.out.println(screenCoord.x + ", " + screenCoord.y);
				if(isSegmentInEllipse(elCenter, a, b, screenCoord))
				{
					glBegin(GL_POINTS);
					glColor3f((val/1023f), (val/1023f), (val/1023f));
			        glVertex2f(screenCoord.x, screenCoord.y);
			        glEnd();
					resMin = val < resMin ? val : resMin;
					resMax = val > resMax ? val : resMax;
					resMean += val;
					counter++;
				}
			}
		System.out.println("Counter = " + counter);
		resArea = (float)round(3.1415926f * Math.abs(a) * Math.abs(b)/100);
		resMean /= counter;
		
		result.put(EllipseParam.AREA, resArea);
		result.put(EllipseParam.MAX, resMax);
		result.put(EllipseParam.MIN, resMin);
		result.put(EllipseParam.MEAN, resMean);
		return result;
	}
	
	protected static double round(double length)
	{
		return Math.round(length * 100.0) / 100.0;
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
