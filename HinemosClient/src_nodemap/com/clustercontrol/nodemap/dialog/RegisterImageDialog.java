/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.clustercontrol.dialog.CommonDialog;
import com.clustercontrol.dialog.ValidateResult;
import com.clustercontrol.nodemap.editpart.MapViewController;
import com.clustercontrol.nodemap.figure.BgFigure;
import com.clustercontrol.nodemap.figure.FileImageFigure;
import com.clustercontrol.nodemap.figure.NodeFigure;
import com.clustercontrol.nodemap.figure.ScopeFigure;
import com.clustercontrol.nodemap.util.NodeMapEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.nodemap.InvalidRole_Exception;

/**
 * 画像ファイル変更用ダイアログ
 * @since 1.0.0
 */
public class RegisterImageDialog extends CommonDialog {

	// ログ
	private static Log m_log = LogFactory.getLog( RegisterImageDialog.class );

	// ----- instance フィールド ----- //

	//Composite用オブジェクト
	private Label filename  = null;
	private Combo comboImg = null;

	private MapViewController m_controller = null;
	private FileImageFigure m_figure = null;
	private boolean m_iconFlag = false;
	private String m_manager = null;

	// ----- コンストラクタ ----- //
	/**
	 * 指定したファシリティIDをホストノードとして仮想ノードを検索、登録するダイアログのインスタンスを返す。
	 * @param parent 親のシェルオブジェクト
	 * @param facilityId 初期表示するノードのファシリティID
	 */
	public RegisterImageDialog(Shell parent, MapViewController controller,
			FileImageFigure figure) {
		super(parent);
		m_controller = controller;
		m_figure = figure;
		m_manager = controller.getManagerName();
		if (m_figure instanceof NodeFigure || m_figure instanceof ScopeFigure) {
			m_iconFlag = true;
		}
	}

	// ----- instance メソッド ----- //
	/**
	 * ダイアログエリアを生成します
	 */
	@Override
	protected void customizeDialog(Composite parent) {
		//共通変数
		GridData gridData = null; // GridDataは毎回必ず作成すること
		GridLayout layout = null;

		////////////////////////////////////////
		// ダイアログ全体の設定
		////////////////////////////////////////

		Shell shell = this.getShell();

		// タイトル
		String title = com.clustercontrol.nodemap.messages.Messages.getString("file.select.image");
		if (m_figure != null) {
			title += " [" + HinemosMessage.replace(m_figure.getFacilityId()) + "]";
		}
		shell.setText(title);

		layout = new GridLayout(1,true);
		layout.marginWidth = 10;
		layout.marginHeight = 10;
		layout.numColumns = 1;
		parent.setLayout(layout);

		Group group = new Group(parent, SWT.SHADOW_NONE);

		group.setLayout(new GridLayout(1, false));

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		group.setLayoutData(gridData);

		// 3-1
		filename = new Label(group, SWT.NONE);
		filename.setText(com.clustercontrol.nodemap.messages.Messages.getString("file.name"));

		// 3-2
		comboImg = new Combo(group, SWT.NONE);
		comboImg.setLayoutData(gridData);

		int counter = 0;
		m_log.debug("make Composite 151 " + m_iconFlag);
		Collection<String> filenameArray = null;
		if (m_figure == null) {
			m_log.debug("m_figure is null");
			return;
		}
		
		String figureFilename = m_figure.getFilename();
		m_log.debug("RegistImageDialog makeComposite filename=" + figureFilename +
				", class=" + m_figure.getClass().getSimpleName());

		try {
			NodeMapEndpointWrapper wrapper = NodeMapEndpointWrapper.getWrapper(m_manager);
			if (m_iconFlag) {
				filenameArray = wrapper.getIconImagePK();
				if (figureFilename == null) {
					figureFilename = "node";
				}
			} else {
				filenameArray = wrapper.getBgImagePK();
				if (figureFilename == null) {
					figureFilename = "default";
				}
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));
			return;
		} catch (Exception e) {
			// 上記以外の例外
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.hinemos.failure.unexpected") + ", " + e.getMessage() + " " + e.getClass().getSimpleName());
			return;
		}

		int tmpCounter = 0;
		for (String registedFilename : filenameArray) {
			comboImg.add(registedFilename);
			/*
			 * 現在のアイコンにフォーカスするため、counterを回す。
			 */
			if (registedFilename != null && registedFilename.equals(figureFilename)) {
				counter = tmpCounter;
			}
			tmpCounter ++;
		}
		comboImg.select(counter);
	}

	/**
	 * OKボタンのテキストを返します。
	 * 
	 * @return OKボタンのテキスト
	 */
	@Override
	protected String getOkButtonText() {
		return Messages.getString("ok");
	}

	/**
	 * キャンセルボタンのテキストを返します。
	 */
	@Override
	protected String getCancelButtonText() {
		return Messages.getString("close");
	}

	/**
	 * 入力値チェックをします。
	 * 
	 * @return 検証結果
	 */
	@Override
	protected ValidateResult validate() {

		ValidateResult result = null;

		// TODO

		return result;
	}

	/**
	 * OKボタンのメイン処理
	 */
	@Override
	protected void okPressed(){
		String filename = comboImg.getText();
		try {
			if (m_iconFlag) {
				m_log.debug("filename = " + filename + " class=" + m_figure);
				if (m_figure instanceof NodeFigure || m_figure instanceof ScopeFigure) {
					m_controller.setIconName(m_figure.getFacilityId(), filename);
					m_figure.imageDraw(filename);
				}
			} else {
				m_log.debug("filename(bg) = " + filename);
				if (m_figure instanceof BgFigure) {
					m_controller.setBgName(filename);
					m_figure.imageDraw(filename);
				}
			}
		} catch (InvalidRole_Exception e) {
			// アクセス権なしの場合、エラーダイアログを表示する
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					Messages.getString("message.accesscontrol.16"));

		} catch (Exception e) {
			m_log.warn("okPressed(), " + e.getMessage(), e);
			MessageDialog.openInformation(
					null,
					Messages.getString("message"),
					e.getMessage() + " " + e.getClass().getSimpleName());
		}
		super.okPressed();
	}
}
