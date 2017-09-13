package edu.psu.ist.acs.micro.event.data;

import java.util.Map;
import java.util.Map.Entry;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.Gazetteer;
import edu.cmu.ml.rtw.generic.data.Serializer;
import edu.cmu.ml.rtw.generic.data.SerializerJSONBSON;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpansDatum;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationUnary;
import edu.cmu.ml.rtw.generic.task.classify.meta.PredictionClassificationDatum;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.CorefRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.DataSetBuilderTokenSpanAnnnotationIndicator;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Entity;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EntityMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Event;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMentionDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMentionPairDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventPairDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventTimeDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableNormalizedTimeValue;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Relation;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.RelationMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Signal;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TimePairDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Value;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.ValueMention;
import edu.psu.ist.acs.micro.event.data.feature.SerializerDataTLinkTypeFeatureMatrixBSONString;
import edu.psu.ist.acs.micro.event.util.EventProperties;

/**
 * EventDataTools contains definitions of cleaning 
 * functions, gazetteers, and other tools used by
 * features in the event models.
 * 
 * @author Bill McDowell
 *
 */
public class EventDataTools extends DataTools {
	public EventDataTools() {
		this(new EventProperties());
		
	}
	
	public EventDataTools(EventProperties properties) {
		this(new OutputWriter(), properties, 0);
		
	}
	
	public EventDataTools(OutputWriter outputWriter, EventDataTools dataTools) {
		this(outputWriter, (EventProperties)dataTools.properties, dataTools.incrementId);
		
		this.timer = dataTools.timer;
		
		for (Entry<String, Gazetteer> entry : dataTools.gazetteers.entrySet())
			this.gazetteers.put(entry.getKey(), entry.getValue());
	}
	
