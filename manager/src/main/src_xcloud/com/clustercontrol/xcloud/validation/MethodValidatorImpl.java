/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.validation.annotation.CustomMethodValidation;
import com.clustercontrol.xcloud.validation.annotation.ParamId;
import com.clustercontrol.xcloud.validation.annotation.ValidatedBy;
import com.clustercontrol.xcloud.validation.annotation.ValidationGroup;

public class MethodValidatorImpl implements MethodValidator {
	private static class ParamValidatorInfoImpl implements ParamValidatorInfo {
		private static enum TypeKind {
			Simple,
			Array,
			List;
		};

		private Method validateMethod;
		private Validator<? extends Annotation, ?>[] validators;
		private TypeKind typeKind;
		private String paramId;
		private ContainerValidator<? extends Annotation, ?>[] containerValidators;

		@Override
		public void validate(Object param, String group) throws PluginException {
			switch (typeKind) {
			case Array:
			case List:
				try {
					for (ContainerValidator<?, ?> cvalidator: containerValidators) {
						getValidateMethod().invoke(cvalidator, new Object[]{param, group});
					}
				}
				catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				}
				catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
				catch (InvocationTargetException e) {
					if (e.getCause() instanceof PluginException) {
						throw (PluginException)e.getCause();
					}
					if (e.getCause() instanceof IllegalStateException) {
						throw (IllegalStateException)e.getCause();
					}
					throw new IllegalStateException(e.getCause());
				}
			default:
				break;
			}

