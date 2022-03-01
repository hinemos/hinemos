/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * ビュー操作用のユーティリティを提供します。
 *
 * @since 6.2.0
 */
public class ViewUtil {

	private static Log log = LogFactory.getLog(ViewUtil.class);

	/**
	 * アクティブなページを返します。見つからない場合はnullを返します。
	 */
	public static IWorkbenchPage findActivePage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null) {
			log.info("findActivePage: Workbench not found.");
			return null;
		}
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null) {
			log.info("findActivePage: Active WorkbenchWindow not found.");
			return null;
		}
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			log.info("findActivePage: Active page not found.");
			return null;
		}
		return page;
	}
	
	/**
	 * アクティブなビューがclazzのインスタンスである場合はそのインスタンスを、そうでなければnullを返します。
	 * <p>
	 * アクティブなビューを探して処理を行いたい場合、通常はnull判定を考慮しなくてよい
	 * {@link #executeWithActive(Class, Consumer)}の使用を推奨しますが、
	 * checked例外を処理する関係で無名クラスの使用が逆効果である場合は、こちらを利用できます。
	 * 
	 * @param clazz 対象ビューを判別するためのクラスオブジェクト。
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findActive(Class<T> clazz) {
		String className = clazz.getName();
		IWorkbenchPage page = findActivePage();
		if (page == null) {
			log.info("findActive: Active page not found. class=" + className);
			return null;
		}
		IWorkbenchPart part = page.getActivePart();
		if (part == null) {
			log.debug("findActive: Active part not found. class=" + className);
			return null;
		}
		if (!clazz.isInstance(part)) {
			log.debug("findActive: Not found. class=" + className);
			return null;
		}
		return (T) part;
	}

	/**
	 * アクティブなページ上のビュー(複数の可能性があります)から、clazzのインスタンスを探して、
	 * リストで返します。
	 * 
	 * @param clazz 対象ビューを判別するためのクラスオブジェクト。
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> find(Class<T> clazz) {
		List<T> result = new ArrayList<>();

		String className = clazz.getName();
		IWorkbenchPage page = findActivePage();
		if (page == null) {
			log.info("find: Active page not found. class=" + className);
			return result;
		}

		for (IViewReference viewref : page.getViewReferences()) {
			// restore=false にすると起動直後のビューについてnullが返る。
			IWorkbenchPart view = viewref.getPart(true);
			if (view == null) {
				log.info("find: ViewReference return null. ID=" + viewref.getId());
				continue;
			}
			if (clazz.isInstance(view)) {
				result.add((T) view);
			}
		}
		return result;
	}

	/**
	 * アクティブなビューがclazzのインスタンスである場合は、consumerを呼びます。
	 * 
	 * @param clazz 対象ビューを判別するためのクラスオブジェクト。
	 * @param consumer ビューを利用するConsumer。
	 */
	public static <T> void executeWithActive(Class<T> clazz, Consumer<T> consumer) {
		T view = findActive(clazz);
		if (view != null) {
			consumer.accept(view);
		}
	}
	
	/**
	 * アクティブなページ上のビュー(複数の可能性があります)がclazzのインスタンスである場合は、consumerを呼びます。
	 * 
	 * @param clazz 対象ビューを判別するためのクラスオブジェクト。
	 * @param consumer ビューを利用するConsumer。
	 */
	public static <T> void executeWith(Class<T> clazz, Consumer<T> consumer) {
		for (T view : find(clazz)) {
			consumer.accept(view);
		}
	}
}
