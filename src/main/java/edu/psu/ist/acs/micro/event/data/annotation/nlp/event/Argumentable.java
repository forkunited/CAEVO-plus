package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public interface Argumentable extends StoredJSONSerializable {
	public enum Type {
		ENTITY,
		VALUE,
		EVENT,
		TIME
	}
	
	String getId();
	Type getArgumentableType();
}
