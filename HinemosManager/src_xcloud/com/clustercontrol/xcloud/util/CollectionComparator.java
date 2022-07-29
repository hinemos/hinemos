/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.ErrorCode;

public class CollectionComparator<O1, O2> {
	private static final Logger logger = Logger.getLogger(CollectionComparator.class);

	public static class Comparator<O1, O2> {
		public boolean match(O1 o1, O2 o2) throws CloudManagerException {return false;}
		public void matched(O1 o1, O2 o2) throws CloudManagerException {}
		public void afterO1(O1 o1) throws CloudManagerException {}
		public void afterO2(O2 o2) throws CloudManagerException {}
	}

	private Comparator<O1, O2> comparator;

	public CollectionComparator(Comparator<O1, O2> comparator) {
		this.comparator = comparator;
	}

	public void compare(Collection<O1> o1s, Collection<O2> o2s) throws CloudManagerException {
		List<O1> tempO1s = new ArrayList<>(o1s);
		List<O2> tempO2s = new ArrayList<>(o2s);
		
		Iterator<O1> o1Iter = tempO1s.iterator();
		while(o1Iter.hasNext()) {
			O1 o1 = o1Iter.next();
			Iterator<O2> o2Iter = tempO2s.iterator();
			while(o2Iter.hasNext()) {
				O2 o2 = o2Iter.next();
				if (comparator.match(o1, o2)) {
					comparator.matched(o1, o2);
					o1Iter.remove();
					o2Iter.remove();
					break;
				}
			}
		}

		for (O1 o1: tempO1s) {
			try{
				comparator.afterO1(o1);
			}catch(CloudManagerException e){
				if (!ErrorCode.UNEXPECTED.match(e) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(e) && e.getCause() == null) {
					if (logger.isDebugEnabled()) {
						logger.warn("AfterO1 failed. " + HinemosMessage.replace(e.getMessage()), e);
					} else {
						logger.warn("AfterO1 failed. " + HinemosMessage.replace(e.getMessage()));
					}
				} else {
					logger.warn("AfterO1 failed. " + HinemosMessage.replace(e.getMessage()), e);
				}
			}
		}
		for (O2 o2: tempO2s) {
			// Test: If try to insert a instance with facilityName longer than column width, that will cause EclipseLink internal exception and interrupt instance detection
			try{
				comparator.afterO2(o2);
			}catch(CloudManagerException e){
				if (!ErrorCode.UNEXPECTED.match(e) && !ErrorCode.HINEMOS_MANAGER_ERROR.match(e) && e.getCause() == null) {
					if (logger.isDebugEnabled()) {
						logger.warn("AfterO2 failed. " + HinemosMessage.replace(e.getMessage()), e);
					} else {
						logger.warn("AfterO2 failed. " + HinemosMessage.replace(e.getMessage()));
					}
				} else {
					logger.warn("AfterO2 failed. " + HinemosMessage.replace(e.getMessage()), e);
				}
			}
		}
	}

	public static <O1, O2> void compare(Collection<O1> o1s, Collection<O2> o2s, Comparator<O1, O2> comparator) throws CloudManagerException {
		new CollectionComparator<O1, O2>(comparator).compare(o1s, o2s);
	}
}
