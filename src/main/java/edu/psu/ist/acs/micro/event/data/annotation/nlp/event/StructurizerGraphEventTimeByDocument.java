package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;

public class StructurizerGraphEventTimeByDocument<L> extends StructurizerGraphEventTime<L> {
	public StructurizerGraphEventTimeByDocument() {
		super();
	}
	
	public StructurizerGraphEventTimeByDocument(DatumContext<EventTimeDatum<L>, L> context) {
		super(context);
	}

	@Override
	protected String getStructureId(EventTimeDatum<L> datum) {
		String id = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getEvent().getSomeMention(0).getTokenSpan().getDocument().getName());
		String targetName = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getTime().getSomeExpression(0).getTokenSpan().getDocument().getName());
		if (!targetName.equals(id)) {
			if (id.compareTo(targetName) < 0)
				id = id + "_" + targetName;
			else 
				id = targetName + "_" + id;
		}
		return id;
	}

	@Override
	public Structurizer<EventTimeDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<EventTimeDatum<L>, L> context) {
		return new StructurizerGraphEventTimeByDocument<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphEventTimeByDocument";
	}
}
