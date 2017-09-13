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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventDatumTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventDatumTools.TimeExpressionExtractor;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;

public class FeatureTimeExpressionAttribute<D extends Datum<L>, L> extends Feature<D, L>{
	public enum Attribute {
		TIMEML_DOCUMENT_FUNCTION
	}
	
	private TimeExpressionExtractor<D, L> expressionExtractor;
	private Attribute attribute;
	private String[] parameterNames = { "expressionExtractor", "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTimeExpressionAttribute() {
		
	}
	
	public FeatureTimeExpressionAttribute(DatumContext<D, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	@Override
	public boolean init(DataSet<D, L> dataSet) {
		if (this.attribute == Attribute.TIMEML_DOCUMENT_FUNCTION) {
			this.vocabulary.put(LinkableTimeExpression.TimeMLDocumentFunction.CREATION_TIME.toString(), 0);
			this.vocabulary.put(LinkableTimeExpression.TimeMLDocumentFunction.NONE.toString(), 1);
		} 
		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(D datum, int offset, Map<Integer, Double> vector) {
		LinkableTimeExpression[] exprs = this.expressionExtractor.extract(datum);
		
		for (LinkableTimeExpression e : exprs) {
			if (this.attribute == Attribute.TIMEML_DOCUMENT_FUNCTION && e.getTimeMLDocumentFunction() != null) {
				if (this.vocabulary.containsKey(e.getTimeMLDocumentFunction().toString()))
					vector.put(offset + this.vocabulary.get(e.getTimeMLDocumentFunction().toString()), 1.0);
			}
		}
		
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TimeExpressionAttribute";
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
		if (parameter.equals("expressionExtractor"))
			return (this.expressionExtractor == null) ? null : Obj.stringValue(this.expressionExtractor.toString());
		else if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : Obj.stringValue(this.attribute.toString());
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("expressionExtractor"))
			this.expressionExtractor = (parameterValue == null) ? null : ((EventDatumTools<D, L>)this.context.getDatumTools()).getTimeExpressionExtractor(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@Override
	public Feature<D, L> makeInstance(
			DatumContext<D, L> context) {
		return new FeatureTimeExpressionAttribute<D, L>(context);
	}

	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		FeatureTimeExpressionAttribute<T, Boolean> binaryFeatureEventAttribute = (FeatureTimeExpressionAttribute<T, Boolean>)binaryFeature;
		binaryFeatureEventAttribute.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureEventAttribute;
	}

	@Override
	protected boolean cloneHelper(Feature<D, L> clone) {
		FeatureTimeExpressionAttribute<D, L> featureClone = (FeatureTimeExpressionAttribute<D, L>)clone;
		featureClone.vocabulary = this.vocabulary;
		return true;
	}

	@Override
	protected boolean fromParseInternalHelper(AssignmentList internalAssignments) {
		return true;
	}

	@Override
	protected AssignmentList toParseInternalHelper(AssignmentList internalAssignments) {
		return internalAssignments;
	}
}
