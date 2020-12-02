package com.wyu.dao;

import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import com.wyu.domain.User;
import com.wyu.utils.DataSourceUtils;

public class UserDao {

	public int regist(User user) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "insert into user values(?,?,?,?,?,?,?,?)";
		int row = runner.update(sql, user.getUid(),user.getUsername(),user.getPassword(),user.getName(),
					user.getEmail(),user.getTelephone(),user.getBirthday(),user.getSex());
		return row;
	}

	public Long checkUsername(String username) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select count(*) from user where username=?";
		Long isExist = (Long) runner.query(sql, new ScalarHandler(), username);
		return isExist;
	}

	public User checkUser(String username, String password) throws SQLException {
		QueryRunner runner = new QueryRunner(DataSourceUtils.getDataSource());
		String sql = "select * from user where username=? and password=?";
		User isExist = runner.query(sql, new BeanHandler<User>(User.class), username,password);
		return isExist;
	}

}