			for (Validator<?, ?> validator: validators) {
				try {
					switch (typeKind) {
					case List:
						if (param == null) continue;
						for (Object element: (List<?>)param) {
							getValidateMethod().invoke(validator, new Object[]{element, group});
						}
						break;
					case Array:
						if (param == null) continue;
						for (Object element: (Object[])param) {
							getValidateMethod().invoke(validator, new Object[]{element, group});
						}
						break;
					default:
						getValidateMethod().invoke(validator, new Object[]{param, group});
					}
				}
				catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				}
				catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
				catch (InvocationTargetException e) {
					if (e.getCause() instanceof PluginException) {
						throw (PluginException)e.getCause();
					}
					if (e.getCause() instanceof IllegalStateException) {
						throw (IllegalStateException)e.getCause();
					}
					throw new IllegalStateException(e.getCause());
				}
			}
		}

		public void setValidators(Validator<?, ?>[] validators) {
			this.validators = validators;
		}
		
		public void setContainerValidators(ContainerValidator<?, ?>[] containerValidators) {
			this.containerValidators = containerValidators;
		}
		
		public void setType(Class<?> returnType) {
			if (List.class.isAssignableFrom(returnType)) {
				typeKind = TypeKind.List;
			}
			else if (returnType.isArray()) {
				typeKind = TypeKind.Array;
			}
			else {
				typeKind = TypeKind.Simple;
			}
		}
		
		private Method getValidateMethod() {
			if (validateMethod == null) {
				try {
					validateMethod = Validator.class.getDeclaredMethod("validate", new Class<?>[]{Object.class, String.class});
				}
				catch (Exception e) {
					if (e instanceof IllegalStateException) {
						throw (IllegalStateException)e;
					}
					throw new IllegalStateException(e);
				}
			}
			return validateMethod;
		}

		public void setParamId(String paramId) {
			this.paramId = paramId;
		}

		@Override
		public String getParamId() {
			return paramId;
		}
	}
	
	private static class MethodContextImpl implements MethodValidationContext {
		private Method method;
		private String validationGroup;
		private Map<String, ParamValidatorInfo> validatorInfos = new LinkedHashMap<String, ParamValidatorInfo>();
		private CustomMethodValidator[] customMethodValidators;
		
		@Override
		public Method method() {
			return method;
		}
		
		public void setMethod(Method method) {
			this.method = method;
		}
		@Override
		public CustomMethodValidator[] getCustomMethodValidators() {
			return customMethodValidators;
		}

		public void setMethodCustomValidators(CustomMethodValidator[] customMethodValidators) {
			this.customMethodValidators = customMethodValidators != null ? customMethodValidators: new CustomMethodValidator[0];
		}

		public void addPropValidator(ParamValidatorInfo validator) {
			validatorInfos.put(validator.getParamId(), validator);
		}

		@Override
		public String validationGroup() {
			return validationGroup;
		}
		public void setValidationGroup(String validationGroup) {
			this.validationGroup = validationGroup;
		}

		@Override
		public ParamValidatorInfo getPramValidator(String paramId) {
			return validatorInfos.get(paramId);
		}

		@Override
		public ParamValidatorInfo[] getParamValidatorInfos() {
			return validatorInfos.values().toArray(new ParamValidatorInfo[validatorInfos.size()]);
		}
	}

	private Map<Method, MethodValidationContext> validationContextMap = new HashMap<Method, MethodValidationContext>(); 
	private static Method initMethod;
	private static Method validateMethod;

	@Override
	public void validate(Method method, Object[] params) throws PluginException {
		MethodValidationContext validationContext = getMethodContext(method);
		
		if (validationContext != null) {
			ParamValidatorInfo pvis[] = validationContext.getParamValidatorInfos();
			if (params.length != pvis.length) {
				throw new IllegalStateException();
			}

			for (int i = 0; i < pvis.length; ++i) {
				pvis[i].validate(params[i], validationContext.validationGroup());
			}

			CustomMethodValidator[] cvs = validationContext.getCustomMethodValidators();
			if (cvs != null) {
				try {
					ParamHolder.Builder builder = new ParamHolder.Builder();
					for (int i = 0; i < pvis.length; ++i) {
						builder.addParam(pvis[i].getParamId() != null ? pvis[i].getParamId(): Integer.toString(i), params[i]);
					}
					ParamHolder ph = builder.build();
					for (CustomMethodValidator cv: cvs) {
						getValidateMethod().invoke(cv, new Object[]{method, ph, validationContext.validationGroup(), validationContext});
					}
				}
				catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				}
				catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
				catch (InvocationTargetException e) {
					if (e.getCause() instanceof PluginException) {
						throw (PluginException)e.getCause();
					}
					if (e.getCause() instanceof IllegalStateException) {
						throw (IllegalStateException)e.getCause();
					}
					throw new IllegalStateException(e.getCause());
				}
			}
		}
	}
	@Override
	public MethodValidationContext getMethodContext(Method method) {
		MethodValidationContext validationContext = validationContextMap.get(method);
		if (validationContext == null) {
			Logger logger = Logger.getLogger(MethodValidatorImpl.class);
			logger.debug("checking : " + method.getName());

			MethodContextImpl mc = null;
			
			CustomMethodValidation c = method.getAnnotation(CustomMethodValidation.class);
			if (c != null) {
				mc = new MethodContextImpl();
				mc.setMethod(method);
				
				List<CustomMethodValidator> cmList = new ArrayList<>();
				try {
					Class<? extends CustomMethodValidator>[] cmClazzes = c.value();
					for (Class<? extends CustomMethodValidator> cmClazz: cmClazzes) {
						cmList.add((CustomMethodValidator)cmClazz.newInstance());
					}
				}
				catch (Exception e) {
					if (e instanceof IllegalStateException) {
						throw (IllegalStateException)e;
					}
					throw new IllegalStateException(e);
				}
				mc.setMethodCustomValidators(cmList.toArray(new CustomMethodValidator[cmList.size()]));
			}
			
			if (method.getParameterAnnotations().length > 0 && mc == null) {
				mc = new MethodContextImpl();
				mc.setMethod(method);
			}

			if (mc != null) {
				ValidationGroup vga = method.getAnnotation(ValidationGroup.class);
				if (vga != null) { 
					mc.setValidationGroup(vga.value());
				}
			}

			{
				// パラメータ名はコンパイル後に情報として残らないため、文字列変換のための ID をパラメータの型を使用して作成。
				StringBuilder elementIdBuilder = new StringBuilder();
				elementIdBuilder.append(Character.toLowerCase(method.getDeclaringClass().getSimpleName().charAt(0)) + method.getDeclaringClass().getSimpleName().substring(1));
				elementIdBuilder.append('.');
				elementIdBuilder.append(Character.toLowerCase(method.getName().charAt(0)) + method.getName().substring(1));
				for (Class<?> clazz: method.getParameterTypes()) {
					elementIdBuilder.append('_');
					elementIdBuilder.append(Character.toLowerCase(clazz.getSimpleName().charAt(0)) + clazz.getSimpleName().substring(1));
				}
				String elementId = elementIdBuilder.toString();
				
				Annotation[][] annotationss = method.getParameterAnnotations();
				for (int i = 0; i < annotationss.length; ++i) {
					ParamId pid = null;
					List<Validator<?, ?>> list = new ArrayList<Validator<?, ?>>();
					List<ContainerValidator<?, ?>> containerList = new ArrayList<ContainerValidator<?, ?>>();
					for (Annotation anno: annotationss[i]) {
						logger.debug("annotation : " + anno.annotationType().getName());
						ValidatedBy vb = anno.annotationType().getAnnotation(ValidatedBy.class);
						
						if (vb != null) {
							Class<?> validatorClass = vb.value();
							logger.debug("validator : " + validatorClass.getName());
							try {
								Validator<?, ?> paramValidator = (Validator<?, ?>)validatorClass.newInstance();

								// validator　を初期化。
								getInitMethod().invoke(paramValidator, new Object[]{anno});
								
								if (paramValidator.getElementId() == null || paramValidator.getElementId().equals("")) {
									// ID に、関数に指定される序列番号を追加して一意にする。
									paramValidator.setElementId(elementId + "." + i);
								}

								if (paramValidator instanceof ContainerValidator) {
									containerList.add((ContainerValidator<?, ?>)paramValidator);
								}
								else {
									list.add(paramValidator);
								}
							}
							catch (Exception e) {
								if (e instanceof IllegalStateException) {
									throw (IllegalStateException)e;
								}
								throw new IllegalStateException(e);
							}
						}
						else if (anno.annotationType() == ParamId.class) {
							pid = ParamId.class.cast(anno);
						}
					}

					if (pid != null) {
						for (Validator<?, ?> v: list) {
							v.setElementId(pid.value());
						}
						for (ContainerValidator<?, ?> v: containerList) {
							v.setElementId(pid.value());
						}
					}
					
					// PropValidatorInfo 作成。
					ParamValidatorInfoImpl pvm = new ParamValidatorInfoImpl();

					pvm.setValidators(list.toArray(new Validator<?, ?>[0]));
					pvm.setContainerValidators(containerList.toArray(new ContainerValidator<?, ?>[0]));
					pvm.setType(method.getParameterTypes()[i]);
					pvm.setParamId(pid != null ? pid.value(): Integer.toString(i));
					
					mc.addPropValidator(pvm);
				}			
			}
			
			if (mc != null) {
				validationContextMap.put(method, mc);
			}
			
			validationContext = mc;
		}
		return validationContext;
	}
	
	private Method getInitMethod() {
		if (initMethod == null) {
			try {
				initMethod = Validator.class.getDeclaredMethod("init", new Class<?>[]{Annotation.class});
			}
			catch (Exception e) {
				if (e instanceof IllegalStateException) {
					throw (IllegalStateException)e;
				}
				throw new IllegalStateException(e);
			}
		}
		return initMethod;
	}

	private Method getValidateMethod() {
		if (validateMethod == null) {
			try {
				validateMethod = CustomMethodValidator.class.getDeclaredMethod("validate", new Class<?>[]{Method.class, ParamHolder.class, String.class, MethodValidationContext.class});
			}
			catch (Exception e) {
				if (e instanceof IllegalStateException) {
					throw (IllegalStateException)e;
				}
				throw new IllegalStateException(e);
			}
		}
		return validateMethod;
	}
}
