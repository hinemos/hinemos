/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.ws.xcloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.internal.bind.api.JAXBRIContext;
import com.sun.xml.internal.bind.api.TypeReference;
import com.sun.xml.internal.ws.api.model.SEIModel;
import com.sun.xml.internal.ws.developer.JAXBContextFactory;
import com.sun.xml.internal.ws.developer.UsesJAXBContextFeature;

public class CloudManagerWSFeatureFactory {
	private final static List<Class<?>> additions = Collections.synchronizedList(new ArrayList<Class<?>>());
	
	
	public static void addClass(Class<?> clazz) {
		additions.add(clazz);
	}
	
	public static WebServiceFeature createWebServiceFeature() {
		return new UsesJAXBContextFeature(new JAXBContextFactory() {
			@Override
			@SuppressWarnings("rawtypes")
			public JAXBRIContext createJAXBContext(SEIModel arg0, List<Class> arg1, List<TypeReference> arg2) throws JAXBException {
				List<Class> classes = new ArrayList<Class>(arg1);
				classes.addAll(additions);
				return JAXBContextFactory.DEFAULT.createJAXBContext(arg0, classes, arg2);
			}
		});
	}
}
