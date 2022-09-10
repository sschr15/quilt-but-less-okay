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
