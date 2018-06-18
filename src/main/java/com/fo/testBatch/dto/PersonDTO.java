package com.fo.testBatch.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class PersonDTO implements Serializable {

	private static final long serialVersionUID = 7979879789791L;
	
	private String name;
	private String surname;
}
