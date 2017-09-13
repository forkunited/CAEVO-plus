package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.DocumentSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.DataSetBuilder;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.generic.util.ThreadMapper.Fn;
import edu.cmu.ml.rtw.generic.util.Triple;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;

// FIXME This needs to be updated to handle ALL_AS_UNLABELED label mode
public class DataSetBuilderEventMentionCoref extends DataSetBuilderDocumentFiltered<EventMentionPairDatum<CorefRelType>, CorefRelType> {
	//private String[] parameterNames = {  };
	
	public DataSetBuilderEventMentionCoref() {
		this(null);
	}
	
	public DataSetBuilderEventMentionCoref(DatumContext<EventMentionPairDatum<CorefRelType>, CorefRelType> context) {
		this.context = context;
	}
	
	@Override
	public DataSetBuilder<EventMentionPairDatum<CorefRelType>, CorefRelType> makeInstance(
			DatumContext<EventMentionPairDatum<CorefRelType>, CorefRelType> context) {
		return new DataSetBuilderEventMentionCoref(context);
	}

	@Override
	public DataSet<EventMentionPairDatum<CorefRelType>, CorefRelType> build() {
		DataSet<EventMentionPairDatum<CorefRelType>, CorefRelType> data = new DataSet<EventMentionPairDatum<CorefRelType>, CorefRelType>(this.context.getDatumTools());

		PairFn<EventMention, Triple<EventMention, EventMention, CorefRelType>> fn = new PairFn<EventMention, Triple<EventMention, EventMention, CorefRelType>>() {
			@Override
			public Triple<EventMention, EventMention, CorefRelType> apply(EventMention o1, EventMention o2) {
				Event event1 = o1.getEvent();
				Event event2 = o2.getEvent();
				
				if (event1 == null || event2 == null) {
					if (labelMode == LabelMode.ONLY_LABELED)
						return null;
					return new Triple<EventMention, EventMention, CorefRelType>(o1, o2, null);
				} else {
					if (labelMode == LabelMode.ONLY_UNLABELED)
						return null;
					return new Triple<EventMention, EventMention, CorefRelType>(o1, o2, 
							(event1.getId().equals(event2.getId()) ? CorefRelType.COREF : CorefRelType.NOT_COREF));
				}
			}
		};

		Map<String, Set<String>> documentClusters = getDocumentClusters();
		DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> docs = getDocuments();
		TreeMap<String, Triple<EventMention, EventMention, CorefRelType>> datums = new TreeMap<>();
		ThreadMapper<Entry<String, Set<String>>, Boolean> threads = new ThreadMapper<Entry<String, Set<String>>, Boolean>(
			new Fn<Entry<String, Set<String>>, Boolean>() {
				@Override
				public Boolean apply(Entry<String, Set<String>> item) {
					Set<String> doneDocs = new HashSet<String>();
					List<Triple<EventMention, EventMention, CorefRelType>> cDatums = new ArrayList<>();
					
					for (String docName1 : item.getValue()) {
						DocumentNLP doc1 = docs.getDocumentByName(docName1, true);
						
						List<Pair<TokenSpan, EventMention>> spanMentions1 = doc1.getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION);
						List<EventMention> mentions1 = new ArrayList<>();
						for (Pair<TokenSpan, EventMention> spanMention : spanMentions1) {
							mentions1.add(spanMention.getSecond());
						}
						
						cDatums = runAllPairs(mentions1, fn, cDatums, false);
						
						doneDocs.add(docName1);
						for (String docName2 : item.getValue()) {
							if (doneDocs.contains(docName2))
								continue;
							
							DocumentNLP doc2 = docs.getDocumentByName(docName2, true);
							List<Pair<TokenSpan, EventMention>> spanMentions2 = doc2.getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION);
							List<EventMention> mentions2 = new ArrayList<>();
							for (Pair<TokenSpan, EventMention> spanMention : spanMentions2) {
								mentions2.add(spanMention.getSecond());
							}
							
							cDatums = runAllPairs(mentions1, mentions2, fn, cDatums); 
						}
					}
					
					synchronized (datums) {
						for (Triple<EventMention, EventMention, CorefRelType> datum : cDatums)
							datums.put(datum.getFirst().getId() + "_" + datum.getSecond().getId(), datum);
					}
					
					return true;
				}
			});
		
		threads.run(documentClusters.entrySet(), this.context.getMaxThreads());
		
		Pair<Integer, Integer> idRange = this.context.getDataTools().getIncrementIdRange(datums.size());
		int i = idRange.getFirst();
		for (Entry<String, Triple<EventMention, EventMention, CorefRelType>> entry : datums.entrySet()) {
			data.add(new EventMentionPairDatum<CorefRelType>(i, entry.getValue().getFirst(), entry.getValue().getSecond(), entry.getValue().getThird()));
			i++;
		}
				
		return data;
	}

	@Override
	public String getGenericName() {
		return "EventMentionCoref";
	}

}
