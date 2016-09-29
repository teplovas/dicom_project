package OpenGLFeatures;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;

import tools.DicomImage;
import tools.ImageHelper;

public class TestFrame {

	private static Map<String, DicomImage> dicomImages = new HashMap<String, DicomImage>();
	private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
	private final static Font boldFont = new Font("SizeButton", Font.BOLD, 16);
	private static Thread renderThread;
	private static JSlider range;
	private static Choice palettes;
	private static JScrollPane miniaturePane;
	
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
	
	private static void showImage(String fileName)
	{
		DicomImage dicomImage = dicomImages.get(fileName);
		
		int width = dicomImage.getWidth();
		int height = dicomImage.getHeight();
		int from = dicomImage.getFrom();
		int to = dicomImage.getTo();

		MainRender.setImageBuffer(dicomImage.getImageBuffer());
		MainRender.setWidth(width);
		MainRender.setHeight(height);
		MainRender.setFrom(from);
		MainRender.setTo(to);
		
		setRange(from, to);
		
		palettes.setEnabled(!dicomImage.isColor());
		if(dicomImage.isColor())
		{
			palettes.select(4);
			MainRender.notUsePalette();
		}
		
		
	}
	
	private static DicomImage readImageFromFile(String fileName)
	{
		DicomImage dicomImage = null;
		try {
			dicomImage = ImageHelper.openImage(fileName);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dicomImages.put(fileName, dicomImage);
		return dicomImage;
	}
	
	private static JLabel createMiniature(DicomImage dcmImg, String fileName)
	{
		int newWidth = dcmImg.getWidth()/10;
		int newHeight = dcmImg.getHeight()/10;
		BufferedImage bigImg = ImageHelper.getBufferedImage(dcmImg.getImageBuffer(), 
				dcmImg.getFrom(), dcmImg.getTo());
		BufferedImage smallImage = new BufferedImage(newWidth, newHeight, 
				dcmImg.isColor() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = smallImage.createGraphics();
		g.drawImage(bigImg, 0, 0, newWidth, newHeight, null);
		g.dispose();
		
		ImageIcon imageIcon = new ImageIcon(smallImage);
		JLabel imagelabel = new JLabel("", imageIcon, JLabel.CENTER);
		imagelabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showImage(fileName);
            }
        });
		
		return imagelabel;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("DICOM");
		JPanel panel = new JPanel();
		palettes = new Choice();
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
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
		
		range = new JSlider(JSlider.VERTICAL);
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
					MainRender.notUsePalette();
					break;
				}
				}

			}
		});
		palettes.setEnabled(false);

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
		
		paletteConstr.fill = GridBagConstraints.VERTICAL;
		paletteConstr.gridx = 2;
		paletteConstr.gridy = 2;
		paletteConstr.gridheight = 3;
		frame.add(range, paletteConstr);
		
		paletteConstr.fill = GridBagConstraints.BOTH;
		paletteConstr.gridx = 0;
		paletteConstr.gridy = 3;
		paletteConstr.gridheight = 2;
		
		
		miniaturePane = new JScrollPane(/*miniPanel*/);
		miniaturePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		frame.add(miniaturePane, paletteConstr);
		
		
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int returnVal = fileChooser.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					String fileName = null;
					if (files.length > 1) 
					{
						JPanel miniPanel = new JPanel();
						GridLayout gd = new GridLayout(files.length, 1);
						gd.setVgap(10);
						miniPanel.setLayout(gd);

						for (File f : files) {
							fileName = f.getAbsolutePath();
							DicomImage image = readImageFromFile(fileName);
							JLabel label = createMiniature(image, fileName);
							miniPanel.add(label);
						}

						miniaturePane.setViewportView(miniPanel);
					}
					else
					{
						fileName = files[0].getAbsolutePath();
						readImageFromFile(fileName);
					}
					showImage(fileName);
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