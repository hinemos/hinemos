/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.jobmanagement.bean.OperationConstant;

/**
 * ジョブ操作の可否を判定するクラスです。
 *
 * @version 2.0.0
 * @since 1.0.0
 */
public class JobOperationJudgment {
	/** ジョブ操作の実行状態毎のパターン */
	private static Map<Integer, ArrayList<Integer>> m_statusPatternMap = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	/** ジョブ操作のジョブ種別毎のパターン */
	private static Map<Integer, ArrayList<Integer>> m_jobPatternMap = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
	/** ジョブネット */
	public static final int TYPE_JOBNET = 0;
	/** ジョブ */
	public static final int TYPE_JOB = 1;
	/** ノード */
	public static final int TYPE_NODE = 2;

	static{
		// ジョブ操作の実行状態毎のパターンをハッシュに保持

		//開始[即時]
		ArrayList<Integer> operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_SUSPEND);
		operation.add(StatusConstant.TYPE_STOP);
		operation.addAll(StatusConstant.getEndList());
		operation.add(StatusConstant.TYPE_ERROR);
		m_statusPatternMap.put(
				OperationConstant.TYPE_START_AT_ONCE, operation);

		//開始[中断解除]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_SUSPEND);
		m_statusPatternMap.put(OperationConstant.TYPE_START_SUSPEND, operation);

		//開始[保留解除]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_RESERVING);
		m_statusPatternMap.put(OperationConstant.TYPE_START_WAIT, operation);

		//開始[スキップ解除]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_SKIP);
		m_statusPatternMap.put(OperationConstant.TYPE_START_SKIP, operation);

		//停止[コマンド]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_RUNNING);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_AT_ONCE, operation);

		//停止[中断]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_RUNNING);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_SUSPEND, operation);

		//停止[保留]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_WAIT);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_WAIT, operation);

		//停止[スキップ]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_WAIT);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_SKIP, operation);

		//停止[状態変更]
		operation = new ArrayList<Integer>();
		operation.addAll(StatusConstant.getEndList());
		operation.add(StatusConstant.TYPE_STOP);
		operation.add(StatusConstant.TYPE_ERROR);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_MAINTENANCE, operation);

		//停止[強制]
		operation = new ArrayList<Integer>();
		operation.add(StatusConstant.TYPE_WAIT);
		operation.add(StatusConstant.TYPE_STOPPING);
		m_statusPatternMap.put(OperationConstant.TYPE_STOP_FORCE, operation);

		// ジョブ操作のジョブ種別毎のパターンをハッシュに保持

		//開始[即時]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_NODE);
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_START_AT_ONCE, operation);

		//開始[中断解除]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_START_SUSPEND, operation);

		//開始[保留解除]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_START_WAIT, operation);

		//開始[スキップ解除]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_START_SKIP, operation);

		//停止[コマンド]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_NODE);
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_AT_ONCE, operation);

		//停止[中断]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_SUSPEND, operation);

		//停止[保留]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_WAIT, operation);

		//停止[スキップ]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_SKIP, operation);

		//停止[状態変更]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_NODE);
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_MAINTENANCE, operation);

		//停止[強制]
		operation = new ArrayList<Integer>();
		operation.add(TYPE_NODE);
		operation.add(TYPE_JOB);
		operation.add(TYPE_JOBNET);
		m_jobPatternMap.put(OperationConstant.TYPE_STOP_FORCE, operation);
	}

	/**
	 * ジョブ操作の可/不可をジョブ種別及び実行状態のパターンに当てはめて判定します。
	 * 
	 * @param operation ジョブの操作種別
	 * @param jobType ジョブ種別
	 * @param status 実行状態
	 * @return 操作可否（true：操作可、false：操作不可）
	 * 
	 * @see com.clustercontrol.bean.StatusConstant
	 * @see com.clustercontrol.jobmanagement.bean.OperationConstant
	 */
	public static boolean judgment(int operation, int jobType, int status) {
		Integer jobOperation = operation;
		boolean jobCheck = false;
		boolean statusCheck = false;

		//ジョブタイプでチェック
		ArrayList<Integer> list = m_jobPatternMap.get(jobOperation);
		if(list != null){
			for(Integer i : list) {
				if(i == jobType){
					jobCheck = true;
					break;
				}
			}
		}

		//実行状態でチェック
		list = m_statusPatternMap.get(jobOperation);
		if(list != null){
			for(Integer i : list){
				if(i == status){
					statusCheck = true;
					break;
				}
			}
		}

		if(jobCheck && statusCheck){
			return true;
		}else{
			return false;
		}
	}
}
