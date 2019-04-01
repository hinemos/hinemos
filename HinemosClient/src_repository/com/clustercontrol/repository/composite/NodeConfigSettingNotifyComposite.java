/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.List;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.notify.composite.NotifyIdListComposite;
import com.clustercontrol.ws.notify.NotifyRelationInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;


/**
 * 構成情報取得設定用通知ID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「通知ID」 ラベル（親）</dd>
 *  <dd>「通知ID一覧」 フィールド（親）</dd>
 *  <dd>「選択」 ボタン（親）</dd>
 * </dl>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigSettingNotifyComposite extends NotifyIdListComposite {

	/** 入力値チェック用 */
	protected ValidateResult validateResult = null;

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite)
	 */
	public NodeConfigSettingNotifyComposite(Composite parent, int style) {
		super(parent, style, true);

		this.initialize(parent);
	}

	public NodeConfigSettingNotifyComposite(Composite parent, int style, int notifyIdType) {
		super(parent, style, true, notifyIdType);

		this.initialize(parent);
	}


	/**
	 * コンポジットを配置します。
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
	}


	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		this.update();
	}
	
	/**
	 * 引数で指定された構成情報収集設定に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info	構成情報収集設定
	 * @return	検証結果
	 */
	public ValidateResult createInputData(NodeConfigSettingInfo info){

		this.validateResult = null;
		if(info != null){
			if(getNotify() != null && getNotify().size() != 0){
				//コンポジットから通知情報を取得します。
				List<NotifyRelationInfo> notifyRelationInfoList = null;
				
				notifyRelationInfoList = info.getNotifyRelationList();
				
				notifyRelationInfoList.clear();
				if (this.getNotify() != null) {
					notifyRelationInfoList.addAll(this.getNotify());
				}
			}

		}
		return this.validateResult;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);

	}

}
