package com.lemon.words.model;

import java.io.Serializable;

/**
 * A POJO that represents a word and its occurrences.
 * Implement Comparable by count and alphabetically (word)
 */
public class WordOccurrences implements Serializable, Comparable<WordOccurrences> {

	private static final long serialVersionUID = 2912209029218275918L;

	private long count; 
	private String word; 

	public WordOccurrences(long count, String word) {
		super();
		this.count = count;
		this.word = word;
	}

	public String getWord() {
		return word;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	@Override
	public int compareTo(WordOccurrences o) {
		if (this.count < o.getCount()) {
			return 1;
		}
		if (this.count > o.getCount()) {
			return -1;
		} else {
			return this.word.compareTo(o.getWord());
		}
	}
}
