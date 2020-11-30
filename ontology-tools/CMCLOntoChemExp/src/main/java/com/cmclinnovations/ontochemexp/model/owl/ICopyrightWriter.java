package com.cmclinnovations.ontochemexp.model.owl;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * Declares the method whose implementation calls the method that reads 
 * a PrIMe experiment's copyright data and metadata from an in-memory temporary 
 * storage to pass them to the corresponding PrIMe to OWL conversion methods.
 * 
 * @author Feroz Farazi (msff2@cam.ac.uk)
 *
 */
public interface ICopyrightWriter {
	static Logger logger = org.slf4j.LoggerFactory.getLogger(ICopyrightWriter.class);
	public void writer(char ch[], int start, int length) throws SAXException;
//	public void writer(String qName) throws SAXException;
	public void writeValue();
	public void setUP();
}
