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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.task.classify.tlink.det.DetTimeTime;

public class MethodClassificationTLinkTypeTimeTime extends MethodClassification<TLinkDatum<TimeMLRelType>, TimeMLRelType> {
	private DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> context;
	
	public MethodClassificationTLinkTypeTimeTime() {
		
	}
	
	public MethodClassificationTLinkTypeTimeTime(DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> context) {
		this.context = context;
	}
	
	@Override
	public String[] getParameterNames() {
		return new String[0];
	}

	@Override
	public Obj getParameterValue(String parameter) {
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		return false;
	}

	@Override
	public boolean init(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> testData) {
		return true;
	}

	@Override
	public MethodClassification<TLinkDatum<TimeMLRelType>, TimeMLRelType> clone(String referenceName) {
		MethodClassificationTLinkTypeTimeTime clone = new MethodClassificationTLinkTypeTimeTime(this.context);
		clone.referenceName = referenceName;
		return clone;
	}

	@Override
	public MethodClassification<TLinkDatum<TimeMLRelType>, TimeMLRelType> makeInstance(
			DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> context) {
		return new MethodClassificationTLinkTypeTimeTime(context);
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
		return "TimeTime";
	}

	@Override
	public boolean hasTrainable() {
		return false;
	}

	@Override
	public Trainable<TLinkDatum<TimeMLRelType>, TimeMLRelType> getTrainable() {
		return null;
	}
	
	@Override
	public Map<TLinkDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> classifyWithScore(
			DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> classifications = classify(data);
		Map<TLinkDatum<TimeMLRelType>, Pair<TimeMLRelType, Double>> scores = new HashMap<>();
		for (Entry<TLinkDatum<TimeMLRelType>, TimeMLRelType> entry : classifications.entrySet())
			scores.put(entry.getKey(), new Pair<TimeMLRelType, Double>(entry.getValue(), 1.0));
		return scores;
	}
	
	@Override
	public Pair<TimeMLRelType, Double> classifyWithScore(
			TLinkDatum<TimeMLRelType> datum) {
		TimeMLRelType label = classify(datum);
		if (label != null)
			return new Pair<TimeMLRelType, Double>(label, 1.0);
		else
			return null;
	}
	
	@Override
	public Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> classify(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data) {
		Map<TLinkDatum<TimeMLRelType>, TimeMLRelType> map = new HashMap<TLinkDatum<TimeMLRelType>, TimeMLRelType>();
		
		for (TLinkDatum<TimeMLRelType> datum : data) {
			TimeMLRelType label = classify(datum);
			if (label != null)
				map.put(datum, label);
		}
	
		return map;
	}
	
	@Override
	public TimeMLRelType classify(TLinkDatum<TimeMLRelType> datum) {		
		TLink tlink = datum.getTLink();
		if (tlink.getType() != TLink.Type.TIME_TIME)
			return null;
		
		LinkableTimeExpression t1 = (LinkableTimeExpression)tlink.getSource();
		LinkableTimeExpression t2 = (LinkableTimeExpression)tlink.getTarget();
		return DetTimeTime.determineRelation(t1, t2);
	}
	
	@Override
	public Map<TLinkDatum<TimeMLRelType>, Double> score(DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data, TimeMLRelType label) {
		Map<TLinkDatum<TimeMLRelType>, Double> scoreMap = new HashMap<>();
		
		for (TLinkDatum<TimeMLRelType> datum : data) {
			scoreMap.put(datum, score(datum, label));
		}
		
		return scoreMap;
	}

	@Override
	public double score(TLinkDatum<TimeMLRelType> datum, TimeMLRelType label) {
		if (label.equals(classify(datum)))
			return 1.0;
		else
			return 0.0;
	}
}
