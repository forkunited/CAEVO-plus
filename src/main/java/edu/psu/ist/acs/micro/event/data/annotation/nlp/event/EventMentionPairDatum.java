package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.ConstituencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.feature.FeatureEventMentionAttribute;

public class EventMentionPairDatum<L> extends Datum<L> {
	private EventMention sourceMention;
	private EventMention targetMention;

	public EventMentionPairDatum(int id, EventMention sourceMention, EventMention targetMention, L label) {
		this.id = id;
		this.sourceMention = sourceMention;
		this.targetMention = targetMention;
		this.label = label;
	}
	
	public EventMention getSourceMention() {
		return this.sourceMention;
	}
	
	public EventMention getTargetMention() {
		return this.targetMention;
	}
	
	public boolean isWithinDocument() {
		return this.sourceMention.getTokenSpan().getDocument().getName().equals(this.targetMention.getTokenSpan().getDocument().getName());
	}
	
	public boolean isWithinSentence() {
		return isWithinDocument() && 
				this.sourceMention.getTokenSpan().getSentenceIndex() ==  this.targetMention.getTokenSpan().getSentenceIndex();
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.id).append(": ").append(this.sourceMention.getId()).append(" ").append(this.targetMention.getId());
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
		
		tools.addGenericDataSetBuilder(new DataSetBuilderEventMentionCoref());
		
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
	
	public static abstract class Tools<L> extends EventDatumTools<EventMentionPairDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureEventMentionAttribute<EventMentionPairDatum<L>, L>());
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionPairDatum<L> datum) {
					return new TokenSpan[] { datum.getSourceMention().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionPairDatum<L> datum) {
					return new TokenSpan[] { datum.getTargetMention().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionPairDatum<L> datum) {
					return new TokenSpan[] {  datum.getSourceMention().getTokenSpan(),
							datum.getTargetMention().getTokenSpan() };
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "BetweenSourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionPairDatum<L> datum) {
					TokenSpan source = datum.getSourceMention().getTokenSpan();
					TokenSpan target = datum.getTargetMention().getTokenSpan();
					if (!source.getDocument().getName().equals(target.getDocument().getName())
							|| source.getSentenceIndex() != target.getSentenceIndex())
						return new TokenSpan[0];
					
					
					return new TokenSpan[] { 
							new TokenSpan(
								source.getDocument(), 
								source.getSentenceIndex(),
								Math.min(source.getEndTokenIndex(), target.getEndTokenIndex()),
								Math.max(source.getStartTokenIndex(), target.getStartTokenIndex())
							) 
					};
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTargetExtent";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionPairDatum<L> datum) {
					TokenSpan sourceExtent = datum.getSourceMention().getExtent();
					if (sourceExtent == null)
						sourceExtent = datum.getSourceMention().getTokenSpan();
					TokenSpan targetExtent = datum.getTargetMention().getExtent();
					if (targetExtent == null)
						targetExtent = datum.getTargetMention().getTokenSpan();
					
					return new TokenSpan[] {  sourceExtent,
							targetExtent };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public EventMention[] extract(EventMentionPairDatum<L> datum) {
					return new EventMention[] { datum.getSourceMention() };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public EventMention[] extract(EventMentionPairDatum<L> datum) {
					return new EventMention[] { datum.getTargetMention() };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventMentionPairDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTarget";
				}
				
				@Override
				public EventMention[] extract(EventMentionPairDatum<L> datum) {
					return new EventMention[] { datum.getSourceMention(), datum.getTargetMention() };
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventMentionPairDatum<L>>() {
				public String toString() { return "PositionWithinSentence"; }
				public boolean indicator(EventMentionPairDatum<L> datum) { 
					return datum.isWithinSentence();
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventMentionPairDatum<L>>() {
				public String toString() { return "PositionWithinSentenceDominant"; }
				public boolean indicator(EventMentionPairDatum<L> datum) { 
					if (datum.isWithinSentence())
						return false;
					
					DocumentNLP document = datum.getSourceMention().getTokenSpan().getDocument();
					ConstituencyParse parse = document.getConstituencyParse(datum.getSourceMention().getTokenSpan().getSentenceIndex());
					if (parse == null)
						return false;
					
					return parse.getRelation(datum.getSourceMention().getTokenSpan(), datum.getTargetMention().getTokenSpan()) != ConstituencyParse.Relation.NONE;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<EventMentionPairDatum<L>>() {
				public String toString() { return "PositionBetweenSentence"; }
				public boolean indicator(EventMentionPairDatum<L> datum) { return datum.isWithinDocument() && !datum.isWithinSentence(); }
			});
				
			this.addDatumIndicator(new DatumIndicator<EventMentionPairDatum<L>>() {
				public String toString() { return "PositionBetweenDocument"; }
				public boolean indicator(EventMentionPairDatum<L> datum) { return !datum.isWithinDocument(); }
			});
			
			this.addGenericStructurizer(new StructurizerGraphEventMentionPair<L>());
		}
		
		@Override
		public EventMentionPairDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				EventMention sourceMention = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("src")), true);
				EventMention targetMention = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("tar")), true);
				return new EventMentionPairDatum<L>(id, sourceMention, targetMention, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(EventMentionPairDatum<L> datum) {
			JSONObject json = new JSONObject();
			
			try {
				json.put("id", String.valueOf(datum.id));
				if (datum.label != null)
					json.put("label", datum.label.toString());
				json.put("src", datum.sourceMention.getStoreReference().toJSON());
				json.put("tar", datum.targetMention.getStoreReference().toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> T makeBinaryDatum(
				EventMentionPairDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			EventMentionPairDatum<Boolean> binaryDatum = new EventMentionPairDatum<Boolean>(datum.getId(), datum.getSourceMention(), datum.getTargetMention(), 
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
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)EventMentionPairDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
