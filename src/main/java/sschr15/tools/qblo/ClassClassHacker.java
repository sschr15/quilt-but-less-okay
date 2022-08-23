package sschr15.tools.qblo;

import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodInsnNode;
import sschr15.tools.qblo.internals.InternalAsm;

import java.lang.instrument.ClassDefinition;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A class that heavily modifies {@link Class}, allowing for extra concerning functionality.
 * <hr/>
 * <h2>Changes made</h2>
 * <ul>
 *     <li>Replaces all calls to {@link System#getSecurityManager()} with null</li>
 *     <li>Allows enum classes to be subclassed and still be treated as an enum</li>
 *     <li>Weakens the requirements on {@link Class#isRecord()}</li>
 *     <li>Includes all non-public fields in calls to {@link Class#getFields()}</li>
 *     <li>Includes all non-public methods in calls to {@link Class#getMethods()}</li>
 *     <li>Removes some caching &#x1f980;</li>
 * </ul>
 */
public class ClassClassHacker {
	public static void main(String[] args) throws Throwable {
		changeClass(true);
	}

	/**
	 * Modify the class {@code Class} to allow for extra concerning functionality.
	 * @see ClassClassHacker
	 * @param export whether to export the modified class
	 */
	public static void changeClass(boolean export) {
		try {
			InternalAsm.init();

			var inst = InstrumentationSetup.getInstrumentation();

			byte[] bytes;
			try (var is = Class.class.getResourceAsStream("/java/lang/Class.class")) {
				bytes = is.readAllBytes();
			}

			var cn = new ClassNode();
			new ClassReader(bytes).accept(cn, 0);

			for (var node : cn.methods) {
				var insns = node.instructions;

				// Patch out all security manager checks
				for (var insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
					if (insn instanceof MethodInsnNode m && m.owner.equals("java/lang/System") && m.desc.equals("()Ljava/lang/SecurityManager;")) {
						insns.insert(insn, new InsnNode(Opcodes.ACONST_NULL));
						insns.remove(insn);
					}
				}

				// ...special modifications
				switch (node.name + node.desc) {
					case "isEnum()Z" -> { // replace so it returns true if any superclass is an enum
						insns.clear();
						node.visitCode();
						node.visitVarInsn(Opcodes.ALOAD, 0);
						node.visitInsn(Opcodes.DUP);
						// Check if this class is java.lang.Enum
						node.visitLdcInsn(Type.getType(Enum.class));
						Label no = new Label();
						node.visitJumpInsn(Opcodes.IF_ACMPNE, no);
						node.visitInsn(Opcodes.POP);
						node.visitInsn(Opcodes.ICONST_1);
						node.visitInsn(Opcodes.IRETURN);

						// if not, check if the superclass is null
						node.visitLabel(no);
						node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
						node.visitInsn(Opcodes.DUP);
						Label nonnull = new Label();
						node.visitJumpInsn(Opcodes.IFNONNULL, nonnull);
						// If null, no
						node.visitInsn(Opcodes.POP);
						node.visitInsn(Opcodes.ICONST_0);
						node.visitInsn(Opcodes.IRETURN);

						// This isn't the topmost class, so check if the superclass is an enum
						node.visitLabel(nonnull);
						node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "isEnum", "()Z", false);
						node.visitInsn(Opcodes.IRETURN);
					}
					case "isRecord()Z" -> { // only check if the native impl declares it as such
						insns.clear();
						node.visitCode();
						node.visitVarInsn(Opcodes.ALOAD, 0);
						node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "isRecord0", "()Z", false);
						node.visitInsn(Opcodes.IRETURN);
					}
					case "privateGetPublicFields()[Ljava/lang/reflect/Field;" -> { // add non-public fields
						var n = node.instructions.getFirst();
						while (!(n instanceof MethodInsnNode) || !((MethodInsnNode) n).name.equals("privateGetDeclaredFields")) {
							n = n.getNext();
						}
						insns.insertBefore(n, new InsnNode(Opcodes.POP));
						insns.insertBefore(n, new InsnNode(Opcodes.ICONST_0));
					}
					case "privateGetPublicMethods()[Ljava/lang/reflect/Method;" -> { // add non-public methods
						var n = node.instructions.getFirst();
						while (!(n instanceof MethodInsnNode) || !((MethodInsnNode) n).name.equals("privateGetDeclaredMethods")) {
							n = n.getNext();
						}
						insns.insertBefore(n, new InsnNode(Opcodes.POP));
						insns.insertBefore(n, new InsnNode(Opcodes.ICONST_0));
					}
					case "getMethodsRecursive(Ljava/lang/String;[Ljava/lang/Class;Z)Ljava/lang/PublicMethods$MethodList;" -> { // add non-public methods (again)
						var n = node.instructions.getFirst();
						while (!(n instanceof MethodInsnNode)) { // find the first method invocation
							n = n.getNext();
						}
						insns.insertBefore(n, new InsnNode(Opcodes.POP));
						insns.insertBefore(n, new InsnNode(Opcodes.ICONST_0));
					}
					case "reflectionData()Ljava/lang/Class$ReflectionData;" -> { // reflection data is 🦀
						insns.clear();
						node.visitCode();
						node.visitInsn(Opcodes.ACONST_NULL);
						node.visitInsn(Opcodes.ARETURN);
					}
					case "getSimpleName()Ljava/lang/String;" -> { // Fix NPE from null reflection data
						insns.clear();
						node.visitCode();
						node.visitVarInsn(Opcodes.ALOAD, 0);
						node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getSimpleName0", "()Ljava/lang/String;", false);
						node.visitInsn(Opcodes.ARETURN);
					}
					case "getCanonicalName()Ljava/lang/String;" -> { // Fix NPE from null reflection data
						insns.clear();
						node.visitCode();
						node.visitVarInsn(Opcodes.ALOAD, 0);
						node.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getCanonicalName0", "()Ljava/lang/String;", false);
						// apparently it checks against a "null sentinel" value
						node.visitInsn(Opcodes.DUP);
						node.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Class$ReflectionData", "NULL_SENTINEL", "Ljava/lang/String;");
						Label nonnull = new Label();
						node.visitJumpInsn(Opcodes.IF_ACMPNE, nonnull);
						node.visitInsn(Opcodes.POP);
						node.visitInsn(Opcodes.ACONST_NULL);
						node.visitLabel(nonnull);
						node.visitInsn(Opcodes.ARETURN);
					}
				}
			}

			var cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			cn.accept(cw);
			bytes = cw.toByteArray();

			if (export) {
				try (var os = Files.newOutputStream(Path.of("./Class.class"))) {
					os.write(bytes);
				}
			}

			inst.redefineClasses(new ClassDefinition(
					Class.class,
					bytes
			));
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
		}
	}
}
