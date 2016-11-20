package com.tomas.spring.batckh.sample.config;

import javax.sql.DataSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
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

}
