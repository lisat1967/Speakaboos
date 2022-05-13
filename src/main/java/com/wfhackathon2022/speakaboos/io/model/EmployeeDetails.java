package com.wfhackathon2022.speakaboos.io.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class EmployeeDetails {

	@Getter @Setter
	@JsonProperty("employeeId")
	private Integer employeeId;
	
	@Getter @Setter
	@JsonProperty("legalFirstName")
	private String legalFirstName;
	
	@Getter @Setter
	@JsonProperty("legalLastName")
	private String legalLastName;
	
	@Getter @Setter
	@JsonProperty("preferredName")
	private String preferredName;
	
}