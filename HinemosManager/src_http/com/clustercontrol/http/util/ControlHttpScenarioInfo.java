/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.http.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.http.model.HttpScenarioCheckInfo;
import com.clustercontrol.http.model.Page;
import com.clustercontrol.http.model.Pattern;
import com.clustercontrol.http.model.Variable;
import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * HTTP監視(シナリオ)判定情報を管理するクラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class ControlHttpScenarioInfo {

	/** 監視対象ID */
	private String m_monitorTypeId;

	/** 監視ID */
	private String m_monitorId;

	/**
	 * コンストラクタ
	 * 
	 * @param monitorId 監視項目ID
	 * @param monitorTypeId 監視対象ID
	 * @version 5.0.0
	 * @since 5.0.0
	 */
	public ControlHttpScenarioInfo(String monitorId, String monitorTypeId) {
		m_monitorId = monitorId;
		m_monitorTypeId = monitorTypeId;
	}

	/**
	 * HTTP監視(シナリオ)情報を取得します。<BR>
	 * 
	 * @return HTTP監視(シナリオ)情報
	 * @throws MonitorNotFound
	 * @version 5.0.0
	 * @since 5.0.0
	 */
	public HttpScenarioCheckInfo get() throws MonitorNotFound {
		// HTTP監視情報を取得
		HttpScenarioCheckInfo check = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitorId);
		check.setMonitorTypeId(m_monitorTypeId);
		return check;
	}

	/**
	 * HTTP監視(シナリオ)情報を追加します。<BR>
	 * 
	 * @param http HTTP監視(シナリオ)情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean add(HttpScenarioCheckInfo http) throws MonitorNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			MonitorInfo monitorInfo = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);
			http.setMonitorId(m_monitorId);
			em.persist(http);
			http.relateToMonitorInfo(monitorInfo);
			
			for (int i = 0; i < http.getPages().size(); ++i) {
				Page page = http.getPages().get(i);
				page.setMonitorId(m_monitorId);
				page.setPageOrderNo(i);
				em.persist(page);
				page.relateToMonitorHttpScenarioInfoEntity(http);

				for (int j = 0; j < page.getPatterns().size(); ++j) {
					Pattern p = page.getPatterns().get(j);
					p.setMonitorId(m_monitorId);
					p.setPageOrderNo(i);
					p.setPatternOrderNo(j);
					em.persist(p);
					p.relateToMonitorHttpScenarioPageInfoEntity(page);
				}

				for (int k = 0; k < page.getVariables().size(); ++k) {
					Variable v = page.getVariables().get(k);
					v.setMonitorId(m_monitorId);
					v.setPageOrderNo(i);
					em.persist(v);
					v.relateToMonitorHttpScenarioPageInfoEntity(page);
				}
			}

			return true;
		}
	}

	/**
	 * HTTP監視(シナリオ)情報を変更します。<BR>
	 * 
	 * @param http HTTP監視(シナリオ)情報
	 * @return 成功した場合、true
	 * @throws MonitorNotFound
	 * @throws InvalidRole
	 * @version 2.1.0
	 * @since 2.1.0
	 */
	public boolean modify(HttpScenarioCheckInfo http) throws MonitorNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			MonitorInfo monitorEntity = com.clustercontrol.monitor.run.util.QueryUtil.getMonitorInfoPK(m_monitorId);

			// HTTP監視(シナリオ)情報を取得
			HttpScenarioCheckInfo entity = QueryUtil.getMonitorHttpScenarioInfoPK(m_monitorId);

			// HTTP監視(シナリオ)情報を設定
			entity.setAuthType(http.getAuthType());
			entity.setAuthUser(http.getAuthUser());
			entity.setAuthPassword(http.getAuthPassword());
			entity.setProxyFlg(http.getProxyFlg());
			entity.setProxyUrl(http.getProxyUrl());
			entity.setProxyPort(http.getProxyPort());
			entity.setProxyUser(http.getProxyUser());
			entity.setProxyPassword(http.getProxyPassword());
			entity.setMonitoringPerPageFlg(http.getMonitoringPerPageFlg());
			entity.setUserAgent(http.getUserAgent());
			entity.setConnectTimeout(http.getConnectTimeout());
			entity.setRequestTimeout(http.getRequestTimeout());
			monitorEntity.setHttpScenarioCheckInfo(entity);

			List<Page> pList = new ArrayList<Page>(http.getPages());
			Iterator<Page> piter = pList.iterator();
			List<Page> peList = new ArrayList<Page>(entity.getPages());

			int pageOrderNo = 0;
			while (piter.hasNext()) {
				Page p = piter.next();

				Iterator<Page> peiter = peList.iterator();
				while (peiter.hasNext()) {
					Page pe = peiter.next();
					if (pageOrderNo == pe.getId().getPageOrderNo()) {
						pe.setUrl(p.getUrl());
						pe.setDescription(p.getDescription());
						pe.setStatusCode(p.getStatusCode());
						pe.setPost(p.getPost());
						pe.setPriority(p.getPriority());
						pe.setMessage(p.getMessage());

						List<Pattern> ptList = new ArrayList<Pattern>(p.getPatterns());
						Iterator<Pattern> ptiter = ptList.iterator();
						List<Pattern> pteList = new ArrayList<Pattern>(pe.getPatterns());

						int patternOrderNo = 0;
						while (ptiter.hasNext()) {
							Pattern pt = ptiter.next();

							Iterator<Pattern> pteiter = pteList.iterator();
							while (pteiter.hasNext()) {
								Pattern pte = pteiter.next();
								if (patternOrderNo == pte.getId().getPatternOrderNo()) {
									pte.setPattern(pt.getPattern());
									pte.setDescription(pt.getDescription());
									pte.setCaseSensitivityFlg(pt.getCaseSensitivityFlg());
									pte.setProcessType(pt.getProcessType());
									pte.setValidFlg(pt.getValidFlg());

									pteiter.remove();
									ptiter.remove();
									break;
								}
							}
							patternOrderNo++;
						}

						for (Pattern pt: ptList) {
							Pattern pte = new Pattern();
							pte.setMonitorId(entity.getMonitorId());
							pte.setPageOrderNo(pageOrderNo);
							pte.setPatternOrderNo(p.getPatterns().indexOf(pt));
							pte.setPattern(pt.getPattern());
							pte.setDescription(pt.getDescription());
							pte.setCaseSensitivityFlg(pt.getCaseSensitivityFlg());
							pte.setProcessType(pt.getProcessType());
							pte.setValidFlg(pt.getValidFlg());
							em.persist(pte);
							pte.relateToMonitorHttpScenarioPageInfoEntity(pe);
						}

						for (Pattern pte: pteList) {
							pe.getPatterns().remove(pte);
							em.remove(pte);
						}

						List<Variable> vList = new ArrayList<Variable>(p.getVariables());
						Iterator<Variable> viter = vList.iterator();
						List<Variable> veList = new ArrayList<Variable>(pe.getVariables());

						while (viter.hasNext()) {
							Variable v = viter.next();

							Iterator<Variable> veiter = veList.iterator();
							while (veiter.hasNext()) {
								Variable ve = veiter.next();
								if (v.getName().equals(ve.getId().getName())) {
									ve.setMatchingWithResponseFlg(v.getMatchingWithResponseFlg());
									ve.setValue(v.getValue());

									veiter.remove();
									viter.remove();
									break;
								}
							}
						}

						for (Variable v: vList) {
							Variable ve = new Variable();
							ve.setMonitorId(entity.getMonitorId());
							ve.setPageOrderNo(pageOrderNo);
							ve.setMatchingWithResponseFlg(v.getMatchingWithResponseFlg());
							ve.setName(v.getName());
							ve.setValue(v.getValue());
							em.persist(ve);
							ve.relateToMonitorHttpScenarioPageInfoEntity(pe);
						}

						for (Variable ve: veList) {
							pe.getVariables().remove(ve);
							em.remove(ve);
						}

						peiter.remove();
						piter.remove();

						break;
					}
				}
				pageOrderNo++;
			}

			for (Page p: pList) {
				Page pe = new Page();
				pe.setMonitorId(entity.getMonitorId());
				pe.setPageOrderNo(http.getPages().indexOf(p));
				pe.setUrl(p.getUrl());
				pe.setDescription(p.getDescription());
				pe.setStatusCode(p.getStatusCode());
				pe.setPost(p.getPost());
				pe.setPriority(p.getPriority());
				pe.setMessage(p.getMessage());
				em.persist(pe);
				pe.relateToMonitorHttpScenarioInfoEntity(entity);
				
				for (Pattern pt: p.getPatterns()) {
					Pattern pte = new Pattern();
					pte.setMonitorId(entity.getMonitorId());
					pte.setPageOrderNo(pe.getPageOrderNo());
					pte.setPatternOrderNo(p.getPatterns().indexOf(pt));
					pte.setPattern(pt.getPattern());
					pte.setDescription(pt.getDescription());
					pte.setCaseSensitivityFlg(pt.getCaseSensitivityFlg());
					pte.setProcessType(pt.getProcessType());
					pte.setValidFlg(pt.getValidFlg());
					em.persist(pte);
					pte.relateToMonitorHttpScenarioPageInfoEntity(pe);
				}

				for (Variable v: p.getVariables()) {
					Variable ve = new Variable();
					ve.setMonitorId(entity.getMonitorId());
					ve.setPageOrderNo(pe.getPageOrderNo());
					ve.setName(v.getName());
					ve.setMatchingWithResponseFlg(v.getMatchingWithResponseFlg());
					ve.setValue(v.getValue());
					em.persist(ve);
					ve.relateToMonitorHttpScenarioPageInfoEntity(pe);
				}
			}

			for (Page pe: peList) {
				entity.getPages().remove(pe);
				em.remove(pe);
			}
	
			return true;
		}
	}
}
