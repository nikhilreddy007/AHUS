import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class AHUS4 {
	private int minUtility = 0;
	private ArrayList<ArrayList<Integer>> highUtilityPatterns = new ArrayList<ArrayList<Integer>>();

	private HashMap<Integer, ArrayList<ArrayList<Integer>>> database = null;
	private HashSet<Integer> promisingItems = null;
	private HashSet<Integer> databaseIConcatList = null;
	private HashSet<Integer> databaseSConcatList = null;
	private HashMap<Integer, HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>>> mapIUList = null;
	private HashMap<ArrayList<Integer>, Integer> mapPEU = null;
	private HashMap<ArrayList<Integer>, Integer> mapASU = null;


	public void runAlgorithm(String inputPath, String outputPath, int minUtility) throws IOException {
		this.minUtility = minUtility;

		HashSet<Integer> initialPromisingItems = getPromisingItems(inputPath);
		HashMap<Integer, ArrayList<ArrayList<Integer>>> initialPrunedDatabase = pruneDatabase(initialPromisingItems, inputPath);

		promisingItems = getPromisingItems(initialPrunedDatabase);
		database = pruneDatabase(promisingItems, initialPrunedDatabase);

		printDatabase(database);

		calcDatabaseConcatLists(promisingItems, database);

		calcIUListPEUandASU(promisingItems, database);

		for(int item : promisingItems) {
			ArrayList<Integer> pattern = new ArrayList<Integer>();
			pattern.add(item);
			if(mapASU.get(pattern) >= minUtility) {
				highUtilityPatterns.add(pattern);
			}
			findHUSPs(pattern, mapASU.get(pattern));
		}

		writeResultsToFile(outputPath);

		printHighUtilityPatterns();
	}


	private HashSet<Integer> getPromisingItems(String inputPath) throws IOException {
		HashSet<Integer> promisingItems = new HashSet<Integer>();

		BufferedReader myInput = null;
		String thisLine;
		try {
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputPath))));
			Map<Integer, Integer> initialSWU = new HashMap<Integer, Integer>();
			Set<Integer> consideredItems = new HashSet<Integer>();

			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty()) {
					continue;
				}

				String tokens[] = thisLine.split(" "); 
				
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));
				
				for(int i = 0; i < tokens.length - 3; i++) {
					String currentToken = tokens[i];

					if(currentToken.length() != 0 && !currentToken.equals("-1")) {

						int positionLeftBracketString = currentToken.indexOf('[');

						String itemString = currentToken.substring(0, positionLeftBracketString);
						Integer item = Integer.parseInt(itemString);
						
						if (!consideredItems.contains(item)) {
							consideredItems.add(item);
						
							Integer swu = initialSWU.get(item);
							
							swu = (swu == null) ? sequenceUtility : swu + sequenceUtility;
							initialSWU.put(item, swu);
						}						
					}
				}
	
				consideredItems.clear();
			}

			for(Entry<Integer, Integer> entry : initialSWU.entrySet()) {
				if(entry.getValue() >= minUtility) {
					promisingItems.add(entry.getKey());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(myInput != null){
				myInput.close();
			}
		}

		return promisingItems;
	}


	private HashMap<Integer, ArrayList<ArrayList<Integer>>> pruneDatabase(HashSet<Integer> promisingItemsParam, String inputPath) throws IOException {
		HashSet<Integer> _promisingItems = new HashSet<Integer>(promisingItemsParam);

		HashMap<Integer, ArrayList<ArrayList<Integer>>> _database = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();

		BufferedReader myInput = null;
		String thisLine;
		try {
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(inputPath))));

			int promisingSequenceCount = 0;

			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true) {
					continue;
				}
				
				ArrayList<Integer> seqList = new ArrayList<Integer>();
				ArrayList<Integer> ulistList = new ArrayList<Integer>();
				ArrayList<Integer> rlistList = new ArrayList<Integer>();

				String tokens[] = thisLine.split(" ");
				
				String sequenceUtilityString = tokens[tokens.length-1];
				int positionColons = sequenceUtilityString.indexOf(':');
				int sequenceUtility = Integer.parseInt(sequenceUtilityString.substring(positionColons+1));

				int remSequenceUtility = sequenceUtility;
				for(int i = 0; i < tokens.length - 2; i++) {
					String currentToken = tokens[i];

					if(currentToken.length() != 0 && !currentToken.equals("-1")) {
						int positionLeftBracketString = currentToken.indexOf('[');

						String itemString = currentToken.substring(0, positionLeftBracketString);
						Integer item = Integer.parseInt(itemString);

						String utilityString = currentToken.substring(positionLeftBracketString+1, currentToken.length()-1);
						Integer utility = Integer.parseInt(utilityString);

						if(!_promisingItems.contains(item)) {
							remSequenceUtility -= utility;
							continue;
						}

						seqList.add(item);

						ulistList.add(utility);

						int remUtility = remSequenceUtility - utility;
						rlistList.add(remUtility);
						remSequenceUtility -= utility;
					} else {
						seqList.add(0);
						ulistList.add(0);
						rlistList.add(0);
					}
				}

				if(seqList.size() == 1) {
					continue;
				}

				promisingSequenceCount++;
				
				ArrayList<ArrayList<Integer>> newSequence = new ArrayList<ArrayList<Integer>>();
				newSequence.add(seqList);
				newSequence.add(ulistList);
				newSequence.add(rlistList);

				_database.put(promisingSequenceCount-1, newSequence);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
		}

		return _database;
	}


	private HashSet<Integer> getPromisingItems(HashMap<Integer, ArrayList<ArrayList<Integer>>> databaseParam) {
		HashMap<Integer, ArrayList<ArrayList<Integer>>> _database = new HashMap<Integer, ArrayList<ArrayList<Integer>>>(databaseParam);

		HashSet<Integer> _promisingItems = new HashSet<Integer>();

		Map<Integer, Integer> initialSWU = new HashMap<Integer, Integer>();
		Set<Integer> consideredItems = new HashSet<Integer>();

		for(int i = 0; i < _database.size(); i++) {
			ArrayList<ArrayList<Integer>> sequence = _database.get(i);
			int sequenceUtility = sequence.get(1).get(0) + sequence.get(2).get(0);
			
			for(int j = 0; j < sequence.get(0).size(); j++) {
				Integer item = sequence.get(0).get(j);
				if(!item.equals(0) && !consideredItems.contains(item)) {
					consideredItems.add(item);
					
					Integer swu = initialSWU.get(item);
					
					swu = (swu == null) ? sequenceUtility : swu + sequenceUtility;
					initialSWU.put(item, swu);
				}
			}

			consideredItems.clear();
		}

		for(Entry<Integer, Integer> entry : initialSWU.entrySet()) {
			if(entry.getValue() >= minUtility) {
				_promisingItems.add(entry.getKey());
			}
		}

		return _promisingItems;
	}


	private HashMap<Integer, ArrayList<ArrayList<Integer>>> pruneDatabase(HashSet<Integer> promisingItemsParam, HashMap<Integer, ArrayList<ArrayList<Integer>>> databaseParam) throws IOException {
		HashSet<Integer> _promisingItems = new HashSet<Integer>(promisingItems);
		HashMap<Integer, ArrayList<ArrayList<Integer>>> oldDatabase = new HashMap<Integer, ArrayList<ArrayList<Integer>>>(databaseParam);
		HashMap<Integer, ArrayList<ArrayList<Integer>>> newDatabase = new HashMap<Integer, ArrayList<ArrayList<Integer>>>();

		int promisingSequenceCount = 0;

		for(int i = 0; i < oldDatabase.size(); i++) {
			ArrayList<Integer> seqList = new ArrayList<Integer>();
			ArrayList<Integer> ulistList = new ArrayList<Integer>();
			ArrayList<Integer> rlistList = new ArrayList<Integer>();
			
			ArrayList<ArrayList<Integer>> sequence = oldDatabase.get(i);
			int sequenceUtility = sequence.get(1).get(0) + sequence.get(2).get(0);

			int remSequenceUtility = sequenceUtility;
			for(int j = 0; j < sequence.get(0).size(); j++) {
				Integer item = sequence.get(0).get(j);

				if(!item.equals(0)) {
					Integer utility = sequence.get(1).get(j);

					if(!_promisingItems.contains(item)) {
						remSequenceUtility -= utility;
						continue;
					}

					seqList.add(item);

					ulistList.add(utility);

					int remUtility = remSequenceUtility - utility;
					rlistList.add(remUtility);
					remSequenceUtility -= utility;
				} else {
					seqList.add(0);
					ulistList.add(0);
					rlistList.add(0);
				}
			}

			if(seqList.size() == 1) {
				continue;
			}

			promisingSequenceCount++;
			
			ArrayList<ArrayList<Integer>> newSequence = new ArrayList<ArrayList<Integer>>();
			newSequence.add(seqList);
			newSequence.add(ulistList);
			newSequence.add(rlistList);

			newDatabase.put(promisingSequenceCount-1, newSequence);
		}

		return newDatabase;
	}


	private void printDatabase(HashMap<Integer, ArrayList<ArrayList<Integer>>> _database) {
		System.out.println("DATABASE:");

		for(int i = 0; i < _database.size(); i++) {
			System.out.println("SEQUENCE: " + i);

			int cols = _database.get(i).get(0).size();
			for(int j = 0; j < 3; j++) {
				for(int k = 0; k < cols; k++)
					System.out.print(_database.get(i).get(j).get(k) + "\t");
				System.out.println();
			}
		}
	}


	private void calcDatabaseConcatLists(HashSet<Integer> promisingItemsParam, HashMap<Integer, ArrayList<ArrayList<Integer>>> databaseParam) {
		HashSet<Integer> _promisingItems = new HashSet<Integer>(promisingItemsParam);
		HashMap<Integer, ArrayList<ArrayList<Integer>>> _database = new HashMap<Integer, ArrayList<ArrayList<Integer>>>(databaseParam);

		HashSet<Integer> iConcatList = new HashSet<Integer>();
		HashSet<Integer> sConcatList = new HashSet<Integer>();

		Boolean done = false;
		for(int i = 0; i < _database.size() && !done; i++) {
			ArrayList<ArrayList<Integer>> sequence = _database.get(i);
			done = true;
			int cols = sequence.get(0).size();
			sConcatList.add(sequence.get(0).get(0));
			for(int j = 1; j < cols; j++) {
				int item = sequence.get(0).get(j);
				if(item != 0) {
					if(sequence.get(0).get(j-1) != 0) {
						iConcatList.add(item);
					} else {
						sConcatList.add(item);
					}
				}
			}
			for(Integer item : _promisingItems) {
				if(!iConcatList.contains(item) || !sConcatList.contains(item)) {
					done = false;
					break;
				}
			}
		}
		
		databaseIConcatList = iConcatList;
		databaseSConcatList = sConcatList;
	}


	private void printDatabaseConcatLists(HashSet<Integer> _databaseIConcatList, HashSet<Integer> _databaseSConcatList) {
		System.out.println("DATABASE I CONCAT LIST: ");
		System.out.println(_databaseIConcatList);

		System.out.println("DATABASE S CONCAT LIST: ");
		System.out.println(_databaseSConcatList);
	}


	private void calcIUListPEUandASU(HashSet<Integer> promisingItemsParam, HashMap<Integer, ArrayList<ArrayList<Integer>>> databaseParam) {
		HashSet<Integer> _promisingItems = new HashSet<Integer>(promisingItemsParam);
		HashMap<Integer, ArrayList<ArrayList<Integer>>> _database = new HashMap<Integer, ArrayList<ArrayList<Integer>>>(databaseParam);

		HashMap<Integer ,HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>>> _mapIUList = new HashMap<Integer, HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>>>();
		HashMap<ArrayList<Integer>, Integer> _mapPEU = new HashMap<ArrayList<Integer>, Integer>();
		HashMap<ArrayList<Integer>, Integer> _mapASU = new HashMap<ArrayList<Integer>, Integer>();

		for(int i : _database.keySet()) {
			ArrayList<ArrayList<Integer>> sequence = _database.get(i);
			HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>> iuListInSequence = new HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>>();
			ArrayList<Integer> pattern = null;
			HashMap<ArrayList<Integer>, Integer> maxUtilInSequence = new HashMap<ArrayList<Integer>, Integer>();
			HashMap<ArrayList<Integer>, Integer> maxPEUInSequence = new HashMap<ArrayList<Integer>, Integer>();

			for(int j=0; j < sequence.get(0).size(); j++) {
				if(sequence.get(0).get(j) == 0) {
					continue;
				}

				Integer item = sequence.get(0).get(j);
				pattern = new ArrayList<Integer>();
				pattern.add(item);

				if(iuListInSequence.get(pattern) == null) {
					iuListInSequence.put(pattern, new ArrayList<ArrayList<Integer>>());
					iuListInSequence.get(pattern).add(new ArrayList<Integer>());
					iuListInSequence.get(pattern).add(new ArrayList<Integer>());
					iuListInSequence.get(pattern).add(new ArrayList<Integer>());
				}
				iuListInSequence.get(pattern).get(0).add(j);
				iuListInSequence.get(pattern).get(1).add(sequence.get(1).get(j));
				iuListInSequence.get(pattern).get(2).add(sequence.get(1).get(j) + sequence.get(2).get(j));

				if(maxUtilInSequence.get(pattern) == null) {
					maxUtilInSequence.put(pattern, 0);
				}
				if(maxUtilInSequence.get(pattern) < sequence.get(1).get(j)) {
					maxUtilInSequence.put(pattern, sequence.get(1).get(j));
				}

				if(maxPEUInSequence.get(pattern) == null) {
					maxPEUInSequence.put(pattern, 0);
				}
				if(maxPEUInSequence.get(pattern) < sequence.get(1).get(j) + sequence.get(2).get(j)) {
					maxPEUInSequence.put(pattern, sequence.get(1).get(j) + sequence.get(2).get(j));
				}
			}

			if(_mapIUList.get(i) == null) {
				_mapIUList.put(i, new HashMap<ArrayList<Integer>, ArrayList<ArrayList<Integer>>>());
			}
			_mapIUList.put(i, iuListInSequence);

			for(ArrayList<Integer> pat : maxUtilInSequence.keySet()) {
				_mapASU.put(pat, (_mapASU.get(pat) == null) ? maxUtilInSequence.get(pat) : _mapASU.get(pat) + maxUtilInSequence.get(pat));
				_mapPEU.put(pat, (_mapPEU.get(pat) == null) ? maxPEUInSequence.get(pat) : _mapPEU.get(pat) + maxPEUInSequence.get(pat));
			}
		}

		mapIUList = _mapIUList;
		mapASU = _mapASU;
		mapPEU = _mapPEU;
	}


	private void calcIUListPEUandASU(ArrayList<Integer> pattern, Integer item, char concatType) {
		ArrayList<Integer> prefixPattern = new ArrayList<Integer>(pattern);
		ArrayList<Integer> itemPattern = new ArrayList<Integer>();
		itemPattern.add(item);

		ArrayList<Integer> extPattern = new ArrayList<Integer>(prefixPattern);
		if(concatType == 'i') {
			extPattern.add(item);
		} else {
			extPattern.add(0);
			extPattern.add(item);
		}

		for(int i : database.keySet()) {
			if(!mapIUList.get(i).keySet().contains(prefixPattern) || !mapIUList.get(i).keySet().contains(itemPattern)) {
				continue;
			}

			ArrayList<ArrayList<Integer>> sequence = database.get(i);
			
			ArrayList<ArrayList<Integer>> itemIUList = mapIUList.get(i).get(itemPattern);
			ArrayList<ArrayList<Integer>> prefixIUList = mapIUList.get(i).get(prefixPattern);
			ArrayList<ArrayList<Integer>> extIUList = new ArrayList<ArrayList<Integer>>();

			for(int itemIndex : itemIUList.get(0)) {
				int rightBound = -1;

				if(concatType == 'i') {
					rightBound = itemIndex-1;
				} else {
					for(int j = itemIndex-1; j >= 0; j--) {
						if(sequence.get(0).get(j) == 0) {
							rightBound = j-1;
							break;
						}
					}
				}

				if(rightBound == -1) {
					continue;
				}

				int max = 0;
				ArrayList<Integer> prefixIndexList = prefixIUList.get(0);
				for(int prefixIndex = 0; prefixIndex < prefixIndexList.size(); prefixIndex++) {
					if(prefixIndexList.get(prefixIndex) <= rightBound) {
						max = (max < prefixIUList.get(1).get(prefixIndex)) ? prefixIUList.get(1).get(prefixIndex) : max;
					} else {
						break;
					}
				}

				if(max != 0) {
					if(extIUList.size() == 0) {
						extIUList.add(new ArrayList<Integer>());
						extIUList.add(new ArrayList<Integer>());
						extIUList.add(new ArrayList<Integer>());
					}
					extIUList.get(0).add(itemIndex);
					extIUList.get(1).add(max + sequence.get(1).get(itemIndex));
					extIUList.get(2).add(max + sequence.get(1).get(itemIndex) + sequence.get(2).get(itemIndex));
				}
			}

			if(extIUList.size() != 0) {
				mapIUList.get(i).put(extPattern, extIUList);

				int maxPEUInSequence = 0, maxUtilInSequence = 0;
				for(int index = 0; index < extIUList.get(0).size(); index++) {
					maxPEUInSequence = (extIUList.get(2).get(index) > maxPEUInSequence) ? extIUList.get(2).get(index) : maxPEUInSequence;
					maxUtilInSequence = (extIUList.get(1).get(index) > maxUtilInSequence) ? extIUList.get(1).get(index) : maxUtilInSequence;
				}
				mapPEU.put(extPattern, (mapPEU.keySet().contains(extPattern) ? mapPEU.get(extPattern) + maxPEUInSequence : maxPEUInSequence));
				mapASU.put(extPattern, (mapASU.keySet().contains(extPattern) ? mapASU.get(extPattern) + maxUtilInSequence : maxUtilInSequence));
			}
		}
	}


	private int getRSU(ArrayList<Integer> pattern, Integer item, char concatType, Integer seqNum, Integer prefixPeu) {
		ArrayList<Integer> prefixPattern = new ArrayList<Integer>(pattern);

		ArrayList<Integer> extPattern = new ArrayList<Integer>(prefixPattern);

		ArrayList<Integer> prefixIndexList = mapIUList.get(seqNum).get(prefixPattern).get(0);

		boolean extPatternExists = false;
		ArrayList<Integer> seq = database.get(seqNum).get(0);

		if(concatType == 'i') {
			for(int prefixIndex : prefixIndexList) {
				boolean itemsetEnded = false;
				for(int index = prefixIndex; index < seq.size(); index++) {
					if(seq.get(index) == 0) {
						itemsetEnded = true;
						break;
					}
					if(!itemsetEnded && seq.get(index) == item) {
						extPatternExists = true;

						extPattern.add(item);

						break;
					}
				}
				if(itemsetEnded || extPatternExists)
					break;
			}
		} else {
			int minIndex = Integer.MAX_VALUE;
			for(int index : prefixIndexList) {
				minIndex = (index < minIndex) ? index : minIndex;
			}

			boolean seqEnded = false;
			for(int index = minIndex; index < seq.size(); index++) {
				if(seq.get(index) == 0) {
					seqEnded = true;
					continue;
				}
				if(seqEnded && seq.get(index) == item) {
					extPatternExists = true;

					extPattern.add(0);
					extPattern.add(item);
					
					break;
				}
			}

		}

		if(extPatternExists) {
			return prefixPeu;
		}
		return 0;
	}


	private void findHUSPs(ArrayList<Integer> pattern, Integer _asuP) {
		ArrayList<Integer> prefixPattern = new ArrayList<Integer>(pattern);
		HashMap<Integer, Integer> peuMap = new HashMap<Integer, Integer>();

		Integer peuP = 0;
		for(int seq : database.keySet()) {
			if(!mapIUList.get(seq).keySet().contains(prefixPattern)) {
				continue;
			}

			ArrayList<Integer> peuList =  mapIUList.get(seq).get(prefixPattern).get(2);
			int max = 0;
			for(int peu : peuList) {
				max = (peu > max) ? peu : max;
			}
			peuMap.put(seq, max);
			peuP += max;
		}

		if(peuP < minUtility)
			return;

		for(Integer item : promisingItems) {
			ArrayList<Integer> itemPattern = new ArrayList<Integer>();
			itemPattern.add(item);

			if(_asuP + mapPEU.get(itemPattern) >= minUtility) {
				boolean canIConcat = true;
				for(int i =prefixPattern.size()-1; i >= 0; i--) {
					if(prefixPattern.get(i).equals(item)) {
						canIConcat = false;
						break;
					}
					if(prefixPattern.get(i).equals(0)) {
						canIConcat = true;
						break;
					}
				}

				if(canIConcat && databaseIConcatList.contains(item)) {
					for(int i : database.keySet()) {
						if(!mapIUList.get(i).keySet().contains(prefixPattern) || !mapIUList.get(i).keySet().contains(itemPattern)) {
							continue;
						}
						// System.out.println();
						if (getRSU(prefixPattern, item, 'i', i, peuMap.get(i)) >= minUtility) {
							calcIUListPEUandASU(prefixPattern, item, 'i');

							ArrayList<Integer> extPattern = new ArrayList<Integer>(prefixPattern);
							extPattern.add(item);

							// System.out.println(extPattern + "\tin\t" + i + "\twith\t" + 'i');
							
							if(mapASU.get(extPattern) >= minUtility)
								highUtilityPatterns.add(extPattern);

							findHUSPs(extPattern, mapASU.get(extPattern));
						}
					}
				}

				if(databaseSConcatList.contains(item)) {
					for(int i : database.keySet()) {
						if(!mapIUList.get(i).keySet().contains(prefixPattern) || !mapIUList.get(i).keySet().contains(itemPattern)) {
							continue;
						}

						if (getRSU(prefixPattern, item, 's', i, peuMap.get(i)) >= minUtility) {
							calcIUListPEUandASU(prefixPattern, item, 's');

							ArrayList<Integer> extPattern = new ArrayList<Integer>(prefixPattern);
							extPattern.add(0);
							extPattern.add(item);
							
							if(mapASU.get(extPattern) >= minUtility)
								highUtilityPatterns.add(extPattern);
							
							findHUSPs(extPattern, mapASU.get(extPattern));
						}
					}
				}
			}
		}
	}


	private void writeResultsToFile(String outputPath) {
	    String lineSeparator = System.getProperty("line.separator");

	    try (FileOutputStream fos = new FileOutputStream(outputPath)) {
	    	for(ArrayList<Integer> pattern : highUtilityPatterns) {
	    		for(Integer item : pattern) {
	    			fos.write(Integer.toString(item).getBytes());
	    			fos.write("\t".getBytes());
	    		}
				fos.write(lineSeparator.getBytes());
	    	}
			fos.flush();
	    } catch (Exception e2) {
	      e2.printStackTrace();
	    }
	}


	private void printHighUtilityPatterns() {
		System.out.println("HIGH UTILITY PATTERNS:");
		
		for(ArrayList<Integer> pattern : highUtilityPatterns) {
			System.out.println(pattern + "\tutility: " + mapASU.get(pattern));
		}

		System.out.println("HIGH UTILITY PATTERN COUNT: " + highUtilityPatterns.size());
	}
}