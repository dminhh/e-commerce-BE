package com.infor.services.impl;

import com.common.utils.ConvertJson;

import com.infor.data.AccountResponse;
import com.infor.data.UserResp;
import com.infor.models.User;
import com.infor.services.IRedisInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class RedisInfoService implements IRedisInfoService {
    @Autowired
    private com.common.services.impl.RedisService redisService;
    @Autowired
    private ConvertJson convertJson;
    @Override
    public AccountResponse getAccount(int account_id) {
        try {
            String key = "account_id:" + account_id;
            String value = redisService.get(key);
            if(value == null) {
                log.info("Account {} not found", account_id);
                return null;
            } else {
                log.info("Account {} found", account_id);
                return convertJson.convertFromJson(value, AccountResponse.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    @Override
    public void pushUserToRedis(User user) {
        try {
            String key = "account_id:" + user.getAccount_id()+":user";
            String value = convertJson.convertToJson(user);
            redisService.save(key, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void updateUserToRedis(User user) {
        try {
            String key = "account_id:" + user.getAccount_id()+":user";
            redisService.delete(key);
            String value = convertJson.convertToJson(user);
            redisService.save(key, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public UserResp getUserByAccount(int accountId) {
        try {
            String key = "account_id:" + accountId + ":user";
            String value = redisService.get(key);
            if(value == null) {
                log.info("User with account {} not found", accountId);
                return null;
            } else {
                log.info("User with account {} found", accountId);
                return convertJson.convertFromJson(value, UserResp.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

}
