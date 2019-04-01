/*
x * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.dialog.CommonMonitorDialog;
import com.clustercontrol.repository.bean.NodeConfigSettingItem;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;
import com.clustercontrol.ws.repository.NodeConfigSettingItemInfo;

/**
 * 構成情報収集条件コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「実行間隔」 コンボボックス</dd>
 *  <dd>「カレンダID」 コンボボックス</dd>
 * </dl>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class NodeConfigTargetComposite extends Composite {

	/** カラム数（タイトル）。 */
	public static final int WIDTH_TITLE = CommonMonitorDialog.WIDTH_TITLE;

	/** カラム数（値）。*/
	public static final int WIDTH_VALUE = CommonMonitorDialog.SHORT_UNIT;

	/** 空白のカラム数。 */
	public static final int WIDTH_WHITE_SPACE = CommonMonitorDialog.MIN_UNIT;

	/** 収集対象構成情報（ホスト名情報） チェックボックス */
	private Button m_checkTargetConfigHostname = null;
	/** 収集対象構成情報（OS） チェックボックス */
	private Button m_checkTargetConfigOs = null;
	/** 収集対象構成情報（HW-CPU情報） チェックボックス */
	private Button m_checkTargetConfigHwCpu = null;
	/** 収集対象構成情報（HW-メモリ情報） チェックボックス */
	private Button m_checkTargetConfigHwMemory = null;
	/** 収集対象構成情報（HW-NIC情報） チェックボックス */
	private Button m_checkTargetConfigHwNic = null;
	/** 収集対象構成情報（HW-ディスク情報） チェックボックス */
	private Button m_checkTargetConfigHwDisk = null;
	/** 収集対象構成情報（HW-ファイルシステム情報） チェックボックス */
	private Button m_checkTargetConfigHwFilesystem = null;
	/** 収集対象構成情報（ネットワーク接続） チェックボックス */
	private Button m_checkTargetConfigNetstat = null;
	/** 収集対象構成情報（プロセス） チェックボックス */
	private Button m_checkTargetConfigProcess = null;
	/** 収集対象構成情報（パッケージ） チェックボックス */
	private Button m_checkTargetConfigPackage = null;

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
	 * @see #initialize()
	 */
	public NodeConfigTargetComposite(Composite parent, int style) {
		super(parent, style);

		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 */
	private void initialize() {

		// 変数として利用されるグリッドデータ
		this.setLayout(new GridLayout(1, true));
		
		// 構成情報収集対象グループ
		Composite groupTarget = new Composite(this, SWT.NONE);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		groupTarget.setLayout(layout);


		// HW-ホスト名情報
		this.m_checkTargetConfigHostname = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHostname.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHostname.setText(NodeConfigSettingItem.HOSTNAME.displayName());
		this.m_checkTargetConfigHostname.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// OS情報
		this.m_checkTargetConfigOs = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigOs.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigOs.setText(NodeConfigSettingItem.OS.displayName());
		this.m_checkTargetConfigOs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-CPU情報
		this.m_checkTargetConfigHwCpu = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHwCpu.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwCpu.setText(NodeConfigSettingItem.HW_CPU.displayName());
		this.m_checkTargetConfigHwCpu.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-メモリ情報
		this.m_checkTargetConfigHwMemory = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHwMemory.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwMemory.setText(NodeConfigSettingItem.HW_MEMORY.displayName());
		this.m_checkTargetConfigHwMemory.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-NIC情報
		this.m_checkTargetConfigHwNic = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHwNic.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwNic.setText(NodeConfigSettingItem.HW_NIC.displayName());
		this.m_checkTargetConfigHwNic.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-ディスク情報
		this.m_checkTargetConfigHwDisk = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHwDisk.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwDisk.setText(NodeConfigSettingItem.HW_DISK.displayName());
		this.m_checkTargetConfigHwDisk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-ファイルシステム情報
		this.m_checkTargetConfigHwFilesystem = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigHwFilesystem.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigHwFilesystem.setText(NodeConfigSettingItem.HW_FILESYSTEM.displayName());
		this.m_checkTargetConfigHwFilesystem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// HW-ネットワーク接続
		this.m_checkTargetConfigNetstat = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigNetstat.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigNetstat.setText(NodeConfigSettingItem.NETSTAT.displayName());
		this.m_checkTargetConfigNetstat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// プロセス情報
		this.m_checkTargetConfigProcess = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigProcess.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigProcess.setText(NodeConfigSettingItem.PROCESS.displayName());
		this.m_checkTargetConfigProcess.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		// パッケージ情報
		this.m_checkTargetConfigPackage = new Button(groupTarget, SWT.CHECK);
		this.m_checkTargetConfigPackage.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		this.m_checkTargetConfigPackage.setText(NodeConfigSettingItem.PACKAGE.displayName());
		this.m_checkTargetConfigPackage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});

		
	}

	/**
	 * 引数で指定された監視情報の値を、各項目に設定します。
	 *
	 * @param info 設定値として用いる監視情報
	 *
	 * @see com.clustercontrol.calendar.composite.CalendarIdListComposite#setText(String)
	 */
	public void setInputData(NodeConfigSettingInfo info) {

		m_checkTargetConfigHostname.setSelection(false);
		m_checkTargetConfigOs.setSelection(false);
		m_checkTargetConfigHwCpu.setSelection(false);
		m_checkTargetConfigHwMemory.setSelection(false);
		m_checkTargetConfigHwNic.setSelection(false);
		m_checkTargetConfigHwDisk.setSelection(false);
		m_checkTargetConfigHwFilesystem.setSelection(false);
		m_checkTargetConfigNetstat.setSelection(false);
		m_checkTargetConfigProcess.setSelection(false);
		m_checkTargetConfigPackage.setSelection(false);

		if(info != null && info.getNodeConfigSettingItemList() != null){
			
			List<NodeConfigSettingItemInfo> items = info.getNodeConfigSettingItemList();

			for (NodeConfigSettingItemInfo it : items){
				if (it.getSettingItemId().equals(NodeConfigSettingItem.HOSTNAME.name())) {
					this.m_checkTargetConfigHostname.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.OS.name())) {
					this.m_checkTargetConfigOs.setSelection(true);

				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.HW_CPU.name())) {
					this.m_checkTargetConfigHwCpu.setSelection(true);

				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.HW_MEMORY.name())) {
					this.m_checkTargetConfigHwMemory.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.HW_NIC.name())) {
					this.m_checkTargetConfigHwNic.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.HW_DISK.name())) {
					this.m_checkTargetConfigHwDisk.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.HW_FILESYSTEM.name())) {
					this.m_checkTargetConfigHwFilesystem.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.NETSTAT.name())) {
					this.m_checkTargetConfigNetstat.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.PROCESS.name())) {
					this.m_checkTargetConfigProcess.setSelection(true);
				
				} else if (it.getSettingItemId().equals(NodeConfigSettingItem.PACKAGE.name())) {
					this.m_checkTargetConfigPackage.setSelection(true);
					
				}
			}
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_checkTargetConfigHostname.setEnabled(enabled);
		this.m_checkTargetConfigOs.setEnabled(enabled);
		this.m_checkTargetConfigHwCpu.setEnabled(enabled);
		this.m_checkTargetConfigHwMemory.setEnabled(enabled);
		this.m_checkTargetConfigHwNic.setEnabled(enabled);
		this.m_checkTargetConfigHwDisk.setEnabled(enabled);
		this.m_checkTargetConfigHwFilesystem.setEnabled(enabled);
		this.m_checkTargetConfigNetstat.setEnabled(enabled);
		this.m_checkTargetConfigProcess.setEnabled(enabled);
		this.m_checkTargetConfigPackage.setEnabled(enabled);
	}


	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 * @return 認証結果
	 */
	protected ValidateResult setValidateResult(String id, String message) {

		ValidateResult validateResult = new ValidateResult();
		validateResult.setValid(false);
		validateResult.setID(id);
		validateResult.setMessage(message);

		return validateResult;
	}
	
	/**
	 * 構成情報取得対象のリストを取得します。
	 * @return 
	 */
	public List<Object> getTarget() {
		List<Object> targetList = new ArrayList<Object>();

		if (this.m_checkTargetConfigHostname.getSelection()) {
			targetList.add(NodeConfigSettingItem.HOSTNAME);
		}
		if (this.m_checkTargetConfigOs.getSelection()) {
			targetList.add(NodeConfigSettingItem.OS);
		}
		if (this.m_checkTargetConfigHwCpu.getSelection()) {
			targetList.add(NodeConfigSettingItem.HW_CPU);
		}
		if (this.m_checkTargetConfigHwMemory.getSelection()) {
			targetList.add(NodeConfigSettingItem.HW_MEMORY);
		}
		if (this.m_checkTargetConfigHwNic.getSelection()) {
			targetList.add(NodeConfigSettingItem.HW_NIC);
		}
		if (this.m_checkTargetConfigHwDisk.getSelection()) {
			targetList.add(NodeConfigSettingItem.HW_DISK);
		}
		if (this.m_checkTargetConfigHwFilesystem.getSelection()) {
			targetList.add(NodeConfigSettingItem.HW_FILESYSTEM);
		}
		if (this.m_checkTargetConfigNetstat.getSelection()) {
			targetList.add(NodeConfigSettingItem.NETSTAT);
		}
		if (this.m_checkTargetConfigPackage.getSelection()) {
			targetList.add(NodeConfigSettingItem.PACKAGE);
		}
		if (this.m_checkTargetConfigProcess.getSelection()) {
			targetList.add(NodeConfigSettingItem.PROCESS);
		}
		return targetList;
	}
}
