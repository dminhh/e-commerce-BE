package com.iden.data;

import com.common.utils.otp.GenerateOTPCode;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int accountid;
    @Builder.Default
    private String otp = GenerateOTPCode.getOTPCode();
}
