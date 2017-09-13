package edu.psu.ist.acs.micro.event.scratch;

//import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import edu.cmu.ml.rtw.generic.util.FileUtil;

public class TestCAEVOFeatureOutput {
	public static void main(String[] args) throws JSONException {
		String caevoStr = FileUtil.readFile(args[0]);
		String microStr = FileUtil.readFile(args[1]);
		String mode = args[2];
		
		String[] caevoLines = caevoStr.split("\n");
		String[] microLines = microStr.split("\n");
		
		//Arrays.sort(caevoLines);
		//Arrays.sort(microLines);
		if (caevoLines.length != microLines.length)
			throw new UnsupportedOperationException("Incorrect data size.");
		
		for (int i = 0; i < microLines.length; i++) {
			String[] caevoLineParts = caevoLines[i].split("\t");
			String[] microLineParts = microLines[i].split("\t");
			JSONObject caevoJson = new JSONObject(caevoLineParts[3]);
			JSONObject microJson = new JSONObject((microLineParts.length > 2) ? microLineParts[3] : microLineParts[0]);
			
			if (caevoLineParts.length != 4) {
				String caevoLabel = caevoLineParts[caevoLineParts.length - 1];
				String microLabel = microLineParts[microLineParts.length - 1];
				if (!caevoLabel.equals(microLabel))
					throw new UnsupportedOperationException("Incorrect label (" + microLabel + ", " + caevoLabel + "): " + microLines[i] + "\n" + caevoLines[i]);
			}
			
			if (caevoJson.length() != microJson.length())
				throw new UnsupportedOperationException("Incorrect number of features:\n" + microLines[i] + "\n" + caevoLines[i]);
			
			microJson = convertMicroToCaevo(microJson, mode);
			
			String[] caevoNames = JSONObject.getNames(caevoJson);
			for (String caevoName : caevoNames) {
				if (!microJson.has(caevoName))
					throw new UnsupportedOperationException("Missing feature (" + caevoName + "):\n" + microLines[i] + "\n" + caevoLines[i]);
				if (!microJson.get(caevoName).equals(caevoJson.get(caevoName)))
					throw new UnsupportedOperationException("Mismatch value (" + caevoName + "):\n" + microLines[i] + "\n" + caevoLines[i]);
			}
			
			String[] microNames = JSONObject.getNames(microJson);
			for (String microName : microNames) {
				if (!caevoJson.has(microName))
					throw new UnsupportedOperationException("Extra feature (" + microName + "):\n" + microLines[i] + "\n" + caevoLines[i]);
			}
		}
		
		System.out.println("SUCCESS!");
	}
	
