package com.infor.services;

import com.infor.data.AccountResponse;
import com.infor.data.UserResp;
import com.infor.models.User;
import org.springframework.stereotype.Service;

@Service
public interface IRedisInfoService {
    AccountResponse getAccount(int account_id);
    void pushUserToRedis(User user);
    void updateUserToRedis(User user);
    UserResp getUserByAccount(int accountId);
}
