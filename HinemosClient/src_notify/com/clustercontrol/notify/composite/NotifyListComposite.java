/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.openapitools.client.model.NotifyInfoResponse;
import org.openapitools.client.model.NotifyInfoResponse.NotifyTypeEnum;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.notify.action.GetNotify;
import com.clustercontrol.notify.action.GetNotifyTableDefineCheckBox;
import com.clustercontrol.notify.action.GetNotifyTableDefineNoCheckBox;
import com.clustercontrol.notify.composite.action.NotifyDoubleClickListener;
import com.clustercontrol.notify.util.NotifyTypeUtil;
import com.clustercontrol.util.CheckBoxSelectionAdapter;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;

/**
 * 通知一覧コンポジットクラス<BR>
 *
 *
 * @version 3.0.0
 * @since 2.0.0
 */
public class NotifyListComposite extends Composite {

	private static Log m_log = LogFactory.getLog( NotifyListComposite.class );
	
	/** テーブルビューアー。 */
	private CommonTableViewer tableViewer = null;

	/***/
	private boolean isSelect = false;

	/** 合計ラベル */
	private Label totalLabel = null;

	/** 検索条件 */
	private Property condition = null;

	/**/
	private List<NotifyRelationInfoResponse> notify;

	/** マネージャ名 */
	private String managerName = null;

	// オーナーロールID
	private String ownerRoleId = null;

	// 表示用フラグ
	private boolean showFlg = true;

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
	public NotifyListComposite(Composite parent, int style, boolean isSelect, String ownerRoleId) {
		super(parent, style);
		this.isSelect =isSelect;
		this.ownerRoleId = ownerRoleId;
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
		WidgetTestUtil.setTestId(this, null, table);
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

		if (this.isSelect) {
			this.tableViewer.createTableColumn(GetNotifyTableDefineCheckBox.get(),
					GetNotifyTableDefineCheckBox.SORT_COLUMN_INDEX,
					GetNotifyTableDefineCheckBox.SORT_ORDER);
		} else {
			this.tableViewer.createTableColumn(GetNotifyTableDefineNoCheckBox.get(),
					GetNotifyTableDefineNoCheckBox.SORT_COLUMN_INDEX1,
					GetNotifyTableDefineNoCheckBox.SORT_COLUMN_INDEX2,
					GetNotifyTableDefineNoCheckBox.SORT_ORDER);
			// 列移動が可能に設定（ビューのみ列移動可能で、監視から選択可能な通知一覧は列移動不可）
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumn(i).setMoveable(true);
			}

			// ダブルクリックリスナの追加
			this.tableViewer.addDoubleClickListener(new NotifyDoubleClickListener(this));
		}
		if(this.isSelect){
			/** チェックボックスの選択を制御するリスナー */
			SelectionAdapter adapter =
					new CheckBoxSelectionAdapter(this, this.tableViewer, GetNotifyTableDefineCheckBox.SELECTION);
			table.addSelectionListener(adapter);
		} else {
			// 合計ラベルの作成
			this.totalLabel = new Label(this, SWT.RIGHT);
			WidgetTestUtil.setTestId(this, "total", totalLabel);
			gridData = new GridData();
			gridData.horizontalAlignment = GridData.FILL;
			gridData.verticalAlignment = GridData.FILL;
			this.totalLabel.setLayoutData(gridData);
		}

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
	 * 通知一覧情報を取得し、テーブルビューアーにセットします。
	 *
	 * @see com.clustercontrol.notify.action.GetNotify#getNotifyList()
	 */
	@Override
	public void update() {

		// 通知一覧をマネージャから取得
		Map<String, List<NotifyInfoResponse>> dispDataMap = null;
		ArrayList<ArrayList<Object>> listInput = new ArrayList<ArrayList<Object>>();

		if (this.isSelect) {
			try {
				m_log.debug("update() : managerName=" + this.managerName + ", ownerRoleId=" + ownerRoleId);
				dispDataMap = new GetNotify().getNotifyListByOwnerRole(this.managerName, this.ownerRoleId);
			} catch (InvalidRole e) {
				m_log.debug("update() : isShowFlg is set to false");
				// 通知一覧のシステム権限（参照）がない場合
				setShowFlg(false);
				return;
			}

		} else {
			m_log.debug("update() : managerName=" + this.managerName);
			dispDataMap = new GetNotify().getNotifyList(this.managerName);
		}

		for(Map.Entry<String, List<NotifyInfoResponse>> entrySet : dispDataMap.entrySet()) {
			List<NotifyInfoResponse> list = null;
			list = entrySet.getValue();
			if(list == null)
			{
				//通知一覧に設定されている通知がない場合
				list = new ArrayList<NotifyInfoResponse>();
			}
			for(NotifyInfoResponse info : list){
				ArrayList<Object> a = new ArrayList<Object>();
				if (this.isSelect) {
					a.add(false);
					//a.add(entrySet.getKey());
					a.add(info.getValidFlg());
					a.add(info.getNotifyId());
					a.add(info.getDescription());
					a.add(NotifyTypeUtil.enumToType(info.getNotifyType(), NotifyTypeEnum.class));
				} else {
					a.add(entrySet.getKey());
					a.add(info.getNotifyId());
					a.add(info.getDescription());
					a.add(NotifyTypeUtil.enumToType(info.getNotifyType(), NotifyTypeEnum.class));
					a.add(info.getValidFlg());
				}
				a.add(info.getOwnerRoleId());
				a.add(info.getCalendarId());
				a.add(info.getRegUser());
				a.add(info.getRegDate());
				a.add(info.getUpdateUser());
				a.add(info.getUpdateDate());
				a.add(null);
				listInput.add(a);
			}
			if(notify == null){
				//変更でない場合などはnotifyを初期化する。
				notify = new ArrayList<>();
			}

			if(isSelect){
				//素の通知一覧ではなく、監視から選択可能な通知一覧の場合
				String tableNotifyId;
				boolean flg = false;

				//変更の通知を通知一覧のチェックボックスに表示させる。
				Iterator<NotifyRelationInfoResponse> it = notify.iterator();
				NotifyRelationInfoResponse nri = null ;
				ArrayList<NotifyRelationInfoResponse> removeNotifyList = new ArrayList<>();

				while(it.hasNext()){
					nri = it.next();
					flg = false;

					for(int i = 0; i < list.size(); i++){
						NotifyInfoResponse info = list.get(i);
						ArrayList<Object> objList = listInput.get(i);
						tableNotifyId = info.getNotifyId();

						if(tableNotifyId.equals(nri.getNotifyId())){
							objList.set(GetNotifyTableDefineCheckBox.SELECTION, true);
							flg=true;
						}
						objList.set(GetNotifyTableDefineCheckBox.VALID_FLG, info.getValidFlg());
						objList.set(GetNotifyTableDefineCheckBox.NOTIFY_ID, info.getNotifyId());
						objList.set(GetNotifyTableDefineCheckBox.DESCRIPTION, info.getDescription());
						objList.set(GetNotifyTableDefineCheckBox.NOTIFY_TYPE, NotifyTypeUtil.enumToType(info.getNotifyType(), NotifyTypeEnum.class));
						objList.set(GetNotifyTableDefineCheckBox.OWNER_ROLE, info.getOwnerRoleId());
						objList.set(GetNotifyTableDefineCheckBox.CALENDAR_ID, info.getCalendarId());

						objList.set(GetNotifyTableDefineCheckBox.CREATE_USER, info.getRegUser());
						objList.set(GetNotifyTableDefineCheckBox.CREATE_TIME, info.getRegDate());
						objList.set(GetNotifyTableDefineCheckBox.UPDATE_USER, info.getUpdateUser());
						objList.set(GetNotifyTableDefineCheckBox.UPDATE_TIME, info.getUpdateDate());
						objList.set(GetNotifyTableDefineCheckBox.DUMMY, null);
						listInput.set(i, objList);
					}

					if(!flg){
						//通知一覧になければ、設定済み通知からも削除
						removeNotifyList.add(nri);
					}
				}
				notify.removeAll(removeNotifyList);

			} else {
				// 合計欄更新
				String[] args = { String.valueOf(listInput.size()) };
				String message = null;
				if (this.condition == null) {
					message = Messages.getString("records", args);
				} else {
					message = Messages.getString("filtered.records", args);
				}
				this.totalLabel.setText(message);
			}
		}

		// テーブル更新
		this.tableViewer.setInput(listInput);
	}


