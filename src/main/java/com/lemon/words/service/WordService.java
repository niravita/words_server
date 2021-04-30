package com.lemon.words.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.lemon.words.model.WordOccurrences;

/**
 * The service that takes care of word business logic
 *
 */
@Service
public class WordService {

	// High level description of the solution:
	// ==========================================
	// We will maintain a sorted array with the words occurrences object which is sortable.
	// When we add \ update an element we simply execute a binary search (log(n) complexity) in order to identify its new position
	// Once we have such array - to get the n-th ranked word is simply getting the n-th item in this sorted array.
	
	// Optimization concerns:
	// ==========================
	// Iv'e optimized for faster /word_ranking retrieval making the /words entrypoint a bit slower. Since my array is always sorted on update the get rank is fast.
	// We could of optimized for faster update (/words) instead - by storing the input very fast without any process and on retrieval (/word_ranking) spend some processing to find the results.
	// I'm maintaining an additional Hashmap to quickly find elements (words) on the sorted array (or if they exists at all)
	// 

	// Concurrency:
	// ================
	// We will use thread safe data structures in order to avoid data access issues
	// Our main update method will be synchronized too.
	// This will incur some performance degradation.
	// We could invest more time to use some sort of a smart locking to allow non synchronized method for better performance.
	
	//	
	// a helper map to quickly find \ check if words exists in the sorted array
	private Map<String, WordOccurrences> wordToOccurrencesMap;

	// Our main sorted array, the array is in ascending order from most occurrences count to least occurrences with lexicographic break even.
	private List<WordOccurrences> occurrencesArray;

	// Some constant strings used in the service
	private static final String WHITE_SPACE = " ";
	private static final String MINUS_SIGN = "-";
	public static final String STATE_FILENAME = "state.txt";
	private static final String FILE_TYPE = "file";
	private static final String URL_TYPE = "url";
	private static final String STRING_TYPE = "string";
	private static final String EMPTY_STRING = "";
	private static final String COMMA_SIGN = ",";
	
	
	// Toggling persist on save on\off (see saveMapToDisk() comments for more information)
	private static boolean persistOnUpdate = true; 
	
	
	/**
	 * This method will be called upon servers start.
	 * It will initialize all of the classes data structures.
	 * The method will load an existing state from the disk - if such state exists.
	 * 
	 * @throws Exception
	 */
	@PostConstruct
	public void init() throws Exception {
		this.wordToOccurrencesMap = new ConcurrentHashMap<>();
		this.occurrencesArray = Collections.synchronizedList(new ArrayList<>());
		this.loadMapFromDisk();
	}

	/**
	 * The method that handles the posting of new words.
	 * 
	 * Instead of a switch case we could of used polymorphism \ factory \ mapper by type.
	 * Since there are only 3 options here (and no future plan to extend this functionality) we will use keep the switch case here.
	 * 
	 * Remark regarding the type='url':
	 * The data parameter should be a URL to a file for example 'http://www.mit.edu/~ecprice/wordlist.10000'
	 * if the URL provided is a standard web page and not a file then the method will count all HTML content including the HTML tags \ javascript code etc..
	 * 
	 * @param type
	 * @param data
	 * @throws Exception
	 */
	public void postWords(String type, String data) throws Exception {

		switch (type) {
		case STRING_TYPE:
			handlePostString(data);
			break;
		case URL_TYPE:
			handlePostUrl(data);
			break;
		case FILE_TYPE:
			handlePostFile(data);
			break;
		default:
			throw new IllegalStateException();
		}
		if (persistOnUpdate) {
			saveMapToDisk();
		}
	}

