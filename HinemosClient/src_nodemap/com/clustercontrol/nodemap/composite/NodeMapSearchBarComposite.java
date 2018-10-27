/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.composite;

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
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.WidgetTestUtil;

/**
 * Search Bar Composite for Nodemap
 * 
 * @version 6.1.0
 * @since 6.1.0
 */
public class NodeMapSearchBarComposite extends Composite{

	private static final Image ICON_SEARCH = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/find.png").createImage(); //$NON-NLS-1$
	//テキストボックスの横幅推奨値
	private static final int textBoxWidthHint =300;
	private NodeMapCanvasComposite m_compose;
	public NodeMapSearchBarComposite( Composite parent, int style, boolean enableKeyPress ){
		super( parent, style );
		createContents(parent, enableKeyPress);
	}

	protected void createContents( final Composite parent, boolean enableKeyPress ){
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);

		final Text txtSearch = new Text(this, SWT.SINGLE | SWT.BORDER );
		txtSearch.setMessage(Messages.getString( "facility.search.hint" ));
		WidgetTestUtil.setTestId(this, "search", txtSearch);
		//textBoxLayout setting
		//余裕幅があっても推奨幅までの表示（不足なら短縮）
		GridData textBoxLayout =new GridData(GridData.GRAB_HORIZONTAL);
		textBoxLayout.widthHint = textBoxWidthHint;
		txtSearch.setLayoutData( textBoxLayout );
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
						m_compose.doSearch(text.getText());
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
				m_compose.doSearch(txtSearch.getText());
			}
		});
	}
	public void setNodeMapCanvasComposite(NodeMapCanvasComposite target){
		m_compose = target;
	}
}
