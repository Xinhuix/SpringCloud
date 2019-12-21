package com.visionvera.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class MysqlJdbc {

	Connection con = null; // 定义一个MYSQL链接对象
	String driver;
	String url;
	String user;
	String pwd;
	public MysqlJdbc() {
		Properties prop = new Properties();
//		String path = (getPath() + "properties/jdbc.properties").replaceAll("%20", " ");
		String path = "/properties/jdbc.properties";
		InputStream in = this.getClass().getResourceAsStream(path);
		try {
			prop.load(in);
			driver = prop.getProperty("mysql.driver").trim();
			url = prop.getProperty("mysql.url").trim();
			user = prop.getProperty("mysql.username").trim();
			pwd = prop.getProperty("mysql.password").trim();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public int open() throws SQLException {
		try {
			Class.forName(driver).newInstance();
			con = (Connection) DriverManager.getConnection(url, user, pwd); // 链接本地MYSQL
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public int close() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public List<String> getResults(String sql) {
		List<String> list = new ArrayList<String>();
		Statement stmt; // 创建声明
		try {
			stmt = (Statement) con.createStatement();
			ResultSet selectRes = stmt.executeQuery(sql);
			while (selectRes.next()) { // 循环输出结果集
				list.add(selectRes.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

	@SuppressWarnings("unused")
	private String getPath() {
		// 取得根目录路径
		String rootPath = getClass().getResource("/").getFile().toString();
		return rootPath.substring(1);
	}

}
