package com.iden.services;

import com.iden.models.Role;

public interface IRoleService {
    Role getRoleByName(String name);
    //create delete
    void createRole(Role account);
    void deleteRole(Role account);
    //validate
    boolean checkIfNameExists(String name);
}
