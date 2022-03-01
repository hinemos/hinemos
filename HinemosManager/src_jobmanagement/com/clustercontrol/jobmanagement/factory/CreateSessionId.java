/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.factory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.HinemosManagerMain;
import com.clustercontrol.bean.StatusConstant;
import com.clustercontrol.commons.util.ILock;
import com.clustercontrol.commons.util.ILockManager;
import com.clustercontrol.commons.util.LockManagerFactory;
import com.clustercontrol.jobmanagement.model.JobSessionEntity;
import com.clustercontrol.jobmanagement.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;

/**
 * セッションIDを作成するクラスです。
 *
 * @version 2.1.0
 * @since 2.1.0
 */
public class CreateSessionId {

	private static Log m_log = LogFactory.getLog( CreateSessionId.class );

	public static final String CONNECTOR = "-";
	public static final String PREMAKE_SCHEDULE_DATE_PREFIX_FORMAT = "yyyyMMddHHmm";
	public static final String PREMAKE_SCHEDULE_DATE_FORMAT = PREMAKE_SCHEDULE_DATE_PREFIX_FORMAT + "00";
	private static String prevDate = "";
	private static int prevNumber = HinemosManagerMain._instanceId;
	/**	ジョブセッション事前生成用、最後に採番した日付 */
	private static String prevDateFroPremake = "";
	/**	ジョブセッション事前生成用、最後に採番した番号 */
	private static int prevNumberFroPremake = HinemosManagerMain._instanceId;

	/**
	 * 現在時刻からセッションIDを作成します。
	 * 2台のクラスタ構成にて、1台目は除数0のミリ秒、2台目は除数1のミリ秒にて
	 * 重複しないように払いだされる。
	 * 
	 * @return セッションID
	 */
	synchronized public static String create() {
		ILockManager lm = LockManagerFactory.instance().create();
		ILock lock = lm.create(CreateSessionId.class.getName());
		
		try {
			lock.writeLock();
			
			String sessionId = null;
	
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			dateFormat.setTimeZone(HinemosTime.getTimeZone());
			String dateString = dateFormat.format(HinemosTime.getDateInstance());
			
			DecimalFormat format = new DecimalFormat("-000");
			if(prevDate.equals(dateString)){
				prevNumber += HinemosManagerMain._instanceCount;
				sessionId = dateString + format.format(prevNumber);
			}
			else{
				sessionId = dateString + format.format(0);
				prevDate = dateString;
				prevNumber = HinemosManagerMain._instanceId;
			}
			if(m_log.isTraceEnabled()){
				m_log.trace("create() : sessionId=" + sessionId);
			}
			return sessionId;
		} finally {
			lock.writeUnlock();
		}
	}

	/**
	 * ジョブセッション事前生成用
	 * 指定された日時に実行予定のセッションIDを取得します。
	 * 
	 * マルチマスタのMC機構の対応は行われていないため、対応が必要になった際は改修する必要がある。
	 * 
	 * @param scheduleDate ジョブ実行予定日時
	 * @param jobkickId 実行契機ID
	 * @return セッションID
	 */
	public static String getPremake(Long scheduleDate, String jobkickId) {
		String sessionId = null;
		SimpleDateFormat scheduleDateFormat = new SimpleDateFormat(PREMAKE_SCHEDULE_DATE_FORMAT);
		scheduleDateFormat.setTimeZone(HinemosTime.getTimeZone());
		String scheduleDateString = scheduleDateFormat.format(new Date(scheduleDate));

		List<JobSessionEntity> list = QueryUtil.getJobSessionListBySessionIdJobkickIdAndStatus(
				scheduleDateString, jobkickId, StatusConstant.TYPE_SCHEDULED);
		if (list != null && list.size() > 0) {
			sessionId = list.get(0).getSessionId();
		}
		return sessionId;
	}

	/**
	 * ジョブセッション事前生成用
	 * 指定された日時からジョブセッション事前生成のセッションIDを作成します。
	 * 2台のクラスタ構成にて、1台目は除数0のミリ秒、2台目は除数1のミリ秒にて
	 * 重複しないように払いだされる。
	 * 
	 * ジョブセッション事前生成については、マルチマスタのMC機構の対応は行われていないため、対応が必要になった際は改修する必要がある。
	 * 
	 * @param scheduleDate ジョブ実行予定日時
	 * @param jobkickId 実行契機ID
	 * @return セッションID
	 */
	synchronized public static String createPremake(Long scheduleDate, String jobkickId) {
		ILockManager lm = LockManagerFactory.instance().create();
		// TODO 可能ならcreate() とはロック管理を別々にする（並列でも問題ない筈）
		ILock lock = lm.create(CreateSessionId.class.getName());
		try {
			lock.writeLock();
			
			String sessionId = null;

			SimpleDateFormat scheduleDateFormat = new SimpleDateFormat(PREMAKE_SCHEDULE_DATE_FORMAT);
			scheduleDateFormat.setTimeZone(HinemosTime.getTimeZone());
			String scheduleDateString = scheduleDateFormat.format(new Date(scheduleDate));

			SimpleDateFormat scheduleDatePrefixFormat  = new SimpleDateFormat(PREMAKE_SCHEDULE_DATE_PREFIX_FORMAT);
			scheduleDatePrefixFormat .setTimeZone(HinemosTime.getTimeZone());
			String scheduleDatePrefixString  = scheduleDatePrefixFormat .format(new Date(scheduleDate));

			// DBへの存在確認
			List<JobSessionEntity> list = QueryUtil.getJobSessionListBySessionIdJobkickId(
					scheduleDatePrefixString, jobkickId);
			if (list != null && list.size() > 0) {
				// 既に存在する場合は実行しない
				return null;
			}

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			dateFormat.setTimeZone(HinemosTime.getTimeZone());
			String dateString = scheduleDateFormat.format(HinemosTime.getDateInstance());

			if(prevDateFroPremake.equals(dateString)){
				prevNumberFroPremake += HinemosManagerMain._instanceCount;
				sessionId = String.format("%s" + CONNECTOR + "%s" + CONNECTOR + "%03d", scheduleDateString, dateString, prevNumberFroPremake);
			}else{
				sessionId = String.format("%s" + CONNECTOR + "%s" + CONNECTOR + "%03d", scheduleDateString, dateString, 0);
				prevDateFroPremake = dateString;
				prevNumberFroPremake = HinemosManagerMain._instanceId;
			}
	
			if(m_log.isTraceEnabled()){
				m_log.trace("createPremake() : sessionId=" + sessionId);
			}
			return sessionId;
		} finally {
			lock.writeUnlock();
		}
	}
}
