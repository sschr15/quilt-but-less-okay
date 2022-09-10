package sschr15.tools.qblo.internals;

import sschr15.tools.qblo.ModuleWidener;

public class Loader {
	static {
		ModuleWidener.exportModule(Object.class, "jdk.internal.loader");
	}

	public static void init() {}
}
