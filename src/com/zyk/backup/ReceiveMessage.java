package com.zyk.backup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

public class ReceiveMessage extends Thread{
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(ReceiveMessage.class);
	/**
	 * 执行线程
	 */
	private Thread t;
	/**
	 * 服务器配置对象
	 */
	private ServerConfig serverConfig;
	/**
	 * 线程名称
	 */
	private String threadName;
	/**
	 * 当前线程使用端口
	 */
	private int curPort;
	/**
	 * 接收到的字符串
	 */
	private String receiveStr;
	
	/**
	 * 构造函数
	 * @param sc
	 * @param tn
	 */
	public ReceiveMessage(ServerConfig sc, String tn) {
		this.serverConfig = sc;
		this.threadName = tn;
		if (this.threadName == "ReceiveThread1") 
			curPort = this.serverConfig.getPort1();
		else if (this.threadName == "ReceiveThread2")
			curPort = this.serverConfig.getPort2();
	}
	/**
	 * 执行接收消息线程
	 */
	public void run() {
		while (true) {
			logger.info(this.threadName + " Ready to connect...");
			SocketChannel sc = null;
			String responseLine = "";
			try {
				InetAddress address = InetAddress.getByName(this.serverConfig.getServerIP());
				sc = SocketChannel.open(new InetSocketAddress(address, curPort));
				logger.info(this.threadName + " Connect success!");
				//连接成功 初始化流
				InputStream inputStream = Channels.newInputStream(sc);
				InputStreamReader is = new InputStreamReader(inputStream, "UTF-8");
				BufferedReader in = new BufferedReader(is);
				this.receiveStr = "";
				responseLine = in.readLine();
				if (responseLine == null) {
					logger.info("hava null");
					sc.close();
					continue;
				}
				this.receiveEnd(responseLine);
				sc.close();
			}
			catch(Exception ex) {
				logger.error(this.threadName + " Connect error: " + ex.toString());
				try {
					//连接失败以后程序暂停5秒
					Thread.sleep(5000);
				}
				catch(Exception ex1) {
					
				}
			}
		}
	}
	/**
	 * 接收到命令字符串后，对字符串进行分解
	 * @param rs   接收到的命令字符串
	 */
	public void receiveEnd(String rs) {
		this.receiveStr = rs;
		logger.info(curPort + " Receive complete: " + this.receiveStr);
		String[] cmds = this.receiveStr.split("\\|");
//        String id = cmds[0];
//        String ip = cmds[1];      //ip地址
        String cmd = cmds[2];     //执行命令
        String dbname = cmds[3];  //数据库实例名
//        String backname = cmds[4];//上传的备份文件名称
//        String c_flog = cmds[5];  //备份标识
//        String savename = cmds[6];//本地保存文件名
        switch(cmd) {
        case "backoracle":
        	logger.info(curPort + " Ready to Backup Oracle DB: " + dbname);
        	BackUpOracle bo = new BackUpOracle("BackUpOracleThread", this.receiveStr);
        	bo.start();
        	break;
        case "db2":
        	logger.info(curPort + " Ready to Backup DB2: " + dbname);
        	//backupDB2(id, ip, dbname, backname);
        	break;
        case "upload":
        	logger.info(curPort + "Start upload data by ftp");
        	
        	break;
        default:
        	break;
        }
	}

	public void start() {
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}
}
