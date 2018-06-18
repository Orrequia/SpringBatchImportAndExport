package com.fo.testBatch.job;

import org.springframework.batch.item.ItemProcessor;

import com.fo.testBatch.dto.PersonDTO;
import com.fo.testBatch.model.Person;

public class PersonItemProcessorImport implements ItemProcessor<PersonDTO, Person> {
		
	public Person process(PersonDTO personDTO) throws Exception {
		Person person = new Person();
		person.setName(personDTO.getName().toUpperCase());
		person.setSurname(personDTO.getSurname().toUpperCase());
		return person;
	}
}
