package com.tomas.spring.batch.sample.batch;

public enum JobStates {

	PAUSED("PAUSED"),
	RUN("RUN");
	
	private String state;

	private JobStates(String state) {
		this.state = state;
	}
	@Override
	public String toString() {
		return state;
	}
}
