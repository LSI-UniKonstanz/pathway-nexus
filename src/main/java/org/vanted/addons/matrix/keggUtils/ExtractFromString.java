package org.vanted.addons.matrix.keggUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.ws.rs.core.MediaType;

import org.graffiti.editor.MainFrame;
import org.vanted.addons.matrix.reading.SubstanceWithPathways;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Url;
import de.ipk_gatersleben.ag_nw.graffiti.services.web.RestService;

public class ExtractFromString {
	
	/**
	 * @param String keggResponse: one or more lines of the sequence
	 * cpd:CXXXXX\tpath:mapXXXXX\n
	 * with XXXXX = numbers representing the Kegg ID for the desired component
	 * @return
	 */
	public static ArrayList<Pathway> extractPathways(String keggResponse) {
		ArrayList<Pathway> pathways = new ArrayList<Pathway>();
		String[] lines = keggResponse.split("\n");
		
		for(String line: lines) {
			line = line.replace("path:", "_");
 			String pwId = line.substring(line.indexOf("_") + 1);

 			//System.out.println(pwId);

			RestService restServiceList = new RestService("https://rest.kegg.jp/" + "list");
			String pwTitle = (String) restServiceList.makeRequest(pwId, MediaType.TEXT_PLAIN_TYPE, String.class);
			pwTitle = pwTitle.replace("path:" + pwId + "\t", "");
			
			Pathway pathway = new Pathway(new KeggId(pwId) ,null, null, pwTitle, null, new Url("https://www.kegg.jp/kegg-bin/show_pathway?" + pwId), null,null, null);
			
			//System.out.println(pathway.getTitle());
			
			pathways.add(pathway);
		}
		return pathways;
	}

	/**
	 * @param String keggResponse: one line of the sequence
	 * cpd:CXXXXX\tcompoundname1;compoundname2;...;compoundnamex
	 * with XXXXX = numbers representing the Kegg ID for the desired compound
	 * @return
	 */
	public static ArrayList<String> extractCompoundNames(String keggResponse) {
		ArrayList<String> names = new ArrayList<String>();
		
		String s = keggResponse.substring(keggResponse.indexOf("   "));
		String[] namesArray = s.split(";");
		for(String name:namesArray) {
			name = name.trim();
			names.add(name);
		}
		return names;
	}
	
