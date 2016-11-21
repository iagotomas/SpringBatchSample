package com.tomas.spring.batch.sample;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.test.context.junit4.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
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

	private static final Logger LOG = LoggerFactory.getLogger(BatchApplicationTest.class);
	@Autowired
	JobLauncherTestUtils	jobLauncher;

	@MockBean(name = "reader")
	ItemReader<Person>		reader;

	@MockBean
	PersonItemProcessor		processor;

	@MockBean
	Tasklet					tasklet;
	@MockBean(name = "writer2a")
	ItemWriter<Person>		writer2a;
	@MockBean(name = "writer2b")
	ItemWriter<Person>		writer2b;
	@MockBean(name = "reader2a")
	ItemReader<Person>		reader2a;
	@MockBean(name = "reader2b")
	ItemReader<Person>		reader2b;
	@MockBean(name = "writer3a")
	ItemWriter<Person>		writer3a;
	@MockBean(name = "writer3b")
	ItemWriter<Person>		writer3b;
	@MockBean(name = "reader3a")
	ItemReader<Person>		reader3a;
	@MockBean(name = "reader3b")
	ItemReader<Person>		reader3b;
	@MockBean(name = "decider2a")
	JobExecutionDecider		decider2a;
	@MockBean(name = "decider2b")
	JobExecutionDecider		decider2b;
	
	@MockBean(name = "decider")
	JobExecutionDecider		decider;
	
	@MockBean
	Predicate<ChunkContext> predicate;

	private Person person(String firstName, String lastName) {
		return new Person(firstName, lastName);
	}


	@Before
	public void setUp() throws UnexpectedInputException, ParseException, NonTransientResourceException, Exception {
		given(reader.read()).willReturn(person("Iago", "Tomas"), person("Iago", "Tomas"), null);
		given(decider.decide(any(), any())).willReturn(new FlowExecutionStatus(JobStates.RUN.toString()));
		given(processor.process(any())).willReturn(person("Iago", "Tomas"), person("Iago2", "Tomas"));
		// given(decider2a.decide(any(), any())).willAnswer(new Answer<FlowExecutionStatus>() {
		// @Override
		// public FlowExecutionStatus answer(InvocationOnMock invocation) throws Throwable {
		// // TODO Auto-generated method stub
		// return null;
		// }
		// });

		given(decider2a.decide(any(), any())).willReturn(new FlowExecutionStatus(JobStates.RUN.toString()));
		given(decider2b.decide(any(), any())).willReturn(new FlowExecutionStatus(JobStates.RUN.toString()));
		given(reader2a.read()).willReturn(person("Iago", "Tomas"), person("Iago", "Tomas"), null);
		given(reader2b.read()).willReturn(person("Iago", "Tomas"), person("Iago", "Tomas"), null);
		given(reader3a.read()).willReturn(person("Iago", "Tomas"), person("Iago", "Tomas"), null);
		given(reader3b.read()).willReturn(person("Iago", "Tomas"), person("Iago", "Tomas"), null);
	}

	
	@Autowired
	JobOperator	jobOperator;
	
	@Autowired 
	JobRepository jobRepository;

	@Test
	public void testJob() throws Exception {
		given(predicate.test(any())).willReturn(false);
		final JobExecution jobExecution = jobLauncher.launchJob();
		jobRepository.update(jobExecution);
		if(!jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			LOG.info("Relaunching job instance id {} ...",jobExecution.getId());
			jobOperator.restart(jobExecution.getId());
			fail("Shouldn't had relaunched");
		}
	}
	
	@Test
	public void testRelaunchJob() throws Exception {

		given(predicate.test(any())).willReturn(true);
//		final JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
//		factory.setDataSource(dataSource);
//		factory.afterPropertiesSet();

		
		final JobExecution jobExecution = jobLauncher.launchJob();
		jobRepository.update(jobExecution);
		if(!jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			LOG.info("Relaunching job instance id {} ...",jobExecution.getId());
			jobOperator.restart(jobExecution.getId());
		}
		else {
			fail("Should had relaunched");
		}
	}
}
