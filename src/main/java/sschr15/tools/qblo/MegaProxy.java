/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import sschr15.tools.qblo.internals.InternalAsm;
import sschr15.tools.qblo.internals.InternalReflectVerifier;
import sschr15.tools.qblo.nativent.flags.JvmFlag;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Like {@link java.lang.reflect.Proxy} but *more*
 */
@SuppressWarnings("unchecked")
public class MegaProxy {
	/**
	 * Create a new subclass of the given class with the method implementations specified in the given map.
	 * @param clazz The class to extend.
	 * @param methods The methods to implement. The key is a method name with its descriptor.
	 * @param <T> The type of the class.
	 * @return A new instance of the given class, which also implements {@link HasClassBytes}.
	 *         Because of weirdness in type variables, this is not explicitly specified.
	 */
	public static <T> T newSubclass(Class<T> clazz, Object[] constructorParams, Map<String, Function> methods) {
		InternalAsm.init();
		InternalReflectVerifier.init();

		int currentClassfileVersion = Integer.parseInt(System.getProperty("java.class.version").split("\\.")[0]);
		String callerName = Thread.currentThread().getStackTrace()[2].getClassName();
		Class<?> caller;
		try {
			caller = Class.forName(callerName);
		} catch (ClassNotFoundException e) {
			throw (Error) Utils.justThrow(e);
		}

		String superName = clazz.getName().replace('.', '/');
		String newName = caller.getName().replace('.', '/') + "$" + Objects.hash(clazz, methods);
		boolean isFinal = Modifier.isFinal(clazz.getModifiers());

		// subclassing final classes needs extra work
		if (isFinal) {
			// step 1: ensure the JVM is a DCEVM build
			ensureDcevm();

			// step 2: un-set the final flag
			ClassNode node = new ClassNode();
			try {
				new ClassReader(clazz.getName()).accept(node, 0);
			} catch (IOException e) {
				throw (Error) Utils.justThrow(e);
			}

			node.access &= ~Opcodes.ACC_FINAL;

			// step 3: redefine the class
			redefineClass(clazz, node);
		}

		// overriding final methods needs extra work
		Class<? super T> currentClass = clazz;
		List<Class<? super T>> classesRequiringModification = new ArrayList<>();
		while (!currentClass.equals(Object.class)) {
			for (var method : currentClass.getDeclaredMethods()) {
				StringBuilder sb = new StringBuilder(method.getName()).append('(');
				for (var param : method.getParameterTypes()) {
					sb.append(Type.getDescriptor(param));
				}
				sb.append(')');
				sb.append(Type.getDescriptor(method.getReturnType()));
				String key = sb.toString();
				if (!methods.containsKey(key)) continue;

				// check if final *OR* less visible than protected
				if (Modifier.isFinal(method.getModifiers())) {
					classesRequiringModification.add(currentClass);
					break;
				} else if (!Modifier.isProtected(method.getModifiers()) && !Modifier.isPublic(method.getModifiers())) {
					classesRequiringModification.add(currentClass);
					break;
				}
			}
			currentClass = currentClass.getSuperclass();
		}

		if (!classesRequiringModification.isEmpty()) {
			ensureDcevm();

			for (var needsRemod : classesRequiringModification) {
				ClassNode node = new ClassNode();
				try {
					new ClassReader(needsRemod.getName()).accept(node, 0);
				} catch (IOException e) {
					throw (Error) Utils.justThrow(e);
				}

				for (var method : node.methods) {
					String key = method.name + method.desc;
					if (!methods.containsKey(key)) continue;

					method.access &= ~Opcodes.ACC_FINAL;
					if (!Modifier.isPublic(method.access)) {
						method.access |= Opcodes.ACC_PROTECTED;
					}
				}

				redefineClass(needsRemod, node);
			}
		}

		Constructor<?>[] constructors = clazz.getDeclaredConstructors();
		Constructor<?> constructor = null;
		for (Constructor<?> c : constructors) {
			if (c.getParameterCount() == constructorParams.length) {
				Class<?>[] params = c.getParameterTypes();
				boolean match = true;
				https://concern.i.ng
				for (int i = 0; i < params.length; i++) {
					Class<?> a = params[i];
					Class<?> b = constructorParams[i].getClass();
					if (b.isPrimitive() && equals(b, a)) {
						b = Utils.box(b);
					}
					if (!params[i].isAssignableFrom(constructorParams[i].getClass())) {
						match = false;
						break https; // because that one line of code is syntactically correct :)
					}
				}
				if (match) {
					constructor = c;
					break;
				}
			}
		}

		if (constructor == null) {
			throw new IllegalArgumentException("No matching constructor found");
		}

		ClassNode node = new ClassNode();
		node.visit(currentClassfileVersion, Opcodes.ACC_PUBLIC, newName, null, superName, new String[]{MegaProxy.HasClassBytes.internalName});
		node.visitField(Opcodes.ACC_PRIVATE, "map", "Ljava/util/Map;", null, null);
		node.visitField(Opcodes.ACC_PRIVATE, "classBytes", "[B", null, null);

		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (Class<?> c : constructor.getParameterTypes()) {
			sb.append(c.descriptorString());
		}
		sb.append(Map.class.descriptorString());
		sb.append(byte[].class.descriptorString());
		sb.append(")V");
		MethodVisitor method = node.visitMethod(Opcodes.ACC_PUBLIC, "<init>", sb.toString(), null, null);
		method.visitCode();
		method.visitVarInsn(Opcodes.ALOAD, 0);
		sb = new StringBuilder("(");
		for (Class<?> c : constructor.getParameterTypes()) {
			sb.append(c.descriptorString());
		}
		sb.append(")V");
		method.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", sb.toString(), false);
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitVarInsn(Opcodes.ALOAD, 1);
		method.visitFieldInsn(Opcodes.PUTFIELD, node.name, "map", "Ljava/util/Map;");
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitVarInsn(Opcodes.ALOAD, 2);
		method.visitFieldInsn(Opcodes.PUTFIELD, node.name, "classBytes", "[B");
		method.visitInsn(Opcodes.RETURN);
		method.visitEnd(); // stack / frames / locals are managed by ASM

		for (Map.Entry<String, Function> entry : methods.entrySet()) {
			String key = entry.getKey();
			String realDesc = key.substring(key.indexOf('('));
			String name = key.substring(0, key.indexOf('('));
			Function function = entry.getValue();
			String functionDesc = "([Ljava/lang/Object;)" + function.returnType();
			String functionInternal = Function.getFunctionType(function).getName().replace('.', '/');
			int paramCount = Type.getArgumentTypes(realDesc).length;

			method = node.visitMethod(Opcodes.ACC_PUBLIC, name, realDesc, null, null);
			method.visitCode();
			method.visitVarInsn(Opcodes.ALOAD, 0);
			method.visitFieldInsn(Opcodes.GETFIELD, node.name, "map", "Ljava/util/Map;");
			method.visitLdcInsn(key);
			method.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
			method.visitTypeInsn(Opcodes.CHECKCAST, functionInternal);
			InternalAsm.putIntNode(method, paramCount);
			method.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
			for (int i = 0; i < paramCount; i++) {
				method.visitInsn(Opcodes.DUP);
				InternalAsm.putIntNode(method, i);
				Type type = Type.getArgumentTypes(realDesc)[i];
				if (type.getSort() != Type.OBJECT) {
					method.visitVarInsn(type.getOpcode(Opcodes.ILOAD), i + 1);
					box(method, type);
				} else {
					method.visitVarInsn(Opcodes.ALOAD, i + 1);
				}
				method.visitInsn(Opcodes.AASTORE);
			}
			method.visitMethodInsn(Opcodes.INVOKEINTERFACE, functionInternal, "apply", functionDesc, true);
			Type returnType = Type.getType(realDesc.substring(realDesc.indexOf(')') + 1));
			if (returnType.getSort() == Type.OBJECT || returnType.getSort() == Type.ARRAY) {
				method.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
			}
			method.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
			method.visitEnd();
		}

		// getClassBytes()[B
		method = node.visitMethod(Opcodes.ACC_PUBLIC, "getClassBytes", "()[B", null, null);
		method.visitCode();
		method.visitVarInsn(Opcodes.ALOAD, 0);
		method.visitFieldInsn(Opcodes.GETFIELD, node.name, "classBytes", "[B");
		method.visitInsn(Opcodes.ARETURN);
		method.visitEnd();

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		node.accept(writer);
		byte[] bytes = writer.toByteArray();
		Class<?> outputClass;
		try {
			outputClass = Unsafe.lookup().in(caller).defineClass(bytes);
		} catch (IllegalAccessException e) {
			throw (Error) Utils.justThrow(e); // This isn't possible - the trusted lookup never throws this exception
		}

		Object[] newParams = Arrays.copyOf(constructorParams, constructorParams.length + 2);
		newParams[newParams.length - 2] = methods;
		newParams[newParams.length - 1] = bytes;
		Class<?>[] paramTypes = Arrays.copyOf(constructor.getParameterTypes(), constructor.getParameterTypes().length + 2);
		paramTypes[paramTypes.length - 2] = Map.class;
		paramTypes[paramTypes.length - 1] = byte[].class;
		try {
			MethodHandle ctor = Unsafe.lookup().findConstructor(outputClass, MethodType.methodType(void.class, paramTypes));
			return (T) ctor.invokeWithArguments(newParams);
		} catch (Throwable e) {
			throw (Error) Utils.justThrow(e);
		}
	}

