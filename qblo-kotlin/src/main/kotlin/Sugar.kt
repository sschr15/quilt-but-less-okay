@file:Suppress("NOTHING_TO_INLINE")

package sschr15.tools.qblo.kt

import sschr15.tools.qblo.BreakingEnums
import sschr15.tools.qblo.MemberAccess
import sschr15.tools.qblo.RawMemoryArray
import sschr15.tools.qblo.internals.AccessAxe
import sschr15.tools.qblo.internals.InternalAsm
import sschr15.tools.qblo.internals.InternalReflectVerifier
import java.lang.reflect.Field
import kotlin.reflect.KClass

public inline fun <reified T : Enum<T>> newEnum(name: String, vararg params: Any?): T =
    BreakingEnums.newInstance(T::class.java, name, *params)

public inline fun <T : Enum<T>> Class<T>.newEnum(name: String, vararg params: Any?): T =
    BreakingEnums.newInstance(this, name, *params)

public inline fun <T : Enum<T>> KClass<T>.newEnum(name: String, vararg params: Any?): T =
    BreakingEnums.newInstance(this.java, name, *params)

public inline fun <T> Field.forceGet(instance: Any? = null): T =
    MemberAccess.getFieldValue(this, instance)

public inline fun Field.forceSet(value: Any?, instance: Any? = null) {
    MemberAccess.setFieldValue(this, instance, value)
}

public inline operator fun RawMemoryArray.set(index: Long, value: Any?) {
    when (value) {
        is Byte -> setByte(index, value)
        is Short -> setShort(index, value)
        is Int -> setInt(index, value)
        is Long -> setLong(index, value)
        is Float -> setFloat(index, value)
        is Double -> setDouble(index, value)
        is Boolean -> setBoolean(index, 0, value)
        is Char -> setShort(index, value.code.toShort())
        else -> setObject(index, value)
    }
}

public inline fun asmInit() {
    InternalAsm.init()
}

public inline fun accessInit() {
    AccessAxe.init()
}

public inline fun reflectInit() {
    InternalReflectVerifier.init()
}