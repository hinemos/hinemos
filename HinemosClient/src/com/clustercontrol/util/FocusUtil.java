/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.util;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

public class FocusUtil {
	private static Log m_log = LogFactory.getLog( FocusUtil.class );
	
	private String lastClazz = null;
	private IContextActivation activation = null;
	private ArrayList<String> dialogList = new ArrayList<>();
	
	/** Get singleton */
	private static FocusUtil getInstance(){
		return SingletonUtil.getSessionInstance( FocusUtil.class );
	}
	
	public static void setView(String clazz) {
		m_log.trace("setView1 : " + clazz);
		String lastClazz = getInstance().lastClazz;
		if (clazz == null || clazz.equals(lastClazz)) {
			return;
		}
		m_log.trace("setView2 : " + clazz);
		getInstance().lastClazz = clazz;
		deactivateContext();
		activateContext();
		// RCPの場合のみ、コンテキストのdeactivate/activateを受けてツールバーを更新する(詳細は※1)
		if (TargetPlatformUtil.isRCP()) {
			if(m_log.isTraceEnabled()){
				m_log.trace("setView3 : clazz = " + clazz );
			}
			updateToolBar(lastClazz);
			updateToolBar(clazz);
			/* ※1
			 * RCPにおける下記事象へのアドホック対応。
			 * - ビュー作成時、ツールバーボタンのツールチップにショートカットキーが表示されない。
			 * - 「追加」「更新」などの、"ビュー作成後に変化がないボタン"は、永遠にショートカットキーが
			 *	 表示されることがない。
			 * - 「コピー」などの、"ビュー作成後に有効状態が切り替わるなどの変化があるボタン"については、
			 *	 変化の際の更新でショートカットキーが表示されるようになる。
			 * 
			 * 次のような動作に改善される。
			 * - 現在有効なキーバインドであればツールチップにショートカットキーが表示される。
			 *	 これはRAPと同じ動作。
			 *	 「現在有効な」というのは、例えば、監視設定[一覧]ビューがアクティブな状態の場合、
			 *	 監視設定[一覧]ビューのツールバーではショートカットキーが表示され、
			 *	 監視設定[通知]ビューのツールバーには表示されない、ということ。
			 *	 (監視設定[通知]ビューがアクティブになれば、逆になる。)
			 * - RAPとは異なり、disabledなボタンにも表示されてしまう。
			 *	 これは簡単には修正できないと思われる。
			 *
			 *FIXME
			 * 本事象は、RAPでは発生しないため、eclipseの不具合である可能性が高い。
			 * 今後、クライアントで採用しているeclipseが 4.5 よりも新しくなった場合、
			 * この処理をコメントアウトしても問題ないかを確認すべき。
			 */	
		}

	}
	
	public static void setDialog(String dialog) {
		deactivateContext();
		getInstance().dialogList.add(dialog);
	}
	public static void unsetDialog(String dialog) {
		getInstance().dialogList.remove(dialog);
		if (getInstance().dialogList.isEmpty()){
			activateContext();
		}
	}
	
	private static void activateContext() {
		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
		if (contextService.getDefinedContextIds().contains(getInstance().lastClazz)) {
			getInstance().activation = contextService.activateContext(getInstance().lastClazz);
			m_log.debug("  activateContext " + getInstance().lastClazz);
		} else {
			// ここは通らないはず。
			// 通ってしまった場合は、plugin.xmlの <extension point="org.eclipse.ui.contexts"> を修正すること
			m_log.warn("notDefined!! : " + getInstance().lastClazz);
		}
	}
	private static void deactivateContext() {
		if (getInstance().activation == null) {
			return;
		}
		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
		m_log.debug("deactivateContext " + getInstance().activation.getContextId());
		contextService.deactivateContext(getInstance().activation);
		getInstance().activation = null;
	}
	
	/**
	 * 指定されたビューのツールバーを更新する。
	 * 本メソッドの実行により、現在のコンテキストに応じてツールチップのショートカット表記が変更される。
	 * 
	 * @param viewId ビューID(ViewPartクラスのFQCN)。nullの場合は何もしない。
	 */
	private static void updateToolBar(String viewId) {
		if(m_log.isDebugEnabled()){
			m_log.debug("updateToolBar: " + viewId);
		}

		if (viewId == null) {
			return;
		}
	
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewId);
		if (view == null) {
			//別パースぺクティブのViewIdが指定された場合は該当のビューは見つからない
			m_log.debug("updateToolBar: Not found, viewId=" + viewId);
			return;
		}
	
		for (IContributionItem it : view.getViewSite().getActionBars().getToolBarManager().getItems()) {
			it.update();
		}
	}
}
