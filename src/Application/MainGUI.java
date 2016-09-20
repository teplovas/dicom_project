package Application;

import java.awt.Canvas;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import OpenGLFeatures.MainRender;
import tools.ImageHelper;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainGUI extends Application {

	private Desktop desktop = Desktop.getDesktop();
	private GridPane inputGridPane;
	private Rectangle mainImage;
	private Integer from;
	private Integer to;
	private Double currentScale = 1.0;

	@Override
	public void start(final Stage stage) {
		ImageIO.scanForPlugins();
		stage.setTitle("DICOM Viewer");
		stage.setFullScreen(true);
		stage.setMinHeight(700);
		stage.setMinWidth(900);
		
		final FileChooser fileChooser = new FileChooser();

		final Button openButton = new Button("Open File");

		final Button plusButton = new Button("", new ImageView(new Image("file:imgs/plus.jpg")));
		final Button minusButton = new Button("", new ImageView(new Image("file:imgs/minus.png")));
		final ComboBox<String> paletts;
		final Slider darknessSlider = new Slider();

		openButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				File file = new File("d://image.dcm");//fileChooser.showOpenDialog(stage);
				if (file != null) {
					openFile(file);
					from = ImageHelper.getMinValue();
					to = ImageHelper.getMaxValue();
					//MainRender.tmpFunc(ImageHelper.getDataBuffer(), ImageHelper.getWidth(), ImageHelper.getHeight());
					darknessSlider.setMax(to);
					darknessSlider.setMin(from);
					currentScale = 1.0;
				}
			}
		});

		plusButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				if (mainImage != null) {
					changeCurrentScale(0.1);
				}
			}
		});

		minusButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				if (mainImage != null) {
					changeCurrentScale(-0.1);
				}
			}
		});

		ObservableList<String> options = FXCollections.observableArrayList("Hot Iron Color Palette",
				"PET Color Palette", "Hot Metal Blue Color Palette", "PET 20 Step Color Palette",
				"Default Color Palette");
		paletts = new ComboBox<String>(options);

		paletts.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue ov, String t, String t1) {
				switch (ov.getValue().toString()) {
				case "Hot Iron Color Palette": {
					MainRender.changePalette("hotIron");//changeColorOfImage(1);
					break;
				}
				case "PET Color Palette": {
					MainRender.changePalette("pet");//changeColorOfImage(2);
					break;
				}
				case "Hot Metal Blue Color Palette": {
					MainRender.changePalette("hotMetalBlue");//changeColorOfImage(3);
					break;
				}
				case "PET 20 Step Color Palette": {
					MainRender.changePalette("pet20");//changeColorOfImage(4);
					break;
				}
				case "Default Color Palette": {
					changeColorOfImage(0);
					break;
				}
				}
			}
		});
				
		inputGridPane = new GridPane();
		inputGridPane.setHgap(6);
		inputGridPane.setVgap(6);
		// inputGridPane.setGridLinesVisible(true);

		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(10);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(80);
		ColumnConstraints column3 = new ColumnConstraints();
		column3.setPercentWidth(10);
		inputGridPane.getColumnConstraints().add(column1);
		inputGridPane.getColumnConstraints().add(column2);
		inputGridPane.getColumnConstraints().add(column3);

		RowConstraints row1 = new RowConstraints();
		row1.setPercentHeight(1);
		RowConstraints row2 = new RowConstraints();
		row2.setPercentHeight(1);
		inputGridPane.getRowConstraints().add(row1);
		inputGridPane.getRowConstraints().add(row2);

		GridPane.setConstraints(openButton, 0, 0);
		GridPane.setConstraints(paletts, 0, 1);
		GridPane.setConstraints(plusButton, 2, 0);
		GridPane.setConstraints(minusButton, 2, 1);
		
		GridPane.setConstraints(darknessSlider, 2, 2);

		inputGridPane.getChildren().addAll(openButton, paletts, plusButton, minusButton, darknessSlider);

		final Pane rootGroup = new VBox(12);
		rootGroup.getChildren().addAll(inputGridPane);
		rootGroup.setPadding(new Insets(12, 12, 12, 12));

		stage.setScene(new Scene(rootGroup));
		stage.show();
	}

	private void changeCurrentScale(double scale) {
		currentScale += scale;
		if (currentScale <= 0) {
			currentScale = 0.1;
		}
		mainImage.setScaleX(currentScale);
		mainImage.setScaleY(currentScale);
	}

	private void openFile(File file) {
		try {

			Image img = getImage(file.getAbsolutePath());
			double width = img.getWidth();
			double height = img.getHeight();
			//ByteBuffer image = MainRender.tmpFunc(ImageHelper.getDataBuffer(), ImageHelper.getWidth(), ImageHelper.getHeight());
			//img = /*SwingFXUtils.toFXImage(*/getBufIm(image, (int)width, (int)height)/*, null)*/;
			
			//mainImage = RectangleBuilder.create().x(-100).y(-100).width(width).height(height)
			//		.fill(new ImagePattern(img, 0, 0, 1, 1, true)).build();

			Canvas canvas = new Canvas();
			SwingNode swingNode = new SwingNode();
			JPanel frame = new JPanel();
			frame.add(canvas);
	        swingNode.setContent(frame);
			ScrollPane scrollPane = new ScrollPane(swingNode);
			scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setFitToHeight(true);
			scrollPane.setFitToWidth(true);
			scrollPane.setManaged(true);
			MainRender.tmpFunc(ImageHelper.getDataBuffer(), ImageHelper.getWidth(), ImageHelper.getHeight(), canvas);
			GridPane.setHalignment(scrollPane, HPos.CENTER);
			GridPane.setValignment(scrollPane, VPos.TOP);
			inputGridPane.add(scrollPane, 1, 1, 1, 2); //.getChildren().add(mainImage);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	private void changeColorOfImage(int paletteType)
	{
		try {
			Image img = ImageHelper.changeOpendGreyImage(from, to, paletteType);
			mainImage.setFill(new ImagePattern(img));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Image getImage(String fileName) {
		try {
			return ImageHelper.openImage(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private WritableImage getBufIm(ByteBuffer bytes, int width, int height)
	{
		WritableImage image = new WritableImage(width, height);
		for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                int i = (x + (width * y)) * 4;
                int r = bytes.get(i) & 0xFF;
                int g = bytes.get(i + 1) & 0xFF;
                int b = bytes.get(i + 2) & 0xFF;

                image.getPixelWriter().setArgb(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
		return image;
	}
	

	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