	@SuppressWarnings("unchecked")
	public EventDataTools(OutputWriter outputWriter, EventProperties properties, int initIncrementId) {
		super(outputWriter, properties);
		
		this.properties = properties;
		this.incrementId = initIncrementId;
		
		this.addGenericContext(new DatumContext<EventMentionDatum<Boolean>, Boolean>(EventMentionDatum.getBooleanTools(this), "EventMentionBoolean"));
		this.addGenericContext(new DatumContext<EventMentionDatum<String>, String>(EventMentionDatum.getStringTools(this), "EventMentionString"));
		this.addGenericContext(new DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType>(TLinkDatum.getTimeMLRelTypeTools(this), "TLinkType"));
		this.addGenericContext(new DatumContext<TLinkDatum<Boolean>, Boolean>(TLinkDatum.getBooleanTools(this), "TLinkBoolean"));
		this.addGenericContext(new DatumContext<EventMentionPairDatum<CorefRelType>, CorefRelType>(EventMentionPairDatum.getCorefRelTypeTools(this), "EventMentionPairCoref"));
		this.addGenericContext(new DatumContext<EventMentionPairDatum<Boolean>, Boolean>(EventMentionPairDatum.getBooleanTools(this), "EventMentionPairBoolean"));

		this.addGenericContext(new DatumContext<EventPairDatum<CorefRelType>, CorefRelType>(EventPairDatum.getCorefRelTypeTools(this), "EventCoref"));
		this.addGenericContext(new DatumContext<EventPairDatum<TimeMLRelType>, TimeMLRelType>(EventPairDatum.getTLinkTypeTools(this), "EventTLink"));
		this.addGenericContext(new DatumContext<TimePairDatum<TimeMLRelType>, TimeMLRelType>(TimePairDatum.getTLinkTypeTools(this), "TimeTLink"));
		this.addGenericContext(new DatumContext<EventTimeDatum<TimeMLRelType>, TimeMLRelType>(EventTimeDatum.getTLinkTypeTools(this), "EventTimeTLink"));
		
		this.addGenericContext(new DatumContext<PredictionClassificationDatum<Boolean>, Boolean>(PredictionClassificationDatum.getBooleanTools(TLinkDatum.getTimeMLRelTypeTools(this)), "MetaTLinkType"));
		
		((DatumContext<TokenSpansDatum<Boolean>, Boolean>)this.genericContexts.get("TokenSpansBoolean")).getDatumTools().addGenericDataSetBuilder(new DataSetBuilderTokenSpanAnnnotationIndicator());
		
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.CREATION_TIME);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.TIME_EXPRESSION);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.EVENT_MENTION);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.RELATION_MENTION);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.ENTITY_MENTION);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.VALUE_MENTION);
		this.addAnnotationTypeNLP(AnnotationTypeNLPEvent.ACE_DOCUMENT_TYPE);
		
		this.addGenericWeightedStructure(new WeightedStructureRelationUnary("O"));
		
		this.addStringPairIndicatorFn(new StringPairIndicator() {
			public String toString() {
				return "EventRelationMutex";
			}
			
			@Override
			public boolean compute(String str1, String str2) {
				if (str1.equals(str2))
					return true;
				
				boolean corefRel = false;
				for (CorefRelType rel : CorefRelType.values()) {
					if (rel.toString().equals(str1) || rel.toString().equals(str2)) {
						if (corefRel)
							return true;
						else
							corefRel = true;
					}
				}
				
				boolean timeRel = false;
				for (TimeMLRelType rel : TimeMLRelType.values()) {
					if (rel.toString().equals(str1) || rel.toString().equals(str2)) {
						if (timeRel)
							return true;
						else
							timeRel = true;
					}
				}
				
				return false;
			}
		});
	}
	
	@Override
	public DataTools makeInstance() {
		return new EventDataTools(this.outputWriter, this);
	}
	
	@Override
	public Map<String, Serializer<?, ?>> getSerializers() {
		Map<String, Serializer<?, ?>> serializers = super.getSerializers();
	
		SerializerJSONBSON<EventMention> eventMentionSerializer = new SerializerJSONBSON<EventMention>("EventMention", new EventMention(this));
		SerializerJSONBSON<Event> eventSerializer = new SerializerJSONBSON<Event>("Event", new Event(this));
		SerializerJSONBSON<EntityMention> entityMentionSerializer = new SerializerJSONBSON<EntityMention>("EntityMention", new EntityMention(this));
		SerializerJSONBSON<Entity> entitySerializer = new SerializerJSONBSON<Entity>("Entity", new Entity(this));
		SerializerJSONBSON<RelationMention> relationMentionSerializer = new SerializerJSONBSON<RelationMention>("RelationMention", new RelationMention(this));
		SerializerJSONBSON<Relation> relationSerializer = new SerializerJSONBSON<Relation>("Relation", new Relation(this));
		SerializerJSONBSON<ValueMention> valueMentionSerializer = new SerializerJSONBSON<ValueMention>("ValueMention", new ValueMention(this));
		SerializerJSONBSON<Value> valueSerializer = new SerializerJSONBSON<Value>("Value", new Value(this));
		SerializerJSONBSON<Signal> signalSerializer = new SerializerJSONBSON<Signal>("Signal", new Signal(this));
		SerializerJSONBSON<LinkableTimeExpression> timeExpressionSerializer = new SerializerJSONBSON<LinkableTimeExpression>("TimeExpression", new LinkableTimeExpression(this));
		SerializerJSONBSON<LinkableNormalizedTimeValue> timeValueSerializer = new SerializerJSONBSON<LinkableNormalizedTimeValue>("NormalizedTimeValue", new LinkableNormalizedTimeValue(this));
		SerializerJSONBSON<TLink> tlinkSerializer = new SerializerJSONBSON<TLink>("TLink", new TLink(this));
		
		SerializerDataTLinkTypeFeatureMatrixBSONString dtltFMatSerializer = new SerializerDataTLinkTypeFeatureMatrixBSONString(this);
		
		
		serializers.put(eventMentionSerializer.getName(), eventMentionSerializer);
		serializers.put(eventSerializer.getName(), eventSerializer);
		serializers.put(entityMentionSerializer.getName(), entityMentionSerializer);
		serializers.put(entitySerializer.getName(), entitySerializer);
		serializers.put(relationMentionSerializer.getName(), relationMentionSerializer);
		serializers.put(relationSerializer.getName(), relationSerializer);
		serializers.put(valueMentionSerializer.getName(), valueMentionSerializer);
		serializers.put(valueSerializer.getName(), valueSerializer);
		serializers.put(signalSerializer.getName(), signalSerializer);
		serializers.put(timeExpressionSerializer.getName(), timeExpressionSerializer);
		serializers.put(timeValueSerializer.getName(), timeValueSerializer);
		serializers.put(tlinkSerializer.getName(), tlinkSerializer);
		serializers.put(dtltFMatSerializer.getName(), dtltFMatSerializer);
		
		return serializers;
	}
}
