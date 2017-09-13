package edu.psu.ist.acs.micro.event.scratch;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.cmu.ml.rtw.generic.data.Serializer;
import edu.cmu.ml.rtw.generic.data.StoredItemSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.AnnotationType;
import edu.cmu.ml.rtw.generic.data.annotation.DocumentSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.ConstituencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DependencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPInMemory;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLPMutable;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Predicate;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.Token;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression.TimeMLDocumentFunction;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression.TimeMLMod;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression.TimeMLType;
import edu.cmu.ml.rtw.generic.data.store.Storage;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorDocument;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorSentence;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorToken;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.AnnotatorTokenSpan;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLP;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPExtendable;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPMateTools;
import edu.cmu.ml.rtw.generic.model.annotator.nlp.PipelineNLPStanford;
import edu.cmu.ml.rtw.generic.util.FileUtil;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.Triple;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.Event;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableNormalizedTimeValue;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLAspect;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLClass;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLMood;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLPoS;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLPolarity;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLTense;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLVerbForm;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable.Type;

public class ConstructTimeBankDense {
	private static EventDataTools dataTools;
	
	public static final String DOCUMENT_COLLECTION = "tbd_docs";
	public static final String EVENT_MENTION_COLLECTION = "tbd_ementions";
	public static final String EVENT_COLLECTION = "tbd_events";
	public static final String TIME_EXPRESSION_COLLECTION = "tbd_timexes";
	public static final String TIME_VALUE_COLLECTION = "tbd_tvalues";
	public static final String TLINK_COLLECTION = "tbd_tlinks";
	//public static final String SIGNAL_COLLECTION = "TimeBankDenseSignals";
	
	private static String storageName;
	private static DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable> storedDocuments;
	private static StoredItemSetInMemoryLazy<EventMention, EventMention> storedEventMentions;
	private static StoredItemSetInMemoryLazy<Event, Event> storedEvents;
	private static StoredItemSetInMemoryLazy<LinkableTimeExpression, LinkableTimeExpression> storedTimeExpressions;
	private static StoredItemSetInMemoryLazy<LinkableNormalizedTimeValue, LinkableNormalizedTimeValue> storedTimeValues;
	private static StoredItemSetInMemoryLazy<TLink, TLink> storedTLinks;
	//private static StoredItemSetInMemoryLazy<Signal, Signal> storedSignals;
	
	private static Set<String> skippedPos = new HashSet<String>();
	private static Set<String> eventIds = new HashSet<String>();
	private static Set<String> timexIds = new HashSet<String>();
	private static Map<String, Map<String, Map<String, TimeMLRelType>>> tlinkTypes;
	private static int linkId = 0;
	private static int eventId = 0;
	private static int eventMentionId = 0;
	private static int timexId = 0;
	private static int timeValueId = 0;
	private static Map<String, Map<String, StoreReference>> references = new HashMap<String, Map<String, StoreReference>>(); 
	private static Map<String, Pair<LinkableNormalizedTimeValue, List<StoreReference>>> timeValues = new HashMap<>();
	
	private static PipelineNLP extraAnnotationPipeline;
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String dataFilePath = args[0];
		String tlinkFilePath = args[1];
		storageName = args[2];
		
		tlinkTypes = loadTLinkTypes(tlinkFilePath);
		dataTools = new EventDataTools();
		
		PipelineNLPStanford stanfordPipe = new PipelineNLPStanford();
		stanfordPipe.initialize(AnnotationTypeNLP.CONSTITUENCY_PARSE);
		extraAnnotationPipeline = stanfordPipe.weld(new PipelineNLPMateTools(dataTools.getProperties()));
		
		Storage<?, ?> storage = dataTools.getStoredItemSetManager().getStorage(storageName);
		if (storage.hasCollection(DOCUMENT_COLLECTION))
			storage.deleteCollection(DOCUMENT_COLLECTION);
		if (storage.hasCollection(EVENT_MENTION_COLLECTION))
			storage.deleteCollection(EVENT_MENTION_COLLECTION);
		if (storage.hasCollection(EVENT_COLLECTION))
			storage.deleteCollection(EVENT_COLLECTION);
		if (storage.hasCollection(TIME_EXPRESSION_COLLECTION))
			storage.deleteCollection(TIME_EXPRESSION_COLLECTION);
		if (storage.hasCollection(TIME_VALUE_COLLECTION))
			storage.deleteCollection(TIME_VALUE_COLLECTION);
		if (storage.hasCollection(TLINK_COLLECTION))
			storage.deleteCollection(TLINK_COLLECTION);
		//if (storage.hasCollection(SIGNAL_COLLECTION))
		//	storage.deleteCollection(SIGNAL_COLLECTION);
		
		Map<String, Serializer<?, ?>> serializers = dataTools.getSerializers();
		
