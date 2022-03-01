/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox;
import com.clustercontrol.rpa.action.GetRpaScenarioTagToDialogTableDefineCheckBox;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.util.RpaScenarioTagUtil;
import com.clustercontrol.util.CheckBoxSelectionAdapter;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * タグ選択ダイアログ用タグ一覧コンポジットクラス<BR>
 */
public class RpaScenarioTagListToDialogComposite extends Composite {

	private static Log m_log = LogFactory.getLog( RpaScenarioTagListToDialogComposite.class );
	
	/** テーブルビューアー */
	private CommonTableViewer tableViewer = null;
	/** 取得しているタグ一覧 */
	private List<RpaScenarioTagResponse> tagList;
	/** タグID：タグ名 */
	private Map<String, String> tagNameMap;
	/** タグID：タグパス */
	private Map<String, String> tagPathMap;
	/** マネージャ名 */
	private String managerName = null;

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
	public RpaScenarioTagListToDialogComposite(Composite parent, int style) {
		super(parent, style);
		this.initialize();
	}

	/**
	 * コンポジットを配置します。
	 *
	 * @see com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox#get()
	 * @see #update()
	 */
	private void initialize() {
		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		final Table table = new Table(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		table.setLayoutData(gridData);

		// テーブルビューアの作成
		this.tableViewer = new CommonTableViewer(table);
		
		this.tableViewer.createTableColumn(GetRpaScenarioTagToDialogTableDefineCheckBox.get(),
		GetNotifyTableDefineCheckBox.SORT_COLUMN_INDEX,
		GetNotifyTableDefineCheckBox.SORT_ORDER);
		
		/** チェックボックスの選択を制御するリスナー */
		SelectionAdapter adapter =
				new CheckBoxSelectionAdapter(this, this.tableViewer, GetRpaScenarioTagToDialogTableDefineCheckBox.SELECTION);
		table.addSelectionListener(adapter);
	}
	
	/**
	 * このコンポジットが利用するテーブルビューアーを返します。
	 *
	 * @return テーブルビューアー
	 */
	public CommonTableViewer getTableViewer() {
		return this.tableViewer;
	}

	/**
	 * このコンポジットが利用するテーブルを返します。
	 *
	 * @return テーブル
	 */
	public Table getTable() {
		return this.tableViewer.getTable();
	}

	/**
	 * コンポジットを更新します。<BR>
	 * タグ一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotifyList()
	 */
	@Override
	public void update() {
		List<RpaScenarioTagResponse> list = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(managerName);
		try {
			list = wrapper.getRpaScenarioTagList(null);
		} catch (InvalidRole e) {
			// 権限なし
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			m_log.warn("update(), " + errMessage, e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}
		if(list == null){
			list = new ArrayList<RpaScenarioTagResponse>();
		}
		
		if(tagList == null){
			tagList = new ArrayList<>();
		}
		if(tagNameMap == null){
			tagNameMap = new HashMap<>();
		}
		if(tagPathMap == null){
			tagPathMap = new HashMap<>();
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}

		ArrayList<Object> listInput = new ArrayList<Object>();
		this.tagNameMap.clear();
		this.tagPathMap.clear();
		
		List<String> tagIdList = new ArrayList<>();
		for (RpaScenarioTagResponse tag : this.tagList){
			tagIdList.add(tag.getTagId());
		}
		
		for (RpaScenarioTagResponse info : list) {
			this.tagNameMap.put(info.getTagId(), info.getTagName());
			this.tagPathMap.put(info.getTagId(), info.getTagPath());
			
			ArrayList<Object> obj = new ArrayList<Object>();
			if(tagIdList.contains(info.getTagId())){
				obj.add(true);
			} else {
				obj.add(false);
			}
			obj.add(info.getTagId());
			obj.add(new RpaScenarioTagUtil().getTagLayer(info, this.tagNameMap, this.tagPathMap));
			obj.add(info.getDescription());
			listInput.add(obj);
		}
		this.tableViewer.setInput(listInput);
	}
	
	/**
	 * このコンポジットで選択されているタグを返します。
	 */
	public List<RpaScenarioTagResponse> getSelectTag(){
		return this.tagList;
	}
	
	/**
	 * このコンポジットで取得しているタグID：タグ名の組み合わせを返します。
	 */
	public Map<String, String> getTagNameMap(){
		return this.tagNameMap;
	}
	
	/**
	 * このコンポジットで取得しているタグID：タグパスの組み合わせを返します。
	 */
	public Map<String, String> getTagPathMap(){
		return this.tagPathMap;
	}


	/**
	 * 検索に設定されているタグ情報をセットします。
	 */
	public void setSelectTag(List<RpaScenarioTagResponse> selectTagList){
		if(selectTagList != null){
			this.tagList =  new ArrayList<>();
		}
		this.tagList = selectTagList;
	}

	/**
	 * OKが押されたときにタグ情報を生成します。
	 *
	 */
	public boolean makeTagData(){

		TableItem[] ti = this.tableViewer.getTable().getItems();
		ArrayList<?> al;
		Collection<RpaScenarioTagResponse> ct = new ArrayList<>();

		for (int i = 0; i<ti.length; i++){
			al = (ArrayList<?>)ti[i].getData();
			
			if((Boolean)al.get(GetRpaScenarioTagToDialogTableDefineCheckBox.SELECTION)){
				RpaScenarioTagResponse tag = new RpaScenarioTagResponse();
				tag.setTagId((String)al.get(GetRpaScenarioTagToDialogTableDefineCheckBox.TAG_ID));
				
				ct.add(tag);
			}
		}

		if (tagList != null) {
			tagList.clear();
			tagList.addAll(ct);
		}
		return true;
	}

	/**
	 * 選択された行のタグを取得する
	 */
	public ArrayList<String> getSelectionData() {

		ArrayList<String> data = new ArrayList<String>();

		//選択されたアイテムを取得
		StructuredSelection selection =
				(StructuredSelection)tableViewer.getSelection();
		List<?> list = selection.toList();

		if (list != null) {

			for(int index = 0; index < list.size(); index++){

				ArrayList<?> info = (ArrayList<?>)list.get(index);
				if (info != null && info.size() > 0) {
					String tagId = (String)info.get(GetRpaScenarioTagToDialogTableDefineCheckBox.TAG_ID);
					data.add(tagId);
				}
			}
		}

		return data;
	}

	/**
	 * @return the managerName
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @param managerName the managerName to set
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

}
