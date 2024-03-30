package org.asf.nexus.tasks.async;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 
 * Async task object - used to create asynchronous methods and tasks
 * 
 * @author Sky Swimmer
 *
 */
public class AsyncTask<T> {
	private Supplier<T> action;
	private Runnable actionR;
	private boolean run;
	private T result;
	boolean running;
	boolean slatedForAsyncRun;

	private AsyncTask() {
	}

	/**
	 * Runs tasks synchronously
	 * 
	 * @param <T>  Return type
	 * @param task Task to run
	 * @return Task result
	 */
	public static <T> T runSynced(AsyncTask<T> task) {
		return task.getResult();
	}

	/**
	 * Awaits tasks
	 * 
	 * @param <T>  Return type
	 * @param task Task to await
	 * @return Task result
	 */
	public static <T> T await(AsyncTask<T> task) {
		// Check current thread
		if (Thread.currentThread() instanceof AsyncTaskThreadHandler)
			return runSynced(task);

		// Run async
		return runAsync(task).getResult();
	}

	/**
	 * Runs a runnable asynchronously
	 * 
	 * @param action Action to run
	 * @return AsyncTask instance
	 */
	public static AsyncTask<Void> runAsync(Runnable action) {
		return AsyncTaskManager.runAsync(action);
	}

	/**
	 * Runs a runnable asynchronously
	 * 
	 * @param action   Action to run
	 * @param callback Method to run on completion
	 * @return AsyncTask instance
	 */
	public static AsyncTask<Void> runAsync(Runnable action, Runnable callback) {
		return runAsync(AsyncTask.createTask(() -> {
			action.run();
			callback.run();
		}));
	}

	/**
	 * Runs a supplier asynchronously
	 * 
	 * @param <T>    Return type
	 * @param action Action to run
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> runAsync(Supplier<T> action) {
		return AsyncTaskManager.runAsync(action);
	}

	/**
	 * Runs a task asynchronously
	 * 
	 * @param <T>  Return type
	 * @param task Task to run
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> runAsync(AsyncTask<T> task) {
		return AsyncTaskManager.runAsync(task);
	}

	/**
	 * Runs a task asynchronously
	 * 
	 * @param task     Task to run
	 * @param callback Callback to run when the task finishes
	 * @return AsyncTask instance
	 */
	public static AsyncTask<Void> runAsync(AsyncTask<Void> task, Runnable callback) {
		return runAsync(() -> {
			task.getResult();
			callback.run();
		});
	}

	/**
	 * Runs a task asynchronously
	 * 
	 * @param <T>      Return type
	 * @param task     Task to run
	 * @param callback Callback to run when the task finishes
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> runAsync(AsyncTask<T> task, Consumer<T> callback) {
		return runAsync(() -> {
			T res = task.getResult();
			callback.accept(res);
			return res;
		});
	}

	/**
	 * Runs a supplier asynchronously
	 * 
	 * @param <T>      Return type
	 * @param action   Action to run
	 * @param callback Method to run on completion
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> runAsync(Supplier<T> action, Consumer<T> callback) {
		return runAsync(AsyncTask.createTask(() -> {
			T res = action.get();
			callback.accept(res);
			return res;
		}));
	}

	/**
	 * Creates an async task object
	 * 
	 * @param <T>    Return type
	 * @param action Action to run
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> createTask(Supplier<T> action) {
		AsyncTask<T> task = new AsyncTask<T>();
		task.action = action;
		return task;
	}

	/**
	 * Creates an async task object
	 * 
	 * @param action Action to run
	 * @return AsyncTask instance
	 */
	public static AsyncTask<Void> createTask(Runnable action) {
		AsyncTask<Void> task = new AsyncTask<Void>();
		task.actionR = action;
		return task;
	}

	void run() {
		try {
			running = true;
			if (action != null)
				result = action.get();
			else
				actionR.run();
		} finally {
			run = true;
		}
	}

	/**
	 * Runs the the task synchronously
	 */
	public void execute() {
		if (slatedForAsyncRun || run || running)
			return;
		run();
	}

	/**
	 * Checks if the task has been started
	 * 
	 * @return True if started, false otherwise
	 */
	public boolean hasStarted() {
		return running;
	}

	/**
	 * Retrieves the result of the task (blocks until completion)
	 * 
	 * @return Result value
	 */
	public T getResult() {
		block();
		return result;
	}

	/**
	 * Checks if the task has completed
	 * 
	 * @return True if completed, false otherwise
	 */
	public boolean hasCompleted() {
		return run;
	}

	/**
	 * Blocks until the task finishes
	 */
	public void block() {
		if (!running && !slatedForAsyncRun)
			execute();
		if (run)
			return;
		while (!run)
			;
	}

}
