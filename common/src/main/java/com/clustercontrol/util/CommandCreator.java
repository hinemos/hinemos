/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import com.clustercontrol.fault.HinemosUnknown;

public class CommandCreator {

	private static Log log = LogFactory.getLog(CommandCreator.class);

	public static enum PlatformType { AUTO, WINDOWS, UNIX, REGACY, UNIX_SU};

	public static final String sysUser;

	public static final String osName;
	public static final PlatformType sysPlatform;

	static {
		sysUser = System.getProperty("user.name");
		log.info("java process user detected : " + sysUser);

		osName = System.getProperty("os.name");
		if (osName != null && (osName.startsWith("Windows") || osName.startsWith("windows"))) {
			sysPlatform = PlatformType.WINDOWS;
		} else {
			sysPlatform = PlatformType.UNIX;
		}
		log.info("os detected : " + osName);
		log.info("platform detected : " + sysPlatform);
	}

	/**
	 * convert platform string to type
	 * @param str platform string
	 * @return platform type
	 */
	public static PlatformType convertPlatform(String str) {
		// Local Variable
		PlatformType platform = null;

		// MAIN
		if ("windows".equals(str)) {
			platform = PlatformType.WINDOWS;
		} else if ("unix".equals(str)) {
			platform = PlatformType.UNIX;
		} else if ("unix.su".equals(str)) {
			platform = PlatformType.UNIX_SU;
		} else if ("compatible".equals(str) || "regacy".equals(str)) {
			platform = PlatformType.REGACY;
		} else {
			platform = PlatformType.AUTO;
		}
		if (log.isDebugEnabled())
			log.debug("string converted. (str = " + str + ", type = " + platform + ")");
		return platform;
	}

	public static String[] createCommand(String execUser, String execCommand, PlatformType platform) throws HinemosUnknown {
		return createCommand(execUser, execCommand, platform, true);
	}

	public static String[] createCommand(String execUser, String execCommand, PlatformType platform, boolean specifyUser) throws HinemosUnknown {
		return createCommand(execUser, execCommand, platform, specifyUser, false);
	}

	/**
	 * create command for platform
	 * @param execUser execution user
	 * @param execCommand command string
	 * @param platform target platform
	 * @return command
	 * @throws HinemosUnknown
	 */
	public static String[] createCommand(String execUser, String execCommand, PlatformType platform, boolean specifyUser, boolean loginFlag) throws HinemosUnknown {
		// Local Variables
		String[] command = null;

		// Main
		if (specifyUser && execUser == null) {
			throw new NullPointerException("execUser is not defined.");
		}
		if (execCommand == null) {
			throw new NullPointerException("execCommand is not defined.");
		}
		if (platform == null) {
			throw new NullPointerException("platform is not defined.");
		}

		String user;
		if (specifyUser) {
			// ジョブを実行するユーザを指定する場合はexecUserをそのまま使用する
			user = execUser;
		} else {
			// エージェントの実行ユーザで実行するの場合はsysuserに置き換える
			user = sysUser;
		}

		switch (platform) {
		case WINDOWS :
			command = createWindowsCommand(user, execCommand);
			break;
		case UNIX :
			command = createUnixCommand(user, execCommand, loginFlag);
			break;
		case UNIX_SU :
			command = createUnixSuCommand(user, execCommand, loginFlag);
			break;
		case REGACY :
			command = createRegacyCommand(user, execCommand, loginFlag);
			break;
		case AUTO :
		default :
			command = createCommand(user, execCommand, sysPlatform, specifyUser, loginFlag);
			break;
		}

		return command;
	}

	/**
	 * create command for Windows like CMD /C [COMMAND]
	 * @param execUser execution user
	 * @param execCommand command string parsed by CMD
	 * @return command and arguments
	 * @throws HinemosUnknown
	 */
	private static String[] createWindowsCommand(String execUser, String execCommand) throws HinemosUnknown {
		// Local Variables
		String[] command = null;
		boolean isDebugEnable = log.isDebugEnabled();
		
		// MAIN
		if (execUser.equals(sysUser)) {
			try {
				command = CommandLineUtils.translateCommandline(execCommand);
			} catch (Exception e) {
				log.warn(e);
				command = new String[]{};
			}
			if (isDebugEnable) {
				log.debug("created command for windows. (cmd = " + Arrays.toString(command) + ")");
			}
		} else {
			throw new HinemosUnknown("execution user and jvm user must be same on Windows. (execUser = " + execUser + ", sysUser = " + sysUser + ")");
		}
		return command;
	}

	/**
	 * create command for UNIX line (sudo -u [USER]) sh -c [COMMAND]
	 * @param execUser execution user
	 * @param execCommand command string parsed by sh
	 * @return command and arguments
	 * @throws HinemosUnknown
	 */
	private static String[] createUnixCommand(String execUser, String execCommand, boolean loginFlag) throws HinemosUnknown {
		// Local Variables
		String[] command = null;

		// MAIN
		if (execUser.equals(sysUser)) {
			command = new String[]{ "sh", "-c", execCommand };
			if (log.isDebugEnabled()) log.debug("created command for unix. (cmd = " + Arrays.toString(command));
		} else {
			if (loginFlag) {
				command = new String[]{ "sudo", "-u", execUser, "-i", "sh", "-c", execCommand };
			} else {
				command = new String[]{ "sudo", "-u", execUser, "sh", "-c", execCommand };
			}
			if (log.isDebugEnabled()) log.debug("created command for unix. (cmd = " + Arrays.toString(command));
		}
		return command;
	}

