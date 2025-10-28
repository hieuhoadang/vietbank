package com.vietbank.dto;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // không serialize các field null
public class ResponseSuccess<T> {
	
	@JsonIgnore
	private String messageKey;
	
	private String message;
	private T data;
	
	public static <T> ResponseSuccess<T> create (String messageKey, T data, MessageSource messageSource)
	{
		String localizedMessage = messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
		return ResponseSuccess.<T>builder()
				.messageKey(messageKey)
				.message(localizedMessage.trim())
				.data(data)
				.build();
		
	}
}
