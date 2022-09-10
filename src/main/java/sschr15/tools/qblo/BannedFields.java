/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import java.lang.invoke.MethodHandle;

public class BannedFields {
	@SuppressWarnings("removal")
	public static void setSecurity(SecurityManager security) {
		try {
			MethodHandle securitySetter = Unsafe.lookup().findStaticSetter(System.class, "security", SecurityManager.class);
			securitySetter.invokeExact(security);
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
		}
	}
}
