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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TimePairDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetTimeTime;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetWordNet;

public class MethodClassificationTimePairTLinkDet extends MethodClassification<TimePairDatum<TimeMLRelType>, TimeMLRelType> {
	public enum Rule {
		TIME_TIME,
		WORD_NET
	}
	
	private Rule rule;
	private String[] parameterNames = { "rule" };
	
	public MethodClassificationTimePairTLinkDet() {
		
	}
	
	public MethodClassificationTimePairTLinkDet(DatumContext<TimePairDatum<TimeMLRelType>, TimeMLRelType> context) {
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
	public boolean init(DataSet<TimePairDatum<TimeMLRelType>, TimeMLRelType> testData) {
		return true;
	}

	@Override
	public MethodClassification<TimePairDatum<TimeMLRelType>, TimeMLRelType> clone(String referenceName) {
		MethodClassificationTimePairTLinkDet clone = new MethodClassificationTimePairTLinkDet(this.context);
		clone.referenceName = referenceName;
		return clone;
	}

	@Override
	public MethodClassification<TimePairDatum<TimeMLRelType>, TimeMLRelType> makeInstance(
			DatumContext<TimePairDatum<TimeMLRelType>, TimeMLRelType> context) {
		return new MethodClassificationTimePairTLinkDet(context);
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
	public Trainable<TimePairDatum<TimeMLRelType>, TimeMLRelType> getTrainable() {
		return null;
	}
	
	@Override
	public Map<TimePairDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> classifyWithScore(
			DataSet<TimePairDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<TimePairDatum<TimeMLRelType>, TimeMLRelType> classifications = classify(data);
		Map<TimePairDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> scores = new HashMap<>();
		for (Entry<TimePairDatum<TimeMLRelType>, TimeMLRelType> entry : classifications.entrySet())
			scores.put(entry.getKey(), new Pair<TimeMLRelType, Double>(entry.getValue(), 1.0));
		return scores;
	}
	
	@Override
	public Pair<TimeMLRelType, Double> classifyWithScore(
			TimePairDatum<TimeMLRelType> datum) {
		TimeMLRelType label = classify(datum);
		if (label != null)
			return new Pair<TimeMLRelType, Double>(label, 1.0);
		else
			return null;
	}
	
	@Override
	public Map<TimePairDatum<TimeMLRelType>, TimeMLRelType> classify(DataSet<TimePairDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<TimePairDatum<TimeMLRelType>, TimeMLRelType> map = new HashMap<TimePairDatum<TimeMLRelType>, TimeMLRelType>();
		
		for (TimePairDatum<TimeMLRelType> datum : data) {
			TimeMLRelType label = classify(datum);
			if (label != null)
				map.put(datum, label);
		}
	
		return map;
	}
	
	@Override
	public TimeMLRelType classify(TimePairDatum<TimeMLRelType> datum) {
		TimeMLRelType rel = null;
		for (int i = 0; i < datum.getSource().getSomeExpressionCount(); i++) {
			for (int j = 0; j < datum.getTarget().getSomeExpressionCount(); j++) {
				TimeMLRelType curRel = determineRelation(datum.getSource().getSomeExpression(i), datum.getTarget().getSomeExpression(j));
				if (rel != null && curRel != null && curRel != rel)
					return null;
				if (curRel != null)
					rel = curRel;
			}
		}
		
		return rel;
	}
	
	private TimeMLRelType determineRelation(LinkableTimeExpression t1, LinkableTimeExpression t2) {
		if (this.rule == Rule.TIME_TIME) {
			return DetTimeTime.determineRelation(t1, t2);
		} else if (this.rule == Rule.WORD_NET) {
			return DetWordNet.determineRelation(t1, t2, this.context.getDataTools().getWordNet());
		} else {
			return null;
		}
	}
	
	@Override
	public Map<TimePairDatum<TimeMLRelType>, Double> score(DataSet<TimePairDatum<TimeMLRelType>, TimeMLRelType> data, TimeMLRelType label) {
		Map<TimePairDatum<TimeMLRelType>, Double> scoreMap = new HashMap<>();
		
		for (TimePairDatum<TimeMLRelType> datum : data) {
			scoreMap.put(datum, score(datum, label));
		}
		
		return scoreMap;
	}

	@Override
	public double score(TimePairDatum<TimeMLRelType> datum, TimeMLRelType label) {
		if (label.equals(classify(datum)))
			return 1.0;
		else
			return 0.0;
	}
}
