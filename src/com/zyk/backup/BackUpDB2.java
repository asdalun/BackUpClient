package com.zyk.backup;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;

public class BackUpDB2 extends Thread {
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(BackUpDB2.class);
	/**
	 * 执行线程
	 */
	private Thread t;
	/**
	 * 线程名称
	 */
	private String threadName;
	/**
	 * 接收的命令串
	 */
	private String commandLine;
	
	public BackUpDB2(String tn, String cl) {
		this.threadName = tn;
		this.commandLine = cl;
	}
	/**
	 * 执行接收消息线程
	 */
	public void run() {
		String[] cmds = this.commandLine.split("\\|");
        String id = cmds[0];
        //String ip = cmds[1];      //ip地址
        //String cmd = cmds[2];     //执行命令
        String dbname = cmds[3];  //数据库实例名
        String backname = cmds[4];//上传的备份文件名称
        //String c_flog = cmds[5];  //备份标识
        //String savename = cmds[6];//本地保存文件名
        //SQL Server 写入
		Date now = new Date(); 
		String updateSql = "update t_soft_dayly_back_new set c_send_time='" + now.toString() + "' where i_id=" + id;
		ConfigManager.getInstance().getSQLServerManager().executeSQL(updateSql);
		for (int i = 0; i < ConfigManager.getInstance().getDB2Config().size(); i++) {       //查找本机中的数据库
			if (dbname.compareTo(ConfigManager.getInstance().getDB2Config().get(i).getDBName()) == 0) {
				if (ConfigManager.getInstance().getDB2Config().get(i).getUserName() != "") {
					if (this.backupOneDB2(ConfigManager.getInstance().getDB2Config().get(i), backname)) {
						//发送消息暂时屏蔽，等待上传完成后，再进行调试
//						String msg = id + "|" + ip + "|" + "upload|" + dbname + "|" + backname + ".dmp" + "|"
//								+ this.cm.getOracleConfig().get(i).getBFPath() + "|" 
//								+ this.cm.getFTPConfig().getUploadPath() + this.cm.getOracleConfig().get(i).getUploadPath() 
//								+ "|" + c_flog + "|" + this.cm.getOracleConfig().get(i).getBFPath()  + "/";
//						this.sendOneMessage(msg);
						//this.uploadToFTP(this.cm.getOracleConfig().get(i).getBFPath(), backname + ".dmp", this.cm.getOracleConfig().get(i).getUploadPath());
						//this.resetBackUp();
						UpLoadToFTP uf = new UpLoadToFTP("UpLoadToFTPThread", 
								ConfigManager.getInstance().getDB2Config().get(i).getBFPath(), 
								backname + ".dmp",
								ConfigManager.getInstance().getDB2Config().get(i).getUploadPath());
						uf.start();
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
			logger.error(this.threadName + "Backup Oracle DB2 have errors: " + db2c.getDBName() + " " + e.toString());
		}
		return false;
	}
	
	public void start() {
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}
}
