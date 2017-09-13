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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventPairDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetGeneralGovernor;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetReichenbach;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetReportingGovernor;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetWordNet;

public class MethodClassificationEventPairTLinkDet extends MethodClassification<EventPairDatum<TimeMLRelType>, TimeMLRelType> {
	public enum Rule {
		GENERAL_GOVERNOR,
		REICHENBACH,
		REPORTING_GOVERNOR,
		WORD_NET
	}
	
	private Rule rule;
	private String[] parameterNames = { "rule" };
	
	public MethodClassificationEventPairTLinkDet() {
		
	}
	
	public MethodClassificationEventPairTLinkDet(DatumContext<EventPairDatum<TimeMLRelType>, TimeMLRelType> context) {
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
	public boolean init(DataSet<EventPairDatum<TimeMLRelType>, TimeMLRelType> testData) {
		return true;
	}

	@Override
	public MethodClassification<EventPairDatum<TimeMLRelType>, TimeMLRelType> clone(String referenceName) {
		MethodClassificationEventPairTLinkDet clone = new MethodClassificationEventPairTLinkDet(this.context);
		clone.referenceName = referenceName;
		return clone;
	}

	@Override
	public MethodClassification<EventPairDatum<TimeMLRelType>, TimeMLRelType> makeInstance(
			DatumContext<EventPairDatum<TimeMLRelType>, TimeMLRelType> context) {
		return new MethodClassificationEventPairTLinkDet(context);
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
	public Trainable<EventPairDatum<TimeMLRelType>, TimeMLRelType> getTrainable() {
		return null;
	}
	
	@Override
	public Map<EventPairDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> classifyWithScore(
			DataSet<EventPairDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<EventPairDatum<TimeMLRelType>, TimeMLRelType> classifications = classify(data);
		Map<EventPairDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> scores = new HashMap<>();
		for (Entry<EventPairDatum<TimeMLRelType>, TimeMLRelType> entry : classifications.entrySet())
			scores.put(entry.getKey(), new Pair<TimeMLRelType, Double>(entry.getValue(), 1.0));
		return scores;
	}
	
	@Override
	public Pair<TimeMLRelType, Double> classifyWithScore(
			EventPairDatum<TimeMLRelType> datum) {
		TimeMLRelType label = classify(datum);
		if (label != null)
			return new Pair<TimeMLRelType, Double>(label, 1.0);
		else
			return null;
	}
	
	@Override
	public Map<EventPairDatum<TimeMLRelType>, TimeMLRelType> classify(DataSet<EventPairDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<EventPairDatum<TimeMLRelType>, TimeMLRelType> map = new HashMap<EventPairDatum<TimeMLRelType>, TimeMLRelType>();
		
		for (EventPairDatum<TimeMLRelType> datum : data) {
			TimeMLRelType label = classify(datum);
			if (label != null)
				map.put(datum, label);
		}
	
		return map;
	}
	
	@Override
	public TimeMLRelType classify(EventPairDatum<TimeMLRelType> datum) {
		TimeMLRelType rel = null;
		for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++) {
			for (int j = 0; j < datum.getTarget().getSomeMentionCount(); j++) {
				TimeMLRelType curRel = determineRelation(datum.getSource().getSomeMention(i), datum.getTarget().getSomeMention(j));
				if (rel != null && curRel != null && curRel != rel)
					return null;
				if (curRel != null)
					rel = curRel;
			}
		}
		
		return rel;
	}
	
	private TimeMLRelType determineRelation(EventMention e1, EventMention e2) {
		if (this.rule == Rule.GENERAL_GOVERNOR) {
			return DetGeneralGovernor.determineRelation(e1, e2);
		} else if (this.rule == Rule.REICHENBACH) {
			return DetReichenbach.determineRelation(e1, e2);
		} else if (this.rule == Rule.REPORTING_GOVERNOR) {
			return DetReportingGovernor.determineRelation(e1, e2);
		} else if (this.rule == Rule.WORD_NET) {
			return DetWordNet.determineRelation(e1, e2, this.context.getDataTools().getWordNet());
		} else {
			return null;
		}
	}
	
	@Override
	public Map<EventPairDatum<TimeMLRelType>, Double> score(DataSet<EventPairDatum<TimeMLRelType>, TimeMLRelType> data, TimeMLRelType label) {
		Map<EventPairDatum<TimeMLRelType>, Double> scoreMap = new HashMap<>();
		
		for (EventPairDatum<TimeMLRelType> datum : data) {
			scoreMap.put(datum, score(datum, label));
		}
		
		return scoreMap;
	}

	@Override
	public double score(EventPairDatum<TimeMLRelType> datum, TimeMLRelType label) {
		if (label.equals(classify(datum)))
			return 1.0;
		else
			return 0.0;
	}
}
