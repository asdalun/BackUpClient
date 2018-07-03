package com.zyk.backup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class SQLServerManager {
	//缓存日志
	private static Logger logger = Logger.getLogger(SQLServerManager.class);
	//数据库名称
	private String dbName;
	//用户名
	private String userName;
	//密码
	private String password;
	//服务器名称
	private String serverName;
	
	SQLServerManager() {
		this.dbName = "";
		this.userName = "";
		this.password = "";
		this.serverName = "";
	}
	
	public void setDBName(String _dbName) {
		this.dbName = _dbName;
	}
	
	public void setUserName(String _userName) {
		this.userName = _userName;
	}
	
	public void setPassword(String _password) {
		this.password = _password;
	}
	
	public void setServerName(String _serverName) {
		this.serverName = _serverName;
	}
	
	public String getDBName() {
		return this.dbName;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	public String getServerName() {
		return this.serverName;
	}
	
	public String getPassword() {
		return this.password;
	}
	//连接数据库
	public Connection getConnection() {
		try 
		{
			Connection con;
			String url = "jdbc:sqlserver://" + this.getServerName() + ":1433;databaseName=" + this.getDBName();
			con = DriverManager.getConnection(url, this.getUserName(), this.getPassword());    
			return con;
		} 
		catch (SQLException e) 
		{
			logger.error("sql db connect fail:" + e.toString());
			return null;
		}
	}
	
	public void getinfo() {
		Connection con = this.getConnection();
		if (con == null) {
			logger.error("con is null");
			return;
		}
		Statement sql;
		ResultSet rs; 
		try {
			sql = con.createStatement();
			rs = sql.executeQuery("select DoorCode from T_PayDetail");
			while (rs.next()) {
				logger.info(rs.getString("DoorCode"));
			}
		}
		catch (SQLException e) {
			logger.error("Search db fail:" + e.toString());
		}
	}
	
	public void executeSQL(String _sql) {
		Connection con = this.getConnection();
		if (con == null) {
			return;
		}
		Statement st;
		try {
			st = con.createStatement();
			st.executeUpdate(_sql);
			st.close();
			con.close();
		}
		catch (SQLException e) {
			logger.error("Update SQL DB fail:" + e.toString());
		}
	}
}
