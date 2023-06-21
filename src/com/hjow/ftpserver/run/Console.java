/*
This project is just simple implementation of Apache MINA FTP server.

Copyright 2023 HJOW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.hjow.ftpserver.run;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.hjow.ftpserver.FTPServer;
import com.hjow.ftpserver.LiteUser;
import com.hjow.ftpserver.LiteUserManager;

public class Console {
	static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Console.class);
	public static void main(String[] args) {
		Properties configs = new Properties();
		InputStream in1 = null;
		try {
			String strRoot, strPort;
			strRoot = System.getProperty("user.home") + File.separator + "ftp";
			strPort = "21";
			
			in1 = Console.class.getResourceAsStream("config.xml");
			if(in1 != null) {
				configs.loadFromXML(in1);
				in1.close();
				in1 = null;
			}
			
			if(configs.containsKey("root")) strRoot = configs.getProperty("root").trim();
			if(configs.containsKey("port")) strPort = configs.getProperty("port").trim();
			
			File roots = new File(strRoot);
			int  port  = Integer.parseInt(strPort.trim());
			
			if(! roots.exists()) roots.mkdirs();
			
			LiteUserManager userManager = new LiteUserManager();
			
			JSONParser parser = new JSONParser();
			JSONArray users = (JSONArray) parser.parse(configs.getProperty("user"));
			
			for(int idx=0; idx<users.size(); idx++) {
				JSONObject userOne = (JSONObject) users.get(idx);
				
				LiteUser one = new LiteUser();
				one.setName(String.valueOf(userOne.get("name")));
				one.setPassword(String.valueOf(userOne.get("password")));
				
				if(userOne.containsKey("enabled")) {
					String ena = userOne.get("enabled").toString().toLowerCase().trim();
					one.setEnabled((ena.equals("y") || ena.equals("yes") || ena.equals("t") || ena.equals("true"))); 
				} else {
					one.setEnabled(true);
				}
				
				if(userOne.containsKey("home")) {
					String strHome = userOne.get("home").toString().trim();
					if(strHome.contains("..")) throw new IllegalArgumentException("Cannot use .. in home directory !");
					one.setHomeDirectory(roots.getAbsolutePath() + strHome);
				} else {
					one.setHomeDirectory(roots.getAbsolutePath());
				}
				
				if(userOne.containsKey("maxIdleTime")) {
					String strMaxIdleTime = userOne.get("maxIdleTime").toString().trim();
					one.setMaxIdleTime(Integer.parseInt(strMaxIdleTime));
				} else {
					one.setMaxIdleTime(0);
				}
				
				if(userOne.containsKey("admin")) {
					String ena = userOne.get("admin").toString().toLowerCase().trim();
					one.setAdmin((ena.equals("y") || ena.equals("yes") || ena.equals("t") || ena.equals("true"))); 
				} else {
					one.setAdmin(true);
				}
				
				one.setAdmin(true);
				one.addAuth(new Authority() {
					@Override
					public boolean canAuthorize(AuthorizationRequest request) {
						LOGGER.info("In liteuser canAuthorize, " + request);
						return true;
					}
					
					@Override
					public AuthorizationRequest authorize(AuthorizationRequest request) {
						LOGGER.info("In liteuser authorize, " + request);
						return request;
					}
				});
				userManager.save(one);
			}
			
			FTPServer server = new FTPServer(port, userManager);
			server.start();
		} catch(Throwable t) {
			LOGGER.error(t.getMessage(), t);
		} finally {
			if(in1 != null) {
				try { in1.close(); } catch(Throwable ignores) {}
			}
		}
	}
}
