package sschr15.tools.qblo;

import org.jetbrains.annotations.Contract;

public class Utils {
	@Contract(" -> null")
	public static <T> T getNull() {
		return null;
	}

	@Contract("_ -> fail")
	public static <T> T justThrow(Throwable t) {
		if (t == null) t = new NullPointerException();
		Unsafe.sun().throwException(t);
		throw new Error();
	}
}
