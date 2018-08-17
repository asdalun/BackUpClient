package com.zyk.backup;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * 用于检测是否需要升级类
 * @author dalun
 *
 */
public class CheckUpgrade {

	private static Logger logger = Logger.getLogger(CheckUpgrade.class);
		
	public CheckUpgrade() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * 检测系统是否需要升级
	 * @return   需要返回true；不需要返回false
	 */
	public boolean isNeedUpgrade() {
		HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        Document document = null;
        try {
        	String url_s = ConfigManager.getInstance().getUpgradeXMLURL();
            URL url = new URL(url_s);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            is = connection.getInputStream();
            SAXReader reader = new SAXReader();
            document = reader.read(is);
            Element root = document.getRootElement();
            Element version = root.element("version");
            double newVer = Double.parseDouble(version.getText());
            double curVer = Double.parseDouble(ConfigManager.getInstance().getCurVersion());
            if (curVer < newVer) {
            	Element autoug = root.element("autoupgrade");
            	//logger.info(autoug.getText());
            	if ("1".compareTo(autoug.getText().trim()) == 0) {
            		//logger.info("1");
            		return true;
            	}
            	else {
            		//logger.info("2");
            		return false;
            	}
            		
            }
            else
            	return false;
        }
        catch(Exception ex) {
        	logger.error("check upgrade error:" + ex.toString());
        	return false;
        }
        finally {
        	// 关闭资源
        	if (null != br) {
	            try {
	                br.close();
	            } 
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        if (null != is) {
	            try {
	                is.close();
	            } 
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        connection.disconnect();// 关闭远程连
        }
	}
}
