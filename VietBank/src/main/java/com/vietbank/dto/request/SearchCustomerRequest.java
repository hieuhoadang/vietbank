package com.vietbank.dto.request;


import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchCustomerRequest {
	private String name;
    private String phone;
    private String idCard;
    @Min(value = 0, message = "Page must be non-negative")
    private int page;
    @Min(value = 1, message = "Size must be at least 1")
    private int size;
}
