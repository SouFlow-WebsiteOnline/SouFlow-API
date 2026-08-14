package com.souflow.models.entities;

import com.souflow.models.enums.RoleCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="roles")
public class Role {
	
	@Id	
	@Enumerated(EnumType.STRING)
	private RoleCode code;

	@Column(name = "name_vn")
	private String nameVn;

	@Column(name = "name_eng")
	private String nameEng;
	
	@Column(name = "del_if")
	private Boolean deleted;
}
