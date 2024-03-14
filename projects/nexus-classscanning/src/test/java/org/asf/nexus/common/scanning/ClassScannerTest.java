package org.asf.nexus.common.scanning;

import org.junit.Test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URLClassLoader;
import java.util.stream.Stream;

public class ClassScannerTest {

	@Test
	public void testDiscovery() {
		ClassScanner scanner = new ClassScanner();
		scanner.addDefaultCp();
		String[] classes = scanner.getAllClassNames();
		assertTrue(Stream.of(classes).anyMatch(t -> t.equals(getClass().getTypeName())));
		assertTrue(scanner.isClassKnown(getClass().getTypeName()));
		scanner.close();
	}

	@Test
	public void testLoading() {
		ClassScanner scanner = new ClassScanner();
		scanner.addDefaultCp();
		Class<?>[] instances = scanner.getAllClassInstances();
		assertTrue(Stream.of(instances).anyMatch(t -> t.getTypeName().equals(getClass().getTypeName())));
		scanner.close();
	}

	@Test
	public void testScanningInterfaces() {
		ClassScanner scanner = new ClassScanner();
		scanner.addDefaultCp();
		String[] classes = scanner.findClassNamesExtending(Closeable.class);
		Class<? extends Closeable>[] closeables = scanner.findClassInstancesExtending(Closeable.class);
		assertTrue(Stream.of(closeables).anyMatch(t -> t.getTypeName().equals(ClassScanner.class.getTypeName())));
		assertTrue(Stream.of(classes).anyMatch(t -> t.equals(ClassScanner.class.getTypeName())));
		scanner.close();
	}

	@Test
	public void testScanningAbstracts() {
		ClassScanner scanner = new ClassScanner();
		scanner.addDefaultCp();
		String[] classes = scanner.findClassNamesExtending(URLClassLoader.class);
		assertTrue(Stream.of(classes).anyMatch(t -> t.equals(DynamicClassLoader.class.getTypeName())));
		scanner.close();
	}

	@Test
	public void testScanningAnnos() {
		ClassScanner scanner = new ClassScanner();
		scanner.addDefaultCp();
		String[] classes = scanner.findAnnotatedClassNames(TestAnno.class);
		assertTrue(Stream.of(classes).anyMatch(t -> t.equals(TestObj.class.getTypeName())));
		scanner.close();
	}

	@Retention(RUNTIME)
	@Target(TYPE)
	public @interface TestAnno {
	}

	@TestAnno
	public class TestObj {

	}

}
