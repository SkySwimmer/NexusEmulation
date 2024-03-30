package org.asf.nexus.tasks.async;

class AsyncTaskThreadHandler extends Thread {

	private boolean available;

	public boolean isAvailable() {
		return available;
	}

	@Override
	public void run() {
		try {
			while (true) {
				available = true;

				// Wait for a task
				AsyncTask<?> task = null;
				long start = System.currentTimeMillis();
				while ((System.currentTimeMillis() - start) < 30000) {
					task = AsyncTaskManager.obtainNext();
					if (task != null)
						break;
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
					}
				}

				// No longer available
				available = false;

				// If no task was selected after 30 seconds, exit
				if (task == null)
					break;

				// Run task
				task.run();
			}
		} finally {
			// End
			synchronized (AsyncTaskManager.threads) {
				AsyncTaskManager.threads.remove(this);
			}
		}
	}

}
