package codeswarm;

import java.util.Date;

/**
 * Describe an event on a file
 */
class FileEvent implements Comparable<Object> {
	private Date date;
	private String author;
	private String filename;
	private String path;
	//private int linesadded;
	//private int linesremoved;
	private int weight;

	public Date getDate(){
		return date;
	}
	
	public String getAuthor(){
		return author;
	}
	
	public String getFilename(){
		return filename;
	}
	
	public String getPath(){
		return path;
	}
	
	public int getWeight(){
		return weight;
	}
	
	/**
	 * short constructor with base data
	 */
	FileEvent(long datenum, String author, String path, String filename) {
		this(datenum, author, path, filename, 1);
	}

	/**
	 * constructor with weight
	 */
	FileEvent(long datenum, String author, String path, String filename, int weight) {
		this.date = new Date(datenum);
		this.author = author;
		this.path = path;
		this.filename = filename;
		this.weight = weight;
	}

	/**
	 * Comparing two events by date (Not Used)
	 * @param o
	 * @return -1 if <, 0 if =, 1 if >
	 */
	public int compareTo(Object o) {
		return date.compareTo(((FileEvent) o).date);
	}
}