		storedDocuments = new DocumentSetInMemoryLazy<DocumentNLP, DocumentNLPMutable>(
				dataTools.getStoredItemSetManager()
				.getItemSet(storageName, DOCUMENT_COLLECTION, true, (Serializer<DocumentNLPMutable, Document>)serializers.get("DocumentNLPBSON")));
		storedEventMentions = dataTools.getStoredItemSetManager()
				.getItemSet(storageName, EVENT_MENTION_COLLECTION, true,(Serializer<EventMention, Document>)serializers.get("JSONBSONEventMention"));
		storedEvents = dataTools.getStoredItemSetManager()
				.getItemSet(storageName, EVENT_COLLECTION, true,(Serializer<Event, Document>)serializers.get("JSONBSONEvent"));
		storedTimeExpressions = dataTools.getStoredItemSetManager()
				.getItemSet(storageName, TIME_EXPRESSION_COLLECTION, true,(Serializer<LinkableTimeExpression, Document>)serializers.get("JSONBSONTimeExpression"));
		storedTimeValues = dataTools.getStoredItemSetManager()
				.getItemSet(storageName, TIME_VALUE_COLLECTION, true,(Serializer<LinkableNormalizedTimeValue, Document>)serializers.get("JSONBSONNormalizedTimeValue"));
		storedTLinks = dataTools.getStoredItemSetManager()
				.getItemSet(storageName, TLINK_COLLECTION, true,(Serializer<TLink, Document>)serializers.get("JSONBSONTLink"));
		//storedSignals = dataTools.getStoredItemSetManager()
		//		.getItemSet(storageName, SIGNAL_COLLECTION, true,(Serializer<Signal, Document>)serializers.get("JSONBSONSignal"));
		
		SAXBuilder builder = new SAXBuilder();
		Document xml = null;
		try {
			xml = builder.build(new File(dataFilePath));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Element element = xml.getRootElement();
		List<Element> fileElements = (List<Element>)element.getChildren("file");
		int i = 0;
		for (Element fileElement : fileElements) {
			if (!documentFromTimeBankDenseXML(fileElement)) {
				System.out.println("Failed to load document (" + i + ").");
				return;
			}
			
			i++;
		}
		
		for (Pair<LinkableNormalizedTimeValue, List<StoreReference>> timeValue : timeValues.values()) {
			if (!storedTimeValues.addItem(timeValue.getFirst())) {
				System.out.println("Failed to store time value.");
				return;
			}
		}
		
		System.out.println("Timex count: " + timexIds.size());
		System.out.println("Event count: " + eventIds.size());
		
		for (String skipped : skippedPos) 
			System.out.println("Note: " + skipped + " was replaced with SYM pos tag");
	}
	
	private static Map<String, Map<String, Map<String, TimeMLRelType>>> loadTLinkTypes(String tlinkFilePath) {
		Map<String, Map<String, Map<String, TimeMLRelType>>> tlinkTypes = new HashMap<>();
		BufferedReader r = FileUtil.getFileReader(tlinkFilePath);
		
		try {
			String line = null;
			while ((line = r.readLine()) != null) {
				String[] lineParts = line.split("\t");
				
				TimeMLRelType relType = null;
				if (lineParts[3].equals("s")) {
					relType = TimeMLRelType.SIMULTANEOUS;
				} else if (lineParts[3].equals("b")) {
					relType = TimeMLRelType.BEFORE;
				} else if (lineParts[3].equals("a")) {
					relType = TimeMLRelType.AFTER;
				} else if (lineParts[3].equals("i")) {
					relType = TimeMLRelType.INCLUDES;
				} else if (lineParts[3].equals("ii")) {
					relType = TimeMLRelType.IS_INCLUDED;
				} else if (lineParts[3].equals("mv")) {
					relType = TimeMLRelType.MUTUAL_VAGUE;
				} else if (lineParts[3].equals("pv")) {
					relType = TimeMLRelType.PARTIAL_VAGUE;
				} else if (lineParts[3].equals("nv")) {
					relType = TimeMLRelType.NONE_VAGUE;
				}
				
				String name = lineParts[0] + ".tml";
				
				if (!tlinkTypes.containsKey(name))
					tlinkTypes.put(name, new HashMap<>());
				if (!tlinkTypes.get(name).containsKey(lineParts[1]))
					tlinkTypes.get(name).put(lineParts[1], new HashMap<>());
				tlinkTypes.get(name).get(lineParts[1]).put(lineParts[2], relType);
			}
		} catch (IOException e) {
			return null;
		}
		
		return tlinkTypes;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean documentFromTimeBankDenseXML(Element element) {
		String name = null;
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("name")) {
				name = element.getAttributeValue("name");
				break;
			}
		}
		
		if (name == null)
			return false;
		
		System.out.println("Loading document " + name + "...");
		
		PipelineNLPExtendable pipeline = new PipelineNLPExtendable();
		DocumentNLPMutable document = new DocumentNLPInMemory(dataTools, name, storageName, DOCUMENT_COLLECTION);
		
		List<Element> entryElements = (List<Element>)element.getChildren("entry");
		Pair<Token, Double>[][] tokens = new Pair[entryElements.size()][];
		Pair<PoSTag, Double>[][] posTags = new Pair[entryElements.size()][];
		Map<Integer, Pair<DependencyParse, Double>> dependencyParses = new HashMap<Integer, Pair<DependencyParse, Double>>();
		Map<Integer, Pair<ConstituencyParse, Double>> constituencyParses = new HashMap<Integer, Pair<ConstituencyParse, Double>>();
		Pair<String, Double> originalText = new Pair<String, Double>(null, null);
		StringBuilder textBuilder = new StringBuilder();
		
