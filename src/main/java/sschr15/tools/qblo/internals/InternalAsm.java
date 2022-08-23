package sschr15.tools.qblo.internals;

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
}
