/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.bean.Filter;
import com.clustercontrol.xcloud.common.CloudMessageConstant;
import com.clustercontrol.xcloud.util.CloudMessage;
import com.clustercontrol.xcloud.validation.annotation.DoubleRange;
import com.clustercontrol.xcloud.validation.annotation.FilterCondition;
import com.clustercontrol.xcloud.validation.annotation.FilterListCondition;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.IdentityNullAllow;
import com.clustercontrol.xcloud.validation.annotation.IntRange;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotEmpty;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.NotNullContainer;
import com.clustercontrol.xcloud.validation.annotation.Pattern;
import com.clustercontrol.xcloud.validation.annotation.Size;
import com.clustercontrol.xcloud.validation.annotation.ValidateNotifyRelationInfo;


public class ValidationUtil {
	public static abstract class AbstractValidator<A extends Annotation, T> implements Validator<A, T> {
		private String elementId;
		private String validationId;
		private String[] groups;
		
		@Override
		public String getElementId() {
			return elementId;
		}
		
		public String getElementIdCode() {
			return CloudMessage.getMessage(getElementId());
		}
		
		@Override
		public void setElementId(String elementId) {
			this.elementId = elementId;
		}
		
		@Override
		public String getValidationId() {
			return validationId;
		}
		
		@Override
		public void setValidationId(String validationId) {
			this.validationId = validationId;
		}
		
		@Override
		public String[] getGroups() {
			return groups;
		}
		
		@Override
		public void setGroups(String[] groups) {
			this.groups = groups == null ? new String[0]: groups;
		}
		
		public void validate(T property, String group) throws PluginException {
			if (groups != null) {
				if (groups.length != 0) {
					if (group == null) {
						return;
					}
					
					boolean doit = false;
					for (String g: groups) {
						if (g.equals(group)) {
							doit = true;
							break;
						}
					}
					if (!doit) {
						return;
					}
				}
			}

			internalValidate(property, group);
		}

		protected abstract void internalValidate(T property, String group) throws PluginException;
		
		protected CloudManagerException createValidationFault(String message) {
			CloudManagerException v = new CloudManagerException(message, "VALIDATION_ERROR");
			return v;
		}
		
		protected CloudManagerException createValidationFault(Exception e) {
			CloudManagerException v = new CloudManagerException(e);
			return v;
		}
	}
	
	public static class IdentityValidator extends AbstractValidator<Identity, String> {
		private int max;

		@Override
		public void init(Identity annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.max = annotation.max();
		}

		@Override
		protected void internalValidate(String property, String group) throws PluginException {
			try {
				CommonValidatorEx.validateId(CloudMessage.getMessage(getElementId()), property, max);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
		}
	}

	public static class SizeValidator extends AbstractValidator<Size, String> {
		private int min;
		private int max;

		@Override
		public void init(Size annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.max = annotation.max();
			this.min = annotation.min();
		}
		@Override
		protected void internalValidate(String property, String group) throws PluginException {
			try {
				CommonValidatorEx.validateString(CloudMessage.getMessage(getElementId()), property, false, min, max);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
		}
	}

	public static class IntRangeValidator<T extends Number> extends AbstractValidator<IntRange, T> {
		private long min;
		private long max;

		@Override
		public void init(IntRange annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.min = annotation.min();
			this.max = annotation.max();
		}
		@Override
		protected void internalValidate(T property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			try {
				CommonValidatorEx.validateLong(CloudMessage.getMessage(getElementId()), property.longValue(), min, max);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
		}
	}
	
	public static class DoubleRangeValidator extends AbstractValidator<DoubleRange, Double> {
		private double min;
		private double max;

		@Override
		public void init(DoubleRange annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.min = annotation.min();
			this.max = annotation.max();
		}
		@Override
		protected void internalValidate(Double property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			try {
				CommonValidatorEx.validateDouble(CloudMessage.getMessage(getElementId()), property, min, max);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
		}
	}
	
	public static class PatternValidator extends AbstractValidator<Pattern, String> {
		private java.util.regex.Pattern pattern;

		@Override
		public void init(Pattern annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.pattern = java.util.regex.Pattern.compile(annotation.pattern());
		}
		@Override
		protected void internalValidate(String property, String group) throws PluginException {
			if (property == null) {
				return;
			}
			
			if(!pattern.matcher(property).matches()){
				throw createValidationFault(CloudMessageConstant.VALIDATION_PATTERN.getMessage(getElementIdCode(), pattern.pattern()));
			}
		}
	}
	
	public static class IdNullAllowValidator extends AbstractValidator<IdentityNullAllow, String> {
		private int max;

		@Override
		public void init(IdentityNullAllow annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());

			this.max = annotation.max();
		}

		@Override
		protected void internalValidate(String property, String group) throws PluginException {
			if (property == null || property.isEmpty()) {
				return;
			}
			
			try {
				CommonValidatorEx.validateId(CloudMessage.getMessage(getElementId()), property, max);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
		}
	}
	
