/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * マネージャ名コンポジットクラス<BR>
 *
 */
public class ManagerListComposite extends Composite {

	// ----- instance フィールド ----- //

	/** マネージャ名コンボボックス */
	private Combo comboManagerName = null;

	/** マネージャ名テキストボックス */
	private Text txtManagerName = null;

	/** 変更可能フラグ */
	private boolean m_enabledFlg = false;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param enabledFlg 変更可否フラグ（true:変更可能、false:変更不可）
	 */
	public ManagerListComposite(Composite parent, int style, boolean enabledFlg) {
		super(parent, style);
		this.m_enabledFlg = enabledFlg;
		this.initialize(parent);
	}


	// ----- instance メソッド ----- //

	/**
	 * コンポジットを生成・構築します。<BR>
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 1;
		this.setLayout(layout);

		/*
		 * ロールID
		 */
		if (this.m_enabledFlg) {
			// 変更可能な場合コンボボックス
			this.comboManagerName = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "managerName", comboManagerName);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboManagerName.setLayoutData(gridData);
		} else {
			// 変更不可な場合テキストボックス
			this.txtManagerName = new Text(this, SWT.BORDER | SWT.LEFT);
			WidgetTestUtil.setTestId(this, "managerName", txtManagerName);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.txtManagerName.setLayoutData(gridData);
			this.txtManagerName.setEnabled(false);
		}

		// 変更可能時のみコンボボックスにデータを設定する
		if (this.m_enabledFlg) {
			// マネージャ名リストの初期化
			this.comboManagerName.removeAll();
			for(String managerName : EndpointManager.getActiveManagerSet()) {
				this.comboManagerName.add(managerName);
			}
			this.update();
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
		this.comboManagerName.select(0);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (this.m_enabledFlg) {
			this.comboManagerName.setEnabled(enabled);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		if (this.m_enabledFlg) {
			return this.comboManagerName.getText();
		} else {
			return this.txtManagerName.getText();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		if (this.m_enabledFlg) {
			this.comboManagerName.setText(string);
		} else {
			this.txtManagerName.setText(string);
		}
	}

	public void addModifyListener(ModifyListener modifyListener){
		comboManagerName.addModifyListener(modifyListener);
	}

	public Combo getComboManagerName() {
		return comboManagerName;
	}

	public void add(String managerName) {
		this.comboManagerName.add(managerName);
		this.update();
	}

	public void delete(String managerName) {
		if (this.comboManagerName.indexOf(managerName) > -1) {
			this.comboManagerName.remove(managerName);
			this.update();
		}
	}

}
