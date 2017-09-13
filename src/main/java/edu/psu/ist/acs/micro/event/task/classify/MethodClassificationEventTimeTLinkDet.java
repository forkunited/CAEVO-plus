package edu.psu.ist.acs.micro.event.task.classify;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.parse.AssignmentList;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.task.classify.MethodClassification;
import edu.cmu.ml.rtw.generic.task.classify.Trainable;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventTimeDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetReportingDCT;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetAdjacentEventTime;

public class MethodClassificationEventTimeTLinkDet extends MethodClassification<EventTimeDatum<TimeMLRelType>, TimeMLRelType> {
	public enum Rule {
		REPORTING_DCT,
		ADJACENT_EVENT_TIME
	}
	
	private Rule rule;
	private String[] parameterNames = { "rule" };
	
	public MethodClassificationEventTimeTLinkDet() {
		
	}
	
	public MethodClassificationEventTimeTLinkDet(DatumContext<EventTimeDatum<TimeMLRelType>, TimeMLRelType> context) {
		this.context = context;
	}
	
	@Override
	public String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	public Obj getParameterValue(String parameter) {
		if (parameter.equals("rule"))
			return (this.rule != null) ? Obj.stringValue(this.rule.toString()) : null;
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("rule"))
			this.rule = (parameterValue == null) ? null : Rule.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@Override
	public boolean init(DataSet<EventTimeDatum<TimeMLRelType>, TimeMLRelType> testData) {
		return true;
	}

	@Override
	public MethodClassification<EventTimeDatum<TimeMLRelType>, TimeMLRelType> clone(String referenceName) {
		MethodClassificationEventTimeTLinkDet clone = new MethodClassificationEventTimeTLinkDet(this.context);
		clone.referenceName = referenceName;
		return clone;
	}

	@Override
	public MethodClassification<EventTimeDatum<TimeMLRelType>, TimeMLRelType> makeInstance(
			DatumContext<EventTimeDatum<TimeMLRelType>, TimeMLRelType> context) {
		return new MethodClassificationEventTimeTLinkDet(context);
	}

	@Override
	protected boolean fromParseInternal(AssignmentList internalAssignments) {
		return true;
	}

	@Override
	protected AssignmentList toParseInternal() {
		return null;
	}

	@Override
	public String getGenericName() {
		return "Det";
	}

	@Override
	public boolean hasTrainable() {
		return false;
	}

	@Override
	public Trainable<EventTimeDatum<TimeMLRelType>, TimeMLRelType> getTrainable() {
		return null;
	}
	
	@Override
	public Map<EventTimeDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> classifyWithScore(
			DataSet<EventTimeDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<EventTimeDatum<TimeMLRelType>, TimeMLRelType> classifications = classify(data);
		Map<EventTimeDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> scores = new HashMap<>();
		for (Entry<EventTimeDatum<TimeMLRelType>, TimeMLRelType> entry : classifications.entrySet())
			scores.put(entry.getKey(), new Pair<TimeMLRelType, Double>(entry.getValue(), 1.0));
		return scores;
	}
	
	@Override
	public Pair<TimeMLRelType, Double> classifyWithScore(
			EventTimeDatum<TimeMLRelType> datum) {
		TimeMLRelType label = classify(datum);
		if (label != null)
			return new Pair<TimeMLRelType, Double>(label, 1.0);
		else
			return null;
	}
	
	@Override
	public Map<EventTimeDatum<TimeMLRelType>, TimeMLRelType> classify(DataSet<EventTimeDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<EventTimeDatum<TimeMLRelType>, TimeMLRelType> map = new HashMap<EventTimeDatum<TimeMLRelType>, TimeMLRelType>();
		
		for (EventTimeDatum<TimeMLRelType> datum : data) {
			TimeMLRelType label = classify(datum);
			if (label != null)
				map.put(datum, label);
		}
	
		return map;
	}
	
	@Override
	public TimeMLRelType classify(EventTimeDatum<TimeMLRelType> datum) {
		TimeMLRelType rel = null;
		for (int i = 0; i < datum.getEvent().getSomeMentionCount(); i++) {
			for (int j = 0; j < datum.getTime().getSomeExpressionCount(); j++) {
				TimeMLRelType curRel = determineRelation(datum.getEvent().getSomeMention(i), datum.getTime().getSomeExpression(j));
				if (rel != null && curRel != null && curRel != rel)
					return null;
				if (curRel != null)
					rel = curRel;
			}
		}
		
		return rel;
	}
	
	private TimeMLRelType determineRelation(EventMention e, LinkableTimeExpression t) {
		if (this.rule == Rule.ADJACENT_EVENT_TIME) {
			return DetAdjacentEventTime.determineRelation(e, t);
		} else if (this.rule == Rule.REPORTING_DCT) {
			return DetReportingDCT.determineRelation(e, t);
		} else {
			return null;
		}
	}
	
	@Override
	public Map<EventTimeDatum<TimeMLRelType>, Double> score(DataSet<EventTimeDatum<TimeMLRelType>, TimeMLRelType> data, TimeMLRelType label) {
		Map<EventTimeDatum<TimeMLRelType>, Double> scoreMap = new HashMap<>();
		
		for (EventTimeDatum<TimeMLRelType> datum : data) {
			scoreMap.put(datum, score(datum, label));
		}
		
		return scoreMap;
	}

	@Override
	public double score(EventTimeDatum<TimeMLRelType> datum, TimeMLRelType label) {
		if (label.equals(classify(datum)))
			return 1.0;
		else
			return 0.0;
	}
}
