package com.lemon.words.control;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.lemon.words.model.dto.PostWordsRequestDTO;
import com.lemon.words.service.WordService;

/**
 * The controller that exposes the word entrypoints
 * 
 * Security(!): this is a public controller, we could of used some sort of
 * authentication\authorization\ddos prevention mechanisms
 *
 *
 * Exception handling(!): Exceptions are simply thrown where a validation \
 * error occurs. We could of use an exception hierarchy with error
 * codes\messages so our clients will receive meaningful error messages
 */
@RestController
@RequestMapping("/")
public class WordController {

	@Autowired
	private WordService wordService;

	@PostMapping(value = "/words")
	@ResponseStatus(code = HttpStatus.OK)
	public void postWords(@RequestBody PostWordsRequestDTO requestDTO) throws Exception {
		this.wordService.postWords(requestDTO.getType(), requestDTO.getData());
	}

	@GetMapping(value = "/word_ranking")
	@ResponseStatus(code = HttpStatus.OK)
	@ResponseBody
	public List<String> getWordRanking(@RequestParam(name = "range") String rangeInput) {
		return this.wordService.getWordRanking(rangeInput);
	}

	/**
	 * This entry point allows saving to disk manually.
	 * @throws Exception
	 */
	@PostMapping(value = "/save")
	@ResponseStatus(code = HttpStatus.OK)
	public void save() throws Exception {
		this.wordService.saveMapToDisk();
	}
}
