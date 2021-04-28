package com.lemon.words.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WordsServiceTests {

	@Autowired
	private WordService wordService;

	/**
	 * Deleting the state file even though its a little risky. Could of moved it \
	 * rename it instead (to avoid data loss)
	 * 
	 * And cleaning the data structures using 'clear()' method
	 * @throws Exception
	 * 
	 */
	@BeforeEach
	public void cleanup() throws Exception {
		wordService.clear();
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

	@Test
	public void postWords_string_file_sanity_case() throws Exception {
		this.wordService.postWords("file", "testfile.txt");
		List<String> wordRankingResultArray = this.wordService.getWordRanking("1,2");
		Assertions.assertEquals(wordRankingResultArray.size(), 2);
		Assertions.assertEquals(wordRankingResultArray.get(0), "data");
	}

}
