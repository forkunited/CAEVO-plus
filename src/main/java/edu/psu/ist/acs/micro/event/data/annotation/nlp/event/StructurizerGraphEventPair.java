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

public class StructurizerGraphEventPair<L> extends StructurizerGraph<EventPairDatum<L>, L> {
	public StructurizerGraphEventPair() {
		super();
	}
	
	public StructurizerGraphEventPair(DatumContext<EventPairDatum<L>, L> context) {
		super(context);
	}
	
	@Override
	protected WeightedStructureRelation makeDatumStructure(EventPairDatum<L> datum, L label) {
		if (label == null)
			return null;
		
		WeightedStructureRelationBinary binaryRel = (WeightedStructureRelationBinary)this.context.getDatumTools().getDataTools().makeWeightedStructure(label.toString(), this.context);
		return new WeightedStructureRelationBinary(
				label.toString(), 
				this.context, 
				String.valueOf(datum.getId()), 
				new WeightedStructureRelationUnary("O", this.context, datum.getSource().getId()),
				new WeightedStructureRelationUnary("O", this.context, datum.getTarget().getId()),
				binaryRel.isOrdered());
	}

	@Override
	protected String getStructureId(EventPairDatum<L> datum) {
		return "0";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List<WeightedStructureRelation> getDatumRelations(EventPairDatum<L> datum, WeightedStructureGraph graph) {
		return (List<WeightedStructureRelation>)(List)graph.getEdges(datum.getSource().getId(), datum.getTarget().getId());
	}

	@Override
	public Structurizer<EventPairDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<EventPairDatum<L>, L> context) {
		return new StructurizerGraphEventPair<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphEventPair";
	}

	@Override
	public DataSet<EventPairDatum<L>, L> makeData(DataSet<EventPairDatum<L>, L> existingData, Map<String, WeightedStructureGraph> structures) {
		Set<String> validClusterRelations = new TreeSet<>();
		validClusterRelations.add("COREF");
		
		Map<String, Event> newEvents = new HashMap<String, Event>();
		DataSet<EventPairDatum<L>, L> newData = new DataSet<>(this.context.getDatumTools());
		Map<String, Set<String>> newIdPairs = new HashMap<>();
		
		Map<String, Event> oldEvents = new HashMap<String, Event>();
		for (EventPairDatum<L> datum : existingData) {
			oldEvents.put(datum.getSource().getId(), datum.getSource());
			oldEvents.put(datum.getTarget().getId(), datum.getTarget());
		}
		
		for (EventPairDatum<L> datum : existingData) {
			Event e1 = datum.getSource();
			Event e2 = datum.getTarget();
			Event newE1 = (newEvents.containsKey(e1.getId())) ? newEvents.get(e1.getId()) : makeEventFromStructure(e1, oldEvents, datum, structures, newEvents, validClusterRelations);
			Event newE2 = (newEvents.containsKey(e2.getId())) ? newEvents.get(e2.getId()) : makeEventFromStructure(e2, oldEvents, datum, structures, newEvents, validClusterRelations);
			
			if (newIdPairs.containsKey(newE1.getId()) && newIdPairs.get(newE1.getId()).contains(newE2.getId()))
				continue;
			
			if (!newIdPairs.containsKey(newE1.getId()))
				newIdPairs.put(newE1.getId(), new HashSet<String>());
			newIdPairs.get(newE1.getId()).add(newE2.getId());
			
			newData.add(new EventPairDatum<L>(this.context.getDataTools().getIncrementId(), newE1, newE2, datum.getLabel()));
		}
	
		return newData;
	}
	
	private Event makeEventFromStructure(Event oldEvent, Map<String, Event> oldEvents, EventPairDatum<L> oldDatum, Map<String, WeightedStructureGraph> structures, Map<String, Event> newEvents, Set<String> validClusterRelations) {
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
