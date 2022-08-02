/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;

/**
 * A static instance of the {@link sun.misc.Unsafe} class,
 * the {@link MethodHandles.Lookup#IMPL_LOOKUP trusted} lookup,
 * and the (normally) inaccessible {@link jdk.internal.misc.Unsafe} class.
 */
@SuppressWarnings({"JavadocReference", "RedundantSuppression", "unused"})
public class Unsafe {
	public static final Objects OBJECTS;
	static {
		try {
			Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			sun.misc.Unsafe sunUnsafe = (sun.misc.Unsafe) field.get(null);

			// jdk.internal.misc.Unsafe is a lot harder to obtain an instance of, due to
			// its "internal" nature. We'll use reflection and the Sun unsafe to get at it.

			// Step 1: get MethodHandles.Lookup.IMPL_LOOKUP
			Class<MethodHandles.Lookup> lookupClass = MethodHandles.Lookup.class;
			Field lookupField = lookupClass.getDeclaredField("IMPL_LOOKUP");

			long offset = sunUnsafe.staticFieldOffset(lookupField);
			Object base = sunUnsafe.staticFieldBase(lookupField);
			MethodHandles.Lookup trustedLookup = (MethodHandles.Lookup) sunUnsafe.getObject(base, offset);

			// Step 2: export jdk.internal.misc package (because it's not exported by default)
			MethodHandle moduleAddExportsOrOpens = trustedLookup.findVirtual(
					Module.class, "implAddExportsOrOpens",
					MethodType.methodType(void.class, String.class, Module.class, boolean.class, boolean.class)
			); // String package, Module toOpenTo, boolean isOpen, boolean syncVM
			Module javaBase = ModuleLayer.boot().findModule("java.base").orElseThrow();
			Module allUnnamed = (Module) trustedLookup.findStaticGetter(Module.class, "EVERYONE_MODULE", Module.class).invoke();
			moduleAddExportsOrOpens.invoke(javaBase, "jdk.internal.misc", allUnnamed, false, true);

			// Step 3: Get the Unsafe instance via method handle invocation
			Class<?> internal = trustedLookup.findClass("jdk.internal.misc.Unsafe");
			MethodHandle theUnsafe = trustedLookup.findStaticGetter(internal, "theUnsafe", internal);
			jdk.internal.misc.Unsafe unsafe = (jdk.internal.misc.Unsafe) theUnsafe.invokeExact();

			// Step 4 (the easy one): Set the objects instance
			OBJECTS = new Objects(sunUnsafe, unsafe, trustedLookup);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to get Unsafe instance", t);
		}
	}

	public static sun.misc.Unsafe sun() {
		return OBJECTS.sunUnsafe;
	}

	public static jdk.internal.misc.Unsafe jdk() {
		return OBJECTS.internalUnsafe;
	}

	public static MethodHandles.Lookup lookup() {
		return OBJECTS.trustedLookup;
	}

	public record Objects(
			sun.misc.Unsafe sunUnsafe,
			jdk.internal.misc.Unsafe internalUnsafe,
			MethodHandles.Lookup trustedLookup
	) {}
}