	/**
	 * Get the file inputStream and handle it line by line
	 * No file validations here (access, exist etc..)
	 * 
	 * Security concern - filename is provided by the client. this is a vulnerability but i ignore that here.
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void handlePostFile(String filename) throws Exception {
		File initialFile = new File(filename);
		InputStream is = new FileInputStream(initialFile);
		this.handleWordsStream(is);
	}

	/**
	 * Get the URL inputStream and handle it line by line
	 * Again, no URL validations here (access, exist etc..)
	 * 
	 * Security concern - url is provided by the client. this is a vulnerability but i ignore that here.
	 * 
	 * @param url
	 * @throws Exception
	 */
	private void handlePostUrl(String url) throws Exception {
		try (InputStream is = new URL(url).openConnection().getInputStream()) {
			handleWordsStream(is);
		}
	}
	/**
	 * Get the string's inputStream and handle it line by line
	 * 
	 * @param data
	 * @throws IOException
	 */
	private void handlePostString(String data) throws IOException {
		try (InputStream inputStream = new ByteArrayInputStream(data.getBytes())) {
			handleWordsStream(inputStream);
		}
	}

	/**
	 * This method receives an input stream, reads it line by line, 
	 * stripping the strings for relevant characters and increment the words occurrences for these words.
	 *  
	 * @param inputStream
	 */
	private void handleWordsStream(InputStream inputStream) {
		String[] wordsInLine = null;
		try (Scanner sc = new Scanner(inputStream, StandardCharsets.UTF_8)) {
			while (sc.hasNextLine()) {
				wordsInLine = stripLineForWordsAndSpaces(sc.nextLine());
				for (String word : wordsInLine) {
					if (isNullOrEmpty(word)) {
						continue;
					}
					incrementWordOccurrences(word.toLowerCase());
				}
			}
		}
	}

	private boolean isNullOrEmpty(String word) {
		return word == null || word.equals(EMPTY_STRING);
	}
	
	/**
	 * Clearing mid word dashes. For example:  'tel-aviv' -> 'tel aviv' 
	 * Leaving only alphabetical words. For example:  'tel aviv321' -> 'tel aviv'
	 * 
	 * @param sc
	 * @return
	 */
	private String[] stripLineForWordsAndSpaces(String lineString) {
		lineString = StringUtils.replace(lineString, MINUS_SIGN, WHITE_SPACE);
		return lineString.split(WHITE_SPACE);
	}


	/**
	 * Here we update a given word into our sorted array.
	 * 
	 * @param word
	 */
	private synchronized void incrementWordOccurrences(String word) {
		
		if (!wordToOccurrencesMap.containsKey(word)) {
			addNewWord(word);
		} else {
			incrementExistingWord(word);
		}
	}

	/**
	 * Increment an existing element can cause its position to change
	 * This is why we remove it from the array and re positioning it
	 * Both of these are using binary search O(log(n))
	 * 
	 * @param word
	 */
	private void incrementExistingWord(String word) {
		WordOccurrences wo = wordToOccurrencesMap.get(word);
		int currentIndex = Collections.binarySearch(occurrencesArray, wo);
		occurrencesArray.remove(currentIndex);
		wo.setCount(wo.getCount() + 1);
		int binarySearchIndexResult = Collections.binarySearch(occurrencesArray, wo);
		occurrencesArray.add(extractIndexFromBinarySearchIndexResult(binarySearchIndexResult), wo);
	}

	/**
	 * Adding a new word using binary search O(log(n))
	 * 
	 * @param word
	 */
	private void addNewWord(String word) {
		WordOccurrences newWo = new WordOccurrences(1l, word);
		if (occurrencesArray.isEmpty()) {
			occurrencesArray.add(0, newWo);
		}
		else {
			int binarySearchIndexResult = Collections.binarySearch(occurrencesArray, newWo);
			occurrencesArray.add(extractIndexFromBinarySearchIndexResult(binarySearchIndexResult),newWo);
		}
		wordToOccurrencesMap.put(word, newWo);
	}
	
	/**
	 * Binary search returns a negative value for the target index. 
	 * here we get the real array index position.
	 * 
	 * @param binarySearchIndexResult
	 * @return
	 */
	private int extractIndexFromBinarySearchIndexResult(int binarySearchIndexResult) {
		return (-binarySearchIndexResult) - 1;
	}

