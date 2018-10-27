/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import com.clustercontrol.bean.PropertyDefineConstant;
import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.calendar.action.AddCalendar;
import com.clustercontrol.calendar.action.GetCalendar;
import com.clustercontrol.calendar.action.ModifyCalendar;
import com.clustercontrol.composite.ManagerListComposite;
import com.clustercontrol.composite.RoleIdListComposite;
import com.clustercontrol.composite.RoleIdListComposite.Mode;
import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.calendar.CalendarPatternInfo;
import com.clustercontrol.ws.calendar.Ymd;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * カレンダ[カレンダパターンの作成・変更]ダイアログクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarPatternDialog extends CommonDialog {
	/** */
	private Shell shell = null;
	/** カレンダコンポジット*/
	private SWTCalendar calPatternCalendar = null;
	/** ID*/
	private Text calPatternIdText = null;
	/** 名*/
	private Text calPatternNameText = null;
	/** 選択した日付を表示するSWTリスト*/
	private List calPatternList = null;

	/** 選択した日付を保持するリスト*/
	private ArrayList<Ymd> m_ymdList;
	/** */
	private CalendarPatternInfo inputData = null;
	/** ID*/
	private String id = "";
	/** 作成 or 変更*/
	private int mode;

	/** オーナーロールID用テキスト */
	private RoleIdListComposite calPatternRoleIdListComposite = null;
	/** マネージャ名 */
	private String managerName = null;
	/** マネージャ名コンボボックス用コンポジット */
	private ManagerListComposite m_managerComposite = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @since 1.0.0
	 */
	public CalendarPatternDialog(Shell parent, String managerName, String id,int mode) {
		super(parent);

		this.managerName = managerName;
		this.id = id;
		this.mode = mode;
	}

	/**
	 * ダイアログ作成
	 *
	 * @see com.clustercontrol.dialog.CommonDialog#customizeDialog(org.eclipse.swt.widgets.Composite)
	 * @since 1.0.0
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		shell = this.getShell();

		// タイトル
		shell.setText(Messages.getString("dialog.calendar.pattern.create.modify"));

		// ラベル
		GridData gridData = new GridData();
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 20;
		layout.marginHeight = 10;
		layout.numColumns = 12;
		parent.setLayout(layout);

		/*
		 * マネージャ
		 */
		Label labelManager = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "manager", labelManager);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelManager.setLayoutData(gridData);
		labelManager.setText(Messages.getString("facility.manager") + " : ");
		if(this.mode == PropertyDefineConstant.MODE_MODIFY){
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, false);
		} else {
			this.m_managerComposite = new ManagerListComposite(parent, SWT.NONE, true);
			this.m_managerComposite.getComboManagerName().addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String managerName = m_managerComposite.getText();
					calPatternRoleIdListComposite.createRoleIdList(managerName);
				}
			});
		}
		WidgetTestUtil.setTestId(this, "managerComposite", this.m_managerComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.m_managerComposite.setLayoutData(gridData);

		if(this.managerName != null) {
			this.m_managerComposite.setText(this.managerName);
		}

		Label label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("");

		/*
		 * カレンダパターンID
		 */
		//ラベル
		Label lblId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "id", lblId);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblId.setLayoutData(gridData);
		lblId.setText(Messages.getString("calendar.pattern.id") + " : ");
		//テキスト
		calPatternIdText = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "id", calPatternIdText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calPatternIdText.setLayoutData(gridData);
		calPatternIdText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("");

		/*
		 * カレンダパターン名
		 */
		//ラベル
		Label lblName = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "name", lblName);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		lblName.setLayoutData(gridData);
		lblName.setText(Messages.getString("calendar.pattern.name") + " : ");
		//テキスト
		calPatternNameText = new Text(parent, SWT.BORDER);
		WidgetTestUtil.setTestId(this, "name", calPatternNameText);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		calPatternNameText.setLayoutData(gridData);
		calPatternNameText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});

		label = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, label);
		gridData = new GridData();
		gridData.horizontalSpan = 3;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
		label.setText("");

		/*
		 * オーナーロールID
		 */
		Label labelRoleId = new Label(parent, SWT.LEFT);
		WidgetTestUtil.setTestId(this, "roleid", labelRoleId);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		labelRoleId.setLayoutData(gridData);
		labelRoleId.setText(Messages.getString("owner.role.id") + " : ");
		if (this.mode == PropertyDefineConstant.MODE_ADD
				|| this.mode == PropertyDefineConstant.MODE_COPY) {
			this.calPatternRoleIdListComposite = new RoleIdListComposite(
					parent, SWT.NONE, this.managerName, true, Mode.OWNER_ROLE);
		} else {
			this.calPatternRoleIdListComposite = new RoleIdListComposite(
					parent, SWT.NONE, this.managerName, false, Mode.OWNER_ROLE);
		}
		WidgetTestUtil.setTestId(this, "roleidlist", calPatternRoleIdListComposite);
		gridData = new GridData();
		gridData.horizontalSpan = 8;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.widthHint = 200;
		gridData.grabExcessHorizontalSpace = true;
		calPatternRoleIdListComposite.setLayoutData(gridData);

		/*
		 * 例外日設定グループ
		 */
		//
		Group calPatternEtcGroup = new Group(parent, SWT.CENTER);
		WidgetTestUtil.setTestId(this, "etc", calPatternEtcGroup);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 12;
		layout = new GridLayout(12, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;

		calPatternEtcGroup.setLayout(layout);
		calPatternEtcGroup.setLayoutData(gridData);
		calPatternEtcGroup.setText(Messages.getString("calendar.pattern.setting"));
		calPatternCalendar = new SWTCalendar(calPatternEtcGroup);
		WidgetTestUtil.setTestId(this, null, calPatternCalendar);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		gridData.verticalSpan = 2;

		calPatternCalendar.setLayoutData(gridData);
		//カレンダー日付変更時リスナー
		calPatternCalendar.addSWTCalendarListener(new SWTCalendarListener(){

			@Override
			public void dateChanged(SWTCalendarEvent event) {
				Date dateBuffer = event.getCalendar().getTime();

				//取得した日付をYmd型へ変換
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
				//カレンダパターンの場合、タイムゾーンは反映しない(休日データ等はタイムゾーンによって日付前後させない)
				String selectDate = sdf.format(dateBuffer);
				String[] selectYmd = selectDate.split("/");
				Ymd selectDay = new Ymd();
				selectDay.setYear(Integer.parseInt(selectYmd[0]));
				selectDay.setMonth(Integer.parseInt(selectYmd[1]));
				selectDay.setDay(Integer.parseInt(selectYmd[2]));

				//登録の有無を確認する
				Boolean flag = false;
				Ymd delYMD = new Ymd();
				for(Ymd ymd : m_ymdList){
					if(equalsYmd(ymd,selectDay)){
						flag = true;
						delYMD = ymd;
						break;
					}
				}
				if(flag){
					//登録済みTRUE の場合、リストから削除
					m_ymdList.remove(delYMD);
				}else {
					//未登録FALSE の場合、リストに追加
					m_ymdList.add(selectDay);
				}
				//昇順ソート
				Collections.sort(m_ymdList, new Comparator<Ymd>(){
					@Override
					public int compare(Ymd y1, Ymd y2) {
						Calendar ymd1 = Calendar.getInstance();
						ymd1.set(y1.getYear(), y1.getMonth() - 1, y1.getDay());
						Calendar ymd2 = Calendar.getInstance();
						ymd2.set(y2.getYear(), y2.getMonth() - 1, y2.getDay());
						return ymd1.getTime().compareTo(ymd2.getTime());
					}
				});
				//表示用SWTリストリセット
				calPatternList.removeAll();
				for(Ymd ymd : m_ymdList){
					//表示用リストに格納
					calPatternList.add(yyyyMMdd(ymd));
				}
				update();
			}
		});
		calPatternCalendar.updateCalendar(m_ymdList);
		//ラベル
		Label lbl = new Label(calPatternEtcGroup, SWT.CENTER);
		WidgetTestUtil.setTestId(this, null, lbl);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.verticalSpan = 1;
		lbl.setLayoutData(gridData);
		lbl.setText(" " + Messages.getString("calendar.pattern.record.date"));
		calPatternList = new List(calPatternEtcGroup,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
		WidgetTestUtil.setTestId(this, null, calPatternList);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 5;
		gridData.verticalSpan = 1;
		calPatternList.setLayoutData(gridData);
		// ダイアログを調整
		this.adjustDialog();
		//ダイアログに情報反映
		this.reflectCalendar();

		//更新
		update();
	}
	/**
	 * ダイアログエリアを調整します。
	 *
	 */
	private void adjustDialog(){
		// サイズを最適化
		// グリッドレイアウトを用いた場合、こうしないと横幅が画面いっぱいになります。
		shell.pack();
		shell.setSize(new Point(480, shell.getSize().y));

		// 画面中央に配置
		Display calPatternAdjustDisplay = shell.getDisplay();
		shell.setLocation((calPatternAdjustDisplay.getBounds().width - shell.getSize().x) / 2,
				(calPatternAdjustDisplay.getBounds().height - shell.getSize().y) / 2);
	}
	/**
	 * ２つのYmd型が等しいか判定
	 * @param y1
	 * @param y2
	 * @return 等しい true 等しくない false
	 */
	private boolean equalsYmd(Ymd y1, Ymd y2){
		if(y1.getYear().equals(y2.getYear())){
			if(y1.getMonth().equals(y2.getMonth())){
				if(y1.getDay().equals(y2.getDay())){
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Ymd(
	 * Integer year
	 * Integer month
	 * Integer day
	 * )をyyyy/MM/ddのString型へ変換する
	 * @param ymd
	 * @return
	 */
	private String yyyyMMdd(Ymd ymd){
		return ymd.getYear() + "/" + ymd.getMonth() + "/" + ymd.getDay();
	}

	/**
	 * 更新処理
	 *
	 */
	private void update(){
		// 必須項目を明示
		// IDのインデックス：9
		if("".equals(this.calPatternIdText.getText())){
			this.calPatternIdText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.calPatternIdText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		// 名のインデックス：9
		if("".equals(this.calPatternNameText.getText())){
			this.calPatternNameText.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.calPatternNameText.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
		//SWTカレンダ更新
		calPatternCalendar.updateCalendar(m_ymdList);
	}
	/**
	 * ダイアログにカレンダパターン情報を反映します。
	 */
	private void reflectCalendar() {
		// 初期表示
		if(mode == PropertyDefineConstant.MODE_MODIFY
				|| mode == PropertyDefineConstant.MODE_COPY){
			// 変更、コピーの場合、情報取得
			inputData = new GetCalendar().getCalendarPattern(this.managerName, this.id);
		}else{
			// 作成の場合
			inputData = new CalendarPatternInfo();
		}
		if (inputData == null)
			throw new InternalError("CalendarPatternInfo is null");

		this.m_ymdList = new ArrayList<Ymd>();
		//カレンダ[カレンダパターン]情報取得
		if (inputData.getCalPatternId() != null) {
			this.id = inputData.getCalPatternId();
			this.calPatternIdText.setText(inputData.getCalPatternId());
			//カレンダパターン定義変更の際にはカレンダパターンIDは変更不可
			if (this.mode == PropertyDefineConstant.MODE_MODIFY) {
				this.calPatternIdText.setEnabled(false);
			}
		}
		if(inputData.getCalPatternName() != null){
			this.calPatternNameText.setText(inputData.getCalPatternName());
		}
		if(inputData.getYmd() != null){
			for(Ymd ymd : inputData.getYmd()){
				m_ymdList = (ArrayList<Ymd>) inputData.getYmd();
				calPatternList.add(yyyyMMdd(ymd));
			}
		}
		else {
			m_ymdList = new ArrayList<Ymd>();
		}

		// オーナーロールID取得
		if (inputData.getOwnerRoleId() != null) {
			this.calPatternRoleIdListComposite.setText(inputData.getOwnerRoleId());
		}

		this.update();
	}
	/**
	 * ダイアログの情報からカレンダ情報を作成します。
	 * @return
	 */
	private void createCalendarIrregularInfo() {
		Long regDate = inputData.getRegDate();
		String regUser = inputData.getRegUser();

		inputData = new CalendarPatternInfo();
		//カレンダパターンID取得
		if(calPatternIdText.getText().length() > 0){
			inputData.setCalPatternId(calPatternIdText.getText());
		}
		//カレンダパターン名取得
		if(calPatternNameText.getText().length() > 0){
			inputData.setCalPatternName(calPatternNameText.getText());
		}
		//登録日取得
		if(m_ymdList == null){
			m_ymdList = new ArrayList<Ymd>();
		}
		for(Ymd ymd : m_ymdList){
			inputData.getYmd().add(ymd);
		}
		inputData.setRegDate(regDate);
		inputData.setRegUser(regUser);


		//オーナーロールID
		if (calPatternRoleIdListComposite.getText().length() > 0) {
			inputData.setOwnerRoleId(calPatternRoleIdListComposite.getText());
		}
	}

	@Override
	protected ValidateResult validate() {
		return null;
	}
	@Override
	protected boolean action() {
		boolean result = false;
		createCalendarIrregularInfo();
		CalendarPatternInfo info = this.inputData;
		String managerName = this.m_managerComposite.getText();
		if(info != null){
			if(mode == PropertyDefineConstant.MODE_ADD){
				// 作成の場合+
				info = this.inputData;
				result = new AddCalendar().addCalendarPatternInfo(managerName, info);
			} else if (mode == PropertyDefineConstant.MODE_MODIFY){
				// 変更の場合
				info.setCalPatternId(calPatternIdText.getText());
				result = new ModifyCalendar().modifyPatternInfo(managerName, info);
			} else if(mode == PropertyDefineConstant.MODE_COPY){
				// コピーの場合
				info.setCalPatternId(calPatternIdText.getText());
				result = new AddCalendar().addCalendarPatternInfo(managerName, info);
			}
		}
		return result;
	}
}
