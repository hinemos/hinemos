/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.calendar.action.GetCalendarDetailTableDefine;
import com.clustercontrol.calendar.bean.DayOfWeekInMonthConstant;
import com.clustercontrol.calendar.bean.MonthConstant;
import com.clustercontrol.calendar.bean.OperateConstant;
import com.clustercontrol.calendar.dialog.CalendarDetailDialog;
import com.clustercontrol.calendar.util.CalendarEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimeStringConverter;
import com.clustercontrol.util.WidgetTestUtil;
import com.clustercontrol.viewer.CommonTableViewer;
import com.clustercontrol.ws.calendar.CalendarDetailInfo;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.InvalidRole_Exception;

/**
 * カレンダ詳細情報一覧コンポジットクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarDetailListComposite extends Composite {
	// ログ
	private static Log m_log = LogFactory.getLog( CalendarDetailListComposite.class );

	/** テーブルビューアー。 */
	private CommonTableViewer m_tableViewer = null;
	/** カレンダ詳細情報一覧 */
	private ArrayList<CalendarDetailInfo> detailList = null;
	/** オーナーロールID */
	private String m_ownerRoleId = null;
	/** マネージャ名 */
	private String m_managerName = null;

	/**
	 * @return the m_managerName
	 */
	public String getManagerName() {
		return m_managerName;
	}

	public void setManagerName(String managerName) {
		this.m_managerName = managerName;
	}
	
	/**
	 *
	 * @return
	 */
	public ArrayList<CalendarDetailInfo> getDetailList(){
		return this.detailList;
	}
	/**
	 *
	 * @return
	 */
	public String getOwnerRoleId() {
		return m_ownerRoleId;
	}
	/**
	 *
	 * @param ownerRoleId
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.m_ownerRoleId = ownerRoleId;
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param managerName マネージャ名
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public CalendarDetailListComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.m_managerName = managerName;
		this.initialize();
	}

	/**
	 * テーブル選択項目のの優先度を上げる
	 */
	public void up() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();//.firstElement;
		ArrayList<?> list =  (ArrayList<?>) selection.getFirstElement();
		//選択したテーブル行番号を取得
		Integer order = (Integer) list.get(0);
		List<CalendarDetailInfo> detailList = this.detailList;

		//orderは、テーブルカラム番号のため、1 ～ n listから値を取得する際は、 order - 1
		order = order-1;
		if(order > 0){
			CalendarDetailInfo a = detailList.get(order);
			CalendarDetailInfo b = detailList.get(order-1);
			detailList.set(order, b);
			detailList.set(order-1, a);
		}
		update();
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(order - 1);
	}
	/**
	 * テーブル選択項目の優先度を下げる
	 */
	public void down() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();//.firstElement;
		ArrayList<?> list =  (ArrayList<?>) selection.getFirstElement();
		//選択したテーブル行番号を取得
		Integer order = (Integer) list.get(0);
		List<CalendarDetailInfo> detailList = this.detailList;
		//list内 order+1 の値を取得するため、
		if(order < detailList.size()){
			//orderは、テーブルカラム番号のため、1 ～ n listから値を取得する際は、 order - 1
			order = order - 1;
			CalendarDetailInfo a = detailList.get(order);
			CalendarDetailInfo b = detailList.get(order + 1);
			detailList.set(order, b);
			detailList.set(order+1, a);
		}
		update();
		//更新後に再度選択項目にフォーカスをあてる
		selectItem(order + 1);
	}
	/**
	 * 引数で指定された判定情報の行を選択状態にします。
	 *
	 * @param identifier 識別キー
	 */
	private void selectItem(Integer order) {
		Table calDetailListSelectItemTable = m_tableViewer.getTable();
		TableItem[] items = calDetailListSelectItemTable.getItems();

		if (items == null || order == null) {
			return;
		}
		calDetailListSelectItemTable.select(order);
		return;
	}
	/**
	 * コンポジットを配置します。
	 */
	private void initialize() {
		/*
		 * カレンダ詳細初期化
		 */
		//this.detailList = new ArrayList<CalendarDetailInfo>();

		GridLayout layout = new GridLayout(1, true);
		this.setLayout(layout);
		layout.marginHeight = 0;
		layout.marginWidth = 0;

		Table calDetialListTable = new Table(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		WidgetTestUtil.setTestId(this, null, calDetialListTable);
		calDetialListTable.setHeaderVisible(true);
		calDetialListTable.setLinesVisible(true);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		calDetialListTable.setLayoutData(gridData);

		// テーブルビューアの作成
		m_tableViewer = new CommonTableViewer(calDetialListTable);
		m_tableViewer.createTableColumn(GetCalendarDetailTableDefine.get(),
				GetCalendarDetailTableDefine.SORT_COLUMN_INDEX,
				GetCalendarDetailTableDefine.SORT_ORDER);
		m_tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Integer order = getSelection();
				List<CalendarDetailInfo> detailList = getDetailList();
				if (order != null) {
					// シェルを取得
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

					//FIXME
					//CalendarDetailDialog dialog = new CalendarDetailDialog(shell,detailList.get(order - 1), m_calendarInfo.getOwnerRoleId());
					CalendarDetailDialog dialog = new CalendarDetailDialog(shell, m_managerName, detailList.get(order - 1), m_ownerRoleId);
					if (dialog.open() == IDialogConstants.OK_ID) {
						detailList.remove(order - 1);
						detailList.add(order - 1,dialog.getInputData());
						setSelection();
					}
				}
			}
		});
	}
	/**
	 * 選択したテーブル行番号を返す。
	 *
	 */
	public Integer getSelection() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();
		if (selection.getFirstElement() instanceof ArrayList) {
			ArrayList<?> list = (ArrayList<?>)selection.getFirstElement();
			if (list.get(0) instanceof Integer) {
				return (Integer)list.get(0);
			}
		}
		return null;
	}

	public void setSelection() {
		Table calDetailListSetSelectionTable = m_tableViewer.getTable();
		WidgetTestUtil.setTestId(this, null, calDetailListSetSelectionTable);
		int selectIndex = calDetailListSetSelectionTable.getSelectionIndex();
		update();
		calDetailListSetSelectionTable.setSelection(selectIndex);
	}
	/**
	 * 現在選択されているアイテムを返します。
	 * <p>
	 * 選択されていない場合は、<code>null</code>を返します。
	 *
	 * @return 選択アイテム
	 */
	public CalendarDetailInfo getFilterItem() {
		StructuredSelection selection = (StructuredSelection) m_tableViewer.getSelection();

		if (selection == null) {
			return null;
		} else {
			return (CalendarDetailInfo) selection.getFirstElement();
		}
	}

	/**
	 * 引数で指定されたカレンダ詳細情報をコンポジット内リストに反映させる
	 * @param detailList
	 */
	public void setDetailList(ArrayList<CalendarDetailInfo> detailList){
		if (detailList != null) {
			this.detailList = detailList;
			this.update();
		}
	}
	/**
	 * コンポジットを更新します。<BR>
	 * カレンダ詳細情報一覧を取得し、テーブルビューアーにセットします。
	 */
	@Override
	public void update() {
		// テーブル更新
		ArrayList<Object> listAll = new ArrayList<Object>();
		int i = 1;
		for (CalendarDetailInfo detail : getDetailList()) {
			ArrayList<Object> list = new ArrayList<Object>();
			String ruleMonthDay = "";
			//規則（日程）表示項目設定
			if(detail.getYear() != null){
				if(detail.getYear() == 0){
					ruleMonthDay = Messages.getString("calendar.detail.every.year");
				}else{
					ruleMonthDay = detail.getYear() + Messages.getString("year");
				}
			}
			if(detail.getMonth() != null && detail.getMonth() >= 0){
				ruleMonthDay = ruleMonthDay + MonthConstant.typeToString(detail.getMonth());
			}
			switch(detail.getDayType()){
			case 0:
				ruleMonthDay = ruleMonthDay + Messages.getString("calendar.detail.everyday");
				break;
			case 1:
				if(detail.getDayOfWeekInMonth() != null && detail.getDayOfWeekInMonth() >= 0){
					ruleMonthDay = ruleMonthDay + " " + DayOfWeekInMonthConstant.typeToString(detail.getDayOfWeekInMonth());
				}
				if(detail.getDayOfWeek() != null && detail.getDayOfWeek() > 0){
					ruleMonthDay = ruleMonthDay + DayOfWeekConstant.typeToString(detail.getDayOfWeek());
				}
				break;
			case 2:
				if(detail.getDate() != null && detail.getDate() > 0){
					ruleMonthDay = ruleMonthDay + " " + detail.getDate() + Messages.getString("monthday");
				}
				break;
			case 3:
				if(detail.getCalPatternId() != null && detail.getCalPatternId().length() > 0){
					ruleMonthDay = ruleMonthDay + " " + getCalendarPatternName(detail.getCalPatternId());
				}
				break;
			default: // 対応しない。
				break;
			}
			// [すべての日]は前後日を出力しない
			if(detail.getAfterday() != null && detail.getAfterday() != 0 && detail.getDayType() != 0){
				if(detail.getAfterday() > 0){
					ruleMonthDay = ruleMonthDay + " " + detail.getAfterday() + Messages.getString("calendar.detail.after.2");
				}else if (detail.getAfterday() < 0){
					ruleMonthDay = ruleMonthDay + " " + Math.abs(detail.getAfterday()) + Messages.getString("calendar.detail.after.3");
				}
			}

			//規則（時間）表示項目設定
			String strFrom = "";
			String strTo = "";
			
			//表示形式を0時未満および24時(及び48時)超にも対応するよう変換する
			// 変換例
			// 48:00～：50:00を02:00と表示されないよう48加算する
			// 24:00～：26:00を02:00と表示されないよう24加算する
			// 00:00～：変換不要
			// 0時未満の場合
			// 前日の23:45は-00:15と表示する
			// 前々日の22:00は-26:00と表示する
			if(detail.getTimeFrom() != null){
				strFrom = TimeStringConverter.formatTime(new Date(detail.getTimeFrom()));
			}
			if(detail.getTimeTo() != null){
				strTo = TimeStringConverter.formatTime(new Date(detail.getTimeTo()));
			}
			String ruleTime = "";
			ruleTime = strFrom + " - " + strTo;

			//順序
			list.add(i);
			//規則（日程）
			list.add(ruleMonthDay);
			//規則（時間）
			list.add(ruleTime);

			//稼動・非稼動
			if (detail.isOperateFlg() != null) {
				if(detail.isOperateFlg()){
					list.add(OperateConstant.typeToString(1));
				}else {
					list.add(OperateConstant.typeToString(0));
				}
			}
			list.add(detail.getDescription());
			listAll.add(list);
			i++;
		}
		m_tableViewer.setInput(listAll);
	}

	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.m_tableViewer.getTable().setEnabled(enabled);
	}

	/**
	 * カレンダパターン名を取得する。
	 *
	 * @param id カレンダパターンID
	 * @return カレンダパターン名
	 */
	private String getCalendarPatternName(String id){
		CalendarPatternInfo info = null;
		//カレンダパターン情報取得
		try {
			CalendarEndpointWrapper wrapper = CalendarEndpointWrapper.getWrapper(this.m_managerName);
			info = wrapper.getCalendarPattern(id);
		} catch (InvalidRole_Exception e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			return null;
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + HinemosMessage.replace(e.getMessage()), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			return null;
		}
		return info.getCalPatternName();
	}

}
