package edu.psu.ist.acs.micro.event.model.annotator.nlp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;




import edu.cmu.ml.rtw.generic.data.StoredItemSet;
import edu.cmu.ml.rtw.generic.data.annotation.AnnotationType;
import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpansDatum;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorTokenSpan;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.task.classify.MethodClassification;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.cmu.ml.rtw.generic.util.Triple;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLAspect;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLClass;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLPolarity;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLTense;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMentionDatum;
import edu.psu.ist.acs.micro.event.util.EventProperties;


public class EventAnnotator implements AnnotatorTokenSpan<EventMention> {
	private static final AnnotationType<?>[] REQUIRED_ANNOTATIONS = new AnnotationType<?>[] {
		AnnotationTypeNLP.TOKEN,
		AnnotationTypeNLP.POS,
		AnnotationTypeNLP.CONSTITUENCY_PARSE,
		AnnotationTypeNLP.DEPENDENCY_PARSE,
		AnnotationTypeNLP.NER,
		AnnotationTypeNLP.TIME_EXPRESSION
	};
	
	public static final File DEFAULT_EVENT_DETECTOR_MODEL_FILE = new File("models/BinaryEvent_Test_StanfordLinear");
	public static final File DEFAULT_EVENT_ATTRIBUTE_MODEL_FILE = new File("models/AttributeEvent_Test_StanfordLinear");
	public static final String DEFAULT_EVENT_DETECTOR_MODEL_PARSE_PATH = "eval.methodEventBinary.method";
	public static final String DEFAULT_EVENT_ATTRIBUTE_TENSE_MODEL_PARSE_PATH = "eval.methodEventAttribute.methodTense";
	public static final String DEFAULT_EVENT_ATTRIBUTE_ASPECT_MODEL_PARSE_PATH = "eval.methodEventAttribute.methodAspect";
	public static final String DEFAULT_EVENT_ATTRIBUTE_CLASS_MODEL_PARSE_PATH = "eval.methodEventAttribute.methodClass";
	public static final String DEFAULT_EVENT_ATTRIBUTE_POLARITY_MODEL_PARSE_PATH = "eval.methodEventAttribute.methodPolarity";
	public static final String DEFAULT_EVENT_ATTRIBUTE_MODALITY_MODEL_PARSE_PATH = "eval.methodEventAttribute.methodModality";
	
	private EventDataTools dataTools;
	private Tools<TokenSpansDatum<Boolean>, Boolean> eventDetectionDatumTools;
	private Tools<EventMentionDatum<String>, String> eventAttributeDatumTools;
	
	private StoredItemSet<EventMention, EventMention> storedEventMentions;
	
	private MethodClassification<TokenSpansDatum<Boolean>, Boolean> eventDetector;
	private MethodClassification<EventMentionDatum<String>, String> eventTenseClassifier;
	private MethodClassification<EventMentionDatum<String>, String> eventAspectClassifier;
	private MethodClassification<EventMentionDatum<String>, String> eventClassClassifier;
	private MethodClassification<EventMentionDatum<String>, String> eventPolarityClassifier;
	private MethodClassification<EventMentionDatum<String>, String> eventModalityClassifier;

