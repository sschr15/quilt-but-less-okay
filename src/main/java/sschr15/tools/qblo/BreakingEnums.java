package sschr15.tools.qblo;

import jdk.internal.reflect.ConstructorAccessor;
import sschr15.tools.qblo.internals.InternalReflectVerifier;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Crack enums right open for *all the modification*
 */
@SuppressWarnings("unchecked")
public class BreakingEnums {
	private static final MethodHandle acquireConstructorAccessor;
	private static final MethodHandle getConstructorAccessor;

	private static final Map<Class<? extends Enum<?>>, String> valueFieldCache = new HashMap<>();

	static {
		InternalReflectVerifier.init(); // Require jdk.internal.reflect to be exported
		try {
			acquireConstructorAccessor = Unsafe.lookup().findVirtual(Constructor.class, "acquireConstructorAccessor", MethodType.methodType(ConstructorAccessor.class));
			getConstructorAccessor = Unsafe.lookup().findVirtual(Constructor.class, "getConstructorAccessor", MethodType.methodType(ConstructorAccessor.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			Unsafe.sun().throwException(e);
			throw new RuntimeException(e);
		}
	}

	public static <T extends Enum<T>> T newInstance(Class<T> clazz, String name, Object... args) {
		try {
			Constructor<T> constructor = (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors()).filter(c -> c.getParameterCount() - 2 == args.length).findFirst().orElseThrow();
			var accessor = (ConstructorAccessor) getConstructorAccessor.invokeExact(constructor);
			if (accessor == null) {
				accessor = (ConstructorAccessor) acquireConstructorAccessor.invokeExact(constructor);
			}

			String valueField = valueFieldCache.computeIfAbsent(clazz, c -> {
				// ok so the "standard" is it being a private (synthetic) static final field
				// so if we look for that, we should find it in most cases
				Field[] fields = c.getDeclaredFields();
				int modifiers = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL | 1 << 12 /* SYNTHETIC */;
				for (Field f : fields) {
					//noinspection MagicConstant
					if (f.getModifiers() == modifiers && f.getType().isArray() && f.getType().getComponentType().equals(clazz)) {
						return f.getName(); // found it
					}
				}
				// There's another method which requires actual bytecode inspection which is a no-thanks when trying to make a lightweight library
				return null;
			});
			if (valueField == null) {
				throw new IllegalArgumentException("No found values field found for enum " + clazz.getName());
			}

			MethodHandle getter = Unsafe.lookup().findStaticGetter(clazz, valueField, clazz.arrayType());
			MethodHandle setter = Unsafe.lookup().findStaticSetter(clazz, valueField, clazz.arrayType());

			// and it's *finally* time to make the new instance
			Object[] allArgs = new Object[args.length + 2];
			allArgs[0] = name;
			allArgs[1] = args;
			System.arraycopy(args, 0, allArgs, 2, args.length);
			T instance = (T) accessor.newInstance(allArgs);

			// and the last thing is to update the values array
			// we can't use `invokeExact` because we're working with generics
			T[] values = (T[]) getter.invoke();
			T[] newValues = Arrays.copyOf(values, values.length + 1);
			newValues[values.length] = instance;
			setter.invoke((Object) newValues); // cast to enforce non-varargs

			return instance;
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
			throw new RuntimeException(t);
		}
	}
}
