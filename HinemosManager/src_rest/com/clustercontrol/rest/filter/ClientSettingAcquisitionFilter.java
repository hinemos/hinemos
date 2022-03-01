/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.filter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.RestHeaderConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.util.HinemosTime;

/**
 * httpヘッダーから処理に必要な設定情報を取得し、スレッドローカルに保存する（クライアント向けAPI用）
 */
@Priority(FilterPriorities.HEADER_DETECTOR)
public class ClientSettingAcquisitionFilter implements ContainerRequestFilter {
	private static final Log log = LogFactory.getLog(ClientSettingAcquisitionFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) {
		//言語設定取得
		String acceptLanguage = requestContext.getHeaders().getFirst(RestHeaderConstant.CLIENT_LANG_SET);
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOCALE_LIST, getLocale(acceptLanguage));
		if (log.isDebugEnabled()) {
			log.debug("filter() : " + RestHeaderConstant.CLIENT_LANG_SET + " = " + acceptLanguage);
		}
		// 日時書式設定取得
		String restsClientDatetimeFormat = requestContext.getHeaders().getFirst(RestHeaderConstant.CLIENT_DT_FORMAT);
		if (log.isDebugEnabled()) {
			log.debug("filter() : " + RestHeaderConstant.CLIENT_DT_FORMAT + " = " + restsClientDatetimeFormat);
		}
		SimpleDateFormat formater = null;
		if (restsClientDatetimeFormat != null) {
			try {
				formater = new SimpleDateFormat(restsClientDatetimeFormat);
				formater.setTimeZone(HinemosTime.getTimeZone());
			} catch (IllegalArgumentException e) {
				String message = String.format("'" + RestHeaderConstant.CLIENT_DT_FORMAT + "' is invalid. format=[%s]",
						restsClientDatetimeFormat);
				log.warn("filter :" + message);
			}
		}
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.REST_DATETIME_FORMAT, formater);
		
		//Hinemosクライアントのバージョン取得
		String restHinemosClientVer = requestContext.getHeaders().getFirst(RestHeaderConstant.CLIENT_VERSION);
		if(restHinemosClientVer != null) {
			HinemosSessionContext.instance().setProperty(HinemosSessionContext.REST_HINEMOS_CLIENT_VERSION, restHinemosClientVer);
		}
		if (log.isDebugEnabled()) {
			log.debug("filter() : " + RestHeaderConstant.CLIENT_VERSION + "=" + restHinemosClientVer);
		}

	}

	private static List<Locale> getLocale(String acceptLanguage) {
		List<Locale> localeList = new ArrayList<Locale>();
		if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
			List<LanguageRange> rangeList = LanguageRange.parse(acceptLanguage);
			for (LanguageRange range : rangeList) {
				localeList.add(new Locale(range.getRange()));
			}
		}
		localeList.add(Locale.getDefault());
		return localeList;
	}
}