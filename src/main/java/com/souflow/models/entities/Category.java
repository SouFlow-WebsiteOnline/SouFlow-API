package com.souflow.models.entities;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="Categories")
public class Category {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;

	@Column(name = "id")
	private String code;

	@Column(name = "name_vn")
	private String nameVn;

	@Column(name = "name_eng")
	private String nameEng;

	@Column(name = "description_vn")
	private String descriptionVn;

	@Column(name = "description_eng")
	private String descriptionEng;

	@Column(name = "del_if")
	private Boolean deleted;
		
	@OneToMany(mappedBy="category")
	private List<Product> products = new ArrayList<>();
}
