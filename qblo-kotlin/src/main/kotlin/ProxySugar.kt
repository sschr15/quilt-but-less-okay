/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER")

package sschr15.tools.qblo.kt

import sschr15.tools.qblo.Function
import sschr15.tools.qblo.MegaProxy
import sschr15.tools.qblo.MegaProxy.HasClassBytes
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

public class OverrideBuilder {
    @PublishedApi
    internal val overrides: MutableMap<String, Function> = mutableMapOf()

    @JvmName("overrideZ")
    public fun String.invoke(block: (Array<Any?>) -> Boolean) {
        overrides[this] = Function.Boolean(block)
    }

    @JvmName("overrideB")
    public fun String.invoke(block: (Array<Any?>) -> Byte) {
        overrides[this] = Function.Byte(block)
    }

    @JvmName("overrideC")
    public fun String.invoke(block: (Array<Any?>) -> Char) {
        overrides[this] = Function.Char(block)
    }

    @JvmName("overrideS")
    public fun String.invoke(block: (Array<Any?>) -> Short) {
        overrides[this] = Function.Short(block)
    }

    @JvmName("overrideI")
    public fun String.invoke(block: (Array<Any?>) -> Int) {
        overrides[this] = Function.Int(block)
    }

    @JvmName("overrideJ")
    public fun String.invoke(block: (Array<Any?>) -> Long) {
        overrides[this] = Function.Long(block)
    }

    @JvmName("overrideF")
    public fun String.invoke(block: (Array<Any?>) -> Float) {
        overrides[this] = Function.Float(block)
    }

    @JvmName("overrideD")
    public fun String.invoke(block: (Array<Any?>) -> Double) {
        overrides[this] = Function.Double(block)
    }

    @JvmName("overrideV")
    public fun String.invoke(block: (Array<Any?>) -> Unit) {
        overrides[this] = Function.Void(block)
    }

    @JvmName("override")
    public fun String.invoke(block: (Array<Any?>) -> Any?) {
        overrides[this] = Function.NonPrimitive(block)
    }
}

/**
 * Subclass this class, using the [builder] to override methods.
 */
public inline fun <I, O> Class<I>.override(
    vararg constructorParams: Any?,
    builder: OverrideBuilder.() -> Unit
): O where I : Any, O : I, O : HasClassBytes { // Suppression required to allow O to subclass I and implement HasClassBytes
    val overrides = OverrideBuilder().apply(builder).overrides
    return if (isInterface) {
        Proxy.newProxyInstance(classLoader, arrayOf(this)) { _, method, args ->
            val namePlusDesc = "${method.name}${method.parameterTypes.joinToString("") { it.descriptorString() }}"
            val func = overrides[namePlusDesc] ?: throw IllegalStateException("No override for $namePlusDesc")
            when (func) {
                is Function.Boolean -> func.apply(*args)
                is Function.Byte -> func.apply(*args)
                is Function.Char -> func.apply(*args)
                is Function.Short -> func.apply(*args)
                is Function.Int -> func.apply(*args)
                is Function.Long -> func.apply(*args)
                is Function.Float -> func.apply(*args)
                is Function.Double -> func.apply(*args)
                is Function.Void -> func.apply(*args)
                is Function.NonPrimitive<*> -> func.apply(*args)
                else -> error("Impossible state - `Function` is a sealed interface")
            }
        }
    } else {
        MegaProxy.newSubclass(this, constructorParams, overrides)
    } as O
}

public inline fun <I, O> KClass<I>.override(
    vararg constructorParams: Any?,
    builder: OverrideBuilder.() -> Unit
): O where I : Any, O : I, O : HasClassBytes = java.override(*constructorParams, builder = builder)

public inline fun <reified I, O> override(
    vararg constructorParams: Any?,
    builder: OverrideBuilder.() -> Unit
): O where I : Any, O : I, O : HasClassBytes = I::class.override(*constructorParams, builder = builder)
