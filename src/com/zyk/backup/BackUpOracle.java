package com.zyk.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class BackUpOracle extends Thread{
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(BackUpOracle.class);
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
	/**
	 * 构造函数
	 * @param cl
	 * @param tn
	 */
	public BackUpOracle(String tn, String cl) {
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
		for (int i = 0; i < ConfigManager.getInstance().getOracleConfig().size(); i++) {       //查找本机中的数据库
			if (dbname.compareTo(ConfigManager.getInstance().getOracleConfig().get(i).getDBName()) == 0) {
				if (ConfigManager.getInstance().getOracleConfig().get(i).getUserName() != "") {
					if (this.bakcupOneOracle(ConfigManager.getInstance().getOracleConfig().get(i), backname)) {
						//发送消息暂时屏蔽，等待上传完成后，再进行调试
//						String msg = id + "|" + ip + "|" + "upload|" + dbname + "|" + backname + ".dmp" + "|"
//								+ this.cm.getOracleConfig().get(i).getBFPath() + "|" 
//								+ this.cm.getFTPConfig().getUploadPath() + this.cm.getOracleConfig().get(i).getUploadPath() 
//								+ "|" + c_flog + "|" + this.cm.getOracleConfig().get(i).getBFPath()  + "/";
//						this.sendOneMessage(msg);
						//this.uploadToFTP(this.cm.getOracleConfig().get(i).getBFPath(), backname + ".dmp", this.cm.getOracleConfig().get(i).getUploadPath());
						//this.resetBackUp();
						UpLoadToFTP uf = new UpLoadToFTP("UpLoadToFTPThread", 
								ConfigManager.getInstance().getOracleConfig().get(i).getBFPath(), 
								backname + ".dmp",
								ConfigManager.getInstance().getOracleConfig().get(i).getUploadPath());
						uf.start();
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
//			logger.info(this.threadName + " Backup command: " + "exp " + oc.getUserName() + "/" + oc.getPassword() + " file=" + oc.getBFPath() + 
//					"/" + backname + ".dmp");
//			Process process = Runtime.getRuntime().exec("exp " + oc.getUserName() + "/" + oc.getPassword() + " file=" + oc.getBFPath() + 
//					"/" + backname + ".dmp");
//			ReadBuffered rb_info = new ReadBuffered(process, "info");
//			rb_info.start();
//			ReadBuffered rb_err = new ReadBuffered(process, "err");
//			rb_err.start();
//			process.waitFor();
			
			List<String> cmdList = Arrays.asList("exp", oc.getUserName() + "/" + oc.getPassword(), "file=" + oc.getBFPath() + "/" + backname + ".dmp" );
			ProcessBuilder pb = new ProcessBuilder(cmdList);
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()));  
	        String line=null;  
	        while((line=reader.readLine())!=null){  
	        	logger.info(line);  
	        }    
	        process.waitFor();
			logger.info(this.threadName + " Backup oracle db success: " + backname);
			return true;
		} 
		catch (Exception e) {
			logger.error(this.threadName + " Backup Oracle DB have errors: " + oc.getDBName() + " " + e.toString());
			return false;
		}
		
	}
	
	public void start() {
		if (t == null) {
			t = new Thread(this, threadName);
			t.start();
		}
	}
}
