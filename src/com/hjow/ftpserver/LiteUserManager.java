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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;

public class LiteUserManager implements UserManager {
	protected List<LiteUser> users = new ArrayList<LiteUser>();
	
	public LiteUserManager() {
		
	}

	@Override
	public User authenticate(Authentication arg0) throws AuthenticationFailedException {
		if(arg0 instanceof UsernamePasswordAuthentication) {
			UsernamePasswordAuthentication authx = (UsernamePasswordAuthentication) arg0;
			try {
				User user = getUserByName(authx.getUsername());
				if(user == null) throw new AuthenticationFailedException("There is no user named " + authx.getUsername());
				
				// 비밀번호 비교해 맞으면 User 객체를 넘겨준다.
				if(user.getPassword().equals(authx.getPassword())) return user;
				
				// SHA-256 적용 이후 HEX String 적용하여 다시한번 비교한다.
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] enc = digest.digest(authx.getPassword().getBytes("UTF-8"));
				if(user.getPassword().equals( Hex.byteArrayToHexString(enc) )) return user;
				
				// TODO : Brute Force 기법 방어대책 강구 필요
				
				throw new AuthenticationFailedException("Wrong password for " + authx.getUsername());
			} catch (FtpException e) {
				throw new AuthenticationFailedException(e.getMessage(), e);
			} catch (NoSuchAlgorithmException e) {
				throw new AuthenticationFailedException(e.getMessage(), e);
			} catch (UnsupportedEncodingException e) {
				throw new AuthenticationFailedException(e.getMessage(), e);
			}
		} else {
			throw new AuthenticationFailedException("Unknown authentication method");
		}
	}

	@Override
	public void delete(String arg0) throws FtpException {
		int idx = 0;
		while(idx < users.size()) {
			if(users.get(idx).getName().equals(arg0)) {
				users.remove(idx);
			} else {
				idx++;
			}
		}
	}

	@Override
	public boolean doesExist(String arg0) throws FtpException {
		for(User u : users) {
			if(u.getName().equals(arg0)) return true;
		}
		return false;
	}

	@Override
	public String getAdminName() throws FtpException {
		for(LiteUser u : users) {
			if(u.isAdmin()) return u.getName();
		}
		return null;
	}

	@Override
	public String[] getAllUserNames() throws FtpException {
		String[] list = new String[users.size()];
		for(int idx=0; idx<list.length; idx++) {
			list[idx] = users.get(idx).getName();
		}
		return list;
	}

	@Override
	public User getUserByName(String arg0) throws FtpException {
		for(User u : users) {
			if(u.getName().equals(arg0)) return u;
		}
		return null;
	}

	@Override
	public boolean isAdmin(String arg0) throws FtpException {
		for(LiteUser u : users) {
			if(u.getName().equals(arg0)) return u.isAdmin();
		}
		return false;
	}

	@Override
	public void save(User arg0) throws FtpException {
		if(! (arg0 instanceof LiteUser)) throw new IllegalArgumentException("Not allowed user type"); 
		users.add((LiteUser) arg0);
	}
}