	/**
	 * 監視に設定されている通知情報をセットします。
	 *
	 * @param notify
	 */
	public void setSelectNotify(List<NotifyRelationInfoResponse> notify){

		if(notify != null){
			this.notify =  new ArrayList<>();
		}
		this.notify = notify;
	}

	/**
	 * 監視に設定する通知情報を返します。
	 *
	 * @return 通知情報のコレクション
	 */
	public List<NotifyRelationInfoResponse> getSelectNotify(){
		return notify;
	}

	/**
	 * OKが押されたときに監視に設定する通知情報を
	 * 生成します。
	 *
	 */
	public boolean makeNotifyData(){

		TableItem[] ti = this.tableViewer.getTable().getItems();
		ArrayList<?> al;
		Collection<NotifyRelationInfoResponse> ct = new ArrayList<>();

		if (this.isSelect) {
			for (int i = 0; i<ti.length; i++){
				al = (ArrayList<?>)ti[i].getData();
				WidgetTestUtil.setTestId(this, "tableitem" + i, ti[i]);

				if((Boolean)al.get(GetNotifyTableDefineCheckBox.SELECTION)){

					NotifyRelationInfoResponse nri = new NotifyRelationInfoResponse();
					nri.setNotifyId((String)al.get(GetNotifyTableDefineCheckBox.NOTIFY_ID));
					nri.setNotifyType(NotifyTypeUtil.typeToEnum(
							(Integer)al.get(GetNotifyTableDefineCheckBox.NOTIFY_TYPE), NotifyRelationInfoResponse.NotifyTypeEnum.class));

					ct.add(nri);
				}
			}
		} else {
			for (int i = 0; i<ti.length; i++){
				al = (ArrayList<?>)ti[i].getData();
			}
		}

		// 通知情報の設定
		if (notify != null) {
			notify.clear();
			notify.addAll(ct);
		}
		return true;

	}

	/**
	 * 選択された行の通知IDを取得する
	 *
	 * @return
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
					String notifyId = (String)info.get(GetNotifyTableDefineCheckBox.NOTIFY_ID);
					data.add(notifyId);
				}
			}
		}

		return data;
	}

	public boolean isShowFlg() {
		return showFlg;
	}

	private void setShowFlg(boolean showFlg) {
		this.showFlg = showFlg;
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
