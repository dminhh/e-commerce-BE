package com.iden.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InitialUser {
    private String user_name;
    private String email;
    private boolean is_active;
    private String account_id;
}
