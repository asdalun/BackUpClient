package com.zyk.backup;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class BackUpClient {

	private static Logger logger = Logger.getLogger(BackUpClient.class);
	//备份操作对象 port1
	private BackUp bu1;
	//备份操作对象 port2
	private BackUp bu2;
	//保持应配置项
	private ConfigManager cm;
	private Timer runTimer1;
	private Timer runTimer2;
	
	public static void main(String[] args) {
		BackUpClient buc = new BackUpClient();
		buc.run();
//		String xx = "23|10.211.248.46|backsql|checkserver|checkserver02";
//		String[] ss = xx.split("\\|");
//		System.out.println(ss[2]);
		
		
	}
	
	TimerTask bfTask1 = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (bu1 == null) {
				bu1 = new BackUp(cm, 1);
			}
			if (bu1.getBackUpState() == 4) {     
				//如果接收数据在等待状态
				bu1.resetBackUp();
			}
			if (bu1.getBackUpState() == 0) {
				//bu1 = null;
				//bu1 = new BackUp(cm, 1);
				bu1.ConnectToSev();
			}
			else if (bu1.getBackUpState() == 1) {      //在等待发送数据状态重新连接
				bu1.resetBackUp();
				bu1.ConnectToSev();
				//logger.info("等待服务器发送数据");
			}
			else if (bu1.getBackUpState() == 2) {
				logger.info("Backuping...");
			}
			
		}
		
	};
	
	
	TimerTask bfTask2 = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (bu2 == null) {
				bu2 = new BackUp(cm, 2);
				//logger.info("the state is new " + bu2.getBackUpState());
			}
			else {
				logger.info("the state is " + bu2.getBackUpState());
			}
			if (bu2.getBackUpState() == 4) {     
				//如果接收数据在等待状态
				bu2.resetBackUp();
			}
			if (bu2.getBackUpState() == 0) {
				//bu2 = null;
				//bu2 = new BackUp(cm, 2);
				bu2.ConnectToSev();
			}
			else if (bu2.getBackUpState() == 1) {      //在等待发送数据状态重新连接
				bu2.resetBackUp();
				bu2.ConnectToSev();
				//logger.info("等待服务器发送数据");
			}
			else if (bu2.getBackUpState() == 2) {
				logger.info("Backuping...");
			}
		}
		
	};
	/**
	 * 应用程序入口
	 */
	private void run() {
		try {
			//初始化配置信息
			cm = new ConfigManager();
			bu1 = null;
			bu2 = null;

			runTimer2 = new Timer();
			runTimer2.schedule(bfTask2, 0, cm.getInterval());
//			Thread.sleep(3000);
//			runTimer1 = new Timer();
//			runTimer1.schedule(bfTask1, 0, cm.getInterval());
			
			
		}
		catch(Exception ex) {
			logger.info("run error: " + ex.toString());
		}
	}
}
