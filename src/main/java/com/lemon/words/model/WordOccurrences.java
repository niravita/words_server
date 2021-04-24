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

	public void setWord(String word) {
		this.word = word;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WordOccurrences other = (WordOccurrences) obj;
		if (count != other.count)
			return false;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
	
	
}
