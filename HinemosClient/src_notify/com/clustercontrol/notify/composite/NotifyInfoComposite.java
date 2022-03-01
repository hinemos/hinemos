/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.composite;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;

import com.clustercontrol.bean.RequiredFieldColorConstant;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.monitor.run.bean.MonitorNumericType;
import com.clustercontrol.notify.bean.PriChangeFailSelectTypeConstant;
import com.clustercontrol.notify.bean.PriChangeFailTypeMessage;
import com.clustercontrol.notify.bean.PriChangeJudgeSelectTypeConstant;
import com.clustercontrol.notify.bean.PriChangeJudgeTypeForPageMessage;
import com.clustercontrol.notify.bean.PriChangeJudgeTypeForPatternMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;


/**
 * アプリケーション付き通知ID一覧コンポジットクラス<BR>
 * <p>
 * <dl>
 *  <dt>コンポジット</dt>
 *  <dd>「通知ID」 ラベル（親）</dd>
 *  <dd>「通知ID一覧」 フィールド（親）</dd>
 *  <dd>「選択」 ボタン（親）</dd>
 *  <dd>「アプリケーション」 テキストボックス</dd>
 * </dl>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class NotifyInfoComposite extends NotifyIdListComposite {
	
	/** カラム数 */
	private static final int WIDTH = 15;

	/** カラム数（ラベル）。 */
	private static final int WIDTH_LABEL = 4;

	/** カラム数（コンボ）。 */
	private static final int WIDTH_COMBO = 8;
	
	
	/** アプリケーション ラベル。 */
	private Label labelApplication = null;

	/** アプリケーション ラベル文字列。 */
	private Text textApplication = null;
	
	/** 判定による重要度変化 選択コンボ。 */
	private Combo comboPriorityChangeJudge  = null;
	
	/** 取得失敗による重要度変化 選択コンボ。 */
	private Combo comboPriorityChangeFail  = null;

	/** アプリケーション に指定できる最小文字数 */
	private int minApplicationLen = 1;

	/** 入力値チェック用 */
	protected ValidateResult validateResult = null;

	/** 数値監視モード  */
	private String m_monitorNumericType = MonitorNumericType.TYPE_BASIC.getType();

	/** 判定による重要度変化 の選択タイプ  */
	private Integer priorityChangeJudgeSelect = null;

	/** 取得失敗による重要度変化 の選択タイプ  */
	private Integer priorityChangeFailSelect = null;


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
	 * @see #initialize(Composite)
	 */
	public NotifyInfoComposite(Composite parent, int style) {
		super(parent, style, true);

		this.initialize(parent);
	}

	public NotifyInfoComposite(Composite parent, int style, int notifyIdType) {
		super(parent, style, true, notifyIdType);

		this.initialize(parent);
	}

	public NotifyInfoComposite(Composite parent, int style, int notifyIdType, String monitorNumericType) {
		super(parent, style, true, notifyIdType);
		this.m_monitorNumericType = monitorNumericType;
		this.initialize(parent);
	}

	public NotifyInfoComposite(Composite parent, int style, Integer priorityChangeJudgeSelect,
			Integer priorityChangeFailSelect) {
		super(parent, style, true);
		this.priorityChangeJudgeSelect = priorityChangeJudgeSelect;
		this.priorityChangeFailSelect = priorityChangeFailSelect;
		this.initialize(parent);
	}

	public NotifyInfoComposite(Composite parent, int style, int notifyIdType, Integer priorityChangeJudgeSelect,
			Integer priorityChangeFailSelect) {
		super(parent, style, true, notifyIdType);
		this.priorityChangeJudgeSelect = priorityChangeJudgeSelect;
		this.priorityChangeFailSelect = priorityChangeFailSelect;
		this.initialize(parent);
	}

	/**
	 * インスタンスを返します。
	 * <p>
	 * 初期処理を呼び出し、コンポジットを配置します。
	 *
	 * @param parent 親のコンポジット
	 * @param style スタイル
	 * @param text アプリケーション ラベル文字列
	 *
	 * @see org.eclipse.swt.SWT
	 * @see org.eclipse.swt.widgets.Composite#Composite(Composite parent, int style)
	 * @see #initialize(Composite)
	 */
	public NotifyInfoComposite(Composite parent, int style, String text) {
		super(parent, style, text);

		this.initialize(parent);
	}

	/**
	 * コンポジットを配置します。
	 */
	private void initialize(Composite parent) {

		// 変数として利用されるグリッドデータ
		GridData gridData = null;
		Label label = null;

		GridLayout layout = new GridLayout(1, true);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 15;
		this.setLayout(layout);

		/*
		 * アプリケーションID
		 */
		// ラベル
		this.labelApplication = new Label(this, SWT.NONE);
		WidgetTestUtil.setTestId(this, "application", labelApplication);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.labelApplication.setLayoutData(gridData);
		this.labelApplication.setText(Messages.getString("application") + " : ");
		// テキスト
		this.textApplication = new Text(this, SWT.BORDER | SWT.LEFT);
		WidgetTestUtil.setTestId(this, null, textApplication);
		gridData = new GridData();
		gridData.horizontalSpan = 10;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.textApplication.setLayoutData(gridData);
		this.textApplication.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				update();
			}
		});
		/*
		 * 判定による重要度変化
		 */
		if(priorityChangeJudgeSelect != null){
			label = new Label(this, SWT.NONE);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH_LABEL;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("NotifyIdListComposite.priority.change.judge") + " : ");
			// コンボボックス
			this.comboPriorityChangeJudge = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH_COMBO;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboPriorityChangeJudge.setLayoutData(gridData);
			if (PriChangeJudgeSelectTypeConstant.TYPE_PATTERN == priorityChangeJudgeSelect) {
				this.comboPriorityChangeJudge.add(PriChangeJudgeTypeForPatternMessage.STRING_ACROSS_MONITOR_DETAIL_ID);
				this.comboPriorityChangeJudge.add(PriChangeJudgeTypeForPatternMessage.STRING_NOT_PRIORITY_CHANGE);
				this.comboPriorityChangeJudge.setText(PriChangeJudgeTypeForPatternMessage.STRING_ACROSS_MONITOR_DETAIL_ID);
			}
			if (PriChangeJudgeSelectTypeConstant.TYPE_PAGE == priorityChangeJudgeSelect) {
				this.comboPriorityChangeJudge.add(PriChangeJudgeTypeForPageMessage.STRING_ACROSS_MONITOR_DETAIL_ID);
				this.comboPriorityChangeJudge.add(PriChangeJudgeTypeForPageMessage.STRING_NOT_PRIORITY_CHANGE);
				this.comboPriorityChangeJudge.setText(PriChangeJudgeTypeForPageMessage.STRING_ACROSS_MONITOR_DETAIL_ID);
			}
			// 空白（レイアウトのパティング用）
			label = new Label(this, SWT.NONE);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH -( WIDTH_LABEL + WIDTH_COMBO) ;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}
		
		/*
		 * 取得失敗による重要度変化
		 */
		if(priorityChangeFailSelect != null){
			label = new Label(this, SWT.NONE);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH_LABEL;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
			label.setText(Messages.getString("NotifyIdListComposite.priority.change.fail") + " : ");
			// コンボボックス
			this.comboPriorityChangeFail = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH_COMBO;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			this.comboPriorityChangeFail.setLayoutData(gridData);
			this.comboPriorityChangeFail.add(PriChangeFailTypeMessage.STRING_PRIORITY_CHANGE);
			this.comboPriorityChangeFail.add(PriChangeFailTypeMessage.STRING_NOT_PRIORITY_CHANGE);
			this.comboPriorityChangeFail.setText(PriChangeFailTypeMessage.STRING_PRIORITY_CHANGE);
			// 空白（レイアウトのパティング用）
			label = new Label(this, SWT.NONE);
			gridData = new GridData();
			gridData.horizontalSpan = WIDTH -( WIDTH_LABEL + WIDTH_COMBO) ;
			gridData.horizontalAlignment = GridData.FILL;
			gridData.grabExcessHorizontalSpace = true;
			label.setLayoutData(gridData);
		}
		
		update();
	}

	/**
	 * コンポジットを更新します。<BR>
	 */
	@Override
	public void update() {
		// 必須入力項目を明示する
		if(this.textApplication.getEnabled() && this.textApplication.getText().length() < this.minApplicationLen){
			this.textApplication.setBackground(RequiredFieldColorConstant.COLOR_REQUIRED);
		}else{
			this.textApplication.setBackground(RequiredFieldColorConstant.COLOR_UNREQUIRED);
		}
	}


	/* (非 Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.textApplication.setEnabled(enabled);
		if (this.comboPriorityChangeJudge != null) {
			this.comboPriorityChangeJudge.setEnabled(enabled);
		}
		if (this.comboPriorityChangeFail != null) {
			this.comboPriorityChangeFail.setEnabled(enabled);
		}
		this.update();
	}

	/**
	 * アプリケーションを返します。
	 *
	 * @return アプリケーション
	 */
	public String getApplication() {

		return this.textApplication.getText();
	}

	/**
	 * アプリケーションを設定します。
	 */
	public void setApplication(String string) {
		this.textApplication.setText(string);
	}


	/**
	 * 判定による重要度変化タイプを返します。
	 *
	 * @return 判定による重要度変化タイプ
	 */
	public MonitorInfoResponse.PriorityChangeJudgmentTypeEnum getPriorityChangeJudgeType() {
		if(priorityChangeJudgeSelect != null ){
			if (PriChangeJudgeSelectTypeConstant.TYPE_PATTERN == priorityChangeJudgeSelect) {
				return PriChangeJudgeTypeForPatternMessage.stringToTypeEnum(this.comboPriorityChangeJudge.getText());
			}
			if (PriChangeJudgeSelectTypeConstant.TYPE_PAGE == priorityChangeJudgeSelect) {
				return PriChangeJudgeTypeForPageMessage.stringToTypeEnum(this.comboPriorityChangeJudge.getText());
			}
		}
		return null;
	}

	/**
	 * 判定による重要度変化タイプを設定します。
	 */
	public void setPriorityChangeJudgeType(MonitorInfoResponse.PriorityChangeJudgmentTypeEnum type) {
		if(priorityChangeJudgeSelect != null ){
			if (PriChangeJudgeSelectTypeConstant.TYPE_PATTERN == priorityChangeJudgeSelect) {
				this.comboPriorityChangeJudge.setText(PriChangeJudgeTypeForPatternMessage.typeEnumToString(type));
			}
			if (PriChangeJudgeSelectTypeConstant.TYPE_PAGE == priorityChangeJudgeSelect) {
				this.comboPriorityChangeJudge.setText(PriChangeJudgeTypeForPageMessage.typeEnumToString(type));
			}
		}
	}

	/**
	 * 取得失敗による重要度変化タイプを返します。
	 *
	 * @return 取得失敗による重要度変化タイプ
	 */
	public MonitorInfoResponse.PriorityChangeFailureTypeEnum getPriorityChangeFailType() {
		if (priorityChangeFailSelect != null &&  PriChangeFailSelectTypeConstant.TYPE_GET == priorityChangeFailSelect) {
			return PriChangeFailTypeMessage.stringToTypeEnum(this.comboPriorityChangeFail.getText());
		}
		return null;
	}

	/**
	 * 取得失敗による重要度変化タイプを設定します。
	 */
	public void setPriorityChangeFailType(MonitorInfoResponse.PriorityChangeFailureTypeEnum type) {
		if (priorityChangeFailSelect != null &&  PriChangeFailSelectTypeConstant.TYPE_GET == priorityChangeFailSelect) {
			this.comboPriorityChangeFail.setText(PriChangeFailTypeMessage.typeEnumToString(type));
		}
	}

	/**
	 * 引数で指定された監視情報に、入力値を設定します。
	 * <p>
	 * 入力値チェックを行い、不正な場合は認証結果を返します。
	 * 不正ではない場合は、<code>null</code>を返します。
	 *
	 * @param info	監視情報
	 * @return	検証結果
	 */
	public ValidateResult createInputData(MonitorInfoResponse info){

		this.validateResult = null;
		if(info != null){
			if(getNotify() != null && getNotify().size() != 0){
				//コンポジットから通知情報を取得します。
				List<NotifyRelationInfoResponse> monitorNotifyList = new ArrayList<>();
				for (NotifyRelationInfoResponse notify : getNotify()) {
					NotifyRelationInfoResponse monitorNotify = new NotifyRelationInfoResponse();
					monitorNotify.setNotifyId(notify.getNotifyId());
					monitorNotify.setNotifyType(notify.getNotifyType());
					monitorNotifyList.add(monitorNotify);
				}
				if (MonitorNumericType.TYPE_PREDICTION.getType().equals(m_monitorNumericType)) {
					info.setPredictionNotifyRelationList(new ArrayList<>());
					info.getPredictionNotifyRelationList().addAll(monitorNotifyList);
				} else if (MonitorNumericType.TYPE_CHANGE.getType().equals(m_monitorNumericType)) {
					info.setChangeNotifyRelationList(new ArrayList<>());
					info.getChangeNotifyRelationList().addAll(monitorNotifyList);
				} else {
					info.setNotifyRelationList(new ArrayList<>());
					info.getNotifyRelationList().addAll(monitorNotifyList);
				}
			}

			// アプリケーションの設定
			if(this.getApplication() != null && !this.getApplication().equals("")){
				if (MonitorNumericType.TYPE_PREDICTION.getType().equals(m_monitorNumericType)) {
					info.setPredictionApplication(this.getApplication());
				} else if (MonitorNumericType.TYPE_CHANGE.getType().equals(m_monitorNumericType)) {
					info.setChangeApplication(this.getApplication());
				} else {
					info.setApplication(this.getApplication());
				}
			}

			//重要度変化の選択
			info.setPriorityChangeJudgmentType(this.getPriorityChangeJudgeType());
			info.setPriorityChangeFailureType(this.getPriorityChangeFailType());

		}
		return this.validateResult;
	}

	/**
	 * 無効な入力値の情報を設定します。
	 *
	 * @param id ID
	 * @param message メッセージ
	 */
	protected void setValidateResult(String id, String message) {

		this.validateResult = new ValidateResult();
		this.validateResult.setValid(false);
		this.validateResult.setID(id);
		this.validateResult.setMessage(message);

	}

	/**
	 * アプリケーション に設定可能な最小文字数を設定します。
	 */
	public void setMinApplicationLen(int minApplicationLen) {
		this.minApplicationLen = minApplicationLen;
		update();
	}

}
