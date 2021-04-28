package com.lemon.words.control;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.words.model.dto.PostWordsRequestDTO;
import com.lemon.words.service.WordService;

@RunWith(SpringRunner.class)
@WebMvcTest(WordController.class)
class WordsApplicationTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private WordService service;

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void post_words_on_legit_input_return_status_200() throws Exception {

		mvc.perform(post("/words")
				.content(mapper.writeValueAsString(generatePostRequest("string", "hi my name is hi my name")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}
	
	@Test
	public void post_words_on_nonlegit_input_return_status_bad_request_400() throws Exception {
		
		mvc.perform(post("/words")				
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	private PostWordsRequestDTO generatePostRequest(String type, String data) {
		return new PostWordsRequestDTO(type, data);
	}
}
