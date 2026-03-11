package com.lqm.score_service;

import org.springframework.boot.SpringApplication;

public class TestTranscriptServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(ScoreServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
