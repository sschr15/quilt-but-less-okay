/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo.internals;

import jdk.internal.loader.BuiltinClassLoader;
import jdk.internal.loader.ClassLoaders;
import sschr15.tools.qblo.ModuleWidener;
import sschr15.tools.qblo.Unsafe;
import sschr15.tools.qblo.annotations.JvmClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class Loader {
	static {
		ModuleWidener.exportModule(Object.class, "jdk.internal.loader");
	}

	public static void init() {}

	public static Class<?> loadInBootLoader(@JvmClass.ForName String name) {
		try {
			MethodHandle bootLoader = Unsafe.lookup().findStatic(ClassLoaders.class, "bootLoader", MethodType.methodType(BuiltinClassLoader.class));
			// To load it, we have to define it right in the boot path
			try (var stream = Unsafe.class.getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
				if (stream == null) {
					throw new NoClassDefFoundError(name);
				}
				byte[] bytes = stream.readAllBytes();
				BuiltinClassLoader cl = (BuiltinClassLoader) bootLoader.invokeExact();
				MethodHandle defineClass = Unsafe.lookup().findVirtual(ClassLoader.class, "defineClass", MethodType.methodType(Class.class, byte[].class, int.class, int.class));
				return (Class<?>) defineClass.invokeExact((ClassLoader) cl, bytes, 0, bytes.length);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
