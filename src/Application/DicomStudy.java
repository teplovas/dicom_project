package Application;

import java.util.ArrayList;
import java.util.List;

public class DicomStudy {
	
	private String id;
	private String date;
	private List<DicomSeria> series = new ArrayList<DicomSeria>();
	private String description;
	
	public DicomStudy(String id)
	{
		this.id = id;
	}	
	
	public List<DicomSeria> getSeries()
	{
		return series;
	}
	
	public void addSeria(DicomSeria seria)
	{
		series.add(seria);
	}
	
	public void putSeries(List<DicomSeria> series)
	{
		this.series = series;
	}
	
	public String getId()
	{
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
