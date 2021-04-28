package com.lemon.words.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WordsServiceTests {

	@Autowired
	private WordService wordService;

	@Test
	public void post_words_with_no_valid_type() throws Exception {
		this.wordService.postWords("non-existing-type", "some data");
	}

}
