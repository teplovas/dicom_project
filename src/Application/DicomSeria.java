package Application;

import java.util.ArrayList;
import java.util.List;

import tools.DicomImage;

public class DicomSeria {
	
	private String id;
	private String studyId;
	private List<String> images = new ArrayList<String>();
	private DicomImage miniature;
	private String description;

	public DicomSeria(String id, String studyId)
	{
		this.id = id;
		this.studyId = studyId;
	}
	
	public List<String> getImages() {
		return images;
	}

	public void addImage(String image) {
		images.add(image);
	}
	
	public String getId()
	{
		return id;
	}

	public DicomImage getMiniature() {
		return miniature;
	}

	public void setMiniature(DicomImage miniature) {
		this.miniature = miniature;
	}

	public String getStudyId() {
		return studyId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof DicomSeria))
		{
			return false;
		}
		DicomSeria other = (DicomSeria)o;
		return id.equals(other.id);
	}
}
