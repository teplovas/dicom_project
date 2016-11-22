package tools;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.util.TagUtils;


public class ConvertTools {
	
	private static boolean isColored;
	
	private static int minValue = Integer.MAX_VALUE;
	
	private static int maxValue = Integer.MIN_VALUE;
	
	private static int rows;
	
	private static int cols;
		
	public static boolean isImageColored()
	{
		return isColored;
	}
	
	
	public static int getMinValue() {
		return minValue;
	}

	public static int getMaxValue() {
		return maxValue;
	}

	public static int getRows() {
		return rows;
	}

	public static int getCols() {
		return cols;
	}

/*	public static ImageData getImage(String fileName, int from, int to) throws Exception
	{
		BufferedImage tmp = convertDcmToBufferedImage(fileName, from, to);
		return convertToSWT(tmp);
	}
	
	public static ImageData getChangedPaletteImage(String fileName, int from, int to, int paletteType) throws Exception
	{
		BufferedImage tmp = convertDcmToBufferedImage(fileName, from, to);
		int[][] palette = PaletteLoader.getPalette(paletteType);
		BufferedImage tmpChanged = changePaletteForGreyScaleImage(tmp, palette);
		return convertToSWT(tmpChanged);
	}*/
	
	public static <T> BufferedImage convertDataToColorImage(T[] dataBuffer)
	{
		BufferedImage img = new BufferedImage(cols,
				rows, BufferedImage.TYPE_INT_RGB);
		WritableRaster image = img.getRaster();
		int s = dataBuffer.length;
		int size = s;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				
				T rr = (T)dataBuffer[s - size];
				size--;
				T g = (T)dataBuffer[s - size];
				size--;
				T b = (T)dataBuffer[s - size];
				size--;
				
					double tmpR =((Byte)rr).doubleValue();
					double tmpG =((Byte)g).doubleValue();
					double tmpB =((Byte)b).doubleValue();
					double[] arr = { tmpR, tmpG, tmpB };
				

				image.setPixel(col, row, arr);
			}
		}
		img.setData(image);
		return img;
	}
	
	public static <T> BufferedImage convertDataToGreyImage(T[] dataBuffer, int from, int to, int w, int h)
	{
		BufferedImage img = new BufferedImage(cols,
				rows, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster image = img.getRaster();
		int s = dataBuffer.length;
		int size = s;
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {

				T b = dataBuffer[s - size];
				size--;
				double b1 = 0;
				if(b instanceof Short)
				{
					b1 = ((Short) b).doubleValue();
				}
				if(b instanceof Byte)
				{
					b1 = ((Byte) b).doubleValue();
				}
				
				double tmp;
				if(to - from > 255)
				{
					tmp = ((b1 - from) / (to - from)) * 255;
					if (b1 < from)
						tmp = 0;
					if (b1 > to)
						tmp = 255;
				}
				else
				{
					tmp = b1;
				}
				//double tmp = ((b1 - from) / (to - from)) * 255;
				//	if (b1 < from)
				//		tmp = from;
				//	if (b1 > to)
				//		tmp = to;
				
				double[] arr = { tmp };
				image.setPixel(col, row, arr);
			}
		}
		img.setData(image);
	/*	try {
		    // retrieve image
		    File outputfile = new File("d:/saved.png");
		    ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
		}*/
		return img;
	}
	
	public static DicomImage readDicomFile(String fileName) throws IOException
	{
		DicomObject dcmObj = null;
		DicomInputStream din = null;
		Object[] data = null;
		DicomImage dcmImg = new DicomImage();
		try{
			//long start = System.currentTimeMillis();
			din = new DicomInputStream(new File(fileName));
			dcmObj = din.readDicomObject();
			//System.out.print((System.currentTimeMillis() - start) + "\t");
			DicomElement photometricInterpretation = dcmObj
					.get(Tag.PhotometricInterpretation);
			//photometricInterpretation.getValueAsString(arg0, arg1)
			DicomElement bitsAllocated = dcmObj.get(Tag.BitsAllocated);
			DicomElement rs = dcmObj.get(Tag.RescaleSlope);
			int bitsPerPixel = bitsAllocated.getBytes()[0];
			
			String imageType = photometricInterpretation.toString();
			isColored = true;
			if("(0028,0004) CS #12 [MONOCHROME2]".equals(imageType) || ("(0028,0004) CS #12 [MONOCHROME1]".equals(imageType)))
			{
				isColored = false;
			}
			DicomElement r = dcmObj.get(Tag.Rows);
			DicomElement c = dcmObj.get(Tag.Columns);
			rows = r.getInt(false);
			cols = c.getInt(false);
			
			dcmImg.setSeriaId(dcmObj.getString(Tag.SeriesInstanceUID));
			dcmImg.setStudyId(dcmObj.getString(Tag.StudyInstanceUID));
			
			dcmImg.setSeriaDescription(dcmObj.getString(Tag.SeriesDescription));
			dcmImg.setStudyDescription(dcmObj.getString(Tag.StudyDescription));
			dcmImg.setStudyDate(dcmObj.getString(Tag.StudyDate));
			
			if(bitsPerPixel == 16)
			{
				//start = System.currentTimeMillis();
				short[] a = dcmObj.getShorts(Tag.PixelData);
				//System.out.print((System.currentTimeMillis() - start) + "\t");
				//start = System.currentTimeMillis();
				data = ArrayUtils.toObject(a/*dcmObj.getShorts(Tag.PixelData)*/);
				//System.out.print((System.currentTimeMillis() - start) + "\t");
			}
			if(bitsPerPixel == 8)
			{
				//start = System.currentTimeMillis();
				data = /*(Object[])*/ArrayUtils.toObject(dcmObj.getBytes(Tag.PixelData));
				//System.out.print((System.currentTimeMillis() - start) + "\t");
			}
			//System.out.print((System.currentTimeMillis() - start) + "\t");
			int size = 0;
			minValue = Integer.MAX_VALUE;
			maxValue = Integer.MIN_VALUE;
			//start = System.currentTimeMillis();
			while(size != data.length)
			{
				Object tmp = data[size];
				//System.out.println(tmp);
				if(tmp instanceof Byte)
				{
					if(minValue > (Byte)tmp)
						minValue = (Byte)tmp;
					if(maxValue < (Byte)tmp)
						maxValue = (Byte)tmp;
				}
				if(tmp instanceof Short)
				{
					if(minValue > (Short)tmp)
						minValue = (Short)tmp;
					if(maxValue < (Short)tmp)
						maxValue = (Short)tmp;
				}
				size++;
			}
			//System.out.print((System.currentTimeMillis() - start) + "\t");
			System.out.print(fileName + "\t");
			dcmImg.setImageBuffer(data);
			dcmImg.setFrom(minValue);
			dcmImg.setTo(maxValue);
			dcmImg.setHeight(rows);
			dcmImg.setWidth(cols);
			dcmImg.setColor(isColored);
			dcmImg.setTagsValues(listHeader(dcmObj));
		}catch(Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
		}
		finally{
			din.close();
		}
		return dcmImg;
	}
	
	public static List<String> listHeader(DicomObject object) {
		   List<String> headers = new ArrayList<String>();
		   Iterator<DicomElement> iter = object.datasetIterator();
		   while(iter.hasNext()) {
		      DicomElement element = iter.next();
		      int tag = element.tag();
		      try {
		         String tagName = object.nameOf(tag);
		         String tagAddr = TagUtils.toString(tag);
		         String tagVR = object.vrOf(tag).toString();
		         if (tagVR.equals("SQ")) {
		            if (element.hasItems()) {
		               headers.add(tagAddr +" ["+  tagVR +"] "+ tagName);
		               listHeader(element.getDicomObject());
		               continue;
		            }
		         }    
		         String tagValue = object.getString(tag);    
		         headers.add(tagAddr +" ["+ tagVR +"] "+ tagName +" ["+ tagValue+"]");
		      } catch (Exception e) {
		         e.printStackTrace();
		      }
		   }  
		   return headers;
		}
		
//	private static BufferedImage convertDcmToBufferedImage(String fileName, int from, int to) throws Exception
//	{
//		BufferedImage jpegImage2 = null;
//		Object[] data = readDicomFile(fileName);
//		
//			if(isColored)
//			{
//				jpegImage2 = convertDataToColorImage(data);
//			}
//				
//			else {
//				
//				jpegImage2 = convertDataToGreyImage(data, from, to);
//			}
//
//		return jpegImage2;
//	}
	
	
	
	public static BufferedImage changePaletteForGreyScaleImage(BufferedImage srcImg, int[][] palette) throws Exception
	{
		int width = srcImg.getWidth();
		int height = srcImg.getHeight();
		
		BufferedImage newImage = new BufferedImage(height, width,
				BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; ++j) {
				int oldRgb = srcImg.getRaster().getSample(i, j, 0);
				int r = palette[oldRgb][0];
				int g = palette[oldRgb][1];
				int b = palette[oldRgb][2];

				int newRgb = ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8)
						| (b & 0x0ff);
				newImage.setRGB(i, j, newRgb);
			}
		}
		return newImage;
	}
	
}
