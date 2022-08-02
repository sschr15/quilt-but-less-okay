package sschr15.tools.qblo;

import java.lang.invoke.MethodHandle;

public class BannedFields {
	@SuppressWarnings("removal")
	public static void setSecurity(SecurityManager security) {
		try {
			MethodHandle securitySetter = Unsafe.lookup().findStaticSetter(System.class, "security", SecurityManager.class);
			securitySetter.invokeExact(security);
		} catch (Throwable t) {
			Unsafe.sun().throwException(t);
		}
	}
}
