package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.LabelMapping;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.ConstituencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.feature.FeatureEventMentionAttribute;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationEventPairCorefDet;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationEventPairTLinkDet;

public class EventPairDatum<L> extends Datum<L> {
	private Event source;
	private Event target;

	public EventPairDatum(int id, Event source, Event target, L label) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.label = label;
	}
	
	public Event getSource() {
		return this.source;
	}
	
	public Event getTarget() {
		return this.target;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.id).append(": ").append(this.source.getId()).append(" ").append(this.target.getId());
		return str.toString();
	}
	
	public static Tools<CorefRelType> getCorefRelTypeTools(DataTools dataTools) {
		Tools<CorefRelType> tools =  new Tools<CorefRelType>(dataTools) {
			@Override
			public CorefRelType labelFromString(String str) {
				if (str == null)
					return null;
				try {
					return CorefRelType.valueOf(str);
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		};
	
		for (CorefRelType relType : CorefRelType.values())
			dataTools.addGenericWeightedStructure(new WeightedStructureRelationBinary(relType.toString(), false));
		
		tools.addGenericClassifyMethod(new MethodClassificationEventPairCorefDet());
		
		tools.addGenericDataSetBuilder(new DataSetBuilderEventCoref());
		
		tools.addLabelMapping(new LabelMapping<CorefRelType>() {
			public CorefRelType map(CorefRelType label) { if (label == CorefRelType.COREF) return label; else return null; }
			public String toString() { return "OnlyCoref"; }
		});
		
		return tools;
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
	
		tools.addGenericClassifyMethod(new MethodClassificationEventPairTLinkDet());
		
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

		tools.addGenericDataSetBuilder(new DataSetBuilderEventTLink());
		
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
	
	public static abstract class Tools<L> extends EventDatumTools<EventPairDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureEventMentionAttribute<EventPairDatum<L>, L>());
		
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public TokenSpan[] extract(EventPairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getSource().getSomeMentionCount()];
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++)
						spans[i] = datum.getSource().getSomeMention(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public TokenSpan[] extract(EventPairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getTarget().getSomeMentionCount()];
					for (int i = 0; i < datum.getTarget().getSomeMentionCount(); i++)
						spans[i] = datum.getTarget().getSomeMention(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceExtent";
				}
				
				@Override
				public TokenSpan[] extract(EventPairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getSource().getSomeMentionCount()];
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++)
						spans[i] = datum.getSource().getSomeMention(i).getExtent();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "TargetExtent";
				}
				
				@Override
				public TokenSpan[] extract(EventPairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getTarget().getSomeMentionCount()];
					for (int i = 0; i < datum.getTarget().getSomeMentionCount(); i++)
						spans[i] = datum.getTarget().getSomeMention(i).getExtent();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "BetweenSourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(EventPairDatum<L> datum) {
					
					List<TokenSpan> spans = new ArrayList<>();
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++) {
						TokenSpan sourceSpan = datum.getSource().getSomeMention(i).getTokenSpan();
						for (int j = 0; j < datum.getTarget().getSomeMentionCount(); j++) {
							TokenSpan targetSpan = datum.getSource().getSomeMention(j).getTokenSpan();
							if (!sourceSpan.getDocument().getName().equals(targetSpan.getDocument().getName())
									|| sourceSpan.getSentenceIndex() != targetSpan.getSentenceIndex())
								continue;
							
							spans.add(new TokenSpan(
									sourceSpan.getDocument(), 
									sourceSpan.getSentenceIndex(),
									Math.min(sourceSpan.getEndTokenIndex(), targetSpan.getEndTokenIndex()),
									Math.max(sourceSpan.getStartTokenIndex(), targetSpan.getStartTokenIndex())
								));
						}
					}
					
					return spans.toArray(new TokenSpan[0]);
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public EventMention[] extract(EventPairDatum<L> datum) {
					EventMention[] mentions = new EventMention[datum.getSource().getSomeMentionCount()];
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++)
						mentions[i] = datum.getSource().getSomeMention(i);
					return mentions;
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public EventMention[] extract(EventPairDatum<L> datum) {
					EventMention[] mentions = new EventMention[datum.getTarget().getSomeMentionCount()];
					for (int i = 0; i < datum.getTarget().getSomeMentionCount(); i++)
						mentions[i] = datum.getTarget().getSomeMention(i);
					return mentions;
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTarget";
				}
				
				@Override
				public EventMention[] extract(EventPairDatum<L> datum) {
					EventMention[] mentions = new EventMention[datum.getSource().getSomeMentionCount() + datum.getTarget().getSomeMentionCount()];
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++)
						mentions[i] = datum.getSource().getSomeMention(i);
					for (int i = 0; i < datum.getTarget().getSomeMentionCount(); i++)
						mentions[i + datum.getSource().getSomeMentionCount()] = datum.getTarget().getSomeMention(i);
					return mentions;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventPairDatum<L>>() {
				public String toString() { return "SomeWithinSentence"; }
				public boolean indicator(EventPairDatum<L> datum) { 
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++) {
						for (int j = 0; j < datum.getTarget().getSomeMentionCount(); j++) {
							EventMention source = datum.getSource().getSomeMention(i);
							EventMention target = datum.getTarget().getSomeMention(j);
							
							if (source.getTokenSpan().getDocument().getName().equals(target.getTokenSpan().getDocument().getName())
									&& source.getTokenSpan().getSentenceIndex() == target.getTokenSpan().getSentenceIndex())
								return true;
						}
					}
					
					return false;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventPairDatum<L>>() {
				public String toString() { return "SomeWithinSentenceDominant"; }
				public boolean indicator(EventPairDatum<L> datum) { 
					for (int i = 0; i < datum.getSource().getSomeMentionCount(); i++) {
						for (int j = 0; j < datum.getTarget().getSomeMentionCount(); j++) {
							EventMention source = datum.getSource().getSomeMention(i);
							EventMention target = datum.getTarget().getSomeMention(j);
							TokenSpan sourceSpan = source.getTokenSpan();
							TokenSpan targetSpan = target.getTokenSpan();
							
							if (sourceSpan.getDocument().getName().equals(targetSpan.getDocument().getName())
									&& sourceSpan.getSentenceIndex() == targetSpan.getSentenceIndex()) {
								DocumentNLP document = sourceSpan.getDocument();
								ConstituencyParse parse = document.getConstituencyParse(sourceSpan.getSentenceIndex());
								if (parse != null && parse.getRelation(sourceSpan, targetSpan) != ConstituencyParse.Relation.NONE)
									return true;
							}
						}
					}
					
					return false;					
				}
			});
			
			this.addGenericStructurizer(new StructurizerGraphEventPair<L>());
			this.addGenericStructurizer(new StructurizerGraphEventPairByDocument<L>());
		}
		
		@Override
		public EventPairDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				Event source = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("src")), true);
				Event target = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("tar")), true);
				return new EventPairDatum<L>(id, source, target, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(EventPairDatum<L> datum) {
			JSONObject json = new JSONObject();
			
			try {
				json.put("id", String.valueOf(datum.id));
				if (datum.label != null)
					json.put("label", datum.label.toString());
				json.put("src", datum.source.getStoreReference().toJSON());
				json.put("tar", datum.target.getStoreReference().toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> T makeBinaryDatum(
				EventPairDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			EventPairDatum<Boolean> binaryDatum = new EventPairDatum<Boolean>(datum.getId(), datum.getSource(), datum.getTarget(), 
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
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)EventPairDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
