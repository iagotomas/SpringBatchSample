package com.tomas.spring.batch.sample.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.tomas.spring.batch.sample.batch.JobStates;
import com.tomas.spring.batch.sample.batch.PersonItemProcessor;
import com.tomas.spring.batch.sample.domain.Person;

@Configuration
public class StepsConfiguration {

	@Autowired
	public DataSource			dataSource;

	@Autowired
	public StepBuilderFactory	stepBuilderFactory;

	@Autowired
	public ItemReader<Person>	reader;

	@Autowired
	public PersonItemProcessor	processor;

	@Autowired
	public ItemWriter<Person>	writer;

	@Autowired
	public JobExecutionDecider	decider;

	@Bean
	public Flow flow(JdbcTemplate template) {
		final FlowBuilder<Flow> builder = new FlowBuilder<Flow>("flow1");
		return builder.start(decider).on(JobStates.RUN.toString()).to(step1()).from(decider)
				.on(JobStates.PAUSED.toString()).end(JobStates.PAUSED.toString()).build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<Person, Person>chunk(1).reader(reader).processor(processor)
				.writer(writer).build();
	}

	@Bean
	public Step step2() {
		return stepBuilderFactory.get("step2").<Person, Person>chunk(1).reader(reader).processor(processor)
				.writer(writer).build();
	}

	@Bean
	public Step step3a() {
		return stepBuilderFactory.get("step3a").<Person, Person>chunk(1).reader(reader).processor(processor)
				.writer(writer).build();
	}

	@Bean
	public Step step3b() {
		return stepBuilderFactory.get("step3b").<Person, Person>chunk(1).reader(reader).processor(processor)
				.writer(writer).build();
	}

	@Bean
	public JobExecutionDecider decider(JdbcTemplate jdbcTemplate) {
		return new JobExecutionDecider() {

			private final Logger log = LoggerFactory.getLogger(JobExecutionDecider.class);
			private final int count = 0;

			@Override
			public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
				Integer count = jdbcTemplate.query("SELECT count(*) FROM people", (ResultSetExtractor<Integer>) rs -> {
					rs.next();
					return rs.getInt(1);
				});
				count++;
				final JobStates state = (count % 2 == 0) ? JobStates.PAUSED : JobStates.RUN;
				log.info("State " + state + " , count:" + (count % 2));
				return new FlowExecutionStatus(state.toString());
			}
		};
	}

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Person> reader() {
		final FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
		reader.setLineMapper(new DefaultLineMapper<Person>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "firstName", "lastName" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Person> writer() {
		final JdbcBatchItemWriter<Person> writer = new JdbcBatchItemWriter<Person>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
		writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
		writer.setDataSource(dataSource);
		return writer;
	}
}
