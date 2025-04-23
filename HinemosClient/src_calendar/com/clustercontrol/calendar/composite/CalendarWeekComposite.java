/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.calendar.composite;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.openapitools.client.model.CalendarDetailInfoResponseP1;

import com.clustercontrol.bean.DateTimeFormatConstant;
import com.clustercontrol.bean.DayOfWeekConstant;
import com.clustercontrol.calendar.util.CalendarRestClientWrapper;
import com.clustercontrol.fault.CalendarNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.util.DateTimeStringConverter;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.TimezoneUtil;

/**
 * カレンダ[週間予定]ビューコンポジットクラス<BR>
 *
 * @version 4.1.0
 * @since 4.1.0
 */
public class CalendarWeekComposite extends Composite {

	// ログ
	private static Log m_log = LogFactory.getLog( CalendarWeekComposite.class );
	// バーの長さ 720(1日1440分の半分)
	private static final int MAX_BAR_LENGTH = 720;
	// バーの太さ
	private static final int BAR_HEIGHT = 15;
	// 左マージン
	private static final int LEFT_MARGIN = 200;
	// バーの縦の配置間隔
	private static final int BAR_SPAN = 40;
	//1日（ミリ秒）
	private static final long DAY24 = 24 * 60 * 60 * 1000;
	//図形を配置するキャンバス
	private FigureCanvas m_canvas;
	//図形を配置するパネル
	private Panel m_panel;
	//現在の月ラベルID
	private Label m_labelId = null;
	//現表示年月日スケジュールバーラベル配列
	private Label[] m_labelMatrix;

	//一週間分のスケジュールバー
	ScheduleBar[] scheduleBars = null;

	/**
	 * コンストラクタ
	 *
	 * @param parent
	 * @param style
	 * @since 2.0.0
	 */
	public CalendarWeekComposite(Composite parent, int style) {
		super(parent, style);
		initialize();
	}
	/**
	 * 初期化処理<BR>
	 *
	 * @since 4.1.0
	 */
	private void initialize() {
		// キャンバス表示コンポジットをparentの残り領域全体に拡張して表示
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.setLayoutData(gridData);

		// キャンバスコンポジット内のレイアウトを設定
		this.setLayout(new FillLayout());

		// 図を配置するキャンバスを生成
		m_canvas = new FigureCanvas(this, SWT.DOUBLE_BUFFERED);

		// 背景(bgimageが存在しない箇所)は白
		m_canvas.setBackground(ColorConstantsWrapper.white());

		// パネル作成
		createPanel();
	}
	
