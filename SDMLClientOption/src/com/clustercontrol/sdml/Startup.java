/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.sdml;

import org.eclipse.ui.IStartup;

import com.clustercontrol.sdml.util.Messages;

/**
 * ワークベンチ起動時に実行されるクラス<BR>
 * {@Activator}はパースペクティブ呼び出し時に起動するため、<BR>
 * それ以前の早期に実行したい処理を定義する<BR>
 */
public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		// クライアントオプションを追加
		SdmlClientOptionManager.getInstance().addOption(new SdmlCommonClientOption());
		SdmlClientOptionManager.getInstance().addOption(new SdmlV1ClientOption());

		// リソースバンドルの初期化処理
		Messages.init();
	}

}
