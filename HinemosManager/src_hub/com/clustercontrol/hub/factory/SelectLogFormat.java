/*

Copyright (C) 2016 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.hub.factory;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.hub.model.LogFormat;
import com.clustercontrol.hub.util.QueryUtil;

/**
 * 監視情報を検索する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class SelectLogFormat {

	/** ログ出力のインスタンス。 */
//	private static Log m_log = LogFactory.getLog( SelectLogFormat.class );

	/**
	 * ログフォーマットID一覧を取得します。<BR>
	 * 
	 * @return ログフォーマットID一覧
	 */
	public List<String> getLogFormatIdList(String ownerRoleId) {
		List<String> list = new ArrayList<String>();
		List<LogFormat> ct = getLogFormatListByOwnerRole(ownerRoleId);
		for (LogFormat format : ct) {
			list.add(format.getLogFormatId());
		}
		return list;
	}
	
	/**
	 * ログフォーマット情報を取得します。
	 * @param formatId
	 * @return
	 * @throws Exception
	 */
	public LogFormat getLogFormat(String formatId) throws Exception{
		if (formatId == null || formatId.isEmpty())
			return null;
		return QueryUtil.getLogFormatPK(formatId);
	}

	/**
	 * オーナーロールIDを条件として
	 * ログフォーマット情報一覧を取得します。
	 * 
	 * @return ログフォーマット情報のリスト
	 */
	public List<LogFormat> getLogFormatListByOwnerRole(String ownerRoleId) {
		return QueryUtil.getLogFormatList_OR(ownerRoleId);
	}

	/**
	 * ログフォーマット情報一覧を取得します。
	 * 
	 * @return ログフォーマット情報のリスト
	 */
	public List<LogFormat> getLogFormatList() {
		return QueryUtil.getLogFormatList();
	}
}