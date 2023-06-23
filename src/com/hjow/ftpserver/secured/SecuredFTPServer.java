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
package com.hjow.ftpserver.secured;

import java.io.File;

import org.apache.ftpserver.ConnectionConfig;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;

import com.hjow.ftpserver.FTPServer;

public class SecuredFTPServer extends FTPServer {
	protected File   fileKeystore     = null;
	protected String keystorePassword = null;
	
    public SecuredFTPServer() {
    	super();
    }
    
    public SecuredFTPServer(int port, UserManager userManager, File keystore, String keystorePassword) throws FtpException {
		this(port, userManager, keystore, keystorePassword, null);
	}
    
    public SecuredFTPServer(int port, UserManager userManager, File keystore, String keystorePassword, ConnectionConfig config) throws FtpException {
		this();
		setKeyStore(keystore);
		setKeyStorePassword(keystorePassword);
		prepare(port, userManager, config);
	}
    
    @Override
    public void prepare(int port, UserManager userManager, ConnectionConfig config) {
    	if(fileKeystore == null) throw new NullPointerException("There is no keystore file selected. Please call 'setKeyStore(file)' first !");
    	if(! fileKeystore.exists()) throw new RuntimeException("File is not exist : " + fileKeystore.getAbsolutePath());
    	if(keystorePassword == null) throw new NullPointerException("There is no keystore password. Please call 'setKeyStorePassword(pw)' first !");
    	
		if(server != null) {
			server.stop();
			server = null;
		}
		
		FtpServerFactory serverFactory = new FtpServerFactory();
		if(config != null) serverFactory.setConnectionConfig(config);
		
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(port);
		
		SslConfigurationFactory sslFactory = new SslConfigurationFactory();
		sslFactory.setKeystoreFile(fileKeystore);
		sslFactory.setKeystorePassword(keystorePassword);
		
		listenerFactory.setSslConfiguration(sslFactory.createSslConfiguration());
		listenerFactory.setImplicitSsl(true);
		
		serverFactory.addListener("default", listenerFactory.createListener());
		serverFactory.setUserManager(userManager);
		
		server = serverFactory.createServer();
	}
    
    public void setKeyStore(File f) {
    	this.fileKeystore = f;
    }
    
    public void setKeyStorePassword(String keystorePassword) {
    	this.keystorePassword = keystorePassword;
    }
}
