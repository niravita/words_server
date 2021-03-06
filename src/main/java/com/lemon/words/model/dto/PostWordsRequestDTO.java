package com.lemon.words.model.dto;

/**
 * The dto for the POST /words request
 */
public class PostWordsRequestDTO {

	private String type;
	private String data;

	public PostWordsRequestDTO() {
		super();
	}

	public PostWordsRequestDTO(String type, String data) {
		super();
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
