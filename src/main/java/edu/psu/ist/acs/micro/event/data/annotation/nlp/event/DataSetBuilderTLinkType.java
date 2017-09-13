package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.cmu.ml.rtw.generic.data.StoredItemSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.DocumentSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.DataSetBuilder;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.ThreadMapper;
import edu.cmu.ml.rtw.generic.util.ThreadMapper.Fn;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TextDirection;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;

public class DataSetBuilderTLinkType extends DataSetBuilderDocumentFiltered<TLinkDatum<TimeMLRelType>, TimeMLRelType> {
	private enum CrossDocumentMode {
		NONE,
		ALL,
		ONLY_TIME_TIME
	}
	
	private enum DirectionMode {
		FORWARD,
		BACKWARD,
		ALL
	}
	
	private DirectionMode directionMode = DirectionMode.FORWARD;
	private String tlinks;
	private int maxSentenceDistance = -1;
	private CrossDocumentMode crossDocMode = CrossDocumentMode.NONE;
	private boolean useLinkIds = false;
	private String[] parameterNames = { "directionMode", "tlinks", "maxSentenceDistance", "crossDocMode", "useLinkIds" };
	
	public DataSetBuilderTLinkType() {
		this(null);
	}
	
	public DataSetBuilderTLinkType(DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> context) {
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
		if (parameter.equals("tlinks"))
			return (this.tlinks == null) ? null : Obj.stringValue(this.tlinks.toString());
		else if (parameter.equals("maxSentenceDistance"))
			return Obj.stringValue(String.valueOf(this.maxSentenceDistance));
		else if (parameter.equals("crossDocMode"))
			return Obj.stringValue(String.valueOf(this.crossDocMode));
		else if (parameter.equals("directionMode"))
			return Obj.stringValue(String.valueOf(this.directionMode));
		else if (parameter.equals("useLinkIds"))
			return Obj.stringValue(String.valueOf(this.useLinkIds));
		else
			return super.getParameterValue(parameter);
	}

