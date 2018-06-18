package com.fo.testBatch.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.fo.testBatch.model.Person;

public interface PersonDAO extends PagingAndSortingRepository<Person, Integer> {

}
