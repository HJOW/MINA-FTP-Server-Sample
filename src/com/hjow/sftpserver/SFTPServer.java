package com.hjow.sftpserver;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.User;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;

import com.hjow.ftpserver.LiteUserManager;

public class SFTPServer implements AutoCloseable {
    protected SshServer ssh;
    protected LiteUserManager userManager;
    
    public void prepare(int port, Path keyPath, CommandFactory commandFactory) {
    	if(ssh != null) {
    		try {  ssh.close();  } catch(Throwable t) { t.printStackTrace(); }
    	}
    	
    	if(userManager == null) {
    		userManager = new LiteUserManager();
    	}
    	
    	ssh = SshServer.setUpDefaultServer();
    	ssh.setPort(port);
    	if(keyPath != null) ssh.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath));
    	else                ssh.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
    	
    	if(commandFactory != null) ssh.setCommandFactory(new ScpCommandFactory.Builder().withDelegate(commandFactory).build());
    	else                       ssh.setCommandFactory(new ScpCommandFactory.Builder().build());
    	ssh.setPasswordAuthenticator(new PasswordAuthenticator() {
			@Override
			public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
				try {
					User user = userManager.authenticate(username, password);
					return (user != null);
				} catch (AuthenticationFailedException e) {
					System.out.println(e.getMessage());
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
