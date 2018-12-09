package com.clustercontrol.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;


public class StringBinderTest {

	@Test public void test2() {
		Map<String, String> param2 = new HashMap<String, String>();
		param2.put("FOO", "foo");
		param2.put("BAR", "$[bar]");
		param2.put("HOGE:uga", "hoge");
		param2.put("HOGE", "zzz");
		param2.put("UGA", "uga");

		StringBinder binder2 = new StringBinder(param2);

		String input = "#[HOGE]";
		assertEquals("zzz", binder2.replace(input));

		input = "#[HOGE:#[UGA]]";
		assertEquals("hoge", binder2.replace(input));

		input = "#[FOO] AAA#[HOGE:#[UGA]]BBB #[BAR]";
		assertEquals("foo AAAhogeBBB $[bar]", binder2.replace(input));

		input = "#[FOO] AAA#[HOGE:#[]]BBB #[BAR]";
		assertEquals("foo AAA#[HOGE:#[]]BBB $[bar]", binder2.replace(input));

		input = "#[FOO] AAA#[HOGE:#[UGA]BBB #[BAR]";
		assertEquals("foo AAA#[HOGE:ugaBBB $[bar]", binder2.replace(input));

		input = "#[FOO] AAA#[HOGE:#[AHE]]BBB #[BAR]";
		assertEquals("foo AAA#[HOGE:#[AHE]]BBB $[bar]", binder2.replace(input));

		input = "#[FOO] AAA#[HOGE:#[UGA]]BBB #[AHE]";
		assertEquals("foo AAAhogeBBB #[AHE]", binder2.replace(input));
	}

	@Test public void test4() {
		String str = "foo #[PARAM] bar #[ESCAPE] #[NOTFOUND] foo bar";
		Map<String, String> param4 = new HashMap<String, String>();
		param4.put("PARAM", "foofoo");
		byte[] byteCode2 = { 0x10 };

		param4.put("ESCAPE", "foo 'bar' \"foo\" `echo aaa` \\ bar $ bar" +
				" [" + new String(byteCode2) + "], [" + new String(byteCode2) + "]");

		StringBinder binder4 = new StringBinder(param4);
		assertEquals("foo foofoo bar foo \\'bar\\' \\\"foo\\\" \\`echo aaa\\` \\\\ bar $ bar [\\x10], [\\x10] #[NOTFOUND] foo bar", binder4.bindParam(str));
	}
		
	@Test public void test3() {
		String str = "echo \"message:#[MESSAGE]; original message:#[ORIGINAL_MESSAGE]\"";
		StringBinder.setReplace(false);
		Map<String, String> param = new HashMap<String, String>();
		param = new HashMap<String, String>();
		param.put("MESSAGE", "This's message");
		param.put("ORIGINAL_MESSAGE", "This is \"message\".(\r\n) (\n) (`) ($) \\ \n(original)");

		StringBinder binder3 = new StringBinder(param);

		assertEquals("echo \"message:This\\'s message; original message:This is \\\"message\\\".(\\xD\\xA) (\\xA) (\\`) ($) \\\\ \\xA(original)\"", binder3.replace(str));
		assertEquals("echo \"message:This\\'s message; original message:This is \\\"message\\\".(\\xD\\xA) (\\xA) (\\`) ($) \\\\ \\xA(original)\"", binder3.bindParam(str));
	}

	@Test public void test1() {
		String str = "foo #[PARAM] bar #[ESCAPE] #[NOTFOUND] foo bar";

		Map<String, String> param = new HashMap<String, String>();
		param.put("PARAM", "foofoo");
		byte[] byteCode = { 0x10 };
		param.put("ESCAPE", "foo 'bar' \"foo\" `echo aaa` \\ bar $ bar" +
				" [" + new String(byteCode) + "], [" + new String(byteCode) + "]");

		StringBinder binder = new StringBinder(param);
		assertEquals("foo foofoo bar foo \\'bar\\' \\\"foo\\\" \\`echo aaa\\` \\\\ bar $ bar [\\x10], [\\x10] #[NOTFOUND] foo bar", binder.bindParam(str));

		StringBinder.setReplace(true);
		assertEquals("foo foofoo bar foo \\'bar\\' \\\"foo\\\" \\`echo aaa\\` \\\\ bar $ bar [?], [?] #[NOTFOUND] foo bar", binder.bindParam(str));

		StringBinder.setReplaceChar("?");
		StringBinder.setReplace(true);
		assertEquals("foo foofoo bar foo \\'bar\\' \\\"foo\\\" \\`echo aaa\\` \\\\ bar $ bar [?], [?] #[NOTFOUND] foo bar", binder.bindParam(str));
    }
}
