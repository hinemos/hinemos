/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.reporting.dialog.TemplateSetDialog;
import com.clustercontrol.reporting.util.ReportingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.reporting.InvalidRole_Exception;
import com.clustercontrol.ws.reporting.TemplateSetInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * テンプレートセットID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「テンプレートセットID」 ラベル</dd>
 *  <dd>「テンプレートセットID一覧」 コンボボックス</dd>
 *  <dd>「[参照」 ボタン</dd>
 * </dl>
 *
 * @version 5.0.a
 * @since 5.0.a
 */
public class TemplateSetIdListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( TemplateSetIdListComposite.class );

	/** テンプレートセットID ラベル文字列。 */
	private String m_text = null;

	/** テンプレートセットID ラベル。 */
	private Label m_labelTemplateSetId = null;

	/** テンプレートセットID一覧 コンボボックス。 */
	private Combo m_comboTemplateSetId = null;

	/** 参照 ボタン。 */
	private Button m_buttonRefer = null;

	/**マネージャ名 */
	private String managerName = null;


	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param labelFlg テンプレートセットIDラベル表示フラグ（表示する場合、<code> true </code>）。
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public TemplateSetIdListComposite(Composite parent, int style, String managerName, boolean labelFlg) {
		super(parent, style);
		m_text = Messages.getString("template.set.id");
		this.managerName = managerName;

		this.initialize(parent, labelFlg);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 * メールテンプレートIDラベルの文字列を指定します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param text テンプレートセットID ラベル文字列
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public TemplateSetIdListComposite(Composite parent, int style, String text) {
		super(parent, style);
		m_text = text;

		this.initialize(parent, true);
	}


	/**
	 * コンポジットを配置します。
	 *
	 * @see #update()
	 */
	private void initialize(Composite parent, boolean labelFlg) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		if(labelFlg){
			layout.numColumns = 10;
		}
		else{
			layout.numColumns = 5;
		}
		this.setLayout(layout);

		/*
		 * テンプレートセットID
		 */

		if(labelFlg){
			// ラベル
			this.m_labelTemplateSetId = new Label(this, SWT.NONE);
			WidgetTestUtil.setTestId(this, "templatesetid", m_labelTemplateSetId);
			gridData = new GridData();
			gridData.horizontalSpan = 3;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.m_labelTemplateSetId.setLayoutData(gridData);
			this.m_labelTemplateSetId.setText(m_text + " : ");
		}

		// コンボボックス
		this.m_comboTemplateSetId = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboTemplateSetId);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboTemplateSetId.setLayoutData(gridData);
		this.m_comboTemplateSetId.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		// 参照 ボタン
		this.m_buttonRefer = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_buttonRefer);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonRefer.setLayoutData(gridData);
		this.m_buttonRefer.setText(Messages.getString("refer"));
		this.m_buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String templateSetId = m_comboTemplateSetId.getText();
				if(templateSetId != null && !"".equals(templateSetId.trim())){
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					// テンプレートセット登録/変更画面を開く
					TemplateSetDialog dialog = new TemplateSetDialog(
							shell,
							TemplateSetIdListComposite.this.managerName,
							templateSetId,
							PropertyDefineConstant.MODE_SHOW);
					dialog.open();
				}
			}
		});

		this.update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * テンプレートセットID一覧情報を取得し、テンプレートセットID一覧コンポジットにセットします。
	 *
	 * @see com.clustercontrol.reporting.action.GetReportingTemplateSet#getReportingTemplateSetIdList()
	 */
	@Override
	public void update() {
		if ("".equals(this.m_comboTemplateSetId.getText())) {
			this.m_comboTemplateSetId.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		} else {
			this.m_comboTemplateSetId.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 * テンプレートセットID一覧情報を取得し、テンプレートセットID一覧コンポジットにセットします。
	 *
	 * @param ownerRoleId
	 */
	public void update(String ownerRoleId) {
		// 初期化
		this.m_comboTemplateSetId.removeAll();

		// 空欄
		this.m_comboTemplateSetId.add("");

		// データ取得
		List<String> list = new ArrayList<String>();
		
		try {
			ReportingEndpointWrapper wrapper = ReportingEndpointWrapper.getWrapper(this.managerName);
			List<TemplateSetInfo> listTmp = wrapper.getTemplateSetInfoList(ownerRoleId);
			for(TemplateSetInfo info : listTmp) {
				list.add(info.getTemplateSetId());
			}
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("update() getTemplateSetInfoList, " + errMessage, e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}
		
		if(list != null){
			// テンプレートIDリスト
			for(int index=0; index<list.size(); index++){
				this.m_comboTemplateSetId.add(list.get(index));
			}
		}
	}
	
	public void setManagerName (String managerName) {
		this.managerName = managerName; 
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_comboTemplateSetId.setEnabled(enabled);
		this.m_buttonRefer.setEnabled(enabled);
	}

	/**
	 * テンプレートセットIDを返します。
	 *
	 * @return テンプレートセットID
	 *
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		return this.m_comboTemplateSetId.getText();
	}

	/**
	 * テンプレートセットIDを設定します。
	 *
	 * @param string テンプレートセットID
	 *
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		this.m_comboTemplateSetId.setText(string);
	}
}
