package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.Calendar;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.AnnotationTypeNLPEvent;


public class LinkableTimeExpression extends TimeExpression implements TLinkable, MentionArgumentable {

	public LinkableTimeExpression(DataTools dataTools) {
		super(dataTools);
	}
	
	public LinkableTimeExpression(DataTools dataTools, StoreReference reference) {
		super(dataTools, reference);
	}
	
	public LinkableTimeExpression(DataTools dataTools, 
						  StoreReference reference,
						  TokenSpan tokenSpan,
						  String id,
						  String sourceId,
						  TimeMLType timeMLType,
						  StoreReference startTimeReference,
						  StoreReference endTimeReference,
						  String quant,
						  String freq,
						  StoreReference valueReference,
						  TimeMLDocumentFunction timeMLDocumentFunction,
						  boolean temporalFunction,
						  StoreReference anchorTimeReference,
						  StoreReference valueFromFunctionReference,
						  TimeMLMod timeMLMod) {
		super(dataTools, reference, tokenSpan, id, sourceId,
				timeMLType, startTimeReference, endTimeReference,
				quant, freq, valueReference, timeMLDocumentFunction, temporalFunction,
				anchorTimeReference, valueFromFunctionReference,
				timeMLMod);
	}
	
	public TLinkable.Type getTLinkableType() {
		return TLinkable.Type.TIME;
	}
	
