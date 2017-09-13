package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.feature.FeatureEventMentionAttribute;

public class EventMentionDatum<L> extends Datum<L> {
	private EventMention mention;

	public EventMentionDatum(int id, EventMention mention, L label) {
		this.id = id;
		this.mention = mention;
		this.label = label;
	}
	
	public EventMention getMention() {
		return this.mention;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.id).append(": ").append(this.mention.getId());
		return str.toString();
	}
	
	public static Tools<String> getStringTools(DataTools dataTools) {
		Tools<String> tools =  new Tools<String>(dataTools) {
			@Override
			public String labelFromString(String str) {
				return str;
			}
		};
		
		tools.addGenericDataSetBuilder(new DataSetBuilderEventMentionAttribute());
	
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
	
	public static abstract class Tools<L> extends EventDatumTools<EventMentionDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureEventMentionAttribute<EventMentionDatum<L>, L>());
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<EventMentionDatum<L>, L>() {
				@Override
				public String toString() {
					return "Mention";
				}
				
				@Override
				public TokenSpan[] extract(EventMentionDatum<L> datum) {
					return new TokenSpan[] { datum.getMention().getTokenSpan() } ;
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<EventMentionDatum<L>, L>() {
				@Override
				public String toString() {
					return "Mention";
				}
				
				@Override
				public EventMention[] extract(EventMentionDatum<L> datum) {
					return new EventMention[] { datum.getMention() };
				}
			});
		}
		
		@Override
		public EventMentionDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				EventMention m = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("m")), true);
				return new EventMentionDatum<L>(id, m, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(EventMentionDatum<L> datum) {
			JSONObject json = new JSONObject();
			
			try {
				json.put("id", String.valueOf(datum.id));
				if (datum.label != null)
					json.put("label", datum.label.toString());
				json.put("m", datum.mention.getStoreReference().toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> T makeBinaryDatum(
				EventMentionDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			EventMentionDatum<Boolean> binaryDatum = new EventMentionDatum<Boolean>(datum.getId(), datum.getMention(), 
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
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)EventMentionDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
