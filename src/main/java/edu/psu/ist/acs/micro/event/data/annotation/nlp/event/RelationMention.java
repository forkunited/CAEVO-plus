package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan.SerializationType;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;
import edu.psu.ist.acs.micro.event.data.annotation.nlp.event.EventMention.TimeMLTense;

public class RelationMention implements StoredJSONSerializable {
	public enum ACELexicalCondition {
		PreMod,
        Participial,
        Preposition,
        Coordination,
        Possessive,
        Verbal,
        Formulaic,
        Other
	}
	
	private String id;
	private TokenSpan tokenSpan;
	private TimeMLTense timeMLTense;
	private ACELexicalCondition aceLexicalCondition;
	private StoreReference relationReference;
	private List<Pair<StoreReference, String>> argumentReferences;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public RelationMention(DataTools dataTools) {
		this.dataTools = dataTools;
		this.argumentReferences = new ArrayList<>();
	}
	
	public RelationMention(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.argumentReferences = new ArrayList<>();
	}
	
	public RelationMention(DataTools dataTools,
						StoreReference reference, 
						String id, 
						TokenSpan tokenSpan,
						TimeMLTense timeMLTense, 
						ACELexicalCondition aceLexicalCondition,
						StoreReference relationReference,
						List<Pair<StoreReference, String>> argumentReferences) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.tokenSpan = tokenSpan;
		this.timeMLTense = timeMLTense;
		this.aceLexicalCondition = aceLexicalCondition;
		this.relationReference = relationReference;
		this.argumentReferences = argumentReferences;
	}
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public TimeMLTense getTimeMLTense() {
		return this.timeMLTense;
	}
	
	public ACELexicalCondition getACELexicalCondition() {
		return this.aceLexicalCondition;
	}
	
	public Relation getRelation() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.relationReference, true);
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
			if (this.timeMLTense != null)
				json.put("timeMLTense", this.timeMLTense.toString());
			if (this.aceLexicalCondition != null)
				json.put("aceLexCond", this.aceLexicalCondition.toString());
			if (this.relationReference != null)
				json.put("relRef", this.relationReference.toJSON());
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
			if (json.has("tokenSpan"))
				this.tokenSpan = TokenSpan.fromJSON(json.getJSONObject("tokenSpan"), this.dataTools.getStoredItemSetManager());
			if (json.has("timeMLTense"))
				this.timeMLTense = TimeMLTense.valueOf(json.getString("timeMLTense"));
			if (json.has("aceLexCond"))
				this.aceLexicalCondition = ACELexicalCondition.valueOf(json.getString("aceLexCond"));
			if (json.has("relRef"))
				this.relationReference = StoreReference.makeFromJSON(json.getJSONObject("relRef"));
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
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new RelationMention(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}
}
