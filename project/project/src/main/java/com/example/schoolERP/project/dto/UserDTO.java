package com.example.schoolERP.project.dto;

import com.example.schoolERP.project.model.User;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

	@NotNull
	@NotEmpty
	private String name;
	private String email;
	private String password;
	private String role;
	
}
