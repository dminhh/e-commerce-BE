package com.infor.data;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ReqUser {
    private String user_name;
    private String email;
    private String full_name;
    private String phone;
    private String avt;
    private boolean is_active;
    private String account_id;
}
