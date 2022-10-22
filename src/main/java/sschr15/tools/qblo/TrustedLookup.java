/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import org.intellij.lang.annotations.Identifier;
import sschr15.tools.qblo.annotations.JvmClass;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

public class TrustedLookup {
	public static MethodHandle findVirtual(Class<?> clazz, @Identifier String name, Class<?> returnType, Class<?>... parameterTypes) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findVirtual(clazz, name, MethodType.methodType(returnType, parameterTypes));
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findStatic(Class<?> clazz, @Identifier String name, Class<?> returnType, Class<?>... parameterTypes) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findStatic(clazz, name, MethodType.methodType(returnType, parameterTypes));
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findConstructor(clazz, MethodType.methodType(void.class, parameterTypes));
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findSpecial(Class<?> clazz, @Identifier String name, Class<?> returnType, Class<?>... parameterTypes) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findSpecial(clazz, name, MethodType.methodType(returnType, parameterTypes), clazz);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findGetter(Class<?> clazz, @Identifier String name, Class<?> returnType) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findGetter(clazz, name, returnType);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findSetter(Class<?> clazz, @Identifier String name, Class<?> parameterType) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findSetter(clazz, name, parameterType);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findStaticGetter(Class<?> clazz, @Identifier String name, Class<?> returnType) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findStaticGetter(clazz, name, returnType);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	public static MethodHandle findStaticSetter(Class<?> clazz, @Identifier String name, Class<?> parameterType) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findStaticSetter(clazz, name, parameterType);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	/**
	 * Get a class, regardless of whether it is considered open or not.
	 */
	public static Class<?> getClass(@JvmClass.ForName String name) {
		try {
			return Unsafe.OBJECTS.trustedLookup().findClass(name);
		} catch (Exception e) {
			throw rethrow(e);
		}
	}

	private static RuntimeException rethrow(Throwable t) {
		Unsafe.OBJECTS.sunUnsafe().throwException(t);
		throw new RuntimeException(t);
	}
}
