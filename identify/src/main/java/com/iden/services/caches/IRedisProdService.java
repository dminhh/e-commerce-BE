package com.iden.services.caches;


import com.iden.data.AccountResponse;
import com.iden.data.UserReq;
import org.springframework.stereotype.Service;

@Service
public interface IRedisProdService {
    AccountResponse getAccount(int accountId);
    UserReq getUserByAccount(int accountId);
}
