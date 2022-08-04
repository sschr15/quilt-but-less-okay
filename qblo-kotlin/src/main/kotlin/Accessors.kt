@file:Suppress("FunctionName", "JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE") // because there's no --add-exports for kotlin

package sschr15.tools.qblo.kt

import java.lang.invoke.MethodHandles
import sun.misc.Unsafe as SunUnsafe
import jdk.internal.misc.Unsafe as JdkUnsafe
import sschr15.tools.qblo.Unsafe as UnsafeAccessors

public fun Unsafe(): JdkUnsafe = UnsafeAccessors.jdk()
public fun SunUnsafe(): SunUnsafe = UnsafeAccessors.sun()
public fun TrustedLookup(): MethodHandles.Lookup = UnsafeAccessors.lookup()
