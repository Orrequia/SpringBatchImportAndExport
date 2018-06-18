package com.fo.testBatch.job;

import org.springframework.batch.item.ItemProcessor;

import com.fo.testBatch.dto.PersonDTO;
import com.fo.testBatch.model.Person;

public class PersonItemProcessorExport implements ItemProcessor<Person, PersonDTO> {
	
	public PersonDTO process(Person person) throws Exception {
		PersonDTO personDTO = new PersonDTO();
		personDTO.setName(person.getName().toLowerCase());
		personDTO.setSurname(person.getSurname().toLowerCase());
		return personDTO;
	}
}