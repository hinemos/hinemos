/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.performance.util;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.performance.bean.CollectorItemInfo;
import com.clustercontrol.performance.monitor.entity.CollectorItemCalcMethodMstPK;
import com.clustercontrol.performance.monitor.entity.CollectorItemCodeMstData;
import com.clustercontrol.performance.monitor.model.CollectorCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCalcMethodMstEntity;
import com.clustercontrol.performance.monitor.util.QueryUtil;
import com.clustercontrol.performance.operator.Operator;
import com.clustercontrol.performance.operator.Operator.CollectedDataNotFoundException;
import com.clustercontrol.performance.operator.Operator.InvalidOverValueException;
import com.clustercontrol.performance.operator.Operator.InvalidValueException;
import com.clustercontrol.performance.operator.Undefined;
import com.clustercontrol.poller.util.DataTable;

/**
 *SNMPで取得した値からグラフに必要な値への計算を行うクラス
 *
 * 
 * @version 4.0.0
 * @since 1.0.0
 */
public final class CalculationMethod {
	private static Log m_log = LogFactory.getLog( CalculationMethod.class );

	private static final String DEFAULT_METHOD_CLASS_PACKAGE = "com.clustercontrol.performance.operator";

	// Operatorを格納するmap。getOperator関数以外からこのmapを変更しないこと
	private static ConcurrentHashMap<CollectorItemCalcMethodMstPK, Operator> m_operationMap =
			new ConcurrentHashMap<CollectorItemCalcMethodMstPK, Operator>();

	public static double getPerformance(
			final String platformId,
			final String subPlatformId,
			final CollectorItemInfo itemInfo,
			final String deviceName,
			final DataTable currentTable,
			final DataTable previousTable) throws CollectedDataNotFoundException, InvalidValueException, IllegalStateException, InvalidOverValueException{

		// 計算方法を取得
		// ItemCodeからItemCodeMstのデータを取得
		CollectorItemCodeMstData itemCodeMst =
				CollectorMasterCache.getCategoryCodeMst(itemInfo.getItemCode());

		String method = CollectorMasterCache.getCollectMethod(
				platformId, subPlatformId, itemCodeMst.getCategoryCode());

		// 収集項目・収集メソッドに該当する計算式クラスを取得する
		// 但し、SubplatformがVMやクラウドの場合、物理環境と同じポーリングができる必要があるため、
		// OperatorがUndefinedだった場合には、Subplatformを空にして再トライする
		Operator ope = getOperator(
				new CollectorItemCalcMethodMstPK(
						method,
						platformId,
						subPlatformId,
						itemInfo.getItemCode()));
		if (!subPlatformId.isEmpty() && ope.getClass().equals(Undefined.class)) {
			m_log.debug("getPerformance() : could not get operator. retry and get physical operator");
			ope = getOperator(
					new CollectorItemCalcMethodMstPK(
							method,
							platformId,
							"",
							itemInfo.getItemCode()));
		}
		if (m_log.isDebugEnabled()) {
			m_log.debug("getPerformance() : method = " + method +
					", platform = " + platformId +
					", subPlatformId = " + subPlatformId +
					", itemCode = " + itemInfo.getItemCode() +
					", Operator = " + ope.toString());
		}
		return ope.calc(currentTable, previousTable, deviceName);
	}

	private static Operator getOperator(CollectorItemCalcMethodMstPK pk){

		// 以前に作られたOperatorが存在すればそれを返す
		Operator returnOperator = m_operationMap.get(pk);
		if (returnOperator != null) {
			m_log.debug("getOperator() : operator has been already cached");
			return returnOperator;
		}

		// 既存のOperatorが無いため新規に登録する
		try {
			if (m_log.isDebugEnabled()) {
				m_log.debug("getOperator() : create new operator = "
						+ pk.getCollectMethod() + ", "
						+ pk.getItemCode() + ", "
						+ pk.getPlatformId() + ", "
						+ pk.getSubPlatformId());
			}
			CollectorItemCalcMethodMstEntity bean = QueryUtil.getCollectorItemCalcMethodMstPK(
					pk.getCollectMethod(),
					pk.getPlatformId(),
					pk.getSubPlatformId(),
					pk.getItemCode());

			CollectorCalcMethodMstEntity methodMst = bean.getCollectorCalcMethodMstEntity();
			if (methodMst == null) {
				CollectorNotFound e = new CollectorNotFound("CollectorCalcMethodMstEntity.findByPrimaryKey"
						+ ", collectMethod = " + pk.getCollectMethod());
				m_log.info(e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}

			try {
				String className = methodMst.getClassName();

				// 指定されているクラス名に"."を含まない場合
				if(className.indexOf('.') == -1){
					// デフォルトのパッケージ名を補完
					className = DEFAULT_METHOD_CLASS_PACKAGE + "." + className;
				}

				Operator ope = (Operator)Class.forName(className).newInstance();
				// 初期化
				ope.setCollectMethod(bean.getId().getCollectMethod());
				ope.setPlatformId(bean.getId().getPlatformId());
				ope.setSubPlatformId(bean.getId().getSubPlatformId());
				ope.setItemCode(bean.getId().getItemCode());
				ope.setExpression(methodMst.getExpression());

				// 作成したOperatorを登録する。既に他のスレッドにより登録されていた場合、null以外の値が返るので
				// その場合にはここで作成したOperatorは使わず、先に登録されていたOperatorを返す
				returnOperator = m_operationMap.putIfAbsent(pk, ope);
				if (returnOperator == null) {
					if (m_log.isDebugEnabled()) {
						m_log.debug("getOperator() : regist new operator. Key = { "
								+ pk.getCollectMethod() + ", "
								+ pk.getItemCode() + ", "
								+ pk.getPlatformId() + ", "
								+ pk.getSubPlatformId() + " }, Operator = "
								+ ope.toString());
					}
					return ope;
				} else {
					m_log.debug("getOperator() : can't regist new operator (Same operator instance has been already registered by other thread");
					return returnOperator;
				}
			} catch (ClassNotFoundException e) {
				m_log.info("registerOperation() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (InstantiationException e) {
				m_log.info("registerOperation() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			} catch (IllegalAccessException e) {
				m_log.info("registerOperation() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
			}
		} catch (CollectorNotFound e) {
			m_log.info("No such entity!  "
					+ pk.getItemCode() + ", "
					+ pk.getPlatformId());
			m_log.debug(e.getMessage(), e);
		}
		// 指定の収集コードに対応する計算方法がDBに登録されていない場合や
		// その他、何らかの理由で登録ができなかった場合、必ずDouble.NaNを返すOperatorを登録する。
		// 但し、既に他のスレッドにより登録済みの場合、登録済みのOperatorを結果として返す
		Operator operatorUndefine = new Undefined();
		returnOperator = m_operationMap.putIfAbsent(pk, operatorUndefine);
		if (returnOperator == null) {
			if (m_log.isDebugEnabled()) {
				m_log.debug("getOperator() : regist new operator. Key = { "
						+ pk.getCollectMethod() + ", "
						+ pk.getItemCode() + ", "
						+ pk.getPlatformId() + ", "
						+ pk.getSubPlatformId() + " }, Operator = "
						+ operatorUndefine.toString());
			}
			return operatorUndefine;
		} else {
			m_log.debug("getOperator() : can't regist new operator (Same operator instance has been already registered by other thread");
			return returnOperator;
		}
	}
}
