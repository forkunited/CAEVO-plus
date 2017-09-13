package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DependencyParse;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DocumentNLP;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.DependencyParse.Dependency;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan.SerializationType;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;


/**
 * Time represents a TimeML Event instance.  In TimeBank,
 * this is represented by an EVENT and a MAKEINSTANCE for
 * that event (with an eid and an eiid identifier)
 * 
 * See http://timeml.org/site/index.html for details.
 * 
 * @author Bill McDowell
 * 
 */
public class EventMention implements TLinkable, MentionArgumentable {	
	public enum Attribute {
		TIMEML_TENSE,
		TIMEML_ASPECT,
		TIMEML_POLARITY,
		TIMEML_CLASS,
		TIMEML_POS,
		TIMEML_MOOD,
		TIMEML_VERB_FORM,
		MODALITY
	}
	
	public enum TimeMLTense {
		FUTURE,
		INFINITIVE,
		PAST,
		PASTPART,
		PRESENT,
		PRESPART,
		PASSIVE,
		NONE
	}
	
	public enum TimeMLAspect {
		PROGRESSIVE,
		
		IMPERFECTIVE, // Spanish only
		IMPERFECTIVE_PROGRESSIVE, // Spanish only
		
		PERFECTIVE,
		PERFECTIVE_PROGRESSIVE,
		NONE
	}
	
	public enum TimeMLPolarity {
		POS,
		NEG
	}
	
	public enum TimeMLClass {
		OCCURRENCE,
		PERCEPTION,
		REPORTING,
		ASPECTUAL,
        STATE,
        I_STATE, 
        I_ACTION,
        NONE
	}
	
	public enum TimeMLPoS {
		ADJECTIVE,
		NOUN,
		VERB,
		PREPOSITION,
		OTHER
	}
	
	public enum TimeMLMood {
		INDICATIVE,
		SUBJUNCTIVE,
		CONDITIONAL,
		IMPERATIVE,
		NONE
	}
	
	public enum TimeMLVerbForm {
		INFINITIVE,
		GERUNDIVE,
		PARTICIPLE,
		NONE
	}

	private String id;
	private TokenSpan tokenSpan;
	private String sourceId;
	private String sourceInstanceId;
	private Signal signal;
	private TimeMLTense timeMLTense;
	private TimeMLAspect timeMLAspect;
	private TimeMLPolarity timeMLPolarity = TimeMLPolarity.POS;
	private TimeMLClass timeMLClass;
	private TimeMLPoS timeMLPoS;
	private TimeMLMood timeMLMood;
	private TimeMLVerbForm timeMLVerbForm;
	private String modality;
	private String cardinality;
	
	private TokenSpan extent;
	private StoreReference eventReference;
	private List<Pair<StoreReference, String>> argumentReferences;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public EventMention(DataTools dataTools) {
		this.dataTools = dataTools;
		this.argumentReferences = new ArrayList<>();
	}
	
	public EventMention(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.argumentReferences = new ArrayList<>();
	}
	
