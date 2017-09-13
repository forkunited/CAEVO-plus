package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;

/**
 * TLink represents a TimeML TLink--a temporal
 * link between a pair of events/times.
 * 
 * See http://timeml.org/site/index.html for details.
 * 
 * @author Bill McDowell
 * 
 */
public class TLink implements StoredJSONSerializable {
	public enum Type {
		EVENT_EVENT,
		EVENT_TIME,
		TIME_TIME
	}
	
	public enum Position {
		DCT,
		WITHIN_SENTENCE,
		BETWEEN_SENTENCE,
		DCT_DCT,
		DCT_BETWEEN_DOCUMENT,
		BETWEEN_DOCUMENT
	}
	
	public enum TextDirection {
		FORWARD,
		BACKWARD,
		NONE
	}
	
	public enum TimeMLRelType {
		OVERLAPS,      // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		OVERLAPPED_BY, // Additional relation for transitivity (shown at http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
		
		OVERLAP, // TempEval2
		BEFORE_OR_OVERLAP, // TempEval2
		OVERLAP_OR_AFTER, // TempEval2
		
		BEFORE,
		AFTER,
		INCLUDES,
		IS_INCLUDED,
		DURING,
		SIMULTANEOUS,
		IAFTER,
		IBEFORE,
		IDENTITY,
		BEGINS,
		ENDS,
		BEGUN_BY,
		ENDED_BY,
		DURING_INV,
		VAGUE,
		
		MUTUAL_VAGUE,
		PARTIAL_VAGUE,
		NONE_VAGUE
	}
	