		for (Element entryElement : entryElements) {
			int sentenceIndex = Integer.parseInt(entryElement.getAttributeValue("sid"));
			
			Element tokensElement = entryElement.getChild("tokens");
			List<Element> tElements = tokensElement.getChildren("t");
			tokens[sentenceIndex] = new Pair[tElements.size()];
			
			for (int j = 0; j < tElements.size(); j++) {
				String tElementText = tElements.get(j).getText();
				int firstQuoteIndex = -1;
				int secondQuoteIndex = tElementText.length();
				for (int i = 0; i < 3; i++) {
					firstQuoteIndex = tElementText.indexOf("\"", firstQuoteIndex+1);
					secondQuoteIndex = tElementText.lastIndexOf("\"", secondQuoteIndex-1);
				}
			
				
				String token = tElementText.substring(firstQuoteIndex + 1, secondQuoteIndex);
				tokens[sentenceIndex][j] = 
						new Pair<Token, Double>(
						new Token(document, tElementText.substring(firstQuoteIndex + 1, secondQuoteIndex)),
						null);
				textBuilder.append(token).append(" ");
			}
		
			Element depsElement = entryElement.getChild("deps");
			if (depsElement != null) {
				dependencyParses.put(sentenceIndex, 
						new Pair<DependencyParse, Double>(
								DependencyParse.fromString(depsElement.getText(), document, sentenceIndex),
								null));
			}
			
			Element parseElement = entryElement.getChild("parse");
			if (parseElement != null) {
				constituencyParses.put(sentenceIndex,
						new Pair<ConstituencyParse, Double>(
								ConstituencyParse.fromString(parseElement.getText(), document, sentenceIndex),
								null));
				posTags[sentenceIndex] = new Pair[tElements.size()];
				for (int j = 0; j < tokens[sentenceIndex].length; j++) {
					String posStr = constituencyParses.get(sentenceIndex).getFirst().getTokenConstituent(j).getLabel();
					PoSTag tag = null;
					try {
						tag = PoSTag.valueOf(posStr);
					} catch (IllegalArgumentException e) {
						tag = PoSTag.SYM;
						skippedPos.add(posStr);
					}
					
					posTags[sentenceIndex][j] = 
						new Pair<PoSTag, Double>(
							tag,
							null);
				}
			}
		}
		
		originalText.setFirst(textBuilder.toString());
		
		pipeline.extend(new AnnotatorDocument<String>() {
			public String getName() { return "tbd"; }
			public AnnotationType<String> produces() { return AnnotationTypeNLP.ORIGINAL_TEXT; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { }; }
			public boolean measuresConfidence() { return false; }
			public Pair<String, Double> annotate(DocumentNLP document) {
				return originalText;
			}
		});
		
		pipeline.extend(new AnnotatorToken<Token>() {
			public String getName() { return "tbd"; }
			public AnnotationType<Token> produces() { return AnnotationTypeNLP.TOKEN; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { AnnotationTypeNLP.ORIGINAL_TEXT }; }
			public boolean measuresConfidence() { return false; }
			public Pair<Token, Double>[][] annotate(DocumentNLP document) {
				return tokens;
			}
		});
		
		pipeline.extend(new AnnotatorToken<PoSTag>() {
			public String getName() { return "tbd"; }
			public AnnotationType<PoSTag> produces() { return AnnotationTypeNLP.POS; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { AnnotationTypeNLP.TOKEN }; }
			public boolean measuresConfidence() { return false; }
			public Pair<PoSTag, Double>[][] annotate(DocumentNLP document) {
				return posTags;
			}
		});
		
		pipeline.extend(new AnnotatorSentence<ConstituencyParse>() {
			public String getName() { return "tbd"; }
			public AnnotationType<ConstituencyParse> produces() { return AnnotationTypeNLP.CONSTITUENCY_PARSE; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { AnnotationTypeNLP.TOKEN }; }
			public boolean measuresConfidence() { return false; }
			public Map<Integer, Pair<ConstituencyParse, Double>> annotate(DocumentNLP document) {
				return constituencyParses;
			}
		});
		
		pipeline.extend(new AnnotatorSentence<DependencyParse>() {
			public String getName() { return "tbd"; }
			public AnnotationType<DependencyParse> produces() { return AnnotationTypeNLP.DEPENDENCY_PARSE; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { AnnotationTypeNLP.TOKEN }; }
			public boolean measuresConfidence() { return false; }
			public Map<Integer, Pair<DependencyParse, Double>> annotate(DocumentNLP document) {
				return dependencyParses;
			}
		});