	public EventMention(DataTools dataTools,
						StoreReference reference, 
						String id, 
						String sourceId, 
						String sourceInstanceId,
						TokenSpan tokenSpan,
						Signal signal,
						TimeMLTense timeMLTense, 
						TimeMLAspect timeMLAspect, 
						TimeMLClass timeMLClass, 
						TimeMLPolarity timeMLPolarity, 
						TimeMLMood timeMLMood, 
						TimeMLVerbForm timeMLVerbForm, 
						TimeMLPoS timeMLPoS,
						String modality,
						String cardinality,
						TokenSpan extent,
						StoreReference eventReference,
						List<Pair<StoreReference, String>> argumentReferences) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.sourceId = sourceId;
		this.sourceInstanceId = sourceInstanceId;
		this.signal = signal;
		this.tokenSpan = tokenSpan;
		this.timeMLTense = timeMLTense;
		this.timeMLAspect = timeMLAspect;
		this.timeMLClass = timeMLClass;
		this.timeMLPolarity = timeMLPolarity;
		this.timeMLMood = timeMLMood;
		this.timeMLVerbForm = timeMLVerbForm;
		this.timeMLPoS = timeMLPoS;
		this.modality = modality;
		this.cardinality = cardinality;
		this.extent = extent;
		this.eventReference = eventReference;
		this.argumentReferences = argumentReferences;
	}
	
	public String getAttributeString(Attribute attribute) {
		if (attribute == Attribute.TIMEML_TENSE && getTimeMLTense() != null) {
			return getTimeMLTense().toString();
		} else if (attribute == Attribute.TIMEML_ASPECT && getTimeMLAspect() != null)
			return getTimeMLAspect().toString();
		else if (attribute == Attribute.TIMEML_POLARITY && getTimeMLPolarity() != null)
			return getTimeMLPolarity().toString();
		else if (attribute == Attribute.TIMEML_CLASS && getTimeMLClass() != null)
			return getTimeMLClass().toString();
		else if (attribute == Attribute.TIMEML_POS && getTimeMLPoS() != null)
			return getTimeMLPoS().toString();
		else if (attribute == Attribute.TIMEML_MOOD && getTimeMLMood() != null)
			return getTimeMLMood().toString();
		else if (attribute == Attribute.TIMEML_VERB_FORM && getTimeMLVerbForm() != null)
			return getTimeMLVerbForm().toString();
		else if (attribute == Attribute.MODALITY && getModality() != null)
			return getModality();
		return "";
	}
	
	public TLinkable.Type getTLinkableType() {
		return TLinkable.Type.EVENT;
	}
	
	/**
	 * @return the event instance id (eiid) for this event
	 */
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	/**
	 * @return the event id (eid) for this event
	 */
	public String getSourceId() {
		return this.sourceId;
	}
	
	public String getSourceInstanceId() {
		return this.sourceInstanceId;
	}
	
	public Signal getSignal() {
		return this.signal;
	}
	
	public TimeMLTense getTimeMLTense() {
		return this.timeMLTense;
	}
	
	public TimeMLTense getTimeMLExtendedTense() {
		int eventIndex = this.tokenSpan.getStartTokenIndex();
		DocumentNLP document = this.tokenSpan.getDocument();
		DependencyParse depParse = document.getDependencyParse(this.tokenSpan.getSentenceIndex());
		if (depParse == null)
			return getTimeMLTense();
		
		List<Dependency> deps = depParse.getGovernedDependencies(eventIndex);
		for (Dependency dep : deps) {
			if (!dep.getType().equals("aux"))
				continue;
			String depTerm = document.getTokenStr(this.tokenSpan.getSentenceIndex(), dep.getDependentTokenIndex()).toLowerCase();
			if (depTerm.equals("would") || depTerm.equals("could") ||
				depTerm.equals("might") || depTerm.equals("may") ||
				depTerm.equals("should") || depTerm.equals("'d") ||
				depTerm.equals("will"))
				return TimeMLTense.FUTURE;
		}
		
		return getTimeMLTense();
	}
	
	public TimeMLAspect getTimeMLAspect() {
		return this.timeMLAspect;
	}
	
	public TimeMLPolarity getTimeMLPolarity() {
		return this.timeMLPolarity;
	}
	
	public TimeMLClass getTimeMLClass() {
		return this.timeMLClass;
	}
	
	/**
	 * @return part-of-speech according to TimeML (some
	 * versions of TempEval have their own PoS tags given
	 * in the data set)
	 */
	public TimeMLPoS getTimeMLPoS() {
		return this.timeMLPoS;
	}
	
	public TimeMLMood getTimeMLMood() {
		return this.timeMLMood;
	}
	
	public TimeMLVerbForm getTimeMLVerbForm() {
		return this.timeMLVerbForm;
	}
	
	public String getModality() {
		return this.modality;
	}
	
	public String getCardinality() {
		return this.cardinality;
	}
	
	public TokenSpan getExtent() {
		return this.extent;
	}
	
	public Event getEvent() {
		if (this.eventReference != null)
			return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.eventReference, true);
		else 
			return null;
	}
	
	public int getArgumentCount() {
		return this.argumentReferences.size();
	}
	
	public String getArgumentRole(int index) {
		return this.argumentReferences.get(index).getSecond();
	}
	
	public MentionArgumentable getArgument(String role) {
		for (int i = 0; i < getArgumentCount(); i++)
			if (this.argumentReferences.get(i).getSecond().equals(role))
				return getArgument(i);
		return null;
	}
	
	public MentionArgumentable getArgument(int index) {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.argumentReferences.get(index).getFirst(), true);
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.tokenSpan != null)
				json.put("tokenSpan", this.tokenSpan.toJSON(SerializationType.STORE_REFERENCE));
			if (this.sourceId != null)
				json.put("sourceId", this.sourceId);
			if (this.sourceInstanceId != null)
				json.put("sourceInstanceId", this.sourceInstanceId);
			if (this.signal != null)
				json.put("signal", this.signal.getStoreReference().toJSON()); 
			if (this.timeMLTense != null)
				json.put("timeMLTense", this.timeMLTense.toString());
			if (this.timeMLAspect != null)
				json.put("timeMLAspect", this.timeMLAspect.toString());
			if (this.timeMLPolarity != null)
				json.put("timeMLPolarity", this.timeMLPolarity.toString());
			if (this.timeMLClass != null)
				json.put("timeMLClass", this.timeMLClass.toString());
			if (this.timeMLPoS != null)
				json.put("timeMLPoS", this.timeMLPoS.toString());
			if (this.timeMLMood != null)
				json.put("timeMLMood", this.timeMLMood.toString());
			if (this.timeMLVerbForm != null)
				json.put("timeMLVerbForm", this.timeMLVerbForm.toString());
			if (this.modality != null)
				json.put("modality", this.modality);
			if (this.cardinality != null)
				json.put("cardinality", this.cardinality);
			if (this.extent != null)
				json.put("extent", this.extent.toJSON(SerializationType.STORE_REFERENCE));
			if (this.eventReference != null)
				json.put("eventRef", this.eventReference.toJSON());
			if (this.argumentReferences != null) {
				JSONArray jsonArgs = new JSONArray();
				for (int i = 0; i < this.argumentReferences.size(); i++) {
					Pair<StoreReference, String> arg = this.argumentReferences.get(i);
					JSONObject jsonArg = new JSONObject();
					jsonArg.put("role", arg.getSecond());
					jsonArg.put("ref", arg.getFirst().toJSON());
					jsonArgs.put(jsonArg);
				}
				json.put("args", jsonArgs);
			}
		} catch (JSONException e) {
			return null;
		}
		
		return json;
	}
	
	@Override
	public boolean fromJSON(JSONObject json) {
		try {
			if (json.has("id"))
				this.id = json.getString("id");
			if (json.has("tokenSpan")) {
				this.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), this.dataTools.getStoredItemSetManager());
			}
			if (json.has("sourceId"))
				this.sourceId = json.getString("sourceId");
			if (json.has("sourceInstanceId"))
				this.sourceInstanceId = json.getString("sourceInstanceId");
			if (json.has("signal")) {
				StoreReference r = new StoreReference();
				if (!r.fromJSON(json.getJSONObject("signal")))
					return false;
				this.signal = this.dataTools.getStoredItemSetManager().resolveStoreReference(r, true);
			}
			if (json.has("timeMLTense"))
				this.timeMLTense = TimeMLTense.valueOf(json.getString("timeMLTense"));
			if (json.has("timeMLAspect"))
				this.timeMLAspect = TimeMLAspect.valueOf(json.getString("timeMLAspect"));
			if (json.has("timeMLPolarity"))
				this.timeMLPolarity = TimeMLPolarity.valueOf(json.getString("timeMLPolarity"));
			if (json.has("timeMLClass"))
				this.timeMLClass = TimeMLClass.valueOf(json.getString("timeMLClass"));
			if (json.has("timeMLPoS"))
				this.timeMLPoS = TimeMLPoS.valueOf(json.getString("timeMLPoS"));
			if (json.has("timeMLMood"))
				this.timeMLMood = TimeMLMood.valueOf(json.getString("timeMLMood"));
			if (json.has("timeMLVerbForm"))
				this.timeMLVerbForm = TimeMLVerbForm.valueOf(json.getString("timeMLVerbForm"));
			if (json.has("modality"))
				this.modality = json.getString("modality");
			if (json.has("polarity"))
				this.cardinality = json.getString("cardinality");
			if (json.has("extent"))
				this.extent = TokenSpan.fromJSON(json.getJSONObject("extent"), this.dataTools.getStoredItemSetManager());
			if (json.has("eventRef"))
				this.eventReference = StoreReference.makeFromJSON(json.getJSONObject("eventRef"));
			if (json.has("args")) {
				this.argumentReferences = new ArrayList<>();
				JSONArray jsonArgs = json.getJSONArray("args");
				for (int i = 0; i < jsonArgs.length(); i++) {
					JSONObject jsonArg = jsonArgs.getJSONObject(i);
					String role = jsonArg.getString("role");
					StoreReference ref = StoreReference.makeFromJSON(jsonArg.getJSONObject("ref"));
					this.argumentReferences.add(new Pair<StoreReference, String>(ref, role));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new EventMention(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public MentionArgumentable.Type getMentionArgumentableType() {
		return MentionArgumentable.Type.EVENT_MENTION;
	}

	@Override
	public Argumentable getArgumentable() {
		return getEvent();
	}
}
