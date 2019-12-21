package com.visionvera.service;

import com.visionvera.dao.xinhxu.UserDao;
import com.visionvera.vo.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    UserDao userDao;

    @Override
    public List<User> querAll() {
        return userDao.querAll();
    }
}
