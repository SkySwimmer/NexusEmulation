package org.asf.nexus.tasks.scheduling;

/**
 * 
 * Scheduled Task Information Object
 * 
 * @author Sky Swimmer
 * 
 */
public class ScheduledTask {
	Runnable action;
	boolean ran;

	boolean async;

	long timeStart;
	long timeWait = -1;

	int interval;
	int limit = 1;
	int cInterval;
	int cCount;

	ScheduledTask() {
	}

	/**
	 * Retrieves the amount of time remaining before the task is invoked
	 * 
	 * @return Amount of milliseconds before the task is up for invocation or -1 if
	 *         not a time-based delay
	 */
	public long getTimeRemainingBeforeInvoke() {
		if (timeWait == -1)
			return -1;
		long time = System.currentTimeMillis();
		if (time - timeStart > timeWait)
			return 0;
		return timeWait - (time - timeStart);
	}

	/**
	 * Checks if the task has been run at least once
	 * 
	 * @return True if the task has been run
	 */
	public boolean hasRun() {
		return ran;
	}

	/**
	 * Blocks until the task has been invoked successfully
	 */
	public void block() {
		while (!ran)
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
	}

	/**
	 * Retrieves the action tick interval (how many ticks before the action is run)
	 * 
	 * @return Action tick interval
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * Retrieves how often the action can run
	 * 
	 * @return Action tick limit
	 */
	public int getActionLimit() {
		return interval;
	}

	/**
	 * Retrieves the amount of remaining ticks before the action stops running
	 * 
	 * @return Remaining tick count
	 */
	public int getRemainingTicks() {
		if (limit == -1)
			return -1;
		return limit - cCount;
	}

	/**
	 * Retrieves the amount of ticks that remain before the action is run
	 * 
	 * @return Amount of ticks before the task runs
	 */
	public int getTicksBeforeStart() {
		if (interval <= 0 || cCount >= limit)
			return 0;
		return interval - cInterval;
	}

}
