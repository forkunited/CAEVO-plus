package edu.psu.ist.acs.micro.event.util;

import edu.cmu.ml.rtw.generic.util.Properties;

/**
 * EventProperties loads and represents a properties
 * configuration file (for specifying file paths and
 * other system dependent information)
 * 
 * @author Bill McDowell
 *
 */
public class EventProperties extends Properties {
	
	public EventProperties() {
		this(null);
	}
	
	public EventProperties(String path) {
		super( new String[] { (path == null) ? "event.properties" : path } );
		
	}
}