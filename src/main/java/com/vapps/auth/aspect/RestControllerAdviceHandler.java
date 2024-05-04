package com.vapps.auth.aspect;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.vapps.auth.dto.AppErrorResponse;
import com.vapps.auth.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class RestControllerAdviceHandler {
    @ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleException(IllegalArgumentException ex, HttpServletRequest request) {
		
		AppErrorResponse errorResponse = new AppErrorResponse(400, 
												ex.getMessage(), 
												LocalDateTime.now(),
												request.getServletPath());
		return ResponseEntity.badRequest().body(errorResponse);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleException(MethodArgumentNotValidException ex, HttpServletRequest request) {
		
		StringBuffer errorMessage = new StringBuffer();
		ex.getAllErrors().forEach(error -> {
			errorMessage.append(error.getDefaultMessage()).append(", ");
		});
		
		//To delete the extra comma at last
		errorMessage.delete(errorMessage.length() - 2, errorMessage.length());
		
		AppErrorResponse errorResponse = new AppErrorResponse(400, 
												errorMessage.toString(), 
												LocalDateTime.now(),
												request.getServletPath());
		return ResponseEntity.badRequest().body(errorResponse);
	}
	
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
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<?> handleException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
		
		AppErrorResponse errorResponse = new AppErrorResponse(HttpStatus.NOT_FOUND.value(), 
												ex.getMessage(), 
												LocalDateTime.now(),
												request.getServletPath());
		return new ResponseEntity<AppErrorResponse>(errorResponse, HttpStatus.NOT_FOUND);
	}
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> handleException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
		
		AppErrorResponse errorResponse = new AppErrorResponse(HttpStatus.BAD_REQUEST.value(), 
												"Invalid Input", 
												LocalDateTime.now(),
												request.getServletPath());
		return new ResponseEntity<AppErrorResponse>(errorResponse, HttpStatus.BAD_REQUEST);
	}
}
