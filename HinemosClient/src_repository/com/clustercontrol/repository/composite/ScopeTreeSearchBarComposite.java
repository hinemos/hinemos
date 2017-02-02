/**********************************************************************
 * Copyright (C) 2016 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package com.clustercontrol.repository.composite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.composite.FacilityTreeComposite;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * Search Bar Composite for TreeViewer
 * 
 * @version 5.1.0
 * @since 5.1.0
 */
public class ScopeTreeSearchBarComposite extends Composite{

	private static final Image ICON_SEARCH = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/find.png").createImage(); //$NON-NLS-1$

	/**
	 * コンストラクタ
	 * @param parent 親コンポジット
	 * @param style スタイル
	 * @param enableKeyPress ボタン押下により検索を行うか
	 */
	public ScopeTreeSearchBarComposite( FacilityTreeComposite parent, int style, boolean enableKeyPress ){
		super( parent, style );
		createContents(parent, enableKeyPress);
	}

	/**
	 * コンポジットを生成します。
	 * 
	 * ノードマップオプションで使用するためprotected
	 */
	protected void createContents( final FacilityTreeComposite parent, boolean enableKeyPress ){
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);

		final Text txtSearch = new Text(this, SWT.SINGLE | SWT.BORDER );
		txtSearch.setMessage(Messages.getString( "facility.search.hint" ));
		WidgetTestUtil.setTestId(this, "search", txtSearch);

		txtSearch.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		if( enableKeyPress ){
			txtSearch.addTraverseListener( new TraverseListener(){
				@Override
				public void keyTraversed( TraverseEvent e ){
					Text text = (Text)e.widget;
					switch(e.detail){
					case(SWT.TRAVERSE_ESCAPE):
						text.setText("");
						text.setFocus();
						break;
					case(SWT.TRAVERSE_RETURN):
						text.selectAll();
						parent.doSearch(text.getText());
						break;
					default:
						break;
					}
				}
			});
		}

		// Create buttons
		ToolBar bar = new ToolBar(this, SWT.FLAT);
		ToolItem btnSearch = new ToolItem(bar, SWT.NONE);
		WidgetTestUtil.setTestId(this, "search", btnSearch);

		btnSearch.setImage( ICON_SEARCH );
		btnSearch.setToolTipText( Messages.getString( "search" ) );

		btnSearch.addSelectionListener( new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				txtSearch.setFocus();
				txtSearch.selectAll();
				parent.doSearch(txtSearch.getText());
			}
		});
	}
}
