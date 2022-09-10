/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo.internals;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.InsnNode;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import sschr15.tools.qblo.ModuleWidener;

public class InternalAsm {
	static {
		String ROOT = "jdk.internal.org.objectweb.asm";
		ModuleWidener.exportModule(Object.class, ROOT);
		ModuleWidener.exportModule(Object.class, ROOT + ".commons");
		ModuleWidener.exportModule(Object.class, ROOT + ".util");
		ModuleWidener.exportModule(Object.class, ROOT + ".tree");
		ModuleWidener.exportModule(Object.class, ROOT + ".tree.analysis");
		ModuleWidener.exportModule(Object.class, ROOT + ".signature");
	}

	public static void init() {}

	public static AbstractInsnNode getInt(int value) {
		if (-1 <= value && value <= 5) {
			return new InsnNode(Opcodes.ICONST_0 + value);
		} else if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
			return new IntInsnNode(Opcodes.BIPUSH, value);
		} else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
			return new IntInsnNode(Opcodes.SIPUSH, value);
		} else {
			return new LdcInsnNode(value);
		}
	}

	public static void putIntNode(MethodVisitor mv, int value) {
		if (-1 <= value && value <= 5) {
			mv.visitInsn(Opcodes.ICONST_0 + value);
		} else if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.BIPUSH, value);
		} else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
			mv.visitIntInsn(Opcodes.SIPUSH, value);
		} else {
			mv.visitLdcInsn(value);
		}
	}
}
