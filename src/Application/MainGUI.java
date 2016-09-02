package Application;

import java.awt.Desktop;
import java.io.File;

import javax.imageio.ImageIO;

import OpenGLFeatures.MainRender;
import tools.ImageHelper;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

	@SuppressWarnings("unchecked")
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
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
					openFile(file);
					MainRender.createDisplay(ImageHelper.getDataBuffer(), ImageHelper.getHeight(), ImageHelper.getWidth());
					from = ImageHelper.getMinValue();
					to = ImageHelper.getMaxValue();
					//MainRender.createTexture(ImageHelper.getDataBuffer(), ImageHelper.getWidth(), ImageHelper.getHeight());
					MainRender.loadAndPrepareShaders(from, to, 1);
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
					changeColorOfImage(1);
					break;
				}
				case "PET Color Palette": {
					changeColorOfImage(2);
					break;
				}
				case "Hot Metal Blue Color Palette": {
					changeColorOfImage(3);
					break;
				}
				case "PET 20 Step Color Palette": {
					changeColorOfImage(4);
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
			mainImage = RectangleBuilder.create().x(-100).y(-100).width(width).height(height)
					.fill(new ImagePattern(img, 0, 0, 1, 1, true)).build();

			ScrollPane scrollPane = new ScrollPane(mainImage);
			scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setFitToHeight(true);
			scrollPane.setFitToWidth(true);
			scrollPane.setManaged(true);
			
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
	
	public static void main(String[] args) {
		Application.launch(args);
	}

}
