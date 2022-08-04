@file:Suppress("UNCHECKED_CAST")

package sschr15.tools.qblo.kt

import sschr15.tools.qblo.MemberAccess
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

public fun <T> Class<*>.staticField(
    name: String,
    type: Class<T>
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    private val getter = TrustedLookup().findStaticGetter(this@staticField, name, type)
    private val field = this@staticField.getDeclaredField(name)
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter.invokeExact() as T
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        MemberAccess.setFieldValue(field, null, value)
    }
}

public fun <T> Any.field(
    name: String,
    type: Class<T>
): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    private val getter = TrustedLookup().findGetter(this@field::class.java, name, type)
    private val field = this@field::class.java.getDeclaredField(name)
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = getter.invokeExact(this@field) as T
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        MemberAccess.setFieldValue(field, this@field, value)
    }
}

public fun <T> KProperty<T>.unsafe(): ReadWriteProperty<Any?, T> = object : ReadWriteProperty<Any?, T> {
    init {
        if (this@unsafe !is KMutableProperty<T>) {
            requireNotNull(javaField) { "KProperty must be backed by a field to be forcibly mutable" }
        }
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = this@unsafe.call()
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this@unsafe is KMutableProperty<T>) {
            this@unsafe.setter.call(value)
        } else {
            val field = this@unsafe.javaField!!
            MemberAccess.setFieldValue(field, thisRef, value)
        }
    }
}
