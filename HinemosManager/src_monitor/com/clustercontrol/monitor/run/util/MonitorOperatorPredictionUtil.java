/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.monitor.run.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.HinemosArithmeticException;
import com.clustercontrol.fault.HinemosIllegalArgumentException;
import com.clustercontrol.monitor.run.bean.MonitorPredictionMethodConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.run.util.MonitorCollectDataCache.MonitorCollectData;
import com.clustercontrol.util.HinemosTime;

/**
 * 将来予測監視の計算ユーティリティクラス<br/>
 * 
 * @since 6.1.0
 */
public class MonitorOperatorPredictionUtil {

	private static Log m_log = LogFactory.getLog(MonitorOperatorPredictionUtil.class);

	/**
	 * 「現在日時＋計測対象時間」時点の将来予測情報を取得する。
	 * 
	 * @param monitorInfo				監視設定
	 * @param facilityId				ファシリティID
	 * @param displayName				DisplayName
	 * @param itemName					ItemName
	 * @param targetDate				現在日時
	 * @return	将来予測情報
	 */
	public static MonitorPredictionDataInfo getPredictionInfo(
			MonitorInfo monitorInfo, String facilityId, String displayName, String itemName, Long targetDate) {

		MonitorPredictionDataInfo rtn = new MonitorPredictionDataInfo();

		// パラメータが設定されていない場合は処理終了
		if (targetDate == null
				|| monitorInfo == null
				|| monitorInfo.getMonitorId() == null
				|| monitorInfo.getPredictionTarget() == null
				|| facilityId == null
				|| facilityId.isEmpty()) {
			return rtn;
		}
		if (displayName == null) {
			displayName = "";
		}
		if (itemName == null) {
			itemName = "";
		}

		m_log.debug("getPredictionInfo start : "
				+ " targetDate=" + targetDate
				+ "monitorId=" + monitorInfo.getMonitorId()
				+ ", facilityId=" + facilityId
				+ ", displayName=" + displayName
				+ ", itemName=" + itemName);

		// 収集値を取得する
		List<MonitorCollectData> monitorCollectDataList 
			= MonitorCollectDataCache.getMonitorCollectDataList(
					monitorInfo.getMonitorId(), facilityId, displayName, itemName, targetDate, 
					monitorInfo.getPredictionAnalysysRange().doubleValue());

		// 値リストの作成
		if (monitorCollectDataList == null 
				|| monitorCollectDataList.isEmpty()) {
			return rtn;
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		sdf.setTimeZone(HinemosTime.getTimeZone());
		StringBuilder sb = new StringBuilder();
		for (MonitorCollectData monitorCollectData : monitorCollectDataList) {
			String strValue = "-";
			if (monitorCollectData.getValue() != null && !monitorCollectData.getValue().isNaN()) {
				strValue = monitorCollectData.getValue().toString();
			}
			sb.append(String.format("(%s, %s)%n", 
					sdf.format(new Date(monitorCollectData.getTime().longValue())), 
					strValue));
		}
		rtn.setValuesStr(sb.toString());

		// 対象データを取得する
		List<Double> xList = new ArrayList<>();
		List<Double> yList = new ArrayList<>();
		Double firstTime = monitorCollectDataList.get(monitorCollectDataList.size() - 1).getTime();
		for (MonitorCollectData monitorCollectData : monitorCollectDataList) {
			if (monitorCollectData.getValue() != null 
					&& !monitorCollectData.getValue().isNaN()) {
				// 日時は相対値で解析する。
				xList.add(monitorCollectData.getTime() - firstTime);
				yList.add(monitorCollectData.getValue());
			}
		}

		// 将来予測監視の最小データ数を下回る場合は処理終了
		Long dataCount = HinemosPropertyCommon.monitor_prediction_lower_limit.getNumericValue();
		if (xList.size() < dataCount) {
			rtn.setErrorMessage("getPredictionData() :"
					+ " monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ": The number of data is insufficient. (prediction)"
					+ " required count=" + dataCount.toString()
					+ ", data count=" + Integer.toString(xList.size()));
			rtn.setNotEnoughFlg(true);
			return rtn;
		}
		
		// 近似予測
		String predictionMethod = monitorInfo.getPredictionMethod();
		if (m_log.isDebugEnabled()) {
			m_log.debug("getPredictionData xList = " + Arrays.toString(xList.toArray())
					+ ", yList = " + Arrays.toString(yList.toArray())
					+ ", predictionMethod = " + predictionMethod);
		}
		m_log.debug("getPredictionData regression start : predictionMethod=" + predictionMethod);
		int order = 0;
		if (MonitorPredictionMethodConstant.POLYNOMIAL_1.equals(predictionMethod)) {
			// 線形回帰（一次）
			order = 1;
		} else if (MonitorPredictionMethodConstant.POLYNOMIAL_2.equals(predictionMethod)) {
			// 非線形回帰（二次）
			order = 2;
		} else if (MonitorPredictionMethodConstant.POLYNOMIAL_3.equals(predictionMethod)) {
			// 非線形回帰（三次）
			order = 3;
		}
		// 線形回帰・非線形回帰の場合のみデータ数のチェックを行う。
		if (xList.size() < order + 1) {
			rtn.setErrorMessage("collectMultiple() :"
					+ " monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ ": The number of data is insufficient. (prediction)"
					+ " required count=" + Integer.toString(order)
					+ ", data count=" + Integer.toString(xList.size()));
			rtn.setNotEnoughFlg(true);
			return rtn;
		}
		//　回帰方程式の係数を取得する
		Double[] coefficients = null;
		try {
			coefficients = getCoefficient(order, xList, yList);
		} catch (HinemosArithmeticException e) {
			m_log.warn("getPredictionData():"
					+ " monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ " : " + e.getMessage(), e);
		} catch (HinemosIllegalArgumentException e) {
			rtn.setErrorMessage("getPredictionData():"
					+ " monitorId=" + monitorInfo.getMonitorId()
					+ ", facilityId=" + facilityId
					+ ", displayName=" + displayName
					+ ", itemName=" + itemName
					+ " : " + e.getMessage());
		}
		if (coefficients == null) {
			return rtn;
		}
		Double[] newCoefficients = new Double[coefficients.length + 1];
		newCoefficients[0] = firstTime;
		for (int i = 0; i < coefficients.length; i++) {
			newCoefficients[i + 1] = coefficients[i];
		}
		rtn.setCoefficients(newCoefficients);

		// 解析する
		// 計算処理のためDouble型にしておく
		Double targetTime = Double.parseDouble(targetDate.toString()) 
				- firstTime 
				+ Double.parseDouble(monitorInfo.getPredictionTarget().toString()) * 60D * 1000D;
		Double dblValue = 0D;
		for (int i = 0; i < coefficients.length; i++) {
			dblValue += (coefficients[i] * Math.pow(targetTime, i));
		}
		rtn.setValue(dblValue);
		m_log.debug("getPredictionData regression end : predictionMethod=" + predictionMethod);
		return rtn;
	}

	/**
	 * 将来予測データを格納する（将来予測監視で使用）
	 */
	public static class MonitorPredictionDataInfo {
		// 将来予測値
		private Double value;
		// 回帰方程式の係数情報（先頭は基準日時）
		private Double[] coefficients;
		// エラーメッセージ
		private String errorMessage;
		// データ不足フラグ（true:不足）
		private boolean notEnoughFlg = false;
		// 値リスト
		private String valuesStr = "";

		public Double getValue() {
			return value;
		}
		public void setValue(Double value) {
			this.value = value;
		}
		public void setCoefficients(Double[] coefficients) {
			this.coefficients = coefficients;
		}
		public Double[] getCoefficients() {
			return coefficients;
		}
		public String getErrorMessage() {
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
		public boolean isNotEnoughFlg() {
			return notEnoughFlg;
		}
		public void setNotEnoughFlg(boolean notEnoughFlg) {
			this.notEnoughFlg = notEnoughFlg;
		}
		public String getValuesStr() {
			return valuesStr;
		}
		public void setValuesStr(String valuesStr) {
			this.valuesStr = valuesStr;
		}
	}

	/**
	 * 回帰方程式の係数を取得する
	 * 
	 * @param order 次元
	 * @param argsX X値
	 * @param argsY Y値
	 * @param count 値の数
	 * @return 偏微分の結果求められる連立方程式　
	 */
	private static Double[] getCoefficient(Integer order, List<Double> xList, List<Double> yList) 
			throws HinemosIllegalArgumentException, HinemosArithmeticException {
		Double[] rtn = null;
		
		// 配列の値が足りなかったらエラー
		if (xList == null
				|| xList.size() == 0
				|| yList == null
				|| yList.size() < xList.size()) {
			throw new HinemosIllegalArgumentException("getCoefficient() : There is no effective data.");
		}

		// 1次以降対応
		if (order < 1) {
			throw new HinemosIllegalArgumentException("getCoefficient() : There is no effective data.");
		}

		Integer count = xList.size();
		m_log.debug("getCoefficient start : "
				+ "order=" + order
				+ ",list.count=" + xList.size()
				+ ",count=" + count);

		try {
			/* 連立方程式を設定
			 * 1次元の場合
			 * na + bΣx = Σy
			 * aΣx + bΣx^2 = Σxy
			 * 
			 * 配列の設定内容
			 * argY = (Σy, Σxy)
			 * argX = ((n,    Σx),
			 *         (Σx,   Σx^2))
			 *         
			 * 2次元の場合
			 * na + bΣx + cΣx^2 = Σy
			 * aΣx + bΣx^2 + cΣx^3 = Σxy
			 * aΣx^2 + bΣx^3 + cΣx^4 = Σx^2y
			 * 
			 * 配列の設定内容
			 * argY = (Σy, Σxy, Σx^2y)
			 * argX = ((n,    Σx,   Σx^2),
			 *         (Σx,   Σx^2, Σx^3),
			 *         (Σx^2, Σx^3, Σx^4))
			 * 
			 * 3次元の場合
			 * na + bΣx + cΣx^2 + dΣx^3 = Σy
			 * aΣx + bΣx^2 + cΣx^3 + dΣx^4 = Σxy
			 * aΣx^2 + bΣx^3 + cΣx^4 + dΣx^5 = Σx^2y
			 * aΣx^3 + bΣx^4 + cΣx^5 + dΣx^6 = Σx^3y
			 *
			 * 配列の設定内容
			 * argY = (Σy, Σxy, Σx^2y, Σx^3y)
			 * argX = ((n,    Σx,   Σx^2, Σx^3),
			 *         (Σx,   Σx^2, Σx^3, Σx^4),
			 *         (Σx^2, Σx^3, Σx^4, Σx^5),
			 *         (Σx^3, Σx^4, Σx^5, Σx^6))
			 */
			m_log.debug("getCoefficient make matrix start");
			double[][] argX = new double[order + 1][order + 1];
			double[] argY = new double[order + 1];
	
			// 値の数を設定
			argX[0][0] = count.doubleValue();
			
			for (int i = 0; i < count; i++) {
				for (int j = 0; j < order + 1; j++) {
					argY[j] = argY[j] + yList.get(i) * Math.pow(xList.get(i), j);
				}
				for (int k = 0; k < order + 1; k++) {
					for (int l=0; l < order + 1; l++) {
						if (k == 0) {
							if (l >= 1) {
								argX[k][l] = argX[k][l] + Math.pow(xList.get(i), k + l);
							}
						} else {
							argX[k][l] = argX[k][l] + Math.pow(xList.get(i), k + l);
						}
					}
				}
			}
			m_log.debug("getCoefficient make matrix end");
	
			m_log.debug("getCoefficient make matrix inverse start");
			// 逆行列の取得
			RealMatrix mArgXInverse = null;
			try {
				// 実行列作成
				RealMatrix mArgX = MatrixUtils.createRealMatrix(argX);
				// 逆行列取得\
				mArgXInverse = new LUDecomposition(mArgX).getSolver().getInverse();
			} catch (NullArgumentException 
					| DimensionMismatchException 
					| NoDataException 
					| SingularMatrixException  e) {
				throw new HinemosArithmeticException("getCoefficient() : Failed in the calculation process."
						+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
			}
			double[][] argXInverse = mArgXInverse.getData();
			m_log.debug("getCoefficient make matrix inverse end");
	
			m_log.debug("getCoefficient make matrix calculation start");
			rtn = new Double[order + 1];
			for (int i = 0; i < order + 1; i++) {
				double tmp = 0D;
				for (int j = 0; j < order + 1; j++) {
					tmp += (argXInverse[i][j] * argY[j]); 
				}
				rtn[i] = tmp;
			}
		} catch (HinemosArithmeticException e) {
			throw e;
		} catch (Exception e) {
			throw new HinemosArithmeticException("getCoefficient() : Failed in the calculation process."
					+ ", " + e.getClass().getSimpleName() + ", " + e.getMessage());
		}
		m_log.debug("getCoefficient make matrix calculation end");
		if (m_log.isDebugEnabled()) {
			m_log.debug("getCoefficient end rtn = " + Arrays.toString(rtn));
		}
		return rtn;
	}

	/**
	 * 動作確認
	 * @param args
	 */
	public static void main(String[] args) {
		List<Double> xList = new ArrayList<>();
		xList.add(2D);
		xList.add(5D);
		xList.add(7D);
		xList.add(10D);
		xList.add(12D);
		List<Double> yList = new ArrayList<>();
		yList.add(13D);
		yList.add(16D);
		yList.add(17D);
		yList.add(25D);
		yList.add(50D);

		//　1次回帰分析（回帰直線）
		Double[] rtn = null;
		try {
			rtn = getCoefficient(1, xList, yList);
			System.out.println("getCoefficient new args1 return");
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < rtn.length; i++) {
				sb.append(rtn[i]);
				sb.append(",");
			}
			sb.append(")");
			System.out.println(sb.toString());
		} catch (HinemosIllegalArgumentException | HinemosArithmeticException e) {
			// 何もしない
		}

		//　2次回帰分析
		try {
			rtn = getCoefficient(2, xList, yList);
			System.out.println("getCoefficient new args2 return");
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < rtn.length; i++) {
				sb.append(rtn[i]);
				sb.append(",");
			}
			sb.append(")");
			System.out.println(sb.toString());
		} catch (HinemosIllegalArgumentException |HinemosArithmeticException e) {
			// 何もしない
		}


		// 3次回帰分析
		try {
			rtn = getCoefficient(3, xList, yList);
			System.out.println("getCoefficient new args3 return");
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < rtn.length; i++) {
				sb.append(rtn[i]);
				sb.append(",");
			}
			sb.append(")");
			System.out.println(sb.toString());
		} catch (HinemosIllegalArgumentException | HinemosArithmeticException e) {
			// 何もしない
		}
	}
}
