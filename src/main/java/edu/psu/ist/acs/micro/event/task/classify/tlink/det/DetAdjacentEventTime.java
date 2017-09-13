package edu.psu.ist.acs.micro.event.task.classify.tlink.det;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DependencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTagClass;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.NormalizedTimeValue;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.NormalizedTimeValue.Reference;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLAspect;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;

public class DetAdjacentEventTime {
	private static final int MAX_DISTANCE = 2;
	
	public static TimeMLRelType determineRelation(EventMention event, LinkableTimeExpression time) {
		if (time.getTokenSpan().getSentenceIndex() != event.getTokenSpan().getSentenceIndex() ||
			!time.getTokenSpan().getDocument().getName().equals(event.getTokenSpan().getDocument().getName()))
			return null;
		
		if (time.getValue().getType() == NormalizedTimeValue.Type.PART_OF_YEAR)
			return null;
		
		TokenSpan eventSpan = event.getTokenSpan();
		if (eventSpan.getLength() > 1)
			return null;
		
		DocumentNLP document = eventSpan.getDocument();
		int sentenceIndex = event.getTokenSpan().getSentenceIndex();
		int eventIndex = eventSpan.getStartTokenIndex();
		int timeIndex = time.getTokenSpan().getStartTokenIndex();
		DependencyParse.DependencyPath path = document.getDependencyParse(sentenceIndex).getPath(eventIndex, timeIndex);

		PoSTag eventTag = document.getPoSTag(eventSpan.getSentenceIndex(), eventIndex);
		if (!PoSTagClass.classContains(PoSTagClass.VB, eventTag))
			return null;
		
		int eventToTimexDist = getDistance(event.getTokenSpan(), time.getTokenSpan());
		
		boolean eventBeforeTime = timeIndex - eventIndex >= 0;
		boolean eventGovernsTime = path != null && path.getDependencyLength() == 1 && path.isAllGoverning();
		
		TimeMLRelType etRel = null;
		if (eventBeforeTime && eventToTimexDist <= MAX_DISTANCE) { // Timex after event adjacent
			if (eventToTimexDist == 0)
				etRel = TimeMLRelType.IS_INCLUDED;
			else if (timeIndex > 0 && document.getPoSTag(sentenceIndex, timeIndex - 1) == PoSTag.IN)
				etRel = getTypeFromPreposition(document.getTokenStr(sentenceIndex, timeIndex - 1));
			else 
				etRel = TimeMLRelType.IS_INCLUDED;
		/* NOTE: This bit was left out of CAEVO because it makes performance alot worse
		} else if (!eventBeforeTime && eventToTimexDist <= MAX_DISTANCE) { // Event after timex adjacent
			if (!eventGovernsTime)
				etRel = TimeMLRelType.VAGUE;
			else if (time.getValue().getReference() == Reference.PRESENT && event.getTimeMLAspect() == TimeMLAspect.PROGRESSIVE)
				etRel = TimeMLRelType.INCLUDES;
			else if (time.getValue().getReference() == Reference.PRESENT)
				etRel = TimeMLRelType.VAGUE;
			else if (eventToTimexDist == 0)
				etRel = TimeMLRelType.IS_INCLUDED;
			else if (timeIndex > 1 && document.getTokenStr(sentenceIndex, timeIndex - 1).toLowerCase().equals("said"))
				etRel = TimeMLRelType.VAGUE;
			else 
				etRel = TimeMLRelType.IS_INCLUDED;*/
		} else if (eventGovernsTime) { // Event governs time
			if (time.getValue().getReference() == Reference.PRESENT && event.getTimeMLAspect() == TimeMLAspect.PROGRESSIVE)
				etRel = TimeMLRelType.INCLUDES;
			else if (time.getValue().getReference() == Reference.PRESENT)
				etRel = TimeMLRelType.VAGUE;
			else 
				etRel = TimeMLRelType.IS_INCLUDED;
		} else if (path != null && path.getDependencyLength() == 1 && path.isAllGovernedBy()) { // Timex governs event
			etRel = TimeMLRelType.IS_INCLUDED;
		}
		
		return etRel;
	}
	
	private static TimeMLRelType getTypeFromPreposition(String text) {
		text = text.toLowerCase();
		
		if (text.equals("in")) {
			return TimeMLRelType.IS_INCLUDED;
		} else if (text.equals("on")) {
			return TimeMLRelType.IS_INCLUDED;
		} else if (text.equals("for")) {
			return TimeMLRelType.SIMULTANEOUS;
		} else if (text.equals("at")) {
			return TimeMLRelType.SIMULTANEOUS;
		} else if (text.equals("by"))  {
			return TimeMLRelType.VAGUE;
		} else if (text.equals("over")) {
			return TimeMLRelType.IS_INCLUDED;
		} else if (text.equals("during")) {
			return TimeMLRelType.IS_INCLUDED;
		} else if (text.equals("throughout")) {
			return TimeMLRelType.SIMULTANEOUS;
		} else if (text.equals("within")) {
			return TimeMLRelType.IS_INCLUDED;
		} else if (text.equals("until")) {
			return TimeMLRelType.BEFORE;
		} else if (text.equals("from")) {
			return TimeMLRelType.AFTER;
		} else if (text.equals("after")) {
			return TimeMLRelType.AFTER;
		} else {
			return TimeMLRelType.IS_INCLUDED;
		}
	}
	
	private static int getDistance(TokenSpan span1, TokenSpan span2) {
		return new TokenSpan(
				span1.getDocument(), 
				span1.getSentenceIndex(),
				Math.min(span1.getEndTokenIndex(), span2.getEndTokenIndex()),
				Math.max(span1.getStartTokenIndex(), span2.getStartTokenIndex())
			).getLength();
	}
}
