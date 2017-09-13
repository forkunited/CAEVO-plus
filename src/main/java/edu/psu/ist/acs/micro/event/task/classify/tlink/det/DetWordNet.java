package edu.psu.ist.acs.micro.event.task.classify.tlink.det;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.WordNet;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;

public class DetWordNet {
	public static TimeMLRelType determineRelation(TLinkable o1, TLinkable o2, WordNet wordNet) {
		if (o1.getTLinkableType() != o2.getTLinkableType())
			return null;
		
		TokenSpan sourceSpan = o1.getTokenSpan();
		TokenSpan targetSpan = o2.getTokenSpan();
		if (sourceSpan.getSentenceIndex() < 0 || targetSpan.getSentenceIndex() < 0)
			return null;
		
		DocumentNLP sourceDocument = sourceSpan.getDocument();
		DocumentNLP targetDocument = targetSpan.getDocument();

		String sourceWord = sourceDocument.getTokenStr(sourceSpan.getSentenceIndex(), sourceSpan.getEndTokenIndex() - 1);
		String targetWord = targetDocument.getTokenStr(targetSpan.getSentenceIndex(), targetSpan.getEndTokenIndex() - 1);
		PoSTag sourcePos = sourceDocument.getPoSTag(sourceSpan.getSentenceIndex(), sourceSpan.getEndTokenIndex() - 1);
		PoSTag targetPos = targetDocument.getPoSTag(targetSpan.getSentenceIndex(), targetSpan.getEndTokenIndex() - 1);
		String sourceLemma = wordNet.getLemma(sourceWord, sourcePos);
		String targetLemma = wordNet.getLemma(targetWord, targetPos);
		
		TimeMLRelType rel = null;
		if (o1.getTLinkableType() == TLinkable.Type.TIME) {
			if (sourceLemma != null && sourceLemma.equals(targetLemma))
				rel = TimeMLRelType.SIMULTANEOUS;
		} else { // EVENT_EVENT
			if (wordNet.areSynonyms(sourceWord, sourcePos, targetWord, targetPos))
				rel = TimeMLRelType.VAGUE;
			else if (sourceLemma != null && sourceLemma.equals(targetLemma))
				rel = TimeMLRelType.VAGUE;
		}
		
		if (rel != null) {
			return rel;
		} else {
			return null;
		}
	}
}
