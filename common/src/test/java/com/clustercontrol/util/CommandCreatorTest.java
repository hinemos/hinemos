package com.clustercontrol.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.CommandCreator.PlatformType;


public class CommandCreatorTest {
	private static final String WIN_USER = System.getProperty("user.name");
	private static final String WIN_ADMIN = "Administrator";

	@Test public void testcreateWindowsCommand1() throws HinemosUnknown {
		System.setProperty("user.name", WIN_ADMIN);

		String[] cmd = CommandCreator.createCommand(WIN_ADMIN, "dir    C:\\dir1\\   ", PlatformType.WINDOWS);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\"}, cmd);
    }

	@Test public void testcreateWindowsCommand2() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_ADMIN, "dir    C:\\dir1\\   \"arg2\" ", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\""}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2"}, cmd);
    }

	@Test public void testcreateWindowsCommand3() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_ADMIN, "dir    C:\\dir1\\   \"arg2\"  \"arg 3\"  \"a r g 4\" ARG-5 'arg6'", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\"", "\"arg 3\"", "\"a r g 4\"", "ARG-5", "'arg6'"}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2", "arg 3", "a r g 4", "ARG-5", "arg6"}, cmd);
    }

	@Test public void testcreateWindowsCommand4() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_ADMIN, "dir    C:\\dir1\\   \"arg2\"  \"arg 3\"  \"a r g 4\" D:\\ARG-5 'arg 6B'", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\"", "\"arg 3\"", "\"a r g 4\"", "D:\\ARG-5", "'arg", "6B'"}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2", "arg 3", "a r g 4", "D:\\ARG-5", "arg 6B"}, cmd);
	}

	@Disabled("TODO Not yet supported now!")
	@Test public void testcreateWindowsCommand5() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir \"arg1-with-quote\\\"\"", PlatformType.WINDOWS);

		assertArrayEquals(new String[] {"dir", "\"arg1-with-quote\\\"\""}, cmd);
    }


	private static final String TEST_CMD = "hogehoge \"a b\" c , d";

	@Test public void testWindows1() throws HinemosUnknown {
		// Windows環境用
		System.setProperty("user.name", WIN_ADMIN);
		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.WINDOWS, true);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}
	@Test public void testWindows2() throws HinemosUnknown {
		assertThrows(HinemosUnknown.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.WINDOWS, true);
			}
		}, "execution user and jvm user must be same on Windows. (execUser = hinemos, sysUser = Administrator)");
	}

	@Disabled("System.setProperty does not work here")
	@Test public void testUNIX1() throws HinemosUnknown {
		// UNIX/Linux環境用
		System.setProperty("user.name", "root");

		String[] commandArr = CommandCreator.createCommand("root", TEST_CMD, PlatformType.UNIX, true);
		assertArrayEquals(new String[] {"sh", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testUNIX2() throws HinemosUnknown {
		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.UNIX, true);
		assertArrayEquals(new String[] {"sudo", "-u", "hinemos", "sh", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}

	@Disabled("System.setProperty does not work here")
	@Test public void testCompatibility1() throws HinemosUnknown {
		// 下位互換性環境用
		System.setProperty("user.name", "root");

		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.REGACY, true);
		assertArrayEquals(new String[] {"su", "hinemos", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testCompatibility2() throws HinemosUnknown {
		System.setProperty("user.name", WIN_ADMIN);

		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.REGACY, true);
		assertArrayEquals(new String[] {"su", "hinemos", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testCompatibility3() throws HinemosUnknown {
		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.REGACY, true);
		assertArrayEquals(new String[] {"su", "Administrator", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}

	@Disabled("System.setProperty os.name is not gonna work")
	@Test public void testAuto1() throws HinemosUnknown {
		// 環境自動識別用
		System.setProperty("user.name", WIN_ADMIN);
		System.setProperty("os.name", "Windows Server 2008");

		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.AUTO, true);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}
	@Disabled("System.setProperty os.name is not gonna work")
	@Test public void testAuto2() throws HinemosUnknown {
		System.setProperty("user.name", "root");
		System.setProperty("os.name", "Linux");
		
		assertThrows(HinemosUnknown.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				CommandCreator.createCommand("root", TEST_CMD, PlatformType.AUTO, true);
			}
		}, "execution user and jvm user must be same on Windows. (execUser = root, sysUser = Administrator)");
	}

	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserWindows1() throws HinemosUnknown {
		// Windows環境用
		System.setProperty("user.name", WIN_ADMIN);
		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.WINDOWS, false);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserWindows2() throws HinemosUnknown {
		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.WINDOWS, false);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}

	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserUNIX1() throws HinemosUnknown {
		// UNIX/Linux環境用
		System.setProperty("user.name", "root");

		String[] commandArr = CommandCreator.createCommand("root", TEST_CMD, PlatformType.UNIX, false);
		assertArrayEquals(new String[] {"sh", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserUNIX2() throws HinemosUnknown {
		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.UNIX, false);
		assertArrayEquals(new String[] {"sh", "-c", "hogehoge \"a b\" c , d"}, commandArr);
	}

	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserCompatibility1() throws HinemosUnknown {
		// 下位互換性環境用
		System.setProperty("user.name", "root");

		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.REGACY, false);
		assertArrayEquals(new String[] {"hogehoge", "\"a", "b\"", "c", ",", "d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserCompatibility2() throws HinemosUnknown {
		System.setProperty("user.name", WIN_ADMIN);

		String[] commandArr = CommandCreator.createCommand("hinemos", TEST_CMD, PlatformType.REGACY, false);
		assertArrayEquals(new String[] {"hogehoge", "\"a", "b\"", "c", ",", "d"}, commandArr);
	}
	@Disabled("System.setProperty does not work here")
	@Test public void testNoUserCompatibility3() throws HinemosUnknown {
		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.REGACY, false);
		assertArrayEquals(new String[] {"hogehoge", "\"a", "b\"", "c", ",", "d"}, commandArr);
	}

	@Disabled("System.setProperty os.name is not gonna work")
	@Test public void testNoUserAuto1() throws HinemosUnknown {
		// 環境自動識別用
		System.setProperty("user.name", WIN_ADMIN);
		System.setProperty("os.name", "Windows Server 2008");

		String[] commandArr = CommandCreator.createCommand(WIN_ADMIN, TEST_CMD, PlatformType.AUTO, false);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}
	@Disabled("System.setProperty os.name is not gonna work")
	@Test public void testNoUserAuto2() throws HinemosUnknown {
		System.setProperty("user.name", "root");
		System.setProperty("os.name", "Linux");
		
		String[] commandArr = CommandCreator.createCommand("root", TEST_CMD, PlatformType.AUTO, false);
		assertArrayEquals(new String[] {"hogehoge", "a b", "c", ",", "d"}, commandArr);
	}
}
