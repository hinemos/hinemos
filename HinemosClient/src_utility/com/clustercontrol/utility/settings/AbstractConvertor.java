/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.settings.job.conv.KickConv;
import com.clustercontrol.utility.settings.model.BaseConv;

/**
 * Convertorのスーパークラス
 * @version 6.0.0
 * @since 6.0.0
 */
public abstract class AbstractConvertor {

	/**ロガー*/
	protected static Log log = LogFactory.getLog(KickConv.class);
	/** スキーマタイプ */
	protected String schemaType = "A";
	/** スキーマバージョン */
	protected String schemaVersion = "0";
	/** スキーマレビジョン */
	protected String schemaRevision = "0";

	/**
	 * XMLとツールの対応バージョンをチェック
	 * @param type XMLのスキーマタイプ
	 * @param version XMLのバージョン
	 * @param revision XMLのリビジョン
	 * @return 続行可能ならばtrueを返します.
	 */
	public int checkSchemaVersion(String type, String version ,String revision){
		return BaseConv.checkSchemaVersion(
				schemaType,schemaVersion,schemaRevision,
				type,version,revision);
	}
}
