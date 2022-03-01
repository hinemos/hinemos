/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.Session.PostCommitAction;
import com.clustercontrol.xcloud.Threading;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionExecutor;
import com.clustercontrol.xcloud.model.CloudTaskEntity;
import com.clustercontrol.xcloud.persistence.PersistenceUtil;
import com.clustercontrol.xcloud.persistence.PersistenceUtil.TransactionScope;
import com.clustercontrol.xcloud.persistence.TransactionException;
import com.clustercontrol.xcloud.persistence.Transactional;
import com.clustercontrol.xcloud.persistence.Transactional.TransactionOption;

public class CloudTaskService {
	protected static class CloudTaskStore implements ICloudTaskStore {
		private String key;
		
		public CloudTaskStore(String key) {
			this.key = key;
		}
		
		@Override
		public String getData() {
			HinemosEntityManager em = Session.current().getEntityManager();
			CloudTaskEntity taskEntity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
			if (taskEntity == null)
				throw new InternalManagerError();
			return taskEntity.getSavedData();
		}

		@Override
		public void save(String data) {
			save(data, Transactional.TransactionOption.Required);
		}

		@Override
		public void save(String data, TransactionOption transactionType) {
			try (TransactionScope scope = new TransactionScope(transactionType)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				CloudTaskEntity taskEntity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
				if (taskEntity == null)
					throw new InternalManagerError();
				
				taskEntity.setSavedData(data);
				scope.complete();
			}
		}
	}
	
	public static class OptionDependTask implements ICloudTask {
		private CloudScopeEntity cloudScope;
		private ICloudTask cloudTask;
		
