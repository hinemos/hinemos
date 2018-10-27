/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.mail.composite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import com.clustercontrol.notify.mail.dialog.MailTemplateCreateDialog;
import com.clustercontrol.notify.mail.util.MailTemplateEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.mailtemplate.InvalidRole_Exception;
import com.clustercontrol.ws.mailtemplate.MailTemplateInfo;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * メールテンプレートID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「メールテンプレートID」 ラベル</dd>
 *  <dd>「メールテンプレートID一覧」 コンボボックス</dd>
 *  <dd>「[一覧]を開く」 ボタン</dd>
 * </dl>
 *
 * @version 3.0.0
 * @since 2.4.0
 */
public class MailTemplateIdListComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( MailTemplateIdListComposite.class );

	/** メールテンプレートID ラベル文字列。 */
	private String m_text = null;

	/** メールテンプレートID ラベル。 */
	private Label m_labelMailTemplateId = null;

	/** メールテンプレートID一覧 コンボボックス。 */
	private Combo m_comboMailTemplateId = null;

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
	 * @param labelFlg メールテンプレートIDラベル表示フラグ（表示する場合、<code> true </code>）。
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public MailTemplateIdListComposite(Composite parent, int style, String managerName, boolean labelFlg) {
		super(parent, style);
		m_text = Messages.getString("mail.template.id");
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
	 * @param text メールテンプレートID ラベル文字列
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite, boolean)
	 */
	public MailTemplateIdListComposite(Composite parent, int style, String text) {
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
		 * メールテンプレートID
		 */

		if(labelFlg){
			// ラベル
			this.m_labelMailTemplateId = new Label(this, SWT.NONE);
			WidgetTestUtil.setTestId(this, "mailtemplateid", m_labelMailTemplateId);
			gridData = new GridData();
			gridData.horizontalSpan = 3;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.m_labelMailTemplateId.setLayoutData(gridData);
			this.m_labelMailTemplateId.setText(m_text + " : ");
		}

		// コンボボックス
		this.m_comboMailTemplateId = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		WidgetTestUtil.setTestId(this, null, m_comboMailTemplateId);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_comboMailTemplateId.setLayoutData(gridData);
		//        this.m_comboMailTemplateId.addSelectionListener(new SelectionAdapter() {
		//        	public void widgetSelected(SelectionEvent e) {
		//
		//        	}
		//        });

		// 参照 ボタン
		this.m_buttonRefer = new Button(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, null, m_buttonRefer);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_buttonRefer.setLayoutData(gridData);
		this.m_buttonRefer.setText(Messages.getString("refer"));
		this.m_buttonRefer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				String mailTemplateId = m_comboMailTemplateId.getText();
				if(mailTemplateId != null && !"".equals(mailTemplateId.trim())){
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					// メールテンプレート登録/変更画面を開く
					MailTemplateCreateDialog dialog = new MailTemplateCreateDialog(
							shell,
							MailTemplateIdListComposite.this.managerName,
							mailTemplateId, PropertyDefineConstant.MODE_SHOW);
					dialog.open();
				}
			}
		});

		this.update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * メールテンプレートID一覧情報を取得し、メールテンプレートID一覧コンポジットにセットします。
	 *
	 * @see com.clustercontrol.notify.mail.action.GetMailTemplate#getMailTemplateIdList()
	 */
	@Override
	public void update() {
	}

	/**
	 * コンポジットを更新します。<BR>
	 * メールテンプレートID一覧情報を取得し、メールテンプレートID一覧コンポジットにセットします。
	 *
	 * @param ownerRoleId
	 */
	public void update(String ownerRoleId) {
		// 初期化
		this.m_comboMailTemplateId.removeAll();

		// 空欄
		this.m_comboMailTemplateId.add("");

		// データ取得
		List<String> list = null;

		try {
			MailTemplateEndpointWrapper wrapper = MailTemplateEndpointWrapper.getWrapper(this.managerName);
			List<MailTemplateInfo> listTmp = wrapper.getMailTemplateListByOwnerRole(ownerRoleId);
			list = new ArrayList<String>();
			for (MailTemplateInfo info : listTmp) {
				list.add(info.getMailTemplateId());
			}
		} catch (InvalidRole_Exception e) {
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
		} catch (Exception e) {
			m_log.warn("update() getMailTemplateList, " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(list != null){
			// メールテンプレートIDリスト
			for(int index=0; index<list.size(); index++){
				this.m_comboMailTemplateId.add(list.get(index));
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
		this.m_comboMailTemplateId.setEnabled(enabled);
		this.m_buttonRefer.setEnabled(enabled);
	}

	/**
	 * メールテンプレートIDを返します。
	 *
	 * @return メールテンプレートID
	 *
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		return this.m_comboMailTemplateId.getText();
	}

	/**
	 * メールテンプレートIDを設定します。
	 *
	 * @param string メールテンプレートID
	 *
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		this.m_comboMailTemplateId.setText(string);
	}
}
