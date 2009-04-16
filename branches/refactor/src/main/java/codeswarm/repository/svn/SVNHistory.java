package codeswarm.repository.svn;

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

// Some parts of this code have been taken from SVNKit's example-code.
// See https://wiki.svnkit.com/Printing_Out_Repository_History

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

import codeswarm.repository.events.CodeSwarmEventsSerializer;
import codeswarm.repository.events.Event;
import codeswarm.repository.events.EventList;

/**
 * Performs the repository lookup and serializes the data.
 * @author tpraxl
 */
public class SVNHistory extends AbstractSVNHistoryVisitor {
	private static Log logger = LogFactory.getLog(SVNHistory.class);
	private	String filename;
	private String url;
	private EventList list = new EventList();
	
	/**
	 * creates an instance of SVNHistory.
	 * @param filename the path to the (xml-)file to serialize the data to.
	 */
	public SVNHistory(String filename){
		this.filename =filename;
	}
	
	/**
	 * @return the path to the file the data is serialized to.
	 */
	public String getFilePath(){
		return "data/"+filename+this.url.hashCode()+".xml";
	}
	
	/**
	 * clears the entire revision cache.
	 */
	public static void clearCache(){
		try {
			Preferences.userNodeForPackage(SVNHistory.class).clear();
		} catch (BackingStoreException ex) {
			logger.error(null, ex);
		}
	}
	
	/**
	 * stores the repository url
	 * @param url the complete repository url.
	 */
	public void handleStart(String url) {
		this.url = url;
	}
	
	/**
	 * looks up the cache. Stops proceeding if a cached version for this
	 * repository was found.
	 * @param pRevision the latest repository revision.
	 * @return false if a cached version was found, true if the history shall
	 * be fetched from repository.
	 */
	public boolean handleFetchingLatestRepositoryRevision(Long pRevision) {
		long revision = pRevision.longValue();
		Preferences p = Preferences.userNodeForPackage(SVNHistory.class);
		long l= p.getLong(Integer.toString(this.url.hashCode()), -1l);
		if(l==revision){
			if(logger.isDebugEnabled()){
				logger.debug("skip fetching " + String.valueOf(l) + " (latest revision is " + revision + ") for " + this.url);
			}
			return false;
		}else{
			if(logger.isDebugEnabled()){
				logger.debug("proceed fetching (latest revision is " + String.valueOf(pRevision) + " , cached revision is " + String.valueOf(l) + " for repository " + this.url);
			}
			Preferences.userNodeForPackage(SVNHistory.class).putLong(Integer.toString(this.url.hashCode()), revision);
			try {
				Preferences.userNodeForPackage(SVNHistory.class).flush();
			} catch (BackingStoreException ex) {
				logger.error(null, ex);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("fetching until revision " + revision);
		}
		return true;
	}
	
	/**
	 * processes a log entry. Adds it to the EventList
	 * @param logEntry the entry to process
	 */
	public void handleLogEntry(SVNLogEntry logEntry) {
		Set<?> keySet = logEntry.getChangedPaths().keySet();
		Iterator<?> i = keySet.iterator();
		while(i.hasNext()){
			String key = (String)i.next();
			SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(key);
			list.addEvent(new Event(entryPath.getPath(),logEntry.getDate().getTime(),logEntry.getAuthor()));
			if(logger.isDebugEnabled()){
				logger.debug("fetched entry " + entryPath.getPath() + "\n date " + logEntry.getDate() + "\n rev. " + logEntry.getRevision() + "\n--");
			}
		}
		/*
		 * displaying all paths that were changed in that revision; changed
		 * path information is represented by SVNLogEntryPath.
		 */
		if (logEntry.getChangedPaths().size() > 0) {
			/*
			 * keys are changed paths
			 */
			Set<?> changedPathsSet = logEntry.getChangedPaths().keySet();

			for (Iterator<?> changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {
				/*
				 * obtains a next SVNLogEntryPath
				 */
				SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry.getChangedPaths().get(changedPaths.next());
				/*
				 * SVNLogEntryPath.getPath returns the changed path itself;
				 *
				 * SVNLogEntryPath.getType returns a charecter describing
				 * how the path was changed ('A' - added, 'D' - deleted or
				 * 'M' - modified);
				 *
				 * If the path was copied from another one (branched) then
				 * SVNLogEntryPath.getCopyPath &
				 * SVNLogEntryPath.getCopyRevision tells where it was copied
				 * from and what revision the origin path was at.
				 */
				if(logger.isDebugEnabled()){
					StringBuffer copyPathInfo = new StringBuffer();
					if(entryPath.getCopyPath()!=null){
						copyPathInfo.append("(from ").append(entryPath.getCopyPath());
						copyPathInfo.append(" rev ").append(entryPath.getCopyRevision()).append(")");
					}
					logger.debug("entry: " + entryPath.getType() + " " + entryPath.getPath() + " " + copyPathInfo.toString());
				}
			}
		}
	}
	
	/**
	 * serializes the log entries
	 */
	public void finishLogEntries() {
		try {
			CodeSwarmEventsSerializer serializer =
				new CodeSwarmEventsSerializer(list);
			serializer.serialize(getFilePath());
		} catch (ParserConfigurationException ex) {
			logger.error(null, ex);
		} catch (TransformerConfigurationException ex) {
			logger.error(null, ex);
		} catch (IOException ex) {
			logger.error(null, ex);
		} catch (TransformerException ex) {
			logger.error(null, ex);
		}
	}
	
	/**
	 * Logs an error statement and stops further processing
	 * @param e the orginal exception
	 * @param url the repository url
	 * @return false
	 */
	public boolean handleCreateRepositoryException(SVNException e, String url) {
		/*
		 * Perhaps a malformed URL is the cause of this exception.
		 */
		logger.error("error while creating an SVNRepository for the location " + url + " : " + e.getMessage(), e);
		return false;
	}
	
	/**
	 * Logs an error statement and stops further processing
	 * Otherwise returns the latest cached revision.
	 * @param e the orginal exception
	 * @return null.
	 */
	public Long handleFetchingLatestRepositoryRevisionException(SVNException e) {
		logger.error("error while fetching the latest repository revision: " + e.getMessage() + ".\nFalling back to cached version (if present).", e);
		return null;
	}
	
	/**
	 * Logs an error statement and stops further processing
	 * @param e the orginal exception
	 * @param url the repository url
	 * @return false
	 */
	public boolean handleCollectingLogInformationException(SVNException e, String url) {
		logger.error("error while collecting log information for " + url + " : " + e.getMessage(), e);
		return false;
	}
	
}
