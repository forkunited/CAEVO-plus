package edu.psu.ist.acs.micro.event.data.annotation.nlp;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.AnnotationTypeNLP.Target;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EntityMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.LinkableTimeExpression;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.RelationMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.ValueMention;

/**
 * 
 * AnnotationTypeNLPEvent represents types of annotations that
 * the micro-event project can add to NLP documents
 * 
 * @author Bill McDowell
 *
 */
public class AnnotationTypeNLPEvent {
	public static final AnnotationTypeNLP<LinkableTimeExpression> CREATION_TIME = new AnnotationTypeNLP<LinkableTimeExpression>("dct", LinkableTimeExpression.class, Target.DOCUMENT);
	public static final AnnotationTypeNLP<EventMention> EVENT_MENTION = new AnnotationTypeNLP<EventMention>("ev_mention", EventMention.class, Target.TOKEN_SPAN);
	public static final AnnotationTypeNLP<RelationMention> RELATION_MENTION = new AnnotationTypeNLP<RelationMention>("r_mention", RelationMention.class, Target.TOKEN_SPAN);
	public static final AnnotationTypeNLP<EntityMention> ENTITY_MENTION = new AnnotationTypeNLP<EntityMention>("en_mention", EntityMention.class, Target.TOKEN_SPAN);
	public static final AnnotationTypeNLP<ValueMention> VALUE_MENTION = new AnnotationTypeNLP<ValueMention>("v_mention", ValueMention.class, Target.TOKEN_SPAN);
	public static final AnnotationTypeNLP<LinkableTimeExpression> TIME_EXPRESSION = new AnnotationTypeNLP<LinkableTimeExpression>("timex", LinkableTimeExpression.class, Target.TOKEN_SPAN);
	public static final AnnotationTypeNLP<ACEDocumentType> ACE_DOCUMENT_TYPE = new AnnotationTypeNLP<ACEDocumentType>("ace_doc_type", ACEDocumentType.class, Target.DOCUMENT);
}
