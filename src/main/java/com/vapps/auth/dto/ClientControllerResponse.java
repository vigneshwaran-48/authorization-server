package com.vapps.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientControllerResponse {

	private int status;
	private String message;
	private LocalDateTime timestamp;
	private String path;
	private List<ClientDTO> data;

}
