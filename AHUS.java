import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class AHUS {
	String inputPath;
	int minUtility = 0;

	final boolean DEBUG = true;

	final Map<Integer, ArrayList<String>> seq = new HashMap<Integer, ArrayList<String>>();
	final Map<Integer, ArrayList<Integer>> ulist = new HashMap<Integer, ArrayList<Integer>>();
	final Map<Integer, ArrayList<Integer>> rlist = new HashMap<Integer, ArrayList<Integer>>();

	public void runAlgorithm(String inputPath, String outputPath, int minUtility) throws IOException {
		this.inputPath = inputPath;
		this.minUtility = minUtility;

		final Map<String, Integer> mapItemToSWU = new HashMap<String, Integer>();
		List<String> consideredItems = new ArrayList<String>();
		List<String> promisingItems = new ArrayList<String>();

		// ========================================= Database scan 1 - Calculating SWU =================================================
		int sequenceCount = 0;
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputPath))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is  empty or is a kind of metadata, skip it
				if (thisLine.isEmpty() == true) {
					continue;
				}
				
				// split the transaction according to the " " separator
				String tokens[] = thisLine.split(" "); 
				
				// get the sequences.add(); utility (the last token on the line)
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));
				
				// Then read each token from this sequences.add(); (except the last three tokens
				// which are -1 -2 and the sequences.add(); utility)
				for(int i = 0; i < tokens.length - 3; i++) {
					String currentToken = tokens[i];
					// if the current token is not -1 
					if(currentToken.length() != 0 && !currentToken.equals("-1")) {
						// find the left brack
						int positionLeftBracketString = currentToken.indexOf('[');
						// System.out.println(currentToken);
						// get the item
						String itemString = currentToken.substring(0, positionLeftBracketString);
						// Integer item = Integer.parseInt(itemString);
						
						//Tin added:		
						if (!consideredItems.contains(itemString)) {
							consideredItems.add(itemString);
						
							// get the current SWU of that item
							Integer swu = mapItemToSWU.get(itemString);
							
							// add the utility of sequences.add(); utility to the swu of this item
							swu = (swu == null) ? sequenceUtility : swu + sequenceUtility;
							mapItemToSWU.put(itemString, swu);
//							System.out.println(" -> " + swu);
						}
//						else System.out.println(item + " is already considered in the same (line) inputPath sequences.add();");								
					}
				}
				
				// Philippe added:
				consideredItems.clear();
				
				// increase sequences.add(); count
				sequenceCount++;
			}

			// compare SWU of all items with minUtility and prune unpromising items
			for(Entry<String, Integer> entry : mapItemToSWU.entrySet()) {
				if(entry.getValue() >= minUtility) {
					promisingItems.add(entry.getKey());
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the inputPath file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }

	    // ============================================If in debug mdoe, print debug data==========================================
	    if(DEBUG) {
			System.out.println("INITIAL ITEM COUNT " + mapItemToSWU.size());
			System.out.println("SEQUENCE COUNT = " + sequenceCount);
			System.out.println("INITIAL SWU OF ITEMS");
			for(Entry<String, Integer> entry : mapItemToSWU.entrySet()) {
				System.out.println("Item: " + entry.getKey() + " swu: " + entry.getValue());
			}
			System.out.println("PROMISING ITEMS:");
			for(String i : promisingItems) {
				System.out.println(i);
			}
		}

		// =============================== Database scan 2 - creating seq, ulist, rlist of all sequences===========================
		myInput = null;
		int promisingSequenceCount = 0;

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputPath))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is  empty or is a kind of metadata, skip it
				if (thisLine.isEmpty() == true) {
					continue;
				}
				
				ArrayList<String> seqList = new ArrayList<String>();
				ArrayList<Integer> ulistList = new ArrayList<Integer>();
				ArrayList<Integer> rlistList = new ArrayList<Integer>();

				// split the transaction according to the " " separator
				String tokens[] = thisLine.split(" "); 
				
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));

				// Then read each token from this sequences.add(); (except the last two tokens
				// which are -2 and the sequences.add(); utility)
				int remSequenceUtility = sequenceUtility;
				for(int i = 0; i < tokens.length - 2; i++) {
					String currentToken = tokens[i];

					// if the current token is not -1 
					if(currentToken.length() != 0 && !currentToken.equals("-1")) {
						// find the left brack
						int positionLeftBracketString = currentToken.indexOf('[');
						// get the item
						String itemString = currentToken.substring(0, positionLeftBracketString);
						// Integer item = Integer.parseInt(itemString);

						//Don't add unpromising items
						if(!promisingItems.contains(itemString)) {
							continue;
						}

						seqList.add(itemString);

						String utilityString = currentToken.substring(positionLeftBracketString+1, currentToken.length()-1);
						Integer utility = Integer.parseInt(utilityString);
						ulistList.add(utility);

						int remUtility = remSequenceUtility - utility;
						remSequenceUtility -= utility;
						rlistList.add(remUtility);
					} else {
						seqList.add("0");
						ulistList.add(0);
						rlistList.add(0);
					}
					// System.out.println(seq);
				}

				//Don't add empty sequences
				if(seqList.size() == 1) {
					continue;
				}

				promisingSequenceCount++;
				seq.put(promisingSequenceCount-1, seqList);
				ulist.put(promisingSequenceCount-1, ulistList);
				rlist.put(promisingSequenceCount-1, rlistList);
			}
		} catch (Exception e) {
			// catches exception if error while reading the inputPath file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }

	    // ============================================If in debug mdoe, print debug data==========================================
	    if(DEBUG) {
	    	System.out.println("PROMISING SEQUENCE COUNT: " + promisingSequenceCount);
		    System.out.println("PURE ARRAY STRUCTURE:");
		    System.out.println("SEQ: " + seq);
		    System.out.println("ULIST: " + ulist);
		    System.out.println("RLIST: " + rlist);
		}

		
		ArrayList<String> prefix = new ArrayList<String>();
		Map<Integer, List<Integer>> iuList = getIUList(prefix);
		int asu = getASU(prefix);
		ahusAlgorithm(prefix, iuList, asu);
	}

	private Map<Integer, List<Integer>> getIUList(ArrayList<String> prefix) {
		Map<Integer, List<Integer>> iuList = new HashMap<Integer, List<Integer>>();
		return iuList;
	}

	private int getASU(ArrayList<String> prefix) {
		return 0;
	}

	private void ahusAlgorithm(ArrayList<String> prefix, Map<Integer, List<Integer>> iuList, int asu) {

	}

	public void printResults() {
		// System.out.println(inputPath);
	}
}