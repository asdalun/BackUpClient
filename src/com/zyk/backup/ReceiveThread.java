package com.zyk.backup;

import java.io.BufferedReader;

import org.apache.log4j.Logger;

public class ReceiveThread extends Thread{

	private Logger logger;
	
	private BufferedReader in;
	
	private BackUp bu;
	
	ReceiveThread(BufferedReader _in, Logger l, BackUp _bu) {
		this.in = _in;
		this.logger = l;
		this.bu = _bu;
	}
	
	public void run() {
		try {
			logger.info("接收服务器的数据");            
			String responseLine = ""; 
			responseLine = in.readLine();
			bu.receiveEnd(responseLine);
			logger.info("接收服务器的数据完毕" + responseLine);
		}
		catch (Exception ex) {
			logger.error("接收数据出错：" + ex.toString());
			//bu.resetBackUp();
			//this.interrupt();
			//bu.resetBackUp();
		}
	}
}
