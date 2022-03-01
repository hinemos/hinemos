/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d;

class ClickableEventHandler extends MouseMotionListener.Stub implements
		MouseListener, FigureListener, ChangeListener, KeyListener,
		FocusListener {

	private MouseEvent lastEvent;

	public void focusLost(FocusEvent fe) {
		Clickable loser = (Clickable) fe.loser;
		loser.repaint();
		loser.getModel().setArmed(false);
		loser.getModel().setPressed(false);
	}

	public void focusGained(FocusEvent fe) {
		Clickable clickable = (Clickable) fe.gainer;
		clickable.repaint();
	}

	public void figureMoved(IFigure source) {
		if (lastEvent == null)
			return;
		mouseDragged(lastEvent);
	}

	public void handleStateChanged(ChangeEvent change) {
		Clickable clickable = (Clickable) change.getSource();
// RAP [am] no mouse over
//		if (change.getPropertyName() == ButtonModel.MOUSEOVER_PROPERTY
//				&& !clickable.isRolloverEnabled())
//			return;
// RAPEND: [am]
		clickable.repaint();
	}

	public void mouseDoubleClicked(MouseEvent me) {
	}

	public void mouseDragged(MouseEvent me) {
		lastEvent = me;
		Clickable click = (Clickable) me.getSource();
		ButtonModel model = click.getModel();
		if (model.isPressed()) {
			boolean over = click.containsPoint(me.getLocation());
			model.setArmed(over);
			model.setMouseOver(over);
		}
	}

	public void mouseEntered(MouseEvent me) {
		Clickable click = (Clickable) me.getSource();
		click.getModel().setMouseOver(true);
		click.addFigureListener(this);
	}

	public void mouseExited(MouseEvent me) {
		Clickable click = (Clickable) me.getSource();
		click.getModel().setMouseOver(false);
		click.removeFigureListener(this);
	}

	public void mouseMoved(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		if (me.button != 1)
			return;
		lastEvent = me;
		Clickable click = (Clickable) me.getSource();
		ButtonModel model = click.getModel();
		click.requestFocus();
		model.setArmed(true);
		model.setPressed(true);
		me.consume();
	}

	public void mouseReleased(MouseEvent me) {
		if (me.button != 1)
			return;
		ButtonModel model = ((Clickable) me.getSource()).getModel();
		if (!model.isPressed())
			return;
		model.setPressed(false);
		model.setArmed(false);
		me.consume();
	}

	public void keyPressed(KeyEvent ke) {
		ButtonModel model = ((Clickable) ke.getSource()).getModel();
		if (ke.character == ' ' || ke.character == '\r') {
			model.setPressed(true);
			model.setArmed(true);
		}
	}

	public void keyReleased(KeyEvent ke) {
		ButtonModel model = ((Clickable) ke.getSource()).getModel();
		if (ke.character == ' ' || ke.character == '\r') {
			model.setPressed(false);
			model.setArmed(false);
		}
	}

}
