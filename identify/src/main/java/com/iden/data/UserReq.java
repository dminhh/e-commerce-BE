package com.iden.data;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserReq {
    private int id;
    private String full_name;
    private String phone;
    private boolean active;
    private String avt;
    private int account_id;
    private String created_at ;
    private String updated_at;
}
