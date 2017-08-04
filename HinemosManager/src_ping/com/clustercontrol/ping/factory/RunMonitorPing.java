/*

 Copyright (C) 2006 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.ping.factory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.collect.bean.Sample;
import com.clustercontrol.collect.util.CollectDataUtil;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.bean.MonitorRunResultInfo;
import com.clustercontrol.monitor.run.factory.RunMonitor;
import com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType;
import com.clustercontrol.monitor.run.model.MonitorJudgementInfo;
import com.clustercontrol.performance.bean.CollectedDataErrorTypeConstant;
import com.clustercontrol.ping.bean.PingResult;
import com.clustercontrol.ping.bean.PingRunCountConstant;
import com.clustercontrol.ping.bean.PingRunIntervalConstant;
import com.clustercontrol.ping.model.PingCheckInfo;
import com.clustercontrol.ping.util.PingProperties;
import com.clustercontrol.ping.util.QueryUtil;
import com.clustercontrol.ping.util.ReachAddress;
import com.clustercontrol.ping.util.ReachAddressFping;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.util.apllog.AplLogger;


/**
 * ping監視を実行するクラス<BR>
 *
 * @version 4.0.0
 * @since 2.0.0
 */
public class RunMonitorPing extends RunMonitorNumericValueType {

	private static Log m_log = LogFactory.getLog( RunMonitorPing.class );

	/** ping監視情報 */
	private PingCheckInfo m_ping = null;

	/** 実行回数 */
	private int m_runCount = PingRunCountConstant.TYPE_COUNT_01;

	/** 実行間隔（ミリ秒） */
	private int m_runInterval = PingRunIntervalConstant.TYPE_SEC_01;

	/** タイムアウト（ミリ秒） */
	private int m_pingTimeout;

	/** メッセージ */
	private String m_message = null;

	/** オリジナルメッセージ */
	private String m_messageOrg = null;

	/** パケット紛失率（%） */
	private int m_lost = 0;

	/** 応答平均時間（ミリ秒） */
	private long m_average = 0;

	//ping実行
	private ReachAddress m_reachability = null;

	//fping実行
	private ReachAddressFping m_reachabilityFping = null;

	//fping利用時のfpingエラー出力メッセージ
	private ArrayList<String> m_MsgErr= null;

	//fping利用時のfpingエラー出力メッセージ
	private ArrayList<String> m_MsgErrV6= null;


	private Hashtable<String, String[]> m_Target;

	/**
	 * 
	 * コンストラクタ
	 * 
	 */
	public RunMonitorPing() {
		super();

		PingProperties.getProperties();
	}

	/**
	 * マルチスレッドを実現するCallableTaskに渡すためのインスタンスを作成するメソッド
	 * 
	 * @see com.clustercontrol.monitor.run.factory.RunMonitor#runMonitorInfo()
	 * @see com.clustercontrol.monitor.run.util.MonitorExecuteTask
	 */
	@Override
	protected RunMonitor createMonitorInstance() {
		return new RunMonitorPing();
	}

	/**
	 * ping数を取得を行います。<BR>
	 * 
	 * @param facilityId ファシリティID
	 * @return 値取得に成功した場合、true
	 */
	@Override
	public boolean collect(String facilityId) {

		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}
		m_message = "";
		m_messageOrg = "";
		m_lost = 0;
		m_average = 0;


		if(m_reachability == null){
			m_reachability = new ReachAddress(m_runCount, m_runInterval, m_pingTimeout);
		}

