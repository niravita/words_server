package com.lemon.words.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WordsServiceTests {

	@Autowired
	private WordService wordService;

	@Test
	public void postWords_illegal_type_exception() throws Exception {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			this.wordService.postWords("non-existing-type", "some data");
		});
	}

}
