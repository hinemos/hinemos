/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.tasktray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class ServiceObserver {
	
	private static final long TASK_SLEEP = 1000;
	
	private boolean shutdown = false;
	
	private ExecutorService observer;
	
	// アイコン（実行）
	private static final String RUN_IMAGE_NAME = "images/icon_running.gif";
	private Image runImage = null;
	
	// アイコン（停止中）
	private static final String STOP_IMAGE_NAME = "images/icon_stop.gif";
	private Image stopImage = null;
	
	private static String runCommand;
	private static String stopCommand;
	private static String restartCommand;
	private static String observCommand;
	
	private static String observMode;
	
	private ResourceBundle bundle;

	public static void setRunCommand(String command) {
		runCommand = command;
	}
	public static void setStopCommand(String command) {
		stopCommand = command;
	}
	public static void setRestartCommand(String command) {
		restartCommand = command;
	}
	public static void setObservCommand(String command) {
		observCommand = command;
	}
	public static void setObservMode(String mode) {
		observMode = mode;
	}

	public ServiceObserver() {
		String homeDir = System.getProperty("homedir");
		
		setRunCommand("cmd /c \"" + homeDir + "\\bin\\ManagerStart.cmd\"");
		setStopCommand("cmd /c \"" + homeDir + "\\bin\\ManagerStop.cmd\"");
		setRestartCommand("cmd /c \"" + homeDir + "\\bin\\ManagerRestart.cmd\"");
		setObservCommand("cmd /c sc query Hinemos_Manager");
		
		Properties properties = new Properties();
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(homeDir + "\\etc\\hinemos_tasktray.properties");
			properties.load(stream);
			setObservMode(properties.getProperty("observe.mode"));
			if(observMode != null && "powershell".equals(observMode)) {
				setObservCommand("cmd /c powershell \"&\'" + homeDir + "\\sbin\\service_observer.ps1\'\"");
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		
		bundle = ResourceBundle.getBundle("messages_tasktray");
	}
	
	public void start() throws SocketException, UnknownHostException, IOException {
		
		if (!SystemTray.isSupported()) {
//			log.warn("SystemTray is not supported.");
			System.exit(1);
		}
		
		try {
			ClassLoader cl = ClassLoader.getSystemClassLoader();
			InputStream runIs = cl.getResourceAsStream(RUN_IMAGE_NAME);
			runImage = ImageIO.read(runIs);
			
			InputStream stopIs = cl.getResourceAsStream(STOP_IMAGE_NAME);
			stopImage = ImageIO.read(stopIs);
			
		} catch (IOException e) {
			e.printStackTrace();
//			log.error("load tasktray image failed.", e);
		}
		
		final TrayIcon icon = new TrayIcon(stopImage);
		icon.setImageAutoSize(true);
		
		// 左クリックでメニューを表示させるため、非表示のFrameを生成
		final JFrame frame = new JFrame("");
		frame.setUndecorated(true);
		frame.setType(Type.UTILITY);

		// メニュー生成
		final PopupMenu menu = new PopupMenu();
		
		final MenuItem menuStart = new MenuItem(bundle.getString("MENU_LABEL_START_SERVICE"));
		menuStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec (runCommand);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		menuStart.setEnabled(false);
		
		final MenuItem menuStop = new MenuItem(bundle.getString("MENU_LABEL_STOP_SERVICE"));
		menuStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec (stopCommand);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		menuStop.setEnabled(false);
		
		final MenuItem menuRestart = new MenuItem(bundle.getString("MENU_LABEL_RESTART_SERVICE"));
		menuRestart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Runtime.getRuntime().exec (restartCommand);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		menuRestart.setEnabled(false);
		
		menu.add(menuStart);
		menu.add(menuStop);
		menu.add(menuRestart);
		icon.setPopupMenu(menu);
		
		icon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					frame.add(menu);
					menu.show(frame, e.getXOnScreen(), e.getYOnScreen());
				}
			}
		});
		
		try {
			frame.setResizable(false);
			frame.setVisible(true);
			SystemTray.getSystemTray().add(icon);
			
		} catch (AWTException e) {
			System.out.println(e);
//			log.error("register tasktray failed.", e);
		}
		
		
		// 状態変化時のイベントハンドラ
		ObserverHandler handler = new ObserverHandler() {
			@Override
			public void onStartService() {
				icon.setToolTip(bundle.getString("TOOLTIP_RUNNNIG"));
				icon.setImage(runImage);
				menuStart.setEnabled(false);
				menuStop.setEnabled(true);
				menuRestart.setEnabled(true);
				icon.displayMessage(bundle.getString("DISPLAY_MESSAGE_CAPTION"), bundle.getString("DISPLAY_MESSAGE_TEXT_START"), TrayIcon.MessageType.INFO);
			}
			
			@Override
			public void onStopService() {
				icon.setToolTip(bundle.getString("TOOLTIP_STOP"));
				icon.setImage(stopImage);
				menuStart.setEnabled(true);
				menuStop.setEnabled(false);
				menuRestart.setEnabled(false);
				icon.displayMessage(bundle.getString("DISPLAY_MESSAGE_CAPTION"), bundle.getString("DISPLAY_MESSAGE_TEXT_STOP"), TrayIcon.MessageType.WARNING);
			}
		};
		
		// 監視開始
		observer = createExecutorService("Observer");
		observer.submit(new ObserverTask(handler));
	}
	
	private ExecutorService createExecutorService(final String name) {
		ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, name);
			}
		});
		return executor;
	}
	
	public void shutdown() {
		shutdown = true;
		observer.shutdown();
	}
	
	private class ObserverTask implements Runnable {
		private boolean firstCheck = true;
		private boolean prevRunning = false;
		private boolean running = false;
		private ObserverHandler handler;
		
		public ObserverTask(ObserverHandler handler) {
			this.handler = handler;
		}

		@Override
		public void run() {
			while (!shutdown && handler != null) {
				Process process = null;
				try {
					process = Runtime.getRuntime().exec(observCommand);
					
					if (process != null ) {
						
						StreamReader inStreamReader = new StreamReader(process.getInputStream());
						inStreamReader.start();
						
						process.waitFor();
						
						inStreamReader.join();
						List<String> result = inStreamReader.getResult();
						
						if(observMode != null && "powershell".equals(observMode)) {
							statusCheckPsResult(result);
						} else {
							statusCheckScResult(result);
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {

					if(process != null){
						process.destroy();
					}
				}
				
				try {
					Thread.sleep(TASK_SLEEP);
				} catch (InterruptedException e) {}
				
			}
		}
		
		private void statusCheckPsResult(List<String> result) {
			for (String line : result) {
				if (line != null && line.startsWith("State:")) {
					line = line.replace("State:", "");
					running = "Running".equals(line);
					
					if (firstCheck || (prevRunning != running)) {
						if (running) {
							handler.onStartService();
						} else {
							handler.onStopService();
						}
					}
					
					firstCheck = false;
					prevRunning = running;
				}
			}
		}
		
		private void statusCheckScResult(List<String> result) {
			for (String line : result) {
				line = line.replace(" ", "");
				if (line != null && line.startsWith("STATE:")) {
					line = line.replace("STATE:", "");
					running = "4RUNNING".equals(line);
					
					if (firstCheck || (prevRunning != running)) {
						if (running) {
							handler.onStartService();
						} else {
							handler.onStopService();
						}
					}
					
					firstCheck = false;
					prevRunning = running;
				}
			}
		}
	}
	
	public interface ObserverHandler {
		void onStartService();
		void onStopService();
	}
	
	static class StreamReader extends Thread {
		private BufferedReader br;
		private ArrayList<String> result;

		public StreamReader(InputStream is) {
			super();
			br = new BufferedReader(new InputStreamReader(is));
			result = new ArrayList<String>();
		}

		@Override
		public void run() {
			try {
				while(true){
					String line = br.readLine();
					if(line != null){
						result.add(line);
					} else{
						break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public ArrayList<String> getResult() {
			return result;
		}
	}
}
