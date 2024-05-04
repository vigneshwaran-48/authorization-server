package com.vapps.auth.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserControllerResponse {

	private Map<String, String> message;
	private UserDTO user;

}
