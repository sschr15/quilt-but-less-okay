/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package sschr15.tools.qblo.nativent.flags;

import sschr15.tools.qblo.Unsafe;
import sschr15.tools.qblo.Utils;
import sschr15.tools.qblo.nativent.NativeHackery;
import sschr15.tools.qblo.nativent.NativeType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public record JvmFlag(long addr, String name, int flags, int type) {
	public void set(int flags) {
		Unsafe.jdk().putInt(addr, flags);
		Unsafe.jdk().putInt(this, OFFSET, flags);
	}

	private static final NativeType TYPE = NativeHackery.getType("JVMFlag");
	private static final long OFFSET = Unsafe.jdk().objectFieldOffset(JvmFlag.class, "flags");
	public static final JvmFlag[] FLAGS;
	public static final Map<String, JvmFlag> FLAGS_MAP;

	static {
		var numFlagsField = TYPE.field("numFlags");
		long size = Unsafe.jdk().getLong(numFlagsField.offset());
		if (size > Integer.MAX_VALUE) throw new IllegalStateException("Too many flags!");
		long flagsPointer = Unsafe.jdk().getAddress(TYPE.field("flags").offset());

		long addrOffset = TYPE.field("_addr").offset();
		long nameOffset = TYPE.field("_name").offset();
		long flagsOffset = TYPE.field("_flags").offset();
		long typeOffset = TYPE.field("_type").offset();

		JvmFlag[] arr = new JvmFlag[(int) size];
		FLAGS_MAP = new HashMap<>();
		for (int i = 0; i < size; i++) {
			long offset = flagsPointer + (i * TYPE.size());
			long addr = Unsafe.jdk().getAddress(offset + addrOffset); // the _addr field is typed as "null" but is 8 bytes long
			String name = Utils.getString(Unsafe.jdk().getAddress(offset + nameOffset));
			int flags = Unsafe.jdk().getInt(offset + flagsOffset);
			int type = Unsafe.jdk().getInt(offset + typeOffset);
			JvmFlag flag = new JvmFlag(addr, name, flags, type);
			arr[i] = flag;
			FLAGS_MAP.put(name, flag);
		}

		FLAGS = Arrays.stream(arr).filter(f -> f.name() != null).sorted(Comparator.comparing(JvmFlag::name)).toArray(JvmFlag[]::new);
	}

	public static JvmFlag get(String name) {
		return FLAGS_MAP.get(name);
	}
}
