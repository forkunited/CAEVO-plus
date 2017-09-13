package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;

public class StructurizerGraphTimePairByDocument<L> extends StructurizerGraphTimePair<L> {
	public StructurizerGraphTimePairByDocument() {
		super();
	}
	
	public StructurizerGraphTimePairByDocument(DatumContext<TimePairDatum<L>, L> context) {
		super(context);
	}

	@Override
	protected String getStructureId(TimePairDatum<L> datum) {
		String id = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getSource().getSomeExpression(0).getTokenSpan().getDocument().getName());
		String targetName = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				datum.getTarget().getSomeExpression(0).getTokenSpan().getDocument().getName());
		if (!targetName.equals(id)) {
			if (id.compareTo(targetName) < 0)
				id = id + "_" + targetName;
			else 
				id = targetName + "_" + id;
		}
		return id;
	}

	@Override
	public Structurizer<TimePairDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<TimePairDatum<L>, L> context) {
		return new StructurizerGraphTimePairByDocument<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphTimePairByDocument";
	}
}
