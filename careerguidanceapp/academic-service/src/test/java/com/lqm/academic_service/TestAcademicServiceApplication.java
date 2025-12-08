package com.lqm.academic_service;

import org.springframework.boot.SpringApplication;

public class TestAcademicServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AcademicServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
