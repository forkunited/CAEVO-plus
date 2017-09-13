package edu.psu.ist.acs.micro.event.data.feature;

import java.util.Map;
import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelIndicator;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.feature.Feature;
import edu.cmu.ml.rtw.generic.parse.AssignmentList;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.BidirectionalLookupTable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;

/**
 * FeatureTLinkAttribute computes an attribute of a TLink
 * as an indicator vector to be used by a model. The attribute
 * that is computed for a TLink is determined by the 'attribute'
 * parameter.  Valid attributes are enum properties of the 
 * temp.data.annotation.timeml.TLink class.  The computed
 * indicator vector has a component for each value of the
 * enum corresponding to the specified attribute.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type of the TLink
 */
public class FeatureTLinkAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		POSITION,
		TYPE,
		OVER_EVENT,
		EVENT_TIME_TYPE
	}
	
	// Determines which attribute to compute
	private Attribute attribute;
	private String[] parameterNames = { "attribute" };
	
	// Mapping from attribute values to indices of the returned vector
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkAttribute() {
		
	}
	
	public FeatureTLinkAttribute(DatumContext<TLinkDatum<L>, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	/**
	 * @param dataSet
	 * @return true if the feature has been initialized with a mapping
	 * from attribute values to their indices within vectors computed
	 * for datums
	 */
	@Override
	public boolean init(DataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.POSITION) {
			TLink.Position[] positions = TLink.Position.values();
			for (int i = 0; i < positions.length; i++)
				this.vocabulary.put(positions[i].toString(), this.vocabulary.size());
		} else if (this.attribute == Attribute.TYPE) {
			TLink.Type[] types = TLink.Type.values();
			for (int i = 0; i < types.length; i++)
				this.vocabulary.put(types[i].toString(), this.vocabulary.size());
		} else if (this.attribute == Attribute.OVER_EVENT) {
			this.vocabulary.put("true", this.vocabulary.size());
			this.vocabulary.put("false", this.vocabulary.size());
		} else if (this.attribute == Attribute.EVENT_TIME_TYPE) {
			this.vocabulary.put("true", this.vocabulary.size());
		}
		
		return true;
	}
	
	/**
	 * @param datum
	 * @return sparse mapping from attribute value indices to indicators of
	 * whether or not the attribute takes the value
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum, int offset, Map<Integer, Double> vector) {
		if (this.attribute == Attribute.POSITION)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getPosition().toString()), 1.0);
		else if (this.attribute == Attribute.TYPE)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getType().toString()), 1.0);
		else if (this.attribute == Attribute.OVER_EVENT) {
			if (datum.getTLink().isOverEvent())
				vector.put(offset, 1.0);
			else
				vector.put(offset + 1, 1.0);
		} else if (this.attribute == Attribute.EVENT_TIME_TYPE) {
			vector.put(offset, (datum.getTLink().getType() == TLink.Type.EVENT_TIME) ? 1.0 : 0.0);
		}
		
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkAttribute";
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
		if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : Obj.stringValue(this.attribute.toString());
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		return binaryFeature;
	}

	@Override
	protected boolean cloneHelper(Feature<TLinkDatum<L>, L> clone) {
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

	@Override
	public Feature<TLinkDatum<L>, L> makeInstance(
			DatumContext<TLinkDatum<L>, L> context) {
		return new FeatureTLinkAttribute<L>(context);
	}
}
