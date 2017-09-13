package edu.psu.ist.acs.micro.event.data.feature;

import java.util.Map;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelIndicator;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.feature.Feature;
import edu.cmu.ml.rtw.generic.parse.AssignmentList;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.BidirectionalLookupTable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;

/**
 * FeatureTLinkTimeRelation computes the relation type 
 * of a Time-Time link according to the grounding of the times
 * to intervals.  The 'relation' property determines whether
 * to compute the relation type between the source and target
 * of a TLink, between the source time of the TLink and the DCT, or
 * between the target time of the TLink and the DCT.  The 
 * computed relation type is represented as an indicator vector
 * to be used by a model.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type of the TLink
 */
public class FeatureTLinkTimeRelation<L> extends Feature<TLinkDatum<L>, L>{
	public enum Relation {
		TARGET_DCT,
		SOURCE_DCT,
		SOURCE_TARGET
	}
	
	private Relation relation;
	private String[] parameterNames = { "relation" };
	
	// Maps relationship types to vector component indices
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkTimeRelation() {
		
	}
	
	public FeatureTLinkTimeRelation(DatumContext<TLinkDatum<L>, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	/**
	 * @param dataSet
	 * @return true if the feature has been initialized with a mapping
	 * from relationship types to vector component indices 
	 */
	@Override
	public boolean init(DataSet<TLinkDatum<L>, L> dataSet) {
		TLink.TimeMLRelType[] relations = TLink.TimeMLRelType.values();
		for (int i = 0 ; i < relations.length; i++){
			this.vocabulary.put(relations[i].toString(), i);
		}
	
		return true;
	}

	/**
	 * @param datum
	 * @return a sparse indicator vector representing the relationship
	 * type of either the datum's Time-Time TLink or the TLink between 
	 * one of the
	 * datum's incident timexes and the DCT according to the groundings
	 * of the times to intervals.
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum, int offset, Map<Integer, Double> vector) {		
		LinkableTimeExpression time1 = null;
		LinkableTimeExpression time2 = null;
		if (this.relation == Relation.SOURCE_DCT) {
			if (datum.getTLink().getSource().getTLinkableType() != TLinkable.Type.TIME)
				return vector;
			time1 = (LinkableTimeExpression)datum.getTLink().getSource();
			DocumentNLP document = time1.getTokenSpan().getDocument();
			time2 = document.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME);
		} else if (this.relation == Relation.TARGET_DCT) {
			if (datum.getTLink().getTarget().getTLinkableType() != TLinkable.Type.TIME)
				return vector;
			time1 = (LinkableTimeExpression)datum.getTLink().getTarget();
			DocumentNLP document = time1.getTokenSpan().getDocument();
			time2 = document.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME);
		} else if (this.relation == Relation.SOURCE_TARGET) {
			if (datum.getTLink().getSource().getTLinkableType() != TLinkable.Type.TIME ||
					datum.getTLink().getTarget().getTLinkableType() != TLinkable.Type.TIME)
				return vector;
			
			time1 = (LinkableTimeExpression)datum.getTLink().getSource();
			time2 = (LinkableTimeExpression)datum.getTLink().getTarget();
		}
		
		if (time1 == null || time2 == null)
			return vector;
		
		TLink.TimeMLRelType relType = time1.getRelationToTime(time2);
		vector.put(offset + this.vocabulary.get(relType.toString()), 1.0);
	
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkTimeRelation";
	}

	@Override
	public int getVocabularySize() {
		return this.vocabulary.size();
	}

	@Override
	public String getVocabularyTerm(int index) {
		return this.vocabulary.reverseGet(index);
	}

	@Override
	protected boolean setVocabularyTerm(int index, String term) {
		this.vocabulary.put(term, index);
		return true;
	}

	@Override
	public String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	public Obj getParameterValue(String parameter) {
		if (parameter.equals("relation"))
			return (this.relation == null) ? null : Obj.stringValue(this.relation.toString());
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("relation"))
			this.relation = (parameterValue == null) ? null : Relation.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@Override
	public Feature<TLinkDatum<L>, L> makeInstance(DatumContext<TLinkDatum<L>, L> context) {
		return new FeatureTLinkTimeRelation<L>(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		FeatureTLinkTimeRelation<Boolean> binaryFeatureTimeRelation = (FeatureTLinkTimeRelation<Boolean>)binaryFeature;
		binaryFeatureTimeRelation.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureTimeRelation;
	}

	@Override
	protected boolean cloneHelper(Feature<TLinkDatum<L>, L> clone) {
		FeatureTLinkTimeRelation<L> featureClone = (FeatureTLinkTimeRelation<L>)clone;
		featureClone.vocabulary = this.vocabulary;
		return true;
	}

	@Override
	protected boolean fromParseInternalHelper(AssignmentList internalAssignments) {
		return true;
	}

	@Override
	protected AssignmentList toParseInternalHelper(
			AssignmentList internalAssignments) {
		return internalAssignments;
	}
}
