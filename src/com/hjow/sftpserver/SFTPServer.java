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
package com.hjow.sftpserver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.User;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hjow.ftpserver.FTPServer;
import com.hjow.ftpserver.LiteUserManager;

/**
 * <pre>
 * SFTP server implementation.
 * 
 * necessary jar
 *     json_simple-1.1.jar    
 *     mina-core-2.0.19.jar
 *     netty-all-4.0.56.Final.jar
 *     slf4j-api-2.0.7.jar
 *     slf4j-simple-2.0.7.jar
 *     sshd-common-2.10.0.jar
 *     sshd-core-2.10.0.jar
 *     sshd-netty-2.10.0.jar
 *     sshd-sftp-2.10.0.jar
 * </pre>
 * 
 * @author HJOW
 *
 */
public class SFTPServer implements AutoCloseable {
	public static final Logger LOGGER = LoggerFactory.getLogger(FTPServer.class);
	
    protected SshServer ssh;
    protected LiteUserManager userManager;
    
    public void prepare(int port, Path home) {
    	prepare(port, home, null);
    }
    
    public void prepare(int port, Path home, Path keyPath) {
    	prepare(port, home, keyPath);
    }
    
    public void prepare(int port, Path home, Path keyPath, CommandFactory commandFactory) {
    	if(ssh != null) {
    		try {  ssh.close();  } catch(Throwable t) { t.printStackTrace(); }
    		ssh = null;
    	}
    	
    	if(userManager == null) {
    		userManager = new LiteUserManager();
    	}
    	
    	ssh = SshServer.setUpDefaultServer();
    	ssh.setPort(port);
    	if(keyPath != null) ssh.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath));
    	else                ssh.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    	
    	ssh.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
    	if(commandFactory != null) ssh.setCommandFactory(commandFactory);
    	
    	if(home != null) {
    		VirtualFileSystemFactory vFileSystemFactory = new VirtualFileSystemFactory(home);
        	ssh.setFileSystemFactory(vFileSystemFactory);
    	}
    	
    	ssh.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
				try {
					User user = userManager.authenticate(username, password);
					return (user != null);
				} catch (AuthenticationFailedException e) {
					LOGGER.warn("Authentication failed at " + this.getClass().getSimpleName() + ", username is " + username + " - " + e.getMessage());
					return false;
				}
			}
		});
    }
    
    public LiteUserManager getUserManager() {
    	return userManager;
    }
    
    public void start() throws IOException {
    	ssh.start();
    }

	@Override
	public void close() throws Exception {
		if(ssh != null) ssh.close();
	}
}