		List<Triple<TokenSpan, LinkableTimeExpression, Double>> timexes = new ArrayList<>();
		List<Triple<TokenSpan, EventMention, Double>> eventMentions = new ArrayList<>();
		for (Element entryElement : entryElements) {
			int sentenceIndex = Integer.parseInt(entryElement.getAttributeValue("sid"));
		
			Element timexesElement = entryElement.getChild("timexes");
			List<Element> timexElements = (List<Element>)timexesElement.getChildren("timex");
			if (timexElements != null && timexElements.size() != 0) {
				for (Element timexElement : timexElements) {
					LinkableTimeExpression timex = timexFromXML(timexElement, document, sentenceIndex);
					if (timex == null) {
						System.out.println("ERROR: Failed to load timex " + timexElement.toString());
						System.exit(0);
					}
					
					timexes.add(new Triple<>(timex.getTokenSpan(), timex, null));
				}
			}
			
			Element eventsElement = entryElement.getChild("events");
			List<Element> eventElements = (List<Element>)eventsElement.getChildren("event");
			if (eventElements != null && eventElements.size() != 0) {
				for (Element eventElement : eventElements) {
					List<EventMention> elemEventMentions = eventFromXML(eventElement, document, sentenceIndex);
					if (eventMentions == null) {
						System.out.println("ERROR: Failed to load events " + eventElement.toString());
						System.exit(0);	
					}
					for (EventMention eventMention : elemEventMentions) {
						eventMentions.add(new Triple<>(eventMention.getTokenSpan(), eventMention, null));
					}
				}
			}
		}
		
		pipeline.extend(new AnnotatorTokenSpan<LinkableTimeExpression>() {
			public String getName() { return "tbd"; }
			public AnnotationType<LinkableTimeExpression> produces() { return AnnotationTypeNLPEvent.TIME_EXPRESSION; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { }; }
			public boolean measuresConfidence() { return false; }
			public List<Triple<TokenSpan, LinkableTimeExpression, Double>> annotate(DocumentNLP document) {
				return timexes;
			}
		});
		
