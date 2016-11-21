package com.tomas.spring.batch.sample.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tomas.spring.batch.sample.batch.JobCompletionNotificationListener;
import com.tomas.spring.batch.sample.domain.Person;

@Configuration
public class TestBatchConfig {

	@Bean
	public JobLauncherTestUtils jobLauncherTestUtils() {
		final JobLauncherTestUtils utils = new JobLauncherTestUtils();
		return utils;
	}

	@Bean
	public JobCompletionNotificationListener listener(JdbcTemplate jdbcTemplate) {
		return new JobCompletionNotificationListener(jdbcTemplate);
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
		final JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		return writer;
	}

	@Autowired
	Tasklet				tasklet;
	@Autowired
	@Resource(name = "writer2a")
	ItemWriter<Person>	writer2a;
	@Autowired
	@Resource(name = "writer2b")
	ItemWriter<Person>	writer2b;
	@Autowired
	@Resource(name = "reader2a")
	ItemReader<Person>	reader2a;
	@Autowired
	@Resource(name = "reader2b")
	ItemReader<Person>	reader2b;

	@Autowired
	@Resource(name = "writer3a")
	ItemWriter<Person>	writer3a;
	@Autowired
	@Resource(name = "writer3b")
	ItemWriter<Person>	writer3b;
	@Autowired
	@Resource(name = "reader3a")
	ItemReader<Person>	reader3a;
	@Autowired
	@Resource(name = "reader3b")
	ItemReader<Person>	reader3b;
	@Autowired
	@Resource(name = "decider2a")
	JobExecutionDecider	decider2a;
	@Autowired
	@Resource(name = "decider2b")
	JobExecutionDecider	decider2b;

	@Autowired
	StepBuilderFactory	stepBuilderFactory;
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").tasklet(tasklet).build();
	}



	private ChunkListener chunkListener() {
		return new ChunkListenerSupport() {

			@Override
			public void afterChunk(ChunkContext context) {
				final StepContext stepContext = context.getStepContext();
				final StepExecution stepExecution = stepContext.getStepExecution();
				stepExecution.setExitStatus(new ExitStatus("PAUSED", "Job Paused due to time box"));
				stepExecution.setTerminateOnly();
			}
		};
	}
	@Bean
	@Qualifier("flow2a")
	public Flow flow2a() {
		final Step step2a = stepBuilderFactory.get("step2a").<Person, Person>chunk(1).reader(reader2a).writer(writer2a)
				.listener(chunkListener())
				.build();
		final Step step3a = stepBuilderFactory.get("step3a").<Person, Person>chunk(1).reader(reader3a).writer(writer3a)
				.build();
		final FlowBuilder<Flow> flowBuilder = new FlowBuilder<Flow>("flow2a");
		flowBuilder.start(decider2a).on("RUN").to(step2a).from(decider2a).on("PAUSED").end("PAUSED");
		return flowBuilder.next(step3a).build();
	}

	@Bean
	@Qualifier("flow2b")
	public Flow flow2b() {
		final Step step2b = stepBuilderFactory.get("step2b").<Person, Person>chunk(1).reader(reader2b).writer(writer2b)
				.build();
		final Step step3b = stepBuilderFactory.get("step3b").<Person, Person>chunk(1).reader(reader3b).writer(writer3b)
				.build();
		final FlowBuilder<Flow> flowBuilder = new FlowBuilder<Flow>("flow2b");
		flowBuilder.start(decider2b).on("RUN").to(step2b).next(step3b).from(decider2b).on("PAUSED").to(step3b);
		return flowBuilder.build();
	}

}
