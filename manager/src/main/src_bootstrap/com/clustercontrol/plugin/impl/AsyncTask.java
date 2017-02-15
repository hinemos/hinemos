/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.plugin.impl;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.plugin.model.AsyncTaskEntity;
import com.clustercontrol.plugin.model.AsyncTaskEntityPK;
import com.clustercontrol.util.HinemosTime;

/**
 * AsyncTaskFactoryで生成されたRunnableを内包し、
 * AsyncWorkerPlugin内で非同期処理の実態となる実行クラス<br/>
 */
public class AsyncTask implements Runnable {

	public static final Log log = LogFactory.getLog(AsyncTask.class);

	// AsyncTaskFactoryで生成されたRunnableインスタンス
	private final Runnable _r;

	// 非同期処理のワーカー名/ID/Runnableに渡すパラメータ/永続化フラグ
	public final String _worker;
	public final long _taskId;
	public final Serializable _param;
	public final boolean _persist;

	public AsyncTask(Runnable r, String worker, Serializable param, long taskId, boolean persist) {
		_r = r;
		_worker = worker;
		_param = param;
		_taskId = taskId;
		_persist = persist;

		if (persist) {
			// 永続化データを登録する
			persist();
		}
	}

	/**
	 * 非同期で実行される処理の実態となるメソッド
	 */
	@Override
	public void run() {

		boolean runnableKicked = false;

		try {
			if (log.isDebugEnabled()) {
				log.debug("running new task. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
			}

			// AsyncTaskFactoryで生成されたRunnableクラスを実行する
			try {
				_r.run();
			} finally {
				runnableKicked = true;
			}

			if (log.isDebugEnabled()) {
				log.debug("task finished. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
			}
		} catch (Throwable t) {
			log.warn("task execution failure. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")", t);
		} finally {
			if (runnableKicked) {
				if (_persist) {
					// 永続化データによりrunを呼ぶことまで保障される。
					// Runnableクラスの成功可否に関わらず、永続化データを削除する
					// ユーザロジックキック中・後に予期せぬエラーが生じた場合は、
					// ユーザトランザクションをrollbackする必要があるため、
					// （仕方がなく）と別トランザクションで永続化データ削除を試みる
					remove();
				}
			}
		}
	}

	/**
	 * SerializableインスタンスをBinaryに変換するメソッド
	 * @param obj Serializableインスタンス
	 * @return シリアライズされたBinary
	 * @throws IOException
	 */
	public static byte[] encodeBinary(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		byte[] bytes = null;

		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			bytes = baos.toByteArray();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (baos != null) {
				baos.close();
			}
		}

		return bytes;
	}

	/**
	 * シリアライズされたBinaryをSerializableインスタンスに変換するメソッド
	 * @param bytes シリアライズされたBinary
	 * @return Serializableインスタンス
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Serializable decodeBinary(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream iaos = null;
		ObjectInputStream ois = null;
		Object obj = null;

		try {
			iaos = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(iaos);

			obj = ois.readObject();
		} finally {
			if (iaos != null) {
				iaos.close();
			}
			if (ois != null) {
				ois.close();
			}
		}

		return (Serializable)obj;
	}

	/**
	 * SerializableインスタンスをXMLに変換するメソッド
	 * @param obj Serializableインスタンス
	 * @return シリアライズされたXML
	 * @throws IOException
	 */
	public static String encodeXML(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = null;
		XMLEncoder enc = null;
		String xml = null;

		try {
			baos = new ByteArrayOutputStream();
			enc = new XMLEncoder(baos);
			enc.writeObject(obj);
			xml = baos.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			log.warn(e);
		} finally {
			if (enc != null) {
				enc.close();
			}
			if (baos != null) {
				baos.close();
			}
		}

		return xml;
	}

	/**
	 * シリアライズされたXMLをSerializableインスタンスに変換するメソッド
	 * @param xml シリアライズされたXML
	 * @return Serializableインスタンス
	 * @throws IOException
	 */
	public static Serializable decodeXML(String xml) throws IOException {
		ByteArrayInputStream bais = null;
		XMLDecoder dec = null;
		Serializable obj = null;

		try {
			bais = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			dec = new XMLDecoder(bais);
			obj = (Serializable)dec.readObject();
		} catch (UnsupportedEncodingException e) {
			log.warn(e);
		} finally {
			if (dec != null) {
				dec.close();
			}
			if (bais != null) {
				bais.close();
			}
		}

		return obj;
	}

	/**
	 * 指定されているワーカー関する永続化データの一覧を返すメソッド
	 * @param worker ワーカー名
	 * @return 永続化されているSerializableインスタンスのリスト
	 */
	public static List<Serializable> getRemainedParams(String worker) {
		List<Serializable> params = new ArrayList<Serializable>(20000);

		JpaTransactionManager tm = null;

		// EntityManager生成
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			Collection<AsyncTaskEntity> taskList = tm.getEntityManager().createNamedQuery("AsyncTaskEntity.findByWorkerOrderByCreateDatetimeTaskId", AsyncTaskEntity.class)
					.setParameter("worker", worker).getResultList();
			if (taskList == null) {
				log.info("no persisted tasks. (worker = " + worker + ")");
			} else {
				for (AsyncTaskEntity task : taskList) {
					try {
						Serializable param = AsyncTask.decodeBinary(task.getParam());
						params.add(param);
					} catch (Exception e) {
						log.warn("deserialization failure. (worker = " + worker + ", taskId = " + task.getId().getTaskId()
								+  ", createDatetime = " + task.getCreateDatetime() + ")", e);
					}
					tm.getEntityManager().remove(task);
				}
			}

			// コミット処理
			tm.commit();
		} catch (Exception e) {
			log.warn("failure of loading remained tasks. (worker = " + worker + ")", e);
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
		}

		return params;
	}

