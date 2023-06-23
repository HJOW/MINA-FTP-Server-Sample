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

package com.hjow.ftpserver;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;

public class FTPServer {
	protected FtpServer server = null;
	
	public FTPServer() {
		
	}

	public FTPServer(int port, UserManager userManager) throws FtpException {
		this();
		prepare(port, userManager);
	}
	
	public FTPServer(int port, UserManager userManager, ConnectionConfig config) throws FtpException {
		this();
		prepare(port, userManager, config);
	}
	
	public void prepare(int port, UserManager userManager) {
		prepare(port, userManager, null);
	}
	
	public void prepare(int port, UserManager userManager, ConnectionConfig config) {
		if(server != null) {
			server.stop();
			server = null;
		}
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		if(config != null) serverFactory.setConnectionConfig(config);
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(port);
		serverFactory.addListener("default", listenerFactory.createListener());
		serverFactory.setUserManager(userManager);
		
		server = serverFactory.createServer();
	}
	
	public void start() throws FtpException {
		if(server == null) throw new NullPointerException("This FTPServer instance is not prepared. Please call 'prepare(int, UserManager)' first !");
		server.start();
	}
	
	public FtpServer getFtpServerInstance() {
		return server;
	}
	
	public void stop() {
		if(server == null) return;
		server.stop();
	}
	
	public boolean isStopped() {
		if(server == null) return true;
		return server.isStopped();
	}
	
	public boolean isSuspended() {
		return server.isSuspended();
	}
	
	public static String getVersion() {
		return org.apache.ftpserver.Version.getVersion();
	}
	
	/*
	// SAMPLE
	public static void main(String[] args) throws FtpException {
		File roots = new File("G:\\temp\\test\\ft\\recv");
		int  port  = 1025;
		
		LiteUserManager userManager = new LiteUserManager();
		
		LiteUser one = new LiteUser();
		one.setName("root");
		one.setPassword("1");
		one.setEnabled(true);
		one.setHomeDirectory(roots.getAbsolutePath());
		one.setMaxIdleTime(0);
		one.setAdmin(true);
		one.addAuth(new Authority() {
			@Override
			public boolean canAuthorize(AuthorizationRequest request) {
				System.out.println("In liteuser canAuthorize, " + request);
				return true;
			}
			
			@Override
			public AuthorizationRequest authorize(AuthorizationRequest request) {
				System.out.println("In liteuser authorize, " + request);
				return request;
			}
		});
		userManager.save(one);
		
		FTPServer server = new FTPServer(port, userManager);
		server.start();
	}
	*/
}