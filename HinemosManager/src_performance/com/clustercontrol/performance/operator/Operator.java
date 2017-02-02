/*

Copyright (C) 2008 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

 */

package com.clustercontrol.performance.operator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.performance.monitor.entity.CollectorPollingMstData;
import com.clustercontrol.performance.monitor.model.CollectorPollingMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;

abstract public class Operator {
	private static Log m_log = LogFactory.getLog( Operator.class );

	// 計算式の文字列
	private String expression;

	// 生成直後に初期化する項目
	private String collectMethod;
	private String platformId;
	private String subPlatformId;
	private String itemCode;
	private List<String> variables = new ArrayList<String>();

	// SNMPで取得する値の型がCOUNTER32であった場合の最大値
	private final static long COUNTER32_MAX_VALUE = ((long)(Integer.MAX_VALUE))*2+1;
	private final static BigInteger COUTNER64_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE).shiftLeft(1).add(BigInteger.valueOf(1));

	private Map<String, CollectorPollingMstData> m_oidMap = new ConcurrentHashMap<String, CollectorPollingMstData>();


	public abstract double calc(DataTable currentTable, DataTable previousTable, String deviceName) throws CollectedDataNotFoundException, InvalidValueException;

	/**
	 * 直近の収集時の値を取得します
	 * @param variable 変数名
	 * @param currentTable
	 * @param deviceName
	 * @return 収集値
	 * @throws CollectedDataNotFoundException
	 * @throws InvalidValueException
	 */
	protected double getCurrentMibValue(String variable, DataTable currentTable, String deviceName)
			throws CollectedDataNotFoundException, InvalidValueException {
		return getValue(variable, currentTable, deviceName);
	}

	/**
	 * 前回収集時の値を取得します
	 * @param variable 変数名
	 * @param previousTable
	 * @param deviceName
	 * @return 収集値
	 * @throws CollectedDataNotFoundException
	 * @throws InvalidValueException
	 */
	protected double getPreviousMibValue(String variable, DataTable previousTable, String deviceName)
			throws CollectedDataNotFoundException, InvalidValueException{
		return getValue(variable, previousTable, deviceName);
	}

	/**
	 * 前回収集時からの差分を取得します。
	 * entryKeyの最後の一文字が"*"の場合は、差分の合計を求めます（合計の差分ではない）。
	 * 
	 * @param variable 変数名
	 * @param currentTable
	 * @param previousTable
	 * @param deviceName
	 * @return 差分値
	 * @throws CollectedDataNotFoundException
	 * @throws InvalidValueException
	 */
	protected double getDifferenceValue(String variable, DataTable currentTable, DataTable previousTable, String deviceName)
			throws CollectedDataNotFoundException, InvalidValueException{
		if(m_log.isDebugEnabled()){
			m_log.debug("getDifferenceValue() variable : " + variable);
		}
		CollectorPollingMstData data = getCollectorPollingMstData(variable);

		// 判定元となる一文字を抽出する
		String entryKey = data.getEntryKey();
		String targetIndex = entryKey.substring(entryKey.lastIndexOf(".") + 1);

		if(m_log.isDebugEnabled()){
			m_log.debug("getDifferenceValue() targetIndex : " + targetIndex);
		}

		// entryKeyインデックスの定義によって処理を変える
		if ("?".equals(targetIndex)){
			// SNMP・WBEMのポーリング結果の場合
			if(getCollectMethod().equals(PollerProtocolConstant.PROTOCOL_SNMP) ||
					getCollectMethod().equals(PollerProtocolConstant.PROTOCOL_WBEM)){

				CollectorPollingMstData mstData = getCollectorPollingMstData("key");
				String pollingTarget = mstData.getPollingTarget();

				if(m_log.isDebugEnabled()){
					m_log.debug("getDifferenceValue() targetKey : " + pollingTarget);
				}

				// 直近の取得時のデータテーブルからキーとなるデバイス名をすべて取得する
				String currentIndex = null;
				String previousIndex = null;

				try{
					Set<TableEntry> currentEntrys = currentTable.getValueSetStartWith(PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));
					Set<TableEntry> previousEntrys = previousTable.getValueSetStartWith(PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));
					if(currentEntrys == null || previousEntrys == null){
						// エラー処理
						String message = "getDifferenceValue() currentEntrys or previousEntrys is null, currentEntrys : " + currentEntrys + ", previousEntrys : " + previousEntrys;
						m_log.debug(message);
						throw new CollectedDataNotFoundException(message);
					}

					// CurrentTable
					// 収集対象のデバイス名と比較し、一致したもののインデックスを取得する
					Iterator<TableEntry> itr = currentEntrys.iterator();
					while(itr.hasNext()) {
						TableEntry entry = itr.next();
						if(m_log.isDebugEnabled()){
							m_log.debug("getDifferenceValue() CurrentTable deviceName : " + getCollectMethod() + ", entry.getValue() : " + entry.getValue());
							m_log.debug("getDifferenceValue() CurrentTable entry.getValue() is " + (entry.getValue()).getClass().getCanonicalName());
						}

						// value はString で判定するため、念のためObjectのClass check
						String value = null;
						if(entry.getValue() instanceof String){
							value = (String)entry.getValue();
						}else if (entry.getValue() == null) {
							m_log.debug("entry.getValue() == null");
							continue;
						}else{
							value = entry.getValue().toString();
						}
						
						if (value.length() > 128) {
							// デバイス名が128文字までなので切り詰め
							value = value.substring(0, 128);
						}
						
						if( value.equals(deviceName) ){
							currentIndex = entry.getKey().substring(entry.getKey().lastIndexOf("."));
							break;
						}
					}

					// PreviousTable
					// 収集対象のデバイス名と比較し、一致したもののインデックスを取得する
					itr = previousEntrys.iterator();
					while(itr.hasNext()) {
						TableEntry entry = itr.next();
						if(m_log.isDebugEnabled()){
							m_log.debug("getDifferenceValue() PreviousTable deviceName : " + getCollectMethod() + ", entry.getValue() : " + entry.getValue());
							m_log.debug("getDifferenceValue() PreviousTable entry.getValue() is " + (entry.getValue()).getClass().getCanonicalName());
						}

						// value はString で判定するため、念のためObjectのClass check
						String value = null;
						if(entry.getValue() instanceof String){
							value = (String)entry.getValue();
						}else if (entry.getValue() == null) {
							m_log.debug("entry.getValue() == null.");
							continue;
						}else{
							value = entry.getValue().toString();
						}
						
						if (value.length() > 128) {
							// デバイス名が128文字までなので切り詰め
							value = value.substring(0, 128);
						}
						if( value.equals(deviceName) ){
							previousIndex = entry.getKey().substring(entry.getKey().lastIndexOf("."));
							break;
						}
					}

					if(m_log.isDebugEnabled()){
						m_log.debug("getDifferenceValue() deviceName : " + deviceName + ", currentIndex : " + currentIndex + ", previousIndex : " + previousIndex);
					}
					// デバイス名が見つからない場合
					if(currentIndex == null) {
						// エラー処理
						StringBuffer detailMsg = new StringBuffer();
						detailMsg.append("DeviceName " + deviceName + " is not exists\n");
						detailMsg.append("Search Device Name is ...\n");

						itr = currentEntrys.iterator();
						while(itr.hasNext()) {
							TableEntry entry = itr.next();
							detailMsg.append(" [" + (String)entry.getValue() + "]\n");
						}

						String message = "getDifferenceValue() deviceName : " + deviceName + ", currentIndex is null";
						m_log.info(message);
						throw new CollectedDataNotFoundException(message);
					}
					if(previousIndex == null) {
						// エラー処理
						String message = "getDifferenceValue() deviceName : " + deviceName + ", previousIndex is null";
						m_log.debug(message);
						throw new CollectedDataNotFoundException(message);
					}

					// pollingTargetにTableEntryから取得するためのKeyを与える
					pollingTarget = data.getPollingTarget();
				} catch (CollectedDataNotFoundException e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("getDifferenceValue() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}

				try {
					return getMibValueDiff(data, currentTable, previousTable,
							PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget + currentIndex),
							PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget + previousIndex));
				} catch (CollectedDataNotFoundException e){
					throw e;
				} catch (Exception e){
					// 直前のgetMibValueDiff()の処理にて、failureValueが定義されていないpolling targetの場合に
					// NullPointerExceptionが発生する。
					// これはver4.0までの動作と同様のため、通常ルートではstacktraceを出力しないようにする。
					String message = "getDifferenceValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.debug(message, e);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}
			}
			// VMのポーリング結果の場合
			else {
				/*
				 * getCollectMethod()はVMWARE, XENなどが入っている。
				 * getCollectMethod()の返り値が、
				 * select * from cc_vm_solution_mst;
				 * に含まれている事をチェックする機構を本来は入れるべき。
				 * 例：含まれていない場合はthrow new CollectedDataNotFound()。
				 * 今回は割愛。
				 */
				try {
					// entryKeyの最後の一文字が"?"の場合
					String pollingTarget = data.getPollingTarget() + "." + deviceName;

					// VMの場合は、現在のテーブルと過去のテーブルでentryKeyを変更することができないため、同じentryKeyを与える
					return getMibValueDiff(data, currentTable, previousTable,
							PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget),
							PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));
				} catch (CollectedDataNotFoundException e){
					throw e;
				} catch (Exception e){
					// 直前のgetMibValueDiff()の処理にて、failureValueが定義されていないpolling targetの場合に
					// NullPointerExceptionが発生する。
					// これはver4.0までの動作と同様のため、通常ルートではstacktraceを出力しないようにする。
					String message = "getDifferenceValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.debug(message, e);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}
			}

		} else if ("*".equals(targetIndex)){
			try {
				// entryKeyの最後の一文字が"*"の場合
				String pollingTarget = data.getPollingTarget();

				// 直近の取得時のデータテーブルからベースとなるpollingTarget配下の値全てを取得
				Set<TableEntry> entries = currentTable.getValueSetStartWith(PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));

				if(entries == null){
					String message = "getDifferenceValue() : entries is null";
					m_log.info(message);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}

				// 合計を求める
				double total = 0;  // 初期値0で初期化
				Iterator<TableEntry> itr = entries.iterator();
				while(itr.hasNext()){
					String tableEntryKey = itr.next().getKey();
					total = total + getMibValueDiff(data, currentTable, previousTable, tableEntryKey, tableEntryKey);
					// entryKeyの値は2度データテーブルから取得する処理となるため高速化が必要な場合は見直す必要あり
				}

				return total;
			} catch (CollectedDataNotFoundException e) {
				throw e;
			} catch (Exception e){
				// エラー処理
				String message = "getDifferenceValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
				m_log.warn(message, e);
				throw new CollectedDataNotFoundException(message);
			}
		} else {
			try {
				return getMibValueDiff(data, currentTable, previousTable,
						PollerProtocolConstant.getEntryKey(getCollectMethod() ,entryKey),
						PollerProtocolConstant.getEntryKey(getCollectMethod() ,entryKey));
			} catch (CollectedDataNotFoundException e){
				throw e;
			} catch (Exception e){
				// 直前のgetMibValueDiff()の処理にて、failureValueが定義されていないpolling targetの場合に
				// NullPointerExceptionが発生する。
				// これはver4.0までの動作と同様のため、通常ルートではstacktraceを出力しないようにする。
				String message = "getDifferenceValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
				m_log.debug(message, e);
				throw new CollectedDataNotFoundException(message);
			}
		}
	}

	private double getValue(String variable, DataTable table, String deviceName)
			throws CollectedDataNotFoundException, InvalidValueException{
		if(m_log.isDebugEnabled()){
			m_log.debug("getValue() variable : " + variable);
		}
		CollectorPollingMstData data = getCollectorPollingMstData(variable);

		// 判定元となるインデックスを抽出する
		String entryKey = data.getEntryKey();
		String targetIndex = entryKey.substring(entryKey.lastIndexOf(".") + 1);

		if(m_log.isDebugEnabled()){
			m_log.debug("getValue() targetIndex : " + targetIndex);
		}

		// 最後のインデックスの定義によって処理を変える
		if ("?".equals(targetIndex)){
			// SNMP・WBEMのポーリング結果の場合
			if(getCollectMethod().equals(PollerProtocolConstant.PROTOCOL_SNMP) ||
					getCollectMethod().equals(PollerProtocolConstant.PROTOCOL_WBEM)){

				CollectorPollingMstData wbemData = getCollectorPollingMstData("key");
				String pollingTarget = wbemData.getPollingTarget();

				if(m_log.isDebugEnabled()){
					m_log.debug("getValue() targetKey : " + pollingTarget);
				}

				// 収集対象のデバイス名と比較し、一致したもののインデックスを取得する
				String index = null;

				try{
					// 直近の取得時のデータテーブルからキーとなるデバイス名をすべて取得する
					Set<TableEntry> entrySet = table.getValueSetStartWith(PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));
					if(entrySet == null){
						// エラー処理
						String message = "getValue() entrySet is null, entrySet : null";
						m_log.debug(message);
						throw new CollectedDataNotFoundException(message);
					}

					Iterator<TableEntry> itr = entrySet.iterator();
					while(itr.hasNext()) {
						TableEntry entry = itr.next();
						if(m_log.isDebugEnabled()){
							m_log.debug("getValue() CurrentTable deviceName : " + getCollectMethod() + ", entry.getValue() : " + entry.getValue());
							m_log.debug("getValue() CurrentTable entry.getValue() is " + (entry.getValue()).getClass().getCanonicalName());
						}
						// value はString で判定するため、念のためObjectのClass check
						String value = null;
						if (entry.getValue() == null) {
							continue;
						} else if(entry.getValue() instanceof String){
							value = (String)entry.getValue();
						}else{
							value = entry.getValue().toString();
						}
						
						if (value.length() > 128) {
							// デバイス名が128文字までなので切り詰め
							value = value.substring(0, 128);
						}
						
						if( value.equals(deviceName) ){
							index = entry.getKey().substring(entry.getKey().lastIndexOf("."));
							break;
						}
					}

					if(m_log.isDebugEnabled()){
						m_log.debug("getValue() index : " + index);
					}

					if(index == null) {
						// エラー処理
						StringBuffer detailMsg = new StringBuffer();
						detailMsg.append("DeviceName " + deviceName + " is not exists\n");
						detailMsg.append("Search Device Name is ...\n");

						itr = entrySet.iterator();
						while(itr.hasNext()) {
							TableEntry entry = itr.next();
							detailMsg.append(" [" + entry.getValue() + "]\n");
						}

						String message = "getValue() deviceName : " + deviceName + ", index is null";
						m_log.info(message);
						throw new CollectedDataNotFoundException(message);
					}

					// pollingTargetにTableEntryから取得するためのKeyを与える
					pollingTarget = data.getPollingTarget();

				} catch (CollectedDataNotFoundException e) {
					throw e;
				} catch (Exception e) {
					m_log.warn("getValue() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}

				try {
					return getMibValueDouble(table, data, PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget + index));
				}
				catch (CollectedDataNotFoundException e){
					throw e;
				} catch (Exception e){
					String message = "getValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.warn(message, e);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}
			}
			// VMのポーリング結果の場合
			else {
				/*
				 * getCollectMethod()はVMWARE, XENなどが入っている。
				 * getCollectMethod()の返り値が、
				 * select * from cc_vm_solution_mst;
				 * に含まれている事をチェックする機構を本来は入れるべき。
				 * 例：含まれていない場合はthrow new CollectedDataNotFound()。
				 * 今回は割愛。
				 */
				try {
					// entryKeyの最後の一文字が"?"の場合
					String pollingTarget = data.getPollingTarget() + "." + deviceName;
					return getMibValueDouble(table, data, PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));
				} catch (CollectedDataNotFoundException e) {
					throw e;
				} catch (Exception e){
					String message = "getValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
					m_log.warn(message, e);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}
			}

		} else if ("*".equals(targetIndex)){
			try {
				// entryKeyの最後の一文字が"*"の場合
				String pollingTarget = data.getPollingTarget();

				// 直近の取得時のデータテーブルからベースとなるpollingTarget配下の値全てを取得
				Set<TableEntry> entries = table.getValueSetStartWith(PollerProtocolConstant.getEntryKey(getCollectMethod() ,pollingTarget));

				if(entries == null){
					String message = "getValue() : entries is null";
					m_log.info(message);
					// エラー処理
					throw new CollectedDataNotFoundException(message);
				}

				// 合計を求める
				double total = 0;  // 初期値0で初期化
				Iterator<TableEntry> itr = entries.iterator();
				while(itr.hasNext()){
					String tableEntryKey = itr.next().getKey();
					total = total + getMibValueDouble(table, data, tableEntryKey);
					// entryKeyの値は2度データテーブルから取得する処理となるため高速化が必要な場合は見直す必要あり
				}

				return total;
			} catch (CollectedDataNotFoundException e) {
				throw e;
			} catch (Exception e){
				// エラー処理
				String message = "getValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
				m_log.warn(message, e);
				throw new CollectedDataNotFoundException(message);
			}
		} else {
			try {
				return getMibValueDouble(table, data, PollerProtocolConstant.getEntryKey(getCollectMethod() ,entryKey));
			} catch (CollectedDataNotFoundException e) {
				throw e;
			} catch (Exception e){
				// エラー処理
				String message = "getValue() : " + e.getClass().getSimpleName() + ", " + e.getMessage();
				m_log.warn(message, e);
				throw new CollectedDataNotFoundException(message);
			}
		}
	}

	/**
	 * 
	 * mibの値をlongで取得するメソッドです。
	 * mib値の差分を計算をする必要がある場合は、このメソッドを使用して値を取得し、
	 * 差分値を計算した後、convLongToDobleメソッドでdouble値に変換してreturnしてください。
	 * 
	 * @see #convLongToDouble(long)
	 * 
	 * @param table
	 * @param data
	 * @param entryKey
	 * @return
	 * @throws CollectedDataNotFoundException 
	 */
	private long getMibValueLong(DataTable table, CollectorPollingMstData data,
			String entryKey) throws CollectedDataNotFoundException {

		if(m_log.isDebugEnabled()){
			m_log.debug("getMibValueLong() entryKey : " + entryKey);
		}

		TableEntry entry = table.getValue(entryKey);

		// 指定のOIDの値が存在しない場合
		if(entry == null){
			if (data.getFailureValue() == null) {
				String message = "failure value is null";
				m_log.debug(message);
				throw new CollectedDataNotFoundException(message);
			}
			
			// 値取得失敗と判定し、値取得失敗時の返却値を返す
			return Long.parseLong(data.getFailureValue());
		} else {
			// pollingでエラーが発生していないかチェックする
			if (!entry.isValid()) {
				String message = entry.getErrorDetail().getMessage();
				throw new CollectedDataNotFoundException(message);
			}
			
			long value = (Long)entry.getValue();

			//			// 負の値の場合
			//			if(value < 0){
			//				// 値のタイプがCounter64の型の場合
			//				// javaのlong型は符号付であるため、Long.MAX_VALUEを超える正の値を扱うことができない
			//				if("Counter64".equals(data.getValueType())){
			//					throw new InvalidValueException();
			//				}
			//			}

			return value;
		}
	}

	/**
	 * 
	 * mibの値をdoubleで取得するメソッドです。
	 * 値を加算する場合、あるいはそのままの値を利用する場合は、このメソッドで値を取得してください。
	 * 
	 * @param table
	 * @param data
	 * @param entryKey
	 * @return
	 * @throws CollectedDataNotFoundException 
	 */
	private double getMibValueDouble(DataTable table, CollectorPollingMstData data,
			String entryKey) throws CollectedDataNotFoundException {

		long longValue = getMibValueLong(table, data, entryKey);
		return convLongToDouble(longValue);

	}

	/**
	 * 
	 * 引数で与えられたlong値を、doubleへ変換します。
	 * 
	 * @param longValue
	 * @return
	 */
	private double convLongToDouble(long longValue) {
		double ret = longValue;
		if (longValue < 0) {
			long tmp = longValue - Long.MAX_VALUE;
			ret = (double)Long.MAX_VALUE + tmp;
		}
		return ret;
	}

	/**
	 * 直近収集と前回収集のデータテーブルから指定のpollingTargetの値を取得しその差分を求める
	 * @throws CollectedDataNotFoundException 
	 */
	private double getMibValueDiff(CollectorPollingMstData data,
			DataTable currentTable, DataTable previousTable, String currentEntryKey, String previousEntryKey)
					throws CollectedDataNotFoundException {

		// return用の変数
		double ret = 0;

		// 前回収集時の値を取得
		long previousValue =  getMibValueLong(previousTable, data, previousEntryKey);

		// 直近収集時の値を取得
		long currentValue =  getMibValueLong(currentTable, data, currentEntryKey);

		// 差分をlongで計算する
		long diff = currentValue - previousValue;


		// 差分が正の値の場合
		if(diff >= 0){
			ret = diff;

			// 差分が負の値の場合はdouble値へ変換する
		} else {

			// 値のタイプがCounter32の型の場合は上限を越えループした場合の処理
			if("Counter32".equals(data.getValueType()) || "Uint32".equals(data.getValueType())) {
				diff = COUNTER32_MAX_VALUE + 1 + diff;
				ret = convLongToDouble(diff);

				// 値のタイプがCounter64の型の場合は上限を越えループした場合の処理
			} else if ("Counter64".equals(data.getValueType()) || "Uint64".equals(data.getValueType())) {
				ret = COUTNER64_MAX_VALUE.add(BigInteger.valueOf(diff + 1)).doubleValue();

			}
		}

		return ret;
	}

	/**
	 * キャッシュからEntryKey定義を取得
	 * 
	 * CollectorPollingMstData には下記が含まれる
	 * ・収集方法
	 * ・プラットフォーム
	 * ・収集項目コード
	 * ・変数名
	 * ・エントリーキー
	 * ・値の型
	 * ・ポーリング対象値
	 * ・取得失敗時の値
	 */
	private CollectorPollingMstData getCollectorPollingMstData(String variable) {
		CollectorPollingMstData data = m_oidMap.get(variable);

		// 登録されていない場合は登録
		if (data == null){
			registerOidMap(variable);

			data = m_oidMap.get(variable);

			// 登録できなかった場合
			if(data == null){
				m_log.info("data=null");
				// エラー処理
				throw new IllegalStateException("variable=" + variable);
			}
		}
		return data;
	}

	private void registerOidMap(String variable){
		try {
			if(m_log.isDebugEnabled()){
				m_log.debug(getPlatformId() + ", " +  getItemCode() + ", " + variable);
			}

			CollectorPollingMstEntity bean = null;
			try {
				bean = QueryUtil.getCollectorPollingMstPK(getCollectMethod(),
						getPlatformId(),
						getSubPlatformId(),
						getItemCode(),
						variable);
			} catch (CollectorNotFound e) {
			}
			if (bean != null) {
				CollectorPollingMstData data = new CollectorPollingMstData(
						bean.getId().getCollectMethod(),
						bean.getId().getPlatformId(),
						bean.getId().getSubPlatformId(),
						bean.getId().getItemCode(),
						bean.getId().getVariableId(),
						bean.getEntryKey(),
						bean.getSnmpValueTypeMstEntity().getValueType(),
						bean.getPollingTarget(),
						bean.getFailureValue());

				m_oidMap.put(variable, data);
			}
		} catch (Exception e) {
			m_log.warn("registerOidMap() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}


	public String getCollectMethod() {
		return collectMethod;
	}

	public void setCollectMethod(String collectMethod) {
		this.collectMethod = collectMethod;
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getSubPlatformId() {
		return subPlatformId;
	}

	public void setSubPlatformId(String subPlatformId) {
		this.subPlatformId = subPlatformId;
	}

	public String getItemCode() {
		return itemCode;
	}

	/**
	 * 収集項目コードを設定します。
	 * 同時に指定の収集項目コードの性能値算出式で使用可能な変数を設定します。
	 */
	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;

		try{
			// 収集項目コードから変数情報を取得する
			List<CollectorPollingMstEntity> beans =
					QueryUtil.getAllCollectorPollingMstVariableId(getCollectMethod(),
							getPlatformId(),
							getSubPlatformId(),
							itemCode);
			// 保持している全ての変数IDをクリア
			getVariables().clear();

			// この収集項目で利用可能な変数IDをリストに保存
			for(CollectorPollingMstEntity bean : beans){
				if(m_log.isDebugEnabled()){
					m_log.debug("add " + bean.getId().getVariableId());
				}
				getVariables().add(bean.getId().getVariableId());
			}
		} catch (Exception e) {
			m_log.warn("setItemCode() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
		}
	}

	public List<String> getVariables() {
		return variables;
	}

	public void setVariables(List<String> variables) {
		this.variables = variables;
	}

	public static class InvalidValueException extends Exception {
		private static final long serialVersionUID = -1102457433444726271L;
		
		public InvalidValueException(String messages) {
			super(messages);
		}
	}

	public class CollectedDataNotFoundException extends Exception {
		private static final long serialVersionUID = 6306555743811316089L;
		
		public CollectedDataNotFoundException(String messages) {
			super(messages + ", itemcode=" + itemCode);
		}
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}
}
