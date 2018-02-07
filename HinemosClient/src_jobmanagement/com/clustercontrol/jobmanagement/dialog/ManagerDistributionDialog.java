/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.jobmanagement.composite.ScriptComponent;
import com.clustercontrol.jobmanagement.util.JobEndpointWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * マネージャから配布ダイアログクラスです。
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class ManagerDistributionDialog extends CommonDialog {
	
	private static Log m_log = LogFactory.getLog( ManagerDistributionDialog.class );
	
	/** マネージャから配布用チェックボックス */
	private Button m_managerDistribution = null;
	/** スクリプト用コンポーネント */
	private ScriptComponent m_component = null;
	/** マネージャ */
	private String m_manager = null;
	/** 読み取り専用フラグ */
	private boolean m_readOnly = false;
	
	/** マネージャから配布 */
	private boolean managerDistribution;
	private String scriptName = null;
	private String scriptEncoding = null;
	private String scriptContent = null;
	
	public void setManager(String manager) {
		this.m_manager = manager;
	}
	
	public boolean getManagerDistribution() {
		return managerDistribution;
	}
	
	public void setManagerDistribution(boolean managerDistribution) {
		this.managerDistribution = managerDistribution;
	}
	
	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getScriptEncoding() {
		return scriptEncoding;
	}

	public void setScriptEncoding(String scriptEncoding) {
		this.scriptEncoding = scriptEncoding;
	}

	public String getScriptContent() {
		return scriptContent;
	}

	public void setScriptContent(String scriptContent) {
		this.scriptContent = scriptContent;
	}

	/** コンストラクタ
	 * 
	 * @param parent
	 * @param readOnly
	 */
	public ManagerDistributionDialog(Shell parent, boolean readOnly) {
		super(parent);
		this.m_readOnly = readOnly;
	}
	
	/**
	 * ダイアログエリアを生成します。
	 *
	 * @param parent 親コンポジット
	 *
	 * @see com.clustercontrol.monitor.action.GetEventFilterProperty#getProperty()
	 * @see com.clustercontrol.bean.JobParamTypeConstant
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		parent.getShell().setText(Messages.getString("job.script.distribution"));
		
		GridLayout layout = new GridLayout(4, false);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		parent.setLayout(layout);
		
		this.m_managerDistribution = new Button(parent, SWT.CHECK);
		WidgetTestUtil.setTestId(this, "m_managerDistribution", this.m_managerDistribution);
		this.m_managerDistribution.setText(Messages.getString("job.manager.distribution"));
		GridData mdGrid = new GridData(220, SizeConstant.SIZE_BUTTON_HEIGHT);
		mdGrid.horizontalSpan = 2;
		this.m_managerDistribution.setLayoutData(mdGrid);
		this.m_managerDistribution.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				WidgetTestUtil.setTestId(this, null, check);
				if (check.getSelection()) {
					m_component.getScriptName().setEditable(true);
					m_component.getScriptUpload().setEnabled(true);
					m_component.getScriptDownload().setEnabled(true);
					m_component.getScriptEncoding().setEditable(true);
					m_component.getScriptContent().setEditable(true);
				} else {
					m_component.getScriptName().setEditable(false);
					m_component.getScriptUpload().setEnabled(false);
					m_component.getScriptDownload().setEnabled(false);
					m_component.getScriptEncoding().setEditable(false);
					m_component.getScriptContent().setEditable(false);
					
				}
				update();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		//dummy
		new Label(parent, SWT.LEFT);
		new Label(parent, SWT.LEFT);
		
		// スクリプト配布：スクリプト名、アップロード、ダウンロード、スクリプトエンコーディング、スクリプト（コンポーネント）
		m_component = new ScriptComponent(parent, this);
		
		// データ反映
		reflectManagerDisribution();
		
		// コンポーネントの無効/有効
		// 読み取り専用の場合
		if(m_readOnly) {
			m_managerDistribution.setEnabled(false);
			m_component.getScriptName().setEditable(false);
			m_component.getScriptUpload().setEnabled(false);
			m_component.getScriptDownload().setEnabled(false);
			m_component.getScriptEncoding().setEditable(false);
			m_component.getScriptContent().setEditable(false);
		// 読み取り専用ではない、かつ、マネージャから配布が未チェックの場合
		} else if(!m_managerDistribution.getSelection()) {
			m_component.getScriptName().setEditable(false);
			m_component.getScriptUpload().setEnabled(false);
			m_component.getScriptDownload().setEnabled(false);
			m_component.getScriptEncoding().setEditable(false);
			m_component.getScriptContent().setEditable(false);
		}
	}

	/**
	 * マネージャ配布情報をダイアログに反映します。
	 */
	private void reflectManagerDisribution() {
		if(managerDistribution) {
			m_managerDistribution.setSelection(true);
		} else {
			m_managerDistribution.setSelection(false);
		}
		
		if(scriptName != null && !scriptName.isEmpty()) {
			m_component.getScriptName().setText(scriptName);
		}
		
		if(scriptEncoding != null && !scriptEncoding.isEmpty()) {
			m_component.getScriptEncoding().setText(scriptEncoding);
		} else {
			m_component.getScriptEncoding().setText("UTF-8");
		}
		
		if(scriptContent != null && !scriptContent.isEmpty()) {
			m_component.getScriptContent().setText(scriptContent);
		}
	}

	public void update(){
		if(m_managerDistribution.getSelection() && "".equals(m_component.getScriptName().getText())) {
			m_component.getScriptName().setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			m_component.getScriptName().setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_managerDistribution.getSelection() && "".equals(m_component.getScriptEncoding().getText())) {
			m_component.getScriptEncoding().setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			m_component.getScriptEncoding().setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		if(m_managerDistribution.getSelection() && "".equals(m_component.getScriptContent().getText())) {
			m_component.getScriptContent().setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			m_component.getScriptContent().setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}
	
	/**
	 * ＯＫボタンテキスト取得
	 *
	 * @return ＯＫボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンテキスト取得
	 *
	 * @return キャンセルボタンのテキスト
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("cancel");
	}

	/**
	 * 入力値チェックをします。
	 *
	 * @return 検証結果
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#validate()
	 */
	@Override
	protected ValidateResult validate() {
		ValidateResult result = null;

		if(m_managerDistribution.getSelection()) {
			setManagerDistribution(true);
			if(m_component.getScriptName() != null && m_component.getScriptName().getText().length() > 0 && 
					m_component.getScriptContent() != null && m_component.getScriptContent().getText().length() > 0 &&
					m_component.getScriptEncoding() != null && m_component.getScriptEncoding().getText().length() > 0) {
				setScriptName(m_component.getScriptName().getText());
				setScriptEncoding(m_component.getScriptEncoding().getText());
				setScriptContent(m_component.getScriptContent().getText());
			} else {
				result = new ValidateResult();
				result.setValid(false);
				result.setID(Messages.getString("message.hinemos.1"));
				result.setMessage(Messages.getString("message.job.160"));
				return result;
			}
		} else {
			setManagerDistribution(false);
			if(m_component.getScriptName() != null && m_component.getScriptContent() != null && m_component.getScriptEncoding() != null) {
				setScriptName(m_component.getScriptName().getText());
				setScriptEncoding(m_component.getScriptEncoding().getText());
				setScriptContent(m_component.getScriptContent().getText());
			}
		}
		
		int scriptSize = m_component.getScriptContent().getText().length();
		int maxsize = getScriptContentMaxSize();
		if(scriptSize > maxsize) {
			result = new ValidateResult();
			result.setValid(false);
			result.setID(Messages.getString("message.hinemos.1"));
			result.setMessage(Messages.getString("message.job.161", new Object[] {scriptSize, maxsize}));
			return result;
		}

		return null;
	}
	
	private int getScriptContentMaxSize() {
		int maxsize = 8192;
		
		JobEndpointWrapper wrapper = JobEndpointWrapper.getWrapper(m_manager);
		try {
			maxsize = wrapper.getScriptContentMaxSize();
		} catch (Exception e) {
			m_log.warn("getScriptContentMaxSize() getHinemosProperty, " + e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		return maxsize;
	}
}
