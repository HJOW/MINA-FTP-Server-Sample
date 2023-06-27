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

import com.hjow.ftpserver.auth.AuthFailList;
import com.hjow.ftpserver.auth.AuthFail;

public class LiteUserManager implements UserManager {
	protected List<LiteUser> users = new ArrayList<LiteUser>();
	protected AuthFailList fails = new AuthFailList();
	
	protected transient int blockStrdCount = 5;
	
	public LiteUserManager() {
		
	}
	
	public User authenticate(String username, String password)  throws AuthenticationFailedException {
		try {
			User user = getUserByName(username);
			if(user == null) throw new AuthenticationFailedException("There is no user named " + username);
			
			// 인증실패 누적현황 조회
			AuthFail tryOne = fails.get(username);
			if(tryOne != null) {
				if(tryOne.getCount() >= blockStrdCount) throw new AuthenticationFailedException("Authentication fail count over.");
			}
			
			// 비밀번호 비교해 맞으면 User 객체를 넘겨준다.
			if(user.getPassword().equals(password)) return user;
			
			// SHA-256 적용 이후 HEX String 적용하여 다시한번 비교한다.
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] enc = digest.digest(password.getBytes("UTF-8"));
			if(user.getPassword().equals( Hex.byteArrayToHexString(enc) )) return user;
			
			// 인증 실패가 확실
			fails.occur(username);
			throw new AuthenticationFailedException("Wrong password for " + username);
		} catch (FtpException e) {
			throw new AuthenticationFailedException(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			throw new AuthenticationFailedException(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			throw new AuthenticationFailedException(e.getMessage(), e);
		}
	}
	
	@Override
	public User authenticate(Authentication arg0) throws AuthenticationFailedException {
		if(arg0 instanceof UsernamePasswordAuthentication) {
			UsernamePasswordAuthentication authx = (UsernamePasswordAuthentication) arg0;
			return authenticate(authx.getUsername(), authx.getPassword());
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
	
	/** 현재 설정된 계정 잠금 기준값을 입력합니다. 기록 만료 시간 이내에 이 횟수 이상 인증 실패 시 계정이 잠기게 됩니다. */
	public void setBlockStandardCount(int count) {
		this.blockStrdCount = count;
	}
	
	/** 현재 설정된 계정 잠금 기준값을 반환합니다. 기록 만료 시간 이내에 이 횟수 이상 인증 실패 시 계정이 잠기게 됩니다. */
	public int getBlockStandardCount() {
		return blockStrdCount;
	}
	
	/** 인증 실패 기록의 만료 시간을 밀리초 단위로 지정합니다. 잦은 인증 실패로 로그인이 잠긴 계정이 해제되는 데 걸리는 시간을 의미합니다. */
	public void setFailExpirationTime(long milliseconds) {
		fails.setExpirationTime(milliseconds);
	}
}