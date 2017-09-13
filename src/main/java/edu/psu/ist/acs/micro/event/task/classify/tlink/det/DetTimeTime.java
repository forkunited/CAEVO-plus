package edu.psu.ist.acs.micro.event.task.classify.tlink.det;

import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;

public class DetTimeTime {
	public static TimeMLRelType determineRelation(LinkableTimeExpression t1, LinkableTimeExpression t2) {
		TimeMLRelType rel = t1.getRelationToTime(t2, false);
		if (rel != null) {
			return rel;
		} else {
			return null;
		}
	}
}
