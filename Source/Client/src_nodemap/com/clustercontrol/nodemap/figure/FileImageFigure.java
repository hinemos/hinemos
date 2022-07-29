/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.figure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.swt.graphics.Image;
import com.clustercontrol.nodemap.util.ImageManager;

/**
 * フィギュアクラス。
 * FacilityFigure, BgFigureクラスが継承する。
 * @since 1.0.0
 */
public abstract class FileImageFigure extends Figure {

	// ログ
	private static Log m_log = LogFactory.getLog( FileImageFigure.class );

	protected String m_filename;
	protected ImageFigure m_image;


	abstract public String getFacilityId();
	abstract public String getFacilityName();
	abstract public boolean isBuiltin();
	abstract public String getOwnerRoleId();
	abstract public void draw(String filename) throws Exception;
	abstract public void imageDraw(String filename) throws Exception;

	public String getFilename() {
		return m_filename;
	}

	public void setFilename(String m_filename) {
		this.m_filename = m_filename;
	}

	protected ImageFigure getImage(String managerName, String filename) throws Exception {
		Image image = ImageManager.loadIcon(managerName, filename);
		if (image == null) {
			return null;
		}
		return new ImageFigure(image);
	}

	public void removeImage() {
		if (m_image != null) {
			IFigure figure = m_image.getParent();
			if (figure != null) {
				figure.remove(m_image);
			} else {
				m_log.debug("figure is null");
			}
		} else {
			m_log.debug("m_image is null");
		}
	}
}