		public OptionDependTask(CloudScopeEntity cloudScope, final String className) throws Exception {
			this.cloudScope = cloudScope;
			this.cloudTask = cloudScope.optionCall(new OptionCallable<ICloudTask>() {
				@Override
				public ICloudTask call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends ICloudTask> taskClazz = (Class<? extends ICloudTask>)option.getClass().getClassLoader().loadClass(className);
						return taskClazz.newInstance();
					} catch(Exception e) {
						throw new InternalManagerError(e);
					}
				}
			});
		}
		
		@Override
		public TaskResult execute(final String key, final String cloudScopeId, final ICloudTaskStore store) throws Exception {
			return cloudScope.optionCall(new OptionCallable<TaskResult>() {
				@Override
				public TaskResult call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					try {
						return cloudTask.execute(key, cloudScopeId, store);
					} catch(Exception e) {
						throw new InternalManagerError(e);
					}
				}
			});
		}
		
		@Override
		public void roleback() {
			try {
				cloudScope.optionExecute(new OptionExecutor() {
					@Override
					public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
						cloudTask.roleback();
					}
				});
			} catch(Exception e) {
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
		}

		@Override
		public void register(final String key, final String cloudScopeId, final ICloudTaskStore store) throws Exception {
			cloudScope.optionExecute(new OptionExecutor() {
				@Override
				public void execute(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
					try {
						cloudTask.register(key, cloudScopeId, store);
					} catch(Exception e) {
						throw new InternalManagerError(e);
					}
				}
			});
		}
	}
	
	protected class Worker implements Runnable {
		protected String key;
		
		public Worker(String key) {
			this.key = key;
		}
		
		@Override
		public void run() {
			HinemosEntityManager em = Session.current().getEntityManager();
			
			CloudTaskEntity taskEntity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
			if (taskEntity == null) {
				cancelWorker(key);
				return;
			}
			
			if (taskEntity.getTaskStatus() != CloudTaskStatus.running)
				updateStatus(CloudTaskStatus.running);
			
			ICloudTask cloudTask = null;
			try {
				cloudTask = createTask(taskEntity);
			} catch(Exception e) {
				cancelWorker(key);
				updateStatus(CloudTaskStatus.failed);
				if (taskEntity.getAutoDelete()) {
					removeTask();
				}
				Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
			}
			
			if (cloudTask != null) {
				try {
					ICloudTask.TaskResult result = cloudTask.execute(key, taskEntity.getCloudScopeId(), new CloudTaskStore(key));
					
					if (result.isFinished) {
						cancelWorker(key);
						updateStatus(CloudTaskStatus.finished);
						if (taskEntity.getAutoDelete())
							removeTask();
					} else {
						updateStatus(CloudTaskStatus.waiting);
					}
				} catch(Exception e) {
					try {
						cloudTask.roleback();
					} catch(Exception e1) {
						Logger.getLogger(this.getClass()).warn(e1.getMessage(), e1);
					}
					
					cancelWorker(key);
					updateStatus(CloudTaskStatus.failed);
					if (taskEntity.getAutoDelete()) {
						removeTask();
					}
					Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
				}
			}
		}
		
		protected void updateStatus(CloudTaskStatus status) {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				CloudTaskEntity taskEntity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
				if (taskEntity == null)
					throw new InternalManagerError();
				
				taskEntity.setTaskStatus(status);
				scope.complete();
			}
		}

		protected void removeTask() {
			try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.RequiredNew)) {
				HinemosEntityManager em = Session.current().getEntityManager();
				CloudTaskEntity taskEntity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
				if (taskEntity == null)
					throw new InternalManagerError();
				
				em.remove(taskEntity);
				scope.complete();
			}
		}
	}
	
	protected static class TaskEntry {
		private String key;
		private Future<?> future;
		private Worker worker;
		
		public String getKey() {
			return key;
		}
		
		public Future<?> getFuture() {
			return future;
		}
		
		public Worker getWorker() {
			return worker;
		}
	}
	
	protected Map<String, TaskEntry> workerMap = new HashMap<String, TaskEntry>();
	
	public void runTask(
			final String id,
			String cloudScopeId,
			String className,
			String initialData,
			boolean autoDelete,
			boolean optionDependent) {
		try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.Required)) {
			CloudTaskEntity entity = new CloudTaskEntity();
			String key = UUID.nameUUIDFromBytes(id.getBytes()).toString();
			entity.setKey(key);
			entity.setCloudScopeId(cloudScopeId);
			entity.setClassName(className);
			entity.setSavedData(initialData);
			entity.setAutoDelete(autoDelete);
			entity.setOptionDependent(optionDependent);
			entity.setTaskStatus(CloudTaskStatus.pending);
			PersistenceUtil.persist(Session.current().getEntityManager(), entity);
			
			Session.current().addPostCommitAction(new PostCommitAction() {
				@Override
				public void postCommit() throws TransactionException {
					Threading.execute(new Runnable() {
						@Override
						public void run() {
							HinemosEntityManager em = Session.current().getEntityManager();
							CloudTaskEntity entity = em.find(CloudTaskEntity.class, key, ObjectPrivilegeMode.READ);
							
							try {
								ICloudTask task = createTask(entity);
								task.register(key, entity.getCloudScopeId(), new CloudTaskStore(key));
							} catch(Exception e) {
								throw new InternalManagerError(e);
							}

							Worker worker = new Worker(key);
							Future<?> future = Threading.scheduleWithFixedDelay(worker, 0, 10, TimeUnit.SECONDS);
							putWorker(key, worker, future);
						}
					});
				}
			});
			
			scope.complete();
		}
	}
	
	protected void putWorker(String key, Worker worker, Future<?> future) {
		synchronized(workerMap) {
			TaskEntry entry = new TaskEntry();
			entry.key = key;
			entry.worker = worker;
			entry.future = future;
			workerMap.put(key, entry);
		}
	}
	
	protected void cancelWorker(String key) {
		synchronized(workerMap) {
			TaskEntry entry = workerMap.get(key);
			entry.future.cancel(false);
		}
	}
	
	public synchronized void startTask(String key) {
	}
	
	public synchronized void stopTask(String key) {
	}
	
	public void removeTask(String key) {
	}
	
	private static volatile CloudTaskService singleton;
	
	public static CloudTaskService singleton() {
		if (singleton == null) {
			synchronized(CloudTaskService.class) {
				if (singleton == null) {
					singleton = new CloudTaskService();
				}
			}
		}
		return singleton;
	}
	
	protected static ICloudTask createTask(CloudTaskEntity taskEntity) throws Exception {
		ICloudTask cloudTask;
		if (taskEntity.getOptionDependent()) {
			CloudScopeEntity cloudScopeEntity = CloudManager.singleton().getCloudScopes().getCloudScope(taskEntity.getCloudScopeId());
			cloudTask = new OptionDependTask(cloudScopeEntity, taskEntity.getClassName());
		} else {
			@SuppressWarnings("unchecked")
			Class<? extends ICloudTask> taskClazz = (Class<? extends ICloudTask>)CloudTaskService.class.getClassLoader().loadClass(taskEntity.getClassName());
			cloudTask = taskClazz.newInstance();
		}
		return cloudTask;
	}

	public synchronized void startService() {
		try (TransactionScope scope = new TransactionScope(Transactional.TransactionOption.Required)) {
			List<CloudTaskEntity> entities = PersistenceUtil.findAll(Session.current().getEntityManager(), CloudTaskEntity.class);
			for (CloudTaskEntity entity: entities) {
				entity.setTaskStatus(CloudTaskStatus.recovering);
				
				final String key = entity.getKey();
				Session.current().addPostCommitAction(new PostCommitAction() {
					@Override
					public void postCommit() throws TransactionException {
						Worker worker = new Worker(key);
						Future<?> future = Threading.scheduleWithFixedDelay(worker, 0, 10, TimeUnit.SECONDS);
						putWorker(key, worker, future);
					}
				});
			}
			scope.complete();
		}
	}

	public synchronized void stopService() {
	}
}
