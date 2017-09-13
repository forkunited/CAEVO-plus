package edu.psu.ist.acs.micro.event.task.classify.tlink.det;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTagClass;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLAspect;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLTense;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;

public class DetReichenbach {
	private static int SENTENCE_WINDOW = Integer.MAX_VALUE;
	private static boolean SAME_TENSE = false;
	private static boolean SAME_SENTENCE = true;
	private static boolean SIMPLIFY_PAST = false;
	private static boolean SIMPLIFY_PRESENT = false;
	private static boolean SIMPLIFY_ASPECT = true;
	private static boolean USE_EXTENDED_TENSE = true;
	//private static boolean USE_EXTENDED_TENSE_ACROSS_SENTENCE = true;
	
	public static TimeMLRelType determineRelation(EventMention e1, EventMention e2) {
		TokenSpan e1Span = e1.getTokenSpan();
		TokenSpan e2Span = e2.getTokenSpan();
		if (!e1.getTokenSpan().getDocument().getName().equals(e2.getTokenSpan().getDocument().getName()))
			return null;
		
		if (e1Span.getSentenceIndex() == e2Span.getSentenceIndex() && !SAME_SENTENCE)
			return null;
		
		if (Math.abs(e1Span.getSentenceIndex() - e2Span.getSentenceIndex()) > SENTENCE_WINDOW)
			return null;
		
		DocumentNLP document = e1.getTokenSpan().getDocument();
		PoSTag pos1 = document.getPoSTag(e1.getTokenSpan().getSentenceIndex(), e1.getTokenSpan().getStartTokenIndex());
		PoSTag pos2 = document.getPoSTag(e2.getTokenSpan().getSentenceIndex(), e2.getTokenSpan().getStartTokenIndex());

		if (!PoSTagClass.classContains(PoSTagClass.VB, pos1) || !PoSTagClass.classContains(PoSTagClass.VB, pos2))
			return null;
		
		if (SAME_TENSE && e1.getTimeMLTense() != e2.getTimeMLTense())
			return null;
		
		TimeMLTense e1Tense = simplifyTense((USE_EXTENDED_TENSE) ? e1.getTimeMLExtendedTense() : e1.getTimeMLTense());
		TimeMLTense e2Tense = simplifyTense((USE_EXTENDED_TENSE) ? e2.getTimeMLExtendedTense() : e2.getTimeMLTense());
		TimeMLAspect e1Aspect = simplifyAspect(e1.getTimeMLAspect());
		TimeMLAspect e2Aspect = simplifyAspect(e2.getTimeMLAspect());
		
		boolean e1Past = (e1Tense == TimeMLTense.PAST);
		boolean e2Past = (e2Tense == TimeMLTense.PAST);
		boolean e1Pres = (e1Tense == TimeMLTense.PRESENT);
		boolean e2Pres = (e2Tense == TimeMLTense.PRESENT);		
		boolean e1Future = (e1Tense == TimeMLTense.FUTURE);
		boolean e2Future = (e2Tense == TimeMLTense.FUTURE);
		boolean e1Perf = (e1Aspect == TimeMLAspect.PERFECTIVE);
		boolean e1None = (e1Aspect == TimeMLAspect.NONE);
		boolean e2Perf = (e2Aspect == TimeMLAspect.PERFECTIVE);
		boolean e2None = (e2Aspect == TimeMLAspect.NONE);
		
		TimeMLRelType rel = null;
		if (e1Past && e1None && e2Past && e2Perf) 
			rel = TimeMLRelType.AFTER;    
		if (e1Past && e1None && e2Future && e2None) 
			rel = TimeMLRelType.BEFORE;
		if (e1Past && e1None && e2Future && e2Perf) 
			rel = TimeMLRelType.BEFORE; 
		if (e1Past && e1Perf && e2Past && e2None) 
			rel = TimeMLRelType.BEFORE ;
		if (e1Past && e1Perf && e2Pres && e2None) 
			rel = TimeMLRelType.BEFORE;
		if (e1Past && e1Perf && e2Pres && e2Perf) 
			rel = TimeMLRelType.BEFORE; 
		if (e1Past && e1Perf && e2Future && e2None) 
			rel = TimeMLRelType.BEFORE;
		if (e1Past && e1Perf && e2Future && e2Perf) 
			rel = TimeMLRelType.BEFORE;
		if (e1Pres && e1None && e2Past && e2Perf) 
			rel = TimeMLRelType.AFTER;
		if (e1Pres && e1None && e2Future && e2None) 
			rel = TimeMLRelType.BEFORE; 
		if (e1Pres && e1Perf && e2Past && e2Perf) 
			rel = TimeMLRelType.AFTER;    
		if (e1Pres && e1Perf && e2Future && e2None) 
			rel = TimeMLRelType.BEFORE;
		if (e1Pres && e1Perf && e2Future && e2Perf) 
			rel = TimeMLRelType.BEFORE;
		if (e1Future && e1None && e2Past && e2None) 
			rel = TimeMLRelType.AFTER;
		if (e1Future && e1None && e2Past && e2Perf) 
			rel = TimeMLRelType.AFTER; 
		if (e1Future && e1None && e2Pres && e2None) 
			rel = TimeMLRelType.AFTER; 
		if (e1Future && e1None && e2Pres && e2Perf) 
			rel = TimeMLRelType.AFTER;
		if (e1Future && e1Perf && e2Past && e2None) 
			rel = TimeMLRelType.AFTER;  
		if (e1Future && e1Perf && e2Past && e2Perf) 
			rel = TimeMLRelType.AFTER;
		if (e1Future && e1Perf && e2Pres && e2Perf) 
			rel = TimeMLRelType.AFTER;    

		if (rel != null) {
			return rel;
		}
	
		return null;
	}
	
	private static TimeMLTense simplifyTense(TimeMLTense tense){
		if (tense == TimeMLTense.PAST || (tense == TimeMLTense.PASTPART && SIMPLIFY_PAST)) 
			return TimeMLTense.PAST;
		else if (tense == TimeMLTense.PRESENT || (tense == TimeMLTense.PRESPART && SIMPLIFY_PRESENT)) 
			return TimeMLTense.PRESENT;
		else if (tense == TimeMLTense.FUTURE) 
			return tense;
		else 
			return null; 
	}
	
	private static TimeMLAspect simplifyAspect(TimeMLAspect aspect){
		if ((SIMPLIFY_ASPECT && aspect == TimeMLAspect.PERFECTIVE_PROGRESSIVE) || aspect == TimeMLAspect.PERFECTIVE)
			return TimeMLAspect.PERFECTIVE;
		else if (aspect == TimeMLAspect.NONE) 
			return aspect;
		else 
			return null; 
	}
}
