package com.alaya.coursesystem.alaya_course_selection;

import org.springframework.boot.SpringApplication;

public class TestAlayaCourseSelectionApplication {

	public static void main(String[] args) {
		SpringApplication.from(AlayaCourseSelectionApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
