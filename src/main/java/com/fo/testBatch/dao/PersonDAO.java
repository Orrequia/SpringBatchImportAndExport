package com.fo.testBatch.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.fo.testBatch.model.Person;

public interface PersonDAO extends PagingAndSortingRepository<Person, Integer> {

	Page<Person> findByIdPerson(Integer idPerson, Pageable pageable);
}
