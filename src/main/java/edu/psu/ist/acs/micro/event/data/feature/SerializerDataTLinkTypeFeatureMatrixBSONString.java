package edu.psu.ist.acs.micro.event.data.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.platanios.learn.math.matrix.Vector;
import org.platanios.learn.math.matrix.Vector.VectorElement;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.Serializer;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.time.TimeExpression;
import edu.cmu.ml.rtw.generic.data.feature.DataFeatureMatrix;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkable;

public class SerializerDataTLinkTypeFeatureMatrixBSONString extends Serializer<DataFeatureMatrix<?, ?>, String> {
	private List<Serializer.Index<DataFeatureMatrix<?, ?>>> indices;
	
	public SerializerDataTLinkTypeFeatureMatrixBSONString(DataTools dataTools) {
		this.indices = new ArrayList<Serializer.Index<DataFeatureMatrix<?, ?>>>();
		
		this.indices.add(new Serializer.Index<DataFeatureMatrix<?, ?>>() {
			@Override
			public String getField() {
				return "name";
			}

			@Override
			public Object getValue(DataFeatureMatrix<?, ?> item) {
				return item.getReferenceName();
			}
		});
	}

	@Override
	public String getName() {
		return "DataTLinkTypeFeatureMatrixBSONString";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String serialize(DataFeatureMatrix item) {
		StringBuilder str = new StringBuilder();
		
		for (Object o : item.getData()) {
			TLinkDatum d = (TLinkDatum)o;
			Vector v = item.getFeatureVocabularyValues(d, false);
			List<Integer> indices = new ArrayList<>();
			List<Double> values = new ArrayList<>();
			for (VectorElement e : v) {
				indices.add(e.index());
				values.add(e.value());
			}
			
			Map names = item.getFeatures().getFeatureVocabularyNamesForIndices(indices);
			
			Document bsonVector = new Document();
			for (int i = 0; i < indices.size(); i++) {
				int index = indices.get(i);
				String name = (String)names.get(index);
				double value = values.get(i);
				bsonVector.append(name, value);
			}
		
			//String labelStr = (d.getLabel() == null) ? "null" : d.getLabel().toString();
		
			String doc = d.getTLink().getSource().getTokenSpan().getDocument().getName();
			String sourceId = null;
			String targetId = null;
			if (d.getTLink().getSource().getTLinkableType() == TLinkable.Type.EVENT) {
				sourceId = ((EventMention)d.getTLink().getSource()).getSourceInstanceId();
			} else {
				sourceId = ((TimeExpression)d.getTLink().getSource()).getSourceId();
			}
			
			if (d.getTLink().getTarget().getTLinkableType() == TLinkable.Type.EVENT) {
				targetId = ((EventMention)d.getTLink().getTarget()).getSourceInstanceId();
			} else {
				targetId = ((TimeExpression)d.getTLink().getTarget()).getSourceId();
			}
			
			str.append(doc)
				.append("\t")
				.append(sourceId)
				.append("\t")
				.append(targetId)
				.append("\t")
				.append(bsonVector.toJson()).append("\n");
		}
		
		return str.toString();
	}

	@Override
	public DataFeatureMatrix<?, ?> deserialize(String object, StoreReference storeReference) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String serializeToString(DataFeatureMatrix<?, ?>  item) {
		return serialize(item);
	}

	@Override
	public DataFeatureMatrix<?, ?>  deserializeFromString(String str, StoreReference storeReference) {
		return deserialize(str, storeReference);
	}

	@Override
	public List<Serializer.Index<DataFeatureMatrix<?, ?>>> getIndices() {
		return this.indices;
	}

}
