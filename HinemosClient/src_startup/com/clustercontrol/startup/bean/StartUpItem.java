package com.clustercontrol.startup.bean;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class StartUpItem {

	// 描画対象のマップの情報を保持したモデル
	private String fieldId;
	private String target;
	private String msgTextTop;
	private String msgTextBottom;
	private String toolTipText;
	private int posX;
	private int posY;
	private String perspectiveName;
	private Color color;
	private int width;
	private int height;
	private Image image;
	private ImageFigure imageFigure;

	public ImageFigure getImageFigure() {
		return imageFigure;
	}
	public void setImageFigure(ImageFigure imageFigure) {
		this.imageFigure = imageFigure;
	}
	private String iconSpace;

	public String getIconSpace() {
		return iconSpace;
	}
	public void setIconSpace(String iconSpace) {
		this.iconSpace = iconSpace;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public void setFieldId(String fieldid) {
		fieldId = fieldid;
	}
	public String getFieldId() {
		return fieldId;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getTarget() {
		return target;
	}
	public String getMsgTextTop() {
		return msgTextTop;
	}
	public void setMsgTextTop(String msgtext) {
		msgTextTop = msgtext;
	}
	public String getMsgTextBottom() {
		return msgTextBottom;
	}
	public void setMsgTextBottom(String msgtext) {
		msgTextBottom = msgtext;
	}
	public String getToolTipText() {
		return toolTipText;
	}
	public void setToolTipText(String tooltiptext) {
		toolTipText = tooltiptext;
	}
	public int getPosX() {
		return posX;
	}
	public void setPosX(int posx) {
		posX = posx;
	}
	public int getPosY() {
		return posY;
	}
	public void setPosY(int posy) {
		posY = posy;
	}
	public void setPerspectiveName(String perspectivename) {
		perspectiveName = perspectivename;
	}
	public String getPerspectiveName() {
		return perspectiveName;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public Color getColor() {
		return color;
	}
}
