/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RoleInfoResponseP1;
import org.openapitools.client.model.UserInfoResponseP3;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.accesscontrol.util.AccessRestClientWrapper;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * オーナーロールIDコンポジットクラス<BR>
 *
 */
public class RoleIdListComposite extends Composite {

	public static enum Mode {OWNER_ROLE, ROLE}
	// ログ
	private static Log m_log = LogFactory.getLog( RoleIdListComposite.class );

	// ----- instance フィールド ----- //

	/** ロールIDコンボボックス */
	private Combo comboRoleId = null;

	/** ロールIDテキストボックス */
	private Text txtRoleId = null;

	/** 変更可能フラグ */
	private boolean m_enabledFlg = false;

	/** 種別 */
	private Mode m_mode = Mode.OWNER_ROLE;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //
	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 * @param enabledFlg 変更可否フラグ（true:変更可能、false:変更不可）
	 */
	public RoleIdListComposite(Composite parent, int style, String managerName, boolean enabledFlg, Mode mode) {
		super(parent, style);
		this.managerName = managerName;
		this.m_enabledFlg = enabledFlg;
		this.m_mode = mode;
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
			this.comboRoleId = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "roleid", comboRoleId);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboRoleId.setLayoutData(gridData);
		} else {
			// 変更不可な場合テキストボックス
			this.txtRoleId = new Text(this, SWT.BORDER | SWT.LEFT);
			WidgetTestUtil.setTestId(this, "roleid", txtRoleId);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.txtRoleId.setLayoutData(gridData);
			this.txtRoleId.setEnabled(false);
		}

		// 変更可能時のみコンボボックスにデータを設定する
		if (this.m_enabledFlg) {
			// ロールIDリストの初期化
			this.createRoleIdList(this.managerName);
			this.update();
			if (m_mode.equals(Mode.OWNER_ROLE)) {
				this.comboRoleId.select(this.comboRoleId.indexOf(RoleIdConstant.ALL_USERS));
			}
		}
	}

	/**
	 * コンボボックスに値を設定します。<BR>
	 * <p>
	 *
	 */
	public void createRoleIdList(String managerName) {
		UserInfoResponseP3 dto = null;
		// データ取得
		try {
			AccessRestClientWrapper wrapper = AccessRestClientWrapper.getWrapper(managerName);
			dto = wrapper.getOwnerRoleIdList();
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		if(dto != null){
			String roleIdOld = this.comboRoleId.getText();
			// クリア
			this.comboRoleId.removeAll();
			// カレンダIDリスト
			if (m_mode.equals(Mode.ROLE)) {
				this.comboRoleId.add("");
			}
			for(RoleInfoResponseP1 roleInfo : dto.getRoleList()){
				this.comboRoleId.add(roleInfo.getRoleId());
			}
			int defaultSelect = this.comboRoleId.indexOf(roleIdOld);
			if (defaultSelect == -1) {
				defaultSelect = this.comboRoleId.indexOf(RoleIdConstant.ALL_USERS);
			}
			this.comboRoleId.select( (-1 == defaultSelect) ? 0 : defaultSelect );
		}
	}

	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
		// 選択されていない場合は、一番上を選択する
		if (comboRoleId.getSelectionIndex() == -1) {
			comboRoleId.select(0);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (this.m_enabledFlg) {
			this.comboRoleId.setEnabled(enabled);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		if (this.m_enabledFlg) {
			return this.comboRoleId.getText();
		} else {
			return this.txtRoleId.getText();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		if (this.m_enabledFlg) {
			this.comboRoleId.setText(string);
		} else {
			this.txtRoleId.setText(string);
		}
	}

	public Combo getComboRoleId() {
		return comboRoleId;
	}

	public void add(String roleId) {
		if (m_enabledFlg) {
			this.comboRoleId.add(roleId);
			this.update();
		}
	}

	public void delete(String roleId) {
		if (m_enabledFlg) {
			if (this.comboRoleId.indexOf(roleId) > -1) {
				this.comboRoleId.remove(roleId);
				this.update();
			}
		}
	}

	public void addComboSelectionListener(SelectionListener listener) {
		if (this.m_enabledFlg) {
			this.comboRoleId.addSelectionListener(listener);
		}
	}
}
