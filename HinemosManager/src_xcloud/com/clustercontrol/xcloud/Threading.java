/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.xcloud.Session.ContextBean;
import com.clustercontrol.xcloud.Session.SessionScope;

public class Threading {
	private static class DelegatedExecutorService extends AbstractExecutorService {
		private final ExecutorService e;
		DelegatedExecutorService(ExecutorService executor) { e = executor; }
		public void execute(Runnable command) { e.execute(command); }
		public void shutdown() { e.shutdown(); }
		public List<Runnable> shutdownNow() { return e.shutdownNow(); }
		public boolean isShutdown() { return e.isShutdown(); }
		public boolean isTerminated() { return e.isTerminated(); }
		public boolean awaitTermination(long timeout, TimeUnit unit)
				throws InterruptedException {
			return e.awaitTermination(timeout, unit);
		}
		public Future<?> submit(Runnable task) {
			return e.submit(task);
		}
		public <T> Future<T> submit(Callable<T> task) {
			return e.submit(task);
		}
		public <T> Future<T> submit(Runnable task, T result) {
			return e.submit(task, result);
		}
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
				throws InterruptedException {
			return e.invokeAll(tasks);
		}
		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit)
						throws InterruptedException {
			return e.invokeAll(tasks, timeout, unit);
		}
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
				throws InterruptedException, ExecutionException {
			return e.invokeAny(tasks);
		}
		public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
				long timeout, TimeUnit unit)
						throws InterruptedException, ExecutionException, TimeoutException {
			return e.invokeAny(tasks, timeout, unit);
		}
	}

	private static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService {
		private final ScheduledExecutorService e;
		DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
			super(executor);
			e = executor;
		}
		public ScheduledFuture<?> schedule(Runnable command, long delay,  TimeUnit unit) {
			return e.schedule(command, delay, unit);
		}
		public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
			return e.schedule(callable, delay, unit);
		}
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,  long period, TimeUnit unit) {
			return e.scheduleAtFixedRate(command, initialDelay, period, unit);
		}
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,  long delay, TimeUnit unit) {
			return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
		}
	}

	private static class FinalizableDelegatedExecutorService extends DelegatedExecutorService {
		FinalizableDelegatedExecutorService(ExecutorService executor) {
			super(executor);
		}
		protected void finalize() {
			super.shutdown();
		}
	}

	private static class FinalizableDelegatedScheduledExecutorService extends DelegatedScheduledExecutorService {
		FinalizableDelegatedScheduledExecutorService(ScheduledExecutorService executor) {
			super(executor);
		}
		protected void finalize() {
			super.shutdown();
		}
	}

	private static class DelegateRunnableFuture<V> implements RunnableFuture<V> {
		// タスクを依頼したスレッドのセキュリティ情報を保存。
		private ContextBean context = Session.isExist() ? Session.current().getContext(): null;
		private RunnableFuture<V> task;

		public DelegateRunnableFuture(RunnableFuture<V> task) {
			this.task = task;
		}

		public void run() {
			if (context != null) {
				// 新規に立ち上げたスレッドで保存したセキュリティ情報を適用。
				try (SessionScope sessionScope = SessionScope.open(context)) {
					task.run();
				}
			} else {
				task.run();
			}
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return task.cancel(mayInterruptIfRunning);
		}

		public boolean isCancelled() {
			return task.isCancelled();
		}

		public boolean isDone() {
			return task.isDone();
		}

		public V get() throws InterruptedException, ExecutionException {
			return task.get();
		}

		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return task.get(timeout, unit);
		}

		@Override
		public int hashCode() {
			return task.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return task.equals(obj);
		}

		@Override
		public String toString() {
			return task.toString();
		}
	}

	private static class ThreadPoolExecutorEx extends ThreadPoolExecutor {
		@Override
		protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
			return new DelegateRunnableFuture<T>(super.newTaskFor(runnable, value));
		}

		@Override
		protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
			return new DelegateRunnableFuture<T>(super.newTaskFor(callable));
		}

		public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
		}

		public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
		}

		public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
		}

		public ThreadPoolExecutorEx(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			Session.poll();
		}

		@Override
		public void execute(final Runnable command) {
			super.execute(new Runnable() {
				private ContextBean context = Session.isExist() ? Session.current().getContext(): null;
				@Override
				public void run() {
					if (context != null) {
						// 新規に立ち上げたスレッドで保存したセキュリティ情報を適用。
						try (SessionScope sessionScope = SessionScope.open(context)) {
							command.run();
						}
					} else {
						command.run();
					}
				}
			});
		}
	}

	private static class DelegateRunnableScheduledFuture<V> extends DelegateRunnableFuture<V> implements RunnableScheduledFuture<V> {
		private RunnableScheduledFuture<V> task;

		public DelegateRunnableScheduledFuture(RunnableScheduledFuture<V> task) {
			super(task);
			this.task = task;
		}

		public long getDelay(TimeUnit unit) {
			return task.getDelay(unit);
		}

		public boolean isPeriodic() {
			return task.isPeriodic();
		}

		public int compareTo(Delayed o) {
			return task.compareTo(o);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((task == null) ? 0 : task.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			@SuppressWarnings("unchecked")
			DelegateRunnableScheduledFuture<V> other = (DelegateRunnableScheduledFuture<V>) obj;
			if (task == null) {
				if (other.task != null)
					return false;
			} else if (!task.equals(other.task))
				return false;
			return true;
		}
	}

	private static class ScheduledExecutorServiceEx extends ScheduledThreadPoolExecutor {
		public ScheduledExecutorServiceEx(int corePoolSize,
				RejectedExecutionHandler handler) {
			super(corePoolSize, handler);
		}

		public ScheduledExecutorServiceEx(int corePoolSize,
				ThreadFactory threadFactory, RejectedExecutionHandler handler) {
			super(corePoolSize, threadFactory, handler);
		}

		public ScheduledExecutorServiceEx(int corePoolSize,
				ThreadFactory threadFactory) {
			super(corePoolSize, threadFactory);
		}

		public ScheduledExecutorServiceEx(int corePoolSize) {
			super(corePoolSize);
		}

		@Override
		protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
			return new DelegateRunnableScheduledFuture<V>(super.decorateTask(callable, task));
		}

		@Override
		protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
			return new DelegateRunnableScheduledFuture<V>(super.decorateTask(runnable, task));
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			Session.poll();
		}
	}

	public static ExecutorService newFixedThreadPool(int nThreads, final String poolName) {
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutorEx(nThreads, nThreads, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, poolName + "-thread-" + threadNumber.getAndIncrement());
			}
		}));
	}

	public static ExecutorService newCachedThreadPool(final String poolName) {
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutorEx(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, poolName + "-thread-" + threadNumber.getAndIncrement());
			}
		}));
	}

	public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, final String poolName) {
		return  new FinalizableDelegatedScheduledExecutorService(new ScheduledExecutorServiceEx(corePoolSize, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, poolName + "-thread-" + threadNumber.getAndIncrement());
			}
		}));
	}

	public static ExecutorService newSingleThreadExecutor(final String poolName) {
		// FinalizableDelegatedExecutorService でラップしないと、finalizer が呼ばれない。
		// 理由は、worker スレッドが、スレッドプールオブジェクト本体の参照を持っているため、ワーカーが行き続けている間は、スレッドプールオブジェクトの参照が切れない。
		// スレッドプールなのでワーカーはプールされるので、本体の参照が永遠に存在し続ける。
		// FinalizableDelegatedExecutorService は、その参照関係とは関係ないので、最終参照者がはずれるタイミングで、gc 対象となる。
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutorEx(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, poolName + "-thread-" + threadNumber.getAndIncrement());
			}
		}));
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor(final String poolName) {
		return new FinalizableDelegatedScheduledExecutorService(new ScheduledExecutorServiceEx(1, new ThreadFactory() {
			private final AtomicInteger threadNumber = new AtomicInteger(1);
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, poolName + "-thread-" + threadNumber.getAndIncrement());
			}
		}));
	}
	
	private static ExecutorService executorService = newCachedThreadPool("SessionContextPool");
	private static ScheduledExecutorService scheduledExecutorService = newScheduledThreadPool(3, "SessionContextScheduler");

	/** エージェント登録時用スレッドプール */
	private static ScheduledExecutorService scheduledExecutorServiceForRegistAgent = newScheduledThreadPool(
			HinemosPropertyCommon.xcloud_autoregist_agent_threadpool_size.getIntegerValue(), "RegistAgentScheduler");

	public static void execute(Runnable command) {
		executorService.execute(command);
	}

	public static <T> Future<T> submit(Callable<T> task) {
		return executorService.submit(task);
	}

	public static <T> Future<T> submit(Runnable task, T result) {
		return executorService.submit(task, result);
	}

	public static Future<?> submit(Runnable task) {
		return executorService.submit(task);
	}

	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/**
	 * エージェント登録時の定期的なアクションを作成して実行します。
	 * 
	 * @param command 実行するタスク
	 * @param initialDelay 最初の遅延実行までの時間
	 * @param delay 実行の終了後から次の開始までの遅延
	 * @param unit initialDelayおよびdelayパラメータの時間単位
	 * @return タスクの保留状態の完了を表すScheduledFuture
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelayForRegistAgent(Runnable command, long initialDelay,
			long delay, TimeUnit unit) {
		return scheduledExecutorServiceForRegistAgent.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

}
