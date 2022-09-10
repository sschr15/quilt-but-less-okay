/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
