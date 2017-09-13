package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;

public class StructurizerGraphEventPairByDocument<L> extends StructurizerGraphEventPair<L> {
	public StructurizerGraphEventPairByDocument() {
		super();
	}
	
	public StructurizerGraphEventPairByDocument(DatumContext<EventPairDatum<L>, L> context) {
		super(context);
	}

	@Override
	protected String getStructureId(EventPairDatum<L> datum) {
		String id = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getSource().getSomeMention(0).getTokenSpan().getDocument().getName());
		String targetName = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getTarget().getSomeMention(0).getTokenSpan().getDocument().getName());
		if (!targetName.equals(id)) {
			if (id.compareTo(targetName) < 0)
				id = id + "_" + targetName;
			else 
				id = targetName + "_" + id;
		}
		return id;
	}

	@Override
	public Structurizer<EventPairDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<EventPairDatum<L>, L> context) {
		return new StructurizerGraphEventPairByDocument<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphEventPairByDocument";
	}
}
