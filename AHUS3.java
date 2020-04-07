import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class AHUS3 {
	String inputPath;
	int minUtility = 0;

	final boolean DEBUG = true;

	final Map<Integer, ArrayList[]> database = new HashMap<Integer, ArrayList[]>();
	private ArrayList<ArrayList<Integer>> highUtilityPatterns = new ArrayList<ArrayList<Integer>>();

	private Set<Integer> promisingItems = new HashSet<Integer>();
	private Set<Integer> databaseIConcatList = new HashSet<Integer>();
	private Set<Integer> databaseSConcatList = new HashSet<Integer>();
	private int promisingSequenceCount = 0;

	public void runAlgorithm(String inputPath, String outputPath, int minUtility) throws IOException {
		this.inputPath = inputPath;
		this.minUtility = minUtility;
		final Map<Integer, Integer> mapItemToSWU = new HashMap<Integer, Integer>();
		List<Integer> consideredItems = new ArrayList<Integer>();

		// ========================================= Database scan 1 - Calculating SWU =================================================
		int sequenceCount = 0;
		myInput = null;
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
						Integer item = Integer.parseInt(itemString);
						
						//Tin added:		
						if (!consideredItems.contains(item)) {
							consideredItems.add(item);
						
							// get the current SWU of that item
							Integer swu = mapItemToSWU.get(item);
							
							// add the utility of sequences.add(); utility to the swu of this item
							swu = (swu == null) ? sequenceUtility : swu + sequenceUtility;
							mapItemToSWU.put(item, swu);
//							System.out.println(" -> " + swu);
						}
//						else System.out.println(item + " is already considered in the same (line) inputPath sequences.add();");								
					}
				}
				
				consideredItems.clear();
				
				// increase sequences.add(); count
				sequenceCount++;
			}

			// compare SWU of all items with minUtility and prune unpromising items
			for(Entry<Integer, Integer> entry : mapItemToSWU.entrySet()) {
				if(entry.getValue() >= minUtility) {
					promisingItems.add(entry.getKey());
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the inputPath file
			e.printStackTrace();
		} finally {
			if(myInput != null){
				myInput.close();
			}
		}

		// ============================================If in debug mdoe, print debug data==========================================
		if(DEBUG) {
			System.out.println("INITIAL ITEM COUNT " + mapItemToSWU.size());
			System.out.println("SEQUENCE COUNT = " + sequenceCount);
			System.out.println("INITIAL SWU OF ITEMS");
			for(Entry<Integer, Integer> entry : mapItemToSWU.entrySet()) {
				System.out.println("Item: " + entry.getKey() + " swu: " + entry.getValue());
			}
			System.out.println("PROMISING ITEMS:");
			for(Integer i : promisingItems) {
				System.out.println(i);
			}
		}

		// =============================== Database scan 2 - creating seq, ulist, rlist of all sequences===========================
		myInput = null;

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputPath))));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is a comment, is  empty or is a kind of metadata, skip it
				if (thisLine.isEmpty() == true) {
					continue;
				}
				
				// System.out.println(sequence);
				ArrayList<String> seqList = new ArrayList<String>();
				ArrayList<Integer> ulistList = new ArrayList<Integer>();
				ArrayList<Integer> rlistList = new ArrayList<Integer>();

				// split the transaction according to the " " separator
				String tokens[] = thisLine.split(" ");
				
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));

				// System.out.println(sequence);
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
						String item = currentToken.substring(0, positionLeftBracketString);
						// Integer item = Integer.parseInt(itemString);

						String utilityString = currentToken.substring(positionLeftBracketString+1, currentToken.length()-1);
						Integer utility = Integer.parseInt(utilityString);

						//Don't add unpromising items
						if(!promisingItems.contains(item)) {
							remSequenceUtility -= utility;
							continue;
						}

						seqList.add(item);

						ulistList.add(utility);
						// sequence[1][j] = utility;

						int remUtility = remSequenceUtility - utility;
						rlistList.add(remUtility);
						remSequenceUtility -= utility;
						// sequence[2][j] = remUtility;
					} else {
						seqList.add("0");
						ulistList.add(0);
						rlistList.add(0);
					}
					// j++;
					// System.out.println(seq);
				}

				//Don't add empty sequences
				if(seqList.size() == 1) {
					continue;
				}

				promisingSequenceCount++;
				
				// System.out.println(seqList);
				ArrayList[] sequence = new ArrayList[3];
				sequence[0] = seqList;
				sequence[1] = ulistList;
				sequence[2] = rlistList;
				// for(int i=0; i < seqList.size(); i++) {
					// sequence[0][i] = seqList.get(i);
					// sequence[1][i] = ulistList.get(i);
					// sequence[2][i] = rlistList.get(i);
				// }
				database.put(promisingSequenceCount-1, sequence);
				// seq.put(promisingSequenceCount-1, seqList);
				// ulist.put(promisingSequenceCount-1, ulistList);
				// rlist.put(promisingSequenceCount-1, rlistList);
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
			for(int i = 0; i < database.size(); i++) {
				System.out.println("SEQUENCE: " + i);
				int cols = database.get(i)[0].length;
				for(int j = 0; j < 3; j++) {
					for(int k = 0; k < cols; k++)
						System.out.print(database.get(i)[j][k] + "\t");
					System.out.println();
				}
			}
		}

		// construct database iConcat and sConcat list
		for(int i = 0; i < database.size(); i++) {
			int cols = database.get(i)[0].length;
			databaseSConcatList.add(database.get(i)[0][0]);
			for(int j = 1; j < cols; j++) {
				int item = database.get(i)[0][j];
				if(item != 0) {
					if(database.get(i)[0][j-1] != 0) {
						databaseIConcatList.add(item);
					} else {
						databaseSConcatList.add(item);
					}
				}
			}
			for(Integer item : promisingItems) {
				if(!databaseIConcatList.contains(item) || !databaseSConcatList.contains(item))
					break;
			}
		}


		// Main Algorithm
		for(int item : promisingItems) {
			ArrayList<Integer> pattern = new ArrayList<Integer>();
			pattern.add(item);
			pattern.add(0);
			calcIUListAndPeu(pattern);
			calcASU(pattern);
			// System.out.println(mapIUList);
			// System.out.println(mapASU);
		}

		for(int item : promisingItems) {
			ArrayList<Integer> pattern = new ArrayList<Integer>();
			pattern.add(item);
			pattern.add(0);
			int asu = mapASU.get(pattern);
			if(asu >= minUtility) {
				highUtilityPatterns.add(pattern);
			}
			findHUSPs(pattern, mapIUList.get(pattern), mapPEU.get(pattern), mapASU.get(pattern));
		}
	}


	private static Map<ArrayList<Integer>, HashMap<Integer, Integer[][]>> mapIUList = new HashMap<ArrayList<Integer>, HashMap<Integer, Integer[][]>>();
	private static Map<ArrayList<Integer>, Integer> mapPEU = new HashMap<ArrayList<Integer>, Integer>();
	private static Map<ArrayList<Integer>, Integer> mapASU = new HashMap<ArrayList<Integer>, Integer>();


	private int getRSU(ArrayList<Integer> pattern, Integer item, char concatType) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);
		// System.out.print(pattern + "\t");
		// System.out.print(item + "\t");
		// System.out.println(concatType);
		if(concatType == 'i') {
			_pattern.add(_pattern.size()-1, item);
		} else {
			_pattern.add(item);
			_pattern.add(0);
		}

		// System.out.println(_pattern);
		calcIUListAndPeu(_pattern);
		if(mapIUList.keySet().contains(_pattern)) {
			// System.out.println(mapIUList.get(_pattern) + "returned");
			return mapPEU.get(pattern); // optimise
		}
		return 0;
	}


	// calculates the iulist, peu, asu of a given pattern and stores them
	private void calcIUListAndPeu(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		if(mapIUList.keySet().contains(_pattern))
			return;

		HashMap<Integer, Integer[][]> iuList = new HashMap<Integer, Integer[][]>();
		int peu = 0;

		if(pattern.size() == 2) {
			peu = 0;
			for(int i : database.keySet()) {
				ArrayList<Integer> index = new ArrayList<Integer>();
				ArrayList<Integer> utility = new ArrayList<Integer>();
				for(int j = 0; j < database.get(i)[0].length; j++) {
					if(database.get(i)[0][j] == _pattern.get(0)) {
						index.add(j);
						utility.add(database.get(i)[1][j]);
					}
				}
				Integer[][] iuListInSeq = new Integer[2][index.size()];
				for(int k = 0; k < index.size(); k++) {
					iuListInSeq[0][k] = index.get(k);
					iuListInSeq[1][k] = utility.get(k);
				}

				if(iuListInSeq[0].length > 0) {
					iuList.put(i, iuListInSeq);

					peu += iuListInSeq[1][0] + database.get(i)[2][iuListInSeq[0][0]];
				}
			}
			if(iuList.size() > 0) {
				mapIUList.put(_pattern, iuList);
				mapPEU.put(_pattern, peu);
			}
			return;
		}

		char concatType = 'i';
		int lastItem = _pattern.get(_pattern.size()-2);
		_pattern.remove(_pattern.size()-2);
		if(_pattern.get(_pattern.size()-2) == 0) {
			_pattern.remove(_pattern.size()-2);
			concatType = 's';
		}
		// calcIUListAndPeu(_pattern);

		// make index list of pattern from _pattern
		HashMap<Integer, Integer[][]> oldIUList = new HashMap<Integer, Integer[][]>(mapIUList.get(_pattern));

		for(int i : oldIUList.keySet()) {
			Integer[][] seqIUList = oldIUList.get(i);
			Integer[][] seqData = database.get(i);
			ArrayList<Integer> newIndexList = new ArrayList<Integer>();

			// for(int oldIndex : seqIUList[0]) {
			// 	if(concatType == 'i') {
			// 		for(int x = seqData[0][oldIndex+1]; x < seqData[0].length; x++) {
			// 			if(seqData[0][x] == 0) {
			// 				break;
			// 			}
			// 			if(seqData[0][x] == lastItem) { 
			// 				newIndexList.add(x);
			// 				break;
			// 			}
			// 		}
			// 	}
			// 	if(concatType == 's') {
			// 		boolean seqEnded = false;
			// 		for(int x = seqData[0][oldIndex+1]; x < seqData[0].length; x++) {
			// 			if(seqData[0][x] == 0) {
			// 				seqEnded = true;
			// 			}
			// 			if(seqEnded && seqData[0][x] == lastItem) {
			// 				newIndexList.add(x);
			// 			}
			// 		}
			// 	}
			// }

			// Integer[][] iuListInSeq = new Integer[2][newIndexList.size()];
			// for(int newIndex = 0; newIndex < newIndexList.size(); newIndex++) {
			// 	int max = 0;
			// 	for(int oldIndex = 0; oldIndex < oldIUList.get(i)[0].length; oldIndex++) {
			// 		if(oldIUList.get(i)[0][oldIndex] < newIndexList.get(newIndex)) {
			// 			if(max < oldIUList.get(i)[1][oldIndex]) {
			// 				max = oldIUList.get(i)[1][oldIndex];
			// 			}
			// 		}
			// 	}
			// 	if(newIndexList.size() != 0) {
			// 		iuListInSeq[0][newIndex] = newIndexList.get(newIndex);
			// 		iuListInSeq[1][newIndex] = max + seqData[1][newIndexList.get(newIndex)];
			// 	}
			// }
			// if(iuListInSeq[0].length != 0) {
			// 	iuList.put(i, iuListInSeq);
			// 	peu += iuListInSeq[1][0] + database.get(i)[2][iuListInSeq[0][0]];
			// }

		}
		if(iuList.size() != 0) {
			mapIUList.put(pattern, iuList);
			mapPEU.put(pattern, peu);
		}
		return;
	}


	private void calcASU(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		int asu = 0;
		for(int i : mapIUList.get(_pattern).keySet()) {
			Integer[] utilListP = mapIUList.get(_pattern).get(i)[1];
			// System.out.println(iuListInSeq[1].length);
			int max = 0;
			for(int j = 0; j < utilListP.length; j++)
				if(utilListP[j] > max)
					max = utilListP[j];
			asu += max;
		}
		mapASU.put(_pattern, asu);
	}


	private void findHUSPs(ArrayList<Integer> pattern, Map<Integer, Integer[][]> iuListP, Integer _peuP, Integer _asuP) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);
		Map<Integer, Integer[][]> _iuListP = new HashMap<Integer, Integer[][]>(iuListP);

		if(_peuP < minUtility)
			return;

		Set<Integer> iConcatList = new HashSet<Integer>();
		Set<Integer> sConcatList = new HashSet<Integer>();

		for(Integer item : promisingItems) {
			ArrayList<Integer> itemArrayList = new ArrayList<Integer>();
			itemArrayList.add(item);
			itemArrayList.add(0);
			if(_asuP + mapPEU.get(itemArrayList) >= minUtility) {
				boolean canIConcat = true;
				for(int i =_pattern.size()-2; i >= 0; i--) {
					if(_pattern.get(i).equals(item)) {
						canIConcat = false;
						break;
					}
					if(_pattern.get(i).equals(0)) {
						canIConcat = true;
						break;
					}
				}
				if(canIConcat && databaseIConcatList.contains(item) && getRSU(_pattern, item, 'i') >= minUtility) // calc iulist, peu
					iConcatList.add(item);
				if(databaseSConcatList.contains(item) && getRSU(_pattern, item, 's') >= minUtility)
					sConcatList.add(item);
			}
		}

		for(Integer item : iConcatList) {
			ArrayList<Integer> extPattern = new ArrayList<Integer>(_pattern);
			extPattern.add(extPattern.size()-1, item);
			calcASU(extPattern);
			if(mapASU.get(extPattern) >= minUtility)
				highUtilityPatterns.add(extPattern);
			findHUSPs(extPattern, mapIUList.get(extPattern), mapPEU.get(extPattern), mapASU.get(extPattern));
		}

		for(Integer item: sConcatList) {
			ArrayList<Integer> extPattern = new ArrayList<Integer>(_pattern);
			extPattern.add(item);
			extPattern.add(0);
			calcASU(extPattern);
			if(mapASU.get(extPattern) >= minUtility)
				highUtilityPatterns.add(extPattern);
			findHUSPs(extPattern, mapIUList.get(extPattern), mapPEU.get(extPattern), mapASU.get(extPattern));
		}

	}

	public void printHUSPs() {
		System.out.println("HIGH UTILITY PATTERNS:");
		System.out.println(highUtilityPatterns);
	}
}