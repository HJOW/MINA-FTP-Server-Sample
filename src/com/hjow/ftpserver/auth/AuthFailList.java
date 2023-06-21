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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 인증 실패 횟수 관리도구
 * 
 * @author HJOW
 *
 */
public class AuthFailList {
	protected transient volatile List<AuthFail> tries = new ArrayList<AuthFail>();
	protected transient long expires = 60000L;
    public AuthFailList() {
    	
    }
    /** 인증 실패 발생 시 이 메소드를 호출해 주세요. 매개변수로 ID나 사용자명 등 사용자 고유값을 넣어 주세요. 반환되는 객체를 통해 현재 인증 실패 누적 횟수를 알 수 있습니다. */
    public synchronized AuthFail occur(String name) {
    	long now = System.currentTimeMillis();
    	
    	// 오래된 건 제거
    	cleanExpires();
    	
    	// 존재여부 확인
    	AuthFail tryOne = null;
    	for(AuthFail t : tries) {
    		if(t.getName().equals(name)) {
    			tryOne = t;
    			break;
    		}
    	}
    	
    	if(tryOne == null) {
    		tryOne = new AuthFail(new Date(now), name, 1);
    		tries.add(tryOne);
    	} else {
    		tryOne.increase();
    	}
    	
    	return tryOne;
    }
    
    /** ID나 사용자명 등 사용자 고유값으로 인증 실패 현황 조회 */
    public synchronized AuthFail get(String name) {
    	cleanExpires();
    	for(AuthFail t : tries) {
    		if(t.getName().equals(name)) {
    			return t;
    		}
    	}
    	return null;
    }
    
    /** 인증 실패 초기화 시간을 지정 (밀리초) */
    public void setExpirationTime(long expires) {
    	this.expires = expires;
    	cleanExpires();
    }
    
    /** 오래된 인증 실패 정보를 제거합니다. */
    public synchronized void cleanExpires() {
    	int i = 0;
    	long now = System.currentTimeMillis();
    	while(i < tries.size()) {
    		AuthFail tryOne = tries.get(i);
    		if(now - tryOne.getOccur().getTime() > expires) {
    			tries.remove(i);
    			continue;
    		}
    		i++;
    	}
    }
}