	/**
	 * @param String keggResponse: one or more lines of the sequence
	 * cpd:CXXXXX\tcompoundname1;compoundname2;...;compoundnamex
	 * with XXXXX = numbers representing the Kegg ID for the compounds
	 * @return: an array of Stings, each containing ID and names
	 */
	public static String[] extractAlternativeCompounds(String keggResponse) {
		String[] lines = keggResponse.split("\n");
		
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i].replace("cpd:", "");
			lines[i]  = line.replace("\t", "   ");	
		}
		
		return lines;
	}
	
	/**
	 * @param String keggResponse: one or more lines of the sequence
	 * path:mapXXXXX\tcpd:CXXXXX\n
	 * with XXXXX = numbers representing the Kegg ID for the desired component
	 * @return
	 */
	public static ArrayList<String> extractCompounds(String keggResponse) throws Exception{
		String[] lines = keggResponse.split("\n");
	
		if(lines.length > 100) { 
			 throw new Exception("experimentData must supply substances with associated pathways");
			 //TODO: Find a way to handle large substance sets
		}
		
		ArrayList<String> cpdIds = new ArrayList<String>();
		int count = 0;
		String idChain = "";
		
		for(String line: lines) {
		
 			String cpdId = line.substring(line.lastIndexOf("\tcpd:") + 5);

 			idChain += "+" + cpdId;
 			count++;
			
 			if(count == 10) {
 				cpdIds.add(idChain);
 				idChain = "";
 				count = 0;
 			}
		}
		cpdIds.add(idChain);
 
		ArrayList<String> idsAndNames = new ArrayList<String>();
		for(String s: cpdIds) {
			if(s.startsWith("+")) {
				s = s.substring(1, s.length());
				
				RestService restServiceList = new RestService("https://rest.kegg.jp/" + "list");
				String collectedResponse = (String) restServiceList.makeRequest(s, MediaType.TEXT_PLAIN_TYPE, String.class);

				String[] compounds = collectedResponse.split("\n");

				for(String c:compounds) {
					 // cpd:CXXXXX\tcompoundname1;compoundname2;...;compoundnamex
					c = c.replace("cpd:", "");
					c = c.replace("\t", "   ");								//since "\t" will not be displayed in an EditableList
					idsAndNames.add(c);
				}
			}
		}
		return idsAndNames;
	}

	/**
	 * returns the pathway classes from a pathway entry
	 * @param keggGetResponse: the complete entry of a Kegg pathway
	 * @return
	 */
	public static ArrayList<String> superPwFromEntry(String keggGetResponse) {
		String[] lines = keggGetResponse.split("\n");
		ArrayList<String> pwClasses = new ArrayList<String>();
		String pwClass = null;
		
		for(String line: lines) {
			if(line.startsWith("CLASS")) {
				pwClass = line.substring(line.indexOf("CLASS") + 12);
				pwClasses = new ArrayList<String>(Arrays.asList(pwClass.split("; ")));
			}
		}

		return pwClasses;
	}
	
	/**
	 * returns the compound classes from a compound entry
	 * @param keggGetResponse: the complete entry of a Kegg compound
	 * @return
	 */
	public static ArrayList<String> extractSubstanceClasses(String keggGetResponse){
		String[] lines = keggGetResponse.split("\n");
		ArrayList<String> categories = new ArrayList<String>();
		boolean found = false;
		int index = 0;
		int start = 0;
		int end = 0;
		
		String id = lines[0].substring(12, 18);
		
		for(String line: lines) {
			if(found && line.contains(id)) {
				end = index - 1;
				found = false;
			}
			
			if(line.startsWith("BRITE") && isCompoundClassification(line)) {
				start = index;
				found = true;
			}
			index++;
		}
		
		for(int i = start + 1; i < end; i++) {
			String line = lines[i];
			String cat = line.substring(line.lastIndexOf("   ") + 3);
			categories.add(cat);
		}
		
		return categories;
	}
	
	/**
	 * returns true if this line from a kegg compound entry contains an idientifier 
	 * of the compound classification of Kegg
	 * @param line
	 * @return
	 */
	private static boolean isCompoundClassification(String line) {
		String[] compoundClasses = {"[BR:br08001]","[BR:br08002]", "[BR:br08003]", "[BR:br08021]", "[BR:br08005]",		//Kegg Brite identities for Compounds
				"[BR:br08006]", "[BR:br08007]", "[BR:br08008]", "[BR:br08009]", "[BR:br08010]"};
		for(String id: compoundClasses) {
			if(line.contains(id)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns the pathways from a compound entry
	 * @param keggGetResponse: the complete entry of a Kegg compound
	 * @return
	 */
	public static ArrayList<String> relatedPwsFromCompoundEntry(String keggGetResponse) {
		String[] lines = keggGetResponse.split("\n");
		ArrayList<String> relPathways = new ArrayList<String>();
		boolean found = false;
		int index = 0;
		int start = 0;
		int end = 0;
		
		for(String line: lines) {
			if(!line.startsWith(" ") && found) {
				end = index;
				found = false;
			}
			
			if(line.startsWith("PATHWAY")) {
				start = index;
				found = true;
			}
			
			index++;
		}
		
		for(int i = start; i < end; i++) {
			String pw = lines[i].substring(lines[i].indexOf("map") + 10);
			relPathways.add(pw);
		}
		
		return relPathways;
	}
	
	/**
	 * returns the realted pathways from a pathway entry
	 * @param keggGetResponse: the complete entry of a Kegg pathway
	 * @return
	 */
	public static ArrayList<String> relatedPwsFromEntry(String keggGetResponse) {
		String[] lines = keggGetResponse.split("\n");
		ArrayList<String> relPathways = new ArrayList<String>();
		boolean found = false;
		int index = 0;
		int start = 0;
		int end = 0;
		
		for(String line: lines) {
			if(!line.startsWith(" ") && found) {
				end = index;
				found = false;
			}
			
			if(line.startsWith("REL_PATHWAY")) {
				start = index;
				found = true;
			}
			
			index++;
		}
		
		for(int i = start; i < end; i++) {
			String pw = lines[i].substring(lines[i].indexOf("map"));
			relPathways.add(pw);
		}
		
		return relPathways;
	}
	
	

}
