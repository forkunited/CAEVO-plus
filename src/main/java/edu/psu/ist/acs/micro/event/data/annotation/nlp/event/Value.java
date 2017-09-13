package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public class Value implements Argumentable {
	public enum ACEType {
		Contact_Info,
        Numeric,
        Job_Title,
        Sentence,
        Crime
	}
	
	public enum ACESubtype {
        URL,
        E_Mail,
        Percent,
        Money,
        Phone_Number
	}
	
	private String id;
	private ACEType aceType;
	private ACESubtype aceSubtype;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public Value(DataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public Value(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	public Value(DataTools dataTools,
					StoreReference reference,
					String id,
					ACESubtype aceSubtype,
					ACEType aceType) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.aceSubtype = aceSubtype;
		this.aceType = aceType;
	}
	
	public String getId() {
		return this.id;
	}
	
	public ACESubtype getACESubtype() {
		return this.aceSubtype;
	}
	
	public ACEType getACEType() {
		return this.aceType;
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.aceType != null)
				json.put("aceType", this.aceType.toString());
			if (this.aceSubtype != null)
				json.put("aceSubtype", this.aceSubtype.toString());
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
			if (json.has("aceType"))
				this.aceType = ACEType.valueOf(json.getString("aceType"));
			if (json.has("aceSubtype"))
				this.aceSubtype = ACESubtype.valueOf(json.getString("aceSubtype"));
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new Value(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public Type getArgumentableType() {
		return Type.VALUE;
	}
}