	// NOTE: Currently only for TimeBank-Dense relations + overlaps/overlapped_by
	// Each sub-array represents a rule of the form (r_1\wedge r_2)->(r_3\vee .. r_n)
	// The first array in each sub-array contain r_1 and r_2
	// The second array in each sub-array contsins r_3...r_n
	// null means nothing can be inferred (disjunction over all)
	private static final TimeMLRelType[][][] timeMLRelTypeCompositionRules = {
		{ { TimeMLRelType.BEFORE, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.INCLUDES }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.AFTER }, { null } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.BEFORE, TimeMLRelType.VAGUE }, { null } },
		
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED} },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.INCLUDES }, { null } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.IS_INCLUDED } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.INCLUDES, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.AFTER }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPS }, { TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.SIMULTANEOUS } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS }, { TimeMLRelType.OVERLAPS } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.OVERLAPPED_BY } },
		{ { TimeMLRelType.SIMULTANEOUS, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.AFTER, TimeMLRelType.BEFORE }, { null } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.INCLUDES }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.OVERLAPS }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.AFTER, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.OVERLAPS, TimeMLRelType.IS_INCLUDED, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.OVERLAPS } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.AFTER }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPS }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPS, TimeMLRelType.VAGUE }, { null } },

		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.BEFORE }, { TimeMLRelType.BEFORE, TimeMLRelType.OVERLAPS, TimeMLRelType.INCLUDES, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.IS_INCLUDED }, { TimeMLRelType.IS_INCLUDED, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.INCLUDES }, { TimeMLRelType.INCLUDES, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.SIMULTANEOUS }, { TimeMLRelType.OVERLAPPED_BY } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER }, { TimeMLRelType.AFTER } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.OVERLAPS }, { TimeMLRelType.INCLUDES, TimeMLRelType.IS_INCLUDED, TimeMLRelType.SIMULTANEOUS, TimeMLRelType.OVERLAPS, TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.OVERLAPPED_BY }, { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.AFTER, TimeMLRelType.VAGUE } },
		{ { TimeMLRelType.OVERLAPPED_BY, TimeMLRelType.VAGUE }, { null } },
		
		{ { TimeMLRelType.VAGUE, TimeMLRelType.BEFORE }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.IS_INCLUDED }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.INCLUDES }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.SIMULTANEOUS }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.AFTER }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.OVERLAPS }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.OVERLAPPED_BY }, { null } },
		{ { TimeMLRelType.VAGUE, TimeMLRelType.VAGUE }, { null } },
	};
	
	/**
	 * @returns an array representing TLink relation type 
	 * composition rules of the form: 
	 * 
	 * ((r(l')=t') and (r(l'')=t'')) implies or_i(r(l''')=t_i)
	 * 
	 * Where r(l)=t means that TLink l has relation t and
	 * 'or_i' is a disjunction over propositions indexed by i.
	 * These are the rules referred to as 'Transitivity' and
	 * 'Disjunctive Transitivity' in the 
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * Each rule is stored as a two-dimensional array of length 2.
	 * The first element of this array contains t' and t'', and
	 * the second element of the array contains t_i for each i.  
	 * 
	 */
	public static final TimeMLRelType[][][] getTimeMLRelTypeCompositionRules() {
		return TLink.timeMLRelTypeCompositionRules;
	}
	
	/**
	 * Represents converse rules for TimeML relationship-types. The
	 * rules are of the form:
	 * 
	 * (r(l')=t') implies (r(l'')=t'')
	 * 
	 * Where l' is a link in the reverse direction of l'' but
	 * between the same events/times, and 
	 * r(l)=t means that TLink l has relation t.
	 * These are the rules referred to as 'Converse' in the
	 * papers/TemporalOrderingNotes.pdf document.  They were 
	 * derived from Allen's interval algebra
	 * (see http://www.ics.uci.edu/~alspaugh/cls/shr/allen.html)
	 * 
	 * @param timeMLRelType (t')
	 * @return converse of timeMLRelType (t'')
	 *
	 */
	public static TimeMLRelType getConverseTimeMLRelType(TimeMLRelType timeMLRelType) {
		if (timeMLRelType == TimeMLRelType.OVERLAPS)
			return TimeMLRelType.OVERLAPPED_BY;
		else if (timeMLRelType == TimeMLRelType.OVERLAPPED_BY)
			return TimeMLRelType.OVERLAPS;
		else if (timeMLRelType == TimeMLRelType.OVERLAP)
			return TimeMLRelType.OVERLAP;
		else if (timeMLRelType == TimeMLRelType.BEFORE_OR_OVERLAP)
			return TimeMLRelType.OVERLAP_OR_AFTER;
		else if (timeMLRelType == TimeMLRelType.OVERLAP_OR_AFTER)
			return TimeMLRelType.BEFORE_OR_OVERLAP;
		else if (timeMLRelType == TimeMLRelType.BEFORE)
			return TimeMLRelType.AFTER;
		else if (timeMLRelType == TimeMLRelType.AFTER)
			return TimeMLRelType.BEFORE;
		else if (timeMLRelType == TimeMLRelType.INCLUDES)
			return TimeMLRelType.IS_INCLUDED;
		else if (timeMLRelType == TimeMLRelType.IS_INCLUDED)
			return TimeMLRelType.INCLUDES;
		else if (timeMLRelType == TimeMLRelType.DURING)
			return TimeMLRelType.DURING_INV;
		else if (timeMLRelType == TimeMLRelType.DURING_INV)
			return TimeMLRelType.DURING;
		else if (timeMLRelType == TimeMLRelType.SIMULTANEOUS)
			return TimeMLRelType.SIMULTANEOUS;
		else if (timeMLRelType == TimeMLRelType.IAFTER)
			return TimeMLRelType.IBEFORE;
		else if (timeMLRelType == TimeMLRelType.IDENTITY)
			return TimeMLRelType.IDENTITY;
		else if (timeMLRelType == TimeMLRelType.BEGINS)
			return TimeMLRelType.BEGUN_BY;
		else if (timeMLRelType == TimeMLRelType.BEGUN_BY)
			return TimeMLRelType.BEGINS;
		else if (timeMLRelType == TimeMLRelType.ENDS)
			return TimeMLRelType.ENDED_BY;
		else if (timeMLRelType == TimeMLRelType.ENDED_BY)
			return TimeMLRelType.ENDS;
		else
			return TimeMLRelType.VAGUE;
	}
	
	private String id;
	private String origin;
	private StoreReference sourceReference;
	private StoreReference targetReference;
	private TimeMLRelType timeMLRelType;
	private Signal signal;
	private String syntax;
	
	private EventDataTools dataTools;
	private StoreReference reference;
	
	public TLink(EventDataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public TLink(EventDataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	public TLink(EventDataTools dataTools, 
				 StoreReference reference, 
				 String id, 
				 String origin,
				 StoreReference sourceReference, 
				 StoreReference targetReference, 
				 TimeMLRelType timeMLRelType,
				 Signal signal,
				 String syntax) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.origin = origin;
		this.sourceReference = sourceReference;
		this.targetReference = targetReference;
		this.timeMLRelType = timeMLRelType;
		this.signal = signal;
		this.syntax = syntax;
	}
	
	public TLink getConverse() {
		return new TLink(this.dataTools,
						 null,
						 this.id,
						 this.origin,
						 this.targetReference,
						 this.sourceReference,
						 TLink.getConverseTimeMLRelType(this.timeMLRelType),
						 this.signal,
						 this.syntax
						 );
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getOrigin() {
		return this.origin;
	}
	
	public TLinkable getFirst() {
		TLinkable source = getSource();
		TLinkable target = getTarget();
		
		String sourceDoc = source.getTokenSpan().getDocument().getName();
		String targetDoc = target.getTokenSpan().getDocument().getName();
		int sourceSent = source.getTokenSpan().getSentenceIndex();
		int targetSent = target.getTokenSpan().getSentenceIndex();
		int sourceToken = source.getTokenSpan().getStartTokenIndex();
		int targetToken = target.getTokenSpan().getStartTokenIndex();
		int sourceEndToken = source.getTokenSpan().getEndTokenIndex();
		int targetEndToken = target.getTokenSpan().getEndTokenIndex();
		
		if (sourceDoc.compareTo(targetDoc) < 0)
			return source;
		else if (sourceDoc.compareTo(targetDoc) > 0)
			return target;
		else if (sourceSent < targetSent)
			return source;
		else if (sourceSent > targetSent)
			return target;
		else if (sourceToken < targetToken)
			return source;
		else if (sourceToken > targetToken)
			return target;
		else if (sourceEndToken < targetEndToken)
			return source;
		else if (sourceEndToken > targetEndToken)
			return target;
		else 
			return source;
	}
	
	public TLinkable getSecond() {
		TLinkable source = getSource();
		TLinkable target = getTarget();
		
		String sourceDoc = source.getTokenSpan().getDocument().getName();
		String targetDoc = target.getTokenSpan().getDocument().getName();
		int sourceSent = source.getTokenSpan().getSentenceIndex();
		int targetSent = target.getTokenSpan().getSentenceIndex();
		int sourceToken = source.getTokenSpan().getStartTokenIndex();
		int targetToken = target.getTokenSpan().getStartTokenIndex();
		int sourceEndToken = source.getTokenSpan().getEndTokenIndex();
		int targetEndToken = target.getTokenSpan().getEndTokenIndex();
		
		if (sourceDoc.compareTo(targetDoc) < 0)
			return target;
		else if (sourceDoc.compareTo(targetDoc) > 0)
			return source;
		else if (sourceSent < targetSent)
			return target;
		else if (sourceSent > targetSent)
			return source;
		else if (sourceToken < targetToken)
			return target;
		else if (sourceToken > targetToken)
			return source;
		else if (sourceEndToken < targetEndToken)
			return target;
		else if (sourceEndToken > targetEndToken)
			return source;
		else 
			return target;
	}
	
	public TLinkable getSource() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.sourceReference, true);
	}
	
	public TLinkable getTarget() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.targetReference, true);
	}
	
	public EventMention getFirstEvent() {
		TLinkable source = getSource();
		if (source.getTLinkableType() == TLinkable.Type.EVENT)
			return (EventMention)source;
		else {
			TLinkable target = getTarget();
			if (target.getTLinkableType() == TLinkable.Type.EVENT)
				return (EventMention)target;
		}
		
		return null;
	}
	
	public LinkableTimeExpression getFirstTime() {
		TLinkable source = getSource();
		if (source.getTLinkableType() == TLinkable.Type.TIME)
			return (LinkableTimeExpression)source;
		else {
			TLinkable target = getTarget();
			if (target.getTLinkableType() == TLinkable.Type.TIME)
				return (LinkableTimeExpression)target;
		}
		
		return null;
	}
	
	public Signal getSignal() {
		return this.signal;
	}
	
	public TimeMLRelType getTimeMLRelType() {
		return this.timeMLRelType;
	}
	
	public TimeMLRelType getConverseTimeMLRelType() {
		return TLink.getConverseTimeMLRelType(this.timeMLRelType);
	}
	
	public String getSyntax() {
		return this.syntax;
	}
	
	public boolean isBetweenDocuments() {
		Position p = getPosition();
		return p == Position.DCT_DCT || p == Position.DCT_BETWEEN_DOCUMENT || p == Position.BETWEEN_DOCUMENT;
	}
	
	public TextDirection getTextDirection() {
		if (isBetweenDocuments())
			return TextDirection.NONE;
		
		Position p = getPosition();
		if (p == Position.DCT)
			return TextDirection.NONE;
		
		TokenSpan sourceSpan = getSource().getTokenSpan();
		TokenSpan targetSpan = getTarget().getTokenSpan();
		
		if (sourceSpan.getSentenceIndex() < targetSpan.getSentenceIndex())
			return TextDirection.FORWARD;
		else if (sourceSpan.getSentenceIndex() > targetSpan.getSentenceIndex())
			return TextDirection.BACKWARD;
		else {
			if (sourceSpan.getStartTokenIndex() < targetSpan.getStartTokenIndex())
				return TextDirection.FORWARD;
			else if (sourceSpan.getStartTokenIndex() > targetSpan.getStartTokenIndex())
				return TextDirection.BACKWARD;
			else
				return TextDirection.NONE;
		}
	}
	
	public Position getPosition() {
		int sourceSentenceIndex = getSource().getTokenSpan().getSentenceIndex();
		int targetSentenceIndex = getTarget().getTokenSpan().getSentenceIndex();
		
		if (!getSource().getTokenSpan().getDocument().getName().equals(getTarget().getTokenSpan().getDocument().getName())) {
			if (sourceSentenceIndex < 0 && targetSentenceIndex < 0)
				return Position.DCT_DCT;
			else if (sourceSentenceIndex < 0 || targetSentenceIndex < 0)
				return Position.DCT_BETWEEN_DOCUMENT;
			else 
				return Position.BETWEEN_DOCUMENT;
		} else if (sourceSentenceIndex < 0 || targetSentenceIndex < 0)
			return Position.DCT;
		else if (sourceSentenceIndex != targetSentenceIndex)
			return Position.BETWEEN_SENTENCE;
		else 
			return Position.WITHIN_SENTENCE;
	}
	
	public Type getType() {
		TLinkable.Type sourceType = getSource().getTLinkableType();
		TLinkable.Type targetType = getTarget().getTLinkableType();
		
		if (sourceType == TLinkable.Type.EVENT && targetType == TLinkable.Type.EVENT)
			return Type.EVENT_EVENT;
		else if (sourceType == TLinkable.Type.TIME && targetType == TLinkable.Type.TIME)
			return Type.TIME_TIME;
		else
			return Type.EVENT_TIME;
	}
	
	private static final TokenSpan.Relation[] OVER_EVENT_RELATIONS = new TokenSpan.Relation[] { TokenSpan.Relation.CONTAINED_BY, TokenSpan.Relation.EQUAL, TokenSpan.Relation.CONTAINS };
	public boolean isOverEvent() {
		if (getPosition() != Position.WITHIN_SENTENCE)
			return false;
		
		TokenSpan source = getSource().getTokenSpan();
		TokenSpan target = getTarget().getTokenSpan();
		int startIndex = Math.min(source.getEndTokenIndex(), target.getEndTokenIndex());
		int endIndex = Math.max(source.getStartTokenIndex(), target.getStartTokenIndex());
		
		for (int i = startIndex; i < endIndex; i++) {
			List<Pair<TokenSpan, EventMention>> es = source.getDocument().getTokenSpanAnnotations(AnnotationTypeNLPEvent.EVENT_MENTION, new TokenSpan(source.getDocument(), source.getSentenceIndex(), i, i + 1), OVER_EVENT_RELATIONS);
			if (es != null && es.size() > 0)
				return true;
		}
		
		return false;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.origin != null)
				json.put("origin", this.origin);
			if (this.sourceReference != null)
				json.put("sourceRef", this.sourceReference.toJSON());
			if (this.targetReference != null)
				json.put("targetRef", this.targetReference.toJSON());
			if (this.signal != null)
				json.put("signal", this.signal.getStoreReference().toJSON());
			if (this.timeMLRelType != null)
				json.put("timeMLRelType", this.timeMLRelType.toString());
			if (this.syntax != null)
				json.put("syntax", this.syntax);
		} catch (JSONException e) {
			return null;
		}
		return json;
	}
	
	@Override
	public boolean fromJSON(JSONObject json) {		
		try {
			if (json.has("id")) {
				this.id = json.getString("id");
			}
			if (json.has("origin"))
				this.origin = json.getString("origin");
			if (json.has("sourceRef")) {
				this.sourceReference = StoreReference.makeFromJSON(json.getJSONObject("sourceRef"));
			}
			if (json.has("targetRef")) {
				this.targetReference = StoreReference.makeFromJSON(json.getJSONObject("targetRef"));
			}
			if (json.has("signal")) {
				StoreReference r = new StoreReference();
				if (!r.fromJSON(json.getJSONObject("signal")))
					return false;
				this.signal = this.dataTools.getStoredItemSetManager().resolveStoreReference(r, true);
			}
			if (json.has("timeMLRelType"))
				this.timeMLRelType = TimeMLRelType.valueOf(json.getString("timeMLRelType"));
			if (json.has("syntax"))
				this.syntax = json.getString("syntax");
		} catch (JSONException e) {
			return false; 
		}
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new TLink(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}
}
