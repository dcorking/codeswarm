package codeswarm.processing;

/*
Copyright 2008-2009 code_swarm project team

This file is part of code_swarm.

code_swarm is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

code_swarm is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with code_swarm.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Date;

/**
 * Describe an event on a file
 */
public class FileEvent implements Comparable<Object> {
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
	public FileEvent(long datenum, String author, String path, String filename) {
		this(datenum, author, path, filename, 1);
	}

	/**
	 * constructor with weight
	 */
	public FileEvent(long datenum, String author, String path, String filename, int weight) {
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