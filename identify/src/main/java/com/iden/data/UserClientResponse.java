package com.iden.data;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserClientResponse {
    private UserReq data;
    private String message;
    private boolean isSuccess;

}
