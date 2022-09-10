package sschr15.tools.qblo.nativent;

import jdk.internal.loader.BootLoader;
import jdk.internal.loader.NativeLibraries;
import sschr15.tools.qblo.Unsafe;
import sschr15.tools.qblo.Utils;
import sschr15.tools.qblo.internals.Loader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeHackery {
	static {
		// Very first thing to do is guarantee native stuff exists
		Loader.init();

		String libPath = System.getProperty("os.name").toLowerCase().contains("win") ? "bin\\server\\jvm.dll" : "lib/server/libjvm.so";
		File libFile = new File(System.getProperty("java.home"), libPath);
		BootLoader.getNativeLibraries().loadLibrary(NativeLibraries.class, libFile);
	}

	public static final Map<String, NativeStruct> STRUCTS = Map.copyOf(getStructs());
	public static final Map<String, NativeType> TYPES = Map.copyOf(getTypes(STRUCTS));

	public static Map<String, NativeStruct> getStructs() {
		Map<String, NativeStruct> structs = new HashMap<>();

		long current = getSymbol("gHotSpotVMStructs");
		long jumpAmount = getSymbol("gHotSpotVMStructEntryArrayStride");

		long typeNameOffset = structOffsetSymbol("TypeName");
		long fieldNameOffset = structOffsetSymbol("FieldName");
		long typeStringOffset = structOffsetSymbol("TypeString");
		long isStaticOffset = structOffsetSymbol("IsStatic");
		long addressOffset = structOffsetSymbol("Address");
		long offsetOffset = structOffsetSymbol("Offset");

		while (true) {
			String typeName = dereferenceString(current + typeNameOffset);
			String fieldName = dereferenceString(current + fieldNameOffset);
			if (typeName == null || fieldName == null) break;

			String typeString = dereferenceString(current + typeStringOffset);
			boolean isStatic = Unsafe.jdk().getByte(current + isStaticOffset) != 0;
			long offset = Unsafe.jdk().getLong(current + (isStatic ? addressOffset : offsetOffset));

			NativeStruct struct = structs.computeIfAbsent(typeName, k -> new NativeStruct(typeName));
			struct.addField(new NativeStruct.Field(fieldName, typeString, offset, isStatic));

			current += jumpAmount;
		}

		return structs;
	}

	public static Map<String, NativeType> getTypes(Map<String, NativeStruct> structs) {
		Map<String, NativeType> types = new HashMap<>();

		long current = getSymbol("gHotSpotVMTypes");
		long jumpAmount = getSymbol("gHotSpotVMTypeEntryArrayStride");

		long typeNameOffset = typeOffsetSymbol("TypeName");
		long superclassNameOffset = typeOffsetSymbol("SuperclassName");
		long sizeOffset = typeOffsetSymbol("Size");
		long isOopTypeOffset = typeOffsetSymbol("IsOopType");
		long isIntegerTypeOffset = typeOffsetSymbol("IsIntegerType");
		long isUnsignedOffset = typeOffsetSymbol("IsUnsigned");

		while (true) {
			String typeName = dereferenceString(current + typeNameOffset);
			if (typeName == null) break;

			String superclassName = dereferenceString(current + superclassNameOffset);
			int size = Unsafe.jdk().getInt(current + sizeOffset);
			boolean isOopType = Unsafe.jdk().getByte(current + isOopTypeOffset) != 0;
			boolean isIntegerType = Unsafe.jdk().getByte(current + isIntegerTypeOffset) != 0;
			boolean isUnsigned = Unsafe.jdk().getByte(current + isUnsignedOffset) != 0;

			NativeStruct struct = structs.get(typeName);
			Map<String, NativeStruct.Field> fields = new HashMap<>();
			if (struct != null) {
				List<NativeStruct.Field> structFields = struct.getFields();
				for (NativeStruct.Field field : structFields) {
					fields.put(field.name(), field);
				}
			}

			types.put(typeName, new NativeType(typeName, superclassName, size, isOopType, isIntegerType, isUnsigned, fields));

			current += jumpAmount;
		}

		return types;
	}

	public static NativeType getType(String name) {
		return TYPES.get(name);
	}

	private static long getSymbol(String name) {
		return Unsafe.jdk().getLong(Utils.findNative(null, name));
	}

	private static long structOffsetSymbol(String name) {
		return getSymbol("gHotSpotVMStructEntry" + name + "Offset");
	}

	private static long typeOffsetSymbol(String name) {
		return getSymbol("gHotSpotVMTypeEntry" + name + "Offset");
	}

	private static String dereferenceString(long address) {
		return Utils.getString(Unsafe.jdk().getLong(address));
	}
}
