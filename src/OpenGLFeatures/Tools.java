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
	 * ����������� �� ����� �������
	 * @return
	 */
	public static boolean isPointInEllipse(Point elCenter, float a, float b, Point pointCoord)
	{
		float res = (((pointCoord.getX() - elCenter.getX())*(pointCoord.getX() - elCenter.getX())) / (a*a)) + 
				(((pointCoord.getY() - elCenter.getY())*(pointCoord.getY() - elCenter.getY())) / (b*b)); 
		return res < 1;
	}
	
	public static boolean isPointOutEllipse(Point elCenter, float a, float b, Point pointCoord)
	{
		float res = (((pointCoord.getX() - elCenter.getX())*(pointCoord.getX() - elCenter.getX())) / (a*a)) + 
				(((pointCoord.getY() - elCenter.getY())*(pointCoord.getY() - elCenter.getY())) / (b*b)); 
		return res > 1;
	}
	
	private static float ellipseRadius(float a, float b, float angle)
	{
		return a*b / (float)Math.sqrt(b*b*Math.cos(angle)*Math.cos(angle) 
				+ a*a*Math.sin(angle)*Math.sin(angle));
	}
	
	/**
	 * @param img - �������� �����������
	 * @param elCenter - ����� �������
	 * @param a, b - �������
	 */
	public static Map<EllipseParam, Float> calculateEllipseParams(DicomImage img, 
			Point elCenter, float a, float b)
	{
		Map<EllipseParam, Float> result = new HashMap<EllipseParam, Float>();
		float resMin = Float.MAX_VALUE, resMax = Float.MIN_VALUE, resMean = 0, resArea = 0;
		int counter = 0;
		
		Object[] buffer = img.getImageBuffer();
		for(float y = 0; y < img.getHeight(); y++)
			for(float x = 0; x < img.getWidth(); x++)
			{
				Object elem = buffer[(int)y * img.getWidth() + (int)x]; 
				float val = elem instanceof Byte ? ((Byte)elem).floatValue() : ((Short)elem).floatValue();
				float scrX = 2 * (x)/img.getWidth() - 1;
				float scrY = 2 * (y)/img.getHeight() - 1;
				Point screenCoord = convertToScreenCoord(new Point(scrX, scrY));
				if(isPointInEllipse(elCenter, a, b, screenCoord))
				{
					resMin = val < resMin ? val : resMin;
					resMax = val > resMax ? val : resMax;
					resMean += val;
					counter++;
				}
			}
		resArea = (float)round(3.1415926f * Math.abs(a/2) * Math.abs(b/2)/100);
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
		float x = 2 * (screenCoord.getX() - ImageRender.getWShift()) / ImageRender.getWidth() - 1;
		float y = 2 * (screenCoord.getY() - ImageRender.getHShift()) / ImageRender.getHeight() - 1;
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
		x = (res.x + 1) * ImageRender.getWidth()/2 + ImageRender.getWShift();
		y = (res.y + 1) * ImageRender.getHeight()/2 + ImageRender.getHShift();
		return new Point(x, y);
	}
}
