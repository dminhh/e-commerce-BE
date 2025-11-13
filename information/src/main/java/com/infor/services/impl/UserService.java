package com.infor.services.impl;

import com.infor.data.UserList;
import com.infor.models.User;
import com.infor.repositories.UserRepository;
import com.infor.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.infor.repositories.UserRepository.Specs.*;

@Service
public class UserService implements IUserService {
    @Autowired
    private UserRepository userRepository;
    @Override
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(int id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByPhone(String phone) {
        return userRepository.findOne(byPhone(phone));
    }

    @Override
    public Optional<User> getUserByAccountId(int account_id) {
        return userRepository.findOne(byAccountId(account_id));
    }

    @Override
    public User changeUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(int id) {
        userRepository.delete(userRepository.findById(id).get());
    }

    @Override
    public UserList getAllUser(int page, int limit,
//                                 String fullName, String userName, String email, String phone,
                               String key,
                               boolean isActive, int accId) {
        Pageable pageable = PageRequest.of(page, limit);
//        // Tạo logic OR cho các trường tìm kiếm tùy chọn
//        Specification<User> searchSpec = Specification.where(byFullName(fullName))
//                .or(byUserName(userName))
//                .or(byEmail(email))
//                .or(byPhone(phone));
//
//        Specification<User> spec = Specification.where(byIsActive(isActive))
//                .and(searchSpec);

        // Kết hợp điều kiện isActive (AND) với tìm kiếm key (OR)
        Specification<User> spec = Specification.where(byIsActive(isActive))
                .and(byKey(key)).and(byNotAccountId(accId));

//
//                .and(byFullName(fullName))
////                .and(byUserName(userName))
////                .and(byEmail(email))
//                .and(byPhone(phone));

        Page<User> userPage = userRepository.findAll(spec, pageable);
//        UserList userListDTO=UserList.builder()
//                .total(userPage.getTotalElements())
//                .curentPage(userPage.getTotalPages())
//                .users(userPage.getContent())
//                .build();
        return UserList.builder()
                .totalPage(userPage.getTotalPages())
                .total(userPage.getTotalElements())
                .users(userPage.getContent())
                .build();
    }
}