		pipeline.extend(new AnnotatorTokenSpan<EventMention>() {
			public String getName() { return "tbd"; }
			public AnnotationType<EventMention> produces() { return AnnotationTypeNLPEvent.EVENT_MENTION; };
			public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { }; }
			public boolean measuresConfidence() { return false; }
			public List<Triple<TokenSpan, EventMention, Double>> annotate(DocumentNLP document) {
				return eventMentions;
			}
		});
		
		List<Element> creationTimeElements = (List<Element>)element.getChildren("timex");
		if (creationTimeElements.size() > 0) {
			LinkableTimeExpression creationTime = timexFromXML(creationTimeElements.get(0), document, -1);
			if (creationTime != null) {
				pipeline.extend(new AnnotatorDocument<LinkableTimeExpression>() {
					public String getName() { return "tbd"; }
					public AnnotationType<LinkableTimeExpression> produces() { return AnnotationTypeNLPEvent.CREATION_TIME; };
					public AnnotationType<?>[] requires() { return new AnnotationType<?>[] { }; }
					public boolean measuresConfidence() { return false; }
					public Pair<LinkableTimeExpression, Double> annotate(DocumentNLP document) {
						return new Pair<LinkableTimeExpression, Double>(creationTime, null);
					}
				});
			} else {
				System.out.println("ERROR: Failed to load creation time.");
				System.exit(0);
			}
		}
		
		
		List<Element> tlinkElements = (List<Element>)element.getChildren("tlink");

		for (int i = 0; i < tlinkElements.size(); i++) {
			TLink tlink = tlinkFromXML(tlinkElements.get(i), document);
			if (tlink == null)
				return false;
		}

		document = addExtraAnnotations(pipeline.run(document));

		storedDocuments.addItem(document);
		
		return true;
	}
	
	// Add SRL and lemmas
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static DocumentNLPMutable addExtraAnnotations(DocumentNLPMutable document) {		
		Pair<String, Double>[][] lemmas = new Pair[document.getSentenceCount()][];
		List<Triple<TokenSpan, Predicate, Double>> predicates = new ArrayList<>();
		
		for (int i = 0; i < document.getSentenceCount(); i++) {
			DocumentNLPMutable tempDocument = new DocumentNLPInMemory(dataTools, document.getName(), document.getSentence(i));
			tempDocument = extraAnnotationPipeline.run(tempDocument);
			
			List<Pair<String, Double>> sentenceLemmas = new ArrayList<Pair<String, Double>>();
			for (int j = 0; j < tempDocument.getSentenceCount(); j++) {
				for (int k = 0; k < tempDocument.getSentenceTokenCount(j); k++) {
					if (j == 0 && tempDocument.getSentenceCount() == 2 && k == tempDocument.getSentenceTokenCount(j) - 1)
						continue;
					
					sentenceLemmas.add(new Pair<String, Double>(tempDocument.getTokenAnnotation(AnnotationTypeNLP.LEMMA, j, k), null));
				}
			}
			
			lemmas[i] = sentenceLemmas.toArray(new Pair[0]);

			if (lemmas[i].length != document.getSentenceTokenCount(i)) {
				throw new UnsupportedOperationException("Mismatching token count (" + lemmas[i].length + "," + document.getSentenceTokenCount(i) + ") for lemmas of " + document.getName() + " " + i + "\n" + document.getSentence(i));
			}
			
			List<Pair<TokenSpan, Predicate>> sentenceSrl = tempDocument.getTokenSpanAnnotations(AnnotationTypeNLP.PREDICATE);
			for (Pair<TokenSpan, Predicate> pair : sentenceSrl) {
				if (pair.getFirst().getSentenceIndex() == 0) {
					Pair<TokenSpan, Predicate> offsetPair = offsetPredicate(pair, i, 0);
					predicates.add(new Triple<TokenSpan, Predicate, Double>(
						offsetPair.getFirst(),
						offsetPair.getSecond(),
						null	
					));
				} else if (pair.getFirst().getSentenceIndex() == 1) { // Handle edge case where sentence is split
					Pair<TokenSpan, Predicate> offsetPair = offsetPredicate(pair, i, tempDocument.getSentenceTokenCount(0) - 1);
					predicates.add(new Triple<TokenSpan, Predicate, Double>(
						offsetPair.getFirst(),
						offsetPair.getSecond(),
						null	
					));
				} else {
					throw new UnsupportedOperationException();
				}
			}
		}
		
		String lemmaAnnotator = extraAnnotationPipeline.getAnnotatorName(AnnotationTypeNLP.LEMMA);
		document.setTokenAnnotation(lemmaAnnotator, AnnotationTypeNLP.LEMMA, lemmas);
		
		String srlAnnotator = extraAnnotationPipeline.getAnnotatorName(AnnotationTypeNLP.PREDICATE);
		document.setTokenSpanAnnotation(srlAnnotator, 
				(AnnotationTypeNLP<?>)AnnotationTypeNLP.PREDICATE, 
				(List)predicates);
		
		return document;
	}
	
	private static Pair<TokenSpan, Predicate> offsetPredicate(Pair<TokenSpan, Predicate> pair, int sentenceIndex, int tokenOffset) {
		TokenSpan span = offsetSpan(pair.getFirst(), sentenceIndex, tokenOffset);
		Map<String, TokenSpan[]> arguments = new TreeMap<String, TokenSpan[]>();
		
		for (String tag : pair.getSecond().getArgumentTags()) {
			TokenSpan[] spans = new TokenSpan[pair.getSecond().getArgument(tag).length];
			for (int i = 0; i < spans.length; i++) {
				spans[i] = offsetSpan(pair.getSecond().getArgument(tag)[i], sentenceIndex, tokenOffset);
			}
			
			arguments.put(tag, spans);
		}
		
		Predicate predicate = new Predicate(pair.getSecond().getSense(), 
											offsetSpan(pair.getSecond().getSpan(), sentenceIndex, tokenOffset), 
											arguments); 
		
		return new Pair<>(span, predicate);
	}
	
	private static TokenSpan offsetSpan(TokenSpan span, int sentenceIndex, int tokenOffset) {
		return new TokenSpan(span.getDocument(), 
				   sentenceIndex, 
				   span.getStartTokenIndex() + tokenOffset, 
				   span.getEndTokenIndex() + tokenOffset);
	}
	
	private static LinkableTimeExpression timexFromXML(Element element, DocumentNLP document, int sentenceIndex) {			
		boolean hasOffset = false;
		boolean hasLength = false;
		boolean hasId = false;
		boolean hasTimeMLType = false;
		boolean hasStartTimeId = false;
		boolean hasEndTimeId = false;
		boolean hasFreq = false;
		boolean hasValue = false;
		boolean hasQuant = false;
		boolean hasTimeMLDocumentFunction = false;
		boolean hasTemporalFunction = false;
		boolean hasAnchorTimeId = false;
		boolean hasValueFromFunctionId = false;
		boolean hasTimeMLMod = false;
		
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("offset"))
				hasOffset = true;
			else if (attribute.getName().equals("length"))
				hasLength = true;
			else if (attribute.getName().equals("tid"))
				hasId = true;
			else if (attribute.getName().equals("type"))
				hasTimeMLType = true;
			else if (attribute.getName().equals("starttid"))
				hasStartTimeId = true;
			else if (attribute.getName().equals("endtid"))
				hasEndTimeId = true;
			else if (attribute.getName().equals("quant"))
				hasQuant = true;
			else if (attribute.getName().equals("docFunction"))
				hasTimeMLDocumentFunction = true;
			else if (attribute.getName().equals("temporalFunction"))
				hasTemporalFunction = true;
			else if (attribute.getName().equals("anchortid"))
				hasAnchorTimeId = true;
			else if (attribute.getName().equals("valueFromFunctionTid"))
				hasValueFromFunctionId = true;
			else if (attribute.getName().equals("freq"))
				hasFreq = true;
			else if (attribute.getName().equals("value"))
				hasValue = true;
			else if (attribute.getName().equals("mod") && attribute.getValue().length() > 0)
				hasTimeMLMod = true;
		}

		TokenSpan tokenSpan = null;
		if (hasOffset) {
			int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
			int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
			tokenSpan = new TokenSpan(document, 
										sentenceIndex, 
										startTokenIndex, 
										endTokenIndex);
		} else {
			tokenSpan = new TokenSpan(document, -1, -1, -1);
		}
		
		String id = null;
		if (hasId)
			id = element.getAttributeValue("tid");
		else
			return null;
		
		StoreReference reference = makeTimexReference(document.getName(), id);
		
		TimeMLType timeMLType = null;
		if (hasTimeMLType)
			timeMLType = TimeMLType.valueOf(element.getAttributeValue("type"));
		
		StoreReference startTimeReference = null;
		if (hasStartTimeId) {
			String starttid = element.getAttributeValue("starttid");
			if (starttid.length() > 0) {
				startTimeReference = makeTimexReference(document.getName(), starttid);
			}
		}
		
		StoreReference endTimeReference = null;
		if (hasEndTimeId) {
			String endtid = element.getAttributeValue("endtid");
			if (endtid.length() > 0) {
				endTimeReference = makeTimexReference(document.getName(), endtid);
			}
		}
		
		String freq = null;
		if (hasFreq)
			freq = element.getAttributeValue("freq");
		LinkableNormalizedTimeValue value = null;
		StoreReference valueRef = null;
		if (hasValue) {
			value = makeTimeValue(document.getName(), element.getAttributeValue("value"), reference);
			valueRef = value.getStoreReference();
		}
		
		String quant = null;
		if (hasQuant)
			quant = element.getAttributeValue("quant");
		
		TimeMLDocumentFunction timeMLDocumentFunction = TimeMLDocumentFunction.NONE;
		if (hasTimeMLDocumentFunction && element.getAttributeValue("docFunction").length() > 0)
			timeMLDocumentFunction = TimeMLDocumentFunction.valueOf(element.getAttributeValue("docFunction"));
		
		boolean temporalFunction = false;
		if (hasTemporalFunction && element.getAttributeValue("temporalFunction").length() > 0)
			temporalFunction = Boolean.parseBoolean(element.getAttributeValue("temporalFunction"));
		
		StoreReference anchorTimeReference = null;
		if (hasAnchorTimeId) {
			String anchortid = element.getAttributeValue("anchortid");
			if (anchortid.length() > 0) {
				anchorTimeReference = makeTimexReference(document.getName(), anchortid);
			}
		}
		
		StoreReference valueFromFunctionReference = null;
		if (hasValueFromFunctionId) {
			String valueFromFunctionTid = element.getAttributeValue("valueFromFunctionTid");
			if (valueFromFunctionTid.length() > 0) {
				valueFromFunctionReference = makeTimexReference(document.getName(), valueFromFunctionTid);
			}
		}
		
		TimeMLMod timeMLMod = null;
		if (hasTimeMLMod)
			timeMLMod = TimeMLMod.valueOf(element.getAttributeValue("mod"));
		
		LinkableTimeExpression timeExpression = new LinkableTimeExpression(dataTools, 
														  reference,
														  tokenSpan,
														  reference.getIndexValue(0).toString(),
														  id,
														  timeMLType,
														  startTimeReference,
														  endTimeReference,
														  quant,
														  freq,
														  valueRef,
														  timeMLDocumentFunction,
														  temporalFunction,
														  anchorTimeReference,
														  valueFromFunctionReference,
														  timeMLMod);
		
		timexIds.add(document.getName() + "_" + id);
		
		if (!storedTimeExpressions.addItem(timeExpression))
			return null;
		else 
			return timeExpression;
	}
	
	public static List<EventMention> eventFromXML(Element element, DocumentNLP document, int sentenceIndex) {
		String[] eiids = element.getAttributeValue("eiid").split(",");
		List<EventMention> events = new ArrayList<EventMention>();
		for (String eiid : eiids) {
			boolean hasOffset = false;
			boolean hasLength = false;
			boolean hasId = false;
			boolean hasTense = false;
			boolean hasAspect = false;
			boolean hasClass = false;
			boolean hasPolarity = false;
			boolean hasPoS = false;
			boolean hasMood = false;
			boolean hasVForm = false;
			boolean hasModality = false;
			boolean hasCardinality = false;
			
			List<Attribute> attributes = (List<Attribute>)element.getAttributes();
			for (Attribute attribute : attributes)
				if (attribute.getName().equals("offset"))
					hasOffset = true;
				else if (attribute.getName().equals("length"))
					hasLength = true;
				else if (attribute.getName().equals("id"))
					hasId = true;
				else if (attribute.getName().equals("tense"))
					hasTense = true;
				else if (attribute.getName().equals("aspect"))
					hasAspect = true;
				else if (attribute.getName().equals("class"))
					hasClass = true;
				else if (attribute.getName().equals("polarity"))
					hasPolarity = true;
				else if (attribute.getName().equals("pos"))
					hasPoS = true;
				else if (attribute.getName().equals("mood"))
					hasMood = true;
				else if (attribute.getName().equals("vform"))
					hasVForm = true;
				else if (attribute.getName().equals("modality"))
					hasModality = true;
				else if (attribute.getName().equals("cardinality"))
					hasCardinality = true;
			
			String id = eiid;
			if (id == null || id.length() == 0) {
				id = element.getAttributeValue("id");
			}
			
			TokenSpan tokenSpan = null;
			if (hasOffset) {
				int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
				int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
				tokenSpan = new TokenSpan(document, 
												sentenceIndex, 
												startTokenIndex, 
												endTokenIndex);
			} else {
				System.out.println("ERROR: Event " + id + " missing offset.");
				return null;
			}
			
			String sourceId = null;
			if (hasId)
				sourceId = element.getAttributeValue("id");
			
			TimeMLTense timeMLTense = null;
			if (hasTense)
				timeMLTense = TimeMLTense.valueOf(element.getAttributeValue("tense"));
			
			TimeMLAspect timeMLAspect = null;
			if (hasAspect)
				timeMLAspect = TimeMLAspect.valueOf(element.getAttributeValue("aspect"));
			
			TimeMLClass timeMLClass = null;
			if (hasClass)
				timeMLClass = TimeMLClass.valueOf(element.getAttributeValue("class"));
			
			TimeMLPolarity timeMLPolarity = null;
			if (hasPolarity)
				timeMLPolarity = TimeMLPolarity.valueOf(element.getAttributeValue("polarity"));
			
			TimeMLPoS timeMLPoS = null;
			if (hasPoS)
				timeMLPoS = TimeMLPoS.valueOf(element.getAttributeValue("pos"));
			
			TimeMLMood timeMLMood = null;
			if (hasMood)
				timeMLMood = TimeMLMood.valueOf(element.getAttributeValue("mood"));
			
			TimeMLVerbForm timeMLVerbForm = null;
			if (hasVForm)
				timeMLVerbForm = TimeMLVerbForm.valueOf(element.getAttributeValue("vform"));
			
			String modality = null;
			if (hasModality)
				modality = element.getAttributeValue("modality");
			
			String cardinality = null;
			if (hasCardinality)
				cardinality = element.getAttributeValue("cardinality");
			
			StoreReference mentionRef = makeEventMentionReference(document.getName(), id);
			StoreReference eventRef = makeEventReference();
			List<StoreReference> mentionRefs = new ArrayList<StoreReference>();
			mentionRefs.add(mentionRef);
			EventMention eventMention = new EventMention(dataTools,
					mentionRef, 
					mentionRef.getIndexValue(0).toString(), 
					sourceId,
					eiid,
					tokenSpan,
					null,
					timeMLTense, 
					timeMLAspect, 
					timeMLClass, 
					timeMLPolarity, 
					timeMLMood, 
					timeMLVerbForm, 
					timeMLPoS,
					modality,
					cardinality,
					null,
					eventRef,
					null);
			
			Event event = new Event(dataTools,
					eventRef, 
					eventRef.getIndexValue(0).toString(),
					null,
					null,
					null,
					new ArrayList<>(),
					mentionRefs);
			
			eventIds.add(document.getName() + "_" + id);
			
			if (!storedEventMentions.addItem(eventMention)) {
				System.out.println("ERROR: Failed to add event mention " + id + " to storage.");
				return null;
			}
			
			if (!storedEvents.addItem(event)) {
				System.out.println("ERROR: Failed to add event " + id + " to storage.");
				return null;
			}
			
			events.add(eventMention);
		}
		
		return events;
	}
	
	public static TLink tlinkFromXML(Element element, DocumentNLP document) {
		boolean hasOrigin = false;
		boolean hasSourceId = false;
		boolean hasTargetId = false;
		boolean hasRelation = false;
		boolean hasSyntax = false;
			
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("origin"))
				hasOrigin = true;
			else if (attribute.getName().equals("event1"))
				hasSourceId = true;
			else if (attribute.getName().equals("event2"))
				hasTargetId = true;
			else if (attribute.getName().equals("relation"))
				hasRelation = true;
			else if (attribute.getName().equals("syntax"))
				hasSyntax = true;
		}
		
		linkId++;
		String id = String.valueOf(linkId);
		
		String origin = null;
		if (hasOrigin)
			origin = element.getAttributeValue("origin");
		
		String syntax = null;
		if (hasSyntax)
			syntax = element.getAttributeValue("syntax");
		
		StoreReference sourceReference = null;
		if (hasSourceId) {
			String sourceId = element.getAttributeValue("event1");
			if (sourceId.startsWith("e"))
				sourceReference = makeEventMentionReference(document.getName(), sourceId);
			else
				sourceReference = makeTimexReference(document.getName(), sourceId);
		}
		
		StoreReference targetReference = null;
		if (hasTargetId) {
			String targetId = element.getAttributeValue("event2");
			if (targetId.startsWith("e"))
				targetReference = makeEventMentionReference(document.getName(), targetId);
			else
				targetReference = makeTimexReference(document.getName(), targetId);
		}
		
		TimeMLRelType timeMLRelType = null;
		if (hasRelation) {
			TLinkable sourceObj = sourceReference.resolve(dataTools, true);
			TLinkable targetObj = targetReference.resolve(dataTools, true);
			String sourceId = null;
			if (sourceObj.getTLinkableType() == Type.TIME) {
				sourceId = ((LinkableTimeExpression)sourceObj).getSourceId();
			} else {
				sourceId = ((EventMention)sourceObj).getSourceId();
			}
			
			String targetId = null;
			if (targetObj.getTLinkableType() == Type.TIME) {
				targetId = ((LinkableTimeExpression)targetObj).getSourceId();
			} else {
				targetId = ((EventMention)targetObj).getSourceId();
			}
			
			if (!tlinkTypes.containsKey(document.getName()) 
					|| !tlinkTypes.get(document.getName()).containsKey(sourceId) 
					|| !tlinkTypes.get(document.getName()).get(sourceId).containsKey(targetId)) {
				System.out.println("ERROR: Missing link " + document.getName() + " " + sourceId + " " + targetId);
				System.exit(0);
			}
			
			TimeMLRelType fineGrainedType = tlinkTypes.get(document.getName()).get(sourceId).get(targetId);
			TimeMLRelType coarseGrainedType = TimeMLRelType.valueOf(element.getAttributeValue("relation"));
			
			if (fineGrainedType.equals(coarseGrainedType)
					|| (coarseGrainedType.equals(TimeMLRelType.VAGUE)
							&& (fineGrainedType.equals(TimeMLRelType.MUTUAL_VAGUE)
									|| fineGrainedType.equals(TimeMLRelType.PARTIAL_VAGUE)
									|| fineGrainedType.equals(TimeMLRelType.NONE_VAGUE)))) {
				timeMLRelType = fineGrainedType;
			} else {
				System.out.println("ERROR: TLink type mismatch " + document.getName() + sourceId + " " + targetId);
				System.exit(0);
			}
		}
			
		StoreReference reference = new StoreReference(storageName, TLINK_COLLECTION, "id", id);
		TLink tlink = new TLink(dataTools, 
				 				reference, 
				 				id, 
				 				origin,
				 				sourceReference, 
				 				targetReference, 
				 				timeMLRelType,
				 				null,
				 				syntax);
		
		if (!storedTLinks.addItem(tlink))
			return null;
		
		return tlink;
	}
	
	private static StoreReference makeEventMentionReference(String documentName, String sourceId) {
		if (!references.containsKey(documentName))
			references.put(documentName, new HashMap<String, StoreReference>());
		if (!references.get(documentName).containsKey(sourceId)) {
			eventMentionId++;
			references.get(documentName).put(sourceId, new StoreReference(storageName, EVENT_MENTION_COLLECTION, "id", String.valueOf(eventMentionId)));
		}
		
		return references.get(documentName).get(sourceId);
	}
	
	private static StoreReference makeTimexReference(String documentName, String sourceId) {
		if (!references.containsKey(documentName))
			references.put(documentName, new HashMap<String, StoreReference>());
		if (!references.get(documentName).containsKey(sourceId)) {
			timexId++;
			references.get(documentName).put(sourceId, new StoreReference(storageName, TIME_EXPRESSION_COLLECTION, "id", String.valueOf(timexId)));
		}
		
		return references.get(documentName).get(sourceId);
	}
	
	private static LinkableNormalizedTimeValue makeTimeValue(String documentName, String value, StoreReference exprRef) {
		String docSpecId = documentName + "_" + value;
		if (timeValues.containsKey(docSpecId)) {
			timeValues.get(docSpecId).getSecond().add(exprRef);
			return timeValues.get(docSpecId).getFirst();
		}
	
		timeValueId++;
		StoreReference ref = new StoreReference(storageName, TIME_VALUE_COLLECTION, "id", String.valueOf(timeValueId));
		List<StoreReference> exprRefs = new ArrayList<>();
		exprRefs.add(exprRef);
		LinkableNormalizedTimeValue timeValue = new LinkableNormalizedTimeValue(dataTools,ref, ref.getIndexValue(0).toString(), value, exprRefs);
		timeValues.put(docSpecId, new Pair<LinkableNormalizedTimeValue, List<StoreReference>>(timeValue, exprRefs));
		
		return timeValue;
	}
	
	private static StoreReference makeEventReference() {
		eventId++;
		return new StoreReference(storageName, EVENT_COLLECTION, "id", String.valueOf(eventId));
	}

	/*
	public static Signal signalFfromXML(Element element, TempDocument document, int sentenceIndex) {
		Signal signal = new Signal();
			
		boolean hasOffset = false;
		boolean hasLength = false;
		boolean hasId = false;
			
		List<Attribute> attributes = (List<Attribute>)element.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getName().equals("offset"))
				hasOffset = true;
			else if (attribute.getName().equals("length"))
				hasLength = true;
			else if (attribute.getName().equals("id"))
				hasId = true;
		}
				
		if (hasOffset) {
			int startTokenIndex = Integer.parseInt(element.getAttributeValue("offset")) - 1;
			int endTokenIndex = startTokenIndex + ((hasLength) ? Integer.parseInt(element.getAttributeValue("length")) : 1);
			signal.tokenSpan = new TokenSpan(document, 
											sentenceIndex, 
											startTokenIndex, 
											endTokenIndex);
		}
		
		if (hasId)
			signal.id = element.getAttributeValue("id");
		
		return signal;
	}*/
}
