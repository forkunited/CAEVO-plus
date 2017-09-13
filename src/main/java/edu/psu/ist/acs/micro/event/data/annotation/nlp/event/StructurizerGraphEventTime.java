package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.StructurizerGraph;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelation;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationUnary;

public class StructurizerGraphEventTime<L> extends StructurizerGraph<EventTimeDatum<L>, L> {
	public StructurizerGraphEventTime() {
		super();
	}
	
	public StructurizerGraphEventTime(DatumContext<EventTimeDatum<L>, L> context) {
		super(context);
	}
	
	@Override
	protected WeightedStructureRelation makeDatumStructure(EventTimeDatum<L> datum, L label) {
		if (label == null)
			return null;
		
		WeightedStructureRelationBinary binaryRel = (WeightedStructureRelationBinary)this.context.getDatumTools().getDataTools().makeWeightedStructure(label.toString(), this.context);
		return new WeightedStructureRelationBinary(
				label.toString(), 
				this.context, 
				String.valueOf(datum.getId()), 
				new WeightedStructureRelationUnary("O", this.context, datum.getEvent().getId()),
				new WeightedStructureRelationUnary("O", this.context, datum.getTime().getId()),
				binaryRel.isOrdered());
	}

	@Override
	protected String getStructureId(EventTimeDatum<L> datum) {
		return "0";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List<WeightedStructureRelation> getDatumRelations(EventTimeDatum<L> datum, WeightedStructureGraph graph) {
		return (List<WeightedStructureRelation>)(List)graph.getEdges(datum.getEvent().getId(), datum.getTime().getId());
	}

	@Override
	public Structurizer<EventTimeDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<EventTimeDatum<L>, L> context) {
		return new StructurizerGraphEventTime<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphEventTime";
	}
	
	@Override
	public DataSet<EventTimeDatum<L>, L> makeData(DataSet<EventTimeDatum<L>, L> existingData, Map<String, WeightedStructureGraph> structures) {
		Set<String> validClusterRelations = new TreeSet<>();
		validClusterRelations.add("COREF");
		
		Map<String, Event> newEvents = new HashMap<String, Event>();
		DataSet<EventTimeDatum<L>, L> newData = new DataSet<>(this.context.getDatumTools());
		Map<String, Set<String>> newIdPairs = new HashMap<>();
		
		Map<String, Event> oldEvents = new HashMap<String, Event>();
		for (EventTimeDatum<L> datum : existingData) {
			oldEvents.put(datum.getEvent().getId(), datum.getEvent());
		}
		
		for (EventTimeDatum<L> datum : existingData) {
			Event e = datum.getEvent();
			LinkableNormalizedTimeValue t = datum.getTime();
			Event newE = (newEvents.containsKey(e.getId())) ? newEvents.get(e.getId()) : makeEventFromStructure(e, oldEvents, datum, structures, newEvents, validClusterRelations);
			
			if (newIdPairs.containsKey(newE.getId()) && newIdPairs.get(newE.getId()).contains(t.getId()))
				continue;
			
			if (!newIdPairs.containsKey(newE.getId()))
				newIdPairs.put(newE.getId(), new HashSet<String>());
			newIdPairs.get(newE.getId()).add(t.getId());
			
			newData.add(new EventTimeDatum<L>(this.context.getDataTools().getIncrementId(), newE, t, datum.getLabel()));
		}
	
		return newData;
	}
	
	private Event makeEventFromStructure(Event oldEvent, Map<String, Event> oldEvents, EventTimeDatum<L> oldDatum, Map<String, WeightedStructureGraph> structures, Map<String, Event> newEvents, Set<String> validClusterRelations) {
		String structureId = getStructureId(oldDatum);
		if (structureId == null || !structures.containsKey(structureId)) {
			newEvents.put(oldEvent.getId(), oldEvent);
			return oldEvent;
		}
		
		WeightedStructureGraph graph = structures.get(structureId);
		if (!graph.hasEdgeFrom(oldEvent.getId())) {
			newEvents.put(oldEvent.getId(), oldEvent);
			return oldEvent;
		}
		
		Set<String> cluster = graph.getClusterIds(oldEvent.getId(), validClusterRelations);
		List<StoreReference> mentionRefs = new ArrayList<>();
		Map<String, StoreReference> mentionRefMap = new HashMap<>();
		Event newEvent = new Event(this.context.getDataTools(), null, oldEvent.getId(), null, null, null, null, mentionRefs);
		
		for (String clusterEventId : cluster) {
			if (!oldEvents.containsKey(clusterEventId))
				continue;
			Event oldClusterEvent = oldEvents.get(clusterEventId);
			for (int i = 0; i < oldClusterEvent.getSomeMentionCount(); i++) {
				EventMention mention = oldClusterEvent.getSomeMention(i);
				mentionRefMap.put(mention.getId(), mention.getStoreReference());
			}
			
			newEvents.put(clusterEventId, newEvent);
		}
		
		mentionRefs.addAll(mentionRefMap.values());
		
		return newEvent;
	}
}
