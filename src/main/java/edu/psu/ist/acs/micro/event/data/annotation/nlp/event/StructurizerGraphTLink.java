package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.List;
import java.util.Map;

import edu.cmu.ml.rtw.generic.data.annotation.DataSet;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.data.annotation.Datum.Tools.Structurizer;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.StructurizerGraph;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureGraph;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelation;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationBinary;
import edu.cmu.ml.rtw.generic.structure.WeightedStructureRelationUnary;

public class StructurizerGraphTLink<L> extends StructurizerGraph<TLinkDatum<L>, L> {
	public StructurizerGraphTLink() {
		super();
	}
	
	public StructurizerGraphTLink(DatumContext<TLinkDatum<L>, L> context) {
		super(context);
	}
	
	@Override
	protected WeightedStructureRelation makeDatumStructure(TLinkDatum<L> datum, L label) {
		if (label == null)
			return null;
		
		WeightedStructureRelationBinary binaryRel = (WeightedStructureRelationBinary)this.context.getDatumTools().getDataTools().makeWeightedStructure(label.toString(), this.context);
		return new WeightedStructureRelationBinary(
				label.toString(), 
				this.context, 
				String.valueOf(datum.getId()), 
				new WeightedStructureRelationUnary("O", this.context, datum.getTLink().getSource().getId()),
				new WeightedStructureRelationUnary("O", this.context, datum.getTLink().getTarget().getId()),
				binaryRel.isOrdered());
	}

	@Override
	protected String getStructureId(TLinkDatum<L> datum) {
		return "0";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List<WeightedStructureRelation> getDatumRelations(TLinkDatum<L> datum, WeightedStructureGraph graph) {
		return (List<WeightedStructureRelation>)(List)graph.getEdges(datum.getTLink().getSource().getId(), datum.getTLink().getTarget().getId());
	}

	@Override
	public Structurizer<TLinkDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<TLinkDatum<L>, L> context) {
		return new StructurizerGraphTLink<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphTLink";
	}

	@Override
	public DataSet<TLinkDatum<L>, L> makeData(
			DataSet<TLinkDatum<L>, L> existingData,
			Map<String, WeightedStructureGraph> structures) {
		throw new UnsupportedOperationException();
	}
}
