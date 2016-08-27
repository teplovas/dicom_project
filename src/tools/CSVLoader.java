package tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVLoader {

	public static int[][] getData(String csvFile)
	{
		int[][] resultArray = new int[256][3];
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(csvFile));
			int i = 0, j = 0; 
			while ((line = br.readLine()) != null) {
				String[] pixelData = line.split(csvSplitBy);
				for(String s : pixelData)
				{
					resultArray[i][j] = Integer.valueOf(s);
					j++;
				}
				j = 0;
				i++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return resultArray;
	}
}
