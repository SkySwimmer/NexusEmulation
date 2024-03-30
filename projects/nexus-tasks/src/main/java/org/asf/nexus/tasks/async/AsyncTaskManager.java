package org.asf.nexus.tasks.async;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 
 * Task manager - used to run asynchronous tasks with thread pooling
 * 
 * @author Sky Swimmer
 *
 */
public class AsyncTaskManager {

	static ArrayList<AsyncTaskThreadHandler> threads = new ArrayList<AsyncTaskThreadHandler>();
	private static ArrayList<AsyncTask<?>> queuedActions = new ArrayList<AsyncTask<?>>();

	static AsyncTask<?> obtainNext() {
		synchronized (queuedActions) {
			if (queuedActions.size() == 0)
				return null;
			return queuedActions.remove(0);
		}
	}

	/**
	 * Runs a runnable asynchronously
	 * 
	 * @param action Action to run
	 * @return AsyncTask instance
	 */
	public static AsyncTask<Void> runAsync(Runnable action) {
		return runAsync(AsyncTask.createTask(action));
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
		return runAsync(AsyncTask.createTask(action));
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
	 * Runs a task asynchronously
	 * 
	 * @param <T>  Return type
	 * @param task Task to run
	 * @return AsyncTask instance
	 */
	public static <T> AsyncTask<T> runAsync(AsyncTask<T> task) {
		if (task.hasCompleted())
			return task;
		if (task.slatedForAsyncRun || task.running)
			return task;
		task.slatedForAsyncRun = true;
		synchronized (threads) {
			// Check if a thread is available, if not, start a new one
			AsyncTaskThreadHandler[] ths = threads.toArray(new AsyncTaskThreadHandler[0]);
			if (!Stream.of(ths).anyMatch(new Predicate<AsyncTaskThreadHandler>() {

				@Override
				public boolean test(AsyncTaskThreadHandler t) {
					return t.isAvailable();
				}

			})) {
				// Start new thread
				AsyncTaskThreadHandler handler = new AsyncTaskThreadHandler();
				handler.setName("Async task thread");
				threads.add(handler);
				handler.setDaemon(true);
				handler.start();
			}

			// Add task
			synchronized (queuedActions) {
				queuedActions.add(task);
			}

			// Return
			return task;
		}
	}

}
