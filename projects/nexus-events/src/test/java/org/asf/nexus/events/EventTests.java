package org.asf.nexus.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.asf.nexus.events.conditions.EventCondition;
import org.asf.nexus.events.conditions.interfaces.IEventCondition;
import org.asf.nexus.events.impl.asm.IEventDispatcher;
import org.asf.nexus.events.impl.asm.IStaticEventDispatcher;
import org.junit.Test;

public class EventTests {

	public static class TestEventOne extends EventObject {
	}

	public static class TestEventTwo extends EventObject {
	}

	public static class TestEventThree extends EventObject {
	}

	public static class TestCondition implements IEventCondition<TestEventThree> {

		@Override
		public Class<TestEventThree> eventType() {
			return TestEventThree.class;
		}

		@Override
		public boolean matching(IEventReceiver receiverType, Method listener, TestEventThree event) {
			return true;
		}

	}

	public static class TestDispatcherOne implements IEventDispatcher {

		@Override
		public void dispatch(IEventReceiver receiver, EventObject event) {
			((TestEventReceivers) receiver).test1((TestEventOne) event);
		}

	}

	public static class TestDispatcherTest2 implements IEventDispatcher {

		@Override
		public void dispatch(IEventReceiver receiver, EventObject event) {
			((TestEventReceivers) receiver).test5((TestEventOne) event);
		}

	}

	public static class TestDispatcherTwo implements IStaticEventDispatcher {

		@Override
		public void dispatch(EventObject event) {
			TestEventReceivers.test2((TestEventTwo) event);
		}

	}

	public static class TestEventReceivers implements IEventReceiver {
		public boolean receivedTestOne;
		public static boolean receivedTestTwo;
		public boolean receivedTestThree;
		public boolean receivedTestFour;
		public boolean receivedTestFive;
		public boolean receivedTestSix;

		@EventListener
		public void test1(TestEventOne event) {
			receivedTestOne = true;
		}

		@EventListener
		public static void test2(TestEventTwo event) {
			receivedTestTwo = true;
		}

		@EventListener
		public void test3(TestEventOne event) {
			receivedTestThree = true;
		}

		@EventListener
		public Object test5(TestEventOne event) {
			receivedTestThree = true;
			return null;
		}

		@EventListener
		private void test4(TestEventOne event) {
			receivedTestFour = true;
		}

		@EventListener
		@EventCondition(TestCondition.class)
		@EventCondition(TestCondition.class)
		public void test5(TestEventThree event) {
			receivedTestFive = true;
		}

		@EventListener
		public void test6(TestEventThree event) {
			receivedTestSix = true;
		}
	}

	private TestEventReceivers rec = new TestEventReceivers();

	public EventTests() {
		// Create receivers
		TestEventReceivers.receivedTestTwo = false;
		EventBus.getInstance().addAllEventsFromReceiver(rec);
	}

	@Test
	public void testPublic() {
		// Dispatch
		rec.receivedTestOne = false;
		rec.receivedTestThree = false;
		EventBus.getInstance().dispatchEvent(new TestEventOne());
		assertTrue(rec.receivedTestOne);
		assertTrue(rec.receivedTestThree);
	}

	@Test
	public void testPrivate() {
		// Dispatch
		rec.receivedTestFour = false;
		EventBus.getInstance().dispatchEvent(new TestEventOne());
		assertFalse(rec.receivedTestFour);
	}

	@Test
	public void testStatic() {
		// Dispatch
		TestEventReceivers.receivedTestTwo = false;
		EventBus.getInstance().dispatchEvent(new TestEventTwo());
		assertTrue(TestEventReceivers.receivedTestTwo);
	}

	@Test
	public void testConditions() {
		// Dispatch
		rec.receivedTestFive = false;
		rec.receivedTestSix = false;
		EventBus.getInstance().dispatchEvent(new TestEventThree());
		assertTrue(rec.receivedTestFive);
		assertTrue(rec.receivedTestSix);
	}

}
