/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo.nativent;

import java.util.Map;

public record NativeType(
		String type, String superclass,
		int size, boolean isObjectOriented,
		boolean isInt, boolean isUnsigned,
		Map<String, NativeStruct.Field> fields
) {
	public NativeStruct.Field field(String name) {
		return fields.get(name);
	}
}
