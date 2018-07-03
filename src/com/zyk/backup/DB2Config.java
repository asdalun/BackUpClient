package com.zyk.backup;

public class DB2Config extends DBConfig {
	/**
	 * 端口号
	 */
	private String port;
	
	DB2Config() {
		super();
		port = "";
	}
	
	public void setPort(String _port) {
		this.port = _port;
	}
	
	public String getPort() {
		return this.port;
	}

}
