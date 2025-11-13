package com.iden.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iden.data.OTP;
import org.springframework.stereotype.Service;


@Service
public interface IOTPService {
    OTP getOTPByAccountId(int accountid) throws JsonProcessingException;
    void deleteOTPByAccountId(int accountid) throws JsonProcessingException;
    OTP getOTPByIdAccount(int accountid) throws JsonProcessingException;
}
