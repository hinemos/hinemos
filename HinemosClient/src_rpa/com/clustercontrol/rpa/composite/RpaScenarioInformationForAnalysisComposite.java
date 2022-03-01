/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.composite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.GetRpaScenarioResponse.ManualTimeCulcTypeEnum;
import org.openapitools.client.model.RpaScenarioTagResponse;

import com.clustercontrol.bean.SizeConstant;
import com.clustercontrol.dialog.DateTimeDialog;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.rpa.util.RpaRestClientWrapper;
import com.clustercontrol.rpa.util.RpaScenarioDialogUtil;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * 分析用情報タブ用のコンポジットクラスです。
 */
public class RpaScenarioInformationForAnalysisComposite extends Composite {
	/** ログ出力のインスタンス */
	private static Log log = LogFactory.getLog( RpaScenarioInformationForAnalysisComposite.class );
	
	/** 手動操作時間指定用ラジオボタン */
	private Button fixTimeParam = null;
	/** 手動操作時間指定用テキスト */
	private Text fixTimeParamText = null;
	/** 自動算出用ラジオボタン */
	private Button autoParam = null;
	/** タグ名一覧コンポジット */
	private RpaScenarioTagNameListComposite tagNameList = null;
	/** 運用開始日時用ボタン */
	private Button opeStartDateButton = null;
	/** 運用開始日時用テキスト */
	private Text opeStartDateText = null;
	/** シェル */
	private Shell shell = null;

	/** マネージャ名 */
	private String managerName = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize()
	 */
	public RpaScenarioInformationForAnalysisComposite(Composite parent, int style, String managerName) {
		super(parent, style);
		this.managerName = managerName;
		shell = this.getShell();
		initialize();
	}

