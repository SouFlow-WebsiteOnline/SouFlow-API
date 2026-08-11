package com.souflow.models.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.souflow.models.entities.Role;
import com.souflow.models.requests.RoleRequest;
import com.souflow.models.responses.RoleResponse;

@Mapper(componentModel = "spring")
public abstract class RoleMapper {
	
	@Mapping(target = "nameVn", 		ignore = true)
	@Mapping(target = "nameEng", 		ignore = true)
	@Mapping(target = "deleted", 		ignore = true)
	public abstract Role toEntity(RoleRequest request);

	@Mapping(target = "code", source = "code")
	public abstract RoleResponse toResponse(Role role);
}