	/**
	 * 初期化処理<BR>
	 *
	 * @since 4.1.0
	 */
	private void createPanel() {
		if (m_panel != null) {
			m_panel.removeAll();
		}
		//パネル作成
		m_panel = new Panel();
		m_canvas.setContents(m_panel);
		m_panel.setLayoutManager(new XYLayout());
		
		//カレンダIDラベル
		m_labelId = new Label(Messages.getString("calendar.id") + " : ");
		m_labelId.setVisible(true);
		m_panel.add(m_labelId);
		Dimension dimension = new Dimension(-1, -1);
		Point point = new Point(0, 0);
		Rectangle zeroRectangle = new Rectangle(point, dimension);
		m_panel.setConstraint(m_labelId, zeroRectangle);
		//時間ラベル
		for (int i = 0; i < 25; i+=3){
			Label label = new Label(String.format("%02d:00", i));
			label.setVisible(true);
			m_panel.add(label);
			dimension = new Dimension(-1,-1);
			point = new Point(30 * i + 183, 20);
			zeroRectangle = new Rectangle(point, dimension);
			m_panel.setConstraint(label, zeroRectangle);
		}
		//表示年月日ラベル
		m_labelMatrix = new Label[7];
		for(int i = 0; i < m_labelMatrix.length; i++){
			m_labelMatrix[i] = new Label("----/--/--");
			m_labelMatrix[i].setVisible(true);
			m_panel.add(m_labelMatrix[i]);
			dimension = new Dimension(-1,-1);
			point = new Point(30, (i+1) * BAR_SPAN);
			zeroRectangle = new Rectangle(point, dimension);
			m_panel.setConstraint(m_labelMatrix[i], zeroRectangle);
		}
	}
	/**
	 * 更新処理<BR>
	 *
	 * @since 4.1.0
	 */
	public void update(String managerName, String calendarId,int year,int month,int day) {
		if (calendarId == null) {
			return;
		}
		// パネルを生成する
		createPanel();

		int period = 7;
		//指定年月日の曜日を取得 
		Calendar now = Calendar.getInstance(TimezoneUtil.getTimeZone());
		now.set(year, month-1, day);
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

		m_panel.repaint(); // Sometimes labels disappear in RAP. Repaint to make sure labels show		it 
		//スケジュールバー更新(指定された日を基準にソート)
		scheduleBars = createScheduleBars(m_panel,dayOfWeek);
		//稼動予定無しの場合
		for(ScheduleBar bar : scheduleBars){
			bar.getInitBar().setBackgroundColor(ColorConstantsWrapper.red());
		}
		try {
			//選択された年月日の稼動時間を取得
			CalendarRestClientWrapper wrapper = CalendarRestClientWrapper.getWrapper(managerName);
			List<CalendarDetailInfoResponseP1> detailList =
					wrapper.getCalendarWeek(calendarId, year, month, day);
			
			for(int i = 0; i < period; i++){
				
				m_log.trace("detailList.size=" + detailList.size() + ", " +
						year + "/" + month + "/" + day);
				
				year = now.get(Calendar.YEAR);
				month = now.get(Calendar.MONTH) + 1;
				day = now.get(Calendar.DAY_OF_MONTH);

				for(CalendarDetailInfoResponseP1 detail : detailList){
					//日付が一致するかどうかチェック 
					if(now.get(Calendar.DATE) == detail.getDayNo()){
						addScheduleBar(m_panel,dayOfWeek,detail.getStartTime(),detail.getEndTime(),detail.getExecuteFlg());
					}
				}
				m_labelMatrix[i].setText(String.format("%02d/%02d/%02d", year, month, day)
						+ " ( " + DayOfWeekConstant.typeToString(dayOfWeek) + " )");
				//指定年月日の曜日を再取得
				now.add(Calendar.DAY_OF_MONTH, 1);
				dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
			}
		} catch (InvalidRole e) {
			// 権限なし
			MessageDialog.openInformation(null, Messages.getString("message"),
					e.getMessage());
		} catch (CalendarNotFound e) {
			// カレンダを削除した際などは、ここを通る。
			m_log.info("update(), " + e.getMessage());
		} catch (Exception e) {
			// 上記以外の例外
			m_log.warn("update(), " + e.getMessage(), e);
			MessageDialog.openError(
					null,
					Messages.getString("failed"),
					Messages.getString(e.getMessage()));
		}
		//カレンダラベルの更新
		if(calendarId.length() > 0){
			m_labelId.setText(Messages.getString("calendar.id") + " : " + calendarId);
		}
		else{
			m_labelId.setText(Messages.getString("calendar.id") + " : ");
		}
	}

	/**
	 *
	 * スケジュールバー更新 : HourTypeが時間指定の場合
	 * 引数の曜日(day)をもとに、曜日のスケジュールバーを判別
	 * 空のスケジュールバーの色を変える
	 * さらに、新たにバーを生成し、空のスケジュールバーに被さるように配置する
	 * @param panel
	 * @param dayOfWeek
	 * @param fromStr
	 * @param toStr
	 */
	private void addScheduleBar(Panel panel,int dayOfWeek,String fromStr,String toStr, boolean operationFlg){
		m_log.trace("dayofweek:" + dayOfWeek + ", from:" + fromStr + ", to:" + toStr + ", operationflg:" + operationFlg);
		long from = DateTimeStringConverter.convertDateStringWithoutOffset(fromStr, DateTimeFormatConstant.COMMON_TIME);
		long to = DateTimeStringConverter.convertDateStringWithoutOffset(toStr, DateTimeFormatConstant.COMMON_TIME);

		int fromBar = (int) (MAX_BAR_LENGTH * ((float)from / DAY24));
		int toBar = (int) (MAX_BAR_LENGTH * ((float)to / DAY24));

		ScheduleBar scheduleBar = null;
		//スケジュールバーは、GUIカレンダの選択した日時が一番上に表示されるため、識別判定
		for(ScheduleBar bar : scheduleBars){
			if(bar.getDayOfWeek() == dayOfWeek){
				scheduleBar = bar;
			}
		}
		//現在更新処理を行っている日稼動時間を描画
		RectangleFigure barToday = new RectangleFigure();
		barToday.setOutline(false);
		if (operationFlg) {
			barToday.setBackgroundColor(ColorConstantsWrapper.green());
		} else {
			barToday.setBackgroundColor(ColorConstantsWrapper.red());
		}
		panel.add(barToday,new Rectangle(LEFT_MARGIN + fromBar, scheduleBar.getY(),
				toBar - fromBar, BAR_HEIGHT));
		
		addCheckPoint(from, scheduleBar.getY(), false);
		addCheckPoint(to, scheduleBar.getY(), true);
	}


