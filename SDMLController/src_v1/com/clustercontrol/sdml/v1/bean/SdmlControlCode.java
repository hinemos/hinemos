/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml.v1.bean;

public class SdmlControlCode {
	private MainCode mainCode = null;
	private SubCode subCode = null;
	private static final String delimiter = "_";

	/**
	 * メインコード
	 */
	public enum MainCode {
		Initialize("Initialize"),
		Start("Start"),
		Stop("Stop"),
		Error("Error"),
		Warning("Warning"),
		Info("Info");

		private final String code;

		private MainCode(final String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	/**
	 * サブコード
	 */
	public enum SubCode {
		Begin("Begin"),
		Set("Set"),
		End("End");

		private final String code;

		private SubCode(final String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}
	}

	/**
	 * コンストラクタ
	 * @param code
	 */
	public SdmlControlCode(String code) {
		if (code == null) {
			return;
		}
		if (code.contains(delimiter)) {
			// 区切り文字を含む場合は分割
			String[] codes = code.split(delimiter, 2);
			MainCode mainCode = getMainCode(codes[0]);
			SubCode subCode = getSubCode(codes[1]);
			if (mainCode != null && subCode != null) {
				// どちらかがnullの場合はどちらもnullとする
				this.mainCode = mainCode;
				this.subCode = subCode;
			}
		} else {
			this.mainCode = getMainCode(code);
		}
	}

	public MainCode getMainCode() {
		return this.mainCode;
	}

	public SubCode getSubCode() {
		return this.subCode;
	}

	public String getFullCode() {
		if (this.mainCode == null) {
			return "";
		} else if (this.subCode == null) {
			return this.mainCode.getCode();
		} else {
			return this.mainCode.getCode() + delimiter + this.subCode.getCode();
		}
	}

	/**
	 * 文字列からEnumに変換
	 * @param code
	 * @return
	 */
	private MainCode getMainCode(final String code) {
		MainCode[] values = MainCode.values();
		for (MainCode value : values) {
			if (value.getCode().equals(code)) {
				return value;
			}
		}
		return null;
	}

	/**
	 * 文字列からEnumに変換
	 * @param code
	 * @return
	 */
	private SubCode getSubCode(final String code) {
		SubCode[] values = SubCode.values();
		for (SubCode value : values) {
			if (value.getCode().equals(code)) {
				return value;
			}
		}
		return null;
	}
}
