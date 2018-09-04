package com.zyk.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Date;

import org.apache.log4j.Logger;

public class BackUp {
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(BackUpClient.class);
	/**
	 * 保存配置项
	 */
	private ConfigManager cm;
	/**
	 * 当前备份状态 0: 初始状态；1: 正接收数据；2: 执行备份数据库；3: 上传数据；
	 */
	private short backupState;
	/**
	 * 接收到的字符串
	 */
	private String receiveStr;
	/**
	 * 当前使用端口
	 */
	private int curPort;
	
	/**
	 * 构造函数
	 * @param _cm    传入配置项
	 */
	BackUp(ConfigManager _cm, int port) {
		logger.info(port + " is new");
		this.cm = _cm;
		if (port == 1) 
			curPort = cm.getServerConfig().getPort1();
		else if (port == 2)
			curPort = cm.getServerConfig().getPort2();
		//this.sendOneMessage("hello");
		//this.sc = null;
		//this.uploadToFTP("hello");
	}
	/**
	 * 连接socket服务器
	 * @throws IOException 
	 */
	public void ConnectToSev() {
		logger.info(curPort + " Ready to connect...");
		SocketChannel sc = null;
		String responseLine = "";
		try {
			InetAddress address = InetAddress.getByName(this.cm.getServerConfig().getServerIP());
			
			sc = SocketChannel.open(new InetSocketAddress(address, curPort));
//			int port = 36667;
//			SocketChannel sc = SocketChannel.open(new InetSocketAddress(address, port));
			logger.info(curPort + " Connect success!");
			//连接成功 初始化流
			InputStream inputStream = Channels.newInputStream(sc);
			InputStreamReader is = new InputStreamReader(inputStream, "UTF-8");
			BufferedReader in = new BufferedReader(is);
			this.backupState = 1;
			this.receiveStr = "";
			responseLine = in.readLine();
			if (responseLine == null) {
				logger.info("hava null");
				sc.close();
				this.resetBackUp();
				return;
			}
			this.receiveEnd(responseLine);

			sc.close();
		}
		catch(Exception ex) {

			logger.error(curPort + " Connect error: " + ex.toString());
			this.resetBackUp();
		}
	}
	/**
	 * 接收到命令字符串后，对字符串进行分解
	 * @param rs   接收到的命令字符串
	 */
	public void receiveEnd(String rs) {
//		if (rs == null) {
//			logger.info("hava null");
//			this.backupState = 0;
//			return;
//		}
			
		this.receiveStr = rs;
		this.backupState = 2;
		logger.info(curPort + " Receive complete: " + this.receiveStr);
		String[] cmds = this.receiveStr.split("\\|");
        String id = cmds[0];
        String ip = cmds[1];      //ip地址
        String cmd = cmds[2];     //执行命令
        String dbname = cmds[3];  //数据库实例名
        String backname = cmds[4];//上传的备份文件名称
        String c_flog = cmds[5];  //备份标识
        String savename = cmds[6];//本地保存文件名
        if (cmd.compareTo("backoracle") == 0) {
        	logger.info(curPort + " Ready to Backup Oracle DB: " + dbname);
        	BackUpOracle bo = new BackUpOracle("BackUpOracleThread", this.receiveStr);
        	bo.start();
        }
        else if (cmd.compareTo("db2") == 0) {
        	logger.info(curPort + " Ready to Backup DB2: " + dbname);
        	BackUpDB2 bd = new BackUpDB2("BackUpDB2Thread",  this.receiveStr);
        	bd.start();
        }
//        switch(cmd) {
//        case "backoracle":
//        	logger.info(curPort + " Ready to Backup Oracle DB: " + dbname);
//        	backupOracle(id, ip, dbname, backname, c_flog, savename);
//        	break;
//        case "db2":
//        	logger.info(curPort + " Ready to Backup DB2: " + dbname);
//        	backupDB2(id, ip, dbname, backname);
//        	break;
//        case "upload":
//        	logger.info(curPort + "Start upload data by ftp");
//        	
//        	break;
//        default:
//        	break;
//        }
	}
	/**
	 * 得到备份对象目前的工作状态
	 * @return
	 */
	public short getBackUpState() {
		return this.backupState;
	}
	/**
	 * 重置备份对象的工作信息
	 */
	public void resetBackUp() {
//		if (this.receiveTherad != null) {
//			this.receiveTherad.interrupt();
//			logger.info(curPort + "Kill a wait thread");
//		}
//		this.receiveTherad = null;
		this.backupState = 0;
		this.receiveStr = "";
	}
	/**
	 * 执行备份Oracle数据库操作
	 * @param id           sql server数据库中的标识ID
	 * @param ip           本机ip地址
	 * @param dbname       需要备份的数据库名称
	 * @param backname     备份后的文件名称
	 */
	public void backupOracle(String id, String ip, String dbname, String backname, 
			String  c_flog, String savename) {
		//Date now = new Date(); 
		//String updateSql = "update t_soft_dayly_back_new set c_send_time='" + now.toString() + "' where i_id=" + id;
		//this.cm.getSQLServerManager().executeSQL(updateSql);
		for (int i = 0; i < this.cm.getOracleConfig().size(); i++) {       //查找本机中的数据库
			if (dbname.compareTo(this.cm.getOracleConfig().get(i).getDBName()) == 0) {
				if (this.cm.getOracleConfig().get(i).getUserName() != "") {
					if (this.bakcupOneOracle(this.cm.getOracleConfig().get(i), backname)) {
						//发送消息暂时屏蔽，等待上传完成后，再进行调试
//						String msg = id + "|" + ip + "|" + "upload|" + dbname + "|" + backname + ".dmp" + "|"
//								+ this.cm.getOracleConfig().get(i).getBFPath() + "|" 
//								+ this.cm.getFTPConfig().getUploadPath() + this.cm.getOracleConfig().get(i).getUploadPath() 
//								+ "|" + c_flog + "|" + this.cm.getOracleConfig().get(i).getBFPath()  + "/";
//						this.sendOneMessage(msg);
						this.backupState = 3;
						this.uploadToFTP(this.cm.getOracleConfig().get(i).getBFPath(), backname + ".dmp", this.cm.getOracleConfig().get(i).getUploadPath());
						//this.resetBackUp();
						break;
					}
				}
			}
		}
	}
	/**
	 * 执行备份oracle数据库的操作
	 * oc: oracle数据库的备份信息
	 * backname：备份文件名称
	 */
	public Boolean bakcupOneOracle(OracleConfig oc, String backname) {
		File saveFile = new File(oc.getBFPath());
		if (!saveFile.exists()) {// 假设文件夹不存在
			saveFile.mkdirs();   // 创建文件夹
		}
		try {
			logger.info(curPort + " Backup command: " + "exp " + oc.getUserName() + "/" + oc.getPassword() + " file=" + oc.getBFPath() + 
					"/" + backname + ".dmp");
			Process process = Runtime.getRuntime().exec("exp " + oc.getUserName() + "/" + oc.getPassword() + " file=" + oc.getBFPath() + 
					"/" + backname + ".dmp");
			
			if(process.waitFor() == 0) {      //0 表示线程正常终止。 
				logger.info(curPort + " Backup oracle db success: " + backname);
				
				return true;
			}
		} 
		catch (Exception e) {
			logger.error(curPort + " Backup Oracle DB have errors: " + oc.getDBName() + " " + e.toString());
			this.resetBackUp();
		}
		return false;
	}
	/**
	 * 执行备份DB2数据库的操作
	 * @param id        
	 * @param ip              本机ip地址
	 * @param dbname          数据库名称
	 * @param backname        备份文件名称
	 */
	public void backupDB2(String id, String ip, String dbname, String backname) {
		Date now = new Date(); 
		String updateSql = "update t_soft_dayly_back_new set c_send_time='" + now.toString() + "' where i_id=" + id;
		this.cm.getSQLServerManager().executeSQL(updateSql);
		for (int i = 0; i < this.cm.getDB2Config().size(); i++) {       //查找本机中的数据库
			if (dbname.compareTo(this.cm.getDB2Config().get(i).getDBName()) == 0) {
				if (this.cm.getDB2Config().get(i).getUserName() != "") {
					if (this.backupOneDB2(this.cm.getDB2Config().get(i), backname)) {
						this.backupState = 3;
						//需要根据备份后的名称进行上传
						this.uploadToFTP(this.cm.getOracleConfig().get(i).getBFPath(), backname + ".dmp", this.cm.getOracleConfig().get(i).getUploadPath());
						//this.resetBackUp();
						break;
					}
				}
			}
		}
	}
	/**
	 * 执行备份DB2数据库的操作
	 * db2c: db2数据库的备份信息
	 * backname：备份文件名称
	 */
	public Boolean backupOneDB2(DB2Config db2c, String backname) {
		//db2 backup db tony online to /home/db2inst/db2backup
		File saveFile = new File(db2c.getBFPath());
		if (!saveFile.exists()) {// 假设文件夹不存在
			saveFile.mkdirs();   // 创建文件夹
		}
		try {
			Process process = Runtime.getRuntime().exec("db2 backup db " + db2c.getDBName() + " online to " + db2c.getBFPath() + " " + 
							backname + " compress");
			if(process.waitFor() == 0) {      //0 表示线程正常终止。 
				return true;
			}
		} 
			catch (Exception e) {
			logger.error(curPort + "Backup Oracle DB2 have errors: " + db2c.getDBName() + " " + e.toString());
		}
		return false;
	}
	/**
	 * 将消息发送到服务器操作  10.192.11.12
	 * @param msg
	 */
	public void sendOneMessage(String msg) {
		ByteBuffer buf = ByteBuffer.allocateDirect(1024);
	    try {
	    	buf.clear();
	    	InetAddress address = InetAddress.getByName(this.cm.getServerConfig().getServerIP());
			SocketChannel sc = SocketChannel.open(new InetSocketAddress(address, this.cm.getServerConfig().getPort1()));
			while (!sc.finishConnect()) {
				logger.info(curPort + " Wait for connect...");
				try {
					Thread.sleep(10);
				} 
				catch (InterruptedException e) {
					logger.error(curPort + " Send error 1:" + e.toString());
				}
			}
			buf.put(msg.getBytes());
			buf.flip();
			sc.write(buf);
			buf.clear();
			logger.info(curPort + " Send message complete:" + msg);
			int numBytesRead;
			while ((numBytesRead = sc.read(buf)) != -1) {
				if (numBytesRead == 0) {
					// 如果没有数据，则稍微等待一下
					try {
						logger.info("wait for receive...");
						Thread.sleep(1);
					} 
					catch (InterruptedException e) {
						logger.error("Send error 1：" + e.toString());
					}
					continue;
				}
				// 转到最开始
				buf.flip();
				String rs = "";
				while (buf.remaining() > 0) {
					System.out.print((char) buf.get());
					rs = rs + (char) buf.get();
				}
				logger.info("Receive Msg: " + rs);
				// 也可以转化为字符串，不过需要借助第三个变量了。
				// buf.get(buff, 0, numBytesRead);
				// System.out.println(new String(buff, 0, numBytesRead, "UTF-8"));
				// 复位，清空
				buf.clear();
			}
	    } 
	    catch (IOException e) {
	    	logger.error(curPort + " Send error 2:" + e.toString());
	    }
	}
	/**
	 * 调用FTPManager 将 备份后的文件上传到 FTP
	 * 服务器，上传信息从msg中分解
	 * @param msg
	 */
	public void uploadToFTP(String savename, String filename, String uploadpath) {
		logger.info("Ready to upload file...");
		//logger.info(msg);
		FTPManager fm = new FTPManager(this.cm.getFTPConfig());
        String re = fm.UploadFile(savename, filename, uploadpath);
        if (re == "success") {
        	this.resetBackUp();
        }
        else {
        	logger.error("Upload happen error. The next time will be try again...");
        }
	}
}
