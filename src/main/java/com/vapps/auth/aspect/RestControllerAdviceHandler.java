package com.vapps.auth.aspect;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vapps.auth.dto.AppErrorResponse;
import com.vapps.auth.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestControllerAdviceHandler {
    
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception ex, HttpServletRequest request) {
		ex.printStackTrace();

		int status = 500;
		String message = "Internal Server Error";

		// In Webclient global error handling the AppException is sent with the Exceptions.ReactiveException
		// So extracting it here.
		if(ex.getCause() instanceof AppException) {
			AppException e = (AppException) ex.getCause();
			status = e.getStatus();
			message = e.getMessage();
		}
		AppErrorResponse errorResponse = new AppErrorResponse(status, 
												message, 
												LocalDateTime.now(),
												request.getServletPath());
		return ResponseEntity.internalServerError().body(errorResponse);
	}
	@ExceptionHandler(AppException.class)
	public ResponseEntity<?> handleException(AppException ex, HttpServletRequest request) {
		
		AppErrorResponse errorResponse = new AppErrorResponse(ex.getStatus(), 
												ex.getMessage(), 
												LocalDateTime.now(),
												request.getServletPath());
		return new ResponseEntity<AppErrorResponse>(errorResponse, 
				HttpStatusCode.valueOf(ex.getStatus()));
	}
	
}
