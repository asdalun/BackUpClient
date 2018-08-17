package com.zyk.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ConfigManager {
	/**
	 * 单例
	 */
	private static ConfigManager cm;
	/**
	 * 当前系统的版本号
	 */
	private String curVer = "0.05";
	/**
	 * 缓存日志
	 */
	private static Logger logger = Logger.getLogger(ConfigManager.class);
	/**
	 * xml配置文件名称
	 */
	private String xmlFile = "aa.xml";
	/**
	 * 服务器配置对象
	 */
	private ServerConfig serverConfig;
	/**
	 * sql数据库对象
	 */
	private SQLServerManager sqlServerManager;
	/**
	 * Oracle数据库配置对象 数组
	 */
	private ArrayList<OracleConfig> oracleConfig_Ary;
	/**
	 * DB2数据库配置对象 数组
	 */
	private ArrayList<DB2Config> db2Config_Ary;
	/**
	 * FTP配置信息
	 */
	private FTPConfig ftpConfig;
	/**
	 * 启动备份线程间隔秒数
	 */
	private long interval;
	/**
	 * 连接失败次数最大默认为2000次，2000次一告警
	 */
	private int MAX_FAIL_CN;
	/**
	 * 升级xml文件的url
	 */
	private String upgradeXMLURL;
	/**
	 * 需要升级的jar包的url
	 */
	private String upgradeJarURL;
	
	ConfigManager() {
		this.serverConfig = new ServerConfig();
		this.sqlServerManager = new SQLServerManager();
		this.oracleConfig_Ary = new ArrayList<OracleConfig>();
		this.db2Config_Ary = new ArrayList<DB2Config>();
		this.ftpConfig = new FTPConfig();
		interval = 10000;
		MAX_FAIL_CN = 2000;
		this.loadConfig();
	}
	/**
	 * 加载配置文件信息  aa.xml
	 */
	private void loadConfig() {
		SAXReader reader = new SAXReader();
		try {
			Document document = reader.read(new File(xmlFile));
			Element dbinfo = document.getRootElement();
			Element intervalItem = dbinfo.element("Interval");
			interval = Long.parseLong(intervalItem.getText());
			Element maxcnItem = dbinfo.element("MaxCN");
			this.MAX_FAIL_CN = Integer.parseInt(maxcnItem.getText());
			Element upgradeXMLItem = dbinfo.element("UpgradeXML");
			this.upgradeXMLURL = upgradeXMLItem.getText();
			Element upgradeJarItem = dbinfo.element("UpgradeJar");
			this.upgradeJarURL = upgradeJarItem.getText();
			Element sqlItem = dbinfo.element("sql");
			this.sqlServerManager.setServerName(sqlItem.attributeValue("servername"));
			this.sqlServerManager.setDBName(sqlItem.attributeValue("dbname"));
			this.sqlServerManager.setUserName(sqlItem.attributeValue("username"));
			this.sqlServerManager.setPassword(sqlItem.attributeValue("password"));
			Iterator<?> it = dbinfo.elementIterator();
			while (it.hasNext()) {
				Element cmdItem = (Element)it.next();
				if (cmdItem.getName().compareTo("Interval") == 0 || cmdItem.getName().compareTo("sql") == 0 
						|| cmdItem.getName().compareTo("MaxCN") == 0 || cmdItem.getName().compareTo("UpgradeXML") == 0
						|| cmdItem.getName().compareTo("UpgradeJar") == 0) {
					continue;
				}
				String itemValue = cmdItem.attributeValue("value");
				//如果节点为本地设置信息
				if (itemValue.compareTo("local") == 0) {
					Iterator<?> it_local = cmdItem.elementIterator();
					while (it_local.hasNext()) {
						Element localItem = (Element)it_local.next();
						this.serverConfig.setLocalIP(localItem.attributeValue("localip"));
						this.serverConfig.setServerIP(localItem.attributeValue("serverip"));
						this.serverConfig.setPort1(Integer.parseInt(localItem.attributeValue("port1")));
						this.serverConfig.setPort2(Integer.parseInt(localItem.attributeValue("port2")));
					}
				}
				//如果节点为oracle数据库备份信息
				if (itemValue.compareTo("oracle") == 0) {
					Iterator<?> it_oracle = cmdItem.elementIterator();
					while (it_oracle.hasNext()) {
						Element oracleItem = (Element)it_oracle.next();
						OracleConfig oracleConfig = new OracleConfig();
						oracleConfig.setDBName(oracleItem.attributeValue("dbname"));
						oracleConfig.setUserName(oracleItem.attributeValue("username"));
						oracleConfig.setPassword(oracleItem.attributeValue("password"));
						oracleConfig.setServerName(oracleItem.attributeValue("servername"));
						oracleConfig.setBFPath(oracleItem.attributeValue("bfpath"));
						oracleConfig.setUploadPath(oracleItem.attributeValue("uploadpath"));
						this.oracleConfig_Ary.add(oracleConfig);
					}
				}
				//如果节点为DB2数据库备份信息
				if (itemValue.compareTo("db2") == 0) {
					Iterator<?> it_db2 = cmdItem.elementIterator();
					while (it_db2.hasNext()) {
						Element db2Item = (Element)it_db2.next();
						DB2Config db2Config = new DB2Config();
						db2Config.setDBName(db2Item.attributeValue("dbname"));
						db2Config.setUserName(db2Item.attributeValue("username"));
						db2Config.setPassword(db2Item.attributeValue("password"));
						db2Config.setServerName(db2Item.attributeValue("servername"));
						db2Config.setBFPath(db2Item.attributeValue("bfpath"));
						db2Config.setUploadPath(db2Item.attributeValue("uploadpath"));
						db2Config.setPort(db2Item.attributeValue("port"));
						this.db2Config_Ary.add(db2Config);
					}
				}
				//如果节点为ftp上传服务信息
				if (itemValue.compareTo("ftp") == 0) {
					Element ftpItem = cmdItem.element("item");
					this.ftpConfig.setServerName(ftpItem.attributeValue("ip"));
					this.ftpConfig.setUserName(ftpItem.attributeValue("username"));
					this.ftpConfig.setPassword(ftpItem.attributeValue("password"));
					this.ftpConfig.setPort(ftpItem.attributeValue("port"));
					this.ftpConfig.setUploadPath(ftpItem.attributeValue("uploadpath"));
				}
			}
		}
		catch (Exception ex) {
			logger.error("load xml error: " + ex.toString());
		}
	}
	
	public long getInterval() {
		return this.interval;
	}
	
	public ServerConfig getServerConfig() {
		return this.serverConfig;
	}
	
	public ArrayList<OracleConfig> getOracleConfig() {
		return this.oracleConfig_Ary;
	}
	
	public ArrayList<DB2Config> getDB2Config() {
		return this.db2Config_Ary;
	}
	
	public SQLServerManager getSQLServerManager() {
		return this.sqlServerManager;
	}
	
	public FTPConfig getFTPConfig() {
		return this.ftpConfig;
	}
	
	public int getMaxCN() {
		return this.MAX_FAIL_CN;
	}
	
	public String getUpgradeXMLURL() {
		return this.upgradeXMLURL;
	}
	
	public String getUpgradeJarURL() {
		return this.upgradeJarURL;
	}
	
	public String getCurVersion() {
		return this.curVer;
	}
	
	public static ConfigManager getInstance() {
		if (cm == null) {
			cm = new ConfigManager();
		}
		return cm;
	}
}

