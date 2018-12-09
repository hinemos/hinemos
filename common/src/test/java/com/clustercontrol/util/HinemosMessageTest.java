package com.clustercontrol.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.clustercontrol.fault.HinemosUnknown;


public class HinemosMessageTest {

	@Test public void testPattern() throws HinemosUnknown {
		String test = "[2016/11/18 10:12:33] stdout=$[MESSAGE_JOB_MONITOR_ORGMSG_JMX:\"$[JMX_DB_CONNECTION_COUNT]\":\"19000\":\"null\":\"$[CONVERT_NO]\"]";
		Pattern pattern = Pattern.compile("\\$\\[([0-9a-zA-Z_]*?)\\]", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(test);

		assertTrue(matcher.find());

//		int okCounter = 0;
//		int ngCounter = 0;
//		for (MessageConstant messageConstant : MessageConstant.values()) {
//			String id = messageConstant.getMessage("aaa", "bbb", "ccc");
//			String message = replace(id);
//			if (id.equals(message)) {
//				System.out.println("error. message doesn't change. " + message);
//				ngCounter ++;
//			} else {
//				System.out.println("id=" + id + ", message=" + message);
//				okCounter ++;
//			}
//		}
//
//		// OKの数がHinemosManager/resource/manager_messages.propertiesのメッセージ数と等しいことを確認する。
//		System.out.println("OK=" + okCounter + ", NG=" + ngCounter);
//		System.out.println("------------");
//
//		// message.repository.1=ノード 「{0}」(ID\={1}) を削除します。よろしいですか？
//		String str = "hoge$[message.repository.1:\"$[edit]\":\"$[message.repository.1:aaa:bbb]\"]\nhoge";
//		System.out.println("str1=" + str);
//		System.out.println("str2=" + replace(str));
//		
//		// str2=hogeノード 「編集」(ID=ノード 「aaa」(ID=bbb) を削除します。よろしいですか？) を削除します。よろしいですか？\nhoge
    }
}
