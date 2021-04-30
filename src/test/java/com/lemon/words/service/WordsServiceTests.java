package com.lemon.words.service;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.lemon.WordsApplication;
import com.lemon.words.model.WordOccurrences;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WordsServiceTests {

	@Autowired
	private WordService wordService;

	/**
	 * Deleting the state file even though its a little risky. Could of moved it \
	 * rename it instead (to avoid data loss)
	 * 
	 * And cleaning the data structures using 'clear()' method
	 * 
	 * @throws Exception
	 * 
	 */
	@BeforeEach
	@AfterAll
	public void cleanup() throws Exception {
		wordService.clear();
	}

	/**
	 * Check comparing words by count - expecting aab[33] before aaa[5]
	 */
	@Test	
	public void compare_two_word_occurrences_elements_by_count() throws Exception {
		WordOccurrences newWo = new WordOccurrences(5l, "aaa");
		WordOccurrences newWo2 = new WordOccurrences(33l, "aab");
		Assertions.assertTrue(newWo.compareTo(newWo2) > 0);

	}
	/**
	 * Check comparing words lexicographically - expecting baa[1] before nnt[1]
	 */
	@Test
	public void compare_two_word_occurrences_elements_by_alphabetic() throws Exception {
		WordOccurrences newWo = new WordOccurrences(1l, "nnt");
		WordOccurrences newWo2 = new WordOccurrences(1l, "baa");
		Assertions.assertTrue(newWo.compareTo(newWo2) > 0);
	}


	@Test
	public void postWords_illegal_type_exception() throws Exception {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			this.wordService.postWords("non-existing-type", "some data");
		});
	}

	@Test
	public void postWords_string_type_sanity_case() throws Exception {
		this.wordService.postWords("string", "some data data");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("1,2");
		Assertions.assertEquals(wordRankingResultArray.size(), 2);
		Assertions.assertEquals(wordRankingResultArray.get(0), "data");
	}

	/**
	 * Using a testfile.txt example here with the following text:
	 *  2-t
	 *  45-d
     *  20-point 
	 */
	@Test
	public void postWords_string_file_sanity_case() throws Exception {
		this.wordService.postWords("file", "testfile.txt");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("2,3");
		Assertions.assertEquals(wordRankingResultArray.size(), 2);
		Assertions.assertEquals(wordRankingResultArray.get(0), "20");
		Assertions.assertEquals(wordRankingResultArray.get(1), "45");
	}

	@Test
	public void postWords_string_url_sanity_case() throws Exception {
		this.wordService.postWords("url", "https://github.com/dwyl/english-words/raw/master/words.txt");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("1");
		Assertions.assertEquals(wordRankingResultArray.size(), 1);
		Assertions.assertEquals(wordRankingResultArray.get(0), "self");
	}
	
	@Test
	public void postWords_string_type__range_sanity_case() throws Exception {
		this.wordService.postWords("string", "some data data");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("1-2");
		Assertions.assertEquals(wordRankingResultArray.size(), 2);
		Assertions.assertEquals(wordRankingResultArray.get(0), "data");
	}
	
	@Test
	public void reusing_existsing_state_on_startup() throws Exception {
		this.wordService.postWords("string", "some data data");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("1-2");
		this.wordService.init();
		wordRankingResultArray = this.wordService.getWordRanking("1");
		Assertions.assertEquals(wordRankingResultArray.get(0), "data");
	}
	
	@Test
	public void applicationContextTest() {
	    WordsApplication.main(new String[] {});
	}
}
