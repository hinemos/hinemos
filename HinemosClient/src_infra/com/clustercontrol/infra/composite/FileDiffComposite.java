/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.composite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.clustercontrol.util.HtmlLoaderUtil;
import com.clustercontrol.util.Messages;

public class FileDiffComposite extends Composite {
	
	// ログ
	private static Log m_log = LogFactory.getLog(FileDiffComposite.class);


	private final Browser browser;
	private boolean loaded = false;
	private String left;
	private String right;
	private String leftHeader;
	private String rightHeader;
	private Boolean isOverIE9 = false;

	public FileDiffComposite(Composite parent, int style) {
		super(parent, style);
		
		GridLayout layout = new GridLayout();
		this.setLayout(layout);
		GridData gridData = null;
		
		browser = new Browser(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1;
		browser.setLayoutData(gridData);
		
		loadDiff();
	}

	private void loadDiff() {
		HtmlLoaderUtil.load(browser, "Diff.html", "com/clustercontrol/infra/util/");
		browser.addProgressListener(new ProgressListener() {
			public void completed(ProgressEvent event) {
				// ページのロードが完了してからjavascriptを実行する
				loaded = true;
				
				// IE9未満は未対応のため警告メッセージを表示して、動作させない
				isOverIE9 = (Boolean)eval("modern = typeof window.getSelection === 'function';"
						+ "if (!modern) {alert('"
						+ Messages.getString("message.infra.under.ie9")
						+ "');}"
						+ "return modern;");
				
				if (!isOverIE9) {
					return;
				}
				
				applyHeader();
				
				// 差分表示
				eval("" + "$('#compare').mergely({"
						+ "cmsettings: { readOnly: true, lineNumbers: true },"
						+ "width: 'auto',"
						+ "height: 'auto',"
						+ "license: 'lgpl-separate-notice',"
						+ "resized: function(){"
						+ "var w = $('#compare').width();"
						+ "var hw = $('.mergely-margin').width() + $('.mergely-column').width();"
						+ "$('#header').width(w);"
						+ "$('#lhs_header, #rhs_header').width(hw);"
						+ "}"
						+ "});");
				
				applyInput();
			}

			public void changed(ProgressEvent event) {
			}
		});
	}

	private String escapeQuote(String input) {
		String output = input.replace("\\", "\\\\");
		output = output.replace("\r", "\\r");
		output = output.replace("\n", "\\n");
		output = output.replace("'", "\\'");
		output = output.replace("\0", "\\0");
		output = output.replace("\u2028", "\\u2028");
		output = output.replace("\u2029", "\\u2029");
		
		return output;
	}

	public void setHeader(String left, String right) {
		this.leftHeader = left;
		this.rightHeader = right;
		applyHeader();
	}
	
	private void applyHeader() {
		if (loaded) {
			eval("$('#lhs_header').text('"+escapeQuote(leftHeader)+"')");
			eval("$('#rhs_header').text('"+escapeQuote(rightHeader)+"')");
		}
	}
	
	public void setInput(String left, String right) {
		this.left = left;
		this.right = right;
		applyInput();
	}
	
	private void applyInput() {
		if (loaded) {
			eval("$('#compare').mergely('lhs', '"+escapeQuote(left)+"');");
			eval("$('#compare').mergely('rhs', '"+escapeQuote(right)+"');");
		}
	}

	private Object eval(String code) {
		Object result = null;
		try {
			if (loaded)
				result = browser.evaluate(code);
		} catch (Exception e) {
			m_log.error(e.getMessage(), e);
		}
		return result;
	}
}