	/**
	 * The method gets the client input and returns the relevant words.
	 * 
	 * Need to validate legal input format and throw relevant exceptions when necessary.
	 * I assumed the input ranges can NOT contain overlapping ranges. (e.g. '1-10,2-4' )
	 * In the current implementation it will create duplications. (could be handled \ validated)
	 * 
	 * @param input
	 * @return
	 */
	public List<String> getWordRanking(String input) {
		
		List<String> result = new ArrayList<>();
		String[] rangesInput = input.split(COMMA_SIGN);
		
		for (String rangeInput : rangesInput) {
			if (!rangeInput.contains(MINUS_SIGN)) {
				// Simple single position (e.g. 2,4,50)
				injectWordsToResult(result, Integer.valueOf(rangeInput));
			} else {
				// ranged position (e.g. 1-4)
				String[] fromToInput = rangeInput.split(MINUS_SIGN);
				String from = fromToInput[0];
				String to = fromToInput[1];
				for (Integer i = Integer.valueOf(from); i <= Long.valueOf(to); i++) {
					injectWordsToResult(result, i);
				}
			}
		}
		return result;
	}

	/**
	 * This method prepares the response to the client by adding the ranked words for the relevant range.
	 * 
	 * @param result
	 * @param rangeInput
	 */
	private void injectWordsToResult(List<String> result, Integer rangeInput) {

		if (occurrencesArray.size() >= rangeInput) {
				result.add(occurrencesArray.get(rangeInput-1).getWord());
		} else {
			// no words with this count.
		}
	}

	/**
	 * Persisting to disk the entire SortedArray (Serializable)
	 * 
	 * We could have several different approaches here to save to disk:
	 *   1) Periodically using a cron job
	 *   2) On demand (manually) - using an endpoint. I created one in the WordController (/words/save)
	 *   3) On each call to (/words) - toggled on right now but can be switched off by changing persistOnUpdate=false	 
	 * 
	 * The tradeoff here is between potentially losing data (that was not persisted before a crash) to degraded performance as the disk is a bottleneck here.
	 * With that said, the size of the English dictionary is a few MB so we talk about a reasonably fast process to persist
	 * Especially if we are using a SSD drive. 
	 *  
	 * 
	 * Could be optimized by persisting only deltas \ buckets \ parts of the array
	 * instead of saving the entire sorted array
	 *  
	 * @throws Exception
	 */
	public synchronized void saveMapToDisk() throws Exception {
		if (!occurrencesArray.isEmpty()) {
			try (FileOutputStream fos = new FileOutputStream(STATE_FILENAME);
					ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(this.occurrencesArray);
			}
		}
	}

	/**
	 * A method that loads an existing state of the sorted array (if exists)
	 * The method re-populates the Map 'wordToOccurrencesMap' with the entire occurrencesArray words collection. 
	 * 
	 * @throws Exception
	 */
	private synchronized void loadMapFromDisk() throws Exception {
		File f = new File(STATE_FILENAME);
		if (f.exists()) {
			try (FileInputStream fin = new FileInputStream(STATE_FILENAME)) {
				if (fin.available() == 0) {
					return;
				}
				try (ObjectInputStream ois = new ObjectInputStream(fin)) {
					occurrencesArray = (List<WordOccurrences>) ois.readObject();
					for (WordOccurrences wordOccurrences : occurrencesArray) {
							this.wordToOccurrencesMap.put(wordOccurrences.getWord(), wordOccurrences);
					}
				}
			}
		}
	}

	/**
	 * a method that allows clearing the state and the data structures
	 * 
	 * @throws Exception
	 */
	public void clear() throws Exception {
		File f = new File(STATE_FILENAME);
		if (f.exists()) {
			f.delete();
		}
		init();
	}

}
