package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;

public class StructurizerGraphTLinkByDocument<L> extends StructurizerGraphTLink<L> {
	public StructurizerGraphTLinkByDocument() {
		super();
	}
	
	public StructurizerGraphTLinkByDocument(DatumContext<TLinkDatum<L>, L> context) {
		super(context);
	}

	@Override
	protected String getStructureId(TLinkDatum<L> datum) {
		TLink tlink = datum.getTLink();
		String id = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
				tlink.getSource().getTokenSpan().getDocument().getName());
		String targetName = DataSetBuilderDocumentFiltered.getDocumentNameWithoutPost(
						 tlink.getTarget().getTokenSpan().getDocument().getName());
		if (!targetName.equals(id)) {
			if (id.compareTo(targetName) < 0)
				id = id + "_" + targetName;
			else 
				id = targetName + "_" + id;
		}
		return id;
	}

	@Override
	public Structurizer<TLinkDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<TLinkDatum<L>, L> context) {
		return new StructurizerGraphTLinkByDocument<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphTLinkByDocument";
	}
}
