package OpenGLFeatures;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import tools.ImageHelper;

public class TestFrame {

	private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
	private final static Font boldFont = new Font("SizeButton", Font.BOLD, 16);
	private static Thread renderThread;
	private static JSlider range;
	
	private static void setRange(int from, int to)
	{
		range.setMinimum(from);
		range.setMaximum(to);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( from, new JLabel("From") );
		labelTable.put( to, new JLabel("To") );
		range.setLabelTable(labelTable);
		range.setPaintLabels(true);
		range.setValue((from + to) / 2);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("DICOM");
		JPanel panel = new JPanel();
		Choice palettes = new Choice();
		
		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
		        "Dicom files", "dcm");
		fileChooser.setFileFilter(filter);
		Button openButton = new Button("Open File...");

		Button plusButton = new Button("+");
		plusButton.setFont(boldFont);
		plusButton.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent ae)
		  {
			  MainRender.changeScale(0.1f);
		  }
		 });
		
		Button minusButton = new Button("-");
		minusButton.setFont(boldFont);
		minusButton.addActionListener(new ActionListener(){
			  public void actionPerformed(ActionEvent ae)
			  {
				  MainRender.changeScale(-0.1f);
			  }
			 });
		
		range = new JSlider(JSlider.HORIZONTAL);
		setRange(0, 10);	
		
		palettes.add("Hot Iron Color Palette");
		palettes.add("PET Color Palette");
		palettes.add("Hot Metal Blue Color Palette");
		palettes.add("PET 20 Step Color Palette");
		palettes.add("Default Color Palette");
		
		palettes.select(4);

		palettes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				
				switch (palettes.getSelectedItem()) {
				case "Hot Iron Color Palette": {
					MainRender.changePalette("hotIron");
					break;
				}
				case "PET Color Palette": {
					MainRender.changePalette("pet");
					break;
				}
				case "Hot Metal Blue Color Palette": {
					MainRender.changePalette("hotMetalBlue");
					break;
				}
				case "PET 20 Step Color Palette": {
					MainRender.changePalette("pet20");
					break;
				}
				case "Default Color Palette": {
					MainRender.changePalette(null);
					break;
				}
				}

			}
		});

		frame.setLayout(new GridBagLayout());
		final Canvas canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(800, 600));

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
		
		GridBagConstraints button = new GridBagConstraints();
		button.fill = GridBagConstraints.NONE;
		button.weightx = 0.5;
		button.gridx = 0;
		button.gridy = 0;
		frame.add(openButton, button);
		
		GridBagConstraints image = new GridBagConstraints();
		image.fill = GridBagConstraints.BOTH;	
		image.gridx = 1;
		image.gridy = 0;
		image.gridheight = 5;
		 
		panel.setPreferredSize(new Dimension(800, 600));
		panel.setMinimumSize(new Dimension(800, 600));
		panel.setMaximumSize(new Dimension(800, 600));
		panel.setLayout(new BorderLayout());
		panel.add(canvas, BorderLayout.CENTER);
		frame.add(panel, image);
		
		button.gridx = 2;
		button.gridy = 0;
		frame.add(plusButton, button);
		
		GridBagConstraints paletteConstr = new GridBagConstraints();
		paletteConstr.fill = GridBagConstraints.NONE;
		paletteConstr.gridx = 0;
		paletteConstr.gridy = 2;
		frame.add(palettes, paletteConstr);
		
		button.gridx = 2;
		button.gridy = 1;
		frame.add(minusButton, button);
		
		paletteConstr.fill = GridBagConstraints.HORIZONTAL;
		paletteConstr.gridx = 0;
		paletteConstr.gridy = 4;
		frame.add(range, paletteConstr);
		
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int returnVal = fileChooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String fileName = fileChooser.getSelectedFile().getAbsolutePath();
					try {
						ImageHelper.openImage(fileName);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
//					if(renderThread != null && renderThread.getState() != Thread.State.TERMINATED)
//					{
//						renderThread.interrupt();
//					}
					int width = ImageHelper.getWidth();
					int height = ImageHelper.getHeight();
					int from = ImageHelper.getMinValue();
					int to = ImageHelper.getMaxValue();

					MainRender.setImageBuffer(ImageHelper.getDataBuffer());
					MainRender.setWidth(width);
					MainRender.setHeight(height);
					MainRender.setFrom(from);
					MainRender.setTo(to);
					
					setRange(from, to);
					//MainRender.renderImage(ImageHelper.getDataBuffer(), width, height);
//					Runnable rendering = new Runnable() {
//						public void run() {
//							MainRender.tmpFunc(ImageHelper.getDataBuffer(), width, height, canvas);
//						}
//					};
//					renderThread = new Thread(rendering);
//					renderThread.start();
				}
			}
		});

		try {
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setPreferredSize(new Dimension(1024, 786));
			frame.setMinimumSize(new Dimension(800, 600));
			frame.pack();
			frame.setVisible(true);
			
			Runnable rendering = new Runnable() {
			public void run() {
				MainRender.initDisplay(canvas);
				MainRender.loadShadersAndPallettes();
				//MainRender.loadPalettes();
				MainRender.startRendering();
			}
		};
		renderThread = new Thread(rendering);
		renderThread.start();
		//MainRender.renderImage(ImageHelper.getDataBuffer(), ImageHelper.getWidth(), ImageHelper.getHeight());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}