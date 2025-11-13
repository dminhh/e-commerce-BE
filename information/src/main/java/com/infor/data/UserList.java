package com.infor.data;

import com.infor.models.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserList {
    private long total;
    private long totalPage;
    private List<User> users;
}
