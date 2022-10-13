package sschr15.tools.qblo.kt

import sschr15.tools.qblo.TrustedLookup
import sschr15.tools.qblo.annotations.JvmClass

public fun classOf(name: @JvmClass String): Class<*> = TrustedLookup.getClass(name)
