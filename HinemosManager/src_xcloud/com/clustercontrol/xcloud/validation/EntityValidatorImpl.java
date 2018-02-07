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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.validation.annotation.CustomEntityValidation;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.ValidatedBy;

public class EntityValidatorImpl implements EntityValidator {
	private static class PropValidatorInfoImpl implements PropValidatorInfo {
		private static enum TypeKind {
			Simple,
			Array,
			List;
		}
		
		private static Method validateMethod;
		private String propName;
		private Method getMethod;
		private String elementId;
		private TypeKind typeKind;
		private Validator<? extends Annotation, ?>[] validators;
		private ContainerValidator<? extends Annotation, ?>[] containerValidators;

		@Override
		public String propName() {
			return propName;
		}

		public void setPropName(String propName) {
			this.propName = propName;
		}
		@Override
		public Method getMethod() {
			return getMethod;
		}

		public void setGetMethod(Method getMethod) {
			this.getMethod = getMethod;
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

		@Override
		public void validate(Object entity, String group) throws PluginException {
			Object property = null;
			try {
				property = getMethod().invoke(entity);
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

			switch (typeKind) {
			case Array:
			case List:
				try {
					for (ContainerValidator<?, ?> cvalidator: containerValidators) {
						getValidateMethod().invoke(cvalidator, new Object[]{property, group});
					}
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
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
						if (property == null) continue;
						for (Object element: (List<?>)property) {
							getValidateMethod().invoke(validator, new Object[]{element, group});
						}
						break;
					case Array:
						if (property == null) continue;
						for (Object element: (Object[])property) {
							getValidateMethod().invoke(validator, new Object[]{element, group});
						}
						break;
					default:
						getValidateMethod().invoke(validator, new Object[]{property, group});
					}
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
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

		@Override
		public String elementId() {
			return elementId;
		}
		public void setElementId(String elementId) {
			this.elementId = elementId;
		}
	}
	
	private static class EntityValidationContextImpl implements EntityValidationContext {
		private Class<?> type;
		private Map<String, PropValidatorInfo> validatorMap = new HashMap<String, PropValidatorInfo>();
		private List<CustomEntityValidator<?>> customValidators = new ArrayList<>();
		
		@Override
		public Class<?> ｔype() {
			return type;
		}
		
		public void setType(Class<?> type) {
			this.type = type;
		}
		@Override
		public List<CustomEntityValidator<?>> getCustomEntityValidators() {
			return customValidators;
		}

		@SuppressWarnings("unused")
		public void setCustomValidators(List<CustomEntityValidator<?>> customValidators) {
			this.customValidators = customValidators;
		}

		@Override
		public void validate(Object entity, String group) throws PluginException {
			for (PropValidatorInfo info: validatorMap.values()) {
				info.validate(entity, group);
			}
		}
		@Override
		public PropValidatorInfo getPropValidator(String propName) {
			return validatorMap.get(propName);
		}
		@Override
		public Map<String, PropValidatorInfo> getPropValidatorMap() {
			return Collections.unmodifiableMap(validatorMap);
		}

		public void putPropValidator(String propName, PropValidatorInfo validator) {
			validatorMap.put(propName, validator);
		}
	}

	private Map<Class<?>, EntityValidationContext> validationContextMap = new HashMap<Class<?>, EntityValidationContext>(); 
	private static Method initMethod;
	private static Method validateMethod;

	@Override
	public void validate(Object entity, String group) throws PluginException {
		EntityValidationContext evc = getEntityContext(entity.getClass());
		
		if (evc != null) {
			evc.validate(entity, group);

			List<CustomEntityValidator<?>> cvs = evc.getCustomEntityValidators();
			for (CustomEntityValidator<?> cv: cvs) {
				try {
					getValidateMethod().invoke(cv, new Object[]{entity, group, evc});
				} catch (IllegalArgumentException e) {
					throw new IllegalStateException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalStateException(e);
				} catch (InvocationTargetException e) {
					if (e.getCause() instanceof CloudManagerException) {
						throw (CloudManagerException)e.getCause();
					}
					if (e.getCause() instanceof IllegalStateException) {
						throw (IllegalStateException)e.getCause();
					}
					throw new IllegalStateException(e.getCause());
				}
			}
		}
	}

	private EntityValidationContext getEntityContext(Class<?> type) {
		EntityValidationContext validationContext = validationContextMap.get(type);
		if (validationContext == null) {
			Logger logger = Logger.getLogger(EntityValidatorImpl.class);
			logger.debug("checking : " + type.getName());

			EntityValidationContextImpl vc = null;
			for (Class<?> currentType = type; currentType != Object.class; currentType = currentType.getSuperclass()) {
				CustomEntityValidation c = currentType.getAnnotation(CustomEntityValidation.class);
				if (c != null) {
					if (vc == null) {
						vc = new EntityValidationContextImpl();
						vc.setType(type);
					}
					
					CustomEntityValidator<?> cv = null;
					try {
						Class<?> cvClazz = c.value();
						cv = (CustomEntityValidator<?>)cvClazz.newInstance();
						vc.getCustomEntityValidators().add(cv);
					}
					catch (Exception e) {
						if (e instanceof IllegalStateException) {
							throw (IllegalStateException)e;
						}
						throw new IllegalStateException(e);
					}
				}
			}
			
			for (Class<?> currentType = type; currentType != Object.class; currentType = currentType.getSuperclass()) {
				for (Method method: currentType.getMethods()) {
					logger.debug("methodName : " + method.getName());
	
					boolean match = false;
					String elementId = null;
					String propName = null;
					Class<?> returnType = null;
	
					Annotation[] annos = method.getAnnotations();
	
					ElementId eid = null;
					List<Validator<?, ?>> list = new ArrayList<Validator<?, ?>>();
					List<ContainerValidator<?, ?>> containerList = new ArrayList<ContainerValidator<?, ?>>();
					for (Annotation anno: annos) {
						logger.debug("annotation : " + anno.annotationType().getName());
						ValidatedBy vb = anno.annotationType().getAnnotation(ValidatedBy.class);
						
						if (vb != null) {
							if (!match) {
								// get 関数か判定。
								// static 定義されているのは、対象外。
								if (Modifier.isStatic(method.getModifiers())) {
									throw new IllegalStateException();
								}
	
								// 共変は、対象外。
								if (method.isSynthetic()) {
									throw new IllegalStateException();
								}
	
								// 引数が 0 個。
								if (method.getParameterTypes().length != 0) {
									throw new IllegalStateException();
								}
	
								// 関数名の先頭は、get がつく。
								if (!method.getName().startsWith("get") && !method.getName().startsWith("is")) {
									throw new IllegalStateException();
								}
	
								// プロパティの型を抽出。
								returnType = method.getReturnType();
								
								// 戻り値は、void でない。
								if (Void.class.isAssignableFrom(returnType) || void.class.isAssignableFrom(returnType)) {
									throw new IllegalStateException();
								}
	
								StringBuilder elementIdBuilder = new StringBuilder();
								elementIdBuilder.append(Character.toLowerCase(type.getSimpleName().charAt(0)) + type.getSimpleName().substring(1));
								elementIdBuilder.append('.');
								propName = method.getName().substring("get".length());
								propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
								elementIdBuilder.append(propName);
								elementId = elementIdBuilder.toString();
	
								match = true;
							}
	
							Class<?> validatorClass = vb.value();
							logger.debug("validator : " + validatorClass.getName());
							try {
								Validator<?, ?> propValidator = (Validator<?, ?>)validatorClass.newInstance();
	
								// validator　を初期化。
								getInitMethod().invoke(propValidator, new Object[]{anno});
	
								if (propValidator.getElementId() == null || propValidator.getElementId().isEmpty()) {
									propValidator.setElementId(elementId);
								}
	
								if (propValidator instanceof ContainerValidator) {
									containerList.add((ContainerValidator<?, ?>)propValidator);
								}
								else {
									list.add(propValidator);
								}
							}
							catch (Exception e) {
								if (e instanceof IllegalStateException) {
									throw (IllegalStateException)e;
								}
								throw new IllegalStateException(e);
							}
						}
						else if (anno.annotationType() == ElementId.class) {
							eid = ElementId.class.cast(anno);
						}
					}
					if (!list.isEmpty() || !containerList.isEmpty()) {
						if (eid != null) {
							for (Validator<?, ?> v: list) {
								v.setElementId(eid.value());
							}
							for (ContainerValidator<?, ?> v: containerList) {
								v.setElementId(eid.value());
							}
						}

						if (vc == null) {
							vc = new EntityValidationContextImpl();
							vc.setType(type);
						}

						// PropValidatorInfo 作成。
						PropValidatorInfoImpl pvi = new PropValidatorInfoImpl();
						pvi.setGetMethod(method);
						pvi.setElementId(eid != null ? eid.value(): elementId);
						pvi.setType(returnType);

						// プロパティ名を抽出。
						pvi.setPropName(propName);
						
						pvi.setValidators(list.toArray(new Validator<?, ?>[0]));
						pvi.setContainerValidators(containerList.toArray(new ContainerValidator<?, ?>[0]));
						
						vc.putPropValidator(propName, pvi);
					}
				}
			}
			
			if (vc != null) {
				validationContextMap.put(type, vc);
			}
			
			validationContext = vc;
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
				validateMethod = CustomEntityValidator.class.getDeclaredMethod("validate", new Class<?>[]{Object.class, String.class, EntityValidationContext.class});
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
