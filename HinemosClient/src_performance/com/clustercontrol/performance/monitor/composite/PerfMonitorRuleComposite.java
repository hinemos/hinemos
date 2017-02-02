/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

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