	private static void redefineClass(Class<?> clazz, ClassNode node) {
		ClassWriter writer = new ClassWriter(0);
		node.accept(writer);
		byte[] bytes = writer.toByteArray();
		try {
			InstrumentationSetup.getInstrumentation().redefineClasses(
					new ClassDefinition(clazz, bytes)
			);
		} catch (ClassNotFoundException | UnmodifiableClassException e) {
			throw (Error) Utils.justThrow(e);
		}
	}

	private static boolean equals(Class<?> a, Class<?> b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		if (a.isPrimitive()) {
			return !b.isPrimitive() && b == Utils.box(a);
		} else if (b.isPrimitive()) {
			return equals(b, a); // guaranteed to only be called once (because the function returns when a is primitive)
		} else {
			return false;
		}
	}

	private static void box(MethodVisitor method, Type type) {
		String unboxedType = type.getDescriptor();
		String boxedType = switch (unboxedType) {
			case "Z" -> "java/lang/Boolean";
			case "C" -> "java/lang/Character";
			case "B" -> "java/lang/Byte";
			case "S" -> "java/lang/Short";
			case "I" -> "java/lang/Integer";
			case "F" -> "java/lang/Float";
			case "J" -> "java/lang/Long";
			case "D" -> "java/lang/Double";
			default -> throw new IllegalArgumentException("Invalid type: " + unboxedType);
		};
		method.visitMethodInsn(Opcodes.INVOKESTATIC, boxedType, "valueOf", "(" + unboxedType + ")L" + boxedType + ";", false);
	}

	private static boolean testedDcevm = false;

	private static void ensureDcevm() {
		if (testedDcevm) {
			return;
		}
		// JBR uses the AllowEnhancedClassRedefinition option, so that's an easy target
		JvmFlag redef = JvmFlag.FLAGS_MAP.get("AllowEnhancedClassRedefinition");
		if (redef == null) {
			throw new IllegalStateException("This JVM does not support enhanced class redefinition, and thus cannot extend final classes or override final methods");
		} else {
			redef.set(0x0002_0011);
			testedDcevm = true;
		}
	}

	public interface HasClassBytes {
		String internalName = HasClassBytes.class.getName().replace('.', '/');
		byte[] getClassBytes();
	}
}
