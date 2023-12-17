package com.farida.PapeterieManagementSystem.dao;


import com.farida.PapeterieManagementSystem.model.Role;

public interface RoleDao {

	public Role findRoleByName(String theRoleName);
	
}