	private static JSONObject convertMicroToCaevo(JSONObject microJson, String mode) throws JSONException {
		JSONObject caevoJson = new JSONObject();
		String[] names = JSONObject.getNames(microJson);
		String caevoName = null;
		for (String name : names) {
			if (name.startsWith("feventTokenBA_")) {
				caevoName = name.replace("feventTokenBA_", "bow-");
				caevoName = caevoName.replace(")", "-rrb-");
				caevoName = caevoName.replace("(", "-lrb-");
			} else if (name.startsWith("fdomET_DOMINATING")) {
				caevoName = name.replace("fdomET_DOMINATING", "dominates");
			} else if (name.startsWith("fdomET_DOMINATED")) {
				caevoName = name.replace("fdomET_DOMINATED", "isDominated");
			} else if (name.startsWith("ftokenPathFirstTypeET_")) {
				caevoName = name.replace("ftokenPathFirstTypeET_", "");
				caevoName = caevoName.replace("//", "");
				if (name.endsWith("EVENT")) {
					caevoName = caevoName.substring(0, caevoName.length() - 5);
					caevoName = "EVENT_" + caevoName + ((caevoName.length() > 0) ? "_" : "") + "TIME";
				} else {
					caevoName = caevoName.substring(0, caevoName.length() - 4);
					caevoName = "TIME_" + caevoName + ((caevoName.length() > 0) ? "_" : "") + "EVENT";
				}
				
				caevoName = "tokenpath-" + caevoName;
			} else if (name.startsWith("feventSynset1_")) {
				caevoName = name.replace("feventSynset1_", "synset1-");
			} else if (name.startsWith("fconPathFirstType_")) {
				caevoName = name.replace("fconPathFirstType_", "");
				caevoName = caevoName.replace("//", "");
				if (name.endsWith("EVENT")) {
					caevoName = caevoName.substring(0, caevoName.length() - 5);
					caevoName = "EVENT_" + caevoName + "_TIME";
				} else {
					caevoName = caevoName.substring(0, caevoName.length() - 4);
					caevoName = "TIME_" + caevoName +"_EVENT";
				}
				
				caevoName = "pathnopos-" + caevoName;
			} else if (name.startsWith("fconPathPosFirstType_")) {
				caevoName = name.replace("fconPathPosFirstType_", "");
				caevoName = caevoName.replace("//", "");
				if (name.endsWith("EVENT")) {
					caevoName = caevoName.substring(0, caevoName.length() - 5);
					caevoName = "EVENT_" + caevoName + "_TIME";
				} else {
					caevoName = caevoName.substring(0, caevoName.length() - 4);
					caevoName = "TIME_" + caevoName +"_EVENT";
				}
				
				caevoName = "pathfull-" + caevoName;
			} else if (name.startsWith("ffirstType_TIME")) {
				caevoName = name.replace("ffirstType_TIME", "time-first");
			} else if (name.startsWith("ffirstType_EVENT")) {
				caevoName = name.replace("ffirstType_EVENT", "event-first");
			} else if (name.startsWith("fdepPathET_")) {
				caevoName = name.replace("fdepPathET_", "");
			} else if (name.startsWith("ftimeToken_")) {
				caevoName = name.replace("ftimeToken_", "timephrase-");
			} else if (name.startsWith("ftimeTokenh_")) {
				caevoName = name.replace("ftimeTokenh_", "timetoken-");
				caevoName = caevoName.replace("[DoW]", "DAYOFWEEK");
			} else if (name.startsWith("feventLemma_")) {
				caevoName = name.replace("feventLemma_", "lemma1-");
			} else if (name.startsWith("feventPosBNI_")) {
				caevoName = name.replace("feventPosBNI_", "pos1-bi-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace("_", "-");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("feventPosB2_1_")) {
				caevoName = name.replace("feventPosB2_1_", "pos1-2-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("feventPosB2_0_")) {
				caevoName = name.replace("feventPosB2_0_", "pos1-1-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("feventPos_")) {
				caevoName = name.replace("feventPos_", "pos1-0-");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("feventToken_")) {
				caevoName = name.replace("feventToken_", "token1-");
			} else if (name.startsWith("fsToken_")) {// Start EE
				caevoName = name.replace("fsToken_", "token1-");
			} else if (name.startsWith("fsPrep_")) {
				caevoName = name.replace("fsPrep_", "prep1-");
			} else if (name.startsWith("fsPos_")) {
				caevoName = name.replace("fsPos_", "pos1-0-");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("fsPosB2_0_")) {
				caevoName = name.replace("fsPosB2_0_", "pos1-1-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("fsPosB2_1_")) {
				caevoName = name.replace("fsPosB2_1_", "pos1-2-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("fsPosBNI_")) {
				caevoName = name.replace("fsPosBNI_", "pos1-bi-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace("_", "-");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("fsLemma_")) {
				caevoName = name.replace("fsLemma_", "lemma1-");
			} else if (name.startsWith("fsSynset1_")) {
				caevoName = name.replace("fsSynset1_", "synset1-");
			} else if (name.startsWith("ftToken_")) {
				caevoName = name.replace("ftToken_", "token2-");	
			} else if (name.startsWith("ftPrep_")) {
				caevoName = name.replace("ftPrep_", "prep2-");
			} else if (name.startsWith("ftPos_")) {
				caevoName = name.replace("ftPos_", "pos2-0-");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("ftPosB2_0_")) {
				caevoName = name.replace("ftPosB2_0_", "pos2-1-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("ftPosB2_1_")) {
				caevoName = name.replace("ftPosB2_1_", "pos2-2-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("ftPosBNI_")) {
				caevoName = name.replace("ftPosBNI_", "pos2-bi-");
				caevoName = caevoName.replace("PRE-0", "<s>");
				caevoName = caevoName.replace("PRE-1", "<pre-s>");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
				caevoName = caevoName.replace("_", "-");
			} else if (name.startsWith("ftLemma_")) {
				caevoName = name.replace("ftLemma_", "lemma2-");
			} else if (name.startsWith("ftSynset1_")) {
				caevoName = name.replace("ftSynset1_", "synset2-");
			} else if (name.startsWith("fstToken_") || name.startsWith("ffsToken_")) {
				if (mode.equalsIgnoreCase("ee"))
					caevoName = name.replace("fstToken_", "BI-");
				else 
					caevoName = name.replace("ffsToken_", "bi-");
				caevoName = caevoName.replace("//", "_");
				caevoName = caevoName.replace("-_", "-");
				caevoName = caevoName.replace("[DoW]", "DAYOFWEEK");
			} else if (name.startsWith("fstPos_")) {
				caevoName = name.replace("fstPos_", "posBi_");
				caevoName = caevoName.replace("//", "-");
				caevoName = caevoName.replace("_", "");
				caevoName = caevoName.replace(")", "-RRB-");
				caevoName = caevoName.replace("(", "-LRB-");
			} else if (name.startsWith("fdepPathEE_")) {
				caevoName = name.replace("fdepPathEE_", "");
			} else if (name.startsWith("fconPathEE_")) {
				caevoName = name.replace("fconPathEE_", "pathnopos-");
			} else if (name.startsWith("fconPathPosEE_")) {
				caevoName = name.replace("fconPathPosEE_", "pathfull-");
			} else if (name.startsWith("foverEventEE_true")) {
				caevoName = name.replace("foverEventEE_true", "notsequential");
			} else if (name.startsWith("foverEventEE_false")) {
				caevoName = name.replace("foverEventEE_false", "sequential");
			} else if (name.startsWith("fdomEE_DOMINATING")) {
				caevoName = name.replace("fdomEE_DOMINATING", "dominates");
			} else if (name.startsWith("fdomEE_DOMINATED")) {
				caevoName = name.replace("fdomEE_DOMINATED", "isDominated");
			} else if (name.startsWith("fconstant_")) {
				caevoName = name.replace("fconstant_c", "order-sameSent-before");
			} else if (name.startsWith("fconstant1_")) {
				caevoName = name.replace("fconstant1_c", "order-sameSent");
			} else if (name.startsWith("fconstant2_")) {
				caevoName = name.replace("fconstant2_c", "order-before");
			} else {
				throw new UnsupportedOperationException("Unrecognized feature: " + name);
			}
			
			caevoJson.put(caevoName, microJson.get(name));
		}
		
		return caevoJson;
	}
}
