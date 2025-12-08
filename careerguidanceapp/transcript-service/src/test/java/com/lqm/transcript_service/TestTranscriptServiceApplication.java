package com.lqm.transcript_service;

import org.springframework.boot.SpringApplication;

public class TestTranscriptServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(TranscriptServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
