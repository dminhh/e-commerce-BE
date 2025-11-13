package com.iden.data;

import com.iden.validators.ICharPassword;
import com.iden.validators.ICharUsername;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangeInput {
    @ICharUsername
    private String username;
    @ICharPassword
    private String password;
}
