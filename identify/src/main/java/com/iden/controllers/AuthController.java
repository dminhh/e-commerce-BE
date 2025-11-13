package com.iden.controllers;

import com.common.DTO.ResponseObject;
import com.common.controllers.ControllerUtil;
import com.common.services.IEmailService;
import com.common.services.IRedisService;
import com.iden.custom.CustomClientException;
import com.iden.data.*;
import com.iden.mappers.AccountMapper;
import com.iden.models.Account;
import com.iden.models.ENUM.ERole;
import com.iden.models.Role;
import com.iden.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final Map<Integer, Account> accounts = new HashMap<>();
    private final String ecommerceEmail = "montoan01102002@gmail.com";
    @Autowired
    private IJwtTokenService jwtTokenService;
    @Autowired
    private IAccountService accountService;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private IEmailService emailService;
    @Autowired
    private IOTPService otpService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IRedisService redisService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IRedisAccountService redisAccountService;

    @Autowired
    private IUserClient userClient;
    /**
     * Chuc nang dang nhap
     * Khi tai khoan cua nguoi dung da xac thuc moi co the dang nhap he thong
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginReq inputDTO) {
        try {
            log.info("login da vao");
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            ResponseLoginDTO respDTO = ResponseLoginDTO.builder().build();
            Optional<Account> account = accountService.getAccountByUsername(inputDTO.getUsername());
            //check pass
            if (account.isPresent() && passwordEncoder.matches(inputDTO.getPassword(), account.get().getPassword())) {
                String JWT = jwtTokenService.generateToken(account.get());
                redisAccountService.pushAccountToRedis(account.get());
                log.info("Got JWT : {}", JWT);


                // Gọi API ngoài thông qua Feign client
                ResponseEntity<UserClientResponse> externalApiResponse =null;
                try {
                    externalApiResponse = userClient.getUserInfo("Bearer " + JWT);
                }catch (CustomClientException e){

                }


                log.info("code");
                UserReq userReq =null;
                if(externalApiResponse==null) {
//                    return ControllerUtil.error("Lỗi khi gọi API ngoài");
                }
//                if (externalApiResponse.getStatusCode().is2xxSuccessful())
                else{
                    log.info("success");
                    UserClientResponse userInfo = (UserClientResponse)externalApiResponse.getBody();
                    // Xử lý dữ liệu từ API ngoài nếu cần
                    userReq = (UserReq) userInfo.getData();

                }
                if(userReq ==null ){
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
                }
                if(!userReq.isActive()){
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ControllerUtil.invalidated(null, "Tai khoan cua ban da bi khoa");
                }
                redisAccountService.pushTokenToRedis(JWT,account.get().getId());
                AccountResponse accountResponse = accountMapper.accountToAccountDTO(account.get(), userReq);
                dataOutput.setSuccess(true).setData(JWT).setMessage("Dang nhap thanh cong");
                respDTO.setToken(JWT);
                respDTO.setAccount(accountResponse);

                return ControllerUtil.ok(respDTO);
            } else {
                dataOutput.setMessage("Dang nhap khong thanh cong");
                return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
            }
        }catch (CustomClientException e) {
            // Xử lý lỗi từ API bên ngoài

            log.error("Lỗi từ API bên ngoài: {}", e.getMessage());
            return ControllerUtil.error("Lỗi khi gọi API ngoài: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerUtil.error("Lỗi server");
        }
    }

//    Ve sua tiep
    @DeleteMapping("/delete/{accountId}")
    public ResponseEntity<Object> Delete(HttpServletRequest request,@RequestParam("accountId") Integer accountId) {
        try {
            String authHeader = request.getHeader("Authorization");
//            SecurityContext context = SecurityContextHolder.getContext();
            String adminId = jwtTokenService.getAccountId(authHeader.substring(7))+"";
            if(!"1".equals(adminId)) {
                return ResponseEntity.badRequest().body(
                        ResponseObject.<String>builder()
                                .message("Bạn không có quyền xem, vui lòng đăng nhập tài khoản ADMIN")
                                .build()
                );
            }
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            ResponseLoginDTO respDTO = ResponseLoginDTO.builder().build();
            Optional<Account> account = accountService.getAccountById(accountId);
            //check pass
            if (account.isPresent()) {
                String JWT = jwtTokenService.generateToken(account.get());
                redisAccountService.pushAccountToRedis(account.get());
                log.info("Got JWT : {}", JWT);


                // Gọi API ngoài thông qua Feign client
                ResponseEntity<UserClientResponse> externalApiResponse =null;
                try {
                    externalApiResponse = userClient.getUserInfo("Bearer " + JWT);
                }catch (CustomClientException e){

                }


                log.info("code");
                UserReq userReq =null;
                if(externalApiResponse==null) {
//                    return ControllerUtil.error("Lỗi khi gọi API ngoài");
                }
//                if (externalApiResponse.getStatusCode().is2xxSuccessful())
                else{
                    log.info("success");
                    UserClientResponse userInfo = (UserClientResponse)externalApiResponse.getBody();
                    // Xử lý dữ liệu từ API ngoài nếu cần
                    userReq = (UserReq) userInfo.getData();

                }
                AccountResponse accountResponse = accountMapper.accountToAccountDTO(account.get(), userReq);
                dataOutput.setSuccess(true).setData(JWT).setMessage("Dang nhap thanh cong");
                respDTO.setToken(JWT);
                respDTO.setAccount(accountResponse);

                return ControllerUtil.ok(respDTO);
            } else {
                dataOutput.setMessage("Dang nhap khong thanh cong");
                return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
            }
        }catch (CustomClientException e) {
            // Xử lý lỗi từ API bên ngoài

            log.error("Lỗi từ API bên ngoài: {}", e.getMessage());
            return ControllerUtil.error("Lỗi khi gọi API ngoài: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerUtil.error("Lỗi server");
        }
    }

    @GetMapping("/role")
    public ResponseEntity<Object> role(@RequestParam("accountId") int accountId) {
        try {

            DataOutput<Object> dataOutput = DataOutput.builder().build();
            AccountResponse respDTO = AccountResponse.builder().build();
            Optional<Account> account = accountService.getAccountById(accountId);
            //check pass
            if (account.isPresent()) {

                return ControllerUtil.ok(account.get().getRole().getId());
            } else {

                return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerUtil.error("Lỗi server");
        }
    }


    @GetMapping("/logout")
    public ResponseEntity<Object> logout(
//            @RequestParam("accountId") int accountId
            HttpServletRequest request
            ) {
        try {
            String authHeader = request.getHeader("Authorization");
//            SecurityContext context = SecurityContextHolder.getContext();
            String accountId = jwtTokenService.getAccountId(authHeader.substring(7))+"";
            redisAccountService.deleteToken(accountId);
            return ControllerUtil.ok("Đăng xuất thành công");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerUtil.error("Lỗi server");
        }
    }

    /**
     * Chuc nang dang nhap
     * Khi tai khoan cua nguoi dung da xac thuc moi co the dang nhap he thong
     */
    @GetMapping("/my-account")
    public ResponseEntity<Object> myAccount(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
//            SecurityContext context = SecurityContextHolder.getContext();
            String accountId = jwtTokenService.getAccountId(authHeader.substring(7))+"";
            if(!authHeader.substring(7).equals(redisAccountService.getTokenFromRedis(Integer.parseInt(accountId)))) {
                return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong, sai token");
            }

            log.info("account, {}" , accountId);
            ResponseLoginDTO responseLoginDTO=  ResponseLoginDTO.builder().build();
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            AccountResponse respDTO = AccountResponse.builder().build();
            Optional<Account> account = accountService.getAccountById(Integer.parseInt(accountId));
            //check pass
            if (account.isPresent()) {
                // Gọi API ngoài thông qua Feign client
                ResponseEntity<UserClientResponse> externalApiResponse =null;
                try {
                    externalApiResponse = userClient.getUserInfo("Bearer " + authHeader.substring(7));
                }catch (CustomClientException e){

                }


                log.info("code");
                UserReq userReq =null;
                if(externalApiResponse==null) {
//                    return ControllerUtil.error("Lỗi khi gọi API ngoài");
                }
//                if (externalApiResponse.getStatusCode().is2xxSuccessful())
                else{
                    log.info("success");
                    UserClientResponse userInfo = (UserClientResponse)externalApiResponse.getBody();
                    // Xử lý dữ liệu từ API ngoài nếu cần
                    userReq = (UserReq) userInfo.getData();

                }
                if(userReq ==null ){
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
                }
                if(!userReq.isActive()){
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ControllerUtil.invalidated(null, "Tai khoan cua ban da bi khoa");
                }
                String JWT = jwtTokenService.generateToken(account.get());

//                assert userReq != null;
                redisAccountService.pushTokenToRedis(JWT,Integer.parseInt(accountId));
                AccountResponse accountResponse = accountMapper.accountToAccountDTO(account.get(), userReq);
                dataOutput.setSuccess(true).setData(JWT).setMessage("Dang nhap thanh cong");
                responseLoginDTO.setToken(JWT);
                responseLoginDTO.setAccount(accountResponse);

//                AccountResponse accountResponse = accountMapper.accountToAccountDTO(account.get(),userReq);
                return ControllerUtil.ok(responseLoginDTO);
            } else {
                dataOutput.setMessage("Dang nhap khong thanh cong");
                return ControllerUtil.invalidated(null, "Dang nhap khong thanh cong");
            }
        }catch (CustomClientException e) {
            // Xử lý lỗi từ API bên ngoài

            log.error("Lỗi từ API bên ngoài: {}", e.getMessage());
            return ControllerUtil.error("Lỗi khi gọi API ngoài: " + e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ControllerUtil.error("Lỗi server");
        }
    }



    /**
     * Chuc nang dang nhap su dung email va otp
     * Su dung email de nhan ma OTP
     */
    @PostMapping("/login/request")
    public ResponseEntity<DataOutput<Object>> loginOTPRequest(@Valid @RequestBody String email) {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            Optional<Account> account = accountService.getAccountByEmail(email);
            if (account.isPresent()) {
                OTP otp = otpService.getOTPByAccountId(account.get().getId());
                if (otp != null) {
                    String OTP = otp.getOtp();
                    emailService.sendEmail(ecommerceEmail, email, email, OTP);
                    log.info("Got OTP : {}", otp.getOtp());
                    dataOutput.setSuccess(true).setData(null).setMessage("Kiem tra email cua ban");
                    return ResponseEntity.ok().body(dataOutput);
                } else {
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ResponseEntity.badRequest().body(dataOutput);
                }
            } else {
                dataOutput.setMessage("Khong tim thay email");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }

    /**
     * Chuc nang xac nhan dang nhap su dung email va otp
     * Xac thuc Email va ma OTP
     */
    @PostMapping("/login/response")
    public ResponseEntity<DataOutput<Object>> loginOTPResponse(@Valid @RequestBody String OTPRequest, String email, int account_id) {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            Optional<Account> account = accountService.getAccountByEmail(email);
            if (account.isPresent()) {
                OTP OTP = otpService.getOTPByAccountId(account_id);
                if (OTP != null) {
                    //xoa otp sau khi da su dung
                    otpService.deleteOTPByAccountId(OTP.getAccountid());
                    String JWT = jwtTokenService.generateToken(account.get());
                    log.info("Got JWT : {}", JWT);
                    dataOutput.setSuccess(true).setData(JWT).setMessage("Dang nhap thanh cong");
                    return ResponseEntity.ok().body(dataOutput);
                } else {
                    dataOutput.setMessage("Dang nhap khong thanh cong");
                    return ResponseEntity.badRequest().body(dataOutput);
                }
            } else {
                dataOutput.setMessage("Khong tim thay email");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }

    /**
     * Chuc nang dang ky nguoi dung
     */
    @PostMapping("/registry")
    public ResponseEntity<DataOutput<Object>> registry(@Valid @RequestBody RegistryInput inputDTO) {
        log.info("register");
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            if (accountService.checkIfUsernameExists(inputDTO.getUsername())) {
                dataOutput.setMessage("Username da ton tai");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            if (accountService.checkIfEmailExists(inputDTO.getEmail())) {
                dataOutput.setMessage("Email da ton tai");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            ERole eRole;
            Role  role;
            if (inputDTO.getRole() == null) {
                eRole = ERole.USER;
                role=roleService.getRoleByName(eRole.name());
            } else {
                eRole = ERole.valueOf(inputDTO.getRole());
                role=roleService.getRoleByName(eRole.name());
            }
            inputDTO.setPassword(passwordEncoder.encode(inputDTO.getPassword()));

            Account account = accountService.createAccount(accountMapper.registryInputToAccount(inputDTO, role));
            InitialUser initialUser = InitialUser.builder()
                    .is_active(true)
                    .user_name(inputDTO.getUsername())
                    .email(inputDTO.getEmail())
                    .account_id(account.getId()+"")
                    .build();
            userClient.initialUser(initialUser);
            dataOutput.setSuccess(true).setData(null).setMessage("Tao tai khoan thanh cong");
            return ResponseEntity.ok(dataOutput);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }
    @PostMapping("/insert")
    public ResponseEntity<DataOutput<Object>> sync() {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            String[][] users = {
                    {"dangnguyen", "123456789", "dangnguyen@example.com", "Đặng Đình Nguyên", "0900000001"},
                    {"hongquyen", "123456789", "hongquyen@example.com", "Bùi Thị Hồng Quyên", "0900000002"},
                    {"khanhnam", "123456789", "khanhnam@example.com", "Nguyễn Khánh Nam", "0900000003"},
                    {"minhhoa", "123456789", "minhhoa@example.com", "Trần Minh Hòa", "0900000004"},
                    {"anhtuan", "123456789", "anhtuan@example.com", "Lê Anh Tuấn", "0900000005"},
                    {"hoanganh", "123456789", "hoanganh@example.com", "Phạm Hoàng Anh", "0900000006"},
                    {"baoquoc", "123456789", "baoquoc@example.com", "Đỗ Bảo Quốc", "0900000007"},
                    {"thuylinh", "123456789", "thuylinh@example.com", "Lý Thúy Linh", "0900000008"},
                    {"quocbao", "123456789", "quocbao@example.com", "Phan Quốc Bảo", "0900000009"},
                    {"kimngoc", "123456789", "kimngoc@example.com", "Võ Kim Ngọc", "0900000010"},
                    {"minhtri", "123456789", "minhtri@example.com", "Đặng Minh Trí", "0900000011"},
                    {"huyhoang", "123456789", "huyhoang@example.com", "Nguyễn Huy Hoàng", "0900000012"},
                    {"lanhuong", "123456789", "lanhuong@example.com", "Bùi Lan Hương", "0900000013"},
                    {"anhthu", "123456789", "anhthu@example.com", "Lê Anh Thư", "0900000014"},
                    {"trungkien", "123456789", "trungkien@example.com", "Phạm Trung Kiên", "0900000015"},
                    {"minhtam", "123456789", "minhtam@example.com", "Ngô Minh Tâm", "0900000016"},
                    {"ngoctrai", "123456789", "ngoctrai@example.com", "Vũ Ngọc Trãi", "0900000017"},
                    {"hoangyen", "123456789", "hoangyen@example.com", "Đỗ Hoàng Yến", "0900000018"},
                    {"quanghuy", "123456789", "quanghuy@example.com", "Phan Quang Huy", "0900000019"},
                    {"duchung", "123456789", "duchung@example.com", "Trần Đức Hùng", "0900000020"},
                    {"baotran", "123456789", "baotran@example.com", "Nguyễn Bảo Trân", "0900000021"},
                    {"tuanminh", "123456789", "tuanminh@example.com", "Lê Tuấn Minh", "0900000022"},
                    {"thanhdat", "123456789", "thanhdat@example.com", "Trần Thành Đạt", "0900000023"},
                    {"phuonganh", "123456789", "phuonganh@example.com", "Vũ Phương Anh", "0900000024"},
                    {"nguyenduc", "123456789", "nguyenduc@example.com", "Đặng Nguyễn Đức", "0900000025"},
                    {"minhvuong", "123456789", "minhvuong@example.com", "Bùi Minh Vương", "0900000026"},
                    {"kimanh", "123456789", "kimanh@example.com", "Phạm Kim Anh", "0900000027"},
                    {"tuanhung", "123456789", "tuanhung@example.com", "Lý Tuấn Hưng", "0900000028"},
                    {"hoangtuan", "123456789", "hoangtuan@example.com", "Đỗ Hoàng Tuấn", "0900000029"},
                    {"quynhanh", "123456789", "quynhanh@example.com", "Trần Quỳnh Anh", "0900000030"},
                    {"thanhha", "123456789", "thanhha@example.com", "Võ Thanh Hà", "0900000031"},
                    {"phuocloc", "123456789", "phuocloc@example.com", "Nguyễn Phước Lộc", "0900000032"},
                    {"kimthoa", "123456789", "kimthoa@example.com", "Bùi Kim Thoa", "0900000033"},
                    {"huyenmy", "123456789", "huyenmy@example.com", "Phạm Huyền My", "0900000034"},
                    {"tuanphat", "123456789", "tuanphat@example.com", "Lê Tuấn Phát", "0900000035"},
                    {"minhhai", "123456789", "minhhai@example.com", "Đặng Minh Hải", "0900000036"},
                    {"ngoctram", "123456789", "ngoctram@example.com", "Phan Ngọc Trâm", "0900000037"},
                    {"kimyen", "123456789", "kimyen@example.com", "Vũ Kim Yến", "0900000038"},
                    {"anhlam", "123456789", "anhlam@example.com", "Trần Anh Lâm", "0900000039"},
                    {"quangnam", "123456789", "quangnam@example.com", "Nguyễn Quang Nam", "0900000040"},
                    {"minhkhoa", "123456789", "minhkhoa@example.com", "Đỗ Minh Khoa", "0900000041"},
                    {"ngocson", "123456789", "ngocson@example.com", "Võ Ngọc Sơn", "0900000042"},
                    {"thienkim", "123456789", "thienkim@example.com", "Lý Thiên Kim", "0900000043"},
                    {"hoangyen", "123456789", "hoangyen@example.com", "Phạm Hoàng Yến", "0900000044"},
                    {"nhuthao", "123456789", "nhuthao@example.com", "Ngô Như Thảo", "0900000045"},
                    {"duclong", "123456789", "duclong@example.com", "Vũ Đức Long", "0900000046"},
                    {"ngocthai", "123456789", "ngocthai@example.com", "Phan Ngọc Thái", "0900000047"},
                    {"thuylinh", "123456789", "thuylinh@example.com", "Nguyễn Thúy Linh", "0900000048"},
                    {"hoangvinh", "123456789", "hoangvinh@example.com", "Trần Hoàng Vinh", "0900000049"},
                    {"bachtu", "123456789", "bachtu@example.com", "Đỗ Bạch Tú", "0900000050"},
                    {"xuanthao", "123456789", "xuanthao@example.com", "Nguyễn Xuân Thảo", "0900000051"},
                    {"anhdung", "123456789", "anhdung@example.com", "Phạm Anh Dũng", "0900000052"},
                    {"minhngoc", "123456789", "minhngoc@example.com", "Đỗ Minh Ngọc", "0900000053"},
                    {"tuananh", "123456789", "tuananh@example.com", "Lê Tuấn Anh", "0900000054"},
                    {"thutrang", "123456789", "thutrang@example.com", "Bùi Thu Trang", "0900000055"},
                    {"hoangminh", "123456789", "hoangminh@example.com", "Trần Hoàng Minh", "0900000056"},
                    {"ngochuyen", "123456789", "ngochuyen@example.com", "Vũ Ngọc Huyền", "0900000057"},
                    {"thanhphong", "123456789", "thanhphong@example.com", "Nguyễn Thành Phong", "0900000058"},
                    {"khoinguyen", "123456789", "khoinguyen@example.com", "Phan Khôi Nguyên", "0900000059"},
                    {"hanhnguyen", "123456789", "hanhnguyen@example.com", "Đặng Hạnh Nguyên", "0900000060"},
                    {"minhduc", "123456789", "minhduc@example.com", "Nguyễn Minh Đức", "0900000061"},
                    {"thithuy", "123456789", "thithuy@example.com", "Bùi Thị Thủy", "0900000062"},
                    {"hoanganh", "123456789", "hoanganh@example.com", "Trần Hoàng Anh", "0900000063"},
                    {"quocanh", "123456789", "quocanh@example.com", "Vũ Quốc Anh", "0900000064"},
                    {"thanhdat", "123456789", "thanhdat@example.com", "Lê Thành Đạt", "0900000065"},
                    {"kimthoa", "123456789", "kimthoa@example.com", "Ngô Kim Thoa", "0900000066"},
                    {"nguyenngoc", "123456789", "nguyenngoc@example.com", "Nguyễn Ngọc", "0900000067"},
                    {"tienthanh", "123456789", "tienthanh@example.com", "Phạm Tiến Thành", "0900000068"},
                    {"xuanphat", "123456789", "xuanphat@example.com", "Đỗ Xuân Phát", "0900000069"},
                    {"linhchi", "123456789", "linhchi@example.com", "Bùi Linh Chi", "0900000070"},
                    {"anhminh", "123456789", "anhminh@example.com", "Trần Anh Minh", "0900000071"},
                    {"thithanh", "123456789", "thithanh@example.com", "Vũ Thị Thanh", "0900000072"},
                    {"anhtai", "123456789", "anhtai@example.com", "Lê Anh Tài", "0900000073"},
                    {"hanhnhung", "123456789", "hanhnhung@example.com", "Phan Hạnh Nhung", "0900000074"},
                    {"hongphuc", "123456789", "hongphuc@example.com", "Nguyễn Hồng Phúc", "0900000075"},
                    {"thanhthuy", "123456789", "thanhthuy@example.com", "Đặng Thanh Thủy", "0900000076"},
                    {"phuochoang", "123456789", "phuochoang@example.com", "Phạm Phước Hoàng", "0900000077"},
                    {"dinhthanh", "123456789", "dinhthanh@example.com", "Trần Đình Thành", "0900000078"},
                    {"thithao", "123456789", "thithao@example.com", "Lý Thị Thảo", "0900000079"},
                    {"thithuy", "123456789", "thithuy@example.com", "Ngô Thị Thúy", "0900000080"}
            };
            for(int i=0;i<users.length;i++){
                RegistryInput inputDTO=new RegistryInput();
                ERole eRole;
                Role  role;

                eRole = ERole.USER;
                role=roleService.getRoleByName(eRole.name());
                inputDTO.setEmail(users[i][2]);
                inputDTO.setUsername(users[i][0]);
                inputDTO.setPassword(passwordEncoder.encode(users[i][1]));

                Account account = accountService.createAccount(accountMapper.registryInputToAccount(inputDTO, role));
                InitialUser1 initialUser = InitialUser1.builder()
                        .is_active(true)
                        .user_name(inputDTO.getUsername())
                        .email(inputDTO.getEmail())
                        .account_id(account.getId()+"")
                        .full_name(users[i][3])
                        .phone(users[i][4])
                        .build();
                userClient.initialUser2(initialUser);
            }

            dataOutput.setSuccess(true).setData(null).setMessage("Tao tai khoan thanh cong");
            return ResponseEntity.ok(dataOutput);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }

    /**
     * Chuc nang quen mat khau su dung email va otp
     * Su dung email de nhan ma OTP
     */
    @GetMapping("/forgot/request")
    public ResponseEntity<DataOutput<Object>> requestForgotPassword(@Valid @RequestBody SendMailInput request) {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            String email=request.getEmail();
            Optional<Account> account = accountService.getAccountByEmail(email);
            if (account.isPresent()) {
                OTP OTP = otpService.getOTPByIdAccount(account.get().getId());
                if (OTP != null) {
                    emailService.sendEmail(ecommerceEmail, email, email, OTP.getOtp());
//                    otpService.getOTPByAccountId(account.get().getId());
                    dataOutput.setData(null).setSuccess(true).setMessage("Kiem tra mail cua ban");
                    return ResponseEntity.ok(dataOutput);
                } else {
                    dataOutput.setMessage("Khong the tao ma OTP");
                    return ResponseEntity.badRequest().body(dataOutput);
                }
            } else {
                dataOutput.setMessage("Khong tim thay tai khoan");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }

    }

    /**
     * Xac thuc tai khoan su dung email va ma otp
     */
    @GetMapping("/forgot/response")
    public ResponseEntity<DataOutput<Object>> responseForgotPassword(@Valid @RequestBody String email, String otp, int account_id) {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            OTP otp1 = otpService.getOTPByAccountId(account_id);
            Optional<Account> account = accountService.getAccountByEmail(email);
            if (otp1 != null) {
                if (account.isPresent()) {
                    String JWT = jwtTokenService.generateToken(account.get());
                    log.info("Got JWT : {}", JWT);
                    dataOutput.setSuccess(true).setData(JWT).setMessage("Lay JWT thanh cong");
                    otpService.deleteOTPByAccountId(account.get().getId());
                    return ResponseEntity.ok().body(dataOutput);
                } else {
                    dataOutput.setMessage("Khong tim thay tai khoan");
                    return ResponseEntity.badRequest().body(dataOutput);
                }
            } else {
                dataOutput.setMessage("Khong tim thay ma OTP");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }

    }

    /**
     * Thay doi tai khoan va mat khau
     * Chi co the hoat dong khi da dang nhap
     */
    @PostMapping("/change/request")
    public ResponseEntity<DataOutput<Object>> requestChangePassword(@Valid @RequestBody ChangeInput changeInput) {
        Optional<Account> account = accountService.getAccountByUsername(changeInput.getUsername());
        DataOutput<Object> dataOutput = DataOutput.builder().build();
        try {
            if (accounts.get(account.get().getId()) != null) {
                dataOutput.setMessage("Tai khoan dang thuc hien yeu cau thay doi thong tin");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            if (accountService.checkIfUsernameExists(changeInput.getUsername())) {
                dataOutput.setMessage("Username da ton tai");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            accounts.put(account.get().getId(), account.get());
            OTP otp = otpService.getOTPByAccountId(account.get().getId());
            if (otp != null) {
                String email = account.get().getEmail();
                String OTP = otp.getOtp();
                emailService.sendEmail(ecommerceEmail, email, email, OTP);
                dataOutput.setMessage("Kiem tra email cua ban").setData(null).setSuccess(true);
                return ResponseEntity.ok().body(dataOutput);
            } else {
                dataOutput.setMessage("Loi khi tao OTP");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }

    //can xoa thay doi trong vong 2 phut neu khong xac thuc otp
    @PostMapping("/change/response")
    public ResponseEntity<DataOutput<Object>> responseChangePassword(@Valid @RequestBody ChangeInput changeInput) {
        Optional<Account> account = accountService.getAccountByUsername(changeInput.getUsername());
        DataOutput<Object> dataOutput = DataOutput.builder().build();
        try {
            if (accounts.get(account.get().getId()) != null) {
                dataOutput.setMessage("Tai khoan dang thuc hien yeu cau thay doi thong tin");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            OTP otp = otpService.getOTPByAccountId(account.get().getId());
            if (otp != null) {
                Account accountInChange = Account.builder()
                        .id(account.get().getId())
                        .username(changeInput.getUsername())
                        .password(changeInput.getPassword())
//                        .phone(account.get().getPhone())
                        .email(account.get().getEmail())
                        .updated_at(LocalDateTime.now())
                        .build();
                accountService.createAccount(accountInChange);
                String JWT = jwtTokenService.generateToken(accountInChange);
                dataOutput.setData(JWT).setSuccess(true).setMessage("Thay doi thanh cong");
                return ResponseEntity.ok().body(dataOutput);
            } else {
                dataOutput.setMessage("Thay doi thong tin that bai");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }
    }

    //Gui mat khau cho nguoi dung khi quen mat khau
    @PostMapping("/forgot-password")
    public ResponseEntity<DataOutput<Object>> forgotPassword(@Valid @RequestBody SendMailInput request) {
        try {
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            String email=request.getEmail();
            Optional<Account> account = accountService.getAccountByEmail(email);
            if (account.isPresent()) {
                String passwordRandom=generateRandomString(10);

                emailService.sendEmail(ecommerceEmail, email, email, passwordRandom);
                String hashPass = passwordEncoder.encode(passwordRandom);
                log.info("password hash: " + hashPass +" : "+passwordRandom);
                account.get().setPassword(hashPass);
                accountService.changePassword(account.get());
                dataOutput.setData(null).setSuccess(true).setMessage("Kiem tra mail cua ban");
                return ResponseEntity.ok(dataOutput);

            } else {
                dataOutput.setMessage("Khong tim thay tai khoan");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }

    }


    @PostMapping("/change-password")
    public ResponseEntity<DataOutput<Object>> changePassword(HttpServletRequest httpServletRequest,@RequestBody ChangePasswordRequest request) {
        try {
            String authHeader = httpServletRequest.getHeader("Authorization");
            String accountId = jwtTokenService.getAccountId(authHeader.substring(7))+"";
            log.info("account, {}" , accountId);
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            Optional<Account> account = accountService.getAccountById(Integer.parseInt(accountId));
            if (account.isPresent()) {
                if(passwordEncoder.matches(request.getOldPassword(), account.get().getPassword())){
                    account.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
                    try {
                        accountService.changePassword(account.get());
                        dataOutput.setData(null).setSuccess(true).setMessage("Thay đổi mật khẩu thành công");
                        return ResponseEntity.ok(dataOutput);
                    }catch(Exception e){
                        log.error(e.getMessage());
                        dataOutput.setData(null).setSuccess(false).setMessage("Thay đổi mật khẩu thất bại");
                        return ResponseEntity.badRequest().body(dataOutput);
                    }

                }
                else {
                    dataOutput.setData(null).setSuccess(false).setMessage("Mật khẩu cũ khoog chính xác");
                    return ResponseEntity.badRequest().body(dataOutput);
                }

            } else {
                dataOutput.setMessage("Khong tim thay tai khoan");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }



    }


    @PostMapping("/change-email")
    public ResponseEntity<DataOutput<Object>> changeMail(HttpServletRequest httpServletRequest,@RequestBody ChangeMailRequest request) {
        try {

            String authHeader = httpServletRequest.getHeader("Authorization");
            String accountId = jwtTokenService.getAccountId(authHeader.substring(7))+"";
            log.info("account, {}" , accountId);
            DataOutput<Object> dataOutput = DataOutput.builder().build();
            if(request.getNewEmail()==null){
                dataOutput.setMessage("Mail không được để trống!");
                return ResponseEntity.badRequest().body(dataOutput);
            }
            Optional<Account> account = accountService.getAccountById(Integer.parseInt(accountId));
            if (account.isPresent()) {
                    account.get().setEmail(request.getNewEmail());
                    try {
                        userClient.changeEmail("Bearer " + authHeader.substring(7),request.getNewEmail());
                        accountService.changePassword(account.get());
                        dataOutput.setData(null).setSuccess(true).setMessage("Cập nhật mail thành công");
                        return ResponseEntity.ok(dataOutput);
                    }catch(Exception e){
                        log.error(e.getMessage());
                        dataOutput.setData(null).setSuccess(false).setMessage("Thay đổi mail thất bại");
                        return ResponseEntity.badRequest().body(dataOutput);
                    }


            } else {
                dataOutput.setMessage("Khong tim thay tai khoan");
                return ResponseEntity.badRequest().body(dataOutput);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(DataOutput.builder().message("Gap loi server, kiem tra log").build());
        }



    }





    //    Function random
    public  String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        return random.ints(length, 0, characters.length())
                .mapToObj(characters::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
