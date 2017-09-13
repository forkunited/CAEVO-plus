package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;

public abstract class EventDatumTools<D extends Datum<L>, L> extends Datum.Tools<D, L> {		
	public static interface EventMentionExtractor<D extends Datum<L>, L> {
		String toString();
		EventMention[] extract(D datum);
	}
	
	public static interface TimeExpressionExtractor<D extends Datum<L>, L> {
		String toString();
		LinkableTimeExpression[] extract(D datum);
	}
	
	private Map<String, EventMentionExtractor<D, L>> eventMentionExtractors;
	private Map<String, TimeExpressionExtractor<D, L>> timeExpressionExtractors;
		
	public EventDatumTools(DataTools dataTools) {
		super(dataTools);
		this.eventMentionExtractors = new HashMap<String, EventMentionExtractor<D, L>>();
		this.timeExpressionExtractors = new HashMap<String, TimeExpressionExtractor<D, L>>();
	}
		
	public EventMentionExtractor<D, L> getEventMentionExtractor(String name) {
		return this.eventMentionExtractors.get(name);
	}
	
	public TimeExpressionExtractor<D, L> getTimeExpressionExtractor(String name) {
		return this.timeExpressionExtractors.get(name);
	}
	
	public boolean addEventMentionExtractor(EventMentionExtractor<D, L> eventMentionExtractor) {
		this.eventMentionExtractors.put(eventMentionExtractor.toString(), eventMentionExtractor);
		return true;
	}
	
	public boolean addTimeExpressionExtractor(TimeExpressionExtractor<D, L> timeExpressionExtractor) {
		this.timeExpressionExtractors.put(timeExpressionExtractor.toString(), timeExpressionExtractor);
		return true;
	}
}
