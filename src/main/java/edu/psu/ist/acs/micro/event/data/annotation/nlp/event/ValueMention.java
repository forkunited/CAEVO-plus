package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan.SerializationType;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public class ValueMention implements MentionArgumentable {
	private String id;
	private TokenSpan tokenSpan;
	private StoreReference valueReference;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public ValueMention(DataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public ValueMention(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	public ValueMention(DataTools dataTools,
					StoreReference reference,
					String id,
					TokenSpan tokenSpan,
					StoreReference valueReference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.tokenSpan = tokenSpan;
		this.valueReference = valueReference;
	}
	
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public Value getValue() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.valueReference, true);
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.tokenSpan != null)
				json.put("tokenSpan", this.tokenSpan.toJSON(SerializationType.STORE_REFERENCE));
			if (this.valueReference != null)
				json.put("valueRef", this.valueReference.toJSON());
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
			if (json.has("valueRef"))
				this.valueReference = StoreReference.makeFromJSON(json.getJSONObject("valueRef"));
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new ValueMention(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public Type getMentionArgumentableType() {
		return Type.VALUE_MENTION;
	}

	@Override
	public Argumentable getArgumentable() {
		return getValue();
	}
}
