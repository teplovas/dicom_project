package UserInterface;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.tool.dcmqr.DcmQR;
import org.dcm4che2.tool.dcmrcv.DcmRcv;
import org.dcm4che2.data.UID;
import org.dcm4che2.net.TransferCapability;

import tools.ConvertTools;
import tools.DicomImage;

public class ConnectPACSDialog  extends JDialog
{
	private static final long serialVersionUID = 6691525001903511004L;
	JTextField titleText = new JTextField();
	JTextField hostNameText = new JTextField();
	JTextField portText = new JTextField();
	JTextField destinationText = new JTextField();
	
	DcmRcv rcv = new DcmRcv();
	DcmQR qr = new DcmQR("dddd");
	
	List<DicomImage> images;

	public ConnectPACSDialog(JFrame parent) 
	{
		super(parent, "PACS сервер");
		
		JPanel pan = new JPanel();
		GridLayout layout = new GridLayout(5, 2);
		layout.setVgap(30);
		pan.setLayout(layout);
		
		pan.add(new JLabel("Название подключения"));
		pan.add(titleText);
		pan.add(new JLabel("IP адрес"));
		pan.add(hostNameText);
		pan.add(new JLabel("Номер порта"));
		pan.add(portText);
		pan.add(new JLabel("Описание"));
		pan.add(destinationText);
		
		pan.add(new JLabel(""));
		JButton start = new JButton("start");
		start.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				startDcmrcvService();
			}
		});
		pan.add(start);
		
		this.add(pan);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
        
        org.apache.log4j.BasicConfigurator.configure();
	}
	
	@Override
	public void dispose() 
	{
		super.dispose();
		//rcv.stop();
	}
	
	public List<DicomImage> getImage()
	{
		return images;
	}
	
	private void startDcmrcvService () {
//        System.out.println("Going to start DICOM receiver service");
//        try {            
//            rcv.setAEtitle("DCM4CHE1");
//            rcv.setHostname("127.0.0.1");
//            rcv.setPort(435);
//            rcv.setDestination("C:\\forth\\npap\\Development\\DevDir");
//            rcv.initTransferCapability();
//            rcv.start();          
//        } catch (IOException ex) {
//            System.out.println("error: " + ex.getMessage());
//        }
		
        DcmQR dcmqr = new DcmQR("AET_TITLE");

        dcmqr.setCalledAET(/*titleText.getText()*/"AWSPIXELMEDPUB", true);
        dcmqr.setRemoteHost(/*hostNameText.getText()*/"184.73.255.26");
        dcmqr.setRemotePort(/*Integer.valueOf(portText.getText())*/11112);
        dcmqr.setQueryLevel(DcmQR.QueryRetrieveLevel.IMAGE);
        dcmqr.setCalling("AET_TEST");
        dcmqr.setCFind(true);
        dcmqr.setCGet(true);
        
        String sintax[] = {"1.2.840.10008.1.2"};

        TransferCapability tc = new TransferCapability(UID.CTImageStorage, 
                sintax, TransferCapability.SCP);
       
        dcmqr.selectTransferSyntax(tc);


        String tsuids[] = {"1.2.840.10008.5.1.4.1.2.2.2"} ;
        dcmqr.addStoreTransferCapability(UID.CTImageStorage, tsuids);
        dcmqr.configureTransferCapability(true);
        dcmqr.setStoreDestination(
                "c:\\DCM_TEST"
        );
        dcmqr.setMoveDest("AE_TITLE");

        try {
        dcmqr.start();
        System.out.println("started");
        dcmqr.open();
        System.out.println("opened");
        List<DicomObject> result = dcmqr.query();
        System.out.println("move");
        //dcmqr.setMoveDest("AET_TEST");
        dcmqr.move(result);
        
        //dcmqr.get(result);
        for(DicomObject dcmObj : result)
        {
        	images.add(ConvertTools.createDicomImage(dcmObj));
        }
        System.out.println("List Size = "+result.size());
        dcmqr.close();
        dcmqr.stop();
        }
        catch (Exception e) {
        	System.out.println("ERROR: " + e.getMessage());
        }
    }
}
