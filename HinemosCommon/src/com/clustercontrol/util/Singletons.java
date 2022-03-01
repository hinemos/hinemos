/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * インスタンスをシングルトンとして管理するためのコンテナクラスです。
 * <p>
 * 個々のクラスでシングルトンパターンを実装してしまうと、テスト実行時にstaticな状態の管理が複雑になります。
 * 本クラスを使用することで、個々のクラスはシングルトンではないクラスとして作成が可能です。
 * ただし、あくまでも提供するのは唯一のインスタンスを生成・提供する機能のみです。
 * 例えば、シングルトンとして使用するためのスレッド安全性などは、個々のクラスで確保する必要があります。
 * 
 */
public class Singletons {
	private static final Log log = LogFactory.getLog(Singletons.class);
	
	private static ConcurrentMap<Class<?>, Object> instances;

	static {
		initialize();
	}
	
	/**
	 * クラスを初期化します。
	 */
	public static void initialize() {
		instances = new ConcurrentHashMap<>();
	}
	
	/**
	 * 指定したクラスのシングルトンを取得します。
	 * 
	 * @param clazz クラスオブジェクト。
	 * @return 指定されたクラスの唯一のオブジェクト。
	 * @throws RuntimeException デフォルトコンストラクタを使ったインスタンス化に失敗した場合に投げます。
	 */
	public static <T> T get(Class<T> clazz) {
		Object o = instances.get(clazz);
		if (o == null) {
			synchronized (clazz) {
				// 複数スレッドによるすり抜け防止のためのダブルチェック
				o = instances.get(clazz);
				if (o == null) {
					try {
						// Class#newInstance は Java9 で非推奨となるので使用しない
						Constructor<T> ctor = clazz.getDeclaredConstructor();
						o = ctor.newInstance();
						instances.put(clazz, o);
						log.info("get : Instantiated " + clazz.getName());
					} catch (Exception e) {
						log.error("Failed to instantiate " + clazz.getName(), e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		@SuppressWarnings("unchecked")
		T ret = (T) o;
		return ret;
	}

	/**
	 * 指定したクラスのインスタンスを{@link #get(Class)}が返すように設定します。
	 * 
	 * @param clazz クラスオブジェクト。
	 * @param instance インスタンス。
	 * @return 引数instanceを返します。
	 */
	public static <T> T set(Class<T> clazz, T instance) {
		instances.put(clazz, instance);
		log.info("set : Class " + clazz.getName() + " -> " + instance.getClass().getName());
		return instance;
	}

	/**
	 * 指定したクラスのインスタンスを{@link #get(Class)}が返すように設定します。
	 * <p>
	 * 紐づくクラスはインスタンスの{{@link #getClass()}}から決定します。
	 * 
	 * @param instance インスタンス。
	 * @return 引数instanceを返します。
	 */
	public static <T> T set(T instance) {
		instances.put(instance.getClass(), instance);
		log.info("set : Class " + instance.getClass().getName());
		return instance;
	}

	/**
	 * 指定されたクラスのシングルトンインスタンスが生成されているかどうかを返します。
	 * 
	 * @param clazz クラスオブジェクト。
	 * @return 生成されている場合はtrue、そうでなければfalse。
	 */
	public static boolean has(Class<?> clazz) {
		return instances.containsKey(clazz);
	}
	
	/**
	 * 指定したクラスのシングルトンを削除します。
	 * 
	 * @param clazz クラスオブジェクト。
	 * @return 削除したインスタンス。存在していなければ null。
	 */
	public static <T> T remove(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		T ret = (T) instances.remove(clazz);
		log.info("remove : Class " + clazz.getName());
		return ret;
	}

	/**
	 * 指定したクラスのインスタンスが既に生成されている場合、新しく生成しなおします。
	 * 未生成の場合は何もしません。
	 * <p>
	 * 本メソッドを呼ぶ直前に {@link #get(Class)} でインスタンスを取得したスレッドがあると、
	 * 同じクラスのインスタンスが同時に2つ存在している状況となる可能性があります。
	 * したがって、本メソッドを利用するクラスでは、そのような状況を考慮してください。
	 * 
	 * @param clazz クラスオブジェクト。
	 * @return 既存のインスタンス。存在していなければ null。
	 */
	public static <T> T update(Class<T> clazz) {
		T ret = remove(clazz);
		if (ret != null) {
			get(clazz);
		}
		return ret;
	}
}
