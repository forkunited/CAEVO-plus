package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.io.File;

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
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable.Type;
import edu.psu.ist.acs.micro.event.data.feature.FeatureEventMentionAttribute;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTLinkAttribute;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTLinkEventAttribute;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTLinkTimeAttribute;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTLinkTimeRelation;
import edu.psu.ist.acs.micro.event.data.feature.FeatureTLinkableType;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeAdjacentEventTime;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeGeneralGovernor;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeReichenbach;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeReportingDCT;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeReportingGovernor;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeTimeTime;
import edu.psu.ist.acs.micro.event.task.classify.MethodClassificationTLinkTypeWordNet;

public class TLinkDatum<L> extends Datum<L> {
	private TLink tlink;

	public TLinkDatum(int id, TLink tlink, L label) {
		this.id = id;
		this.tlink = tlink;
		this.label = label;
	}
	
	public TLink getTLink() {
		return this.tlink;
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.label)
		   .append(": ")
		   .append(this.tlink.getId())
		   .append(" ")
		   .append(this.tlink.getSource().getTokenSpan().toString())
		   .append(" ")
		   .append(this.tlink.getTarget().getTokenSpan().toString())
		   .append(" ").append(
				this.tlink.getSource().getTokenSpan().getDocument().getSentence(
						this.tlink.getSource().getTokenSpan().getSentenceIndex()));
		
		return str.toString();
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
		Tools<Boolean> tools =  new Tools<Boolean>(dataTools) {
			@Override
			public Boolean labelFromString(String str) {
				if (str == null)
					return null;
				return str.toLowerCase().equals("true") || str.equals("1");
			}
		};
	