	/**
	 * AsyncTaskインスタンスを永続化するメソッド<br/>
	 */
	public void persist() {

		byte[] bytes = null;
		try {
			if (log.isDebugEnabled()) {
				log.debug("serializing parameter. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
			}
			// バイナリデータが含まれる可能性があるため、XMLエンコードではなく、binaryエンコードを利用する
			bytes = AsyncTask.encodeBinary(_param);
		} catch (IOException e) {
			log.warn("serialization failure. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")", e);
			return;
		}

		// EntityManager生成
		JpaTransactionManager tm = null;

		try {
			tm = new JpaTransactionManager();
			tm.begin();

			AsyncTaskEntity asyncTask = new AsyncTaskEntity(_worker, _taskId);
			asyncTask.setParam(bytes);
			asyncTask.setCreateDatetime(HinemosTime.currentTimeMillis());

			// コミット処理
			tm.commit();

			if (log.isDebugEnabled()) {
				log.debug("task is persisted. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
			}
		} catch (Exception e) {
			log.warn("skipped persistence of task. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")", e);
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
		}
	}

	/**
	 * AsyncTaskインスタンスの永続化データを削除するメソッド<br/>
	 */
	public void remove() {
		JpaTransactionManager tm = null;

		// EntityManager生成
		try {
			tm = new JpaTransactionManager();
			tm.begin();

			AsyncTaskEntity asyncTask = tm.getEntityManager().find(
					AsyncTaskEntity.class,
					new AsyncTaskEntityPK(_worker, _taskId),
					ObjectPrivilegeMode.READ);
			if (asyncTask == null) {
				log.warn("persisted task not found. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
			} else {
				tm.getEntityManager().remove(asyncTask);
				if (log.isDebugEnabled()) {
					log.debug("persisted task is removed. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")");
				}
			}

			// コミット処理
			tm.commit();
		} catch (Exception e) {
			log.warn("skipped removing persistence of task. (worker = " + _worker + ", taskId = " + _taskId + ", param = " + _param + ")", e);
			if (tm != null)
				tm.rollback();
		} finally {
			if (tm != null)
				tm.close();
		}
	}

}
