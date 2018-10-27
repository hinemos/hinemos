/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.difference;

import java.util.ArrayList;
import java.util.List;




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.utility.difference.anno.OrderBy;

/**
 * OrderBy に指定した順番で、カラムの優先度を計算するクラス。
 * 
 * @version 2.0.0
 * @since 2.0.0
 * 
 *
 */
public class IndexComparator implements java.util.Comparator<ResultD> {

	private static Log logger = LogFactory.getLog(IndexComparator.class);

	private static class Pattern {
		public String name;
		public List<Pattern> children = new ArrayList<Pattern>();
		public List<String> names = new ArrayList<String>();
	}
	
	private Pattern root;
	
	public IndexComparator(OrderBy orderby) {
		root = new Pattern();
		root.name = "root";
		
		for (String prop: orderby.props) {
			prop = prop.replace(".", "@");
			String[] names = prop.split("@");

			Pattern top = root;
			int topIndex = 0;
			boolean isNew = false;
			{
				Pattern parent = root;
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					Pattern p = parent.children.isEmpty() ? null: parent.children.get(parent.children.size() - 1);
					if (p != null && p.name.compareToIgnoreCase(name) == 0) {
						parent = p;
						continue;
					}
	
					if (parent.name.equals("*")) {
						top = parent;
						topIndex = i;
					}

					isNew = true;
					break;
				}
			}
			
			if (isNew) {
				Pattern parent = top;
				for (int j = topIndex; j < names.length; j++) {
					Pattern p = new Pattern();
					p.name = names[j];
					parent.children.add(p);
					parent = p;
				}
			}
		}
	}
	
	private List<Long> index(String[] columnIds) {
		List<Long> indexs = new ArrayList<Long>();

		recusive(root, columnIds, 0, indexs);
		
		if (indexs.isEmpty()) {
			indexs.add((long)Integer.MAX_VALUE << 32);
		}

		return indexs;
	}
	
	private void recusive(Pattern parent, String[] columnIds, int offset, List<Long> indexs) {
		for (int i = 0; i < parent.children.size(); ++i) {
			Pattern child = parent.children.get(i);
			
			if (child.name.equals(columnIds[offset])) {
				if (child.children.isEmpty() && columnIds.length - 1 == offset) {
					indexs.add((long)i << 32);
					return;
				}
				else if (
					child.children.isEmpty() && columnIds.length - 1 != offset ||
					!child.children.isEmpty() && columnIds.length - 1 == offset
					) {
					continue;
				}
				else {
					indexs.add((long)i << 32);
					int size = indexs.size();
					recusive(child, columnIds, offset + 1, indexs);
					
					if (size == indexs.size()) {
						indexs.remove(indexs.size() - 1);
					}
					else {
						return;
					}
				}
			}
			else if (child.name.equals("*")) {
				boolean find = false;
				for (int j = 0; j < child.names.size(); ++j) {
					if (child.names.get(j).equals(columnIds[offset])) {
						find = true;
						indexs.add((((long)i) << 32) + j);
						int size = indexs.size();
						recusive(child, columnIds, offset + 1, indexs);
						if (size == indexs.size()) {
							indexs.remove(indexs.size() - 1);
						}
						else {
							for (int l = 0; l < columnIds.length; ++l) {
								logger.debug(columnIds[l]);
								logger.debug(indexs.get(l).toString());
							}
							return;
						}
					}
				}
				
				if (!find) {
					child.names.add(columnIds[offset]);
					indexs.add((((long)i) << 32) + child.names.size() - 1);
					int size = indexs.size();
					recusive(child, columnIds, offset + 1, indexs);
					if (size == indexs.size()) {
						indexs.remove(indexs.size() - 1);
					}
					else {
						for (int l = 0; l < columnIds.length; ++l) {
							logger.debug(columnIds[l]);
							logger.debug(indexs.get(l).toString());
						}
						return;
					}
				}
			}
		}
	}
	
	@Override
	public int compare(ResultD r1, ResultD r2) {
		List<Long> indexs1 = index(r1.getColumnId());
		List<Long> indexs2 = index(r2.getColumnId());
		
		long ret = indexs1.size() - indexs2.size();
		for (int i = 0; i < indexs1.size() && i < indexs2.size(); ++i) {
			ret = indexs1.get(i) - indexs2.get(i);
			if (ret != 0) {
				break;
			}
			if ((i == indexs1.size() - 1) || (i == indexs2.size() - 1)) {
				ret = indexs1.size() - indexs2.size();
				break;
			}
		}
		
		return ret > 0 ? 1:(ret < 0 ? -1: 0);
	}
}