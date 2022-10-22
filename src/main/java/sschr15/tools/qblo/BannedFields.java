/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import sschr15.tools.qblo.internals.Loader;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;

public class BannedFields {
	@SuppressWarnings("removal")
	public static void setSecurity(SecurityManager security) {
		try {
			MethodHandle securitySetter = Unsafe.lookup().findStaticSetter(System.class, "security", SecurityManager.class);
			securitySetter.invokeExact(security);

			// force update security manager
			// by classloading goodness!
			ModuleWidener.exportModule(Object.class);
			Class<?> clazz = Loader.loadInBootLoader("sschr15.tools.qblo.BannedFields$SecuritySettingMeasures");
			Unsafe.jdk().ensureClassInitialized(clazz);
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
		}
	}

	private static class SecuritySettingMeasures {
		static {
			try {
				Field f = System.class.getDeclaredField("allowSecurityManager");
				f.setAccessible(true);
				f.set(null, 2); // = MAYBE
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
}
