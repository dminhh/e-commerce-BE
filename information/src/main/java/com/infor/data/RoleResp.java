package com.infor.data;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Component
public class RoleResp {
    int id;
    String name;
    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updated_at = LocalDateTime.now();
}
