package codeswarm;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XMLQueueLoader implements Runnable {
	private final String fullFilename;
	private BlockingQueue<FileEvent> queue;
	private boolean isXMLSorted;
	private java.util.Vector<TaskListener> listenerList = new java.util.Vector<TaskListener>();

	//used to ensure that input is sorted when we are told it is
	private long maximumDateSeenSoFar = 0;

	public XMLQueueLoader(String fullFilename, BlockingQueue<FileEvent> queue, boolean isXMLSorted) {
		this.fullFilename = fullFilename;
		this.queue = queue;
		this.isXMLSorted = isXMLSorted;
	}
	
	public void addTaskListener(TaskListener listener){
		listenerList.add(listener);
	}
	
	public void removeTaskListener(TaskListener listener){
		listenerList.remove(listenerList);
	}
	
	private void fireTaskDoneEvent(){
		Iterator<TaskListener> it = listenerList.iterator();
		while(it.hasNext()){
			it.next().fireTaskDoneEvent();
		}
	}

	public void run(){
		XMLReader reader = null;
		try {
			reader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			System.out.println("Couldn't find/create an XML SAX Reader");
			e.printStackTrace();
			System.exit(1);
		}
		
		reader.setContentHandler(new DefaultHandler(){
			public void startElement(String uri, String localName, String name,
					Attributes atts) throws SAXException {
				if (name.equals("event")){
					String eventFilename = atts.getValue("filename");
					String eventDatestr = atts.getValue("date");
					long eventDate = Long.parseLong(eventDatestr);
					String eventWeightStr = atts.getValue("weight");
					int eventWeight = 1;
					if (eventWeightStr != null) {
						eventWeight = Integer.parseInt(eventWeightStr);
					}

					//It's difficult for the user to tell that they're missing events,
					//so we should crash in this case
					if (isXMLSorted){
						if (eventDate < maximumDateSeenSoFar){
							System.out.println("Input not sorted, you must set IsInputSorted to false in your config file");
							System.exit(1);
						}
						else
							maximumDateSeenSoFar = eventDate;
					}

					String eventAuthor = atts.getValue("author");
					// int eventLinesAdded = atts.getValue( "linesadded" );
					// int eventLinesRemoved = atts.getValue( "linesremoved" );

					FileEvent evt = new FileEvent(eventDate, eventAuthor, "", eventFilename, eventWeight);
					try {
						queue.put(evt);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Interrupted while trying to put into eventsQueue");
						e.printStackTrace();
						System.exit(1);
					}
				}
			}
			
			public void endDocument(){
				fireTaskDoneEvent();
			}
			
		});
		
		try {
			reader.parse(fullFilename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error parsing xml:");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
}