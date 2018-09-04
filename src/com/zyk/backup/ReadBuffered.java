package com.zyk.backup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class ReadBuffered extends Thread{
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(ReadBuffered.class);
	/*
	 * 执行备份的线程
	 */
	private Process process;
	/*
	 * 读取的类型 info：正常的信息；err：错误的信息
	 */
	private String readType;
		
	public ReadBuffered(Process p, String rt) {
		// TODO Auto-generated constructor stub
		this.process = p;
		this.readType = rt;
	}
	
	public void run() {
		BufferedReader in = null;
		if (this.readType.compareTo("info") == 0)
			in = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
		else if (this.readType.compareTo("err") == 0)
			in = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
		String line = null;
		try {
			while((line = in.readLine()) != null) {
				logger.info(this.readType + line);
			}
			in.close();
		}
		catch (IOException e) {
			logger.error(this.readType + "read buffered error:" + e.toString());
		}		
	}

}
