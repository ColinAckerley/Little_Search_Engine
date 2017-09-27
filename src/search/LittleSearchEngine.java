package search;
import java.io.*;
import java.util.*;
class Occurrence
{
	String document;
	int frequency;
	public Occurrence(String doc, int freq)
	{
		document = doc;
		frequency = freq;
	}
	public String toString()
	{
		return "(" + document + "," + frequency + ")";
	}
}

public class LittleSearchEngine
{
	HashMap<String, ArrayList<Occurrence>> keywordsIndex;
	HashMap<String, String> noiseWords;
	public LittleSearchEngine()
	{
		keywordsIndex = new HashMap<String, ArrayList<Occurrence>>(1000, 2.0f);
		noiseWords = new HashMap<String, String>(100, 2.0f);
	}
	public void makeIndex(String docsFile, String noiseWordsFile) throws FileNotFoundException
	{
		// load noise words to hash table
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext())
		{
			String word = sc.next();
			noiseWords.put(word, word);
		}
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext())
		{
			String docFile = sc.next();
			HashMap<String, Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
	}
	public HashMap<String, Occurrence> loadKeyWords(String docFile) throws FileNotFoundException
	{
		HashMap<String, Occurrence> keywords = new HashMap<String, Occurrence>();
		File docs = new File(docFile); // Setup stuff & error check
		if(docFile == null)
			throw new FileNotFoundException();
		@SuppressWarnings("resource")
		Scanner words = new Scanner(docs);
		String curWord = null;
		while (words.hasNext()) // While there is still words in document
		{
			curWord = getKeyWord(words.next()); // Current word
			if(curWord != null)
			{
				if(!keywords.containsKey(curWord)) // If not already in list
				{
					// New occurrence w/ frequency of 1
					Occurrence occ = new Occurrence(docFile, 1);
					keywords.put(curWord, occ);
				}
				else if(keywords.containsKey(curWord)) // If already in list
					// Increment the frequency of the word
					keywords.get(curWord).frequency++;
			}
		}
		return keywords; // Return list of keywords
	}
	public void mergeKeyWords(HashMap<String, Occurrence> kws)
	{
		for(String curKey:kws.keySet()) // For every key in the kws keyset
		{
			if(keywordsIndex.containsKey(curKey)) // If key already in
			{
				ArrayList<Occurrence> caseA = keywordsIndex.get(curKey);
				caseA.add(kws.get(curKey)); // Add curKey to caseB
				insertLastOccurrence(caseA); // Insert the last occurence
				keywordsIndex.put(curKey, caseA); // Add this to the keywords
													// index
			}
			else // Else if not already in the keywordsIndex
			{
				ArrayList<Occurrence> caseB = new ArrayList<Occurrence>();
				caseB.add(kws.get(curKey)); // Add current key to array list
				insertLastOccurrence(caseB); // Insert the last occurence
				keywordsIndex.put(curKey, caseB); // Put this into the keywords
													// index
			}
		}
	}
	public String getKeyWord(String word)
	{
		if(word.equals(null) || word == null)
			return null;
		word = word.toLowerCase();
		word = word.trim();
		boolean firstSymbol = false;
		if(Character.isDigit(word.charAt(word.length() - 1)))
			return null;
		for(int i = 0; i < word.length(); i++) // If there is a symbol in the
												// middle of the word
		{
			if(!(Character.isLetter(word.charAt(i))))
				firstSymbol = true;
			if(firstSymbol && Character.isLetter(word.charAt(i)))
				return null;
		}
		while (
			word.endsWith(".") || word.endsWith(",") || word.endsWith("?") || word.endsWith("!")
					|| (word.endsWith(":") || word.endsWith(";"))
		)
		{
			word = word.substring(0, word.length() - 1);
		}
		if(word.length() == 0) // If invalid length
			return null;
		if(!Character.isLetter(word.charAt(word.length() - 1)))
			return null;
		if(noiseWords.containsKey(word)) // Return null if word is a
											// noiseword
			return null;
		System.out.println("Word is " + word);
		return word;
	}
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs)
	{
		if(occs.size() == 1) // If size is invalid
			return null;
		int l = 0; // Setting up variables we will be using
		int h = occs.size() - 2;
		int mid = 0;
		int target = occs.get(occs.size() - 1).frequency; // Value we are
															// looking for
		ArrayList<Integer> midpoints = new ArrayList<Integer>();
		while (h >= l) // Basic binary search
		{
			mid = ((l + h) / 2);
			int data = occs.get(mid).frequency;
			midpoints.add(mid);
			if(data == target)
				break;
			else if(data < target)
			{
				h = mid - 1;
			}
			else if(data > target)
			{
				l = mid + 1;
				if(h <= mid)
					mid = mid + 1;
			}
		}
		Occurrence temp = occs.get(occs.size() - 1);
		occs.add(midpoints.get(midpoints.size() - 1), temp);
		occs.remove(occs.size() - 1);
		return midpoints;
	}
	public ArrayList<String> top5search(String kw1, String kw2)
	{
		ArrayList<String> results = new ArrayList<String>(); // Everything that
																// will be used
		ArrayList<Occurrence> word1 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> word2 = new ArrayList<Occurrence>();
		ArrayList<Occurrence> both = new ArrayList<Occurrence>();
		if(keywordsIndex.containsKey(kw1))
			word1.addAll(keywordsIndex.get(kw1));
		if(keywordsIndex.containsKey(kw2))
			word2.addAll(keywordsIndex.get(kw2));
		both.addAll(word1);
		both.addAll(word2);
		for(int i = 0; i < both.size() - 1; i++) // Sort the arraylist of both
		{
			for(int j = 1; j < both.size() - i; j++)
			{
				if(both.get(j - 1).frequency < both.get(j).frequency)
				{
					Occurrence tmp = both.get(j - 1);
					both.set(j - 1, both.get(j));
					both.set(j, tmp);
				}
			}
		}
		for(int i = 0; i < both.size() - 1; i++) // Remove any duplicate entries
		{
			for(int j = i + 1; j < both.size(); j++)
			{
				if(both.get(i).document == both.get(j).document)
					both.remove(j);
			}
		}
		if(both.size() > 5)
		{
			both = new ArrayList<Occurrence>(both.subList(0, 5));
		}
		System.out.println(both);
		for(Occurrence cur:both)
		{
			results.add(cur.document);
		}
		return results;
	}
}
