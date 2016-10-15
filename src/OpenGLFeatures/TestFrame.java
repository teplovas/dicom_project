package OpenGLFeatures;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import Application.DicomTagsDialog;
import tools.DicomImage;
import tools.ImageHelper;

public class TestFrame extends JFrame {

	private static final long serialVersionUID = 3195487268099554955L;
	private Map<String, DicomImage> dicomImages = new HashMap<String, DicomImage>();
	private final AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
	private final Font boldFont = new Font("SizeButton", Font.BOLD, 16);
	private final Border border = BorderFactory.createLineBorder(Color.blue, 1);
	private Thread renderThread;
	private RangeSlider range;
	private Choice palettes;
	private JScrollPane miniaturesPane;
	private List<JLabel> labels = new ArrayList<JLabel>();
	private String currentFileName;
	private InfoAction infoAction;
	Button plusButton;
	Button minusButton;
	JFileChooser fileChooser;
	OpenAction openAction;

	public TestFrame() {
		super("DICOM");
	}

	public void createFrame() {
		JPanel panel = new JPanel();
		
		JToolBar toolBar = new JToolBar();
		toolBar.setRollover(true);
		addToolBarElements(toolBar);
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Dicom files", "dcm");
		fileChooser.setFileFilter(filter);

		plusButton = new Button("+");
		plusButton.setFont(boldFont);

		minusButton = new Button("-");
		minusButton.setFont(boldFont);

	    range = new RangeSlider();
	    range.setOrientation(JSlider.VERTICAL);
		range.setEnabled(false);
		setRange(-128, 127);

		this.setLayout(new GridBagLayout());
		final Canvas canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(1000, 800));

		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				newCanvasSize.set(canvas.getSize());
			}
		});

		this.addWindowFocusListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				canvas.requestFocusInWindow();
			}
		});

		
		GridBagConstraints toolB = new GridBagConstraints();
		toolB.fill = GridBagConstraints.HORIZONTAL;
		toolB.weightx = 0.5;
		toolB.gridx = 0;
		toolB.gridy = 0;
		toolB.gridwidth = 3;
		this.add(toolBar, toolB);
		
		GridBagConstraints button = new GridBagConstraints();
		button.fill = GridBagConstraints.NONE;
		button.weightx = 0.5;
		button.gridx = 0;
		button.gridy = 0;

		GridBagConstraints image = new GridBagConstraints();
		image.fill = GridBagConstraints.BOTH;
		image.gridx = 1;
		image.gridy = 0;
		image.gridheight = 5;

		//panel.setPreferredSize(new Dimension(800, 600));
		panel.setMinimumSize(new Dimension(1000, 600));
		//panel.setMaximumSize(new Dimension(800, 600));
		panel.setLayout(new BorderLayout());
		panel.add(canvas, BorderLayout.CENTER);
		this.add(panel, image);

		button.gridx = 2;
		button.gridy = 1;
		this.add(plusButton, button);

		GridBagConstraints paletteConstr = new GridBagConstraints();
		paletteConstr.fill = GridBagConstraints.HORIZONTAL;
		paletteConstr.gridx = 0;
		paletteConstr.gridy = 1;
		JLabel label = new JLabel("                                            ");
		label.setSize(200, label.getHeight());
		label.setPreferredSize(new Dimension(200, 0));
		//label.setVisible(false);
		this.add(label, paletteConstr);

		button.gridx = 2;
		button.gridy = 2;
		this.add(minusButton, button);

		paletteConstr.fill = GridBagConstraints.VERTICAL;
		paletteConstr.gridx = 2;
		paletteConstr.gridy = 3;
		paletteConstr.gridheight = 3;
		this.add(range, paletteConstr);

		paletteConstr.fill = GridBagConstraints.BOTH;
		paletteConstr.gridx = 0;
		paletteConstr.gridy = 2;
		paletteConstr.gridheight = 3;

		miniaturesPane = new JScrollPane();
		miniaturesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		miniaturesPane.setPreferredSize(new Dimension(200, 0));
		this.add(miniaturesPane, paletteConstr);

		setListeners();

		try {
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setMinimumSize(new Dimension(800, 600));
			this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
			this.setVisible(true);
			//this.pack();

			Runnable rendering = new Runnable() {
				public void run() {
					MainRender.initDisplay(canvas);
					MainRender.loadShadersAndPallettes();
					MainRender.startRendering();
				}
			};
			renderThread = new Thread(rendering);
			renderThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addToolBarElements(JToolBar toolBar) {
		
		ImageIcon imageIcon = new ImageIcon("res/openIcon.png");
		openAction = new OpenAction(imageIcon, "Открыть файл"); 
        toolBar.add(openAction);
        
        imageIcon = new ImageIcon("res/infoIcon.png");
        infoAction = new InfoAction(imageIcon, "Информация о файле"); 
        toolBar.add(infoAction);
        
        toolBar.addSeparator();
        JLabel paletteLabel = new JLabel("Палитра");
        toolBar.add(paletteLabel);
        
        palettes = new Choice();
        palettes.add("");
        palettes.add("Hot Iron Color");
		palettes.add("PET Color");
		palettes.add("Hot Metal Blue Color");
		palettes.add("PET 20 Step Color");
		palettes.select(0);
		palettes.setEnabled(false);
		
		toolBar.add(palettes);
    }
	
	private void setListeners()
	{
		plusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				MainRender.changeScale(0.1f);
			}
		});
		minusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				MainRender.changeScale(-0.1f);
			}
		});
		range.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				MainRender.setFrom(range.getValue());
				MainRender.setTo(range.getUpperValue());
			}
		});
		palettes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {

				switch (palettes.getSelectedItem()) {
				case "Hot Iron Color": {
					MainRender.changePalette("hotIron");
					break;
				}
				case "PET Color": {
					MainRender.changePalette("pet");
					break;
				}
				case "Hot Metal Blue Color": {
					MainRender.changePalette("hotMetalBlue");
					break;
				}
				case "PET 20 Step Color": {
					MainRender.changePalette("pet20");
					break;
				}
				case "": {
					MainRender.notUsePalette();
					break;
				}
				}

			}
		});
	}

	private void setRange(int from, int to) {
		range.setMinimum(from);
		range.setMaximum(to);
		range.setValue(from);
		range.setUpperValue(to);
	}

	/**
	 * Отрисовать изображение
	 * 
	 * @param fileName
	 */
	private void showImage(String fileName) {
		this.currentFileName = fileName;
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
		if (dicomImage.isColor()) {
			palettes.select(0);
			MainRender.notUsePalette();
		}
		range.setEnabled(true);
	}

	/**
	 * Считать изображение из файла и поместить в мапу
	 * 
	 * @param fileName
	 * @return
	 */
	private DicomImage readImageFromFile(String fileName) {
		DicomImage dicomImage = null;
		try {
			dicomImage = ImageHelper.openImage(fileName);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		dicomImages.put(fileName, dicomImage);
		return dicomImage;
	}

	/**
	 * Сделать миниатюру для изображения
	 * 
	 * @param dcmImg
	 * @param fileName
	 * @return
	 */
	private JLabel createMiniature(DicomImage dcmImg, String fileName) {
		int newWidth = dcmImg.getWidth() / 10;
		int newHeight = dcmImg.getHeight() / 10;

		BufferedImage bigImg = ImageHelper.getBufferedImage(dcmImg.getImageBuffer(), dcmImg.getFrom(), dcmImg.getTo());
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
				clearLabelsBorder();
				imagelabel.setBorder(border);
			}
		});

		return imagelabel;
	}

	private void clearLabelsBorder() {
		for (JLabel label : labels) {
			label.setBorder(null);
		}
	}
	
	class OpenAction extends ToolBarAction {
		private static final long serialVersionUID = 3876578108001749755L;

		public OpenAction(Icon icon, String description) {
			super(icon, description);
		}

		public void actionPerformed(ActionEvent e) {
			int returnVal = fileChooser.showOpenDialog(TestFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] files = fileChooser.getSelectedFiles();
				String fileName = null;
				dicomImages.clear();
				labels.clear();
				if (files.length > 1) {
					JPanel miniPanel = new JPanel();
					GridLayout gd = new GridLayout(files.length, 1);
					gd.setVgap(10);
					miniPanel.setLayout(gd);

					for (File f : files) {
						fileName = f.getAbsolutePath();
						long start = System.currentTimeMillis();
						DicomImage image = readImageFromFile(fileName);
						// System.out.println("read: " +
						// (System.currentTimeMillis() - start));
						start = System.currentTimeMillis();
						JLabel label = createMiniature(image, fileName);
						// System.out.println("mini: " +
						// (System.currentTimeMillis() - start));
						labels.add(label);
						miniPanel.add(label);
					}

					miniaturesPane.setViewportView(miniPanel);
				} else {
					fileName = files[0].getAbsolutePath();
					readImageFromFile(fileName);
				}
				showImage(fileName);
			}
		}
	}

	class InfoAction extends ToolBarAction {
		private static final long serialVersionUID = 6140859768252974830L;

		public InfoAction(Icon icon, String description) {
			super(icon, description);
		}

		public void actionPerformed(ActionEvent e) {
			if(currentFileName != null)
			{
				DicomTagsDialog dlg = new DicomTagsDialog(TestFrame.this, dicomImages.get(currentFileName));
				dlg.setSize(500, 500);
			}
		}
	}
	
	abstract class ToolBarAction extends AbstractAction {
		public ToolBarAction(Icon icon, String description) {
			super("", icon);
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke('c', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			putValue(SHORT_DESCRIPTION, description);
		}
		abstract public void actionPerformed(ActionEvent e);
	}

	public static void main(String[] args) {
		try {
		      UIManager
		          .setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } catch (Exception ex) {
		      System.err.println("Error loading L&F: " + ex);
		    }

		TestFrame frame = new TestFrame();
		frame.addWindowListener(new WindowAdapter() {
		      public void windowClosing(WindowEvent e) {
		        System.exit(0);
		      }
		    });
		frame.createFrame();
	}
}