/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * 親タグコンポジットクラス<BR>
 */
public class RpaScenarioTagPathListComposite extends Composite {

	// ログ
		private static Log log = LogFactory.getLog( RpaScenarioTagPathListComposite.class );

	// ----- instance フィールド ----- //

	/** 親タグコンボボックス（表示用のIDのみ保持） */
	private Combo comboScenarioTagPath = null;

	/** 親タグ(ID,階層を関連付けて保持) */
	private Map<String, String> mapScenarioTagPath = new HashMap<>();

	/** 親タグテキストボックス */
	private Text txtScenarioTagPath = null;

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
	public RpaScenarioTagPathListComposite(Composite parent, int style, String managerName, boolean enabledFlg) {
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
		 * ロールID
		 */
		if (this.enabledFlg) {
			// 変更可能な場合コンボボックス
			this.comboScenarioTagPath = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			WidgetTestUtil.setTestId(this, "scenarioTagPath", comboScenarioTagPath);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboScenarioTagPath.setLayoutData(gridData);
		} else {
			// 変更不可な場合テキストボックス
			this.txtScenarioTagPath = new Text(this, SWT.BORDER | SWT.LEFT);
			WidgetTestUtil.setTestId(this, "scenarioTagPath", txtScenarioTagPath);

			gridData = new GridData();
			gridData.horizontalSpan = 1;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.txtScenarioTagPath.setLayoutData(gridData);
			this.txtScenarioTagPath.setEnabled(false);
		}

		// 変更可能時はコンボボックスに、変更不可時は親タグ表示用のデータを設定する
		if (this.enabledFlg) {
			// 親タグリストの初期化
			this.createTagPathList();
			this.update();
		} else {
			this.createTagPathMap();
		}
	}

	/**
	 * コンボボックスに値を設定します。<BR>
	 * <p>
	 *
	 */
	public void createTagPathList() {
		List<RpaScenarioTagResponse> dtoList = null;
		// データ取得
		dtoList = callTagList();

		if(dtoList != null){
			String tagPathOld = this.comboScenarioTagPath.getText();
			// クリア
			this.mapScenarioTagPath.clear();
			this.comboScenarioTagPath.removeAll();
			this.comboScenarioTagPath.add("");
			
			for(RpaScenarioTagResponse tag : dtoList){
				this.mapScenarioTagPath.put(tag.getTagId(), tag.getTagPath());
				this.comboScenarioTagPath.add(tag.getTagId());
			}
			int defaultSelect = this.comboScenarioTagPath.indexOf(tagPathOld);
			this.comboScenarioTagPath.select( (-1 == defaultSelect) ? 0 : defaultSelect );
		}
	}
	
	private List<RpaScenarioTagResponse> callTagList(){
		List<RpaScenarioTagResponse> dtoList = null;
		try {
			RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.managerName);
			dtoList = wrapper.getRpaScenarioTagList(null);
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			// 上記以外の例外
			log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}
		return dtoList;
	}
	
	/**
	 * コンポジットを更新します。<BR>
	 * <p>
	 *
	 */
	@Override
	public void update() {
		this.comboScenarioTagPath.select(0);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (this.enabledFlg) {
			this.comboScenarioTagPath.setEnabled(enabled);
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#getText()
	 */
	public String getText() {
		if (this.enabledFlg) {
			return this.comboScenarioTagPath.getText();
		} else {
			return this.txtScenarioTagPath.getText();
		}
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Combo#setText(java.lang.String)
	 */
	public void setText(String string) {
		if (this.enabledFlg) {
			this.comboScenarioTagPath.setText(string);
		} else {
			this.txtScenarioTagPath.setText(string);
		}
	}

	public String getTagPath(String tagId) {
		String ret = "";
		if (! this.mapScenarioTagPath.isEmpty()){
			ret = setReturnTagPath(tagId);
		}
		return ret;
	}
	
	private String setReturnTagPath(String tagId){
		String tagPath = this.mapScenarioTagPath.get(tagId);
		if (tagId != null && tagId.length() > 0){
			if (tagPath != null && tagPath.equals("")){
				return "\\" + tagId;
			} else if (tagPath != null){
				return tagPath + "\\" + tagId;
			}
		} 
		return "";
	}
	
	public String getParentTagId(String tagPath) {
		String ret = "";
		if (tagPath != null && !tagPath.equals("")){
			int index = tagPath.lastIndexOf("\\");
			ret = tagPath.substring(index + 1);
		}
		return ret;
	}
	
	public void createTagPathMap(){
		List<RpaScenarioTagResponse> dtoList = null;
		dtoList = callTagList();
		
		if(dtoList != null){
			// クリア
			this.mapScenarioTagPath.clear();
			
			for(RpaScenarioTagResponse tag : dtoList){
				this.mapScenarioTagPath.put(tag.getTagId(), tag.getTagPath());
			}
		}
	}

	public void addModifyListener(ModifyListener modifyListener){
		comboScenarioTagPath.addModifyListener(modifyListener);
	}

	public Combo getComboManagerName() {
		return comboScenarioTagPath;
	}

	public void add(String managerName) {
		this.comboScenarioTagPath.add(managerName);
		this.update();
	}

	public void delete(String managerName) {
		if (this.comboScenarioTagPath.indexOf(managerName) > -1) {
			this.comboScenarioTagPath.remove(managerName);
			this.update();
		}
	}

	public void addComboSelectionListener(SelectionListener listener) {
		if (this.enabledFlg) {
			this.comboScenarioTagPath.addSelectionListener(listener);
		}
	}
	
}
