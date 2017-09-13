package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public class Entity implements Argumentable {
	public enum ACEClass {
		SPC,
		USP,
		GEN,
		NEG
	}
	
	public enum ACESubtype {
		Media,
		Educational,
		Nuclear,
		Indeterminate,
		Exploding,
		Plant,
		Subarea_Vehicle,
		Chemical,
		Nation,
		Underspecified,
		Water_Body,
		Entertainment,
		Non_Governmental,
		State_or_Province,
		Land,
		Region_International,
		Sports,
		Building_Grounds,
		Sharp,
		Group,
		Government,
		Path,
		County_or_District,
		Land_Region_Natural,
		GPE_Cluster,
		Commercial,
		Air,
		Projectile,
		Blunt,
		Subarea_Facility,
		Airport,
		Continent,
		Address,
		Celestial,
		Special,
		Boundary,
		Region_General,
		Individual,
		Shooting,
		Religious,
		Water,
		Medical_Science,
		Population_Center,
		Biological
	}
	
	public enum ACEType {
		FAC,
		VEH,
		GPE,
		WEA,
		ORG,
		LOC,
		PER
	}
	
	private String id;
	private String defaultName;
	private ACEClass aceClass;
	private ACESubtype aceSubtype;
	private ACEType aceType;	
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public Entity(DataTools dataTools) {
		this.dataTools = dataTools;
	}
	
	public Entity(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
	}
	
	public Entity(DataTools dataTools,
					StoreReference reference,
					String id,
					String defaultName,
					ACEClass aceClass,
					ACESubtype aceSubtype,
					ACEType aceType) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.defaultName = defaultName;
		this.aceClass = aceClass;
		this.aceSubtype = aceSubtype;
		this.aceType = aceType;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String defaultName() {
		return this.defaultName;
	}
	
	public ACEClass getACEClass() {
		return this.aceClass;
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
			if (this.defaultName != null)
				json.put("name", this.defaultName);
			if (this.aceClass != null)
				json.put("aceClass", this.aceClass.toString());
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
			if (json.has("name"))
				this.defaultName = json.getString("name");
			if (json.has("aceClass"))
				this.aceClass = ACEClass.valueOf(json.getString("aceClass"));
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
		return new Entity(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public Type getArgumentableType() {
		return Type.ENTITY;
	}
}