		try{
			// ノードの属性取得
			NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

			String ipNetworkNumber = info.getIpAddressV4();
			String nodeName = info.getNodeName();

			boolean result = m_reachability.isReachable(ipNetworkNumber, nodeName);
			m_message = m_reachability.getMessage();
			m_messageOrg = m_reachability.getMessageOrg();
			if(result){
				m_lost = m_reachability.getLost();
				m_average = m_reachability.getAverage();
				m_value = m_average;
			}
			return result;
		}
		catch(FacilityNotFound e){
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES_PING.getMessage();
			m_messageOrg = e.getMessage();
			return false;
		} catch(HinemosUnknown e){
			m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES_PING.getMessage();
			m_messageOrg = e.getMessage();
			return false;
		}
	}

	/**
	 * 　fpingによるping数の取得を行います。<BR>
	 * @param facilityId Ping対象のファシリティID(スコープ)collectのfacilityIDとは異なる
	 * @return 値取得に成功した場合、true
	 * @throws HinemosUnknown
	 */
	private boolean collectFping(ArrayList<String> facilityList, ArrayList<Integer> priporityList) throws HinemosUnknown {

		//ノードの収集時刻を保存
		if (m_now != null) {
			m_nodeDate = m_now.getTime();
		}

		//まずは、データを作ります。
		// hosts[] →　IPアドレスリスト(String の配列)
		// hostsv6[] → IPv6アドレスリスト(Stringの配列)
		// node    → IPアドレスとノード名のリスト
		// target  → nodoのリスト
		HashSet<String> hosts     = new HashSet<String>();
		HashSet<String> hostsv6   = new HashSet<String>();
		m_Target = new Hashtable<String, String[]>();

		String facilityId = null;
		int version = 4;
		String[] node;
		for(int index=0; index<facilityList.size(); index++){
			facilityId = facilityList.get(index);
			if(facilityId != null && !"".equals(facilityId)){

				node = new String[2];

				try{

					// ノードの属性取得
					NodeInfo info = new RepositoryControllerBean().getNode(facilityId);

					//m_log.error(facilityAttrMap.get(FacilityAttributeConstant.IPPROTOCOLNUMBER));
					//プロトコルのバージョンが指定されていればversionを指定する。

					if(info.getIpAddressVersion() != null){
						version = info.getIpAddressVersion();
					}else{
						version = 4;
					}

					if(version == 6){

						InetAddress[] ip = InetAddress.getAllByName(info.getIpAddressV6());

						if(ip.length != 1){
							//IPアドレスをInetAddressクラスでデコードしているのに1つじゃないときは
							//UnnownHostExcption
							UnknownHostException e = new UnknownHostException();
							m_log.info("collectFping() : "
									+ e.getClass().getSimpleName() + ", " + e.getMessage());
							throw e;
						}

						node[0] = ip[0].getHostAddress();

						if(node[0] != null && !node[0].equals("")){

							//IPアドレスをHashSetに入れていく。
							hostsv6.add(node[0]);
						}
					}else{
						node[0] = info.getIpAddressV4();
						if(node[0] != null && !node[0].equals("")){

							//IPアドレスをHashSetに入れていく。
							hosts.add(node[0]);
						}
					}
					if(node[0] != null && !node[0].equals("")){
						node[1] = info.getNodeName();
						//targetをつめていく。
						m_Target.put(facilityId,node);
					}
				}catch(FacilityNotFound e){
					m_message = MessageConstant.MESSAGE_COULD_NOT_GET_NODE_ATTRIBUTES_PING.getMessage();
					m_messageOrg = e.getMessage();
					return false;
				} catch (UnknownHostException e) {
					// 何もしない
				}
			}
		}

		if(m_reachabilityFping == null){
			m_reachabilityFping = new ReachAddressFping(m_runCount, m_runInterval, m_pingTimeout);
		}

		boolean result=true;
		boolean resultTmp=true;
		//IPv4のホストが在ればfpingを利用して監視
		if(hosts.size() !=0 ) {

			result = m_reachabilityFping.isReachable(hosts , 4);
			m_MsgErr = m_reachabilityFping.getM_errMsg();
		}
		//IPv6のホストが在ればfping6を利用して監視
		if(hostsv6.size() !=0 ) {

			resultTmp = m_reachabilityFping.isReachable(hostsv6 , 6);
			m_MsgErrV6 = m_reachabilityFping.getM_errMsg();

			//結果は&であわせる
			result = result & resultTmp;
		}
		return result;
	}

	/**
	 * 	fpingの標準出力から、IPアドレス毎の結果に整形します<BR>
	 *  nodeMapでも使用中のため、public。
	 * 
	 * @param hosts      fpingしたアドレスのリスト
	 * @param message    出力メッセージ
	 * @param count      ping回数
	 * @return (IPアドレス 応答率、平均応答時間)のハッシュテーブル
	 */
	public Hashtable<String, PingResult> wrapUpFping(ArrayList<String> messages,  int count, int version){

		Hashtable<String, PingResult> ret = new Hashtable<String, PingResult>();
		HashMap<String, String> normalMap = new HashMap<String, String>(); //IPアドレス, メッセージ
		HashMap<String, String> aberrantMap = new HashMap<String, String>(); //IPアドレス, メッセージ
		HashSet<String> hostSet = new HashSet<String>();

		/**
		 * データセットとして必要なものは以下の5つ
		 * 分割したオリジナルメッセージからデータを作る。
		 * 
		 **/
		String msg;
		String msgOrg;
		int lost;
		float average = 0;
		float reachRatio;

		//IPアドレスの正規表現パターン

		Pattern patternIp;
		if (version == 6){
			patternIp = Pattern.compile("^([0-9].*)|^(\\:\\:.*)");
		}else{
			patternIp = Pattern.compile("^([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}.*)");
		}

		//fpingの出力を分割するパターン
		Pattern patternSp = Pattern.compile("(\\s:\\s|\\s)+");
		//正常な出力パターン
		Pattern patternNormal = Pattern.compile("([0-9]+\\.[0-9]+|-)");

		//IPアドレス判定用
		Matcher matcherIp;
		Matcher matcherValue;

		String message;

		/*
		 * fpingの出力正式は
		 * 応答がある場合							127.0.0.1 : 0.10 1.23
		 * 応答がない場合							127.0.0.1 : - -
		 * その他(複数のIPから応答がある)			127.0.0.1 : duplicate for [0], xx bytes, x.xx ms
		 * その他(デフォルトGWが設定されていない)	127.0.0.1 : (空白)
		 * など･･･
		 * 多岐にわたるため、正常な応答を上記の「応答がある場合」、「応答がない場合」の
		 * 2つに絞り、それ以外のものは、メッセージの余白に追記する。
		 * さらに、正常以外だった該当のIPアドレスの情報が存在しない場合は、
		 * 重要度不明で通知する。
		 */


		/*
		 * 取得結果の要素チェック
		 */
		Iterator<String> itr = messages.iterator();
		m_log.debug("wrapUpFping(): start logic: " + messages.size());
		while(itr.hasNext()) {

			message = itr.next();
			m_log.debug("wrapUpFping(): checkpoint");

			//IPアドレスの形式が正しい場合は、処理を続行する
			boolean bValidIP = false;
			if (version == 6)
			{
				m_log.debug("wrapUpFping(): into IPv6 loop");
				String[] strs = message.split(" ");
				try
				{
					InetAddress.getByName(strs[0]);
					bValidIP = true;
				}
				catch (Exception e)
				{
					m_log.warn("wrapUpFping() invalid IPv6 adress: original message: " + message, e);
					m_log.warn("wrapUpFping() stack trace: "
							+ e.getClass().getSimpleName() + ", " + e.getMessage(), e);
				}
			}
			else
			{
				matcherIp = patternIp.matcher(message);
				if(matcherIp.matches())
				{
					bValidIP = true;
				}
			}
			if(bValidIP){

				//各IPの結果を要素ごとに確認する
				String[] strs = patternSp.split(message);

				//各IPに対する結果を判定
				boolean isNormal = true;
				for(int i = 1; i< strs.length; i++) {

					matcherValue = patternNormal.matcher(strs[i]);
					if(!matcherValue.matches()){
						isNormal = false;
					}
				}

				if(isNormal) {
					normalMap.put(strs[0], message);
					m_log.debug("wrapUpFping() : normalValue : " + message);
				}
				else {
					// 既に該当するIPアドレスに情報がある場合は、追記する
					if(aberrantMap.get(strs[0]) != null && !(aberrantMap.get(strs[0])).equals("")){
						String aberrantMessage = aberrantMap.get(strs[0]);
						aberrantMessage = aberrantMessage + "\n" + message;
						aberrantMap.put(strs[0], aberrantMessage);
					}
					else {
						aberrantMap.put(strs[0], message);
					}
					m_log.debug("wrapUpFping() : aberrantValue : " + message);
				}

				hostSet.add(strs[0]);
			}
		}



		/*
		 * チェックした内容を結果としてまとめる
		 */
		itr = hostSet.iterator();
		m_log.debug("wrapUpFping() : before into check result append loop");
		while(itr.hasNext()) {
			m_log.debug("wrapUpFping() : after into check result append loop");

			String host = itr.next();
			String normalMessage = normalMap.get(host);
			String aberrantMessage = aberrantMap.get(host);

			if(normalMessage != null && !normalMessage.equals("")) {

				// String の分割する。
				String[] strs = patternSp.split(normalMessage);

				/*以下の変数はメッセージを作成するために必要*/
				float max = 0;   	      		//最大応答時間(初期値0秒)
				float min = Float.MAX_VALUE;	//最小応答時間(初期値Floatの最大値)
				int num = 0;					//受信パケット

				//収集回数分だけ入っているデータを読み出す。
				for (int i = 1; i <= count; i++ ){

					if(strs[i].equals("-")) {

						//結果「-」である場合は何もしない

					}
					else {

						//パケット数はcount up
						num++;

						//最大応答時間のチェックと更新
						if(max < Float.parseFloat(strs[i])) {
							max =  Float.parseFloat(strs[i]);
						}

						//最小応答時間のチェックと更新
						if(min > Float.parseFloat(strs[i])) {
							min =  Float.parseFloat(strs[i]);
						}

						//平均応答時間
						average += Float.parseFloat(strs[i]);
					}
				}

				//平均なので、和を実行回数で割っておきます。
				average /= num;


				/*
				 * 出力結果の作成
				 */

				StringBuffer buffer = new StringBuffer();
				buffer.append("Pinging " + host  + " (" + host + ") .\n\n");

				// 受信パケットが0の場合
				if (num == 0) {
					//受信パケットが0の時はlost→100 reach→0
					lost = 100;
					reachRatio = 0;

					for (int i = 0 ; i < count; i++ )
						// buffer.append("From " + strs[0] + " icmp_seq="+ index +" Destination Host Unreachable");
						buffer.append("Reply from " + host + " icmp_seq="+ i +" Destination Host Unreachable\n");

					buffer.append("\nPing statistics for " + host + ":\n");
					buffer.append( "Packets: Sent = " + count + ", Received = " + num + ", Lost = " + (count-num) + " (" + lost + "% loss),");

				}
				else {
					lost = (count - num) * 100 / count;
					reachRatio = (float)num * 100 / count;

					buffer.append("\nPing statistics for " + host + ":\n");
					buffer.append( "Packets: Sent = " + count + ", Received = " + num + ", Lost = " + (count-num) + " (" + lost + "% loss),");
					buffer.append("Approximate round trip times in milli-seconds:\n");
					buffer.append("\tMinimum = " + min
							+ "ms, Maximum = " + max
							+ "ms, Average = " + average + "ms\n");
				}


				// 異常なメッセージも存在する場合は、msgOrgに追加
				if(aberrantMessage != null && !aberrantMessage.equals("")) {
					buffer.append("\n\n" + aberrantMessage + "\n");
				}


				msgOrg =buffer.toString();
				msg = "Packets: Sent = " + count + ", Received = " + num + ", Lost = " + (count-num) + " (" + lost + "% loss)";

				PingResult res = new PingResult(host, msg, msgOrg, lost, average, reachRatio);

				ret.put(host, res);

				m_log.debug("wrapUpFping() : success msg = " + msg + ", msgOrg = " +msgOrg);

				msg = "";
				msgOrg = "" ;
				lost = 100;
				average = 0;
				reachRatio = 0;

			}
			// 正常なメッセージが存在しない場合
			else {

				msg = "Failed to get a value.";
				msgOrg = "Failed to get a value.";
				if(aberrantMessage != null && !aberrantMessage.equals("")) {
					msgOrg = msgOrg + "\n\n" + aberrantMessage;
				}

				PingResult res = new PingResult(host, msg, msgOrg, -1, -1, -1);

				ret.put(host, res);

				m_log.debug("wrapUpFping() : failure msg = " + msg + ", msgOrg = " +msgOrg);

				msg = "";
				msgOrg = "" ;
			}

		}

		return ret;
	}

	/* (非 Javadoc)
	 * ping監視情報を設定
	 * @see com.clustercontrol.monitor.run.factory.OperationNumericValueInfo#setMonitorAdditionInfo()
	 */
	@Override
	protected void setCheckInfo() throws MonitorNotFound {

		// ping監視情報を取得
		if (!m_isMonitorJob) {
			m_ping = QueryUtil.getMonitorPingInfoPK(m_monitorId);
		} else {
			m_ping = QueryUtil.getMonitorPingInfoPK(m_monitor.getMonitorId());
		}

		// ping監視情報を設定
		if(m_ping.getRunCount() != null)
			m_runCount = m_ping.getRunCount().intValue();
		if(m_ping.getRunInterval() != null)
			m_runInterval = m_ping.getRunInterval().intValue();
		if(m_ping.getTimeout() != null)
			m_pingTimeout = m_ping.getTimeout().intValue();
	}


	/* (非 Javadoc)
	 * 判定結果を取得
	 * @see com.clustercontrol.monitor.run.factory.RunMonitorNumericValueType#getCheckResult(boolean)
	 */
	@Override
	public int getCheckResult(boolean ret) {
		// lowerLimit : 時間（ミリ秒）
		// upperLimit : パケット紛失率（％）

		int result = -1;
		MonitorJudgementInfo info = null;

		// 値取得の成功時
		if(ret){

			// 通知をチェック
			info = m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_INFO));
			if(m_lost < info.getThresholdUpperLimit() && m_average < info.getThresholdLowerLimit()){
				result = PriorityConstant.TYPE_INFO;
			}
			else {
				// 警告の範囲チェック
				info = m_judgementInfoList.get(Integer.valueOf(PriorityConstant.TYPE_WARNING));
				if(m_lost < info.getThresholdUpperLimit() && m_average < info.getThresholdLowerLimit()){
					result = PriorityConstant.TYPE_WARNING;
				}
				else{
					// 危険（通知・警告以外）
					result = PriorityConstant.TYPE_CRITICAL;
				}
			}
		}
		return result;
	}

	/* (非 Javadoc)
	 * ノード用メッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessage(int)
	 */
	@Override
	public String getMessage(int id) {
		return m_message;
	}

	/* (非 Javadoc)
	 * ノード用オリジナルメッセージを取得
	 * @see com.clustercontrol.monitor.run.factory.OperationMonitor#getMessageOrg(int)
	 */
	@Override
	public String getMessageOrg(int id) {
		return m_messageOrg;
	}



	/**
	 * ping監視を実行します。
	 * <p>
	 * <ol>
	 * <li>fpingに対応するためにRunMonitorのrunMonitorInfoをoverrideします。</li>
	 * <li>監視情報を取得し、保持します（{@link #setMonitorInfo(String, String)}）。</li>
	 * <li>判定情報を取得し、判定情報マップに保持します（{@link #setJudgementInfo()}）。</li>
	 * <li>チェック条件情報を取得し、保持します（{@link #setCheckInfo()}）。</li>
	 * <li>ファシリティ毎に監視を実行し、値を収集します。 （{@link #collect(String)}）。</li>
	 * <li>監視結果から、判定結果を取得します。 （{@link #getCheckResult(boolean)}）。</li>
	 * <li>監視結果から、重要度を取得します（{@link #getPriority(int)}）。</li>
	 * <li>監視結果を通知します（{@link #notify(boolean, String, int, Date)}）。</li>
	 * </ol>
	 * 
	 * @return 実行に成功した場合、</code> true </code>
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * 
	 * @see #setMonitorInfo(String, String)
	 * @see #setJudgementInfo()
	 * @see #setCheckInfo()
	 * @see #collect(String)
	 * @see #getCheckResult(boolean)
	 * @see #getPriority(int)
	 * @see #notify(boolean, String, int, Date)
	 */
	@Override
	protected boolean runMonitorInfo()throws MonitorNotFound, HinemosUnknown {

		m_now = HinemosTime.getDateInstance();
		m_priorityMap = new HashMap<Integer, ArrayList<String>>();
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_INFO),		new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_WARNING),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_CRITICAL),	new ArrayList<String>());
		m_priorityMap.put(Integer.valueOf(PriorityConstant.TYPE_UNKNOWN),	new ArrayList<String>());
		
		List<Sample> sampleList = new ArrayList<Sample>();
		
		// 監視基本情報を設定
		boolean run = this.setMonitorInfo(m_monitorTypeId, m_monitorId);
		if(!run){
			// 処理終了
			return true;
		}

		// 判定情報を設定
		setJudgementInfo();

		// チェック条件情報を設定
		setCheckInfo();

		// ファシリティIDの配下全ての一覧を取得
		ArrayList<String> facilityList = new RepositoryControllerBean().getExecTargetFacilityIdList(m_facilityId, m_monitor.getOwnerRoleId());
		if (facilityList.size() == 0) return true;


		// ファシリティ毎に監視情報を収集
		ArrayList<Integer> priorityList = new ArrayList<Integer>();

		// 収集値の入れ物を作成
		Sample sample = null;
		if(m_monitor.getCollectorFlg()){
			sample = new Sample(HinemosTime.getDateInstance(), m_monitor.getMonitorId());
		}

		//fping利用フラグがfalseの時はver2.2までの方式でping監視を行う。
		if(!PingProperties.isFpingEnable()){

			String facilityId = null;
			for(int index=0; index<facilityList.size(); index++){
				facilityId = facilityList.get(index);
				if(facilityId != null && !"".equals(facilityId)){

					// 監視値を収集
					boolean ret = collect(facilityId);

					// 監視値より判定結果を取得
					int checkResult = getCheckResult(ret);

					// スコープの値取得時刻を設定
					if(m_nodeDate > m_scopeDate){
						m_scopeDate = m_nodeDate;
					}

					// ノードのログ出力情報を送信
					// 監視管理へ通知
					if (!m_isMonitorJob) {
						notify(true, facilityId, checkResult, new Date(m_nodeDate));
					} else {
						m_monitorRunResultInfo = new MonitorRunResultInfo();
						MonitorJudgementInfo info = m_judgementInfoList.get(checkResult);
						if(info != null){
							m_monitorRunResultInfo.setPriority(info.getPriority());
						}
						else{
							m_monitorRunResultInfo.setPriority(m_failurePriority);
						}
						m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(m_messageOrg, m_message));
						m_monitorRunResultInfo.setNodeDate(m_nodeDate);
					}

					// 収集値の登録
					if(sample != null){
						int errorCode = -1;
						if(ret){
							errorCode = CollectedDataErrorTypeConstant.NOT_ERROR;
						}else{
							errorCode = CollectedDataErrorTypeConstant.UNKNOWN;
						}
						sample.set(facilityId, m_monitor.getItemName(), m_value, errorCode);
					}
				}
			}
		}//fping利用フラグがtrueの時は新しいping監視を行う。
		else{
			//	 監視値を収集
			//   fpingを利用する場合には、ファシリティのリストを与えて一気にpingします。
			boolean ret = collectFping(facilityList,priorityList);


			if(ret){
				Hashtable<String, PingResult> fpingResultSet = new Hashtable<String, PingResult>();
				Hashtable<String, PingResult> fpingResultSetV6 = new Hashtable<String, PingResult>();
				//データの整形を行います。
				if(m_MsgErr != null){
					fpingResultSet = wrapUpFping(m_MsgErr, m_runCount,4);
				}
				if(m_MsgErrV6 != null){
					m_log.debug("runMonitorInfo(): fpingResultSetV6 check");
					fpingResultSetV6 = wrapUpFping(m_MsgErrV6, m_runCount,6);
				}

				//IPv4の情報が存在しない場合は、IPv6の情報で上書きする
				m_log.debug("runMonitorInfo(): before fpingResultSet check");
				if( fpingResultSet.size() == 0){
					m_log.debug("runMonitorInfo(): after fpingResultSet check");
					fpingResultSet = fpingResultSetV6;
				}
				//IPv4の情報が存在する場合は、IPv6の情報を追加する
				else if(fpingResultSetV6 .size()!= 0){
					fpingResultSet.putAll(fpingResultSetV6);
				}

				//最初に呼び出されたIPアドレスのリスト
				Iterator<String> it = facilityList.iterator();

				String targetFacility;
				PingResult nodeResult;
				String[] node;

				while(it.hasNext()){

					//与えられたFacilityId個々に
					targetFacility = it.next();
					//ノード名、IPアドレスを取得
					node = m_Target.get(targetFacility);

					// ポーリングされていないノード(IPアドレス未記入のノード)を除く。
					if(node != null) {

						m_log.debug("runMonitorInfo(): before fpingResultSet.get()");
						nodeResult = fpingResultSet.get(node[0]);

						if(nodeResult ==null){
							m_log.debug("runMonitorInfo(): after fpingResultSet.get()");

							/* fping失敗で結果がありません。*/
							String[] args = {m_monitorId};
							String logMsg = "";
							if (m_MsgErr != null)
							{
								logMsg = m_MsgErr.toString();
							}
							if (m_MsgErrV6 != null)
							{
								logMsg += " , " + m_MsgErrV6.toString();
							}
							AplLogger.put(PriorityConstant.TYPE_CRITICAL, HinemosModuleConstant.MONITOR_PING, MessageConstant.MESSAGE_SYS_001_MON_PNG, args, logMsg);
							m_log.info("Fping no response.");

							/* fping失敗で結果がありません。*/
							sample.set(targetFacility, m_monitor.getItemName(), Double.NaN, CollectedDataErrorTypeConstant.UNKNOWN);
						}else{

							//結果データからイベントを生成するためにデータを読みだす。
							m_lost = nodeResult.getLost();
							//現行の使用の平均応答時間はmsec(long)のため変換する。
							m_average  = (long)nodeResult.getAverage();
							m_message  = nodeResult.getMesseage();
							m_messageOrg = nodeResult.getMesseageOrg();
							m_value = m_average;

							if(m_log.isDebugEnabled()){
								m_log.debug("runMonitorInfo() monitorId = " + m_monitorId + ", facilityId = " + targetFacility + ", average = " + nodeResult.getAverage() + ", value = " + m_value);
							}

							// 監視値より判定結果を取得
							int checkResult;
							// 値取得に失敗している場合
							if (m_lost == -1) {
								checkResult = getCheckResult(false);
							}
							else {
								checkResult = getCheckResult(true);
							}
							boolean collectorResult = true;
							if (m_lost == -1 || m_lost == 100) {
								collectorResult = false;
							}

							// スコープの値取得時刻を設定
							if(m_nodeDate > m_scopeDate){
								m_scopeDate = m_nodeDate;
							}

							// ノードのログ出力情報を送信
							// 監視管理へ通知
							if (!m_isMonitorJob) {
								notify(true, targetFacility, checkResult, new Date(m_nodeDate));
							} else {
								m_monitorRunResultInfo = new MonitorRunResultInfo();
								m_monitorRunResultInfo.setPriority(checkResult);
								m_monitorRunResultInfo.setMessageOrg(makeJobOrgMessage(m_messageOrg, m_message));
								m_monitorRunResultInfo.setNodeDate(m_nodeDate);
							}

							// 収集値の登録
							if(sample != null){
								int errorCode = -1;
								if(collectorResult){
									errorCode = CollectedDataErrorTypeConstant.NOT_ERROR;
								}else{
									errorCode = CollectedDataErrorTypeConstant.UNKNOWN;
								}
								sample.set(targetFacility, m_monitor.getItemName(), (double)nodeResult.getAverage(), errorCode);
							}
						}
					}
				}
			}
		}
		// 収集値をまとめて登録
		if(sample != null){
			sampleList.add(sample);
		}
		if(!sampleList.isEmpty()){
			CollectDataUtil.put(sampleList);
		}
		return true;
	}

	@Override
	protected String makeJobOrgMessage(String orgMsg, String msg) {
		if (m_monitor == null || m_monitor.getPingCheckInfo() == null) {
			return "";
		}
		String[] args = {
				String.valueOf(m_monitor.getPingCheckInfo().getRunCount()),
				String.valueOf(m_monitor.getPingCheckInfo().getRunInterval()),
				String.valueOf(m_monitor.getPingCheckInfo().getTimeout())};
		return MessageConstant.MESSAGE_JOB_MONITOR_ORGMSG_PING.getMessage(args)
				+ "\n" + orgMsg;
	}
}
