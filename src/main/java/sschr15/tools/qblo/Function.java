/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import java.util.Arrays;

public sealed interface Function {
	String returnType();

	static Function noReturn(Void action) {
		return action;
	}

	static Function returnInt(Int action) {
		return action;
	}

	static Function returnLong(Long action) {
		return action;
	}

	static Function returnFloat(Float action) {
		return action;
	}

	static Function returnDouble(Double action) {
		return action;
	}

	static Function returnBoolean(Boolean action) {
		return action;
	}

	static Function returnChar(Char action) {
		return action;
	}

	static Function returnByte(Byte action) {
		return action;
	}

	static Function returnShort(Short action) {
		return action;
	}

	static <T> Function returnObject(NonPrimitive<T> action) {
		return action;
	}

	non-sealed interface Void extends Function {
		void apply(Object... args);
		default String returnType() {
			return "V";
		}
	}
	non-sealed interface Int extends Function {
		int apply(Object... args);
		default String returnType() {
			return "I";
		}
	}
	non-sealed interface Long extends Function {
		long apply(Object... args);
		default String returnType() {
			return "J";
		}
	}
	non-sealed interface Double extends Function {
		double apply(Object... args);
		default String returnType() {
			return "D";
		}
	}
	non-sealed interface Float extends Function {
		float apply(Object... args);
		default String returnType() {
			return "F";
		}
	}
	non-sealed interface Short extends Function {
		short apply(Object... args);
		default String returnType() {
			return "S";
		}
	}
	non-sealed interface Char extends Function {
		char apply(Object... args);
		default String returnType() {
			return "C";
		}
	}
	non-sealed interface Byte extends Function {
		byte apply(Object... args);
		default String returnType() {
			return "B";
		}
	}
	non-sealed interface Boolean extends Function {
		boolean apply(Object... args);
		default String returnType() {
			return "Z";
		}
	}

	non-sealed interface NonPrimitive<T> extends Function {
		T apply(Object... args);
		default String returnType() {
			return Object.class.descriptorString();
		}
	}

	@SuppressWarnings("unchecked")
	Class<? extends Function>[] types = new Class[] {
			Void.class,
			Int.class,
			Long.class,
			Double.class,
			Float.class,
			Short.class,
			Char.class,
			Byte.class,
			Boolean.class,
			NonPrimitive.class
	};

	static Class<? extends Function> getFunctionType(Function function) {
		Class<?> clazz = function.getClass(); // likely a lambda
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> iface : interfaces) {
			if (Arrays.asList(types).contains(iface)) {
				return (Class<? extends Function>) iface;
			}
		}
		throw new IllegalArgumentException("Function " + function + " is not a valid function");
	}
}
