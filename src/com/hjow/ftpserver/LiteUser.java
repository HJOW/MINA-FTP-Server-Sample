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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;

public class LiteUser implements User {
	protected String name, password, homeDirectory;
	protected int    maxIdleTime;
	protected boolean enabled, admin;
	protected List<Authority> auths = new Vector<Authority>();
	
	public LiteUser() {
		enabled = true;
		admin   = false;
		maxIdleTime = 60;
	}
	
	public LiteUser(String name, String password, String homeDirectory) {
		this();
		setName(name);
		setPassword(password);
		setHomeDirectory(homeDirectory);
	}
	
	@Override
	public String getPassword() {
		return password;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public int getMaxIdleTime() {
		return maxIdleTime;
	}
	
	@Override
	public String getHomeDirectory() {
		return new File(homeDirectory).getAbsolutePath();
	}
	
	@Override
	public boolean getEnabled() {
		return enabled;
	}
	
	@Override
	public List<? extends Authority> getAuthorities(Class<? extends Authority> clazz) {
		List<Authority> lists = new ArrayList<Authority>();
		for(Authority a : getAuthorities()) {
			if(a.getClass().equals(clazz)) lists.add(a);
		}
		return lists;
	}
	
	@Override
	public List<? extends Authority> getAuthorities() {
		return auths;
	}
	
	@Override
	public AuthorizationRequest authorize(AuthorizationRequest request) {
		return request;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setAuths(List<Authority> auths) {
		this.auths = auths;
	}
	
	public void addAuth(Authority a) {
		this.auths.add(a);
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}