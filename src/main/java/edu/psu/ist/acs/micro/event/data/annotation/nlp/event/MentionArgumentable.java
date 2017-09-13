package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public interface MentionArgumentable extends StoredJSONSerializable {
	public enum Type {
		ENTITY_MENTION,
		VALUE_MENTION,
		EVENT_MENTION,
		TIME_EXPRESSION
	}
	
	String getId();
	Type getMentionArgumentableType();
	Argumentable getArgumentable();
}
