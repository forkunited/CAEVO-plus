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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.CorefRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventPairDatum;
import edu.psu.ist.acs.micro.event.task.classify.ecoref.det.DetWordNetLemma;
import edu.psu.ist.acs.micro.event.task.classify.ecoref.det.DetWordNetSynset;

public class MethodClassificationEventPairCorefDet extends MethodClassification<EventPairDatum<CorefRelType>, CorefRelType> {
	public enum Rule {
		WORD_NET_LEMMA,
		WORD_NET_SYNSET
	}
	
	private Rule rule;
	private String[] parameterNames = { "rule" };
	
	public MethodClassificationEventPairCorefDet() {
		
	}
	
	public MethodClassificationEventPairCorefDet(DatumContext<EventPairDatum<CorefRelType>, CorefRelType> context) {
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
	public boolean init(DataSet<EventPairDatum<CorefRelType>, CorefRelType> testData) {
		return true;
	}

	@Override
	public MethodClassification<EventPairDatum<CorefRelType>, CorefRelType> clone(String referenceName) {
		MethodClassificationEventPairCorefDet clone = new MethodClassificationEventPairCorefDet(this.context);
		clone.referenceName = referenceName;
		return clone;
	}

	@Override
	public MethodClassification<EventPairDatum<CorefRelType>, CorefRelType> makeInstance(
			DatumContext<EventPairDatum<CorefRelType>, CorefRelType> context) {
		return new MethodClassificationEventPairCorefDet(context);
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
	public Trainable<EventPairDatum<CorefRelType>, CorefRelType> getTrainable() {
		return null;
	}
	
	@Override
	public Map<EventPairDatum<CorefRelType>, Pair<CorefRelType, Double>> classifyWithScore(
			DataSet<EventPairDatum<CorefRelType>, CorefRelType> data) {
		Map<EventPairDatum<CorefRelType>, CorefRelType> classifications = classify(data);
		Map<EventPairDatum<CorefRelType>, Pair<CorefRelType, Double>> scores = new HashMap<>();
		for (Entry<EventPairDatum<CorefRelType>, CorefRelType> entry : classifications.entrySet())
			scores.put(entry.getKey(), new Pair<CorefRelType, Double>(entry.getValue(), 1.0));
		return scores;
	}
	
	@Override
	public Pair<CorefRelType, Double> classifyWithScore(
			EventPairDatum<CorefRelType> datum) {
		CorefRelType label = classify(datum);
		if (label != null)
			return new Pair<CorefRelType, Double>(label, 1.0);
		else
			return null;
	}
	
	@Override
	public Map<EventPairDatum<CorefRelType>, CorefRelType> classify(DataSet<EventPairDatum<CorefRelType>, CorefRelType> data) {
		Map<EventPairDatum<CorefRelType>, CorefRelType> map = new HashMap<EventPairDatum<CorefRelType>, CorefRelType>();
		
		for (EventPairDatum<CorefRelType> datum : data) {
			CorefRelType label = classify(datum);
			if (label != null)
				map.put(datum, label);
		}
	
		return map;
	}
	
	@Override
	public CorefRelType classify(EventPairDatum<CorefRelType> datum) {
		CorefRelType rel = null;
		for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++) {
			for (int j = 0; j < datum.getTarget().getSomeMentionCount(); j++) {
				CorefRelType curRel = determineRelation(datum.getSource().getSomeMention(i), datum.getTarget().getSomeMention(j));
				if (rel != null && curRel != null && curRel != rel)
					return null;
				if (curRel != null)
					rel = curRel;
			}
		}
		
		return rel;
	}
	
	private CorefRelType determineRelation(EventMention e1, EventMention e2) {
		if (this.rule == Rule.WORD_NET_LEMMA) {
			return DetWordNetLemma.determineRelation(e1, e2, this.context.getDataTools().getWordNet());
		} else if (this.rule == Rule.WORD_NET_SYNSET) {
			return DetWordNetSynset.determineRelation(e1, e2, this.context.getDataTools().getWordNet());
		} else {
			return null;
		}
	}

	@Override
	public Map<EventPairDatum<CorefRelType>, Double> score(DataSet<EventPairDatum<CorefRelType>, CorefRelType> data, CorefRelType label) {
		Map<EventPairDatum<CorefRelType>, Double> scoreMap = new HashMap<>();
		
		for (EventPairDatum<CorefRelType> datum : data) {
			scoreMap.put(datum, score(datum, label));
		}
		
		return scoreMap;
	}

	@Override
	public double score(EventPairDatum<CorefRelType> datum, CorefRelType label) {
		if (label.equals(classify(datum)))
			return 1.0;
		else
			return 0.0;
	}
}
