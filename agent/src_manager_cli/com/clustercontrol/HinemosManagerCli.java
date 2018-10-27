/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class HinemosManagerCli {
	private static final int DEFAULT_CONNECT_TIMEOUT = 10*1000;
	private static final int DEFAULT_RESPONSE_TIMEOUT = 60*1000;

	private String ip = null;
	private String port; 
	private String user = null;
	private String password = null;

	private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	private int responseTimeout = DEFAULT_RESPONSE_TIMEOUT;

	private String name;
	private String operation;
	private String attribute;
	private String[] operationArgs;
	private boolean doesOutputInfo;

	public HinemosManagerCli(String[] args) {
		parse(args);
	}

	private void invoke() {
		System.setProperty("sun.rmi.transport.connectionTimeout", String.valueOf(connectTimeout));
		System.setProperty("sun.rmi.transport.tcp.responseTimeout", String.valueOf(responseTimeout));

		try {
			JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", ip, port));
			Map<String, Object> env = new HashMap<String, Object>();
			if (user != null && password != null) {
				env.put(JMXConnector.CREDENTIALS, new String[]{user, password});
			}
			MBeanServerConnection mbsc = JMXConnectorFactory.connect(url, env)
					.getMBeanServerConnection();
			
			ObjectName mbeanName = new ObjectName("com.clustercontrol.mbean:type=" + name);
			if (doesOutputInfo) {
				printMBeanInfo(mbsc.getMBeanInfo(mbeanName));
				return;
			}

			Object ret;
			if (attribute != null) {
				ret = mbsc.getAttribute(mbeanName, attribute);
			} else {
				String[] signature = new String[operationArgs.length];
				for (int i = 0; i < signature.length; i++) {
					signature[i] = String.class.getName();
				}
				ret = mbsc.invoke(mbeanName, operation, operationArgs, signature);
			}
			System.out.println(ret);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	private void printMBeanInfo(MBeanInfo mbeanInfo) {
		MBeanAttributeInfo[] attributeInfos = mbeanInfo.getAttributes();
		System.out.println("Attributes:");
		for (MBeanAttributeInfo attributeInfo : attributeInfos) {
			System.out.println(String.format("\t%s: %s", attributeInfo.getName(), attributeInfo.getType()));
		}
		
		MBeanOperationInfo[] operationInfos = mbeanInfo.getOperations();
		System.out.println("Operations:");
		for (MBeanOperationInfo operationInfo : operationInfos) {
			MBeanParameterInfo[] paramInfos = operationInfo.getSignature();

			StringBuffer paramStr = new StringBuffer();
			for (MBeanParameterInfo paramInfo : paramInfos) {
				paramStr.append(paramInfo.getType() + ",");
			}
			if (paramStr.length() != 0) {
				paramStr.append(paramStr.substring(0, paramStr.length() - 1));
			}
			
			System.out.println(String.format("\t%s %s(%s)", operationInfo.getReturnType(), operationInfo.getName(), paramStr));
		}
	}

	private void parse(String args[]) {
		Options options = new Options();

		options.addOption("H", true, "Hinemos Manager's IP address  (ex. 127.0.0.1)");
		options.addOption("P", true, "Hinemos Manager's JMX port (ex. 7100)");
		options.addOption("u", true, "JMX User Name");
		options.addOption("p", true, "JMX Password");
		options.addOption("cto", true, "Connection Timeout [sec]");
		options.addOption("rto", true, "Response Timeout [sec]");
		options.addOption("n", true, "MBean Name (ex. ManagerMXBean)");
		options.addOption("o", true, "MBean Operation");
		options.addOption("a", true, "MBean Attribute");
		options.addOption("i", false, "MBean Info");

		CommandLineParser parser = new DefaultParser();
		CommandLine cl = null;
		try {
			cl = parser.parse(options, args, true);
		} catch (ParseException e) {
			HelpFormatter hFormatter = new HelpFormatter();
			hFormatter.printHelp(String.format("java %s [opts] [args]", HinemosManagerCli.class.getName()), options);
			System.exit(1);
		}

		ip = cl.getOptionValue("H", "127.0.0.1");
		port = cl.getOptionValue("P", "7100");
		
		user = cl.getOptionValue("u", "hinemos");
		password = cl.getOptionValue("p", "hinemos");
		
		connectTimeout = Integer.parseInt(cl.getOptionValue("cto", Integer.toString(DEFAULT_CONNECT_TIMEOUT)));
		responseTimeout = Integer.parseInt(cl.getOptionValue("rto", Integer.toString(DEFAULT_RESPONSE_TIMEOUT)));
		
		name = cl.getOptionValue("n", "ManagerMXBean");
		operation = cl.getOptionValue("o", null);
		attribute = cl.getOptionValue("a", null);
		doesOutputInfo = cl.hasOption("i");
		
		operationArgs = cl.getArgs();

		if (operation == null && attribute == null && !doesOutputInfo) {
			HelpFormatter hFormatter = new HelpFormatter();
			hFormatter.printHelp(String.format("java %s [opts] [args]", HinemosManagerCli.class.getName()), options);
			System.exit(1);
		}
	}
	
	public static void main(String args[]) {
		new HinemosManagerCli(args).invoke();
	}
}