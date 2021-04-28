package com.lemon.words.control;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.words.model.dto.PostWordsRequestDTO;
import com.lemon.words.service.WordService;

@RunWith(SpringRunner.class)
@WebMvcTest(WordController.class)
/**
 * Unit tests for controllers, Usually to check HTTP status codes and spring's
 * MVC parameter\payload validations.
 * 
 * I didn't test for 405 errors (Method not allowed)
 * 
 */
class WordsControllerTests {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private WordService service;

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	/*
	 * We don't check the types of input here (string,url,file) as these tests are
	 * only controller layer tests. Service unit tests will check types.
	 */
	public void post_words_on_legit_input_return_status_200() throws Exception {

		mvc.perform(post("/words").content(toJson(new PostWordsRequestDTO("string", "one,two")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void post_words_on_nonlegit_input_return_status_bad_request_400() throws Exception {

		mvc.perform(post("/words").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void get_word_ranking_without_parameter_return_status_bad_request_400() throws Exception {

		mvc.perform(get("/word_ranking").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
	}

	@Test
	public void get_word_ranking_with_parameter_return_status_200() throws Exception {

		mvc.perform(get("/word_ranking").param("range", "1,2,3")
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void post_save_return_status_200() throws Exception {

		mvc.perform(post("/save").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	private String toJson(Object pojo) throws JsonProcessingException {
		return mapper.writeValueAsString(pojo);
	}
}
