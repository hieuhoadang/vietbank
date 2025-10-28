package com.vietbank.config;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
// Ẩn trance
public class CustomErrorAttributes extends DefaultErrorAttributes {
	
	@Override
	public Map<String , Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options)
	{
		ErrorAttributeOptions newOptions = options.excluding(ErrorAttributeOptions.Include.STACK_TRACE);
		Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, newOptions);
		
		errorAttributes.remove("trance");
		return errorAttributes;
		
	}
}
