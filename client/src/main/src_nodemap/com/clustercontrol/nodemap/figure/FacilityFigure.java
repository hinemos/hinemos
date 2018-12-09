/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.figure;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.ColorConstantsWrapper;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FocusBorder;
import org.eclipse.draw2d.FocusEvent;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import com.clustercontrol.bean.PriorityColorConstant;
import com.clustercontrol.bean.PriorityConstant;
import com.clustercontrol.nodemap.util.RelationViewController;
import com.clustercontrol.repository.bean.FacilityConstant;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.ws.nodemap.FacilityElement;

/**
 * アイコン(ノード、スコープ)画像のクラス。
 * @since 1.0.0
 */
public abstract class FacilityFigure extends FileImageFigure {

	// ログ
	private static Log m_log = LogFactory.getLog( FacilityFigure.class );

	private Layer m_layer; //背景
	private RoundedRectangle m_backGround;
	private Label m_label;
	// ツールチップ
	private Panel m_tooltip;
	private final FacilityElement m_element;
	private final String m_managerName;

	public FacilityFigure(String managerName, FacilityElement element){
		this.setFocusTraversable(true);
		this.setRequestFocusEnabled(true);

		// 背景色を白に設定する
		this.setBackgroundColor(ColorConstantsWrapper.white());

		m_element = element;
		m_managerName = managerName;
	}

	@Override
	public void draw(String filename) throws Exception {
		m_filename = filename;
		// 上書きのため、一度全部消す。
		this.removeAll();

		// レイアウトを設定する
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		layout.setStretchMinorAxis(false);
		this.setLayoutManager(layout);

		// 背景を作成する
		m_layer = new Layer();
		m_layer.setLayoutManager(new StackLayout());

		// 背景画像を作成する
		m_backGround = new RoundedRectangle();

		m_layer.add(m_backGround);
		imageDraw(filename);

		// 初期の色を設定
		setPriority(PriorityConstant.TYPE_NONE);

		// ツールチップを生成
		m_tooltip = new Panel();
		m_tooltip.setLayoutManager(new FlowLayout(false));
		this.setToolTip(m_tooltip);
		this.add(m_layer);
	}

	/**
	 * RegistImageの際にimageだけ再描画する必要があるため、
	 * drawメソッドから切り出してimageDrawメソッドを作成する。
	 * @throws Exception
	 */
	@Override
	public void imageDraw(String filename) throws Exception {
		removeImage();

		// 画像を設定する
		m_image = getImage(m_managerName, filename);

		int width = m_image.getImage().getImageData().width;
		int height = m_image.getImage().getImageData().height;
		m_backGround.setSize(width + 8, height + 8);
		m_layer.add(m_image);
	}

	@Override
	public void repaint(){
		super.repaint();
		drawLabel();
	}

	public void setPriority(int priority){
		if(m_backGround == null) {
			m_log.warn("setPriority(), setPriority m_backGround1 is null");
			return;
		}

		Color color = null;
		switch (priority) {
		case PriorityConstant.TYPE_INFO:
			color = PriorityColorConstant.COLOR_INFO;
			break;
		case PriorityConstant.TYPE_WARNING:
			color = PriorityColorConstant.COLOR_WARNING;
			break;
		case PriorityConstant.TYPE_UNKNOWN:
			color = PriorityColorConstant.COLOR_UNKNOWN;
			break;
		case PriorityConstant.TYPE_CRITICAL:
			color = PriorityColorConstant.COLOR_CRITICAL;
			break;
		default:
			color = PriorityColorConstant.COLOR_NONE;
			break;
		}
		if (!m_element.isValid()
				&& FacilityConstant.TYPE_NODE_STRING.equals(m_element.getTypeName())) {
			/*
			 * 枠は太く、priority色をつける。
			 * 中身は灰色。
			 */
			m_backGround.setLineWidth(3);
			m_backGround.setForegroundColor(color);
			m_backGround.setBackgroundColor(ColorConstantsWrapper.lightGray());
		} else {
			/*
			 * 枠は細く。
			 * 中身はpriority色。
			 */
			m_backGround.setLineWidth(1);
			m_backGround.setForegroundColor(ColorConstantsWrapper.darkGray());
			m_backGround.setBackgroundColor(color);
		}
	}

	@Override
	public void handleFocusGained(FocusEvent fe){
		m_log.debug("handleFocusGained " + fe);

		// ラベルを再描画
		setFocusLabel(true);

		/*
		 * イベントビューとステータスビューの表示を変更する。
		 */
		RelationViewController.updateScopeTreeView(m_element.getParentId(), m_element.getFacilityId());
		RelationViewController.updateStatusEventView(getFacilityId(), m_element.getParentId());
	}

