package edu.psu.ist.acs.micro.event.task.classify.tlink.det;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLAspect;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLClass;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLTense;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;

public class DetReportingDCT {
	public static TimeMLRelType determineRelation(EventMention event, LinkableTimeExpression time) {
		if (!time.getValue().getValue().equals(time.getTokenSpan().getDocument().getDocumentAnnotation(AnnotationTypeNLP.CREATION_TIME).getValue().getValue()) 
				|| 
				!event.getTokenSpan().getDocument().getName().equals(time.getTokenSpan().getDocument().getName())
				||
				event.getTokenSpan().getSentenceIndex() != time.getTokenSpan().getSentenceIndex())
			return null;
		
		if (event.getTimeMLClass() == TimeMLClass.REPORTING 
				&& (event.getTimeMLTense() != TimeMLTense.PAST
				|| event.getTimeMLAspect() != TimeMLAspect.PERFECTIVE)) {
			return TimeMLRelType.IS_INCLUDED;
		} else {
			return null;
		}
	}
}
