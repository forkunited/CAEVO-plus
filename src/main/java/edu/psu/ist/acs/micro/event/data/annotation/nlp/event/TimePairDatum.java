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
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTimeExpressionAttribute;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTimePairTLinkDet;

public class TimePairDatum<L> extends Datum<L> {
	private LinkableNormalizedTimeValue source;
	private LinkableNormalizedTimeValue target;

	public TimePairDatum(int id, LinkableNormalizedTimeValue source, LinkableNormalizedTimeValue target, L label) {
		this.id = id;
		this.source = source;
		this.target = target;
		this.label = label;
	}
	
	public LinkableNormalizedTimeValue getSource() {
		return this.source;
	}
	
	public LinkableNormalizedTimeValue getTarget() {
		return this.target;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.id).append(": ").append(this.source.getId()).append(" ").append(this.target.getId());
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
	
		tools.addGenericClassifyMethod(new MethodClassificationTimePairTLinkDet());
		
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

		tools.addGenericDataSetBuilder(new DataSetBuilderTimeTLink());
		
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
	
	public static abstract class Tools<L> extends Datum.Tools<TimePairDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureTimeExpressionAttribute<TimePairDatum<L>, L>());
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TimePairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public TokenSpan[] extract(TimePairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getSource().getSomeExpressionCount()];
					for (int i = 0; i < datum.getSource().getSomeExpressionCount(); i++)
						spans[i] = datum.getSource().getSomeExpression(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TimePairDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public TokenSpan[] extract(TimePairDatum<L> datum) {
					TokenSpan[] spans = new TokenSpan[datum.getTarget().getSomeExpressionCount()];
					for (int i = 0; i < datum.getTarget().getSomeExpressionCount(); i++)
						spans[i] = datum.getTarget().getSomeExpression(i).getTokenSpan();
					return spans;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TimePairDatum<L>, L>() {
				@Override
				public String toString() {
					return "BetweenSourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(TimePairDatum<L> datum) {
					
					List<TokenSpan> spans = new ArrayList<>();
					for (int i = 0; i < datum.getSource().getSomeExpressionCount(); i++) {
						TokenSpan sourceSpan = datum.getSource().getSomeExpression(i).getTokenSpan();
						for (int j = 0; j < datum.getTarget().getSomeExpressionCount(); j++) {
							TokenSpan targetSpan = datum.getSource().getSomeExpression(i).getTokenSpan();
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
			
			this.addGenericStructurizer(new StructurizerGraphTimePair<L>());
			this.addGenericStructurizer(new StructurizerGraphTimePairByDocument<L>());
		}
		
		@Override
		public TimePairDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				LinkableNormalizedTimeValue source = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("src")), true);
				LinkableNormalizedTimeValue target = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("tar")), true);
				return new TimePairDatum<L>(id, source, target, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(TimePairDatum<L> datum) {
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
				TimePairDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			TimePairDatum<Boolean> binaryDatum = new TimePairDatum<Boolean>(datum.getId(), datum.getSource(), datum.getTarget(), 
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
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)TimePairDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
