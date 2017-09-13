package edu.psu.ist.acs.micro.event.task.classify.ecoref.det;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.PoSTag;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.WordNet;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.CorefRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLClass;

public class DetWordNetLemma {
	public static CorefRelType determineRelation(EventMention e1, EventMention e2, WordNet wordNet) {
		if (e1.getTimeMLClass() == TimeMLClass.REPORTING || e2.getTimeMLClass() == TimeMLClass.REPORTING)
			return null;
		
		TokenSpan s1 = e1.getTokenSpan();
		TokenSpan s2 = e2.getTokenSpan();
			
		DocumentNLP d1 = s1.getDocument();
		DocumentNLP d2 = s2.getDocument();

		String sourceWord = d1.getTokenStr(s1.getSentenceIndex(), s1.getEndTokenIndex() - 1);
		String targetWord = d2.getTokenStr(s2.getSentenceIndex(), s2.getEndTokenIndex() - 1);
		PoSTag sourcePos = d1.getPoSTag(s1.getSentenceIndex(), s1.getEndTokenIndex() - 1);
		PoSTag targetPos = d2.getPoSTag(s2.getSentenceIndex(), s2.getEndTokenIndex() - 1);
		String sourceLemma = wordNet.getLemma(sourceWord, sourcePos);
		String targetLemma = wordNet.getLemma(targetWord, targetPos);
		
		if (sourceLemma != null && targetLemma != null && sourceLemma.equals(targetLemma))
			return CorefRelType.COREF;
		else
			return null;
	}
}
