package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan.SerializationType;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

/**
 * Time represents a TimeML Signal.
 * 
 * See http://timeml.org/site/index.html for details.
 * 
 * @author Bill McDowell
 * 
 */
public class Signal implements StoredJSONSerializable {	
	private String id;
	private TokenSpan tokenSpan;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public Signal(DataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public Signal(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.tokenSpan != null)
				json.put("tokenSpan", this.tokenSpan.toJSON(SerializationType.STORE_REFERENCE));
		} catch (JSONException e) {
			return null;
		}
		return json;
	}
	
	public boolean fromJSON(JSONObject json) {		
		try {
			if (json.has("id"))
				this.id = json.getString("id");
			if (json.has("tokenSpan"))
				this.tokenSpan = TokenSpan.fromJSON(json, this.dataTools.getStoredItemSetManager());
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}
	
	

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		Signal signal = new Signal(this.dataTools, reference);
		return signal;
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}
}
