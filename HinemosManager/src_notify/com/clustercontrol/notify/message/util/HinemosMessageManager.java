/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.notify.message.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.notify.message.model.HinemosMessageJsonModel;
import com.clustercontrol.util.HinemosTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * メッセージ通知用のHinemosメッセージをキュー管理するクラスです。<BR>
 * 一定数メッセージ蓄積 または 一定時間経過でキューのメッセージを全送信します。
 */
public class HinemosMessageManager {

	private static final Log log = LogFactory.getLog(HinemosMessageManager.class);

	private static final String workerName = "HinemosMessageQueue";

	private static final Object lock;

	private static final ScheduledThreadPoolExecutor executor;

	/** 実行中のScheduledFuture.  findbugs対応にてvolatile化 */
	private static volatile ScheduledFuture<?> runningFuture = null;

	/** 送信バッファ */
	private static final List<String> buffer;

	static {
		lock = new Object();
		buffer = new ArrayList<>();

		executor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, workerName);
				t.setDaemon(true);
				return t;
			}
		});

		//初回のスケジュール実行を走らせる（以降はSendMessageTask.run()側で一定間隔実行される）
		execute(false);
	}

	/**
	 * キュー（送信バッファ）へHinemosメッセージを追加します。<BR>
	 * キュー蓄積数または時間経過でフィルタマネージャへ全送信します。
	 * 
	 * @param hinemosMessage HinemosメッセージのJSON文字列
	 */
	public static void add(String hinemosMessage) {
		synchronized (lock) {

			buffer.add(hinemosMessage);

			// キュー蓄積数が上限か判定。 上限の場合はスケジュールを中止して即送信
			int sizeThreshold = HinemosPropertyCommon.notify_message_buffer_threshold_size.getIntegerValue();
			if (buffer.size() >= sizeThreshold) {
				if (runningFuture != null) {
					runningFuture.cancel(true);
				}
				execute(true);
				return;
			}
		}
	}

	/**
	 * キューのメッセージを全送信します。<BR>
	 * 本メソッド実行はシャットダウン時にのみ呼ばれる想定です。<BR>
	 * シャットダウン処理が進行しないよう、同期的に処理実行します。
	 */
	public static void forceSendMessage() {

		// スケジュール実行を止める
		if (runningFuture != null) {
			runningFuture.cancel(true);
			runningFuture = null;
		}

		// メッセージ蓄積なしの場合は送信せず終了
		if (buffer == null || buffer.size() == 0) {
			return;
		}

		// 蓄積しているHinemosメッセージ送信
		String hinemosMessage = integrateMessage(new ArrayList<>(buffer));
		Future<?> future = SendMessageExecutor.sendMessage(hinemosMessage);

		// タイムアウトを指定して、送信処理完了を待つ
		long timeout = HinemosPropertyCommon.notify_message_buffer_shutdown_timeout.getNumericValue();
		try {
			future.wait(timeout);
		} catch (InterruptedException e) {
			log.warn("HinemosMessage notification canceled on shutdown.");
		}

		if (!future.isDone()) {
			log.warn("HinemosMessage notification timed out on shutdown.");
		}
	}

	/**
	 * 指定時間経過後にHinemosメッセージを送信するスレッドを開始します。
	 * 
	 * @param sendNow すぐに送信するかどうかのフラグ
	 */
	private static void execute(boolean sendNow) {
		long delay = 0L;
		if (!sendNow) {
			delay = HinemosPropertyCommon.notify_message_buffer_threshold_time.getNumericValue();
		}
		runningFuture = executor.schedule(new SendMessageTask(), delay, TimeUnit.MILLISECONDS);
	}

	/**
	 * メッセージ（通知情報）を集約します。
	 */
	private static String integrateMessage(List<String> messageList) {

		// 日付フォーマッタ（ミリ秒精度のISO8601）
		String formatString = "yyyy-MM-dd'T'hh:mm:ss.SSSXXX";
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		Date nowDate = HinemosTime.getDateInstance();

		// HinemosMessageのmessages要素変換
		List<HinemosMessageJsonModel.MessageJsonModel> messages = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();
		for (String message : messageList) {
			try {
				messages.add(mapper.readValue(message, HinemosMessageJsonModel.MessageJsonModel.class));
			} catch (JsonProcessingException e) {
				log.error("Failed to convert to json : " + message);
			}
		}

		// HinemosMessage変換
		HinemosMessageJsonModel hinemosMessage = new HinemosMessageJsonModel();
		hinemosMessage.setFormat("hinemos");
		hinemosMessage.setSourceId(HinemosPropertyCommon.notify_message_source_id.getStringValue());
		hinemosMessage.setDestinationId(HinemosPropertyCommon.notify_message_destination_id.getStringValue());
		hinemosMessage.setSentTime(format.format(nowDate));
		hinemosMessage.setMessages(messages.toArray(new HinemosMessageJsonModel.MessageJsonModel[0]));
		String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(hinemosMessage);
		} catch (JsonProcessingException e) {
			log.error("Failed to convert to json : hinemosMessage");
		}

		return jsonString;
	}

	/**
	 * Hinemosメッセージを送信をスケジュール実行するためのクラス
	 */
	private static class SendMessageTask implements Runnable {

		@Override
		public void run() {
			synchronized (lock) {

				// メッセージ蓄積なしの場合は送信せず、次実行をスケジュールする
				if (buffer == null || buffer.size() == 0) {
					execute(false);
					return;
				}

				String hinemosMessage = integrateMessage(new ArrayList<>(buffer));
				SendMessageExecutor.sendMessage(hinemosMessage);
				buffer.clear();

				// 次の実行をスケジュールする（一定間隔で送信実行するため）
				execute(false);
			}
		}
	}
}
