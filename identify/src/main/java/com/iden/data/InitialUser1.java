package com.iden.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitialUser1 {
    private String user_name;
    private String email;
    private String full_name;
    private String phone;
    private boolean is_active;
    private String account_id;
}
