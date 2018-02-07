/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.jobmanagement.composite;

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
 * Search Bar Composite for TreeViewer
 * 
 * @version 5.0.0
 * @since 5.0.0
 */
public class TreeSearchBarComposite extends Composite{

	private static final Image ICON_SEARCH = AbstractUIPlugin.imageDescriptorFromPlugin(ClusterControlPlugin.getPluginId(), "$nl$/icons/find.png").createImage(); //$NON-NLS-1$

	public TreeSearchBarComposite( JobTreeComposite parent, int style, boolean enableKeyPress ){
		super( parent, style );
		createContents(parent, enableKeyPress);
	}

	protected void createContents( final JobTreeComposite parent, boolean enableKeyPress ){
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);

		final Text txtSearch = new Text(this, SWT.SINGLE | SWT.BORDER );
		txtSearch.setMessage(Messages.getString( "jobmanagement.search.hint" ));
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
