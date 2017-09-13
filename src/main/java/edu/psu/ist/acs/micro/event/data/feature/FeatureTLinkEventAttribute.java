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
import edu.cmu.ml.rtw.generic.util.CounterTable;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable.Type;

public class FeatureTLinkEventAttribute<L> extends Feature<TLinkDatum<L>, L>{
	public enum Attribute {
		TIMEML_TENSE,
		TIMEML_ASPECT,
		TIMEML_POLARITY,
		TIMEML_CLASS,
		TIMEML_POS,
		TIMEML_MOOD,
		TIMEML_VERB_FORM,
		MODALITY
	}
	
	public enum SourceOrTarget {
		SOURCE,
		TARGET,
		EITHER
	}
	
	private SourceOrTarget sourceOrTarget;
	private Attribute attribute;
	private String[] parameterNames = { "sourceOrTarget", "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureTLinkEventAttribute() {
		
	}
	
	public FeatureTLinkEventAttribute(DatumContext<TLinkDatum<L>, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	@Override
	public boolean init(DataSet<TLinkDatum<L>, L> dataSet) {
		if (this.attribute == Attribute.TIMEML_TENSE) {
			EventMention.TimeMLTense[] tenses = EventMention.TimeMLTense.values();
			for (int i = 0 ; i < tenses.length; i++){
				this.vocabulary.put(tenses[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_ASPECT) {
			EventMention.TimeMLAspect[] aspects = EventMention.TimeMLAspect.values();
			for (int i = 0 ; i < aspects.length; i++){
				this.vocabulary.put(aspects[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_POLARITY) {
			EventMention.TimeMLPolarity[] polarities = EventMention.TimeMLPolarity.values();
			for (int i = 0 ; i < polarities.length; i++){
				this.vocabulary.put(polarities[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_CLASS) {
			EventMention.TimeMLClass[] classes = EventMention.TimeMLClass.values();
			for (int i = 0 ; i < classes.length; i++){
				this.vocabulary.put(classes[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_POS) {
			EventMention.TimeMLPoS[] poss = EventMention.TimeMLPoS.values();
			for (int i = 0 ; i < poss.length; i++){
				this.vocabulary.put(poss[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_MOOD) {
			EventMention.TimeMLMood[] moods = EventMention.TimeMLMood.values();
			for (int i = 0 ; i < moods.length; i++){
				this.vocabulary.put(moods[i].toString(), i);
			}
		} else if (this.attribute == Attribute.TIMEML_VERB_FORM) {
			EventMention.TimeMLVerbForm[] verbForms = EventMention.TimeMLVerbForm.values();
			for (int i = 0 ; i < verbForms.length; i++){
				this.vocabulary.put(verbForms[i].toString(), i);
			}
		} else if (this.attribute == Attribute.MODALITY) {
			CounterTable<String> counter = new CounterTable<String>();
			
			dataSet.map(new ThreadMapper.Fn<TLinkDatum<L>, Boolean>() {
				@Override
				public Boolean apply(TLinkDatum<L> datum) {
					TLinkable source = datum.getTLink().getSource();
					TLinkable target = datum.getTLink().getTarget();
					if (source.getTLinkableType() == TLinkable.Type.EVENT)
						counter.incrementCount(((EventMention)source).getModality() != null ? ((EventMention)source).getModality() : "");
					if (target.getTLinkableType() == TLinkable.Type.EVENT)
						counter.incrementCount(((EventMention)target).getModality() != null ? ((EventMention)target).getModality() : "");
				
					return true;
				}
			}, this.context.getMaxThreads());
			
			counter.incrementCount("");
			
			this.vocabulary = new BidirectionalLookupTable<String, Integer>(counter.buildIndex());
		}		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(TLinkDatum<L> datum, int offset, Map<Integer, Double> vector) {
		TLinkable linkable = null;
		if (this.sourceOrTarget == SourceOrTarget.SOURCE || (this.sourceOrTarget == SourceOrTarget.EITHER && datum.getTLink().getSource().getTLinkableType() == Type.EVENT))
			linkable = datum.getTLink().getSource();
		else if (this.sourceOrTarget == SourceOrTarget.TARGET || (this.sourceOrTarget == SourceOrTarget.EITHER && datum.getTLink().getTarget().getTLinkableType() == Type.EVENT))
			linkable = datum.getTLink().getTarget();
		
		EventMention e = null;
		// to make sure we're only adding features for events:
		if (linkable != null && linkable.getTLinkableType() == TLinkable.Type.EVENT)
			e = (EventMention)linkable;
		else
			return vector;
		
		if (this.attribute == Attribute.TIMEML_TENSE)
			vector.put(offset + this.vocabulary.get(e.getTimeMLTense().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_ASPECT)
			vector.put(offset + this.vocabulary.get(e.getTimeMLAspect().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_POLARITY)
			vector.put(offset + this.vocabulary.get(e.getTimeMLPolarity().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_CLASS)
			vector.put(offset + this.vocabulary.get(e.getTimeMLClass().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_POS)
			vector.put(offset + this.vocabulary.get(e.getTimeMLPoS().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_MOOD)
			vector.put(offset + this.vocabulary.get(e.getTimeMLMood().toString()), 1.0);
		else if (this.attribute == Attribute.TIMEML_VERB_FORM)
			vector.put(offset + this.vocabulary.get(e.getTimeMLVerbForm().toString()), 1.0);
		else if (this.attribute == Attribute.MODALITY) {
			if (e.getModality() == null || this.vocabulary.containsKey(e.getModality()))
				vector.put(offset + this.vocabulary.get((e.getModality() != null) ? e.getModality() : ""), 1.0);
		}
		return vector;
	}

	@Override
	public String getGenericName() {
		return "TLinkEventAttribute";
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
		return new FeatureTLinkEventAttribute<L>(context);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		FeatureTLinkEventAttribute<Boolean> binaryFeatureEventAttribute = (FeatureTLinkEventAttribute<Boolean>)binaryFeature;
		binaryFeatureEventAttribute.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureEventAttribute;
	}

	@Override
	protected boolean cloneHelper(Feature<TLinkDatum<L>, L> clone) {
		FeatureTLinkEventAttribute<L> featureClone = (FeatureTLinkEventAttribute<L>)clone;
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
