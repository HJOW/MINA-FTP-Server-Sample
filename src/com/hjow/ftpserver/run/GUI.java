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
package com.hjow.ftpserver.run;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.hjow.ftpserver.FTPServer;
import com.hjow.ftpserver.LiteUser;
import com.hjow.ftpserver.LiteUserManager;
import com.hjow.ftpserver.run.gui.StreamCapturer;
import com.hjow.ftpserver.run.gui.StreamConsumer;

public class GUI {
	static org.slf4j.Logger LOGGER = null;
	
	protected Properties configs = new Properties();
	protected FTPServer server;
	
	protected JFrame frame;
	protected JTabbedPane tab;
	protected JTextField tfRoot;
	protected JSpinner spPort;
	protected JButton btnRun;
	
	protected volatile JTextArea taLog;
	
	protected PrintStream psStrd, psOld;
	
	public GUI() {
		prepareConfig();
		beforeUIInit();
		
		frame = new JFrame();
		frame.setSize(600, 500);
		frame.setLayout(new BorderLayout());
		frame.setTitle("FTP Server");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				prepareExit();
				System.exit(0);
			}
		});
		
		JPanel pnMain = new JPanel();
		pnMain.setLayout(new BorderLayout());
		frame.add(pnMain, BorderLayout.CENTER);
		
		JPanel pnUp, pnCenter;
		
		pnUp = new JPanel();
		pnUp.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		pnCenter = new JPanel();
		pnCenter.setLayout(new BorderLayout());
		
		pnMain.add(pnUp    , BorderLayout.NORTH);
		pnMain.add(pnCenter, BorderLayout.CENTER);
		
		tab = new JTabbedPane();
		pnCenter.add(tab, BorderLayout.CENTER);
		
		JPanel pnTab;
		
		pnTab = new JPanel();
		pnTab.setLayout(new BorderLayout());
		
		taLog = new JTextArea();
		taLog.setEditable(false);
		
		pnTab.add(new JScrollPane(taLog), BorderLayout.CENTER);
		tab.add("LOG", pnTab);
		
		JLabel lb;
		
		lb = new JLabel("Port");
		spPort = new JSpinner(new SpinnerNumberModel(21, 20, 65500, 1));
		
		pnUp.add(lb);
		pnUp.add(spPort);
		
		lb = new JLabel("Directory");
		tfRoot = new JTextField(20);
		
		pnUp.add(lb);
		pnUp.add(tfRoot);
		
		btnRun = new JButton("▶");
		btnRun.setEnabled(false);
		btnRun.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(btnRun.getText().equals("▶")) {
					try {
						if(server != null) server.stop();
						configs.setProperty("port", String.valueOf(spPort.getValue()));
						configs.setProperty("root", tfRoot.getText());
						prepareServer();
						server.start();
						btnRun.setText("■");
					} catch (Throwable tx) {
						if(LOGGER == null) tx.printStackTrace();
						else LOGGER.error(tx.getMessage(), tx);
						if(frame != null && frame.isVisible()) JOptionPane.showMessageDialog(frame, "ERROR : " + tx.getMessage());
						else                                   JOptionPane.showMessageDialog(null , "ERROR : " + tx.getMessage());
						btnRun.setText("▶");
					}
				} else {
					try {
						server.stop();
						server = null;
						btnRun.setText("▶");
					} catch (Throwable tx) {
						if(LOGGER == null) tx.printStackTrace();
						else LOGGER.error(tx.getMessage(), tx);
						if(frame != null && frame.isVisible()) JOptionPane.showMessageDialog(frame, "ERROR : " + tx.getMessage());
						else                                   JOptionPane.showMessageDialog(null , "ERROR : " + tx.getMessage());
						btnRun.setText("■");
					}
				}
			}
		});
		pnUp.add(btnRun);
		
		psOld = System.out;
		psStrd = new PrintStream(new StreamCapturer(new StreamConsumer() {
			@Override
			public void appendText(String text) {
				taLog.append("\n" + text);
				taLog.setCaretPosition(taLog.getDocument().getLength() - 1);
			}
		}, psOld));
		System.setOut(psStrd);
		LOGGER = org.slf4j.LoggerFactory.getLogger(GUI.class);
		
		tfRoot.setText(configs.getProperty("root"));
		try { spPort.setValue(Integer.valueOf(configs.getProperty("port"))); } catch(Throwable t) { spPort.setValue(21); }
		btnRun.setEnabled(true);
	}
	
	protected void prepareConfig() {
		InputStream in1 = null;
		try {
			in1 = Console.class.getResourceAsStream("config.xml");
			if(in1 != null) {
				configs.loadFromXML(in1);
				in1.close();
				in1 = null;
			}
		} catch(Throwable t) {
			if(LOGGER == null) t.printStackTrace();
			else LOGGER.error(t.getMessage(), t);
			if(frame != null && frame.isVisible()) JOptionPane.showMessageDialog(frame, "ERROR : " + t.getMessage());
			else                                   JOptionPane.showMessageDialog(null , "ERROR : " + t.getMessage());
		} finally {
			try { in1.close(); } catch(Throwable ignores) {}
		}
	}
	
	protected void prepareServer() {
		try {
			String strRoot, strPort;
			strRoot = System.getProperty("user.home") + File.separator + "ftp";
			strPort = "21";
			
			if(configs.containsKey("root")) strRoot = configs.getProperty("root").trim();
			if(configs.containsKey("port")) strPort = configs.getProperty("port").trim();
			
			File roots = new File(strRoot);
			int  port  = Integer.parseInt(strPort.trim());
			
			if(! roots.exists()) roots.mkdirs();
			
			LiteUserManager userManager = new LiteUserManager();
			
			JSONParser parser = new JSONParser();
			JSONArray users = (JSONArray) parser.parse(configs.getProperty("user"));
			
			for(int idx=0; idx<users.size(); idx++) {
				JSONObject userOne = (JSONObject) users.get(idx);
				
				LiteUser one = new LiteUser();
				one.setName(String.valueOf(userOne.get("name")));
				one.setPassword(String.valueOf(userOne.get("password")));
				
				if(userOne.containsKey("enabled")) {
					String ena = userOne.get("enabled").toString().toLowerCase().trim();
					one.setEnabled((ena.equals("y") || ena.equals("yes") || ena.equals("t") || ena.equals("true"))); 
				} else {
					one.setEnabled(true);
				}
				
				if(userOne.containsKey("home")) {
					String strHome = userOne.get("home").toString().trim();
					if(strHome.contains("..")) throw new IllegalArgumentException("Cannot use .. in home directory !");
					one.setHomeDirectory(roots.getAbsolutePath() + strHome);
				} else {
					one.setHomeDirectory(roots.getAbsolutePath());
				}
				
				if(userOne.containsKey("maxIdleTime")) {
					String strMaxIdleTime = userOne.get("maxIdleTime").toString().trim();
					one.setMaxIdleTime(Integer.parseInt(strMaxIdleTime));
				} else {
					one.setMaxIdleTime(0);
				}
				
				if(userOne.containsKey("admin")) {
					String ena = userOne.get("admin").toString().toLowerCase().trim();
					one.setAdmin((ena.equals("y") || ena.equals("yes") || ena.equals("t") || ena.equals("true"))); 
				} else {
					one.setAdmin(true);
				}
				
				one.setAdmin(true);
				one.addAuth(new Authority() {
					@Override
					public boolean canAuthorize(AuthorizationRequest request) {
						if(LOGGER == null) System.out.println("In liteuser canAuthorize, " + request);
						else LOGGER.info("In liteuser canAuthorize, " + request);
						return true;
					}
					
					@Override
					public AuthorizationRequest authorize(AuthorizationRequest request) {
						if(LOGGER == null) System.out.println("In liteuser authorize, " + request);
						else LOGGER.info("In liteuser authorize, " + request);
						return request;
					}
				});
				userManager.save(one);
			}
			
			server = new FTPServer(port, userManager);
		} catch(Throwable t) {
			if(LOGGER == null) t.printStackTrace();
			else LOGGER.error(t.getMessage(), t);
			if(frame != null && frame.isVisible()) JOptionPane.showMessageDialog(frame, "ERROR : " + t.getMessage());
			else                                   JOptionPane.showMessageDialog(null , "ERROR : " + t.getMessage());
		}
	}
	
	protected void prepareExit() {
		if(server != null) {
			try { server.stop(); } catch(Throwable t) { if(LOGGER == null) t.printStackTrace(); else LOGGER.error(t.getMessage(), t); }
		}
		if(psStrd != null) {
			System.setOut(psOld);
			psOld = null;
			psStrd.close();
			psStrd = null;
		}
	}
	
	protected void beforeUIInit() {
		try {
			String lk = "Nimbus";
			if(configs.containsKey("theme")) lk = configs.getProperty("theme");
			for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if(lk.equals(info.getName()) || lk.equals(info.getClassName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch(Throwable t) {
			if(LOGGER == null) t.printStackTrace();
			else LOGGER.warn(t.getMessage(), t);
		}
	}
	
	public void start() {
		frame.setVisible(true);
	}
	
	public static void main(String[] args) {
		new GUI().start();
	}
}
