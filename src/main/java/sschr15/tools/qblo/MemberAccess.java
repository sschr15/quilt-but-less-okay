/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import jdk.internal.reflect.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class MemberAccess {
	/**
	 * Assumes that the caller has already checked the type of the field.
	 */
	public static <T> T getFieldValue(@NotNull Field field, @Nullable Object instance) {
		try {
			return (T) field.get(instance);
		} catch (IllegalAccessException e) {
			// Traditional method didn't work; Plan B: try some dirtier tricks.
			String primitive = field.getType().isPrimitive() ? field.getType().getName() : "";

			var unsafe = Unsafe.jdk();

			Object base;
			long offset;
			if (Modifier.isStatic(field.getModifiers())) {
				base = unsafe.staticFieldBase(field);
				offset = unsafe.staticFieldOffset(field);
			} else {
				if (instance != null) {
					base = instance;
				} else {
					throw new IllegalArgumentException("Instance field has no instance");
				}
				offset = unsafe.objectFieldOffset(field);
			}

			return (T) switch (primitive) {
				case "byte" -> unsafe.getByte(base, offset);
				case "short" -> unsafe.getShort(base, offset);
				case "int" -> unsafe.getInt(base, offset);
				case "long" -> unsafe.getLong(base, offset);
				case "float" -> unsafe.getFloat(base, offset);
				case "double" -> unsafe.getDouble(base, offset);
				case "char" -> unsafe.getChar(base, offset);
				case "boolean" -> unsafe.getBoolean(base, offset);
				default -> unsafe.getReference(base, offset);
			};
		}
	}

	@SuppressWarnings("ConstantConditions")
	public static void setFieldValue(@NotNull Field field, @Nullable Object instance, @Nullable Object value) {
		if (field.getType().isPrimitive()) {
			Objects.requireNonNull(value, "Primitives cannot be null");
		}

		int staticFinal = Modifier.STATIC | Modifier.FINAL;
		if ((field.getModifiers() & staticFinal) == staticFinal) {
			Class<?> type = field.getType();
			/*
			 * All fields *can* be set via reflection, but if the fields are static and final while
			 * also being a type that javac inlines, then modifying the field will likely do nothing
			 * helpful.
			 */
			if (type.isPrimitive() || type.equals(String.class)) {
				Class<?> caller = Reflection.getCallerClass();
				System.err.println("Class " + caller + " is trying to set a static final field. This will not update inlined constants.");
			}
		}

		// Jump immediately to the unsafe order of operations.
		var unsafe = Unsafe.jdk();

		Object base;
		long offset;
		if (Modifier.isStatic(field.getModifiers())) {
			base = unsafe.staticFieldBase(field);
			offset = unsafe.staticFieldOffset(field);
		} else {
			if (instance != null) {
				base = instance;
			} else {
				throw new IllegalArgumentException("Instance field has no instance");
			}
			offset = unsafe.objectFieldOffset(field);
		}

		switch (field.getType().getName()) {
			case "byte" -> unsafe.putByte(base, offset, (Byte) value);
			case "short" -> unsafe.putShort(base, offset, (Short) value);
			case "int" -> unsafe.putInt(base, offset, (Integer) value);
			case "long" -> unsafe.putLong(base, offset, (Long) value);
			case "float" -> unsafe.putFloat(base, offset, (Float) value);
			case "double" -> unsafe.putDouble(base, offset, (Double) value);
			case "char" -> unsafe.putChar(base, offset, (Character) value);
			case "boolean" -> unsafe.putBoolean(base, offset, (Boolean) value);
			default -> unsafe.putReference(base, offset, value);
		}
	}
}
