package com.clustercontrol.snmptrap.util;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.snmptrap.model.TrapCheckInfo;

public class CharsetUtil {

	private static Log m_log = LogFactory.getLog( CharsetUtil.class );

	/**
	 * SNMPTRAP監視の文字コード変換処理のための事前チェックをします。<BR>
	 * 
	 * @param info
	 * @throws HinemosUnknown
	 */
	public static void checkCharset(MonitorInfo info) throws HinemosUnknown {
		m_log.debug("checkCharset() : start monitorId = " + info.getMonitorId());

		// SNMPTRAPのチェック条件
		TrapCheckInfo trapInfo = info.getTrapCheckInfo();
		if(trapInfo == null){
			String msg = "It is not the definition of snmptrap monitor";
			m_log.info(msg);
			throw new HinemosUnknown(msg);
		}

		// 変換しない場合はreturn
		if (!trapInfo.getCharsetConvert()) {
			m_log.debug("checkCharset() : start monitorId = " + info.getMonitorId() + " CHARSET_CONVERT_OFF");
			return;
		}

		// 変換する場合は文字セットを確認する
		String charsetName = trapInfo.getCharsetName();
		try {
			if (!Charset.isSupported(charsetName)) {
				String msg = "[" + charsetName + "] is not supported charset name!";
				m_log.info(msg);
				throw new HinemosUnknown(msg);
			}
		} catch (IllegalCharsetNameException e) {
			String msg = "[" + charsetName + "] is not supported charset name!";
			m_log.info(msg);
			throw new HinemosUnknown(msg);
		} catch (Exception e) {
			String msg = e.getMessage();
			m_log.warn(msg, e);
			throw new HinemosUnknown(msg, e);
		}
		m_log.debug("checkCharset() : end monitorId = " + info.getMonitorId());
	}


}
