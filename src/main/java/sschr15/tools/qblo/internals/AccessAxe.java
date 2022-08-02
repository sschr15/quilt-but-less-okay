package sschr15.tools.qblo.internals;

import sschr15.tools.qblo.ModuleWidener;

/**
 * Opens jdk.internal.access to everyone.
 */
public class AccessAxe {
	static {
		ModuleWidener.exportModule(Object.class, "jdk.internal.access");
	}

	public static void init() {}
}
