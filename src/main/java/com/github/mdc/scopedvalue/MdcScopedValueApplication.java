package com.github.mdc.scopedvalue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.github.mdc.scopedvalue.controller.Controller;

@SpringBootApplication
@ComponentScan(basePackageClasses = {Controller.class})
public class MdcScopedValueApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MdcScopedValueApplication.class, args);
	}

}