/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.util;


import com.clustercontrol.ws.monitor.EventDataInfo;
import com.clustercontrol.ws.monitor.EventFilterInfo;

/**
 * EventUtil
 * 
 */
public class EventUtil {

	public static void setUserItemValue(EventDataInfo info, int index, String value) {
		if (info == null) {
			return;
		}
		
		switch (index) {
		case 1 :
			info.setUserItem01(value);
			break;
			
		case 2 :
			info.setUserItem02(value);
			break;
			
		case 3 :
			info.setUserItem03(value);
			break;
			
		case 4 :
			info.setUserItem04(value);
			break;
			
		case 5 :
			info.setUserItem05(value);
			break;
			
		case 6 :
			info.setUserItem06(value);
			break;
			
		case 7 :
			info.setUserItem07(value);
			break;
			
		case 8 :
			info.setUserItem08(value);
			break;
			
		case 9 :
			info.setUserItem09(value);
			break;
			
		case 10 :
			info.setUserItem10(value);
			break;
			
		case 11 :
			info.setUserItem11(value);
			break;
			
		case 12 :
			info.setUserItem12(value);
			break;
			
		case 13 :
			info.setUserItem13(value);
			break;
			
		case 14 :
			info.setUserItem14(value);
			break;
			
		case 15 :
			info.setUserItem15(value);
			break;
			
		case 16 :
			info.setUserItem16(value);
			break;
			
		case 17 :
			info.setUserItem17(value);
			break;
			
		case 18 :
			info.setUserItem18(value);
			break;
			
		case 19 :
			info.setUserItem19(value);
			break;
			
		case 20 :
			info.setUserItem20(value);
			break;
			
		case 21 :
			info.setUserItem21(value);
			break;
			
		case 22 :
			info.setUserItem22(value);
			break;
			
		case 23 :
			info.setUserItem23(value);
			break;
			
		case 24 :
			info.setUserItem24(value);
			break;
			
		case 25 :
			info.setUserItem25(value);
			break;
			
		case 26 :
			info.setUserItem26(value);
			break;
			
		case 27 :
			info.setUserItem27(value);
			break;
			
		case 28 :
			info.setUserItem28(value);
			break;
			
		case 29 :
			info.setUserItem29(value);
			break;
			
		case 30 :
			info.setUserItem30(value);
			break;
			
		case 31 :
			info.setUserItem31(value);
			break;
			
		case 32 :
			info.setUserItem32(value);
			break;
			
		case 33 :
			info.setUserItem33(value);
			break;
			
		case 34 :
			info.setUserItem34(value);
			break;
			
		case 35 :
			info.setUserItem35(value);
			break;
			
		case 36 :
			info.setUserItem36(value);
			break;
			
		case 37 :
			info.setUserItem37(value);
			break;
			
		case 38 :
			info.setUserItem38(value);
			break;
			
		case 39 :
			info.setUserItem39(value);
			break;
			
		case 40 :
			info.setUserItem40(value);
			break;
			
		default :
			break;
		}
	}

