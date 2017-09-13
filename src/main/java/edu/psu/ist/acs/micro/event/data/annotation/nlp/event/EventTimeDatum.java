package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelMapping;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression.TimeMLDocumentFunction;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.feature.FeatureEventMentionAttribute;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTimeExpressionAttribute;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationEventTimeTLinkDet;

public class EventTimeDatum<L> extends Datum<L> {
	private Event event;
	private LinkableNormalizedTimeValue time;

	public EventTimeDatum(int id, Event event, LinkableNormalizedTimeValue time, L label) {
		this.id = id;
		this.event = event;
		this.time = time;
		this.label = label;
	}
	
	public Event getEvent() {
		return this.event;
	}
	
	public LinkableNormalizedTimeValue getTime() {
		return this.time;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.id).append(": ").append(this.event.getId()).append(" ").append(this.time.getId());
		return str.toString();
	}
	
	public static Tools<TLink.TimeMLRelType> getTLinkTypeTools(DataTools dataTools) {
		Tools<TimeMLRelType> tools =  new Tools<TimeMLRelType>(dataTools) {
			@Override
			public TimeMLRelType labelFromString(String str) {
				if (str == null)
					return null;
				try {
					return TimeMLRelType.valueOf(str);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		};
	
		
		tools.addGenericClassifyMethod(new MethodClassificationEventTimeTLinkDet());
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			@Override
			public TimeMLRelType map(TimeMLRelType label) {
				if (label == null)
					return null;
				else if (label == TimeMLRelType.AFTER)
					return TimeMLRelType.AFTER;
				else if (label == TimeMLRelType.BEFORE)
					return TimeMLRelType.BEFORE;
				else if (label == TimeMLRelType.IS_INCLUDED)
					return TimeMLRelType.IS_INCLUDED;
				else if (label == TimeMLRelType.INCLUDES)
					return TimeMLRelType.INCLUDES;
				else if (label == TimeMLRelType.SIMULTANEOUS)
					return TimeMLRelType.SIMULTANEOUS;
				else 
					return TimeMLRelType.VAGUE;
			}
			
			@Override
			public String toString() {
				return "TBD";
			}
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			@Override
			public TimeMLRelType map(TimeMLRelType label) {
				if (label == null)
					return null;
				else if (label == TimeMLRelType.AFTER)
					return TimeMLRelType.AFTER;
				else if (label == TimeMLRelType.BEFORE)
					return TimeMLRelType.BEFORE;
				else if (label == TimeMLRelType.IS_INCLUDED)
					return TimeMLRelType.IS_INCLUDED;
				else if (label == TimeMLRelType.INCLUDES)
					return TimeMLRelType.INCLUDES;
				else if (label == TimeMLRelType.SIMULTANEOUS)
					return TimeMLRelType.SIMULTANEOUS;
				else if (label == TimeMLRelType.NONE_VAGUE)
					return null;
				else if (label == TimeMLRelType.PARTIAL_VAGUE)
					return null;
				else 
					return TimeMLRelType.VAGUE;
			}
			
			@Override
			public String toString() {
				return "TBDNoPVNV";
			}
		});
		
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.BEFORE) return label; else return null; }
			public String toString() { return "OnlyB"; }
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.AFTER) return label; else return null; }
			public String toString() { return "OnlyA"; }
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.INCLUDES) return label; else return null; }
			public String toString() { return "OnlyI"; }
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.IS_INCLUDED) return label; else return null; }
			public String toString() { return "OnlyII"; }
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.SIMULTANEOUS) return label; else return null; }
			public String toString() { return "OnlyS"; }
		});
		
		tools.addLabelMapping(new LabelMapping<TimeMLRelType>() {
			public TimeMLRelType map(TimeMLRelType label) { if (label == TimeMLRelType.VAGUE) return label; else return null; }
			public String toString() { return "OnlyV"; }
		});

		tools.addGenericDataSetBuilder(new DataSetBuilderEventTimeTLink());
		
		for (TimeMLRelType relType : TimeMLRelType.values())
			dataTools.addGenericWeightedStructure(new WeightedStructureRelationBinary(relType.toString(), (relType != TimeMLRelType.VAGUE && relType != TimeMLRelType.SIMULTANEOUS)));
		
		return tools;
	}
	
	public static Tools<String> getStringTools(DataTools dataTools) {
		Tools<String> tools =  new Tools<String>(dataTools) {
			@Override
			public String labelFromString(String str) {
				return str;
			}
		};
	
		return tools;
	}
	
	public static Tools<Boolean> getBooleanTools(DataTools dataTools) {
		Tools<Boolean> tools = new Tools<Boolean>(dataTools) {
			@Override
			public Boolean labelFromString(String str) {
				if (str == null)
					return null;
				return str.toLowerCase().equals("true") || str.equals("1");
			}
		};
	
		return tools;
	}
	
	public static abstract class Tools<L> extends EventDatumTools<EventTimeDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureEventMentionAttribute<EventTimeDatum<L>, L>());
			this.addGenericFeature(new FeatureTimeExpressionAttribute<EventTimeDatum<L>, L>());
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "Event";
				}
				
				@Override
				public TokenSpan[] extract(EventTimeDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getEvent().getSomeMentionCount()];
					for (int i = 0; i < datum.getEvent().getSomeMentionCount(); i++)
						spans[i] = datum.getEvent().getSomeMention(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "Time";
				}
				
				@Override
				public TokenSpan[] extract(EventTimeDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getTime().getSomeExpressionCount()];
					for (int i = 0; i < datum.getTime().getSomeExpressionCount(); i++)
						spans[i] = datum.getTime().getSomeExpression(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "EventExtent";
				}
				
				@Override
				public TokenSpan[] extract(EventTimeDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getEvent().getSomeMentionCount()];
					for (int i = 0; i < datum.getEvent().getSomeMentionCount(); i++)
						spans[i] = datum.getEvent().getSomeMention(i).getExtent();
					return spans;
				}
			});
		
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "BetweenEventTime";
				}
				
				@Override
				public TokenSpan[] extract(EventTimeDatum<L> datum) {
					
					List<TokenSpan> spans = new ArrayList<>();
					for (int i = 0; i < datum.getEvent().getSomeMentionCount(); i++) {
						TokenSpan eventSpan = datum.getEvent().getSomeMention(i).getTokenSpan();
						for (int j = 0; j < datum.getTime().getSomeExpressionCount(); j++) {
							TokenSpan timeSpan = datum.getTime().getSomeExpression(j).getTokenSpan();
							if (!eventSpan.getDocument().getName().equals(timeSpan.getDocument().getName())
									|| eventSpan.getSentenceIndex() != timeSpan.getSentenceIndex())
								continue;
							
							spans.add(new TokenSpan(
									eventSpan.getDocument(), 
									eventSpan.getSentenceIndex(),
									Math.min(eventSpan.getEndTokenIndex(), timeSpan.getEndTokenIndex()),
									Math.max(eventSpan.getStartTokenIndex(), timeSpan.getStartTokenIndex())
								));
						}
					}
					
					return spans.toArray(new TokenSpan[0]);
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "Event";
				}
				
				@Override
				public EventMention[] extract(EventTimeDatum<L> datum) {
					EventMention[] mentions = new EventMention[datum.getEvent().getSomeMentionCount()];
					for (int i = 0; i < datum.getEvent().getSomeMentionCount(); i++)
						mentions[i] = datum.getEvent().getSomeMention(i);
					return mentions;
				}
			});
			
			this.addTimeExpressionExtractor(new TimeExpressionExtractor<EventTimeDatum<L>, L>() {
				@Override
				public String toString() {
					return "Time";
				}
				
				@Override
				public LinkableTimeExpression[] extract(EventTimeDatum<L> datum) {
					LinkableTimeExpression[] expressions = new LinkableTimeExpression[datum.getTime().getSomeExpressionCount()];
					for (int i = 0; i < datum.getTime().getSomeExpressionCount(); i++)
						expressions[i] = datum.getTime().getSomeExpression(i);
					return expressions;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventTimeDatum<L>>() {
				public String toString() { return "SomeWithinSentenceOrDCT"; }
				public boolean indicator(EventTimeDatum<L> datum) { 					
					for (int i = 0; i < datum.getTime().getSomeExpressionCount(); i++) {
						if (datum.getTime().getSomeExpression(i).getTimeMLDocumentFunction() == TimeMLDocumentFunction.CREATION_TIME)
							return true;
						for (int j = 0; j < datum.getEvent().getSomeMentionCount(); j++) {
							LinkableTimeExpression time = datum.getTime().getSomeExpression(i);
							EventMention event = datum.getEvent().getSomeMention(j);
							
							if (time.getTokenSpan().getDocument().getName().equals(event.getTokenSpan().getDocument().getName())
									&& time.getTokenSpan().getSentenceIndex() == event.getTokenSpan().getSentenceIndex())
								return true;
						}
					}
					
					return false;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventTimeDatum<L>>() {
				public String toString() { return "SomeWithinSentence"; }
				public boolean indicator(EventTimeDatum<L> datum) { 					
					for (int i = 0; i < datum.getTime().getSomeExpressionCount(); i++) {
						for (int j = 0; j < datum.getEvent().getSomeMentionCount(); j++) {
							LinkableTimeExpression time = datum.getTime().getSomeExpression(i);
							EventMention event = datum.getEvent().getSomeMention(j);
							
							if (time.getTokenSpan().getDocument().getName().equals(event.getTokenSpan().getDocument().getName())
									&& time.getTokenSpan().getSentenceIndex() == event.getTokenSpan().getSentenceIndex())
								return true;
						}
					}
					
					return false;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventTimeDatum<L>>() {
				public String toString() { return "SomeDCT"; }
				public boolean indicator(EventTimeDatum<L> datum) { 					
					for (int i = 0; i < datum.getTime().getSomeExpressionCount(); i++) {
						if (datum.getTime().getSomeExpression(i).getTimeMLDocumentFunction() == TimeMLDocumentFunction.CREATION_TIME)
							return true;
					}
					
					return false;
				}
			});
			
			this.addGenericStructurizer(new StructurizerGraphEventTime<L>());
			this.addGenericStructurizer(new StructurizerGraphEventTimeByDocument<L>());
		}
		
		@Override
		public EventTimeDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				Event event = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("e")), true);
				LinkableNormalizedTimeValue time = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("t")), true);
				return new EventTimeDatum<L>(id, event, time, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(EventTimeDatum<L> datum) {
			JSONObject json = new JSONObject();
			
			try {
				json.put("id", String.valueOf(datum.id));
				if (datum.label != null)
					json.put("label", datum.label.toString());
				json.put("e", datum.event.getStoreReference().toJSON());
				json.put("t", datum.time.getStoreReference().toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> T makeBinaryDatum(
				EventTimeDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			EventTimeDatum<Boolean> binaryDatum = new EventTimeDatum<Boolean>(datum.getId(), datum.getEvent(), datum.getTime(), 
					(labelIndicator == null || datum.getLabel() == null) ? null : labelIndicator.indicator(datum.getLabel()));
			
			if (labelIndicator != null && datum.getLabel() != null) {
				double labelWeight = labelIndicator.weight(datum.getLabel());
				binaryDatum.setLabelWeight(true, labelWeight);
			}
			
			return (T)(binaryDatum);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> Datum.Tools<T, Boolean> makeBinaryDatumTools(
				LabelIndicator<L> labelIndicator) {
			OutputWriter genericOutput = this.dataTools.getOutputWriter();
			OutputWriter output = new OutputWriter(
					(genericOutput.getDebugFilePath() != null) ? new File(genericOutput.getDebugFilePath() + "." + labelIndicator.toString()) : null,
					(genericOutput.getResultsFilePath() != null) ? new File(genericOutput.getResultsFilePath() + "." + labelIndicator.toString()) : null,
					(genericOutput.getDataFilePath() != null) ? new File(genericOutput.getDataFilePath() + "." + labelIndicator.toString()) : null,
					(genericOutput.getModelFilePath() != null) ? new File(genericOutput.getModelFilePath() + "." + labelIndicator.toString()) : null
				);
			DataTools dataTools = this.dataTools.makeInstance(output);
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)EventTimeDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