		return tools;
	}
	
	public static Tools<TimeMLRelType> getTimeMLRelTypeTools(DataTools dataTools) {
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
	
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeAdjacentEventTime());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeGeneralGovernor());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeReichenbach());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeReportingGovernor());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeTimeTime());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeWordNet());
		tools.addGenericClassifyMethod(new MethodClassificationTLinkTypeReportingDCT());
		
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
				else if (label == TimeMLRelType.VAGUE)
					return TimeMLRelType.VAGUE;
				else 
					return null;
			}
			
			@Override
			public String toString() {
				return "OnlyTBD";
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

		tools.addGenericDataSetBuilder(new DataSetBuilderTLinkType());
		
		for (TimeMLRelType relType : TimeMLRelType.values())
			dataTools.addGenericWeightedStructure(new WeightedStructureRelationBinary(relType.toString(), (relType != TimeMLRelType.VAGUE && relType != TimeMLRelType.SIMULTANEOUS)));
		
		return tools;
	}
	
	public static abstract class Tools<L> extends EventDatumTools<TLinkDatum<L>, L> { 
		public Tools(DataTools dataTools) {
			super(dataTools);
			
			this.addGenericFeature(new FeatureTLinkableType<L>());
			this.addGenericFeature(new FeatureTLinkAttribute<L>());
			this.addGenericFeature(new FeatureTLinkEventAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeAttribute<L>());
			this.addGenericFeature(new FeatureTLinkTimeRelation<L>());
			this.addGenericFeature(new FeatureEventMentionAttribute<TLinkDatum<L>, L>());
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "First";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getFirst().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstHead";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					TokenSpan span = tlinkDatum.getTLink().getFirst().getTokenSpan();
					return new TokenSpan[] {span.getSubspan(span.getLength() - 1, span.getLength()) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstTail";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getFirst().getTokenSpan().getSubspan(0, 1) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "Second";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getSecond().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SecondHead";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					TokenSpan span = tlinkDatum.getTLink().getSecond().getTokenSpan();
					return new TokenSpan[] {span.getSubspan(span.getLength() - 1, span.getLength()) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SecondTail";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getSecond().getTokenSpan().getSubspan(0, 1) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceHead";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					TokenSpan span = tlinkDatum.getTLink().getSource().getTokenSpan();
					return new TokenSpan[] {span.getSubspan(span.getLength() - 1, span.getLength()) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTail";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan().getSubspan(0, 1) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan() } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "TargetHead";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					TokenSpan span = tlinkDatum.getTLink().getTarget().getTokenSpan();
					return new TokenSpan[] {span.getSubspan(span.getLength() - 1, span.getLength()) } ;
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "TargetTail";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan().getSubspan(0, 1) } ;
				}
			});
			
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] {  tlinkDatum.getTLink().getSource().getTokenSpan(),
							tlinkDatum.getTLink().getTarget().getTokenSpan() };
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTargetTails";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					return new TokenSpan[] {  tlinkDatum.getTLink().getSource().getTokenSpan().getSubspan(0, 1),
							tlinkDatum.getTLink().getTarget().getTokenSpan().getSubspan(0, 1) };
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "BetweenSourceTarget";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					TokenSpan source = tlinkDatum.getTLink().getSource().getTokenSpan();
					TokenSpan target = tlinkDatum.getTLink().getTarget().getTokenSpan();
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
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstEvent";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.EVENT)
						return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan() };
					else if (tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.EVENT)
						return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan() };
					else
						return new TokenSpan[0];
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstTime";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.TIME)
						return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan() };
					else if (tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.TIME)
						return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan() };
					else
						return new TokenSpan[0];
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstTimeHead";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.TIME) {
						TokenSpan span = tlinkDatum.getTLink().getSource().getTokenSpan();
						return new TokenSpan[] { span.getSubspan(span.getLength() - 1, span.getLength()) };
					} else if (tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.TIME) {
						TokenSpan span = tlinkDatum.getTLink().getTarget().getTokenSpan();
						return new TokenSpan[] { span.getSubspan(span.getLength() - 1, span.getLength()) };
					} else
						return new TokenSpan[0];
				}
			});
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstTimeTail";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.TIME) {
						TokenSpan span = tlinkDatum.getTLink().getSource().getTokenSpan();
						return new TokenSpan[] { span.getSubspan(0, 1) };
					} else if (tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.TIME) {
						TokenSpan span = tlinkDatum.getTLink().getTarget().getTokenSpan();
						return new TokenSpan[] { span.getSubspan(0, 1) };
					} else
						return new TokenSpan[0];
				}
			});
			
			
			this.addTokenSpanExtractor(new TokenSpanExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "EventTime";
				}
				
				@Override
				public TokenSpan[] extract(TLinkDatum<L> tlinkDatum) {
					if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.EVENT && tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.TIME)
						return new TokenSpan[] { tlinkDatum.getTLink().getSource().getTokenSpan(), tlinkDatum.getTLink().getTarget().getTokenSpan() };
					else if (tlinkDatum.getTLink().getSource().getTLinkableType() == Type.TIME && tlinkDatum.getTLink().getTarget().getTLinkableType() == Type.EVENT)
						return new TokenSpan[] { tlinkDatum.getTLink().getTarget().getTokenSpan(), tlinkDatum.getTLink().getSource().getTokenSpan() };
					else
						return new TokenSpan[0];
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "Source";
				}
				
				@Override
				public EventMention[] extract(TLinkDatum<L> datum) {
					return new EventMention[] { (EventMention)datum.getTLink().getSource() };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "Target";
				}
				
				@Override
				public EventMention[] extract(TLinkDatum<L> datum) {
					return new EventMention[] { (EventMention)datum.getTLink().getTarget() };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "SourceTarget";
				}
				
				@Override
				public EventMention[] extract(TLinkDatum<L> datum) {
					return new EventMention[] { (EventMention)datum.getTLink().getSource(), (EventMention)datum.getTLink().getTarget() };
				}
			});
			
			this.addEventMentionExtractor(new EventMentionExtractor<TLinkDatum<L>, L>() {
				@Override
				public String toString() {
					return "FirstEvent";
				}
				
				@Override
				public EventMention[] extract(TLinkDatum<L> datum) {
					if (datum.getTLink().getSource().getTLinkableType() == Type.EVENT)
						return new EventMention[] { (EventMention)datum.getTLink().getSource() };
					else
						return new EventMention[] { (EventMention)datum.getTLink().getTarget() };
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "TypeEventEvent"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getType() == TLink.Type.EVENT_EVENT; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "TypeEventTime"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getType() == TLink.Type.EVENT_TIME; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "TypeTimeTime"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getType() == TLink.Type.TIME_TIME; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionDCT"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.DCT; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionWithinSentence"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.WITHIN_SENTENCE; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionWithinSentenceDominant"; }
				public boolean indicator(TLinkDatum<L> datum) { 
					if (datum.getTLink().getPosition() != TLink.Position.WITHIN_SENTENCE)
						return false;
					
					TLink link = datum.getTLink();
					DocumentNLP document = link.getSource().getTokenSpan().getDocument();
					ConstituencyParse parse = document.getConstituencyParse(link.getSource().getTokenSpan().getSentenceIndex());
					if (parse == null)
						return false;
					
					return parse.getRelation(link.getSource().getTokenSpan(), link.getTarget().getTokenSpan()) != ConstituencyParse.Relation.NONE;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionWithinSentenceNotDominant"; }
				public boolean indicator(TLinkDatum<L> datum) { 
					if (datum.getTLink().getPosition() != TLink.Position.WITHIN_SENTENCE)
						return false;
					
					TLink link = datum.getTLink();
					DocumentNLP document = link.getSource().getTokenSpan().getDocument();
					ConstituencyParse parse = document.getConstituencyParse(link.getSource().getTokenSpan().getSentenceIndex());
					if (parse == null)
						return false;
					
					return parse.getRelation(link.getSource().getTokenSpan(), link.getTarget().getTokenSpan()) == ConstituencyParse.Relation.NONE;
				}
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionBetweenSentence"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.BETWEEN_SENTENCE; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionDCTDCT"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.DCT_DCT; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionDCTBetweenDocument"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.DCT_BETWEEN_DOCUMENT; }
			});
			
			this.addDatumIndicator(new DatumIndicator<TLinkDatum<L>>() {
				public String toString() { return "PositionBetweenDocument"; }
				public boolean indicator(TLinkDatum<L> datum) { return datum.getTLink().getPosition() == TLink.Position.BETWEEN_DOCUMENT; }
			});
			
			this.addGenericStructurizer(new StructurizerGraphTLink<L>());
			this.addGenericStructurizer(new StructurizerGraphTLinkByDocument<L>());
		}
		
		@Override
		public TLinkDatum<L> datumFromJSON(JSONObject json) {
			try {
				int id = Integer.valueOf(json.getString("id"));
				
				L label = (json.has("label")) ? labelFromString(json.getString("label")) : null;
				TLink tlink = this.dataTools.getStoredItemSetManager()
						.resolveStoreReference(StoreReference.makeFromJSON(json.getJSONObject("tlink")), true);
				return new TLinkDatum<L>(id, tlink, label);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		public JSONObject datumToJSON(TLinkDatum<L> datum) {
			JSONObject json = new JSONObject();
			
			try {
				json.put("id", String.valueOf(datum.id));
				if (datum.label != null)
					json.put("label", datum.label.toString());
				json.put("tlink", datum.tlink.getStoreReference().toJSON());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return json;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T extends Datum<Boolean>> T makeBinaryDatum(
				TLinkDatum<L> datum,
				LabelIndicator<L> labelIndicator) {
			
			TLinkDatum<Boolean> binaryDatum = new TLinkDatum<Boolean>(datum.getId(), datum.getTLink(), 
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
			Datum.Tools<T, Boolean> binaryTools = (Datum.Tools<T, Boolean>)TLinkDatum.getBooleanTools(dataTools);
			
			return binaryTools;
			
		}
	}
}
