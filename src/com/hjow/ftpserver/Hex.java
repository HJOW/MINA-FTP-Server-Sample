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

/**
 * 바이너리와 HEX String 상호 변환용 클래스 
 * 
 * @author HJOW
 *
 */
public class Hex {
	/** HEX String 형식 문자열로부터 원본 바이너리 데이터 추출 */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/** 바이너리 데이터를 HEX String 형식 문자열로 변환 */
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X", b & 0xff));
		}
		return sb.toString();
	}
}
