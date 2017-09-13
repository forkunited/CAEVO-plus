package edu.psu.ist.acs.micro.event.data.feature;

import java.util.Map;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelIndicator;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.NormalizedTimeValue;
import edu.cmu.ml.rtw.generic.data.feature.Feature;
import edu.cmu.ml.rtw.generic.parse.AssignmentList;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.BidirectionalLookupTable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableNormalizedTimeValue;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable.Type;

/**
 * FeatureTLinkTimeAttribute computes an attribute of a TLink'ed
 * time expression
 * as an indicator vector to be used by a model. The attribute
 * that is computed for a TLink time expression is determined by 
 * the 'attribute'
 * parameter, and which time expression of the TLink to 
 * use is determined by the 'sourceOrTarget' property.
 * Valid attributes are enum properties of the 
 * temp.data.annotation.timeml.Time class.  The computed
 * indicator vector has a component for each value of the
 * enum corresponding to the specified attribute.  
 * 
 * @author Bill McDowell
 *
 * @param <L> label type of the TLink
 */
public class FeatureTLinkTimeAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		TIMEML_TYPE,
		REFERENCE,
		VALUE_TYPE
	}
	
	public enum SourceOrTarget {
		SOURCE,
		TARGET,
		EITHER
	}
	
	private SourceOrTarget sourceOrTarget;
	private Attribute attribute;
	private String[] parameterNames = { "sourceOrTarget", "attribute" };
	
	// Maps attribute values to indices within computed vectors
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkTimeAttribute() {
		
	}
	
	public FeatureTLinkTimeAttribute(DatumContext<TLinkDatum<L>, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	/**
	 * @param dataSet
	 * @return true if the feature has been initialized with a mapping
	 * from the attribute values to components in the feature vectors
	 */
	@Override
	public boolean init(DataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.TIMEML_TYPE) {
			LinkableTimeExpression.TimeMLType[] types = LinkableTimeExpression.TimeMLType.values();
			for (int i = 0 ; i < types.length; i++) {
				this.vocabulary.put(types[i].toString(), i);
			}
		} else if (this.attribute == Attribute.VALUE_TYPE) {
			NormalizedTimeValue.Type[] types = NormalizedTimeValue.Type.values();
			for (int i = 0; i < types.length; i++) {
				this.vocabulary.put(types[i].toString(), i);
			}
		} else if (this.attribute == Attribute.REFERENCE) {
			LinkableNormalizedTimeValue.Reference[] refs = LinkableNormalizedTimeValue.Reference.values();
			for (int i = 0; i < refs.length; i++) {
				this.vocabulary.put(refs[i].toString(), i);
			}
		}
		
		return true;
	}

	/**
	 * @param datum
	 * @return sparse vector representing an attribute value for
	 * a time linked by a TLink
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum, int offset, Map<Integer, Double> vector) {		
		TLinkable linkable = null;
		if (this.sourceOrTarget == SourceOrTarget.SOURCE || (this.sourceOrTarget == SourceOrTarget.EITHER && datum.getTLink().getSource().getTLinkableType() == Type.TIME))
			linkable = datum.getTLink().getSource();
		else if (this.sourceOrTarget == SourceOrTarget.TARGET || (this.sourceOrTarget == SourceOrTarget.EITHER && datum.getTLink().getTarget().getTLinkableType() == Type.TIME))
			linkable = datum.getTLink().getTarget();
		
		LinkableTimeExpression t = null;
		// to make sure we're only adding features for events:
		if (linkable.getTLinkableType() == TLinkable.Type.TIME)
			t = (LinkableTimeExpression) linkable;
		else
			return vector;
		
		if (this.attribute == Attribute.TIMEML_TYPE)
			vector.put(offset + this.vocabulary.get(t.getTimeMLType().toString()), 1.0);
		else if (this.attribute == Attribute.VALUE_TYPE && t.getValue().getType() != null)
			vector.put(offset + this.vocabulary.get(t.getValue().getType().toString()), 1.0);
		else if (this.attribute == Attribute.REFERENCE)
			vector.put(offset + this.vocabulary.get(t.getValue().getReference().toString()), 1.0);
		
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkTimeAttribute";
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
		if (parameter.equals("sourceOrTarget"))
			return (this.sourceOrTarget == null) ? null : Obj.stringValue(this.sourceOrTarget.toString());
		else if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : Obj.stringValue(this.attribute.toString());
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("sourceOrTarget"))
			this.sourceOrTarget = (parameterValue == null) ? null : SourceOrTarget.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}
	
	@Override
	public Feature<TLinkDatum<L>, L> makeInstance(
			DatumContext<TLinkDatum<L>, L> context) {
		return new FeatureTLinkTimeAttribute<L>(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		FeatureTLinkTimeAttribute<Boolean> binaryFeatureTimeAttribute = (FeatureTLinkTimeAttribute<Boolean>)binaryFeature;
		binaryFeatureTimeAttribute.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureTimeAttribute;
	}

	@Override
	protected boolean cloneHelper(Feature<TLinkDatum<L>, L> clone) {
		FeatureTLinkTimeAttribute<L> featureClone = (FeatureTLinkTimeAttribute<L>)clone;
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
