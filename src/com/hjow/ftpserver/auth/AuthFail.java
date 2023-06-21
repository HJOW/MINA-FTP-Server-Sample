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
package com.hjow.ftpserver.auth;

import java.io.Serializable;
import java.util.Date;

/**
 * 인증 실패 정보 VO
 * 
 * @author HJOW
 *
 */
public class AuthFail implements Serializable {
	private static final long serialVersionUID = 5416474849591563303L;
	protected Date   occur;
    protected String name;
    protected int    count;
    public AuthFail() {
    	this.occur = new Date(System.currentTimeMillis());
    }
    public AuthFail(String name, int count) {
		this();
		this.name  = name;
		this.count = count;
	}
	public AuthFail(Date occur, String name, int count) {
		this();
		this.occur = occur;
		this.name  = name;
		this.count = count;
	}
	public Date getOccur() {
		return occur;
	}
	public String getName() {
		return name;
	}
	public int getCount() {
		return count;
	}
	public void setOccur(Date occur) {
		this.occur = occur;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int increase() {
		occur = new Date(System.currentTimeMillis());
		count++;
		return count;
	}
}
