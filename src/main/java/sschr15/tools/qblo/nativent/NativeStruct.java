package sschr15.tools.qblo.nativent;

import java.util.ArrayList;
import java.util.List;

public class NativeStruct {
	public final String name;
	private final List<Field> fields;

	public NativeStruct(String name) {
		this.name = name;
		this.fields = new ArrayList<>();
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public List<Field> getFields() {
		return List.copyOf(fields);
	}

	public record Field(String name, String type, long offset, boolean isStatic) {}
}
