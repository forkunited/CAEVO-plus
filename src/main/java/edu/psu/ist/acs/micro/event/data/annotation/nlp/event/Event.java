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

public class Event implements Argumentable {
	public enum ACEType {
		Business,
        Transaction,
        Justice,
        Contact,
        Movement,
        Life,
        Conflict,
        Personnel
	}
	
	public enum ACESubtype {
		Phone_Write,
		Appeal,
		Be_Born,
		Trial_Hearing,
		Marry,
		Arrest_Jail,
		Nominate,
		Fine,
		Attack,
		Demonstrate,
		Release_Parole,
		Declare_Bankruptcy,
		Start_Org,
		Charge_Indict,
		Acquit,
		Die,
		Elect,
		Convict,
		Pardon,
		Transfer_Ownership,
		Meet,
		End_Position,
		Sentence,
		Start_Position,
		Transport,
		Divorce,
		Transfer_Money,
		Merge_Org,
		Sue,
		Execute,
		Injure,
		Extradite,
		End_Org
	}
	
	public enum ACEGenericity {
		Generic,
		Specific
	}
	
	public enum ACEEventArgumentRole {
		Position,
        Seller,
        Instrument,
        Person,
        Artifact,
        Time_At_End,
        Agent,
        Money,
        Crime,
        Beneficiary,
        Price,
        Origin,
        Time_At_Beginning,
        Victim,
        Vehicle,
        Giver,
        Time_Ending,
        Prosecutor,
        Target,
        Recipient,
        Plaintiff,
        Time_After,
        Time_Before,
        Entity,
        Time_Starting,
        Attacker,
        Sentence,
        Org,
        Place,
        Time_Within,
        Time_Holds,
        Destination,
        Defendant,
        Adjudicator,
        Buyer
	}
	
	private String id;
	private ACEType aceType;
	private ACEGenericity aceGenericity;
	private ACESubtype aceSubtype;
	private List<Pair<StoreReference, String>> argumentReferences;
	private List<StoreReference> someMentionReferences;
	
	private StoreReference reference;
	private DataTools dataTools;
	
	public Event(DataTools dataTools) {
		this.dataTools = dataTools;
		this.argumentReferences = new ArrayList<>();
	}
	
	public Event(DataTools dataTools, StoreReference reference) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.argumentReferences = new ArrayList<>();
	}
	
	public Event(DataTools dataTools,
				StoreReference reference, 
				String id,
				ACEType aceType,
				ACEGenericity aceGenericity,
				ACESubtype aceSubtype,
				List<Pair<StoreReference, String>> argumentReferences,
				List<StoreReference> someMentionReferences) {
		this.dataTools = dataTools;
		this.reference = reference;
		this.id = id;
		this.aceType = aceType;
		this.aceGenericity = aceGenericity;
		this.aceSubtype = aceSubtype;
		this.argumentReferences = argumentReferences;
		this.someMentionReferences = someMentionReferences;
	}

	public String getId() {
		return this.id;
	}
	
	public ACEType getACEType() {
		return this.aceType;
	}
	
	public ACEGenericity getACEGenericity() {
		return this.aceGenericity;
	}
	
	public ACESubtype getACESubtype() {
		return this.aceSubtype;
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
	
	public int getSomeMentionCount() {
		return this.someMentionReferences.size();
	}
	
	public EventMention getSomeMention(int index) {
		return this.dataTools.getStoredItemSetManager().resolveStoreReference(this.someMentionReferences.get(index), true);
	}
	
	@Override
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
		try {
			if (this.id != null)
				json.put("id", this.id);
			if (this.aceType != null)
				json.put("aceType", this.aceType.toString());
			if (this.aceGenericity != null)
				json.put("aceGenericity", this.aceGenericity.toString());
			if (this.aceSubtype != null)
				json.put("aceSubtype", this.aceSubtype.toString());
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
			
			if (this.someMentionReferences != null) {
				JSONArray jsonMentions = new JSONArray();
				for (int i = 0; i < this.someMentionReferences.size(); i++) {
					StoreReference ref = this.someMentionReferences.get(i);
					jsonMentions.put(ref.toJSON());
				}
				json.put("someMentions", jsonMentions);
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
			if (json.has("aceGenericity"))
				this.aceGenericity = ACEGenericity.valueOf(json.getString("aceGenericity"));
			if (json.has("aceSubtype"))
				this.aceSubtype = ACESubtype.valueOf(json.getString("aceSubtype"));
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
			
			if (json.has("someMentions")) {
				this.someMentionReferences = new ArrayList<>();
				JSONArray jsonMentions = json.getJSONArray("someMentions");
				for (int i = 0; i < jsonMentions.length(); i++) {
					StoreReference ref = StoreReference.makeFromJSON(jsonMentions.getJSONObject(i));
					this.someMentionReferences.add(ref);
				}
			} 
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public StoredJSONSerializable makeInstance(StoreReference reference) {
		return new Event(this.dataTools, reference);
	}

	@Override
	public StoreReference getStoreReference() {
		return this.reference;
	}

	@Override
	public Type getArgumentableType() {
		return Type.EVENT;
	}
}
