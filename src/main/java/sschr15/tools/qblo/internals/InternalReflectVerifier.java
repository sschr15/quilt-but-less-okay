package sschr15.tools.qblo.internals;

import jdk.internal.reflect.Reflection;
import sschr15.tools.qblo.ModuleWidener;
import sschr15.tools.qblo.Unsafe;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;

public class InternalReflectVerifier {
	static {
		ModuleWidener.exportModule(Object.class, "jdk.internal.reflect");
	}

	public static void init() {}

	/**
	 * Hack into {@link Reflection} to force the field and method filters to be empty.
	 */
	public static synchronized void openTheFloodgates() {
		var trusted = Unsafe.lookup();
		try {
			MethodHandle fieldFilterSetter = trusted.findStaticSetter(Reflection.class, "fieldFilterMap", Map.class);
			MethodHandle methodFilterSetter = trusted.findStaticSetter(Reflection.class, "methodFilterMap", Map.class);

			Map<Class<?>, Set<String>> emptyMap = Map.of();

			fieldFilterSetter.invoke(emptyMap);
			methodFilterSetter.invoke(emptyMap);
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
		}
	}
}