	/**
	 * スケジュールバー
	 * 稼動・非稼動が切り替わる時刻を表示する
	 * @param key
	 * @param y
	 * @param endFlag
	 * @param chkPointlbl
	 */
	private void addCheckPoint(long key, int y, boolean endFlag) {
		long time = key;

		if (time == 0 || time == DAY24) {
			return;
		}
		//時間ラベル
		String strTime = DateTimeStringConverter.formatLongDateWithoutOffset(time, DateTimeFormatConstant.HR_MIN);

		Label label = new Label(strTime);
		label.setVisible(true);
		m_panel.add(label);
		Dimension dimension = new Dimension(-1,-1);
		int x = (int)(LEFT_MARGIN + time * MAX_BAR_LENGTH / DAY24);
		if (endFlag) {
			// x -= 55; // HH:mm:SS表示のときはこちら
			x -= 35;
		}
		Point point = new Point(x, y);
		Rectangle rectangle = new Rectangle(point, dimension);
		m_panel.setConstraint(label, rectangle);
	}

	/**
	 * GUIカレンダで選択した日にちから一週間分のスケジュールバーを作成する
	 * 選択した日にちが一番上に配置される
	 * @param panel
	 * @param dayOfWeek
	 * @return
	 */
	private ScheduleBar[] createScheduleBars(Panel panel,int dayOfWeek){
		ScheduleBar[] bars = new ScheduleBar[7];
		for(int i = 0; i < bars.length; i++){
			bars[i] = new ScheduleBar(dayOfWeek, LEFT_MARGIN, BAR_SPAN * (i + 1));
			if(dayOfWeek == 7){
				dayOfWeek = 1;
			}
			else {
				dayOfWeek++;
			}
			//白色を初期の色として設定
			bars[i].getInitBar().setBackgroundColor(ColorConstantsWrapper.white());
			//指定の座標に、縦BAR_HEIGHT,横MAX_BAR_LENGTHの四角形を作成
			panel.add(bars[i].getInitBar(),new Rectangle(bars[i].getX(), bars[i].getY(),
					MAX_BAR_LENGTH, BAR_HEIGHT));
		}
		return bars;
	}
}
/**
 * スケジュールバーを1本管理するクラス
 * スケジュールバーの曜日情報、座標を管理する
 *
 */
class ScheduleBar{
	//曜日スケジュールバー
	private RectangleFigure initBar;
	//x座標
	private int x;
	//y座標
	private int y;
	//曜日
	private int dayOfWeek;
	/**
	 * コンストラクタ
	 * @param initBar
	 * @param dayOfWeek
	 * @param x
	 * @param y
	 */
	public ScheduleBar(int dayOfWeek, int x, int y){
		this.initBar = new RectangleFigure();
		this.initBar.setOutline(false);
		this.dayOfWeek = dayOfWeek;
		this.x = x;
		this.y = y;
	}
	//曜日スケジュールバー
	public RectangleFigure getInitBar() {
		return initBar;
	}
	//曜日スケジュールバー
	public void setInitBar(RectangleFigure initBar) {
		this.initBar = initBar;
	}
	//x座標
	public int getX() {
		return x;
	}
	//x座標
	public void setX(int x) {
		this.x = x;
	}
	//y座標
	public int getY() {
		return y;
	}
	//y座標
	public void setY(int y) {
		this.y = y;
	}
	//曜日
	public int getDayOfWeek() {
		return dayOfWeek;
	}
	//曜日
	public void setDayOfWeek(int dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
}