	public LinkableTimeExpression getStartTime() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.startTimeReference, true);
	}
	
	public LinkableTimeExpression getEndTime() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.endTimeReference, true);
	}
	
	/**
	 * @return a NormalizedTimeValue representing the 
	 * grounded time-interval referenced by the Time
	 * expression
	 */
	public LinkableNormalizedTimeValue getValue() {
		LinkableNormalizedTimeValue value = this.dataTools.getStoredItemSetManager().resolveStoreReference(this.valueReference, true);
		
		if (this.timeMLDocumentFunction == TimeMLDocumentFunction.CREATION_TIME) {
			if (FORCE_DATE_DCT) {
				LinkableNormalizedTimeValue dateValue = value.toDate();
				if (dateValue != null)
					value = dateValue;
			}
		} 
		
		return value;
	}
	
	public LinkableTimeExpression getAnchorTime() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.anchorTimeReference, true);
	}
	
	public LinkableTimeExpression getValueFromFunction() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.valueFromFunctionReference, true);
	}
	
	/**
	 * @param time
	 * @return TLink relation type between this Time and the given
	 * time according to their grounded time intervals.  
	 */
	public TLink.TimeMLRelType getRelationToTime(LinkableTimeExpression time) {
		return getRelationToTime(time, true);
	}
	
	public TLink.TimeMLRelType getRelationToTime(LinkableTimeExpression time, boolean unknownVague) {
		LinkableNormalizedTimeValue thisValue = getValue();
		LinkableNormalizedTimeValue timeValue = time.getValue();
		if (thisValue == null || timeValue == null)
			return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
		if (this.timeMLType != LinkableTimeExpression.TimeMLType.DATE && this.timeMLType != LinkableTimeExpression.TimeMLType.TIME)
			return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
		if (time.timeMLType != LinkableTimeExpression.TimeMLType.DATE && time.timeMLType != LinkableTimeExpression.TimeMLType.TIME)
			return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
		
		DocumentNLP timeDocument = time.getTokenSpan().getDocument();
		LinkableTimeExpression timeCreationTime = null;
		if (timeDocument.hasAnnotationType(AnnotationTypeNLPEvent.CREATION_TIME))
			timeCreationTime = timeDocument.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME);
		
		DocumentNLP thisDocument = getTokenSpan().getDocument();
		LinkableTimeExpression thisCreationTime = null;
		if (thisDocument.hasAnnotationType(AnnotationTypeNLPEvent.CREATION_TIME))
			thisCreationTime = thisDocument.getDocumentAnnotation(AnnotationTypeNLPEvent.CREATION_TIME);

		if (thisValue.getReference() != LinkableNormalizedTimeValue.Reference.NONE 
				|| timeValue.getReference() != LinkableNormalizedTimeValue.Reference.NONE) {
			if (thisCreationTime == null || timeCreationTime == null 
					|| thisCreationTime.getValue().getReference() != LinkableNormalizedTimeValue.Reference.NONE
					|| timeCreationTime.getValue().getReference() != LinkableNormalizedTimeValue.Reference.NONE)
				return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			
			int thisCtTimeCt = 0;
			if (!thisDocument.getName().equals(timeDocument.getName())) {
				TLink.TimeMLRelType ctRel = thisCreationTime.getRelationToTime(timeCreationTime);
				if (ctRel == TLink.TimeMLRelType.BEFORE)
					thisCtTimeCt = -1;
				else if (ctRel == TLink.TimeMLRelType.AFTER)
					thisCtTimeCt = 1;
				else if (ctRel == TLink.TimeMLRelType.SIMULTANEOUS
						|| ctRel == TLink.TimeMLRelType.INCLUDES
						|| ctRel == TLink.TimeMLRelType.IS_INCLUDED)
					thisCtTimeCt = 0;
				else 
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			}
			
			
			// Relate this to creation and time to creation based on past
			// and future references
			int thisCt = 0, timeCt = 0; 
			
			if (thisValue.getReference() != LinkableNormalizedTimeValue.Reference.NONE) {
				if (thisValue.getReference() == LinkableNormalizedTimeValue.Reference.FUTURE)
					thisCt = 1;
				else if (thisValue.getReference() == LinkableNormalizedTimeValue.Reference.PAST)
					thisCt = -1;
				else
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			} else {
				TLink.TimeMLRelType thisCtRelation = getRelationToTime(thisCreationTime);
				if (thisCtRelation == TLink.TimeMLRelType.VAGUE)
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
				else if (thisCtRelation == TLink.TimeMLRelType.BEFORE)
					thisCt = -1;
				else if (thisCtRelation == TLink.TimeMLRelType.AFTER)
					thisCt = 1;
				/*else if (getTimeMLDocumentFunction() == TimeMLDocumentFunction.CREATION_TIME &&
						(thisCtRelation == TLink.TimeMLRelType.SIMULTANEOUS || thisCtRelation == TLink.TimeMLRelType.IS_INCLUDED))
					thisCt = 0;*/
				else
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			}
			
			if (timeValue.getReference() != LinkableNormalizedTimeValue.Reference.NONE) {
				if (timeValue.getReference() == LinkableNormalizedTimeValue.Reference.FUTURE)
					timeCt = 1;
				else if (timeValue.getReference() == LinkableNormalizedTimeValue.Reference.PAST)
					timeCt = -1;
				else
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			} else {
				TLink.TimeMLRelType timeCtRelation = time.getRelationToTime(timeCreationTime);
				if (timeCtRelation == TLink.TimeMLRelType.VAGUE)
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
				else if (timeCtRelation == TLink.TimeMLRelType.BEFORE)
					timeCt = -1;
				else if (timeCtRelation == TLink.TimeMLRelType.AFTER)
					timeCt = 1;
				/*else if (time.getTimeMLDocumentFunction() == TimeMLDocumentFunction.CREATION_TIME &&
							(timeCtRelation == TLink.TimeMLRelType.SIMULTANEOUS || timeCtRelation == TLink.TimeMLRelType.IS_INCLUDED))
					timeCt = 0;*/
				else
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			}
			
			if (thisCtTimeCt == 0) {
				if (thisCt < timeCt)
					return TLink.TimeMLRelType.BEFORE;
				else if (timeCt < thisCt)
					return TLink.TimeMLRelType.AFTER;
				else 
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			} else if (thisCtTimeCt < 0) {
				if (thisCt <= 0 && timeCt >= 0)
					return TLink.TimeMLRelType.BEFORE;
				else 
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			} else {
				if (timeCt <= 0 && thisCt >= 0)
					return TLink.TimeMLRelType.AFTER;
				else 
					return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
			}
		}
		
		Pair<Calendar, Calendar> thisInterval = thisValue.getRange();
		Pair<Calendar, Calendar> timeInterval = timeValue.getRange();
		
		if (thisInterval == null || timeInterval == null)
			return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
		
		int startStart = thisInterval.getFirst().compareTo(timeInterval.getFirst());
		int startEnd = thisInterval.getFirst().compareTo(timeInterval.getSecond());
		int endStart = thisInterval.getSecond().compareTo(timeInterval.getFirst());
		int endEnd = thisInterval.getSecond().compareTo(timeInterval.getSecond());
		
		if (startStart == 0 && endEnd == 0)
			return TLink.TimeMLRelType.SIMULTANEOUS;
		else if (endStart <= 0)
			return TLink.TimeMLRelType.BEFORE;
		else if (startEnd >= 0)
			return TLink.TimeMLRelType.AFTER;
		else if (startStart < 0 && endEnd > 0)
			return TLink.TimeMLRelType.INCLUDES;
		else if (startStart > 0 && endEnd < 0)
			return TLink.TimeMLRelType.IS_INCLUDED;
		else if (startStart > 0 && startEnd < 0 && endEnd > 0)
			return TLink.TimeMLRelType.OVERLAPPED_BY;
		else if (startStart < 0 && endStart > 0 && endEnd < 0)
			return TLink.TimeMLRelType.OVERLAPS;
		else
			return (unknownVague) ? TLink.TimeMLRelType.VAGUE : null;
	}

	@Override
	public MentionArgumentable.Type getMentionArgumentableType() {
		return MentionArgumentable.Type.TIME_EXPRESSION;
	}

	@Override
	public Argumentable getArgumentable() {
		return getValue();
	}
	
	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new LinkableTimeExpression(this.dataTools, reference);
	}
}
