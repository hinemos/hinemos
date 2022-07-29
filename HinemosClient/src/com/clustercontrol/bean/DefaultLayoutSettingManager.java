/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * デフォルトレイアウト設定管理クラス<BR>
 * 
 */
public class DefaultLayoutSettingManager {

	
	private static Log m_log = LogFactory.getLog( DefaultLayoutSettingManager.class );
	
	private File layoutFilePath;
	private long layoutFileLastTimestamp = -1;//起動直後に、ファイルなしの判定を通すため、初期値を-1に指定
	private LayoutRoot layoutRoot;
	
	public DefaultLayoutSettingManager(File layoutFilePath) {
		this.layoutFilePath = layoutFilePath;
	}
	
	public void reload() {
		
		if (!isReloadNecessary()) {
			return;
		}
		if (!layoutFilePath.exists()) {
			//ファイルが存在しない時、
			m_log.info("default Layout file not exists: " + layoutFilePath);
			this.layoutRoot = null;
		} else {
			m_log.info("default Layout file reload: " + layoutFilePath);
			this.layoutRoot = LayoutXmlParser.parseLayoutXml(layoutFilePath);
		}
	}
	
	private boolean isReloadNecessary() {
		if (layoutFileLastTimestamp != 0 && !layoutFilePath.exists()) {
			layoutFileLastTimestamp = 0;
			//ファイルが存在しないとき、削除されている可能性があるので、リロード（保持している情報）を破棄する
			return true;
		}
		if (layoutFileLastTimestamp != layoutFilePath.lastModified()) {
			//前回の最終更新日時から更新されている場合
			layoutFileLastTimestamp = layoutFilePath.lastModified();
			return true;
		}
		
		return false;
	}
	
	public ViewLayout getViewLayout(String viewId) {
		synchronized (this) {
			this.reload();
		}
		
		if (this.layoutRoot == null) {
			return null;
		}
		return layoutRoot.getView(viewId);
	}
	
	public static class LayoutRoot {
		private Map<String, ViewLayout> viewMap;
		
		public LayoutRoot() {
			this.viewMap = new ConcurrentHashMap<>();
		}
		
		public ViewLayout getView(String id) {
			return this.viewMap.get(id);
		}
		
		public ViewLayout addView(String id, ViewLayout view) {
			return this.viewMap.put(id, view);
		}
	}

	public static class ViewLayout {
		private String id;
		private Map<String, IViewItem> itemMap;
		
		public ViewLayout() {
			this.itemMap = new ConcurrentHashMap<>();
		}
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		@SuppressWarnings("unchecked")
		public <T extends IViewItem> T getViewItem(String id, Class<T> clazz) {
			IViewItem item = itemMap.get(id);
			if (item == null) {
				return null;
			}
			if (!item.getClass().equals(clazz)) {
				return null;
			}
			return (T)itemMap.get(id);
		}
		
		public void addViewItem(String id, IViewItem item) {
			itemMap.put(id, item);
		}
	}
	
	public static class ListLayout implements IViewItem {
		private String id;
		private List<ColumnLayout> columnList;

		public ListLayout() {
			this.columnList = new ArrayList<>();
		}
		
		@Override
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}

		public List<ColumnLayout> getColumnList() {
			return columnList;
		}

		public void addColumnList(ColumnLayout column) {
			this.columnList.add(column);
		}
	
	}
	
	public static class ColumnLayout {
		
		private String id;
		private int width;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public int getWidth() {
			return width;
		}
		public void setWidth(int width) {
			this.width = width;
		}
		
	}
	
	public interface IViewItem {
		public String getId();
	}
	
	public static class LayoutXmlParser {
		
		private static Log m_log = LogFactory.getLog( LayoutXmlParser.class );
		
		private static final String TAG_ROOT = "layout";
		private static final String TAG_VIEW = "view";
		private static final String TAG_LIST = "list";
		private static final String TAG_COLUMNS = "columns";
		private static final String TAG_COLUMN = "column";
		private static final String ATTR_ID = "id";
		private static final String ATTR_COLUMN_WIDTH = "width";
		
		
		public static LayoutRoot parseLayoutXml(File layoutFilePath) {
			LayoutRoot rootLayout = null;
			try {
				DocumentBuilder documentbuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = documentbuilder.parse(new FileInputStream(layoutFilePath));
				Element root = document.getDocumentElement();
				if (TAG_ROOT.equals(root.getNodeName())) {
					
					rootLayout = new LayoutRoot();
					
					for (int i = 0; i < root.getChildNodes().getLength(); i++) {
						Node child = root.getChildNodes().item(i);
						if (child.getNodeType() == Node.ELEMENT_NODE && TAG_VIEW.equals(child.getNodeName())) {
							ViewLayout view = parseView((Element) child);
							if (view != null && view.getId() != null) {
								rootLayout.addView(view.getId(), view);
							}
						}
					}
				}
				
			} catch (Exception e) {
				m_log.info("default Layout file parse error path: " + layoutFilePath);
				m_log.info(e);
			}
			return rootLayout;
		}
		
		public static ViewLayout parseView(Element element) {
			ViewLayout view = new ViewLayout();
			view.setId(element.getAttribute(ATTR_ID));
			
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				Node child = element.getChildNodes().item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && TAG_LIST.equals(child.getNodeName())) {
					ListLayout list = parseList((Element) child);
					if (list != null && list.getId() != null) {
						view.addViewItem(list.getId(), list);
					}
				}
			}
			return view;
		}
		
		public static ListLayout parseList(Element element) {
			ListLayout list = new ListLayout();
			list.setId(element.getAttribute(ATTR_ID));
			
			for (int i = 0; i < element.getChildNodes().getLength(); i++) {
				Node child = element.getChildNodes().item(i);
				if (child.getNodeType() == Node.ELEMENT_NODE && TAG_COLUMNS.equals(child.getNodeName())) {
					Element columns = (Element) child;
					for (int j = 0; j < columns.getChildNodes().getLength(); j++) {
						Node grandson = columns.getChildNodes().item(j);
						if (grandson.getNodeType() == Node.ELEMENT_NODE && TAG_COLUMN.equals(grandson.getNodeName())) {
							ColumnLayout column = parseColumn((Element) grandson);
							if (column != null && column.getId() != null) {
								list.addColumnList(column);
							}
						}
					}
				}
			}
			return list;
		}
		
		public static ColumnLayout parseColumn(Element element) {
			ColumnLayout column = new ColumnLayout();
			column.setId(element.getAttribute(ATTR_ID));
			Integer width = null;
			try {
				width = Integer.parseInt(element.getAttribute(ATTR_COLUMN_WIDTH));
				column.setWidth(width);
			} catch (NumberFormatException e) {
				//ignore
			}
			
			return column;
		}
		
	}
}
