package OpenGLFeatures;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicReference;

import tools.ImageHelper;

public class TestFrame {

	private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();

	public static void main(String[] args) {
		Frame frame = new Frame("Test");
		Choice palettes = new Choice();
		palettes.add("Hot Iron Color Palette");
		palettes.add("PET Color Palette");
		palettes.add("Hot Metal Blue Color Palette");
		palettes.add("PET 20 Step Color Palette");
		palettes.add("Default Color Palette");

		palettes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				;
				switch (palettes.getSelectedItem()) {
				case "Hot Iron Color Palette": {
					MainRender.changePalette("hotIron");// changeColorOfImage(1);
					break;
				}
				case "PET Color Palette": {
					MainRender.changePalette("pet");// changeColorOfImage(2);
					break;
				}
				case "Hot Metal Blue Color Palette": {
					MainRender.changePalette("hotMetalBlue");// changeColorOfImage(3);
					break;
				}
				case "PET 20 Step Color Palette": {
					MainRender.changePalette("pet20");// changeColorOfImage(4);
					break;
				}
				case "Default Color Palette": {

					break;
				}
				}

			}
		});

		frame.setLayout(new BorderLayout());
		final Canvas canvas = new Canvas();

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				newCanvasSize.set(canvas.getSize());
			}
		});

		frame.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				canvas.requestFocusInWindow();
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				MainRender.destroy();
			}
		});

		frame.add(canvas, BorderLayout.CENTER);
		frame.add(palettes, BorderLayout.SOUTH);

		try {
			frame.setPreferredSize(new Dimension(1024, 786));
			frame.setMinimumSize(new Dimension(800, 600));
			frame.pack();
			frame.setVisible(true);
			try {
				ImageHelper.openImage("d://image.dcm");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int width = ImageHelper.getWidth();
			int height = ImageHelper.getHeight();

			System.out.println("Runnable running");
			MainRender.tmpFunc(ImageHelper.getDataBuffer(), width, height, canvas);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}