/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import com.clustercontrol.client.ui.actions.ActionFactoryX;
import com.clustercontrol.util.Messages;

/**
 * ActionBarAdvisorクラスを継承するクラス<BR>
 * RCPのActionBarの設定などを行うクラス
 *  クライアントの表示に利用します。
 * 
 * @version 2.0.0
 * @since 2.0.0
 */
public class ClusterControlActionBarAdvisor extends ActionBarAdvisor {
	//private IWorkbenchAction helpContentsAction;
	private IAction aboutAction;

	public ClusterControlActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		//ヘルプコンテンツアクション(org.eclipse.ui.internal.actions.HelpContentsAction)を作成  （現状使用しない）
		//helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		//register(helpContentsAction);

		//製品情報アクション(org.eclipse.ui.internal.about.AboutAction)を作成
		aboutAction = ActionFactoryX.ABOUT.create(window);
		//aboutAction.setText(aboutAction.getText());

		register(aboutAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		// ヘルプメニューを追加（現状使用しない）
		//MenuManager helpMenu = new MenuManager("&"+Messages.getString("help"),
		//		IWorkbenchActionConstants.M_HELP);
		// 製品情報用ヘルプメニューを追加
		MenuManager aboutMenu = new MenuManager(Messages.getString("help") + " (&H)", IWorkbenchActionConstants.M_HELP + "_about");

		//menuBar.add(helpMenu); //（現状使用しない）
		menuBar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(aboutMenu);

		//ヘルプコンテンツアクションを追加 （現状使用しない）
		//helpMenu.add(helpContentsAction);

		//製品情報アクションを追加
		aboutMenu.add(aboutAction);

		//ヘルプメニューを非表示とする。（現状使用しない）
		//helpMenu.setVisible(false);
	}
}
