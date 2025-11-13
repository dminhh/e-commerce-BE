package com.iden.services;

import com.iden.models.Account;
import org.springframework.stereotype.Service;

@Service
public interface IRedisAccountService {
    void saveAccount(Account account);
    void pushAccountToRedis(Account account);
    Account getAccountFromRedis(int accountId);
    void pushTokenToRedis(String token, int accountId);
    String getTokenFromRedis(int accountId);
    Account updateAccountToRedis(Account account );
    void deleteToken(String accountId);
}
