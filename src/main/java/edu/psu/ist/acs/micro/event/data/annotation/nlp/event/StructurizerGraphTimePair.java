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

public class StructurizerGraphTimePair<L> extends StructurizerGraph<TimePairDatum<L>, L> {
	public StructurizerGraphTimePair() {
		super();
	}
	
	public StructurizerGraphTimePair(DatumContext<TimePairDatum<L>, L> context) {
		super(context);
	}
	
	@Override
	protected WeightedStructureRelation makeDatumStructure(TimePairDatum<L> datum, L label) {
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
	protected String getStructureId(TimePairDatum<L> datum) {
		return "0";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List<WeightedStructureRelation> getDatumRelations(TimePairDatum<L> datum, WeightedStructureGraph graph) {
		return (List<WeightedStructureRelation>)(List)graph.getEdges(datum.getSource().getId(), datum.getTarget().getId());
	}

	@Override
	public Structurizer<TimePairDatum<L>, L, WeightedStructureGraph> makeInstance(DatumContext<TimePairDatum<L>, L> context) {
		return new StructurizerGraphTimePair<L>(context);
	}

	@Override
	public String getGenericName() {
		return "GraphTimePair";
	}

	@Override
	public DataSet<TimePairDatum<L>, L> makeData(
			DataSet<TimePairDatum<L>, L> existingData,
			Map<String, WeightedStructureGraph> structures) {
		return existingData;
	}
}