	private int maxSentenceLength;

	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions) {
		this(storedEventMentions, DEFAULT_EVENT_DETECTOR_MODEL_FILE, DEFAULT_EVENT_ATTRIBUTE_MODEL_FILE, 0);
	}
	
	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions, EventDataTools dataTools) {
		this(storedEventMentions, dataTools, 0);
	}
	
	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions, int maxSentenceLength) {
		this(storedEventMentions, DEFAULT_EVENT_DETECTOR_MODEL_FILE, DEFAULT_EVENT_ATTRIBUTE_MODEL_FILE, maxSentenceLength);
	}
	
	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions, EventDataTools dataTools, int maxSentenceLength) {
		this(storedEventMentions, 
				DEFAULT_EVENT_DETECTOR_MODEL_FILE, 
				DEFAULT_EVENT_ATTRIBUTE_MODEL_FILE,
				DEFAULT_EVENT_DETECTOR_MODEL_PARSE_PATH,
				DEFAULT_EVENT_ATTRIBUTE_TENSE_MODEL_PARSE_PATH,
				DEFAULT_EVENT_ATTRIBUTE_ASPECT_MODEL_PARSE_PATH,
				DEFAULT_EVENT_ATTRIBUTE_CLASS_MODEL_PARSE_PATH,
				DEFAULT_EVENT_ATTRIBUTE_POLARITY_MODEL_PARSE_PATH,
				DEFAULT_EVENT_ATTRIBUTE_MODALITY_MODEL_PARSE_PATH,
				0,
				dataTools, 
				maxSentenceLength);
	}

	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions, 
						  File eventDetectorModelFile, 
						  File eventAttributeModelFile,
						  int maxSentenceLength) {
		this(storedEventMentions,
			 eventDetectorModelFile, 
			 eventAttributeModelFile,
			 DEFAULT_EVENT_DETECTOR_MODEL_PARSE_PATH,
			 DEFAULT_EVENT_ATTRIBUTE_TENSE_MODEL_PARSE_PATH,
			 DEFAULT_EVENT_ATTRIBUTE_ASPECT_MODEL_PARSE_PATH,
			 DEFAULT_EVENT_ATTRIBUTE_CLASS_MODEL_PARSE_PATH,
			 DEFAULT_EVENT_ATTRIBUTE_POLARITY_MODEL_PARSE_PATH,
			 DEFAULT_EVENT_ATTRIBUTE_MODALITY_MODEL_PARSE_PATH,
			 maxSentenceLength
		);
	}
	
	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions,
			  File eventDetectorModelFile, 
			  File eventAttributeModelFile, 
			  String eventDetectorParsePath, 
			  String eventAttributeTenseParsePath, 
			  String eventAttributeAspectParsePath, 
			  String eventAttributeClassParsePath, 
			  String eventAttributePolarityParsePath, 
			  String eventAttributeModalityParsePath,
			  int maxSentenceLength) {
		this(storedEventMentions,
			 eventDetectorModelFile,
			 eventAttributeModelFile, 
			 eventDetectorParsePath, 
			 eventAttributeTenseParsePath, 
			 eventAttributeAspectParsePath, 
			 eventAttributeClassParsePath, 
			 eventAttributePolarityParsePath, 
			 eventAttributeModalityParsePath,
			 0, null, maxSentenceLength);
	}
	
	public EventAnnotator(StoredItemSet<EventMention, EventMention> storedEventMentions,
						  File eventDetectorModelFile, 
						  File eventAttributeModelFile, 
						  String eventDetectorParsePath, 
						  String eventAttributeTenseParsePath, 
						  String eventAttributeAspectParsePath, 
						  String eventAttributeClassParsePath, 
						  String eventAttributePolarityParsePath, 
						  String eventAttributeModalityParsePath,
						  int initIncrementId,
						  EventDataTools dataTools,
						  int maxSentenceLength) {
		this.dataTools = dataTools != null ? dataTools : new EventDataTools(new OutputWriter(), new EventProperties(), initIncrementId);
		this.eventDetectionDatumTools = TokenSpansDatum.getBooleanTools(this.dataTools);
		this.eventAttributeDatumTools = EventMentionDatum.getStringTools(this.dataTools);
		
		this.storedEventMentions = storedEventMentions;
		
		this.maxSentenceLength = maxSentenceLength;
		
		if (!deserialize(eventDetectorModelFile, 
						 eventAttributeModelFile, 
						 eventDetectorParsePath,
						 eventAttributeTenseParsePath,
						 eventAttributeAspectParsePath,
						 eventAttributeClassParsePath,
						 eventAttributePolarityParsePath,
						 eventAttributeModalityParsePath
						))
			throw new IllegalArgumentException();
	}
	
	public boolean deserialize(File eventDetectorModelFile, 
							  File eventAttributeModelFile, 
							  String eventDetectorParsePath, 
							  String eventAttributeTenseParsePath, 
							  String eventAttributeAspectParsePath, 
							  String eventAttributeClassParsePath, 
							  String eventAttributePolarityParsePath, 
							  String eventAttributeModalityParsePath) {
		
		this.eventDetector = DatumContext.run(this.eventDetectionDatumTools, eventDetectorModelFile).getMatchClassifyMethod(Obj.curlyBracedValue(eventDetectorParsePath));
		DatumContext<EventMentionDatum<String>, String> attrCtx = DatumContext.run(this.eventAttributeDatumTools, eventAttributeModelFile);
		
		this.eventTenseClassifier = attrCtx.getMatchClassifyMethod(Obj.curlyBracedValue(eventAttributeTenseParsePath));
		this.eventAspectClassifier = attrCtx.getMatchClassifyMethod(Obj.curlyBracedValue(eventAttributeAspectParsePath));
		this.eventClassClassifier = attrCtx.getMatchClassifyMethod(Obj.curlyBracedValue(eventAttributeClassParsePath));
		this.eventPolarityClassifier = attrCtx.getMatchClassifyMethod(Obj.curlyBracedValue(eventAttributePolarityParsePath));
		this.eventModalityClassifier = attrCtx.getMatchClassifyMethod(Obj.curlyBracedValue(eventAttributeModalityParsePath));
		
		return true;
	}

	@Override
	public String getName() {
		return "psu_event-0.0.1";
	}

	@Override
	public AnnotationType<EventMention> produces() {
		return AnnotationTypeNLPEvent.EVENT_MENTION;
	}

	@Override
	public AnnotationType<?>[] requires() {
		return REQUIRED_ANNOTATIONS;
	}

	@Override
	public boolean measuresConfidence() {
		return false;
	}

	private DataSet<TokenSpansDatum<Boolean>, Boolean> constructEventDetectionData(DocumentNLP document) {
		DataSet<TokenSpansDatum<Boolean>, Boolean> data = new DataSet<TokenSpansDatum<Boolean>, Boolean>(this.eventDetectionDatumTools, null);
		
		for (int i = 0; i < document.getSentenceCount(); i++) {
			if (this.maxSentenceLength > 0 && document.getSentenceTokenCount(i) > this.maxSentenceLength)
				continue;
			
			for (int j = 0; j < document.getSentenceTokenCount(i); j++) {
				TokenSpan span = new TokenSpan(document, i, j, j+1);
				data.add(new TokenSpansDatum<Boolean>(this.dataTools.getIncrementId(), new TokenSpan[] { span }, null));
			}
		}
		
		return data;
	}
	
	private DataSet<EventMentionDatum<String>, String> constructEventAttributeData(Map<TokenSpansDatum<Boolean>, Boolean> eventDetectionOutput) {
		DataSet<EventMentionDatum<String>, String> data = new DataSet<EventMentionDatum<String>, String>(this.eventAttributeDatumTools, null);
		
		for (Entry<TokenSpansDatum<Boolean>, Boolean> entry : eventDetectionOutput.entrySet()) {
			if (!entry.getValue())
				continue;
			EventMention eventMention = new EventMention(dataTools,
					null, 
					String.valueOf(entry.getKey().getId()), 
					null,
					null,
					entry.getKey().getTokenSpans()[0],
					null,
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null,
					null,
					null,
					null,
					null);
			
			EventMentionDatum<String> datum = new EventMentionDatum<String>(entry.getKey().getId(), eventMention, null);
			data.add(datum);
		}
		
		return data;
	}
	
	@Override
	public List<Triple<TokenSpan, EventMention, Double>> annotate(DocumentNLP document) {
		DataSet<EventMentionDatum<String>, String> mentionData = 
				constructEventAttributeData(
					this.eventDetector.classify(
						constructEventDetectionData(document)));
		
		Map<EventMentionDatum<String>, String> tenses = this.eventTenseClassifier.classify(mentionData);
		Map<EventMentionDatum<String>, String> aspects = this.eventAspectClassifier.classify(mentionData);
		Map<EventMentionDatum<String>, String> classes = this.eventClassClassifier.classify(mentionData);
		Map<EventMentionDatum<String>, String> polarities = this.eventPolarityClassifier.classify(mentionData);
		Map<EventMentionDatum<String>, String> modalities = this.eventModalityClassifier.classify(mentionData);
		
		List<Triple<TokenSpan, EventMention, Double>> mentions = new ArrayList<Triple<TokenSpan, EventMention, Double>>();

		String storageName = this.storedEventMentions.getStoredItems().getStorageName();
		String collectionName = this.storedEventMentions.getStoredItems().getName();
		
		for (EventMentionDatum<String> datum : mentionData) {
			TimeMLTense tense = TimeMLTense.valueOf(tenses.get(datum));
			TimeMLAspect aspect = TimeMLAspect.valueOf(aspects.get(datum));
			TimeMLClass clazz = TimeMLClass.valueOf(classes.get(datum));
			TimeMLPolarity polarity = TimeMLPolarity.valueOf(polarities.get(datum));
			String modality = modalities.get(datum);
			
			
			EventMention eventMention = 
				new EventMention(dataTools,
					new StoreReference(storageName, collectionName, "id", datum.getMention().getId()), 
					datum.getMention().getId(), 
					null,
					null,
					datum.getMention().getTokenSpan(),
					null,
					tense, 
					aspect, 
					clazz, 
					polarity, 
					null, 
					null, 
					null, 
					modality,
					null,
					null,
					null,
					null);
		
			this.storedEventMentions.addItem(eventMention);
			
			mentions.add(new Triple<TokenSpan, EventMention, Double>(eventMention.getTokenSpan(), eventMention, null));	
		}
		
		return mentions;
	}
}
