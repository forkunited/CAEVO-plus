package edu.psu.ist.acs.micro.event.scratch;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.Context;
import edu.cmu.ml.rtw.generic.data.Serializer;
import edu.cmu.ml.rtw.generic.data.StoredItemSetInMemoryLazy;
import edu.cmu.ml.rtw.generic.data.annotation.Datum;
import edu.cmu.ml.rtw.generic.data.annotation.DatumContext;
import edu.cmu.ml.rtw.generic.parse.Obj;
import edu.cmu.ml.rtw.generic.task.classify.multi.MethodMultiClassification;
import edu.cmu.ml.rtw.generic.task.classify.multi.TaskMultiClassification;
import edu.cmu.ml.rtw.generic.util.OutputWriter;
import edu.psu.ist.acs.micro.event.data.EventDataTools;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLink.TimeMLRelType;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.TLinkDatum;
import edu.psu.ist.acs.micro.event.util.EventProperties;

public class TrainAndPredictTLinks {
	public static void main(String[] args) {
		String trainingCtxPath = args[0];
		String modelSubContextName = args[1];
		String modelName = args[2];
		
		EventDataTools dataTools = new EventDataTools(new OutputWriter(), new EventProperties(), 0);
		
		// Train model
		Context trainCtx = Context.run(dataTools, new File(trainingCtxPath));
		
		System.out.println("Finished training...");
		
		// Pull model from context
		MethodMultiClassification modelMethod = trainCtx.getMatchContext(Obj.curlyBracedValue(modelSubContextName))
												   		.getMatchMultiClassifyMethod(Obj.curlyBracedValue(modelName));
		
		System.out.println("Loading default CAEVO output...");
		
		// Load data
		@SuppressWarnings("unchecked")
		Datum.Tools<TLinkDatum<TimeMLRelType>, TimeMLRelType> datumTools = 
		((DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType>)trainCtx.getMatchContext(Obj.curlyBracedValue(modelSubContextName))).getDatumTools();

		DatumContext<TLinkDatum<TimeMLRelType>, TimeMLRelType> dataCtx = DatumContext.run(
			datumTools,
			new File("src/main/resources/contexts/tlinkType/data/LoadRaw.ctx"));
		TaskMultiClassification task = dataCtx.getMatchMultiClassifyTask(Obj.curlyBracedValue("task"));		
		
		System.out.println("Classifying default CAEVO output...");
		
		// Run model on data
		Map<Datum<?>, ?> results = modelMethod.classify(task).get(0);
		
		System.out.println("Outputting results...");
		
		// Output results
		Map<String, Serializer<?, ?>> serializers = dataTools.getSerializers();
		
		@SuppressWarnings("unchecked")
		StoredItemSetInMemoryLazy<TLink, TLink> storedTLinks = dataTools.getStoredItemSetManager()
								.getItemSet("RawEventBson", "plus_tlinks", true,
										    (Serializer<TLink, Document>)serializers.get("JSONBSONTLink"));

		
		for (Entry<Datum<?>, ?> entry : results.entrySet()) {
			@SuppressWarnings("unchecked")
			TLinkDatum<TimeMLRelType> datum = (TLinkDatum<TimeMLRelType>)entry.getKey();
			TLink tlink = datum.getTLink();
			TimeMLRelType label = (TimeMLRelType)entry.getValue();
			
			JSONObject jsonLink = tlink.toJSON();
			jsonLink.put("relation", label.toString());
			TLink newLink = new TLink(dataTools);
			newLink.fromJSON(jsonLink);
			
			storedTLinks.addItem(newLink);
		}
	}
}
