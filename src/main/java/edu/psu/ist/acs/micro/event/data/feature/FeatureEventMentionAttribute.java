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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventDatumTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventDatumTools.EventMentionExtractor;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.Attribute;

public class FeatureEventMentionAttribute<D extends Datum<L>, L> extends Feature<D, L>{
	private EventMentionExtractor<D, L> mentionExtractor;
	private Attribute attribute;
	private String[] parameterNames = { "mentionExtractor", "attribute" };
	
	private BidirectionalLookupTable<String, Integer> vocabulary;
	
	public FeatureEventMentionAttribute() {
		
	}
	
	public FeatureEventMentionAttribute(DatumContext<D, L> context) {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
		this.context = context;
	}
	
	@Override
	public boolean init(DataSet<D, L> dataSet) {
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
			
			dataSet.map(new ThreadMapper.Fn<D, Boolean>() {
				@Override
				public Boolean apply(D datum) {
					EventMention[] mentions = mentionExtractor.extract(datum);
					for (EventMention e : mentions)
						counter.incrementCount((e.getModality() != null ? e.getModality() : ""));				
					return true;
				}
			}, this.context.getMaxThreads());
			
			counter.incrementCount("");
			
			this.vocabulary = new BidirectionalLookupTable<String, Integer>(counter.buildIndex());
		}		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(D datum, int offset, Map<Integer, Double> vector) {
		EventMention[] mentions = this.mentionExtractor.extract(datum);
		
		for (EventMention e : mentions) {
			if (this.attribute == Attribute.TIMEML_TENSE && e.getTimeMLTense() != null) {
				vector.put(offset + this.vocabulary.get(e.getTimeMLTense().toString()), 1.0);
			} else if (this.attribute == Attribute.TIMEML_ASPECT && e.getTimeMLAspect() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLAspect().toString()), 1.0);
			else if (this.attribute == Attribute.TIMEML_POLARITY && e.getTimeMLPolarity() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLPolarity().toString()), 1.0);
			else if (this.attribute == Attribute.TIMEML_CLASS && e.getTimeMLClass() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLClass().toString()), 1.0);
			else if (this.attribute == Attribute.TIMEML_POS && e.getTimeMLPoS() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLPoS().toString()), 1.0);
			else if (this.attribute == Attribute.TIMEML_MOOD && e.getTimeMLMood() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLMood().toString()), 1.0);
			else if (this.attribute == Attribute.TIMEML_VERB_FORM && e.getTimeMLVerbForm() != null)
				vector.put(offset + this.vocabulary.get(e.getTimeMLVerbForm().toString()), 1.0);
			else if (this.attribute == Attribute.MODALITY) {
				if (e.getModality() == null || this.vocabulary.containsKey(e.getModality()))
					vector.put(offset + this.vocabulary.get((e.getModality() != null) ? e.getModality() : ""), 1.0);
			}
		}
		
		return vector;
	}

	@Override
	public String getGenericName() {
		return "EventMentionAttribute";
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
		if (parameter.equals("mentionExtractor"))
			return (this.mentionExtractor == null) ? null : Obj.stringValue(this.mentionExtractor.toString());
		else if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : Obj.stringValue(this.attribute.toString());
		return null;
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("mentionExtractor"))
			this.mentionExtractor = (parameterValue == null) ? null : ((EventDatumTools<D, L>)this.context.getDatumTools()).getEventMentionExtractor(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(this.context.getMatchValue(parameterValue));
		else
			return false;
		return true;
	}

	@Override
	public Feature<D, L> makeInstance(
			DatumContext<D, L> context) {
		return new FeatureEventMentionAttribute<D, L>(context);
	}

	@Override
	protected <T extends Datum<Boolean>> Feature<T, Boolean> makeBinaryHelper(
			DatumContext<T, Boolean> context, LabelIndicator<L> labelIndicator,
			Feature<T, Boolean> binaryFeature) {
		FeatureEventMentionAttribute<T, Boolean> binaryFeatureEventAttribute = (FeatureEventMentionAttribute<T, Boolean>)binaryFeature;
		binaryFeatureEventAttribute.vocabulary = this.vocabulary;
		return (Feature<T, Boolean>)binaryFeatureEventAttribute;
	}

	@Override
	protected boolean cloneHelper(Feature<D, L> clone) {
		FeatureEventMentionAttribute<D, L> featureClone = (FeatureEventMentionAttribute<D, L>)clone;
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
