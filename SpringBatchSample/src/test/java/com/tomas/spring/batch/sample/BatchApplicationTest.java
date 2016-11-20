package com.tomas.spring.batch.sample;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

/*
@formatter:off
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit4.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@formatter:on
*/

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tomas.spring.batch.sample.batch.JobStates;
import com.tomas.spring.batch.sample.batch.PersonItemProcessor;
import com.tomas.spring.batch.sample.config.BatchConfiguration;
import com.tomas.spring.batch.sample.config.TestBatchConfig;
import com.tomas.spring.batch.sample.domain.Person;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { BatchConfiguration.class, TestBatchConfig.class })
public class BatchApplicationTest {

	@Autowired
	JobLauncherTestUtils	jobLauncher;

	@MockBean
	ItemReader<Person>		reader;

	@MockBean
	PersonItemProcessor		processor;

	// @MockBean
	// ItemWriter<Person> writer;

	@MockBean
	JobExecutionDecider		decider;


	private Person person(String firstName, String lastName) {
		return new Person(firstName, lastName);
	}

	@Before
	public void setUp() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		given(reader.read()).willReturn(person("Iago", "Tomas"), null);
		given(decider.decide(any(), any())).willReturn(new FlowExecutionStatus(JobStates.RUN.toString()));
		given(processor.process(any())).willReturn(person("Iago", "Tomas"));
	}

	@Test
	public void testJob() throws Exception {
		jobLauncher.launchJob();
	}
}
