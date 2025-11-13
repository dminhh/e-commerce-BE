package com.iden.configs;

import com.iden.data.InitialUser;
import com.iden.models.Account;
import com.iden.models.ENUM.ERole;
import com.iden.repositories.IAccountRepository;
import com.iden.repositories.IRoleRepository;
import com.iden.services.IUserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.iden.repositories.IAccountRepository.Specs.byUsername;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;
    IRoleRepository roleRepository;
    IUserClient userClient;

    @Bean
//    @ConditionalOnProperty(
//            prefix = "spring",
//            value = "datasource.driverClassName",
//            havingValue =
//                    "com.mysql.cj.jdbc.Driver") // dua theo properties là gi ( cau hinh nhu tren thì Bean tren chi init
//    // khi  no la mysql)
    ApplicationRunner applicationRunner(IAccountRepository accountRepository) {
        return args -> {
            if (accountRepository.findOne(byUsername("admin")).isEmpty()) {
                Account initAccont = Account.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .email("dinhnguyen250802@gmail.com")
                        .role(roleRepository.findByName(ERole.ADMIN.name()).orElse(null))
                        .build();
                Account account = accountRepository.save(initAccont);
                InitialUser initialUser = InitialUser.builder()
                        .is_active(true)
                        .user_name("admin")
                        .email("dinhnguyen250802@gmail.com")
                        .account_id(account.getId()+"")
                        .build();
                userClient.initialUser(initialUser);

                log.warn("admin user has been create with default, please change it!");
            }
        };
    }
}
