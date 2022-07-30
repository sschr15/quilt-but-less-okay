/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

import java.util.Arrays;

public class TestInit implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		try {
			Arrays INSTANCE = (Arrays) TrustedLookup.findConstructor(Arrays.class).invokeExact();

			System.out.println(INSTANCE);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}