	@Override
	public boolean setParameterValue(String parameter, Obj parameterValue) {
		if (parameter.equals("tlinks"))
			this.tlinks = (parameterValue == null) ? null : this.context.getMatchValue(parameterValue);
		else if (parameter.equals("maxSentenceDistance"))
			this.maxSentenceDistance = Integer.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("crossDocMode"))
			this.crossDocMode = CrossDocumentMode.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("directionMode"))
			this.directionMode = DirectionMode.valueOf(this.context.getMatchValue(parameterValue));
		else if (parameter.equals("useLinkIds"))
			this.useLinkIds = Boolean.valueOf(this.context.getMatchValue(parameterValue));
		else
			return super.setParameterValue(parameter, parameterValue);
		return true;
	}
	
	@Override
	public DataSetBuilder<TLinkDatum<TimeMLRelType>, TimeMLRelType> makeInstance(
			DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> context) {
		return new DataSetBuilderTLinkType(context);
	}

	private boolean linkMeetsCrossDocumentMode(TLink link, boolean labeled) {
		return !link.isBetweenDocuments() || this.crossDocMode == CrossDocumentMode.ALL || (this.crossDocMode == CrossDocumentMode.ONLY_TIME_TIME && link.getType() == TLink.Type.TIME_TIME);
	}
	
	@Override
	public DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> build() {
		StoredItemSetInMemoryLazy<TLink, TLink> tlinkSet = (this.tlinks == null) ? null :
				this.context.getDataTools().getStoredItemSetManager().getItemSet(this.storage, this.tlinks);
		
		DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType> data = new DataSet<TLinkDatum<TimeMLRelType>, TimeMLRelType>(this.context.getDatumTools());
		
		Map<String, Set<String>> labeledPairs = new HashMap<>();
		TreeMap<Integer, TLink> labeledLinks = new TreeMap<>();
		if (tlinkSet != null) {
			tlinkSet.map(new Fn<TLink, Boolean>() {
				@Override
				public Boolean apply(TLink tlink) {
					DocumentNLP doc = tlink.getSource().getTokenSpan().getDocument();
					if (!matchesFilter(doc))
						return true;
					
					int sourceSI = tlink.getSource().getTokenSpan().getSentenceIndex();
					int targetSI = tlink.getTarget().getTokenSpan().getSentenceIndex();
					if (maxSentenceDistance >= 0 && 
							sourceSI >= 0 && targetSI >= 0 && 
							Math.abs(sourceSI - targetSI) > maxSentenceDistance) {
						return true;
					}
					
					TextDirection d = tlink.getTextDirection();
					if (directionMode == DirectionMode.FORWARD && d != TextDirection.FORWARD && d != TextDirection.NONE)
						return true;
					if (directionMode == DirectionMode.BACKWARD && d != TextDirection.BACKWARD && d != TextDirection.NONE)
						return true;
						
					synchronized (labeledLinks) {
						if (tlink.getType() == TLink.Type.EVENT_TIME && tlink.getSource().getTLinkableType() == TLinkable.Type.TIME)
							tlink = tlink.getConverse();
							
						if (!labeledPairs.containsKey(tlink.getSource().getId()))
							labeledPairs.put(tlink.getSource().getId(), new HashSet<>());
						labeledPairs.get(tlink.getSource().getId()).add(tlink.getTarget().getId());				
						
						if (labelMode != LabelMode.ONLY_UNLABELED && linkMeetsCrossDocumentMode(tlink, true)) {	
							labeledLinks.put(Integer.valueOf(tlink.getId()), tlink);
						}
					}
					
					return true;
				}
				
			}, this.context.getMaxThreads(), this.context.getDataTools().getGlobalRandom());
		}
		
		Pair<Integer, Integer> idRange = this.context.getDataTools().getIncrementIdRange(labeledLinks.size());
		int i = idRange.getFirst();
		for (Entry<Integer, TLink> entry : labeledLinks.entrySet()) {
			int id = (this.useLinkIds) ? entry.getKey() : i;
			if (this.labelMode != LabelMode.ALL_AS_UNLABELED)
				data.add(new TLinkDatum<TimeMLRelType>(
					id,  entry.getValue(), (labelMapping != null) ? labelMapping.map(entry.getValue().getTimeMLRelType()) : entry.getValue().getTimeMLRelType()));
			else
				data.add(new TLinkDatum<TimeMLRelType>(id,  entry.getValue(), null));

			i++;
		}
		
		PairFn<Pair<TokenSpan, StoreReference>, TLink> fn = new PairFn<Pair<TokenSpan, StoreReference>, TLink>() {
			@Override
			public TLink apply(Pair<TokenSpan, StoreReference> o1, Pair<TokenSpan, StoreReference> o2) {
				String id1 = o1.getSecond().getIndexValue(0).toString();
				String id2 = o2.getSecond().getIndexValue(0).toString();
				
				if (maxSentenceDistance >= 0 && o1.getFirst() != null && o2.getFirst() != null) {
					int si1 = o1.getFirst().getSentenceIndex();
					int si2 = o2.getFirst().getSentenceIndex();
					if (o1.getFirst().getDocument().getName().equals(o2.getFirst().getDocument().getName()) 
							&& si1 >= 0 && si2 >= 0 && Math.abs(si1 - si2) > maxSentenceDistance)
						return null;
				}
				
				
				TLink link = new TLink((EventDataTools)context.getDataTools(), null, id1 + "_" + id2, null, o1.getSecond(), o2.getSecond(), null,null,null);
				TextDirection d = link.getTextDirection();
				if (directionMode == DirectionMode.FORWARD && d != TextDirection.FORWARD && d != TextDirection.NONE)
					return null;
				if (directionMode == DirectionMode.BACKWARD && d != TextDirection.BACKWARD && d != TextDirection.NONE)
					return null;

				if (link.getType() == TLink.Type.EVENT_TIME && link.getSource().getTLinkableType() == TLinkable.Type.TIME) {
					link = new TLink((EventDataTools)context.getDataTools(), null, id2 + "_" + id1, null, o2.getSecond(), o1.getSecond(), null,null,null);
					String temp = id2;
					id2 = id1;
					id1 = temp;
				}
				
				if (labeledPairs.containsKey(id1) && labeledPairs.get(id1).contains(id2))
					return null;
				else if (linkMeetsCrossDocumentMode(link, false))
					return link;
				else
					return null;
			}
		};
		
		if (this.labelMode != LabelMode.ONLY_LABELED || this.crossDocMode != CrossDocumentMode.NONE) {
			Map<String, Set<String>> documentClusters = getDocumentClusters();
			DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> docs = getDocuments();
			TreeMap<String, TLink> unlabeledLinks = new TreeMap<String, TLink>();
			ThreadMapper<Entry<String, Set<String>>, Boolean> threads = new ThreadMapper<Entry<String, Set<String>>, Boolean>(
				new Fn<Entry<String, Set<String>>, Boolean>() {
					@Override
					public Boolean apply(Entry<String, Set<String>> item) {
						List<TLink> links = new ArrayList<TLink>();
						
						for (String docName1 : item.getValue()) {
							DocumentNLP doc1 = docs.getDocumentByName(docName1, true);
							
							List<Pair<TokenSpan, StoreReference>> doc1Refs = new ArrayList<>();
							List<Pair<TokenSpan, EventMention>> doc1Mentions = doc1.getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION);
							List<Pair<TokenSpan, LinkableTimeExpression>> doc1Exprs = doc1.getTokenSpanAnnotations(AnnotationTypeNLPEvent.TIME_EXPRESSION);
							for (Pair<TokenSpan, EventMention> m : doc1Mentions)
								doc1Refs.add(new Pair<TokenSpan, StoreReference>(m.getFirst(), m.getSecond().getStoreReference()));
							for (Pair<TokenSpan, LinkableTimeExpression> e : doc1Exprs)
								doc1Refs.add(new Pair<TokenSpan, StoreReference>(e.getFirst(), e.getSecond().getStoreReference()));
							doc1Refs.add(new Pair<TokenSpan, StoreReference>(null, doc1.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME).getStoreReference()));
							
							
							if (labelMode != LabelMode.ONLY_LABELED)
								links = runAllPairs(doc1Refs, fn, links, true); 
							
							if (crossDocMode == CrossDocumentMode.NONE)
								continue;
							
							for (String docName2 : item.getValue()) {
								if (docName1.equals(docName2))
									continue;
								
								DocumentNLP doc2 = docs.getDocumentByName(docName2, true);
								List<Pair<TokenSpan, StoreReference>> doc2Refs = new ArrayList<>();
								List<Pair<TokenSpan, EventMention>> doc2Mentions = doc2.getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION);
								List<Pair<TokenSpan, LinkableTimeExpression>> doc2Exprs = doc2.getTokenSpanAnnotations(AnnotationTypeNLPEvent.TIME_EXPRESSION);
								for (Pair<TokenSpan, EventMention> m : doc2Mentions)
									doc2Refs.add(new Pair<TokenSpan, StoreReference>(m.getFirst(), m.getSecond().getStoreReference()));
								for (Pair<TokenSpan, LinkableTimeExpression> e : doc2Exprs)
									doc2Refs.add(new Pair<TokenSpan, StoreReference>(e.getFirst(), e.getSecond().getStoreReference()));
								doc2Refs.add(new Pair<TokenSpan, StoreReference>(null, doc2.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME).getStoreReference()));
								
								links = runAllPairs(doc1Refs, doc2Refs, fn, links); 
							}
						}
						
						synchronized (unlabeledLinks) {
							for (TLink link : links)
								unlabeledLinks.put(link.getId(), link);
						}
						
						return true;
					}
				});
			
			threads.run(documentClusters.entrySet(), this.context.getMaxThreads());
			
			idRange = this.context.getDataTools().getIncrementIdRange(labeledLinks.size());
			i = idRange.getFirst();
			for (Entry<String, TLink> entry : unlabeledLinks.entrySet()) {
				data.add(new TLinkDatum<TimeMLRelType>(i, entry.getValue(), null));
				i++;
			}
		}
		
		return data;
	}

	@Override
	public String getGenericName() {
		return "TLinkType";
	}

}
