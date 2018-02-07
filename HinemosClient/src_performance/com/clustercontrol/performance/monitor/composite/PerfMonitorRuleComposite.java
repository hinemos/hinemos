/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.monitor.composite;

import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.monitor.run.composite.MonitorRuleComposite;

/**
 * 監視条件を設定するコンポジットクラス
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class PerfMonitorRuleComposite extends MonitorRuleComposite {
	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。
	 * 
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 */
	public PerfMonitorRuleComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを生成・構築します。
	 */
	private void initialize() {
	}
}
