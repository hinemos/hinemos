/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.jobmanagement.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.openapitools.client.model.JobRpaInfoResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.rpa.bean.RpaJobTypeConstant;
import com.clustercontrol.jobmanagement.util.JobDialogUtil;
import com.clustercontrol.util.Messages;

/**
 * RPAシナリオタブ用のコンポジットクラスです
 */
public class RpaComposite extends Composite {
	/** 直接実行ラジオボタン */
	private Button m_directExecButton = null;
	/** 間接実行ラジオボタン */
	private Button m_indirectExecButton = null;
	/** タブフォルダー */
	private TabFolder m_tabFolder = null;
	/** 直接実行タブ */
	private TabItem m_directTabItem = null;
	/** 間接実行タブ */
	private TabItem m_indirectTabItem = null;
	/** 直接実行用コンポジット */
	private RpaDirectExecutionComposite m_directExecutionComposite = null;
	/** 間接実行用コンポジット */
	private RpaIndirectExecutionComposite m_indirectExecutionComposite = null;
	/** マネージャ名 */
	private String m_managerName = null;

	private JobRpaInfoResponse m_rpa = null;

	public RpaComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		initialize();
	}

	private void initialize() {
		this.setLayout(JobDialogUtil.getParentLayout());

		// 直接実行ラジオボタン
		this.m_directExecButton = new Button(this, SWT.RADIO);
		this.m_directExecButton.setText(Messages.getString("rpa.direct.execution.detail"));
		this.m_directExecButton.setLayoutData(new RowData(400, SizeConstant.SIZE_BUTTON_HEIGHT));

		// 間接実行ラジオボタン
		this.m_indirectExecButton = new Button(this, SWT.RADIO);
		this.m_indirectExecButton.setText(Messages.getString("rpa.indirect.execution.detail"));
		this.m_indirectExecButton.setLayoutData(new RowData(400, SizeConstant.SIZE_BUTTON_HEIGHT));

		// タブフォルダー
		this.m_tabFolder = new TabFolder(this, SWT.NONE);

		// 直接実行タブ
		this.m_directTabItem = new TabItem(this.m_tabFolder, SWT.NONE);
		this.m_directTabItem.setText(Messages.getString("rpa.direct.execution"));
		this.m_directExecutionComposite = new RpaDirectExecutionComposite(m_tabFolder, SWT.NONE, m_managerName);
		this.m_directTabItem.setControl(m_directExecutionComposite);

		// 間接実行タブ
		this.m_indirectTabItem = new TabItem(m_tabFolder, SWT.NONE);
		this.m_indirectTabItem.setText(Messages.getString("rpa.indirect.execution"));
		this.m_indirectExecutionComposite = new RpaIndirectExecutionComposite(m_tabFolder, SWT.NONE, m_managerName);
		this.m_indirectTabItem.setControl(m_indirectExecutionComposite);

		// ラジオボタンとタブの連動
		this.m_directExecButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button radio = (Button) e.getSource();
				if (radio.getSelection()) {
					m_tabFolder.setSelection(m_directTabItem);
					setRpaJobType(RpaJobTypeConstant.DIRECT);
				}
			}
		});

		this.m_indirectExecButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button radio = (Button) e.getSource();
				if (radio.getSelection()) {
					m_tabFolder.setSelection(m_indirectTabItem);
					setRpaJobType(RpaJobTypeConstant.INDIRECT);
				}
			}
		});

		this.m_tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRadio();
			}
		});
	}

	/**
	 * 選択されたタブに合わせてラジオボタンを切り替えます。
	 */
	private void updateRadio() {
		// 読み取り専用の場合はラジオボタンの選択を連動させない
		if (m_directExecButton.getEnabled() && m_indirectExecButton.getEnabled()) {
			if (m_tabFolder.getSelectionIndex() == m_tabFolder.indexOf(m_directTabItem)) {
				// 直接実行タブがアクティブ
				m_directExecButton.setSelection(true);
				m_indirectExecButton.setSelection(false);
				setRpaJobType(RpaJobTypeConstant.DIRECT);
			} else {
				// 間接実行タブがアクティブ
				m_directExecButton.setSelection(false);
				m_indirectExecButton.setSelection(true);
				setRpaJobType(RpaJobTypeConstant.INDIRECT);
			}
		}
	}

	public void reflectRpaJobInfo() {
		// 初期値
		m_directExecButton.setSelection(true);
		m_indirectExecButton.setSelection(false);

		if (m_rpa != null) {
			if (m_rpa.getRpaJobType() != null) {
				if (m_rpa.getRpaJobType() == JobRpaInfoResponse.RpaJobTypeEnum.DIRECT) {
					// 直接実行
					m_directExecButton.setSelection(true);
					m_indirectExecButton.setSelection(false);
					m_tabFolder.setSelection(m_directTabItem);
					setRpaJobType(RpaJobTypeConstant.DIRECT);
				} else {
					// 間接実行
					m_directExecButton.setSelection(false);
					m_indirectExecButton.setSelection(true);
					m_tabFolder.setSelection(m_indirectTabItem);
					setRpaJobType(RpaJobTypeConstant.INDIRECT);
				}
			}
		}
		// 直接実行タブ
		m_directExecutionComposite.reflectRpaJobInfo();
		// 間接実行タブ
		m_indirectExecutionComposite.reflectRpaJobInfo();
	}

	public ValidateResult createRpaJobInfo() {
		ValidateResult result = null;

		if (m_rpa == null) {
			m_rpa = new JobRpaInfoResponse();
			// インスタンスを複数のCompositeで共有するため、ここでsetしておく
			setRpaJobInfo(m_rpa);
		}

		// RPAジョブ種別
		if (m_directExecButton.getSelection()) {
			m_rpa.setRpaJobType(JobRpaInfoResponse.RpaJobTypeEnum.DIRECT);
		} else {
			m_rpa.setRpaJobType(JobRpaInfoResponse.RpaJobTypeEnum.INDIRECT);
		}

		// 必須項目の有無のチェックは選択されているRPAジョブ種別のタブのみ行う
		// 直接実行タブ
		if ((result = m_directExecutionComposite.validateRpaJobInfo()) != null) {
			return result;
		}
		// 間接実行タブ
		if ((result = m_indirectExecutionComposite.validateRpaJobInfo()) != null) {
			return result;
		}

		// 直接実行タブ
		m_directExecutionComposite.createRpaJobInfo();
		// 間接実行タブ
		m_indirectExecutionComposite.createRpaJobInfo();
		return result;
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// ラジオボタン
		m_directExecButton.setEnabled(enabled);
		m_indirectExecButton.setEnabled(enabled);
		// 直接実行タブ
		m_directExecutionComposite.setEnabled(enabled);
		// 間接実行タブ
		m_indirectExecutionComposite.setEnabled(enabled);
		if (enabled) {
			// 編集モードになった際に選択中のタブをラジオボタンに反映
			updateRadio();
		}
	}

	public void setOwnerRoleId(String ownerRoleId) {
		m_directExecutionComposite.setOwnerRoleId(ownerRoleId);
	}

	/**
	 * @return the m_rpa
	 */
	public JobRpaInfoResponse getRpaJobInfo() {
		return m_rpa;
	}

	/**
	 * 直接実行のラジオボタンが選択されているかを返します
	 */
	public boolean isDirectExecButtonSelected() {
		return m_directExecButton.getSelection();
	}

	/**
	 * @param m_rpa
	 *            the m_rpa to set
	 */
	public void setRpaJobInfo(JobRpaInfoResponse rpa) {
		this.m_rpa = rpa;
		// 直接実行タブ
		m_directExecutionComposite.setRpaJobInfo(rpa);
		// 間接実行タブ
		m_indirectExecutionComposite.setRpaJobInfo(rpa);
	}

	/**
	 * RPAシナリオジョブ種別を設定します。<br>
	 * 必須項目のチェック有無を判断するために使用します。
	 * 
	 * @param rpaJobType
	 */
	private void setRpaJobType(int rpaJobType) {
		m_directExecutionComposite.setRpaJobType(rpaJobType);
		m_indirectExecutionComposite.setRpaJobType(rpaJobType);
	}
}
