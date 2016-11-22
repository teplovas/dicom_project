package tools;

import java.util.List;

public class DicomImage {
	
	private Integer width;
	private Integer height;
	private Integer from;
	private Integer to;
	private Object[] imageBuffer;
	private boolean isColor;
	private List<String> tagsValues;
	private String studyId;
	private String seriaId;
	private String studyDescription;
	private String seriaDescription;
	private String studyDate;
	
	
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public Integer getHeight() {
		return height;
	}
	public void setHeight(Integer height) {
		this.height = height;
	}
	public Integer getFrom() {
		return from;
	}
	public void setFrom(Integer from) {
		this.from = from;
	}
	public Integer getTo() {
		return to;
	}
	public void setTo(Integer to) {
		this.to = to;
	}
	public Object[] getImageBuffer() {
		return imageBuffer;
	}
	public void setImageBuffer(Object[] imageBuffer) {
		this.imageBuffer = imageBuffer;
	}
	public boolean isColor() {
		return isColor;
	}
	public void setColor(boolean isColor) {
		this.isColor = isColor;
	}
	public List<String> getTagsValues() {
		return tagsValues;
	}
	public void setTagsValues(List<String> tagsValues) {
		this.tagsValues = tagsValues;
	}
	public String getStudyId() {
		return studyId;
	}
	public void setStudyId(String studyId) {
		this.studyId = studyId;
	}
	public String getSeriaId() {
		return seriaId;
	}
	public void setSeriaId(String seriaId) {
		this.seriaId = seriaId;
	}
	public String getStudyDescription() {
		return studyDescription;
	}
	public void setStudyDescription(String studyDescription) {
		this.studyDescription = studyDescription;
	}
	public String getSeriaDescription() {
		return seriaDescription;
	}
	public void setSeriaDescription(String seriaDescription) {
		this.seriaDescription = seriaDescription;
	}
	public String getStudyDate() {
		return studyDate;
	}
	public void setStudyDate(String studyDate) {
		this.studyDate = studyDate;
	}
}
