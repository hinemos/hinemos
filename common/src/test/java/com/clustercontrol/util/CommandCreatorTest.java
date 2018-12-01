package com.clustercontrol.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.CommandCreator.PlatformType;


public class CommandCreatorTest {
	private static final String WIN_USER = System.getProperty("user.name");

	@Test public void testcreateWindowsCommand1() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir    C:\\dir1\\   ", PlatformType.WINDOWS);

		assertArrayEquals(new String[] {"dir", "C:\\dir1\\"}, cmd);
    }

	@Test public void testcreateWindowsCommand2() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir    C:\\dir1\\   \"arg2\" ", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\""}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2"}, cmd);
    }

	@Test public void testcreateWindowsCommand3() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir    C:\\dir1\\   \"arg2\"  \"arg 3\"  \"a r g 4\" ARG-5 'arg6'", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\"", "\"arg 3\"", "\"a r g 4\"", "ARG-5", "'arg6'"}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2", "arg 3", "a r g 4", "ARG-5", "arg6"}, cmd);
    }

	@Test public void testcreateWindowsCommand4() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir    C:\\dir1\\   \"arg2\"  \"arg 3\"  \"a r g 4\" D:\\ARG-5 'arg 6B'", PlatformType.WINDOWS);

		//assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "\"arg2\"", "\"arg 3\"", "\"a r g 4\"", "D:\\ARG-5", "'arg", "6B'"}, cmd);
		assertArrayEquals(new String[] {"dir", "C:\\dir1\\", "arg2", "arg 3", "a r g 4", "D:\\ARG-5", "arg 6B"}, cmd);
    }

	@Disabled("TODO Not yet supported now!")
	@Test public void testcreateWindowsCommand5() throws HinemosUnknown {
		String[] cmd = CommandCreator.createCommand(WIN_USER, "dir \"arg1-with-quote\\\"\"", PlatformType.WINDOWS);

		assertArrayEquals(new String[] {"dir", "\"arg1-with-quote\\\"\""}, cmd);
    }
}
