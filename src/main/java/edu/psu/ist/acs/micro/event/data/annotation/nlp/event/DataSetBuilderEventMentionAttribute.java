package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.Arrays;
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
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.generic.util.ThreadMapper.Fn;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.Attribute;

public class DataSetBuilderEventMentionAttribute extends DataSetBuilderDocumentFiltered<EventMentionDatum<String>, String> {	
	private Attribute attribute;
	private String[] parameterNames = { "attribute" };
	
	public DataSetBuilderEventMentionAttribute() {
		this(null);
	}
	
	public DataSetBuilderEventMentionAttribute(DatumContext<EventMentionDatum<String>, String> context) {
		this.context = context;
	}
	
	@Override
	public String[] getParameterNames() {
		String[] parentParameterNames = super.getParameterNames();
		String[] parameterNames = Arrays.copyOf(this.parameterNames, this.parameterNames.length + parentParameterNames.length);
		for (int i = 0; i < parentParameterNames.length; i++)
			parameterNames[this.parameterNames.length + i] = parentParameterNames[i];
		return parameterNames;
	}

	@Override
	public Obj getParameterValue(String parameter) {
		if (parameter.equals("attribute"))
			return (this.attribute == null) ? null : Obj.stringValue(this.attribute.toString());
		else
			return super.getParameterValue(parameter);
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("attribute"))
			this.attribute = (parameterValue == null) ? null : Attribute.valueOf(this.context.getMatchValue(parameterValue));
		else
			return super.setParameterValue(parameter, parameterValue);
		return true;
	}
	
	@Override
	public DataSetBuilder<EventMentionDatum<String>, String> makeInstance(
			DatumContext<EventMentionDatum<String>, String> context) {
		return new DataSetBuilderEventMentionAttribute(context);
	}

	@Override
	public DataSet<EventMentionDatum<String>, String> build() {
		DataSet<EventMentionDatum<String>, String> data = new DataSet<EventMentionDatum<String>, String>(this.context.getDatumTools());

		Map<String, Set<String>> documentClusters = getDocumentClusters();
		DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> docs = getDocuments();
		TreeMap<String, Pair<EventMention, String>> datums = new TreeMap<>();
		ThreadMapper<Entry<String, Set<String>>, Boolean> threads = new ThreadMapper<Entry<String, Set<String>>, Boolean>(
			new Fn<Entry<String, Set<String>>, Boolean>() {
				@Override
				public Boolean apply(Entry<String, Set<String>> item) {
					for (String docName1 : item.getValue()) {
						DocumentNLP doc1 = docs.getDocumentByName(docName1, true);
						List<Pair<TokenSpan, EventMention>> mentions = doc1.getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION);
						for (Pair<TokenSpan, EventMention> mentionPair : mentions) {
							EventMention mention = mentionPair.getSecond();
							
							synchronized (datums) {
								datums.put(mention.getId(), 
										new Pair<EventMention, String>(mention, mention.getAttributeString(attribute)));
							}
						}
					}
					
					return true;
				}
			});
		
		threads.run(documentClusters.entrySet(), this.context.getMaxThreads());
		
		Pair<Integer, Integer> idRange = this.context.getDataTools().getIncrementIdRange(datums.size());
		int i = idRange.getFirst();
		for (Entry<String, Pair<EventMention, String>> entry : datums.entrySet()) {
			data.add(new EventMentionDatum<String>(i, entry.getValue().getFirst(), entry.getValue().getSecond()));
			i++;
		}
				
		return data;
	}

	@Override
	public String getGenericName() {
		return "EventMentionAttribute";
	}

}
