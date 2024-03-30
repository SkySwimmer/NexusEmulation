package org.asf.nexus.tasks.scheduling;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.asf.nexus.tasks.async.AsyncTask;

/**
 * 
 * Task scheduling system
 * 
 * @author Sky Swimmer
 * 
 */
public class TaskScheduler {

	private ArrayList<ScheduledTask> tasks = new ArrayList<ScheduledTask>();
	private Logger logger = LogManager.getLogger("TaskScheduler");

	/**
	 * Ticks the task scheduler, required for tasks to be executed, should be run
	 * from either a loop, a server tick event, or a game engine tick/update call
	 */
	public void tick() {
		ArrayList<ScheduledTask> tasksL;
		synchronized (tasks) {
			tasksL = new ArrayList<ScheduledTask>(tasks);
		}

		// Run tasks
		for (ScheduledTask task : tasksL) {
			if ((task.timeWait != -1 && System.currentTimeMillis() - task.timeStart < task.timeWait)
					|| (task.interval != -1 && task.cInterval++ < task.interval))
				continue;

			// Run the action
			if (!task.async) {
				try {
					if (System.getProperty("debugMode") == null) {
						try {
							task.action.run();
						} catch (Exception e) {
							logger.error("An error occurred while running a scheduled task", e);
						}
					} else
						task.action.run();
				} finally {
					task.ran = true;

					// Reset
					task.cInterval = 0;
					task.timeStart = System.currentTimeMillis();

					// Increase count
					if (task.limit != -1)
						task.cCount++;

					// Remove if needed
					if (task.limit != -1 && task.cCount >= task.limit) {
						synchronized (tasks) {
							tasks.remove(task);
						}
					}
				}
			} else {
				AsyncTask.runAsync(() -> {
					try {
						task.action.run();
					} finally {
						task.ran = true;
					}
				});

				// Reset
				task.cInterval = 0;
				task.timeStart = System.currentTimeMillis();

				// Increase count
				if (task.limit != -1)
					task.cCount++;

				// Remove if needed
				if (task.limit != -1 && task.cCount >= task.limit) {
					synchronized (tasks) {
						tasks.remove(task);
					}
				}
			}
		}
	}

	/**
	 * Cancels scheduled tasks
	 * 
	 * @param task Task to cancel
	 */
	public void cancel(ScheduledTask task) {
		synchronized (tasks) {
			tasks.remove(task);
		}
	}

	/**
	 * Schedules an action that will run after the given amount of seconds have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param time   Amount of seconds to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask afterSecs(Runnable action, long time) {
		return afterMs(action, time * 1000);
	}

	/**
	 * Schedules an action that will run after the given amount of seconds have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param time   Amount of seconds to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask afterSecsAsync(Runnable action, long time) {
		return afterMsAsync(action, time * 1000);
	}

	/**
	 * Schedules an action that will run after the given amount of milliseconds have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param time   Amount of seconds to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask afterMs(Runnable action, long time) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeStart = System.currentTimeMillis();
		t.timeWait = time;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that will run after the given amount of milliseconds have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param time   Amount of seconds to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask afterMsAsync(Runnable action, long time) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeStart = System.currentTimeMillis();
		t.timeWait = time;
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs only once
	 * 
	 * @param action Action to schedule
	 * @return ScheduledTask instance
	 */
	public ScheduledTask oneshot(Runnable action) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs only once
	 * 
	 * @param action Action to schedule
	 * @return ScheduledTask instance
	 */
	public ScheduledTask oneshotAsync(Runnable action) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs only once after a specific amount of ticks have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param delay  Amount of ticks to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask delayed(Runnable action, int delay) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = delay;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs only once after a specific amount of ticks have
	 * passed
	 * 
	 * @param action Action to schedule
	 * @param delay  Amount of ticks to wait before running the task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask delayedAsync(Runnable action, int delay) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = delay;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a interval
	 * 
	 * @param action   Action to schedule
	 * @param interval Ticks to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask interval(Runnable action, int interval) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = interval;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a interval
	 * 
	 * @param action   Action to schedule
	 * @param interval Ticks to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalAsync(Runnable action, int interval) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = interval;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a second-based interval
	 * 
	 * @param action Action to schedule
	 * @param secs   Seconds to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalSecs(Runnable action, long secs) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = secs * 1000;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a second-based interval
	 * 
	 * @param action Action to schedule
	 * @param secs   Seconds to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalSecsAsync(Runnable action, long secs) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = secs * 1000;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a millisecond-based interval
	 * 
	 * @param action Action to schedule
	 * @param millis Milliseconds to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalMs(Runnable action, long millis) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = millis;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a millisecond-based interval
	 * 
	 * @param action Action to schedule
	 * @param millis Milliseconds to wait each time before running the action
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalMsAsync(Runnable action, long millis) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = millis;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a interval (only a specific amount of times)
	 * 
	 * @param action   Action to schedule
	 * @param interval Ticks to wait each time before running the action
	 * @param limit    The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask interval(Runnable action, int interval, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = interval;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a interval (only a specific amount of times)
	 * 
	 * @param action   Action to schedule
	 * @param interval Ticks to wait each time before running the action
	 * @param limit    The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalAsync(Runnable action, int interval, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.interval = interval;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a second-based interval (only a specific
	 * amount of times)
	 * 
	 * @param action Action to schedule
	 * @param secs   Seconds to wait each time before running the action
	 * @param limit  The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalSecs(Runnable action, long secs, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = secs * 1000;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a second-based interval (only a specific
	 * amount of times)
	 * 
	 * @param action Action to schedule
	 * @param secs   Seconds to wait each time before running the action
	 * @param limit  The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalSecsAsync(Runnable action, long secs, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = secs * 1000;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a millisecond-based interval (only a
	 * specific amount of times)
	 * 
	 * @param action Action to schedule
	 * @param millis Milliseconds to wait each time before running the action
	 * @param limit  The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalMs(Runnable action, long millis, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = millis;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on a millisecond-based interval (only a
	 * specific amount of times)
	 * 
	 * @param action Action to schedule
	 * @param millis Milliseconds to wait each time before running the action
	 * @param limit  The amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask intervalMsAsync(Runnable action, long millis, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.timeWait = millis;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on every tick
	 * 
	 * @param action Action to schedule
	 * @return ScheduledTask instance
	 */
	public ScheduledTask repeat(Runnable action) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on every tick
	 * 
	 * @param action Action to schedule
	 * @return ScheduledTask instance
	 */
	public ScheduledTask repeatAsync(Runnable action) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.limit = -1;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on every tick until its limit is reached
	 * 
	 * @param action Action to schedule
	 * @param limit  Amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask repeat(Runnable action, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

	/**
	 * Schedules an action that runs on every tick until its limit is reached
	 * 
	 * @param action Action to schedule
	 * @param limit  Amount of times to run this task
	 * @return ScheduledTask instance
	 */
	public ScheduledTask repeatAsync(Runnable action, int limit) {
		ScheduledTask t = new ScheduledTask();
		t.action = action;
		t.limit = limit;
		t.timeStart = System.currentTimeMillis();
		t.async = true;
		synchronized (tasks) {
			tasks.add(t);
		}
		return t;
	}

}
