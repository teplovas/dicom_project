package Application;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tools.DicomImage;

public class DicomTagsDialog extends JDialog
{
	private static final long serialVersionUID = 4702550762587054409L;
	private JScrollPane scrollPane;

	public DicomTagsDialog(JFrame parent, DicomImage dcmImg) 
	{
		super(parent, "Информация о файле");
		
		scrollPane = new JScrollPane();
		JTextArea tags = new JTextArea(createTagsList(dcmImg.getTagsValues()));
		tags.setFont(tags.getFont().deriveFont(12f));
		scrollPane.setViewportView(tags);
		getContentPane().add(scrollPane);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
	}
	
	private String createTagsList(List<String> tags)
	{
		StringBuilder res = new StringBuilder();
		for(String tag : tags)
		{
			res.append(tag + '\n');
		}
		return res.toString();
	}
}
