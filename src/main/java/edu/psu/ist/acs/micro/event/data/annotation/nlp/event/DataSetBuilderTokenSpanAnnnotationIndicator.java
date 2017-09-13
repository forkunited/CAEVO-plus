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
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpansDatum;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.generic.util.ThreadMapper.Fn;

public class DataSetBuilderTokenSpanAnnnotationIndicator extends DataSetBuilderDocumentFiltered<TokenSpansDatum<Boolean>, Boolean> {
	private AnnotationTypeNLP<?> annotationType;
	private String[] parameterNames = { "annotationType" };
	
	public DataSetBuilderTokenSpanAnnnotationIndicator() {
		this(null);
	}
	
	public DataSetBuilderTokenSpanAnnnotationIndicator(DatumContext<TokenSpansDatum<Boolean>, Boolean> context) {
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
		if (parameter.equals("annotationType"))
			return (this.annotationType == null) ? null : Obj.stringValue(this.annotationType.getType());
		else
			return super.getParameterValue(parameter);
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("annotationType"))
			this.annotationType = (parameterValue == null) ? null : this.context.getDataTools().getAnnotationTypeNLP(this.context.getMatchValue(parameterValue));
		else
			return super.setParameterValue(parameter, parameterValue);
		return true;
	}
	
	@Override
	public DataSetBuilder<TokenSpansDatum<Boolean>, Boolean> makeInstance(
			DatumContext<TokenSpansDatum<Boolean>, Boolean> context) {
		return new DataSetBuilderTokenSpanAnnnotationIndicator(context);
	}

	@Override
	public DataSet<TokenSpansDatum<Boolean>, Boolean> build() {
		DataSet<TokenSpansDatum<Boolean>, Boolean> data = new DataSet<TokenSpansDatum<Boolean>, Boolean>(this.context.getDatumTools());

		Map<String, Set<String>> documentClusters = getDocumentClusters();
		DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> docs = getDocuments();
		TreeMap<String, Pair<TokenSpan, Boolean>> datums = new TreeMap<>();
		ThreadMapper<Entry<String, Set<String>>, Boolean> threads = new ThreadMapper<Entry<String, Set<String>>, Boolean>(
			new Fn<Entry<String, Set<String>>, Boolean>() {
				@Override
				public Boolean apply(Entry<String, Set<String>> item) {
					for (String docName1 : item.getValue()) {
						DocumentNLP doc1 = docs.getDocumentByName(docName1, true);
						
						for (int i = 0; i < doc1.getSentenceCount(); i++) {
							for (int j = 0; j < doc1.getSentenceTokenCount(i); j++) {
								TokenSpan span = new TokenSpan(doc1, i, j, j+1);
								@SuppressWarnings("rawtypes")
								List mentions = (List)doc1.getTokenSpanAnnotations(annotationType, span);
							
								synchronized (datums) {
									datums.put(doc1.getName() + "_" + i + "_" + j, 
											new Pair<TokenSpan, Boolean>(span, mentions != null && mentions.size() > 0));
								}	
							}
						}
					}
					
					return true;
				}
			});
		
		threads.run(documentClusters.entrySet(), this.context.getMaxThreads());
		
		Pair<Integer, Integer> idRange = this.context.getDataTools().getIncrementIdRange(datums.size());
		int i = idRange.getFirst();
		for (Entry<String, Pair<TokenSpan, Boolean>> entry : datums.entrySet()) {
			data.add(new TokenSpansDatum<Boolean>(i, new TokenSpan[] { entry.getValue().getFirst() }, entry.getValue().getSecond()));
			i++;
		}
				
		return data;
	}

	@Override
	public String getGenericName() {
		return "TokenSpanAnnotationIndicator";
	}

}
