package com.wyu.service;

import java.sql.SQLException;

import com.wyu.dao.UserDao;
import com.wyu.domain.User;

public class UserService {

	public boolean regist(User user) {
		UserDao dao = new UserDao();
		int row = 0;
		try {
			row = dao.regist(user);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return row>0?true:false;
	}

	public boolean checkUsername(String username) {
		UserDao dao = new UserDao();
		Long isExist = 0L;
		try {
			isExist = dao.checkUsername(username);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isExist>0?true:false;
	}

	public User checkUser(String username, String password) {
		UserDao dao = new UserDao();
		User isExist = null;
		try {
			isExist = dao.checkUser(username,password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return isExist;
	}

}
