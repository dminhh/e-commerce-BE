package com.iden.mappers;

import com.iden.data.AccountResponse;
import com.iden.data.RegistryInput;
import com.iden.data.UserReq;
import com.iden.models.Account;
import com.iden.models.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);
    @Mapping(source = "regis.username", target = "username")
    @Mapping(source = "regis.password", target = "password")
//    @Mapping(source = "regis.phone", target = "phone")
    @Mapping(source = "regis.email", target = "email")
    @Mapping(source = "role", target = "role")
    @Mapping(target = "id", ignore = true) // Bỏ qua ánh xạ cho id
    Account registryInputToAccount(RegistryInput regis, Role role);




    @Mapping(source = "account.username", target = "username")
    @Mapping(source = "account.email", target = "email")
    @Mapping(source = "account.role", target = "role")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "account.id", target = "id")
    AccountResponse accountToAccountDTO(Account account, UserReq user);
}
