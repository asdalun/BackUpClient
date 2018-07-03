package com.zyk.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

public class FTPManager {
	//缓存日志
	private static Logger logger = Logger.getLogger(FTPManager.class);
	/**
	 * ftp配置信息项
	 */
	private FTPConfig ftpConfig;
	/**
	 * 准备上传的文件名称
	 */
	private String fileName;
	/**
	 * 准备上传的文件的本地路径＋文件名称
	 */
	private String localPath;
	/**
	 * 上传到的ftp路径
	 */
	private String toFTPPath;
	/**
	 * ftp 客户端
	 */
	private FTPClient ftpClient;
	
	FTPManager(FTPConfig _ftpConfig) {
		this.ftpConfig = _ftpConfig;
		this.fileName = "";
		this.toFTPPath = this.ftpConfig.getBFPath();
		this.localPath = "";
		ftpClient = null;
	}
	/**
	 * 上传文件到ftp服务器
	 * @param _localPath    本地路径＋本地文件名称
	 * @param _fileName     本地文件名称
	 * @param _toFTPPath    目标文件夹
	 * @return "success" 上传成功； "error" 上传失败；
	 */
	public String UploadFile(String _localPath, String _fileName, String _toFTPPath) {
		logger.info(_localPath + ", " + _fileName + ", " + _toFTPPath); 
		String re = "";
		this.localPath = _localPath + "/" + _fileName;
		this.fileName = _fileName;
		this.toFTPPath = _toFTPPath;
		if (!this.initFTPClient()) {
			return "error";
		}
		InputStream inputStream = null;
		try {
			logger.info("Start to ftp file...");
			inputStream = new FileInputStream(new File(this.localPath));
			this.ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			this.ftpClient.enterLocalPassiveMode();
			//logger.info(this.toFTPPath);
			//this.ftpClient.makeDirectory(this.toFTPPath);
			//this.ftpClient.enterLocalPassiveMode();
			//logger.info("upload path: " + this.ftpConfig.getUploadPath());
			if (!this.ftpClient.changeWorkingDirectory(this.toFTPPath)) {
				logger.error("change dir error!");
				//re = "error";
			}
			this.ftpClient.storeFile(this.fileName, inputStream);
			inputStream.close();
			this.ftpClient.logout();
			re = "success";
			logger.info("ftp upload complete!");
		}
		catch (Exception e) {
			logger.error("ftp upload error: " + e.toString());
			re = "error";
		}
		finally {
			if (this.ftpClient.isConnected()) {
				try {
					this.ftpClient.disconnect();
					logger.info("ftp connect close.");
				}
				catch (Exception e) {
					logger.error("Close ftp connect happen error: " + e.toString());
				}
			}
		}
		return re;
	}
	/**
	 * 初始化ftpClient
	 */
	public Boolean initFTPClient() {
		this.ftpClient = new FTPClient();
		this.ftpClient.setControlEncoding("utf-8");
		try {
			logger.info("Connect ftp server:" + this.ftpConfig.getServerName() + ":" + this.ftpConfig.getPort());
			this.ftpClient.connect(this.ftpConfig.getServerName(), Integer.parseInt(this.ftpConfig.getPort()));
			this.ftpClient.login(this.ftpConfig.getUserName(), this.ftpConfig.getPassword());
			int replyCode = this.ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				logger.error("Connect ftp fail");
				return false;
			}
			logger.info("Connect ftp success");
			return true;
		}
		catch(Exception e) {
			logger.error("Connect ftp fail:" + e.toString());
			return false;
		}
	}
	
}
