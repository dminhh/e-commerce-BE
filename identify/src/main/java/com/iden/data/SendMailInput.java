package com.iden.data;

import com.iden.validators.ICharEmail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendMailInput {
    @ICharEmail
    private String email;
}