	/**
	 * コンポジットを構築します。
	 */
	private void initialize() {

		this.setLayout(RpaScenarioDialogUtil.getParentLayout());

		/*
		 * シナリオタグ一覧
		 */
		Composite tagNameListComposite = new Composite(this, SWT.NONE);
		tagNameListComposite.setLayout(new GridLayout(3, false));
		// ラベル
		Label labelScenarioTag = new Label(tagNameListComposite, SWT.CENTER);
		labelScenarioTag.setText(Messages.getString("rpa.tag") + " : ");
		GridData gd_labelScenarioTag = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_labelScenarioTag.horizontalSpan = 2;
		labelScenarioTag.setLayoutData(gd_labelScenarioTag);
		// コンポジット
		this.tagNameList = new RpaScenarioTagNameListComposite(tagNameListComposite, SWT.CENTER);
		this.tagNameList.setManagerName(this.managerName);

		/*
		 * 運用開始日時
		 */
		Composite startDateComposite = new Composite(this, SWT.NONE);
		startDateComposite.setLayout(new GridLayout(4, false));
		// ラベル
		Label labelStartDate = new Label(startDateComposite, SWT.LEFT);
		GridData gd_labelStartDate = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gd_labelStartDate.horizontalSpan = 2;
		labelStartDate.setLayoutData(gd_labelStartDate);
		labelStartDate.setText(Messages.getString("rpa.scenario.ope.start.date") + " : ");
		// テキスト
		opeStartDateText = new Text(startDateComposite, SWT.BORDER | SWT.LEFT);
		opeStartDateText.setLayoutData(new GridData(160, SizeConstant.SIZE_TEXT_HEIGHT));
		// 日時ダイアログからの入力しか受け付けません
		opeStartDateText.setEnabled(false);
		opeStartDateText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		// 追加ボタン
		opeStartDateButton = new Button(startDateComposite, SWT.NONE);
		opeStartDateButton.setLayoutData(new GridData(40, SizeConstant.SIZE_BUTTON_HEIGHT + 5));
		opeStartDateButton.setText(Messages.getString("calendar.button"));
		opeStartDateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DateTimeDialog dialog = new DateTimeDialog(shell);
				if (opeStartDateText.getText().length() > 0) {
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					try {
						Date date = sdf.parse(opeStartDateText.getText());
						dialog.setDate(date);
					} catch (ParseException e1) {
						log.warn("opeStartDateText : " + e1.getMessage());
						
					}
				}
				if (dialog.open() == IDialogConstants.OK_ID) {
					// ダイアログより取得した日時を"yyyy/MM/dd HH:mm:ss"の形式に変換
					SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
					String tmp = sdf.format(dialog.getDate());
					opeStartDateText.setText(tmp);
					update();
				}
			}
		});

		/*
		 * 手動操作時間
		 */
		Composite manualTimeComposite = new Composite(this, SWT.NONE);
		manualTimeComposite.setLayout(new GridLayout(3, false));

		// ラベル
		Label labelManualTime = new Label(manualTimeComposite, SWT.LEFT);
		GridData grid = new GridData(100, SizeConstant.SIZE_LABEL_HEIGHT);
		grid.horizontalSpan = 3;
		labelManualTime.setLayoutData(grid);
		labelManualTime.setText(Messages.getString("rpa.scenario.manual.time") + " : ");
		
		// 自動算出する（ラジオ）
		this.autoParam = new Button(manualTimeComposite, SWT.RADIO);
		this.autoParam.setText(Messages.getString("rpa.scenario.manual.time.auto") + " : ");
		grid = new GridData(160, SizeConstant.SIZE_BUTTON_HEIGHT);
		grid.horizontalSpan = 3;
		this.autoParam.setLayoutData(grid);
		this.autoParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					fixTimeParam.setSelection(false);
					fixTimeParamText.setEditable(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		// 時間を指定する（ラジオ）
		this.fixTimeParam = new Button(manualTimeComposite, SWT.RADIO);
		this.fixTimeParam.setText(Messages.getString("rpa.scenario.manual.time.fix.time") + " : ");
		this.fixTimeParam.setLayoutData(new GridData(160, SizeConstant.SIZE_BUTTON_HEIGHT));
		this.fixTimeParam.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button check = (Button) e.getSource();
				if (check.getSelection()) {
					fixTimeParamText.setEditable(true);
					autoParam.setSelection(false);
				}
				update();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		
		// 手動操作時間（テキスト）
		this.fixTimeParamText = new Text(manualTimeComposite, SWT.BORDER);
		this.fixTimeParamText.setLayoutData(new GridData(200, SizeConstant.SIZE_TEXT_HEIGHT));
		this.fixTimeParamText.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
	}

	/**
	 * 分析用情報をコンポジットに反映します。
	 */
	public void reflectInfomation() {
		this.autoParam.setSelection(true);
		this.fixTimeParamText.setEditable(false);
	}

	/**
	 * シナリオタグ情報を設定します。
	 *
	 * @param tagList シナリオタグ情報
	 */
	public void setScenarioTagList(List<RpaScenarioTagResponse> tagList) {
		List<RpaScenarioTagResponse> list = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<String, String>();
		RpaRestClientWrapper wrapper = RpaRestClientWrapper.getWrapper(this.managerName);
		try {
			list = wrapper.getRpaScenarioTagList(null);
		} catch (InvalidRole e) {
			// 権限なし
			errorMsgs.put( this.managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			// 上記以外の例外
			String errMessage = HinemosMessage.replace(e.getMessage());
			log.warn("update(), " + errMessage, e);
			errorMsgs.put( this.managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + errMessage);
		}
		if(list == null){
			list = new ArrayList<RpaScenarioTagResponse>();
		}
		
		Map<String, String> tagNameMap = new HashMap<>();
		Map<String, String> tagPathMap = new HashMap<>();
		for (RpaScenarioTagResponse info : list) {
			tagNameMap.put(info.getTagId(), info.getTagName());
			tagPathMap.put(info.getTagId(), info.getTagPath());
		}
		this.tagNameList.setTagList(tagList, tagNameMap, tagPathMap);
	}

	/**
	 * シナリオタグ情報を返します。
	 *
	 * @return シナリオタグ情報
	 */
	public List<RpaScenarioTagResponse> getScenarioTagList() {
		return this.tagNameList.getTagList();
	}
	
	/**
	 * 運用開始日時情報を設定します。
	 *
	 * @param start 運用開始日時情報
	 */
	public void setOpeStartDate(Long start) {
		Date date = new Date(start);
		// "yyyy/MM/dd HH:mm:ss"の形式に変換
		SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
		String dateText = sdf.format(date);
		this.opeStartDateText.setText(dateText);
	}

	/**
	 * 運用開始日時情報を返します。
	 *
	 * @return 運用開始日時情報
	 */
	public Long getStartDate() {
		Date date = null;
		if (this.opeStartDateText.getText().length() > 0) {
			SimpleDateFormat sdf = TimezoneUtil.getSimpleDateFormat();
			try {
				date = sdf.parse(this.opeStartDateText.getText());
			} catch (ParseException e) {
				log.warn("opeStartDateText : " + e.getMessage());
			}
		}
		if (date != null){
			return date.getTime();
		} else {
			return null;
		}
	}

	/**
	 * 手動操作時間情報を設定します。
	 *
	 * @param start 手動操作時間情報
	 */
	public void setManualTime(ManualTimeCulcTypeEnum type, Long time) {
		if (type == ManualTimeCulcTypeEnum.AUTO) {
			this.autoParam.setSelection(true);
			this.fixTimeParam.setSelection(false);
			this.fixTimeParamText.setEditable(false);
		} else {
			this.autoParam.setSelection(false);
			this.fixTimeParam.setSelection(true);
			this.fixTimeParamText.setEditable(true);
			Long milliseconds = time / 1000;
			this.fixTimeParamText.setText(milliseconds.toString());
		}
		
	}

	/**
	 * 手動操作時間タイプ情報を返します。
	 *
	 * @return 手動操作時間タイプ情報
	 */
	public ManualTimeCulcTypeEnum getManualTimeCulcType() {
		if(this.autoParam.getSelection()){
			return ManualTimeCulcTypeEnum.AUTO;
		} else {
			return ManualTimeCulcTypeEnum.FIX_TIME;
		}
	}
	
	/**
	 * 手動操作時間情報を返します。
	 *
	 * @return 手動操作時間情報
	 */
	public String getManualTime() {
		if(this.autoParam.getSelection()){
			return null;
		} else {
			return this.fixTimeParamText.getText();
		}
	}

	/**
	 * 読み込み専用時にグレーアウトします。
	 */
	@Override
	public void setEnabled(boolean enabled) {
		this.autoParam.setEnabled(enabled);
		this.fixTimeParam.setEnabled(enabled);
		this.fixTimeParamText.setEnabled(enabled);
		this.tagNameList.setEnabled(enabled);
		this.opeStartDateButton.setEnabled(enabled);
		this.opeStartDateText.setEnabled(enabled);
	}
}
