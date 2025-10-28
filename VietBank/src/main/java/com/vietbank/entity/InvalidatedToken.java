package com.vietbank.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "invalidated_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidatedToken {
	
	@Id
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
	private String id;
	@Column(name = "expiry_time", nullable = false)
	private LocalDateTime expriyTime;
}
