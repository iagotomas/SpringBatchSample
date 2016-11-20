package com.tomas.spring.batch.sample.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tomas.spring.batch.sample.batch.JobCompletionNotificationListener;
import com.tomas.spring.batch.sample.batch.JobStates;
import com.tomas.spring.batch.sample.batch.PersonItemProcessor;
import com.tomas.spring.batch.sample.domain.Person;

@Configuration
@EnableBatchProcessing
@Import(DatabaseConfiguration.class)
public class BatchConfiguration extends DefaultBatchConfigurer {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Autowired
	public ItemReader<Person> reader;

	@Autowired
	PersonItemProcessor processor;

	@Autowired
	ItemWriter<Person> writer;

	@Autowired
	JobExecutionDecider decider;
	// end::readerwriterprocessor[]

	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}
	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, JdbcTemplate template) {
		final Flow flow1 = flow1(template);
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener)
				.start(flow1).on("PAUSED").end("COMPLETED WITH PAUSE").from(flow1).on("COMPLETED")
				.end("COMPLETED WITHOUT PAUSE").end().build();
	}

	@Bean
	public Flow flow1(JdbcTemplate template) {
		final FlowBuilder<Flow> builder = new FlowBuilder<Flow>("flow1");
		return builder.start(decider).on(JobStates.RUN.toString()).to(step1()).from(decider)
				.on(JobStates.PAUSED.toString()).end(JobStates.PAUSED.toString()).build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<Person, Person>chunk(1).reader(reader).processor(processor)
				.writer(writer).build();
	}
	// end::jobstep[]
}