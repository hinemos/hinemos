/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.RpaToolResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.UrlNotFound;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;


/**
 * RPAツールコンポジットクラス
 */
public class RpaToolListComposite extends Composite {

	// ログ
		private static Log m_log = LogFactory.getLog( RpaToolListComposite.class );

	// ----- instance フィールド ----- //

	/** RPAツールコンボボックス（表示用のツール名のみ保持） */
	private Combo comboRpaTool = null;

	/** Rpaツールテキストボックス */
	private Text txtRpaTool = null;

	/** 変更可能フラグ */
	private boolean enabledFlg = false;

	/** マネージャ名 */
	private String managerName = null;

	// ----- コンストラクタ ----- //

	/**
	 * インスタンスを返します。<BR>
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param enabledFlg 変更可否フラグ（true:変更可能、false:変更不可）
	 */
	public RpaToolListComposite(Composite parent, int style, String managerName, boolean enabledFlg) {
		super(parent, style);
		this.managerName = managerName;
		this.enabledFlg = enabledFlg;
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
		 * RPAツール設定
		 */
		if (this.enabledFlg) {
			// 変更可能な場合コンボボックス
			this.comboRpaTool = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboRpaTool.setLayoutData(gridData);
		} else {
			// 変更不可な場合テキストボックス
			this.txtRpaTool = new Text(this, SWT.BORDER | SWT.LEFT);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.txtRpaTool.setLayoutData(gridData);
			this.txtRpaTool.setEnabled(false);
		}

		
		// 変更可能時はコンボボックスを表示する
		this.refresh();
		this.update();
	}

	/**
	 * コンボボックスに値を設定します。<BR>
	 * <p>
	 *
	 */
	private void setCompoRpaTool(List<RpaToolResponse> dtoList){
		if(dtoList != null){
			String toolOld = this.comboRpaTool.getText();
			this.comboRpaTool.removeAll();
			
			for(RpaToolResponse tool : dtoList){
				this.comboRpaTool.add(tool.getRpaToolId());
			}
			int defaultSelect = this.comboRpaTool.indexOf(toolOld);
			this.comboRpaTool.select( (-1 == defaultSelect) ? 0 : defaultSelect );
		}
	}
	
	/**
	 * RPAツール情報を取得します。<BR>
	 * <p>
	 *
	 */
	private List<RpaToolResponse> callRpaToolList(){
		List<RpaToolResponse> dtoList = null;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.managerName);
			dtoList = wrapper.getRpaTool();
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// エンタープライズ機能が無効の場合は無視する
			if(UrlNotFound.class.equals(e.getCause().getClass())) {
				return dtoList;
			}
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return dtoList;
	}
	
	/**
	 * マネージャから最新の情報を取得する。
	 */
	public void refresh() {
		if (this.enabledFlg) {
			List<RpaToolResponse> dtoList = this.callRpaToolList();
			this.setCompoRpaTool(dtoList);
			this.comboRpaTool.select(0);
		}
	}
	
	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabledFlg) {
			this.comboRpaTool.setEnabled(enabled);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		if (this.enabledFlg) {
			return this.comboRpaTool.getText();
		} else {
			return this.txtRpaTool.getText();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		if (this.enabledFlg) {
			this.comboRpaTool.setText(string);
		} else {
			this.txtRpaTool.setText(string);
		}
	}
	
	public void setRpaToolId(String rpaToolId) {
		setText(rpaToolId);
	}

	public void addModifyListener(ModifyListener modifyListener){
		comboRpaTool.addModifyListener(modifyListener);
	}

	public Combo getComboManagerName() {
		return comboRpaTool;
	}

	public void addComboSelectionListener(SelectionListener listener) {
		if (this.enabledFlg) {
			this.comboRpaTool.addSelectionListener(listener);
		}
	}

	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}
	
}
