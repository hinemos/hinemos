/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * REST向けのリソースメソッドに関する、ログの出力用アノテーション。
 * <br>
 * 操作ログ上の 機能名を指定します。
 */
@Retention(RetentionPolicy.RUNTIME)  
@Target({ 
	ElementType.TYPE
})
public @interface RestLogFunc {

	public enum LogFuncName {
		Default,// 規定値専用 あえて選択することはない
		Common,
		Repository,
		Agent,
		Access,
		Calendar,
		Notify,
		Infra,
		Monitor,
		MonSystemlog,
		MonLogfile,
		MonLogcount,
		MonAgent,
		MonCustom,
		MonHttp,
		MonHttpScenario,
		MonProcess,
		MonSql,
		MonSnmp,
		MonPing,
		MonSnmptrap,
		MonResource,
		MonPort,
		MonWinService,
		MonWinEvent,
		MonCustomtrap,
		MonCorrelation,
		MonCompound,
		MonBinaryFile,
		MonPcketCapture,
		MonJmx,
		Collector,
		Job,
		Maintenance,
		MailTemplate,
		NodeMap,
		JobMap,
		Reporting,
		Hub,
		xCloud,
		AWS,
		VMware,
		FilterSetting,
		Utility,
		Sdml,
		Ha,
		Rpa,
		Grafana
	};
	
	LogFuncName name();
	
}

