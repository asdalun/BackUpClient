package com.zyk.backup;

public class ServerConfig {
	//本机IP地址
	private String localIP;
	//备份服务器IP地址
	private String serverIP;
	//端口1
	private int port1;
	//端口2
	private int port2;
	
	ServerConfig() {
		localIP = "";
		serverIP = "";
		port1 = 0;
		port2 = 0;
	}
	
	public void setLocalIP(String _localIP) {
		this.localIP = _localIP;
	}
	
	public void setServerIP(String _serverIP) {
		this.serverIP = _serverIP;
	}
	
	public void setPort1(int _port1) {
		this.port1 = _port1;
	}
	
	public void setPort2(int _port2) {
		this.port2 = _port2;
	}
	
	public String getLocalIP() {
		return this.localIP;
	}
	
	public String getServerIP() {
		return this.serverIP;
	}
	
	public int getPort1() {
		return this.port1;
	}
	
	public int getPort2() {
		return this.port2;
	}
}
