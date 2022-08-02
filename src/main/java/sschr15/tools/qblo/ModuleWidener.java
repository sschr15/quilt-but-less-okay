/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import java.lang.invoke.MethodHandle;

public class ModuleWidener {
	private static final Module EVERYONE_MODULE;
	private static final MethodHandle moduleAddExportsOrOpens; // Module.addExportsOrOpens(String package, Module to, boolean openInsteadOfExport, boolean syncVm)

	static {
		try {
			EVERYONE_MODULE = (Module) TrustedLookup.findStaticGetter(Module.class, "EVERYONE_MODULE", Module.class).invokeExact();

			moduleAddExportsOrOpens = TrustedLookup.findVirtual(
					Module.class, "implAddExportsOrOpens",
					void.class, String.class, Module.class, boolean.class, boolean.class
			);
		} catch (Throwable e) {
			Unsafe.OBJECTS.sunUnsafe().throwException(e);
			throw new RuntimeException(e);
		}
	}

	public static void exportModule(Module module, String pkg) {
		try {
			// both `exports` and `opens` the module to the EVERYONE_MODULE
			moduleAddExportsOrOpens.invokeExact(module, pkg, EVERYONE_MODULE, false, true);
			moduleAddExportsOrOpens.invokeExact(module, pkg, EVERYONE_MODULE, true, true);
		} catch (Throwable e) {
			Unsafe.OBJECTS.sunUnsafe().throwException(e);
			throw new RuntimeException(e);
		}
	}

	public static void exportModule(Class<?> classInPackage) {
		exportModule(classInPackage.getModule(), classInPackage.getPackageName());
	}

	public static void exportModule(Class<?> classInModule, String pkg) {
		exportModule(classInModule.getModule(), pkg);
	}
}
