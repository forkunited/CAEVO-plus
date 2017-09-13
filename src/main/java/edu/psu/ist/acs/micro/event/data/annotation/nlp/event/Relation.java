package edu.psu.ist.acs.micro.event.data.annotation.nlp.event;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.data.DataTools;
import edu.cmu.ml.rtw.generic.data.store.StoreReference;
import edu.cmu.ml.rtw.generic.util.Pair;
import edu.cmu.ml.rtw.generic.util.StoredJSONSerializable;

public class Relation implements StoredJSONSerializable {
	public enum ACERelationArgumentRole {
		Arg_1,
        Time_At_Beginning,
        Arg_2,
        Time_Within,
        Time_After,
        Time_Holds,
        Time_Ending,
        Time_Before,
        Time_Starting,
        Time_At_End
	}
	
	public enum ACEType {
		PHYS,
        ART,
        ORG_AFF,
        METONYMY,
        PART_WHOLE,
        GEN_AFF,
        PER_SOC,

	}
	
	public enum ACESubtype {
		Founder,
        User_Owner_Inventor_Manufacturer,
        Sports_Affiliation,
        Citizen_Resident_Religion_Ethnicity,
        Near,
        Investor_Shareholder,
        Ownership,
        Artifact,
        Student_Alum,
        Lasting_Personal,
        Business,
        Located,
        Org_Location,
        Membership,
        Geographical,
        Family,
        Employment,
        Subsidiary
	}
	
	private String id;
	private ACEType aceType;
	private ACESubtype aceSubtype;
	private String modality;
	private List<Pair<StoreReference, String>> argumentReferences;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public Relation(DataTools dataTools) {
		this.dataTools = dataTools;
		this.argumentReferences = new ArrayList<>();
	}
	
	public Relation(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.argumentReferences = new ArrayList<>();
	}
	
	public Relation(DataTools dataTools,
						StoreReference reference, 
						String id, 
						ACEType aceType,
						ACESubtype aceSubtype,
						String modality,
						List<Pair<StoreReference, String>> argumentReferences) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.aceType = aceType;
		this.aceSubtype = aceSubtype;
		this.modality = modality;
		this.argumentReferences = argumentReferences;
	}
	
	public String getId() {
		return this.id;
	}
	
	public ACEType getACEType() {
		return this.aceType;
	}
	
	public ACESubtype getACESubtype() {
		return this.aceSubtype;
	}
	
	public String getModality() {
		return this.modality;
	}
	
	public int getArgumentCount() {
		return this.argumentReferences.size();
	}
	
	public String getArgumentRole(int index) {
		return this.argumentReferences.get(index).getSecond();
	}
	
	public Argumentable getArgument(String role) {
		for (int i = 0; i < getArgumentCount(); i++)
			if (this.argumentReferences.get(i).getSecond().equals(role))
				return getArgument(i);
		return null;
	}
	
	public Argumentable getArgument(int index) {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.argumentReferences.get(index).getFirst(), true);
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
			if (this.modality != null)
				json.put("modality", this.modality);
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
			if (json.has("aceType"))
				this.aceType = ACEType.valueOf(json.getString("aceType"));
			if (json.has("aceSubtype"))
				this.aceSubtype = ACESubtype.valueOf(json.getString("aceSubtype"));
			if (json.has("modality"))
				this.modality = json.getString("modality");
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
		return new Relation(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}
}
