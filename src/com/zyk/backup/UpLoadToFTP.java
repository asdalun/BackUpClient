package com.zyk.backup;

import org.apache.log4j.Logger;

public class UpLoadToFTP extends Thread{
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(UpLoadToFTP.class);
	/**
	 * 执行线程
	 */
	private Thread t;
	/**
	 * 服务器配置对象
	 */
	private ConfigManager cm;
	/**
	 * 线程名称
	 */
	private String threadName;
	/**
	 * 备份后的文件所在路径
	 */
	private String bfPath;
	/**
	 * 备份文件名称
	 */
	private String fileName;
	/**
	 * 上传路径，ftp服务器路径
	 */
	private String uploadPath;
	
	public UpLoadToFTP(String tn, String bp, String fn, String up, ConfigManager _cm) {
		// TODO Auto-generated constructor stub
		this.threadName = tn;
		this.bfPath = bp;
		this.fileName = fn;
		this.uploadPath = up;
		this.cm = _cm;
	}
	
	public void run() {
		logger.info("Ready to upload file...");
		//logger.info(msg);
		FTPManager fm = new FTPManager(this.cm.getFTPConfig());
        String re = fm.UploadFile(this.bfPath, this.fileName, this.uploadPath);
        if (re == "success") {
        	logger.error("upload success!");
        }
        else {
        	logger.error("Upload happen error. The next time will be try again...");
        }
	}
	
	public void start() {
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}

}
