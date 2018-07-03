package com.zyk.backup;

public class DBConfig {
	//数据库名称
	private String dbName;
	//用户名
	private String userName;
	//密码
	private String password;
	//服务器名称
	private String serverName;
	//备份路径
	private String bfPath;
	//上传路径
	private String uploadPath;
	
	DBConfig() {
		this.dbName = "";
		this.userName = "";
		this.password = "";
		this.serverName = "";
		this.bfPath = "";
		this.uploadPath = "";
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
	
	public void setBFPath(String _bfPath) {
		this.bfPath = _bfPath;
	}
	
	public void setUploadPath(String _uploadPath) {
		this.uploadPath = _uploadPath;
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
	
	public String getBFPath() {
		return this.bfPath;
	}
	
	public String getUploadPath() {
		return this.uploadPath;
	}
}
