package com.tomas.spring.batch.sample.config;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tomas.spring.batch.sample.batch.JobCompletionNotificationListener;

@Configuration
@EnableBatchProcessing
@Import(DatabaseConfiguration.class)
public class BatchConfiguration extends DefaultBatchConfigurer {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;


	@Autowired
	public DataSource dataSource;


	// end::readerwriterprocessor[]

	@Bean
	public TaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor("springBatch-");
	}

	@Autowired
	@Resource(name = "decider")
	JobExecutionDecider	decider1;

	@Autowired
	Step				step1;

	@Autowired
	@Resource(name = "flow2b")
	Flow				flow2b;

	@Autowired
	@Resource(name = "flow2a")
	Flow				flow2a;

	@Override
	public void setDataSource(DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	@Bean
	@Qualifier("splitFlow")
	public Flow splitFlow() {

		final FlowBuilder<Flow> splitBuilder = new FlowBuilder<>("splitFlow");
		splitBuilder.split(taskExecutor()).add(flow2a, flow2b);
		return splitBuilder.build();
	}

	@Bean
	@Qualifier("completeFlow")
	public Flow completeFlow() {
		final FlowBuilder<Flow> builder = new FlowBuilder<>("completeFlow");
		builder
		.start(decider1)
		.on("RUN")
			.to(step1)
			.next(splitFlow())
		.from(decider1)
		.on("PAUSED")
			.to(splitFlow());
		return builder.build();
	}
	// tag::jobstep[]
	@Bean
	public Job importUserJob(JobCompletionNotificationListener listener, JdbcTemplate template) {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).listener(listener)
				.start(completeFlow()).end().build();
	}
	// end::jobstep[]
}