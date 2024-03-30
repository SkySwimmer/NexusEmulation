package org.asf.nexus.tasks.async;

import static org.asf.nexus.tasks.async.AsyncTask.*;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AsyncTests {

	private boolean syncCompleted;
	private boolean syncCompleted2;

	@Test
	public void testSynchronized() {
		syncCompleted = false;
		runSynced(testCall());
		assertTrue(syncCompleted);
		syncCompleted = false;
	}

	@Test
	public void testAsyncCallback() {
		syncCompleted = false;
		runAsync(testCall(), () -> {
			assertTrue(syncCompleted);
		}).block();
		syncCompleted = false;
	}

	@Test
	public void testAsynchronized() {
		syncCompleted = false;
		await(testCall());
		assertTrue(syncCompleted);
		syncCompleted = false;
	}

	@Test
	public void testAsynchronizedNestedAwait() {
		syncCompleted = false;
		syncCompleted2 = false;
		await(testCall());
		assertTrue(syncCompleted);
		assertTrue(syncCompleted2);
		syncCompleted = false;
		syncCompleted2 = false;
	}

	@Test
	public void testAsynchronizedFetcher() {
		runAsync(testFetcher(), result -> {
			assertTrue(result.equals("Hello world"));
		}).block();
	}

	public AsyncTask<String> testFetcher() {
		return createTask(() -> {
			return "Hello world";
		});
	}

	public AsyncTask<Void> testCall() {
		return createTask(() -> {
			await(testCall2());
			syncCompleted = true;
		});
	}

	public AsyncTask<Void> testCall2() {
		return createTask(() -> {
			syncCompleted2 = true;
		});
	}

}
