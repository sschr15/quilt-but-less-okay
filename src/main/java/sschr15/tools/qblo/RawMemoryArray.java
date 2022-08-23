package sschr15.tools.qblo;

import java.lang.reflect.Field;

/**
 * An array of memory. It doesn't care what you're storing, but it *does* safely
 * check for out-of-bounds accesses.
 */
@SuppressWarnings("unused")
public class RawMemoryArray implements AutoCloseable {
	private final long address;
	public final long size;
	private Object working = new Object();

	private static final long WORKING_OFFSET;
	private static final int OBJECT_ADDRESS_SIZE = Unsafe.jdk().addressSize();

	static {
		try {
			Field field = RawMemoryArray.class.getDeclaredField("working");
			WORKING_OFFSET = Unsafe.jdk().objectFieldOffset(field);
		} catch (Throwable t) {
			throw (Error) Utils.justThrow(t);
		}
	}

	public RawMemoryArray(long size) {
		this.address = Unsafe.jdk().allocateMemory(size);
		this.size = size;
	}

	public boolean getBoolean(long index, byte bit) {
		checkIndex(index, 1);
		return (Unsafe.jdk().getByte(address + index) & (1 << bit)) != 0;
	}

	public void setBoolean(long index, byte bit, boolean value) {
		checkIndex(index, 1);
		byte b = Unsafe.jdk().getByte(address + index);
		if (value) {
			b |= (1 << bit);
		} else {
			b &= ~(1 << bit);
		}
		Unsafe.jdk().putByte(address + index, b);
	}

	public byte getByte(long index) {
		checkIndex(index, 1);
		return Unsafe.jdk().getByte(address + index);
	}

	public void setByte(long index, byte value) {
		checkIndex(index, 1);
		Unsafe.jdk().putByte(address + index, value);
	}

	public short getShort(long index) {
		checkIndex(index, 2);
		return Unsafe.jdk().getShort(address + index);
	}

	public void setShort(long index, short value) {
		checkIndex(index, 2);
		Unsafe.jdk().putShort(address + index, value);
	}

	public int getInt(long index) {
		checkIndex(index, 4);
		return Unsafe.jdk().getInt(address + index);
	}

	public void setInt(long index, int value) {
		checkIndex(index, 4);
		Unsafe.jdk().putInt(address + index, value);
	}

	public long getLong(long index) {
		checkIndex(index, 8);
		return Unsafe.jdk().getLong(address + index);
	}

	public void setLong(long index, long value) {
		checkIndex(index, 8);
		Unsafe.jdk().putLong(address + index, value);
	}

	public float getFloat(long index) {
		checkIndex(index, 4);
		return Unsafe.jdk().getFloat(address + index);
	}

	public void setFloat(long index, float value) {
		checkIndex(index, 4);
		Unsafe.jdk().putFloat(address + index, value);
	}

	public double getDouble(long index) {
		checkIndex(index, 8);
		return Unsafe.jdk().getDouble(address + index);
	}

	public void setDouble(long index, double value) {
		checkIndex(index, 8);
		Unsafe.jdk().putDouble(address + index, value);
	}

	public Object getObject(long index) {
		checkIndex(index, OBJECT_ADDRESS_SIZE);
		if (OBJECT_ADDRESS_SIZE == 4) {
			int ptr = Unsafe.jdk().getInt(address + index);
			Unsafe.jdk().putInt(this, WORKING_OFFSET, ptr);
		} else {
			long ptr = Unsafe.jdk().getLong(address + index);
			Unsafe.jdk().putLong(this, WORKING_OFFSET, ptr);
		}
		return working;
	}

	public void setObject(long index, Object value) {
		checkIndex(index, OBJECT_ADDRESS_SIZE);
		working = value;
		if (OBJECT_ADDRESS_SIZE == 4) {
			int ptr = Unsafe.jdk().getInt(this, WORKING_OFFSET);
			Unsafe.jdk().putInt(address + index, ptr);
		} else {
			long ptr = Unsafe.jdk().getLong(this, WORKING_OFFSET);
			Unsafe.jdk().putLong(address + index, ptr);
		}
	}

	public boolean isValidIndex(long index) {
		try {
			checkIndex(index, 1);
			return true;
		} catch (IndexOutOfBoundsException ignored) {
			return false;
		}
	}

	private void checkIndex(long index, long size) {
		long offset = address - index;
		if (offset < 0 || index >= this.size || index + size > this.size) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public void close() {
		Unsafe.jdk().freeMemory(address);
	}
}
