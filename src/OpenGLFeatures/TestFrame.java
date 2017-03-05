package OpenGLFeatures;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.eclipse.swt.internal.win32.MEASUREITEMSTRUCT;

import Application.*;
import tools.DicomImage;
import tools.ImageHelper;

import de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel;

public class TestFrame extends JFrame{

	private static final long serialVersionUID = 3195487268099554955L;
	private Map<String, DicomImage> dicomImages = new LinkedHashMap<String, DicomImage>();
	private final AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
	private Thread renderThread;
	private RangeSlider range;
	private JScrollPane miniaturesPane;
	private JSlider changeImageSlider;
	private Map<String, JLabel> labels = new LinkedHashMap<String, JLabel>();
	private String currentFileName;
	private Map<String, DicomSeria> series = new HashMap<String, DicomSeria>();
	private DicomSeria currentSeria;
	private boolean isMultipleSelection = false;

	JFileChooser fileChooser;

	public TestFrame() {
		super("DICOM");
	}

	public void createFrame() {
		JPanel panel = new JPanel();
		
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		JMenuBar menuBar = new JMenuBar();
		
		ImageIcon imageIcon = new ImageIcon("res/fileMenu.png");
		JMenu fileMenu = new JMenu("");
		fileMenu.setIcon(imageIcon);
		fileMenu.setToolTipText("Файл");
		
		fileMenu.setHorizontalTextPosition(SwingConstants.CENTER);
		fileMenu.setVerticalTextPosition(SwingConstants.BOTTOM);
		menuBar.add(fileMenu);
		
		menuBar.add( Box.createHorizontalStrut( 10 ) );
		
		imageIcon = new ImageIcon("res/openIcon.png");
		JMenuItem openFileItem = new JMenuItem("Открыть", imageIcon);
		openFileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openFileActions();
			}
		});
	    fileMenu.add(openFileItem);
	    
	    imageIcon = new ImageIcon("res/infoIcon.png");
	    JMenuItem infoFileItem = new JMenuItem("Информация о файле", imageIcon);
		infoFileItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentFileName != null)
				{
					DicomTagsDialog dlg = new DicomTagsDialog(TestFrame.this, dicomImages.get(currentFileName));
					dlg.setSize(500, 500);
				}
			}
		});
	    fileMenu.add(infoFileItem);
	    
	    //===================Palette================================
	    imageIcon = new ImageIcon("res/paletteIcon.png");
		JMenu paletteMenu = new JMenu("");
		paletteMenu.setIcon(imageIcon);
		paletteMenu.setToolTipText("Палитра");
		
		paletteMenu.setHorizontalTextPosition(SwingConstants.CENTER);
		paletteMenu.setVerticalTextPosition(SwingConstants.BOTTOM);
		menuBar.add(paletteMenu);
		
		paletteMenu.add(createPaletteItem("Черно-белая", "def", null));
		paletteMenu.add(createPaletteItem("Hot Iron", "hotIron", new ImageIcon("res/hotIronIcon.png")));
		paletteMenu.add(createPaletteItem("PET Color", "pet", new ImageIcon("res/petIcon.png")));
		paletteMenu.add(createPaletteItem("Hot Metal Blue Color", "hotMetalBlue", 
				new ImageIcon("res/hotMetalBlueIcon.png")));
		paletteMenu.add(createPaletteItem("PET 20 Step Color", "pet20", new ImageIcon("res/pet20Icon.png")));
		
		//===================Scroll================================
		imageIcon = new ImageIcon("res/scrollIcon.png");
		JMenu scrollMenu = new JMenu("");
		
		JCheckBoxMenuItem multSelectMenuItem = new JCheckBoxMenuItem("Серия кадров");

	    ActionListener aListener = new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	    	  AbstractButton aButton = (AbstractButton) event.getSource();
	          isMultipleSelection = aButton.getModel().isSelected();
	      }
	    };
	    multSelectMenuItem.addActionListener(aListener);
		scrollMenu.setIcon(imageIcon);
		scrollMenu.add(multSelectMenuItem);
		menuBar.add(scrollMenu);
		
		//===================Mesurements===================================
		imageIcon = new ImageIcon("res/mesurementsIcon.png");
		JMenu mesureMenu = new JMenu("");
		mesureMenu.setToolTipText("Измерения");
		
		JCheckBoxMenuItem mesureMenuItem = new JCheckBoxMenuItem("Длина");

	    aListener = new ActionListener() {
	      public void actionPerformed(ActionEvent event) {
	    	  AbstractButton aButton = (AbstractButton) event.getSource();
	    	  //MainRender.setMesurements(aButton.getModel().isSelected());
	    	  RenderingLoop.setMesurements(aButton.getModel().isSelected());
	      }
	    };
	    mesureMenuItem.addActionListener(aListener);
	    mesureMenu.add(mesureMenuItem);
		
		mesureMenu.setIcon(imageIcon);
		menuBar.add(mesureMenu);
		
		//===================Invert================================
		JButton invertMenu = new JButton("");
		invertMenu.setToolTipText("Инверитровать цвета");
		invertMenu.addActionListener(new ActionListener() {
			private boolean isSelected = false;
			public void actionPerformed(ActionEvent e) {
				isSelected = !isSelected;
				//MainRender.setInvert(isSelected);
				RenderingLoop.setInvert(isSelected);
			}
		});
		imageIcon = new ImageIcon("res/invertIcon.png");
		invertMenu.setIcon(imageIcon);
		menuBar.add(invertMenu);
		
		//===================Rotate================================
		JButton rotateMenu = new JButton("");
		rotateMenu.setToolTipText("Поворот на 90°");
		rotateMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//MainRender.setRotate(true);
				RenderingLoop.setRotate(true);
			}
		});
		imageIcon = new ImageIcon("res/rotateIcon.png");
		rotateMenu.setIcon(imageIcon);
		menuBar.add(rotateMenu);
		
		//===================Zoom In================================
		JButton zoomIn = new JButton("");
		zoomIn.setToolTipText("Увеличить");
		zoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//MainRender.changeScale(.1f);
				RenderingLoop.changeScale(.1f);
			}
		});
		imageIcon = new ImageIcon("res/zoomInIcon.png");
		zoomIn.setIcon(imageIcon);
		menuBar.add(zoomIn);
		
		//================Zoom Out==================================
		JButton zoomOut = new JButton("");
		zoomOut.setToolTipText("Уменьшить");
		zoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//MainRender.changeScale(-.1f);
				RenderingLoop.changeScale(-.1f);
			}
		});
		imageIcon = new ImageIcon("res/zoomOutIcon.png");
		zoomOut.setIcon(imageIcon);
		menuBar.add(zoomOut);
		//===========================================================
		
	    this.setJMenuBar(menuBar);
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Dicom файл", "dcm");
		fileChooser.setFileFilter(filter);

	    range = new RangeSlider();
	    range.setOrientation(JSlider.VERTICAL);
		range.setEnabled(false);
		setRange(-128, 127);
		
		changeImageSlider = new JSlider(JSlider.VERTICAL);
		changeImageSlider.setMinimum(0);
		changeImageSlider.setInverted(true);
		changeImageSlider.setVisible(false);

		this.setLayout(new GridBagLayout());
		final Canvas canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(800, 800));

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

		GridBagConstraints constraint = new GridBagConstraints();
		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.gridx = 0;
		constraint.gridy = 1;
		JLabel label = new JLabel("                                               ");
		this.add(label, constraint);
		
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridx = 0;
		constraint.gridy = 2;
		constraint.gridheight = 3;

		miniaturesPane = new JScrollPane();
		miniaturesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		miniaturesPane.setPreferredSize(new Dimension(200, 0));
		miniaturesPane.setFocusable(true);
		this.add(miniaturesPane, constraint);
		
		GridBagConstraints imageConstraint = new GridBagConstraints();
		imageConstraint.fill = GridBagConstraints.BOTH;
		imageConstraint.ipadx = 200;
		imageConstraint.gridx = 1;
		imageConstraint.gridy = 0;
		imageConstraint.gridheight = 5;

		GTransparentPanel trans = new GTransparentPanel();
		//canvas.add
		
		//panel.setPreferredSize(new Dimension(800, 600));
		panel.setMinimumSize(new Dimension(1000, 600));
		//panel.setMaximumSize(new Dimension(800, 600));
		panel.setLayout(new BorderLayout());
		panel.add(canvas, BorderLayout.CENTER);
		this.add(panel, imageConstraint);

		constraint.fill = GridBagConstraints.BOTH;
		//constraint.ipadx = 200;
		constraint.gridx = 2;
		constraint.gridy = 3;
		constraint.gridheight = 3;
		this.add(changeImageSlider, constraint);
		
		constraint.gridx = 3;
		this.add(range, constraint);

		setListeners();

		try {
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setMinimumSize(new Dimension(800, 600));
			this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
			this.setVisible(true);
			//this.pack();

			Runnable rendering = new Runnable() {
				public void run() {
//					MainRender.initDisplay(canvas);
//					MainRender.loadShadersAndPallettes();
//					MainRender.startRendering();
					RenderingLoop.init(canvas);
					RenderingLoop.startRender();
				}
			};
			renderThread = new Thread(rendering);
			renderThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JMenuItem createPaletteItem(String name, String code, Icon icon)
	{
		JMenuItem peletteItem = new JMenuItem(name, icon);
		peletteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(currentFileName != null)
				{
					if(!"def".equals(code))
					{
						//MainRender.changePalette(code);
						RenderingLoop.changePalette(code);
					}
					else
					{
						//MainRender.notUsePalette();
						RenderingLoop.notUsePalette();
					}
				}
			}
		});
		return peletteItem;
	}
	

	private void setListeners()
	{
		range.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
//				MainRender.setFrom(range.getValue());
//				MainRender.setTo(range.getUpperValue());
				RenderingLoop.setFrom(range.getValue());
				RenderingLoop.setTo(range.getUpperValue());
			}
		});
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(isMultipleSelection)
				{
					int notches = e.getWheelRotation();
					int newVal = changeImageSlider.getValue();
				       if (notches < 0) {
				           //up
				    	   newVal = (newVal == changeImageSlider.getMaximum()) ? newVal 
				    			   : newVal + 1;				    	   
				       } else {
				          //down
				    	   newVal = (newVal == changeImageSlider.getMinimum()) ? newVal 
				    			   : newVal - 1;
				       }
				       changeImageSlider.setValue(newVal);
				}
			}
			
		});
		
		changeImageSlider.addChangeListener(new ChangeListener() {
			private int currentValue = 0;
			@Override
			public void stateChanged(ChangeEvent e) {
				if(currentFileName == null || currentSeria == null)
				{
					return;
				}
				if(currentValue > changeImageSlider.getValue())
				{
					changeImageDown();
				}
				else if(currentValue < changeImageSlider.getValue())
				{
					changeImageUp();
				}
				currentValue = changeImageSlider.getValue();
			}
		});
	}
	
	private void changeImageUp()
	{
		Iterator<String> it = currentSeria.getImages().iterator();
		int imgNumber = 0;
 	   while(it.hasNext())
 	   {
 		   imgNumber++;
 		   if(it.next().equals(currentFileName))
 		   {
 			   if(it.hasNext())
 			   {
 				   //MainRender.setCurrentImageNumber(imgNumber);
 				  RenderingLoop.setCurrentImageNumber(imgNumber);
 				   showImage(it.next(), false);
 			   }
 			   break;
 		   }
 	   }
	}
	
	private void changeImageDown()
	{
		List<String> reverse = new ArrayList<String>(currentSeria.getImages());
 	   Collections.reverse(reverse);
 	   Iterator<String> it = reverse.iterator();
 	   int imgNumber = dicomImages.size();
 	   while(it.hasNext())
 	   {
 		   imgNumber--;
 		   if(it.next().equals(currentFileName))
 		   {
 			   if(it.hasNext())
 			   {
 				   //MainRender.setCurrentImageNumber(imgNumber);
 				  RenderingLoop.setCurrentImageNumber(imgNumber);
 				   showImage(it.next(), false);
 			   }
 			   break;
 		   }
 	   }
	}

	private void setRange(int from, int to) {
		range.setMinimum(from);
		range.setMaximum(to);
		range.setValue(from);
		range.setUpperValue(to);
	}

	/**
	 * Îòðèñîâàòü èçîáðàæåíèå
	 * 
	 * @param fileName
	 */
	private void showImage(String fileName, boolean isChangeRange) {
		this.currentFileName = fileName;
		DicomImage dicomImage = dicomImages.get(fileName);

		int width = dicomImage.getWidth();
		int height = dicomImage.getHeight();
		int from = dicomImage.getFrom();
		int to = dicomImage.getTo();

//		MainRender.setImageBuffer(dicomImage.getImageBuffer());
//		MainRender.setSize(width, height);
//		MainRender.setPixelSpacing(dicomImage.getPixelSpacing());
		RenderingLoop.loadImage(dicomImage);
		
		if(isChangeRange)
		{
			setRange(from, to);
//			MainRender.setFrom(from);
//			MainRender.setTo(to);
			RenderingLoop.setFrom(from);
			RenderingLoop.setTo(to);
		}

		if (dicomImage.isColor()) {
			//MainRender.notUsePalette();
			RenderingLoop.notUsePalette();
		}
		this.setTitle("DICOM(" + fileName + ")");
	}

	/**
	 * Ñ÷èòàòü èçîáðàæåíèå èç ôàéëà è ïîìåñòèòü â ìàïó
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
	 * Ñäåëàòü ìèíèàòþðó äëÿ èçîáðàæåíèÿ
	 * 
	 * @param dcmImg
	 * @param seriaName
	 * @return
	 */
	private JLabel createMiniature(DicomImage dcmImg, String seriaName, Integer count) {
		int newWidth = dcmImg.getWidth() / 10;
		int newHeight = dcmImg.getHeight() / 10;

		BufferedImage bigImg = ImageHelper.getBufferedImage(dcmImg.getImageBuffer(), dcmImg.getFrom(), dcmImg.getTo()
				, dcmImg.getWidth(), dcmImg.getHeight());
		BufferedImage smallImage = new BufferedImage(newWidth, newHeight,
				dcmImg.isColor() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY);
		Graphics g = smallImage.createGraphics();
		g.drawImage(bigImg, 0, 0, newWidth, newHeight, null);
		g.dispose();

		ImageIcon imageIcon = new ImageIcon(smallImage);
		JLabel imagelabel = new JLabel("", imageIcon, JLabel.CENTER);
		imagelabel.setText(count + "");
		
		imagelabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				currentSeria = series.get(seriaName);
				//MainRender.setNumberOfImages(currentSeria.getImages().size());
				RenderingLoop.setNumberOfImages(currentSeria.getImages().size());
				showImage(series.get(seriaName).getImages().get(0), true);
//				setRange(dcmImg.getFrom(), dcmImg.getTo());
//				MainRender.setFrom(dcmImg.getFrom());
//				MainRender.setTo(dcmImg.getTo());
				changeImageSlider.setMaximum(currentSeria.getImages().size());
				changeImageSlider.setVisible(true);
				changeImageSlider.setValue(0);
				range.setEnabled(true);
			}
		});

		return imagelabel;
	}

	private void clearLabelsBorder() {
		for (JLabel label : labels.values()) {
			label.setBorder(null);
		}
	}
	
	
	private void openFileActions()
	{
		try {
			isMultipleSelection = false;
			int returnVal = fileChooser.showOpenDialog(TestFrame.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] files = fileChooser.getSelectedFiles();
				TestFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				String fileName = null;
				dicomImages.clear();
				labels.clear();
				series.clear();
				
				Map<String, DicomStudy> stuies = new HashMap<String, DicomStudy>();
				
				if (files.length > 1) {
					JPanel miniPanel = new JPanel();
					miniPanel.setFocusable(true);
					
					DicomSeria seria;
					DicomStudy study;

					for (File f : files) {
						fileName = f.getAbsolutePath();
						long start = System.currentTimeMillis();
						DicomImage image = readImageFromFile(fileName);
						System.out.println(
								 (System.currentTimeMillis() - start));
						start = System.currentTimeMillis();
						if(!series.containsKey(image.getSeriaId()))
						{
							seria = new DicomSeria(image.getSeriaId(), image.getStudyId());
							seria.setDescription(image.getSeriaDescription());
							seria.addImage(fileName);
							seria.setMiniature(image);
							series.put(image.getSeriaId(), seria);
						}
						else
						{
							seria = series.get(image.getSeriaId());
							seria.addImage(fileName);
							series.put(image.getSeriaId(), seria);
						}
						if(!stuies.containsKey(image.getStudyId()))
						{
							study = new DicomStudy(image.getStudyId());
							study.setDescription(image.getStudyDescription());
							study.setDate(image.getStudyDate());
						}
						else
						{
							study = stuies.get(image.getStudyId());
						}
						if(!study.getSeries().contains(seria))
						{
							study.addSeria(seria);
						}
						stuies.put(study.getId(), study);
						
					}

					long start = System.currentTimeMillis();
					
					 
					
					GridLayout gd = new GridLayout(stuies.size(), 1);
					gd.setVgap(10);
					miniPanel.setLayout(gd);
					
					start = System.currentTimeMillis();
					for(DicomStudy s: stuies.values())
					{
						miniPanel.add(createStudyPart(s));
					}
					
					miniaturesPane.setViewportView(miniPanel);
					
				} else {
					fileName = files[0].getAbsolutePath();
					readImageFromFile(fileName);
					showImage(fileName, true);
					range.setEnabled(true);
					changeImageSlider.setVisible(false);
					miniaturesPane.setViewportView(null);
					//MainRender.setCurrentImageNumber(1);
					RenderingLoop.setCurrentImageNumber(1);
				}
				//MainRender.setNumberOfImages(files.length);
				RenderingLoop.setNumberOfImages(files.length);
			}
		} finally {
			TestFrame.this.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	private JPanel createStudyPart(DicomStudy study)
	{
		JPanel studyPanel = new JPanel();
		
		GridLayout gd = new GridLayout(study.getSeries().size(), 1);
		gd.setVgap(10);
		studyPanel.setLayout(gd);
		studyPanel.setFocusable(true);
		
		String desc = study.getDescription() != null ? study.getDescription()
				: makeDateFromString(study.getDate());
		Border border = BorderFactory.createTitledBorder(desc);
		studyPanel.setBorder(border);
		
		for(DicomSeria seria : study.getSeries())
		{
			JLabel label = createMiniature(seria.getMiniature(), seria.getId(), seria.getImages().size());
			border = BorderFactory.createTitledBorder(seria.getDescription());
			label.setBorder(border);
			label.createToolTip();
			studyPanel.add(label);
		}
		
		return studyPanel;
	}
	
	private String makeDateFromString(String date)
	{
		if(date == null)
			return null;
		StringBuilder res = new StringBuilder(date.substring(0, 4));
		res.append("-");
		res.append(date.substring(4, 6));
		res.append("-");
		res.append(date.substring(6, 8));
		return res.toString();
	}
	
	class GTransparentPanel extends JPanel {
	    public GTransparentPanel() {
	        this.setOpaque(false);
	        this.addMouseListener(new MouseAdapter() 
	        {
	        	@Override
	        	public void mousePressed(MouseEvent e) {
	        		System.out.println("DLLLLLLLLLLLLLLL");
	        	}
			});
	    }
	    
	    @Override
	    public void paintComponent(Graphics g) {
	        //rectangle originates at 10,10 and ends at 240,240
	        g.drawRect(10, 10, 240, 240);
	        //filled Rectangle with rounded corners.    
	        g.fillRoundRect(50, 50, 100, 100, 80, 80);
	    }
	}
	
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new SyntheticaAluOxideLookAndFeel());
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

