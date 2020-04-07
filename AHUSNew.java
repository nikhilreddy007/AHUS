import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class AHUSNew {
	String inputPath;
	int minUtility = 0;

	final boolean DEBUG = true;

	final Map<Integer, Integer[][]> database = new HashMap<Integer, Integer[][]>();
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
				
				// Philippe added:
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
				ArrayList<Integer> seqList = new ArrayList<Integer>();
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
						String itemString = currentToken.substring(0, positionLeftBracketString);
						Integer item = Integer.parseInt(itemString);

						//Don't add unpromising items
						if(!promisingItems.contains(item)) {
							continue;
						}

						seqList.add(item);

						String utilityString = currentToken.substring(positionLeftBracketString+1, currentToken.length()-1);
						Integer utility = Integer.parseInt(utilityString);
						ulistList.add(utility);
						// sequence[1][j] = utility;

						int remUtility = remSequenceUtility - utility;
						remSequenceUtility -= utility;
						rlistList.add(remUtility);
						// sequence[2][j] = remUtility;
					} else {
						seqList.add(0);
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
				Integer[][] sequence = new Integer[3][seqList.size()];
				for(int i=0; i < seqList.size(); i++) {
					sequence[0][i] = seqList.get(i);
					sequence[1][i] = ulistList.get(i);
					sequence[2][i] = rlistList.get(i);
				}
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
		// for each primising item, find all husps

		for(int item : promisingItems) {
			ArrayList<Integer> pattern = new ArrayList<Integer>();
			pattern.add(item);
			HashMap<Integer, Integer[][]> iulist = getIUList(pattern);
			int peu = getPEU(pattern);
		}

		for(int item : promisingItems) {
			ArrayList<Integer> pattern = new ArrayList<Integer>();
			pattern.add(item);
			int asu = getASU(pattern);
			if(asu >= minUtility) {
				highUtilityPatterns.add(pattern);
			}
			findHUSPs(pattern);
		}
	}

	// collection of iulist of all the patterns that are generated
	private static Map<ArrayList<Integer>, HashMap<Integer, Integer[][]>> mapIUList = new HashMap<ArrayList<Integer>, HashMap<Integer, Integer[][]>>();

	// calculates the iulist of a given pattern and stores it in mapIUList
	private Map<Integer, Integer[][]> getIUList(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		if(mapIUList.get(_pattern) != null) {
			return mapIUList.get(_pattern);
		}
		Map<Integer, Integer[][]> iuList = new HashMap<Integer, Integer[][]>();

		return iuList;
	}


	private int getASU(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		Map<Integer, Integer[][]> iuListP = getIUList(_pattern);
		int asu = 0;
		for(int i = 0; i < iuListP.size(); i++) {
			Integer[] utilListP = iuListP.get(i)[1];
			// System.out.println(iuListInSeq[1].length);
			int max = 0;
			for(int j = 0; j < utilListP.length; j++)
				if(utilListP[j] > max)
					max = utilListP[j];
			asu += max;
		}
		return asu;
	}


	private static Map<ArrayList<Integer>, Integer> mapPEU = new HashMap<ArrayList<Integer>, Integer>();

	private int getPEU(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		if(mapPEU.get(_pattern) != null) {
			return mapPEU.get(_pattern);
		}
		Map<Integer, Integer[][]> iuList = getIUList(_pattern);
		int peu = 0;
		for(Integer i : iuList.keySet()) {
			peu += iuList.get(i)[1][0] + database.get(i)[2][iuListI.get(i)[0][0]];
		}
		mapPEU.put(_pattern, peu);
		return peu;
	}

	private int getRSU(ArrayList<Integer> pattern, Integer item, char concatType) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		if(concatType == 'i') {
			_pattern.add(item);
		} else {
			_pattern.add(0);
			_pattern.add(item);
		}

		Map<Integer, Integer[][]> iuList = getIUList(_pattern);
		if(iuList.size() != 0) {
			return getPEU(new ArrayList<Integer>(pattern)); // optimise
		}
		return 0;
	}

	private int getEUU(ArrayList<Integer> pattern, Integer item) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);

		Map<Integer, Integer[][]> iuListP = getIUList(_pattern);
		int asuP = 0;
		for(int i = 0; i < iuListP.size(); i++) {
			Integer[] utilListP = iuListP.get(i)[1];
			// System.out.println(iuListInSeq[1].length);
			int max = 0;
			for(int j = 0; j < utilListP.length; j++)
				if(utilListP[j] > max)
					max = utilListP[j];
			asuP += max;
		}

		ArrayList<Integer> _i = new ArrayList<Integer>();
		_i.add(item);
		return asuP + mapPEU.get(_i);
	}

	private void findHUSPs(ArrayList<Integer> pattern) {
		ArrayList<Integer> _pattern = new ArrayList<Integer>(pattern);
		if(getPEU(pattern) < minUtility)
			return;

		Set<Integer> iConcatList = new HashSet<Integer>();
		Set<Integer> sConcatList = new HashSet<Integer>();

		for(Integer item : promisingItems) {
			if(getEUU(_pattern, item) >= minUtility) {
				boolean canIConcat = false;
				for(int i =_pattern.size()-1; i >= 0; i--) {
					if(pattern.get(i) == item) {
						canIConcat = false;
						break;
					}
					if(_pattern.get(i) == 0) {
						canIConcat = true;
						break;
					}
				}
				if(canIConcat && databaseIConcatList.contains(item) && getRSU(_pattern, item, 'i') >= minUtility)
					iConcatList.add(item);
				if(databaseSConcatList.contains(item) && getRSU(_pattern, item, 'i') >= minUtility)
					sConcatList.add(item);
			}

			// if(getRSU(_pattern, item, 'i') == 0)
			// 	iConcatList.remove(item);
			// if(getRSU(_pattern, item, 's') == 0)
			// 	sConcatList.remove(item);
		}

		for(Integer item : iConcatList) {
			ArrayList<Integer> extPattern = new ArrayList<Integer>(_pattern);
			extPattern.add(item);
			if(getASU(extPattern) >= minUtility)
				highUtilityPatterns.add(extPattern);
			findHUSPs(extPattern);
		}

		for(Integer item: sConcatList) {
			ArrayList<Integer> extPattern = new ArrayList<Integer>(_pattern);
			extPattern.add(0);
			extPattern.add(item);
			if(getASU(extPattern) >= minUtility)
				highUtilityPatterns.add(extPattern);
			findHUSPs(extPattern);
		}

	}

	public void printHUSPs() {
		System.out.println("HIGH UTILITY PATTERNS:");
		System.out.println(highUtilityPatterns);
	}
}