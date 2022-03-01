/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.filtersetting.bean;

/**
 * イベント履歴のフィルタ条件項目種別。
 */
public enum EventFilterItemType implements FilterItemType {
	PRIORITY_CRITICAL(1),
	PRIORITY_WARNING(2),
	PRIORITY_INFO(3),
	PRIORITY_UNKNOWN(4),
	GENERATION_DATE_FROM(5),
	GENERATION_DATE_TO(6),
	OUTPUT_DATE_FROM(7),
	OUTPUT_DATE_TO(8),
	MONITOR_ID(9),
	MONITOR_DETAIL(10),
	APPLICATION(11),
	MESSAGE(12),
	CONFIRM_FLG_NOT_YET(13),
	CONFIRM_FLG_DOING(14),
	CONFIRM_FLG_DONE(15),
	CONFIRM_USER(16),
	COMMENT(17),
	COMMENT_USER(18),
	COLLECT_GRAPH_FLG(19),
	OWNER_ROLE_ID(20),
	USER_ITEM_01(21),
	USER_ITEM_02(22),
	USER_ITEM_03(23),
	USER_ITEM_04(24),
	USER_ITEM_05(25),
	USER_ITEM_06(26),
	USER_ITEM_07(27),
	USER_ITEM_08(28),
	USER_ITEM_09(29),
	USER_ITEM_10(30),
	USER_ITEM_11(31),
	USER_ITEM_12(32),
	USER_ITEM_13(33),
	USER_ITEM_14(34),
	USER_ITEM_15(35),
	USER_ITEM_16(36),
	USER_ITEM_17(37),
	USER_ITEM_18(38),
	USER_ITEM_19(39),
	USER_ITEM_20(40),
	USER_ITEM_21(41),
	USER_ITEM_22(42),
	USER_ITEM_23(43),
	USER_ITEM_24(44),
	USER_ITEM_25(45),
	USER_ITEM_26(46),
	USER_ITEM_27(47),
	USER_ITEM_28(48),
	USER_ITEM_29(49),
	USER_ITEM_30(50),
	USER_ITEM_31(51),
	USER_ITEM_32(52),
	USER_ITEM_33(53),
	USER_ITEM_34(54),
	USER_ITEM_35(55),
	USER_ITEM_36(56),
	USER_ITEM_37(57),
	USER_ITEM_38(58),
	USER_ITEM_39(59),
	USER_ITEM_40(60),
	POSITION_FROM(61),
	POSITION_TO(62),
	NOTIFY_UUID(63);

	/** ユーザ定義項目の総数 */
	public static final int MAX_USER_ITEM_NUMBER = 40;

	private final Integer code;

	private EventFilterItemType(int code) {
		this.code = Integer.valueOf(code);
	}

	public static EventFilterItemType fromCode(Integer code) {
		for (EventFilterItemType it : EventFilterItemType.values()) {
			if (it.code.equals(code)) return it;
		}
		throw new IllegalArgumentException("Unknown code=" + code);
	}

	@Override
	public Integer getCode() {
		return code;
	}

}
