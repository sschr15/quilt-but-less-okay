/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import org.jetbrains.annotations.Contract;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

@SuppressWarnings("unchecked")
public class Utils {
	private static final MethodHandle findNative = getOrThrow(() -> Unsafe.lookup().findStatic(ClassLoader.class, "findNative", MethodType.methodType(long.class, ClassLoader.class, String.class)));

	public static long findNative(ClassLoader loader, String name) {
		try {
			return (long) findNative.invokeExact(loader, name);
		} catch (Throwable e) {
			return justThrow(e);
		}
	}

	public static <T> Class<T> box(Class<T> clazz) {
		return !clazz.isPrimitive() ? clazz : (Class<T>) switch (clazz.getName()) {
			case "boolean" -> Boolean.class;
			case "byte" -> Byte.class;
			case "char" -> Character.class;
			case "short" -> Short.class;
			case "int" -> Integer.class;
			case "long" -> Long.class;
			case "float" -> Float.class;
			case "double" -> Double.class;
			case "void" -> Void.class;
			default -> throw new IllegalArgumentException("Unknown primitive type: " + clazz.getName());
		};
	}

	public static String getString(long address) {
		if (address == 0) return null;
		StringBuilder sb = new StringBuilder();
		while (true) {
			char c = (char) Unsafe.jdk().getByte(address++);
			if (c == 0) break;
			sb.append(c);
		}
		return sb.toString();
	}

	@Contract(" -> null")
	public static <T> T getNull() {
		return null;
	}

	@Contract("_ -> fail")
	public static <T> T justThrow(Throwable t) {
		if (t == null) t = new NullPointerException();
		Unsafe.sun().throwException(t);
		throw new Error();
	}

	public static <T> T getOrThrow(Producer<T> producer) {
		try {
			return producer.get();
		} catch (Throwable t) {
			return justThrow(t);
		}
	}

	public interface Producer<T> {
		T get() throws Throwable;
	}
}
