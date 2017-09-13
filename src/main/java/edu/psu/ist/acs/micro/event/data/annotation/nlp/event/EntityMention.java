package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan;
import edu.cmu.ml.rtw.generic.data.annotation.nlp.TokenSpan.SerializationType;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public class EntityMention implements MentionArgumentable {
	public enum ACERole {
		GPE,
        LOC,
        ORG,
        PER
	}
	
	public enum ACEType {
		NAM,
        PRO,
        NOM 
	}
	
	private String id;
	private Boolean metonymy = null;
	private ACERole aceRole;
	private ACEType aceType;
	private TokenSpan tokenSpan;
	private TokenSpan head;
	private StoreReference entityReference;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public EntityMention(DataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public EntityMention(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	public EntityMention(DataTools dataTools,
						StoreReference reference,
						String id,
						Boolean metonymy,
						ACERole aceRole,
						ACEType aceType,
						TokenSpan tokenSpan,
						TokenSpan head,
						StoreReference entityReference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.metonymy = metonymy;
		this.aceRole = aceRole;
		this.aceType = aceType;
		this.tokenSpan = tokenSpan;
		this.head = head;
		this.entityReference = entityReference;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Boolean isMetonymy() {
		return this.metonymy;
	}
	
	public ACERole getACERole() {
		return this.aceRole;
	}
	
	public ACEType getACEType() {
		return this.aceType;
	}
	
	public TokenSpan getTokenSpan() {
		return this.tokenSpan;
	}
	
	public TokenSpan getHead() {
		return this.head;
	}
	
	public Entity getEntity() {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.entityReference, true);
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.tokenSpan != null)
				json.put("tokenSpan", this.tokenSpan.toJSON(SerializationType.STORE_REFERENCE));
			if (this.metonymy != null)
				json.put("metonymy", this.metonymy);
			if (this.aceRole != null)
				json.put("aceRole", this.aceRole.toString());
			if (this.aceType != null)
				json.put("aceType", this.aceType.toString());
			if (this.head != null)
				json.put("head", this.head.toJSON(SerializationType.STORE_REFERENCE));
			if (this.entityReference != null)
				json.put("entityRef", this.entityReference.toJSON());
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
			if (json.has("metonymy"))
				this.metonymy = json.getBoolean("metonymy");
			if (json.has("aceRole"))
				this.aceRole = ACERole.valueOf(json.getString("aceRole"));
			if (json.has("aceType"))
				this.aceType = ACEType.valueOf(json.getString("aceType"));
			if (json.has("head"))
				this.head = TokenSpan.fromJSON(json.getJSONObject("head"), this.dataTools.getStoredItemSetManager());
			if (json.has("entityRef"))
				this.entityReference = StoreReference.makeFromJSON(json.getJSONObject("entityRef"));
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new EntityMention(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public Type getMentionArgumentableType() {
		return Type.ENTITY_MENTION;
	}

	@Override
	public Argumentable getArgumentable() {
		return getEntity();
	}

}
