/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util.validation;

import static com.clustercontrol.xcloud.common.CloudConstants.bundle_messages;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.clustercontrol.xcloud.common.CloudStringConstants;
import com.clustercontrol.xcloud.util.validation.annotation.CharacterLimit;
import com.clustercontrol.xcloud.util.validation.annotation.DoubleLimit;
import com.clustercontrol.xcloud.util.validation.annotation.FloatLimit;
import com.clustercontrol.xcloud.util.validation.annotation.IntegerLimit;
import com.clustercontrol.xcloud.util.validation.annotation.LongLimit;
import com.clustercontrol.xcloud.util.validation.annotation.RelationalRequiredInput;
import com.clustercontrol.xcloud.util.validation.annotation.RelationalRequiredSelect;
import com.clustercontrol.xcloud.util.validation.annotation.RequiredInput;
import com.clustercontrol.xcloud.util.validation.annotation.RequiredSelect;

public class ControlValidatorImpl implements ControlValidator {
	@Override
	public boolean validate(Dialog dialog) throws ValidateException, Exception {
		Class<? extends Dialog> clazz = dialog.getClass();
		
		Field[] fields = clazz.getDeclaredFields();
		
		for (Field field: fields) {
			if (!(Control.class.isAssignableFrom(field.getType())))
				continue;
			
			for(Annotation annotation: field.getAnnotations()){
				if(annotation instanceof RequiredInput){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));
						if(text.isEmpty()){
							String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
							labelName = labelName == null ? field.getName(): labelName;
							String errMsg = MessageFormat.format(CloudStringConstants.msgValidationRequiredInputMessage, labelName);
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if(annotation instanceof RequiredSelect){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getSelectionIndex", new Class[0]);
						int index = (Integer)(method.invoke(field.get(dialog), new Object[0]));
						if(index == -1){
							String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
							labelName = labelName == null ? field.getName(): labelName;
							String errMsg = bundle_messages.getString("validation.required_select.message", new String[]{labelName});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if (annotation instanceof CharacterLimit){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));
						int count = text.length();
						
						if(text.isEmpty()){
							continue;
						}
						
						int max = (Integer)(annotation.getClass().getMethod("max", new Class[0]).invoke(annotation, new Object[0]));
						int min = (Integer)(annotation.getClass().getMethod("min", new Class[0]).invoke(annotation, new Object[0]));
						String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
						labelName = labelName == null ? field.getName(): labelName;
						
						if(min == 0 && count > max){
							String errMsg = bundle_messages.getString("validation.character_limit_max.message", new String[]{labelName, String.valueOf(max)});
							throw new ValidateException(errMsg);
						} else if (count < min || count > max){
							String errMsg = bundle_messages.getString("validation.character_limit_period.message", new String[]{labelName, String.valueOf(min), String.valueOf(max)});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if (annotation instanceof IntegerLimit){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));

						if(text.isEmpty()){
							continue;
						}
						
						int max = (Integer)(annotation.getClass().getMethod("max", new Class[0]).invoke(annotation, new Object[0]));
						int min = (Integer)(annotation.getClass().getMethod("min", new Class[0]).invoke(annotation, new Object[0]));
						String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
						labelName = labelName == null ? field.getName(): labelName;

						Long inputValue = null;
						try{
							inputValue = Long.parseLong(text);
						} catch(NumberFormatException e){
							if(inputValue == null){
								String errMsg = bundle_messages.getString("validation.integer_limit.message", new String[]{labelName});
								throw new ValidateException(errMsg);
							}
						}

						if(inputValue > max){
							String errMsg = bundle_messages.getString("validation.integer_limit_max.message", new String[]{labelName, String.valueOf(max)});
							throw new ValidateException(errMsg);
						} else if (inputValue < min){
							String errMsg = bundle_messages.getString("validation.integer_limit_min.message", new String[]{labelName, String.valueOf(min)});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if (annotation instanceof LongLimit){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));

						if(text.isEmpty()){
							continue;
						}
						
						long max = (Long)(annotation.getClass().getMethod("max", new Class[0]).invoke(annotation, new Object[0]));
						long min = (Long)(annotation.getClass().getMethod("min", new Class[0]).invoke(annotation, new Object[0]));
						
						String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
						labelName = labelName == null ? field.getName(): labelName;

						Long inputValue = null;
						try{
							inputValue = Long.parseLong(text);
						} catch(NumberFormatException e){
							String errMsg = bundle_messages.getString("validation.integer_limit.message", new String[]{labelName});
							throw new ValidateException(errMsg);
						}

						if(inputValue > max){
							String errMsg = bundle_messages.getString("validation.integer_limit_max.message", new String[]{labelName, String.valueOf(max)});
							throw new ValidateException(errMsg);
						} else if (inputValue < min){
							String errMsg = bundle_messages.getString("validation.integer_limit_min.message", new String[]{labelName, String.valueOf(min)});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if (annotation instanceof FloatLimit){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));

						if(text.isEmpty()){
							continue;
						}
						
						float max = (Float)(annotation.getClass().getMethod("max", new Class[0]).invoke(annotation, new Object[0]));
						float min = (Float)(annotation.getClass().getMethod("min", new Class[0]).invoke(annotation, new Object[0]));
						String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
						labelName = labelName == null ? field.getName(): labelName;

						Float inputValue = null;
						try{
							inputValue = Float.parseFloat(text);
						} catch(NumberFormatException e){
							String errMsg = bundle_messages.getString("validation.decimal_limit.message", new String[]{labelName});
							throw new ValidateException(errMsg);
						}

						if(inputValue > max){
							String errMsg = bundle_messages.getString("validation.decimal_limit_max.message", new String[]{labelName, String.valueOf(max)});
							throw new ValidateException(errMsg);
						} else if (inputValue < min){
							String errMsg = bundle_messages.getString("validation.decimal_limit_min.message", new String[]{labelName, String.valueOf(min)});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if (annotation instanceof DoubleLimit){
					Boolean accessibility = null;
					try {
						accessibility = field.isAccessible();
						field.setAccessible(true);
						Method method = field.getType().getMethod("getText", new Class[0]);
						String text = (String)(method.invoke(field.get(dialog), new Object[0]));

						if(text.isEmpty()){
							continue;
						}
						
						double max = (Double)(annotation.getClass().getMethod("max", new Class[0]).invoke(annotation, new Object[0]));
						double min = (Double)(annotation.getClass().getMethod("min", new Class[0]).invoke(annotation, new Object[0]));
						String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
						labelName = labelName == null ? field.getName(): labelName;

						Double inputValue = null;
						try{
							inputValue = Double.parseDouble(text);
						} catch(NumberFormatException e){
							String errMsg = bundle_messages.getString("validation.decimal_limit.message", new String[]{labelName});
							throw new ValidateException(errMsg);
						}

						if(inputValue > max){
							String errMsg = bundle_messages.getString("validation.decimal_limit_max.message", new String[]{labelName, String.valueOf(max)});
							throw new ValidateException(errMsg);
						} else if (inputValue < min){
							String errMsg = bundle_messages.getString("validation.decimal_limit_min.message", new String[]{labelName, String.valueOf(min)});
							throw new ValidateException(errMsg);
						}
					} catch (NoSuchMethodException e) {
						System.out.println("No such method:" + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
					}
				} else if(annotation instanceof RelationalRequiredInput){
					Boolean accessibility = null;
					Field targetField = null;
					Boolean targetAccessibility = null;
					try {
						targetField = clazz.getDeclaredField((String)(annotation.getClass().getMethod("name", new Class[0]).invoke(annotation, new Object[0])));
						
						targetAccessibility = targetField.isAccessible();
						targetField.setAccessible(true);
						Method targetMethod = targetField.getType().getMethod("getSelection", new Class[0]);
						Boolean selection = (Boolean)(targetMethod.invoke(targetField.get(dialog), new Object[0]));
							
						if(selection){
							accessibility = field.isAccessible();
							field.setAccessible(true);
							Method method = field.getType().getMethod("getText", new Class[0]);
							String text = (String)(method.invoke(field.get(dialog), new Object[0]));
							if(text.isEmpty()){
								String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
								labelName = labelName == null ? field.getName(): labelName;
								String errMsg = bundle_messages.getString("validation.required_input.message", new String[]{labelName});
								throw new ValidateException(errMsg);
							}
						}
					} catch (NoSuchFieldException e){
						System.out.println("No such Field: " + e.getMessage());
						continue;
					} catch (NoSuchMethodException e) {
						System.out.println("No such method: " + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
						if(targetField != null && targetAccessibility != null){
							targetField.setAccessible(targetAccessibility);
						}
					}
				} else if(annotation instanceof RelationalRequiredSelect){
					Boolean accessibility = null;
					Field targetField = null;
					Boolean targetAccessibility = null;
					try {
						targetField = clazz.getDeclaredField((String)(annotation.getClass().getMethod("name", new Class[0]).invoke(annotation, new Object[0])));
						
						targetAccessibility = targetField.isAccessible();
						targetField.setAccessible(true);
						Method targetMethod = targetField.getType().getMethod("getSelection", new Class[0]);
						Boolean selection = (Boolean)(targetMethod.invoke(targetField.get(dialog), new Object[0]));
							
						if(selection){
							accessibility = field.isAccessible();
							field.setAccessible(true);
							Method method = field.getType().getMethod("getSelectionIndex", new Class[0]);
							int index = (Integer)(method.invoke(field.get(dialog), new Object[0]));
							if(index == -1){
								String labelName = (String)((Widget)field.get(dialog)).getData(labelKey);
								labelName = labelName == null ? field.getName(): labelName;
								String errMsg = bundle_messages.getString("validation.required_select.message", new String[]{labelName});
								throw new ValidateException(errMsg);
							}
						}
					} catch (NoSuchFieldException e){
						System.out.println("No such Field: " + e.getMessage());
						continue;
					} catch (NoSuchMethodException e) {
						System.out.println("No such method: " + e.getMessage());
						continue;
					} finally {
						if(accessibility != null){
							field.setAccessible(accessibility);
						}
						if(targetField != null && targetAccessibility != null){
							targetField.setAccessible(targetAccessibility);
						}
					}
				}
			}
		}
		
		return true;
	}
}