	public static class NotEmptyValidator extends AbstractValidator<NotEmpty, List<?>> implements ContainerValidator<NotEmpty, List<?>> {
		@Override
		public void init(NotEmpty annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(List<?> property, String group) throws PluginException {
			if (property != null && property.isEmpty()) {
				throw createValidationFault(CloudMessageConstant.VALIDATION_NOTNULL.getMessage(getElementIdCode()));
			}
		}
	}

	public static class NotNullValidator extends AbstractValidator<NotNull, Object> {
		@Override
		public void init(NotNull annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(Object property, String group) throws PluginException {
			if (property == null) {
				throw createValidationFault(CloudMessageConstant.VALIDATION_NOTNULL.getMessage(getElementIdCode()));
			}
		}
	}

	public static class IntoEntityValidator extends AbstractValidator<Into, Object> {
		@Override
		public void init(Into annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(Object property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			ValidationUtil.getEntityValidator().validate(property, group);
		}
	}

	public static class FilterConditionValidator extends AbstractValidator<FilterCondition, Filter> {
		String[] filters;
		
		@Override
		public void init(FilterCondition annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
			filters = annotation.filters();
		}

		@Override
		protected void internalValidate(Filter property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			String filterName = null;
			boolean doit = true;
			if (filters != null && filters.length > 0) {
				doit = false;
				for (String f: filters) {
					filterName = property.getName();
					if (f.equals(filterName)) {
						doit = true;
						break;
					}
				}
			}

			
			if (!doit) {
				StringBuilder sb = new StringBuilder(); 
				for (String f: filters) {
					if (sb.length() != 0) {
						sb.append(',');
					}
					sb.append(f);
				}
				String filtersString = sb.toString();
				throw createValidationFault(CloudMessageConstant.VALIDATION_FILTERCONDITION.getMessage(getElementIdCode(), filterName, filtersString));
			}
		}
	}

	public static class FilterListConditionValidator extends AbstractValidator<FilterListCondition, List<Filter>> implements ContainerValidator<FilterListCondition, List<Filter>> {
		String[] requiredFilters;
		
		@Override
		public void init(FilterListCondition annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
			requiredFilters = annotation.requiredFilters();
		}

		@Override
		protected void internalValidate(List<Filter> property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			boolean doit = true;
			if (requiredFilters != null && requiredFilters.length > 0) {
				List<String> required = new ArrayList<String>(Arrays.asList(requiredFilters));
				Iterator<String> requiredIter = required.iterator();
				while (requiredIter.hasNext()) {
					String filterName = requiredIter.next();
					for (Filter f: property) {
						if (f.getName().equals(filterName)) {
							requiredIter.remove();
							break;
						}
					}
				}
				
				if (required.size() > 0) {
					doit = false;
				}
			}
				
			if (!doit) {
				StringBuilder sb = new StringBuilder(); 
				for (String f: requiredFilters) {
					if (sb.length() != 0) {
						sb.append(',');
					}
					sb.append(f);
				}
				String filtersString = sb.toString();
				throw createValidationFault(CloudMessageConstant.VALIDATION_FILTERLISTCONDITION.getMessage(getElementIdCode(), filtersString));
			}
		}
	}
	
	public static class ValidateNotifyRelationInfoValidator extends AbstractValidator<ValidateNotifyRelationInfo, NotifyRelationInfo> {
		@Override
		public void init(ValidateNotifyRelationInfo annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(NotifyRelationInfo property, String group) throws PluginException {
			if (property == null) {
				return;
			}

			try {
				CommonValidator.validateNotifyId(CloudMessage.getMessage(property.getNotifyId()), true, null);
			}
			catch (InvalidSetting e) {
				throw createValidationFault(e.getMessage());
			}
			catch (Exception e) {
				throw createValidationFault(e);
			}
		}
	}
	
	public static class NotNullContainerValidator extends AbstractValidator<NotNullContainer, Object> implements ContainerValidator<NotNullContainer, Object> {
		@Override
		public void init(NotNullContainer annotation) {
			setElementId(annotation.elementId());
			setValidationId(annotation.validationId());
			setGroups(annotation.groups());
		}

		@Override
		protected void internalValidate(Object property, String group) throws PluginException {
			if (property == null) {
				throw createValidationFault(CloudMessageConstant.VALIDATION_NOTNULL.getMessage(getElementIdCode()));
			}
		}
	}
	
	private static EntityValidator entityValidator;
	private static MethodValidator methodValidator;

	public static synchronized EntityValidator getEntityValidator() {
		if (entityValidator == null) {
			entityValidator = new EntityValidatorImpl();
		}
		return entityValidator;
	}
	
	public static synchronized MethodValidator getMethodValidator() {
		if (methodValidator == null) {
			methodValidator = new MethodValidatorImpl();
		}
		return methodValidator;
	}
}
