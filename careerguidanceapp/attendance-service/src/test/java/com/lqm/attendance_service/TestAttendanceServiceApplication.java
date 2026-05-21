package com.lqm.attendance_service;

import org.springframework.boot.SpringApplication;

public class TestAttendanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(AttendanceServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
