package tools;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import tools.ConvertTools;
import tools.PaletteLoader;

public class ImageHelper <T> {
	private static Object[] imageDataBuffer;
	
	public static int getMaxValue()
	{
		return ConvertTools.getMaxValue();
	}
	
	public static int getMinValue()
	{
		return ConvertTools.getMinValue();
	}
	
	public static Object[] getDataBuffer()
	{
		return imageDataBuffer;
	}
	
	public static Image openImage(String fileName) throws Exception
	{
		imageDataBuffer = ConvertTools.readDicomFile(fileName); 
		if(ConvertTools.isImageColored())
		{
			return SwingFXUtils.toFXImage(ConvertTools.convertDataToColorImage(imageDataBuffer), null);
		}
		else
		{
			return SwingFXUtils.toFXImage(ConvertTools.convertDataToGreyImage(
					imageDataBuffer, ConvertTools.getMinValue(), ConvertTools.getMaxValue()), null);
		}
	}
	
	public static int getHeight()
	{
		return ConvertTools.getRows();
	}
	
	public static int getWidth()
	{
		return ConvertTools.getCols();
	}
	
	public static Image changeOpendGreyImage(int from, int to, int paletteType) throws Exception
	{
		
		if (paletteType > 0) {
			BufferedImage tmpImg = ConvertTools.convertDataToGreyImage(
					imageDataBuffer, from, to);
			int[][] palette = PaletteLoader.getPalette(paletteType);
			BufferedImage tmpChanged = ConvertTools
					.changePaletteForGreyScaleImage(tmpImg, palette);
			return SwingFXUtils.toFXImage(tmpChanged, null);
		}		
		return SwingFXUtils.toFXImage(ConvertTools.convertDataToGreyImage(
				imageDataBuffer, from, to), null);
		
		
	}
	
}
