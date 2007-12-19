package de.dailab.jiactng.agentcore.comm.message;

import de.dailab.jiactng.agentcore.knowledge.IFact;


/**
 * Klasse, die ein zu verschickendes File kapselt.
 * @author janko
 *
 */
public class FileContent implements IFact {

	String _filename;
	char[] _content;
	
	public String getFilename() {
		return _filename;
	}
	
	public char[] getContent() {
		return _content;
	}

	public void setContent(char[] content) {
		_content = content;
	}

	public void setFilename(String filename) {
		_filename = filename;
	}
	
	public String toString() {
		return "[Filename:"+_filename+", size:"+_content.length+"]";
	}
	
}
