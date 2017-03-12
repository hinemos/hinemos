/**********************************************************************
 * Copyright (C) 2014 NTT DATA Corporation
 * This program is free software; you can redistribute it and/or
 * Modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2.
 * 
 * This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *********************************************************************/

package org.eclipse.jface.text;


import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * TextViewer for RAP
 * 
 * Used by com.clustercontrol.dialog.TextAreaDialog
 * 
 * @see org.eclipse.swt.widgets.Text
 * @since 5.0.0
 */
public class TextViewer extends Viewer implements ITextViewer{

	StyledText fTextWidget;
	IDocument fDocument;
	boolean editable = false;

	/**
	 * 
	 * @param parent
	 * @param style
	 */
	public TextViewer(Composite parent, int style) {
		fTextWidget = new StyledText(parent, style);
	}

	public void setDocument(IDocument doc, int start, int len){
		this.fDocument = doc;
		this.fTextWidget.setText(doc.get());
	}

	public void setHyperlinkPresenter(DefaultHyperlinkPresenter presenter){
		// Do nothing
	}

	public void setHyperlinkDetectors(IHyperlinkDetector[] detectors, int style){
		// Do nothing
	}

	public IDocument getDocument() {
		this.fDocument.set(fTextWidget.getText());
		return this.fDocument;
	}

	public void setEditable(boolean editable) {
		if (fTextWidget != null)
			fTextWidget.setEditable(editable);
	}

	@Override
	public StyledText getTextWidget() {
		return fTextWidget;
	}

	@Override
	public Control getControl() {
		return null;
	}

	@Override
	public Object getInput() {
		return null;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {
		
	}

	@Override
	public void setInput(Object input) {
		
	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {
		
	}

	public boolean isEditable() {
		return this.fTextWidget.isEnabled();
	}

	public IRegion getVisibleRegion() {

		IDocument document= getVisibleDocument();

		return new Region(0, document == null ? 0 : document.getLength());
	}

	private IDocument getVisibleDocument() {
		return fDocument;
	}

	public void setSelectedRange(int offset, int length) {
		
	}

	public int getTopInset() {
		return 0;
	}

	public int getTopIndex() {
		return 0;
	}

	public void setTopIndex(int i) {
		
	}

	public void setVisibleRegion(int offset, int length) {
		
	}

	public void resetVisibleRegion() {
		
	}

	public void setDocument(IDocument document) {
		this.fDocument = document;
		this.fTextWidget.setText(fDocument.get());
	}

	public void addViewportListener(IViewportListener iViewportListener) {
		
	}

	public Point getSelectedRange() {
		return null;
	}

	public int getBottomIndex() {
		return 0;
	}

	public void revealRange(int offset, int length) {
		
	}
	
	@Override
	public void addHelpListener(HelpListener listener) {
	}

	@Override
	public void setDocument(Document document, int modelRangeOffset,
			int modelRangeLength) {
		this.fDocument = document;
		this.fTextWidget.setText(fDocument.get());
	}
}