	@Override
	public void handleFocusLost(FocusEvent fe){
		m_log.debug("handleFocusLost " + fe);

		// ラベルを再描画
		setFocusLabel(false);
	}

	public void addTooltip(String str){
		// ツールチップに内容を設定
		m_tooltip.add(new Label(str));
	}

	public FacilityElement getFacilityElement() {
		return m_element;
	}

	@Override
	public String getFacilityId() {
		return m_element.getFacilityId();
	}

	@Override
	public String getFacilityName() {
		return m_element.getFacilityName();
	}
	
	@Override
	public boolean isBuiltin() {
		return m_element.isBuiltin();
	}
	
	@Override
	public String getOwnerRoleId() {
		return m_element.getOwnerRoleId();
	}
	
	public RoundedRectangle getBackGround() {
		return m_backGround;
	}

	@Override
	public void remove(IFigure figure){
		// ラベルを削除する
		removeLabel();

		super.remove(figure);
	}

	@Override
	public void removeAll(){
		// ラベルを削除する
		removeLabel();

		super.removeAll();
	}

	/**
	 * FacilityElementからkeyに該当する属性値を返す
	 * 
	 * @param key
	 * @return 属性値
	 */
	protected String getFacilityElementProperty(String key) {
		String resultStr = "";

		List<FacilityElement.Attributes.Entry> entries = getFacilityElement().getAttributes().getEntry();
		for (FacilityElement.Attributes.Entry entry : entries) {
			if (key.equals(entry.getKey())) {
				resultStr = (String)entry.getValue();
				break;
			}
		}
		return resultStr;
	}

	/**
	 * ラベルを新規に生成する
	 */
	private void createLabel(){
		// ラベルの表示内容を設定
		String label = "";
		if (m_element != null) {
			label = HinemosMessage.replace(m_element.getFacilityName());
		}

		m_label = new Label(label);

		// 親のFigureに登録
		// このFigureが親のFigureのレイヤの何番目に配置されているのかインデックスを取得する
		int index = getParent().getChildren().indexOf(this);
		getParent().add(m_label, index);
		
		

		// ラベルのフォーカスを設定
		setFocusLabel(false);
	}

	/**
	 * ラベルを再描画する。
	 * Draw2D的には、FaclityFigureとは別のFigureオブジェクトとして描画する。
	 * よって、FaclityFigureの移動時には再描画が必要となる。
	 */
	private void drawLabel(){
		if(getParent() == null){
			return;
		}

		// ラベルを生成
		if (m_label == null) {
			createLabel();
		}

		Point location = new Point();
		location.x = getLocation().x + (m_backGround.getSize().width / 2) - (m_label.getSize().width / 2);
		location.y = getLocation().y + m_backGround.getSize().height + 2;
		Dimension dimension = new Dimension(-1, -1);
		Rectangle rectangle = new Rectangle(location, dimension);

		// ラベルの位置を移動する
		getParent().setConstraint(m_label, rectangle);
	}

	/**
	 * フォーカスにより表示を変更います
	 */
	private void setFocusLabel(boolean focus){
		// フォーカスされている場合
		if(focus == false){
			// FIXME サイズ指定がきかない
			// 最大サイズを設定
			// 横のサイズは背景画像の横のサイズに合わせる
			// ラベルの表示サイズを戻す
			//			m_label.setMaximumSize(new Dimension(32, 96));
			//			m_label.setPreferredSize(new Dimension(32, 96));
			//			m_label.setSize(new Dimension(32, 96));

			m_label.setBackgroundColor(ColorConstantsWrapper.white());
			m_label.setForegroundColor(ColorConstantsWrapper.black());
			m_label.setBorder(null);
			m_label.setOpaque(true);
		} else {
			// FIXME サイズ指定がきかない
			// ラベルの表示サイズを拡大
			//			m_label.setMaximumSize(new Dimension(1024, 96));

			m_label.setBackgroundColor(ColorConstantsWrapper.darkBlue());
			m_label.setForegroundColor(ColorConstantsWrapper.white());
			m_label.setBorder(new FocusBorder());
			m_label.setOpaque(true);
		}
	}
	
	public String getManagerName() {
		return m_managerName;
	}


	/**
	 * ラベルを削除します
	 */
	private void removeLabel(){
		IFigure parent = getParent();

		if(parent != null){
			parent.remove(m_label);
		}
	}
}
