package tools;

public class PaletteLoader {

	public static int[][] getPalette(int paletteType)
	{
		int[][] palette;
		switch (paletteType) {
		case 1:
			palette = CSVLoader.getData("palettes/hotIron.csv");
			break;
		case 2:
			palette = CSVLoader.getData("palettes/PET.csv");
			break;
		case 3:
			palette = CSVLoader.getData("palettes/hotMetallBlue.csv");
			break;
		case 4:
			palette = CSVLoader.getData("palettes/PET20Step.csv");
			break;
		default:
			palette = null;
		}
		return palette;
	}
}
