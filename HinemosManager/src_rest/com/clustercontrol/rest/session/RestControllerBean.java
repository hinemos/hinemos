/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.util.HinemosTime;


/**
 * RESTに関連する機能の管理を行う Session Bean クラス<BR>
 * <p>クライアントからの Entity Bean へのアクセスは、Session Bean を介して行います。<BR>
 *
 */
public class RestControllerBean {
	/** ログ出力のインスタンス<BR> */
	private static Log m_log = LogFactory.getLog( RestControllerBean.class );

	public RestControllerBean() {
	}

	/**
     * エージェントからのリクエストを登録。リクエストの重複受信防止用<BR>
     * リクエスト及びエージェントの識別情報は {@link HinemosSessionContext} から取得します。
     *
     * @param systemFunction 呼び出し元で任意に設定可能な文字列(64文字以内)。
     * @param resourceMethod 呼び出し元メソッド名(64文字以内)。
     * @throws HinemosUnknown
     * @return 登録の成否。 IDが重複した場合、falseを返却。
     */
    public boolean registerRestAgentRequest(String systemFunction, String resourceMethod) throws HinemosUnknown {
        String agentRequestId = (String) HinemosSessionContext.instance().getProperty(RestHeaderConstant.AGENT_REQUEST_ID);
        String agentIdentifier = (String) HinemosSessionContext.instance().getProperty(RestHeaderConstant.AGENT_IDENTIFIER);
        return registerRestAgentRequest(agentRequestId, agentIdentifier, systemFunction, resourceMethod);
    }

    /**
     * エージェントからのリクエストを登録。リクエストの重複受信防止用<BR>
     *
     * @param requestId リクエストの個体識別子。
     * @param agentId Hinemosエージェントの個体識別子。
     * @param systemFunction 呼び出し元で任意に設定可能な文字列(64文字以内)。
     * @param resourceMethod 呼び出し元メソッド名(64文字以内)。
     * @throws HinemosUnknown
     * @return 登録の成否。 IDが重複した場合、falseを返却。
     */
    public boolean registerRestAgentRequest(String requestId, String agentId, String systemFunction, String resourceMethod)
            throws HinemosUnknown {

        m_log.debug("registerRestAgentRequest() : requestId = " + requestId + ", agentId = " + agentId
                + ", systemFunction = " + systemFunction + ", resourceMethod = " + resourceMethod);

        if (requestId == null || requestId.trim().length() == 0) {
            throw new HinemosUnknown("Agent Request ID is required.");
		}

		JpaTransactionManager jtm = null;
		int ret = -1;

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			// リクエストIDが未登録の場合のみINSERTされるクエリを実行する
			// 返ってきたINSERT件数でリクエストIDの重複チェックを行う
			ret = em.createNamedQuery("RestAgentRequestEntity.insertRequestId", Integer.class, ObjectPrivilegeMode.NONE)
					.setParameter("requestId", requestId).setParameter("agentId", agentId)
					.setParameter("systemFunction", systemFunction).setParameter("resourceMethod", resourceMethod)
					.setParameter("regDate", HinemosTime.currentTimeMillis()).executeUpdate();

			jtm.commit();
		} catch (Exception e) {
			if (jtm != null)
				jtm.rollback();
            m_log.warn("registerRestAgentRequest() : "
                    + e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			throw new HinemosUnknown(e.getMessage(), e);
		} finally {
			if (jtm != null)
				jtm.close();
		}
		if (ret == 0) {
			// INSERT件数が0件の場合はリクエストIDが登録済みのため以降の処理は行わない
			m_log.info("registerRestAgentRequest() : Duplicate. requestId = " + requestId + ", agentId = " + agentId
					+ ", systemFunction = " + systemFunction + ", resourceMethod = " + resourceMethod);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 登録後、保持時間が経過したリクエストのデータを廃棄<BR>
	 *
	 * @param keepTimeMillis 保持時間（ミリ秒）
	 * @throws HinemosUnknown
	 */
	public void discardRestAgentRequest(Long keepTimeMillis) {
		m_log.debug("discardRestAgentRequest() : start");

		JpaTransactionManager jtm = null;

		try {
			Long limitRegDate = HinemosTime.currentTimeMillis() - keepTimeMillis ;
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			em.createNamedQuery("RestAgentRequestEntity.deleteByRegDate")
			.setParameter("regDate", limitRegDate)
			.executeUpdate();

			jtm.commit();
		} catch (Exception e) {
			m_log.error("discardRestAgentRequest() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
	
	/**
	 * リクエストIDを削除する。<BR>
	 * 
	 */
	public void deleteRestAgentRequest() {
		m_log.debug("deleteRestAgentRequest() : start");

		JpaTransactionManager jtm = null;
		String agentRequestId = (String) HinemosSessionContext.instance().getProperty(RestHeaderConstant.AGENT_REQUEST_ID);

		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			HinemosEntityManager em = jtm.getEntityManager();

			em.createNamedQuery("RestAgentRequestEntity.deleteByRequestId")
			.setParameter("requestId", agentRequestId)
			.executeUpdate();

			jtm.commit();
		} catch (Exception e) {
			m_log.error("deleteRestAgentRequest() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
			if (jtm != null)
				jtm.rollback();
		} finally {
			if (jtm != null)
				jtm.close();
		}
	}
}
