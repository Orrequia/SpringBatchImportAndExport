package com.fo.testBatch.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort;

import com.fo.testBatch.dao.PersonDAO;
import com.fo.testBatch.dto.PersonDTO;
import com.fo.testBatch.model.Person;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public PersonDAO personDAO;
	
	//@Value("#{jobParameters['time']}")
	//private Long time;
	
	private Map<String, Sort.Direction> sorts;
	
	//READERS
	@Bean
	public FlatFileItemReader<PersonDTO> readerFromFile() {
		return new FlatFileItemReaderBuilder<PersonDTO>()
				.name("personItemReader")
				.resource(new ClassPathResource("source.txt"))
				.delimited()
				.names(new String[] {"name", "surname"})
				.fieldSetMapper(new BeanWrapperFieldSetMapper<PersonDTO>() {{
					setTargetType(PersonDTO.class);
				}})
				.build();
	}
	
	@Bean
	@StepScope
	public RepositoryItemReader<Person> readerFromDB(@Value("#{jobParameters['time']}") Long time) {
		this.sorts = new HashMap<String, Sort.Direction>();
		log.info(Long.toString(time));
		List<Object> args = new ArrayList<>();
		args.add(121);
		return new RepositoryItemReaderBuilder<Person>()
				.sorts(this.sorts)
				.methodName("findByIdPerson")
				.arguments(args)
				.name("reader")
				.repository(personDAO)
				.build();
	}
	
	//PROCESSORS
	@Bean
	public PersonItemProcessorImport processorImport() {
		return new PersonItemProcessorImport();
	}
	
	@Bean
	public PersonItemProcessorExport processorExport() {
		return new PersonItemProcessorExport();
	}
	
	//WRITERS
	@Bean
	public RepositoryItemWriter<Person> writerToDB() {
		return new RepositoryItemWriterBuilder<Person>()
				.methodName("save")
				.repository(personDAO)
				.build();
	}
	
	@Bean 
	public FlatFileItemWriter<PersonDTO> writerToFile() {
		BeanWrapperFieldExtractor<PersonDTO> fieldExtractor = new BeanWrapperFieldExtractor<PersonDTO>();
		fieldExtractor.setNames(new String[] {"name", "surname"});
		fieldExtractor.afterPropertiesSet();
		
		return new FlatFileItemWriterBuilder<PersonDTO>()
				.name("personItemWriter")
				.resource(new FileSystemResource("target.txt"))
				.lineAggregator(new DelimitedLineAggregator<PersonDTO>() {{
					setDelimiter(",");
					setFieldExtractor(fieldExtractor);
				}})
				.build();
	}
	
	//JOBS	
	@Bean
	public Job exportJob(JobCompletionNotificationListener listener, Step exportStep) {
		return jobBuilderFactory.get("exportJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(exportStep)
				.end()
				.build();
	}
	
	@Bean
	public Job importJob(JobCompletionNotificationListener listener, Step importStep) {
		return jobBuilderFactory.get("stepImport")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(importStep)
				.end()
				.build();
	}
	
	//STEPS
	@Bean
	public Step exportStep(RepositoryItemReader<Person> reader) {
		return stepBuilderFactory.get("exportStep")
				.<Person, PersonDTO> chunk(10)
				.reader(reader) 
				.processor(processorExport())
				.writer(writerToFile())
				.build();
	}
	
	@Bean
	public Step importStep() {
		return stepBuilderFactory.get("importStep")
				.<PersonDTO, Person> chunk(10)
				.reader(readerFromFile())
				.processor(processorImport())
				.writer(writerToDB())
				.build();
	}
}