//package OpenGLFeatures;
//
//import java.awt.BorderLayout;
//import java.awt.Button;
//import java.awt.Canvas;
//import java.awt.Choice;
//import java.awt.Color;
//import java.awt.Cursor;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.Graphics;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.GridLayout;
//import java.awt.Toolkit;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.awt.event.KeyListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.stream.Collector;
//import java.util.stream.Collectors;
//
//import javax.swing.AbstractAction;
//import javax.swing.AbstractButton;
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.Icon;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JFileChooser;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JPopupMenu;
//import javax.swing.JScrollPane;
//import javax.swing.JSeparator;
//import javax.swing.JSlider;
//import javax.swing.JToolBar;
//import javax.swing.JTree;
//import javax.swing.KeyStroke;
//import javax.swing.SwingConstants;
//import javax.swing.UIManager;
//import javax.swing.border.Border;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.MenuEvent;
//import javax.swing.event.MenuListener;
//import javax.swing.filechooser.FileNameExtensionFilter;
//
//import org.eclipse.swt.internal.win32.MEASUREITEMSTRUCT;
//
//import Application.*;
//import tools.DicomImage;
//import tools.ImageHelper;
//
//import de.javasoft.plaf.synthetica.SyntheticaAluOxideLookAndFeel;
//
//public class TestFrame extends JFrame{
//
//	private static final long serialVersionUID = 3195487268099554955L;
//	private Map<String, DicomImage> dicomImages = new LinkedHashMap<String, DicomImage>();
//	private final AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
//	private Thread renderThread;
//	private RangeSlider range;
//	private JScrollPane miniaturesPane;
//	private JSlider changeImageSlider;
//	private Map<String, JLabel> labels = new LinkedHashMap<String, JLabel>();
//	private String currentFileName;
//	private Map<String, DicomSeria> series = new HashMap<String, DicomSeria>();
//	private DicomSeria currentSeria;
//	private boolean isMultipleSelection = false;
//
//	JFileChooser fileChooser;
//
//	public TestFrame() {
//		super("DICOM");
//	}
//
//	public void createFrame() {
//		JPanel panel = new JPanel();
//		
//		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
//		JMenuBar menuBar = new JMenuBar();
//		
//		ImageIcon imageIcon = new ImageIcon("res/fileMenu.png");
//		JMenu fileMenu = new JMenu("");
//		fileMenu.setIcon(imageIcon);
//		fileMenu.setToolTipText("Файл");
//		
//		fileMenu.setHorizontalTextPosition(SwingConstants.CENTER);
//		fileMenu.setVerticalTextPosition(SwingConstants.BOTTOM);
//		menuBar.add(fileMenu);
//		
//		menuBar.add( Box.createHorizontalStrut( 10 ) );
//		
//		imageIcon = new ImageIcon("res/openIcon.png");
//		JMenuItem openFileItem = new JMenuItem("Открыть", imageIcon);
//		openFileItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				openFileActions();
//			}
//		});
//	    fileMenu.add(openFileItem);
//	    
//	    imageIcon = new ImageIcon("res/infoIcon.png");
//	    JMenuItem infoFileItem = new JMenuItem("Информация", imageIcon);
//		infoFileItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if(currentFileName != null)
//				{
//					DicomTagsDialog dlg = new DicomTagsDialog(TestFrame.this, dicomImages.get(currentFileName));
//					dlg.setSize(500, 500);
//				}
//			}
//		});
//	    fileMenu.add(infoFileItem);
//	    
//	    //===================Palette================================
//	    imageIcon = new ImageIcon("res/paletteIcon.png");
//		JMenu paletteMenu = new JMenu("");
//		paletteMenu.setIcon(imageIcon);
//		paletteMenu.setToolTipText("Палитра");
//		
//		paletteMenu.setHorizontalTextPosition(SwingConstants.CENTER);
//		paletteMenu.setVerticalTextPosition(SwingConstants.BOTTOM);
//		menuBar.add(paletteMenu);
//		
//		paletteMenu.add(createPaletteItem("Чёрно-белое", "def", null));
//		paletteMenu.add(createPaletteItem("Hot Iron", "hotIron", new ImageIcon("res/hotIronIcon.png")));
//		paletteMenu.add(createPaletteItem("PET Color", "pet", new ImageIcon("res/petIcon.png")));
//		paletteMenu.add(createPaletteItem("Hot Metal Blue Color", "hotMetalBlue", 
//				new ImageIcon("res/hotMetalBlueIcon.png")));
//		paletteMenu.add(createPaletteItem("PET 20 Step Color", "pet20", new ImageIcon("res/pet20Icon.png")));
//		
//		//===================Scroll================================
//		imageIcon = new ImageIcon("res/scrollIcon.png");
//		JMenu scrollMenu = new JMenu("");
//		
//		JCheckBoxMenuItem multSelectMenuItem = new JCheckBoxMenuItem("Серия кадров");
//
//	    ActionListener aListener = new ActionListener() {
//	      public void actionPerformed(ActionEvent event) {
//	    	  AbstractButton aButton = (AbstractButton) event.getSource();
//	          isMultipleSelection = aButton.getModel().isSelected();
//	      }
//	    };
//	    multSelectMenuItem.addActionListener(aListener);
//		scrollMenu.setIcon(imageIcon);
//		scrollMenu.add(multSelectMenuItem);
//		menuBar.add(scrollMenu);
//		
//		//===================Mesurements===================================
//		imageIcon = new ImageIcon("res/mesurementsIcon.png");
//		JMenu mesureMenu = new JMenu("");
//		
//		mesureMenu.addMenuListener(new MenuListener() {
//			boolean isPressed = false;
//	        @Override
//	        public void menuSelected(MenuEvent e) {
//	        	isPressed = !isPressed;
//				//MainRender.setMesurements(isPressed);	        
//			}
//
//	        @Override
//	        public void menuDeselected(MenuEvent e) {
//	            System.out.println("menuDeselected");
//
//	        }
//
//	        @Override
//	        public void menuCanceled(MenuEvent e) {
//	            System.out.println("menuCanceled");
//
//	        }
//	    });
//		
//		mesureMenu.setIcon(imageIcon);
//		menuBar.add(mesureMenu);
//		
//		//===================Invert================================
//		JButton invertMenu = new JButton("");
//		invertMenu.setToolTipText("Инвертировать цвета");
//		invertMenu.addActionListener(new ActionListener() {
//			private boolean isSelected = false;
//			public void actionPerformed(ActionEvent e) {
//				isSelected = !isSelected;
//				MainRender.setInvert(isSelected);
//			}
//		});
//		imageIcon = new ImageIcon("res/invertIcon.png");
//		invertMenu.setIcon(imageIcon);
//		menuBar.add(invertMenu);
//		
//		//===================Rotate================================
//		JButton rotateMenu = new JButton("");
//		rotateMenu.setToolTipText("Повернуть на 90°");
//		rotateMenu.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				MainRender.setRotate(true);
//			}
//		});
//		imageIcon = new ImageIcon("res/rotateIcon.png");
//		rotateMenu.setIcon(imageIcon);
//		menuBar.add(rotateMenu);
//		
//		//===================Zoom In================================
//		JButton zoomIn = new JButton("");
//		zoomIn.setToolTipText("Увеличить изображение");
//		zoomIn.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				MainRender.changeScale(.1f);
//			}
//		});
//		imageIcon = new ImageIcon("res/zoomInIcon.png");
//		zoomIn.setIcon(imageIcon);
//		menuBar.add(zoomIn);
//		
//		//================Zoom Out==================================
//		JButton zoomOut = new JButton("");
//		zoomOut.setToolTipText("Уменьшить изображение");
//		zoomOut.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				MainRender.changeScale(-.1f);
//			}
//		});
//		imageIcon = new ImageIcon("res/zoomOutIcon.png");
//		zoomOut.setIcon(imageIcon);
//		menuBar.add(zoomOut);
//		//===========================================================
//		
//	    this.setJMenuBar(menuBar);
//		
//		fileChooser = new JFileChooser();
//		fileChooser.setMultiSelectionEnabled(true);
//		FileNameExtensionFilter filter = new FileNameExtensionFilter("Dicom файлы", "dcm");
//		fileChooser.setFileFilter(filter);
//
//	    range = new RangeSlider();
//	    range.setOrientation(JSlider.VERTICAL);
//		range.setEnabled(false);
//		setRange(-128, 127);
//		
//		changeImageSlider = new JSlider(JSlider.VERTICAL);
//		changeImageSlider.setMinimum(0);
//		changeImageSlider.setInverted(true);
//		changeImageSlider.setVisible(false);
//
//		this.setLayout(new GridBagLayout());
//		final Canvas canvas = new Canvas();
//		canvas.setPreferredSize(new Dimension(800, 800));
//
//		canvas.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentResized(ComponentEvent e) {
//				newCanvasSize.set(canvas.getSize());
//			}
//		});
//
//		this.addWindowFocusListener(new WindowAdapter() {
//			@Override
//			public void windowGainedFocus(WindowEvent e) {
//				canvas.requestFocusInWindow();
//			}
//		});
//
//		GridBagConstraints constraint = new GridBagConstraints();
//		constraint.fill = GridBagConstraints.HORIZONTAL;
//		constraint.gridx = 0;
//		constraint.gridy = 1;
//		JLabel label = new JLabel("                                               ");
//		this.add(label, constraint);
//		
//		constraint.fill = GridBagConstraints.BOTH;
//		constraint.gridx = 0;
//		constraint.gridy = 2;
//		constraint.gridheight = 3;
//
//		miniaturesPane = new JScrollPane();
//		miniaturesPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//		miniaturesPane.setPreferredSize(new Dimension(200, 0));
//		miniaturesPane.setFocusable(true);
//		this.add(miniaturesPane, constraint);
//		
//		GridBagConstraints imageConstraint = new GridBagConstraints();
//		imageConstraint.fill = GridBagConstraints.BOTH;
//		imageConstraint.ipadx = 200;
//		imageConstraint.gridx = 1;
//		imageConstraint.gridy = 0;
//		imageConstraint.gridheight = 5;
//
//		GTransparentPanel trans = new GTransparentPanel();
//		//canvas.add
//		
//		//panel.setPreferredSize(new Dimension(800, 600));
//		panel.setMinimumSize(new Dimension(1000, 600));
//		//panel.setMaximumSize(new Dimension(800, 600));
//		panel.setLayout(new BorderLayout());
//		panel.add(canvas, BorderLayout.CENTER);
//		this.add(panel, imageConstraint);
//
//		constraint.fill = GridBagConstraints.BOTH;
//		//constraint.ipadx = 200;
//		constraint.gridx = 2;
//		constraint.gridy = 3;
//		constraint.gridheight = 3;
//		this.add(changeImageSlider, constraint);
//		
//		constraint.gridx = 3;
//		this.add(range, constraint);
//
//		setListeners();
//
//		try {
//			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			this.setMinimumSize(new Dimension(800, 600));
//			this.setExtendedState(JFrame.MAXIMIZED_BOTH); 
//			this.setVisible(true);
//			//this.pack();
//
//			Runnable rendering = new Runnable() {
//				public void run() {
//					MainRender.initDisplay(canvas);
//					MainRender.loadShadersAndPallettes();
//					MainRender.startRendering();
//				}
//			};
//			renderThread = new Thread(rendering);
//			renderThread.start();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private JMenuItem createPaletteItem(String name, String code, Icon icon)
//	{
//		JMenuItem peletteItem = new JMenuItem(name, icon);
//		peletteItem.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				if(currentFileName != null)
//				{
//					if(!"def".equals(code))
//					{
//						MainRender.changePalette(code);
//					}
//					else
//					{
//						MainRender.notUsePalette();
//					}
//				}
//			}
//		});
//		return peletteItem;
//	}
//	
//
//	private void setListeners()
//	{
//		range.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				MainRender.setFrom(range.getValue());
//				MainRender.setTo(range.getUpperValue());
//			}
//		});
//		
//		this.addMouseWheelListener(new MouseWheelListener() {
//			@Override
//			public void mouseWheelMoved(MouseWheelEvent e) {
//				if(isMultipleSelection)
//				{
//					int notches = e.getWheelRotation();
//					int newVal = changeImageSlider.getValue();
//				       if (notches < 0) {
//				           //up
//				    	   newVal = (newVal == changeImageSlider.getMaximum()) ? newVal 
//				    			   : newVal + 1;				    	   
//				       } else {
//				          //down
//				    	   newVal = (newVal == changeImageSlider.getMinimum()) ? newVal 
//				    			   : newVal - 1;
//				       }
//				       changeImageSlider.setValue(newVal);
//				}
//			}
//			
//		});
//		
//		changeImageSlider.addChangeListener(new ChangeListener() {
//			private int currentValue = 0;
//			@Override
//			public void stateChanged(ChangeEvent e) {
//				if(currentFileName == null || currentSeria == null)
//				{
//					return;
//				}
//				if(currentValue > changeImageSlider.getValue())
//				{
//					changeImageDown();
//				}
//				else if(currentValue < changeImageSlider.getValue())
//				{
//					changeImageUp();
//				}
//				currentValue = changeImageSlider.getValue();
//			}
//		});
//	}
//	
//	private void changeImageUp()
//	{
//		Iterator<String> it = currentSeria.getImages().iterator();
//		int imgNumber = 0;
// 	   while(it.hasNext())
// 	   {
// 		   imgNumber++;
// 		   if(it.next().equals(currentFileName))
// 		   {
// 			   if(it.hasNext())
// 			   {
// 				   //MainRender.setCurrentImageNumber(imgNumber);
// 				   showImage(it.next(), false);
// 			   }
// 			   break;
// 		   }
// 	   }
//	}
//	
//	private void changeImageDown()
//	{
//		List<String> reverse = new ArrayList<String>(currentSeria.getImages());
// 	   Collections.reverse(reverse);
// 	   Iterator<String> it = reverse.iterator();
// 	   int imgNumber = dicomImages.size();
// 	   while(it.hasNext())
// 	   {
// 		   imgNumber--;
// 		   if(it.next().equals(currentFileName))
// 		   {
// 			   if(it.hasNext())
// 			   {
// 				   //MainRender.setCurrentImageNumber(imgNumber);
// 				   showImage(it.next(), false);
// 			   }
// 			   break;
// 		   }
// 	   }
//	}
//
//	private void setRange(int from, int to) {
//		range.setMinimum(from);
//		range.setMaximum(to);
//		range.setValue(from);
//		range.setUpperValue(to);
//	}
//
//	/**
//	 * Отрисовать изображение
//	 * 
//	 * @param fileName
//	 */
//	private void showImage(String fileName, boolean isChangeRange) {
//		this.currentFileName = fileName;
//		DicomImage dicomImage = dicomImages.get(fileName);
//
//		int width = dicomImage.getWidth();
//		int height = dicomImage.getHeight();
//		int from = dicomImage.getFrom();
//		int to = dicomImage.getTo();
//
//		MainRender.setImageBuffer(dicomImage.getImageBuffer());
//		MainRender.setWidth(width);
//		MainRender.setHeight(height);
////		MainRender.setSize(width, height);
//		
//		if(isChangeRange)
//		{
//			setRange(from, to);
//			MainRender.setFrom(from);
//			MainRender.setTo(to);
//		}
//
//		if (dicomImage.isColor()) {
//			MainRender.notUsePalette();
//		}
//		this.setTitle("DICOM(" + fileName + ")");
//	}
//
//	/**
//	 * Считать изображение из файла и поместить в мапу
//	 * 
//	 * @param fileName
//	 * @return
//	 */
//	private DicomImage readImageFromFile(String fileName) {
//		DicomImage dicomImage = null;
//		try {
//			dicomImage = ImageHelper.openImage(fileName);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		dicomImages.put(fileName, dicomImage);
//		return dicomImage;
//	}
//
//	/**
//	 * Сделать миниатюру для изображения
//	 * 
//	 * @param dcmImg
//	 * @param seriaName
//	 * @return
//	 */
//	private JLabel createMiniature(DicomImage dcmImg, String seriaName, Integer count) {
//		int newWidth = dcmImg.getWidth() / 10;
//		int newHeight = dcmImg.getHeight() / 10;
//
//		BufferedImage bigImg = ImageHelper.getBufferedImage(dcmImg.getImageBuffer(), dcmImg.getFrom(), dcmImg.getTo()
//				, dcmImg.getWidth(), dcmImg.getHeight());
//		BufferedImage smallImage = new BufferedImage(newWidth, newHeight,
//				dcmImg.isColor() ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_BYTE_GRAY);
//		Graphics g = smallImage.createGraphics();
//		g.drawImage(bigImg, 0, 0, newWidth, newHeight, null);
//		g.dispose();
//
//		ImageIcon imageIcon = new ImageIcon(smallImage);
//		JLabel imagelabel = new JLabel("", imageIcon, JLabel.CENTER);
//		imagelabel.setText(count + "");
//		
//		imagelabel.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				currentSeria = series.get(seriaName);
//				showImage(series.get(seriaName).getImages().get(0), true);
////				setRange(dcmImg.getFrom(), dcmImg.getTo());
////				MainRender.setFrom(dcmImg.getFrom());
////				MainRender.setTo(dcmImg.getTo());
//				changeImageSlider.setMaximum(currentSeria.getImages().size());
//				changeImageSlider.setVisible(true);
//				changeImageSlider.setValue(0);
//				range.setEnabled(true);
//			}
//		});
//
//		return imagelabel;
//	}
//
//	private void clearLabelsBorder() {
//		for (JLabel label : labels.values()) {
//			label.setBorder(null);
//		}
//	}
//	
//	
//	private void openFileActions()
//	{
//		try {
//			isMultipleSelection = false;
//			int returnVal = fileChooser.showOpenDialog(TestFrame.this);
//			if (returnVal == JFileChooser.APPROVE_OPTION) {
//				File[] files = fileChooser.getSelectedFiles();
//				TestFrame.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//				String fileName = null;
//				dicomImages.clear();
//				labels.clear();
//				series.clear();
//				
//				Map<String, DicomStudy> stuies = new HashMap<String, DicomStudy>();
//				
//				if (files.length > 1) {
//					JPanel miniPanel = new JPanel();
//					miniPanel.setFocusable(true);
//					
//					DicomSeria seria;
//					DicomStudy study;
//
//					for (File f : files) {
//						fileName = f.getAbsolutePath();
//						long start = System.currentTimeMillis();
//						DicomImage image = readImageFromFile(fileName);
//						System.out.println(
//								 (System.currentTimeMillis() - start));
//						start = System.currentTimeMillis();
//						if(!series.containsKey(image.getSeriaId()))
//						{
//							seria = new DicomSeria(image.getSeriaId(), image.getStudyId());
//							seria.setDescription(image.getSeriaDescription());
//							seria.addImage(fileName);
//							seria.setMiniature(image);
//							series.put(image.getSeriaId(), seria);
//						}
//						else
//						{
//							seria = series.get(image.getSeriaId());
//							seria.addImage(fileName);
//							series.put(image.getSeriaId(), seria);
//						}
//						if(!stuies.containsKey(image.getStudyId()))
//						{
//							study = new DicomStudy(image.getStudyId());
//							study.setDescription(image.getStudyDescription());
//							study.setDate(image.getStudyDate());
//						}
//						else
//						{
//							study = stuies.get(image.getStudyId());
//						}
//						if(!study.getSeries().contains(seria))
//						{
//							study.addSeria(seria);
//						}
//						stuies.put(study.getId(), study);
//						
//					}
//
//					long start = System.currentTimeMillis();
//					
//					 
//					
//					GridLayout gd = new GridLayout(stuies.size(), 1);
//					gd.setVgap(10);
//					miniPanel.setLayout(gd);
//					
//					start = System.currentTimeMillis();
//					for(DicomStudy s: stuies.values())
//					{
//						miniPanel.add(createStudyPart(s));
//					}
//					
//					miniaturesPane.setViewportView(miniPanel);
//					
//				} else {
//					fileName = files[0].getAbsolutePath();
//					readImageFromFile(fileName);
//					showImage(fileName, true);
//					range.setEnabled(true);
//					changeImageSlider.setVisible(false);
//					miniaturesPane.setViewportView(null);
//					//MainRender.setCurrentImageNumber(1);
//				}
//				//MainRender.setNumberOfImages(files.length);
//			}
//		} finally {
//			TestFrame.this.setCursor(Cursor.getDefaultCursor());
//		}
//	}
//	
//	private JPanel createStudyPart(DicomStudy study)
//	{
//		JPanel studyPanel = new JPanel();
//		
//		GridLayout gd = new GridLayout(study.getSeries().size(), 1);
//		gd.setVgap(10);
//		studyPanel.setLayout(gd);
//		studyPanel.setFocusable(true);
//		
//		String desc = study.getDescription() != null ? study.getDescription()
//				: makeDateFromString(study.getDate());
//		Border border = BorderFactory.createTitledBorder(desc);
//		studyPanel.setBorder(border);
//		
//		for(DicomSeria seria : study.getSeries())
//		{
//			JLabel label = createMiniature(seria.getMiniature(), seria.getId(), seria.getImages().size());
//			border = BorderFactory.createTitledBorder(seria.getDescription());
//			label.setBorder(border);
//			label.createToolTip();
//			studyPanel.add(label);
//		}
//		
//		return studyPanel;
//	}
//	
//	private String makeDateFromString(String date)
//	{
//		if(date == null)
//			return null;
//		StringBuilder res = new StringBuilder(date.substring(0, 4));
//		res.append("-");
//		res.append(date.substring(4, 6));
//		res.append("-");
//		res.append(date.substring(6, 8));
//		return res.toString();
//	}
//	
//	class GTransparentPanel extends JPanel {
//	    public GTransparentPanel() {
//	        this.setOpaque(false);
//	        this.addMouseListener(new MouseAdapter() 
//	        {
//	        	@Override
//	        	public void mousePressed(MouseEvent e) {
//	        		System.out.println("DLLLLLLLLLLLLLLL");
//	        	}
//			});
//	    }
//	    
//	    @Override
//	    public void paintComponent(Graphics g) {
//	        //rectangle originates at 10,10 and ends at 240,240
//	        g.drawRect(10, 10, 240, 240);
//	        //filled Rectangle with rounded corners.    
//	        g.fillRoundRect(50, 50, 100, 100, 80, 80);
//	    }
//	}
//	
//	
//	public static void main(String[] args) {
//		try {
//			UIManager.setLookAndFeel(new SyntheticaAluOxideLookAndFeel());
//		    } catch (Exception ex) {
//		      System.err.println("Error loading L&F: " + ex);
//		    }
//
//		TestFrame frame = new TestFrame();
//		frame.addWindowListener(new WindowAdapter() {
//		      public void windowClosing(WindowEvent e) {
//		        System.exit(0);
//		      }
//		    });
//		frame.createFrame();
//	}
//}