	public static String getUserItemValue(EventDataInfo info, int index) {
		
		if (info == null) {
			return "";
		}
		
		String retVal = null;
		
		switch (index) {
		case 1 :
			retVal = info.getUserItem01();
			break;
			
		case 2 :
			retVal = info.getUserItem02();
			break;
			
		case 3 :
			retVal = info.getUserItem03();
			break;
			
		case 4 :
			retVal = info.getUserItem04();
			break;
			
		case 5 :
			retVal = info.getUserItem05();
			break;
			
		case 6 :
			retVal = info.getUserItem06();
			break;
			
		case 7 :
			retVal = info.getUserItem07();
			break;
			
		case 8 :
			retVal = info.getUserItem08();
			break;
			
		case 9 :
			retVal = info.getUserItem09();
			break;
			
		case 10 :
			retVal = info.getUserItem10();
			break;
			
		case 11 :
			retVal = info.getUserItem11();
			break;
			
		case 12 :
			retVal = info.getUserItem12();
			break;
			
		case 13 :
			retVal = info.getUserItem13();
			break;
			
		case 14 :
			retVal = info.getUserItem14();
			break;
			
		case 15 :
			retVal = info.getUserItem15();
			break;
			
		case 16 :
			retVal = info.getUserItem16();
			break;
			
		case 17 :
			retVal = info.getUserItem17();
			break;
			
		case 18 :
			retVal = info.getUserItem18();
			break;
			
		case 19 :
			retVal = info.getUserItem19();
			break;
			
		case 20 :
			retVal = info.getUserItem20();
			break;
			
		case 21 :
			retVal = info.getUserItem21();
			break;
			
		case 22 :
			retVal = info.getUserItem22();
			break;
			
		case 23 :
			retVal = info.getUserItem23();
			break;
			
		case 24 :
			retVal = info.getUserItem24();
			break;
			
		case 25 :
			retVal = info.getUserItem25();
			break;
			
		case 26 :
			retVal = info.getUserItem26();
			break;
			
		case 27 :
			retVal = info.getUserItem27();
			break;
			
		case 28 :
			retVal = info.getUserItem28();
			break;
			
		case 29 :
			retVal = info.getUserItem29();
			break;
			
		case 30 :
			retVal = info.getUserItem30();
			break;
			
		case 31 :
			retVal = info.getUserItem31();
			break;
			
		case 32 :
			retVal = info.getUserItem32();
			break;
			
		case 33 :
			retVal = info.getUserItem33();
			break;
			
		case 34 :
			retVal = info.getUserItem34();
			break;
			
		case 35 :
			retVal = info.getUserItem35();
			break;
			
		case 36 :
			retVal = info.getUserItem36();
			break;
			
		case 37 :
			retVal = info.getUserItem37();
			break;
			
		case 38 :
			retVal = info.getUserItem38();
			break;
			
		case 39 :
			retVal = info.getUserItem39();
			break;
			
		case 40 :
			retVal = info.getUserItem40();
			break;
		
		default :
			break;
		}
		
		if (retVal == null) {
			retVal = "";
		}
		return retVal;
	}
	
	public static void setUserItemValue(EventFilterInfo info, int index, String value) {
		if (info == null) {
			return;
		}
		
		switch (index) {
		case 1 :
			info.setUserItem01(value);
			break;
			
		case 2 :
			info.setUserItem02(value);
			break;
			
		case 3 :
			info.setUserItem03(value);
			break;
			
		case 4 :
			info.setUserItem04(value);
			break;
			
		case 5 :
			info.setUserItem05(value);
			break;
			
		case 6 :
			info.setUserItem06(value);
			break;
			
		case 7 :
			info.setUserItem07(value);
			break;
			
		case 8 :
			info.setUserItem08(value);
			break;
			
		case 9 :
			info.setUserItem09(value);
			break;
			
		case 10 :
			info.setUserItem10(value);
			break;
			
		case 11 :
			info.setUserItem11(value);
			break;
			
		case 12 :
			info.setUserItem12(value);
			break;
			
		case 13 :
			info.setUserItem13(value);
			break;
			
		case 14 :
			info.setUserItem14(value);
			break;
			
		case 15 :
			info.setUserItem15(value);
			break;
			
		case 16 :
			info.setUserItem16(value);
			break;
			
		case 17 :
			info.setUserItem17(value);
			break;
			
		case 18 :
			info.setUserItem18(value);
			break;
			
		case 19 :
			info.setUserItem19(value);
			break;
			
		case 20 :
			info.setUserItem20(value);
			break;
			
		case 21 :
			info.setUserItem21(value);
			break;
			
		case 22 :
			info.setUserItem22(value);
			break;
			
		case 23 :
			info.setUserItem23(value);
			break;
			
		case 24 :
			info.setUserItem24(value);
			break;
			
		case 25 :
			info.setUserItem25(value);
			break;
			
		case 26 :
			info.setUserItem26(value);
			break;
			
		case 27 :
			info.setUserItem27(value);
			break;
			
		case 28 :
			info.setUserItem28(value);
			break;
			
		case 29 :
			info.setUserItem29(value);
			break;
			
		case 30 :
			info.setUserItem30(value);
			break;
			
		case 31 :
			info.setUserItem31(value);
			break;
			
		case 32 :
			info.setUserItem32(value);
			break;
			
		case 33 :
			info.setUserItem33(value);
			break;
			
		case 34 :
			info.setUserItem34(value);
			break;
			
		case 35 :
			info.setUserItem35(value);
			break;
			
		case 36 :
			info.setUserItem36(value);
			break;
			
		case 37 :
			info.setUserItem37(value);
			break;
			
		case 38 :
			info.setUserItem38(value);
			break;
			
		case 39 :
			info.setUserItem39(value);
			break;
			
		case 40 :
			info.setUserItem40(value);
			break;
			
		default:
			break;
		}
	}
}