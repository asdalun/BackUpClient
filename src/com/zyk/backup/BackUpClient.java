package com.zyk.backup;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;

public class BackUpClient {

	private static Logger logger = Logger.getLogger(BackUpClient.class);
	
	public static void main(String[] args) {
		BackUpClient buc = new BackUpClient();
		buc.run();
	}
	
	TimerTask upgradeTask = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			CheckUpgrade cu = new CheckUpgrade();
			if (cu.isNeedUpgrade()) {
				logger.info("ready to upgrade");
				try {
					Runtime runtime = Runtime.getRuntime();
					//Process process = runtime.exec("java -jar Upgrade.jar " + ConfigManager.getInstance().getUpgradeJarURL());
					runtime.exec("java -jar Upgrade.jar " + ConfigManager.getInstance().getUpgradeJarURL());
					logger.info("main end");
					System.exit(0);
//					if (process.waitFor() == 0) {
//						logger.info("main end");
//						System.exit(0);
//					}
				}
				catch (Exception ex) {
					logger.error("run upgrade error:" + ex.toString());
				}
			}
		}
		
	};
	/**
	 * 应用程序入口
	 */
	private void run() {
		try {
			//初始化配置信息
			logger.info("main start " + ConfigManager.getInstance().getCurVersion());
			ReceiveMessage rm1 = new ReceiveMessage("T1");
			rm1.start();
			ReceiveMessage rm2 = new ReceiveMessage("T2");
			rm2.start();	
			Timer timerUpgrade = new Timer();
			timerUpgrade.schedule(upgradeTask, 5000, ConfigManager.getInstance().getInterval());
		}
		catch(Exception ex) {
			logger.info("run error: " + ex.toString());
		}
	}

}