	/**
	 * create command (su [USER] -c)
	 * @param execUser execution user
	 * @param execCommand command string
	 * @return command and arguments
	 * @throws HinemosException
	 */
	private static String[] createUnixSuCommand(String execUser, String execCommand, boolean loginFlag) throws HinemosUnknown {
			String[] command = null;

			if (execUser.equals(sysUser)) {
				command = new String[]{ "sh", "-c", execCommand };
			} else {
				command = createSu(execUser, execCommand, loginFlag);
			}
			if (log.isDebugEnabled()) log.debug("created command for unix. (cmd = " + Arrays.toString(command));
			return command;
	}

	/**
	 * create regacy (before 3.1) command like (su [USER] -c) execCommand
	 * @param execUser execution user
	 * @param execCommand command string
	 * @return command and arguments
	 * @throws HinemosUnknown
	 */
	private static String[] createRegacyCommand(String execUser, String execCommand, boolean loginFlag) throws HinemosUnknown {
		// Local Variables
		String[] command = null;

		// MAIN
		if (execUser.equals(sysUser)) {
			// split by half space
			command = execCommand.split(" ");
		} else {
			command = createSu(execUser, execCommand, loginFlag);
			if (log.isDebugEnabled()) log.debug("created command for regacy. (cmd = " + Arrays.toString(command));
		}
		return command;
	}

	private static String[] createSu(String execUser, String execCommand, boolean loginFlag) throws HinemosUnknown {
		String [] command = null;
		if ("root".equals(sysUser)) {
			if (loginFlag) {
				command = new String[]{ "su", "-", execUser, "-c", execCommand };
			} else {
				command = new String[]{ "su", execUser, "-c", execCommand };
			}
		} else {
			throw new HinemosUnknown("jvm user must be root. jvm user is " + sysUser);
		}
		return command;
	}
	/**
	 * プロダクト実行ユーザを取得します。
	 * @return プロダクト実行ユーザ
	 */
	public static String getSysUser() {
		return sysUser;
	}


	public static void main(String[] args) {

		String command = "hogehoge \"a b\" c , d";
		String[] commandArr = null;


		System.out.println("SpecifyUser = YES");

		// Windows環境用
		System.setProperty("user.name", "Administrator");
		try {
			commandArr = createCommand("Administrator", command, PlatformType.WINDOWS, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("hinemos", command, PlatformType.WINDOWS, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		// UNIX/Linux環境用
		System.setProperty("user.name", "root");
		try {
			commandArr = createCommand("root", command, PlatformType.UNIX, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("hinemos", command, PlatformType.UNIX, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		// 下位互換性環境用
		System.setProperty("user.name", "root");
		try {
			commandArr = createCommand("hinemos", command, PlatformType.REGACY, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		System.setProperty("user.name", "Administrator");
		try {
			commandArr = createCommand("hinemos", command, PlatformType.REGACY, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("Administrator", command, PlatformType.REGACY, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		// 環境自動識別用
		System.setProperty("user.name", "Administrator");
		System.setProperty("os.name", "Windows Server 2008");
		try {
			commandArr = createCommand("Administrator", command, PlatformType.AUTO, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		System.setProperty("user.name", "root");
		System.setProperty("os.name", "Linux");
		try {
			commandArr = createCommand("root", command, PlatformType.AUTO, true);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		System.out.println("SpecifyUser = NO");

		// Windows環境用
		System.setProperty("user.name", "Administrator");
		try {
			commandArr = createCommand("Administrator", command, PlatformType.WINDOWS, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("hinemos", command, PlatformType.WINDOWS, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		// UNIX/Linux環境用
		System.setProperty("user.name", "root");
		try {
			commandArr = createCommand("root", command, PlatformType.UNIX, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("hinemos", command, PlatformType.UNIX, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		// 下位互換性環境用
		System.setProperty("user.name", "root");
		try {
			commandArr = createCommand("hinemos", command, PlatformType.REGACY, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		System.setProperty("user.name", "Administrator");
		try {
			commandArr = createCommand("hinemos", command, PlatformType.REGACY, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		try {
			commandArr = createCommand("Administrator", command, PlatformType.REGACY, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
		// 環境自動識別用
		System.setProperty("user.name", "Administrator");
		System.setProperty("os.name", "Windows Server 2008");
		try {
			commandArr = createCommand("Administrator", command, PlatformType.AUTO, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}

		System.setProperty("user.name", "root");
		System.setProperty("os.name", "Linux");
		try {
			commandArr = createCommand("root", command, PlatformType.AUTO, false);
			if (commandArr != null) for (String arg : commandArr) { System.out.println("arg : " + arg); }
			System.out.println();
		} catch (Exception e) {
			System.out.println("ERROR MSG * " + e.getMessage() + "\n");
		}
	}
}
