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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;

/**
 * FeatureTLinkableType computes the TLinkable-type 
 * (either EVENT or TIME) of one of the entities linked
 * by the TLink as an indicator vector to be used by a
 * model.  Whether the TLinkable-type is computed
 * for the source or target entity of the TLink is 
 * determined by the 'sourceOrTarget' parameter.
 * 
 * @author Bill McDowell
 *
 * @param <L> label type for the TLinks
 */
public class FeatureTLinkableType<L> extends Feature<TLinkDatum<L>, L> {
	public enum Part {
		SOURCE,
		TARGET,
		FIRST,
		SECOND
	}
	
	private Part part; 
	
	// Vector mapping EVENT and TIME to indices
	private BidirectionalLookupTable<String, Integer> vocabulary;
	private String[] parameterNames = { "part"};

	public FeatureTLinkableType() {
		
	}
	
	public FeatureTLinkableType(DatumContext<TLinkDatum<L>, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}

	@Override
	public boolean init(DataSet<TLinkDatum<L>, L> data) {
		this.vocabulary.put(TLinkable.Type.EVENT.toString(), this.vocabulary.size()); // Map EVENT to 0
		this.vocabulary.put(TLinkable.Type.TIME.toString(), this.vocabulary.size()); // Map TIME to 1
		return true;
	}

	/**
	 * @param datum
	 * @return sparse indicator vector representing whether the specified
	 * entity (source or target) of the TLink is an EVENT or a TIME.
	 */
	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum, int offset,
			Map<Integer, Double> vector) {
		if (this.part == Part.SOURCE)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getSource().getTLinkableType().toString()), 1.0);
		else if (this.part == Part.TARGET)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getTarget().getTLinkableType().toString()), 1.0);
		else if (this.part == Part.FIRST)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getFirst().getTLinkableType().toString()), 1.0);
		else if (this.part == Part.SECOND)
			vector.put(offset + this.vocabulary.get(datum.getTLink().getSecond().getTLinkableType().toString()), 1.0);
		else
			return null;
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkableType";
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
		return parameterNames;
	}

	@Override
	public Obj getParameterValue(String parameter) {
		if (parameter.equals("part"))
			return (this.part == null) ? null : Obj.stringValue(this.part.toString());
		else
			return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("part"))
			this.part = (parameterValue == null) ? null : Part.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		
		FeatureTLinkableType<Boolean> binaryFeatureTLinkableType = (FeatureTLinkableType<Boolean>)binaryFeature;
		binaryFeatureTLinkableType.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureTLinkableType;
	}

	@Override
	protected boolean cloneHelper(Feature<TLinkDatum<L>, L> clone) {
		FeatureTLinkableType<L> featureClone = (FeatureTLinkableType<L>)clone;
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

	@Override
	public Feature<TLinkDatum<L>, L> makeInstance(
			DatumContext<TLinkDatum<L>, L> context) {
		return new FeatureTLinkableType<L>(context);
	}
}
