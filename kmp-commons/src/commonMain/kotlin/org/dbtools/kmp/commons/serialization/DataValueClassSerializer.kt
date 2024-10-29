package org.dbtools.kmp.commons.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Because 'value class' do not work well for KMP (not supported on all platforms) data classes can be used instead.
 *
 * These interfaces and classes simplify the support for data classes in Kotlin Serialization
 *
 * Example:
 * @Serializable(with = UserIdSerializer::class)
 * data class UserId(override val value: String): DataValueStringClass
 *
 * object UserIdSerializer : DataClassStringSerializer<UserId>("UserIdSerializer", { UserId(it) })
 */

interface DataValueStringClass {
    val value: String
}

open class DataClassStringSerializer<T : DataValueStringClass>(serialName: String, val createInstance: (String) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeString())
}


interface DataValueIntClass {
    val value: Int
}

open class DataClassIntSerializer<T : DataValueIntClass>(serialName: String, val createInstance: (Int) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeInt(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeInt())
}


interface DataValueLongClass {
    val value: Long
}

open class DataClassLongSerializer<T : DataValueLongClass>(serialName: String, val createInstance: (Long) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeLong(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeLong())
}

interface DataValueFloatClass {
    val value: Float
}

open class DataClassFloatSerializer<T : DataValueFloatClass>(serialName: String, val createInstance: (Float) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeFloat(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeFloat())
}

interface DataValueDoubleClass {
    val value: Double
}

open class DataClassDoubleSerializer<T : DataValueDoubleClass>(serialName: String, val createInstance: (Double) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeDouble(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeDouble())
}

interface DataValueBooleanClass {
    val value: Boolean
}

open class DataClassBooleanSerializer<T : DataValueBooleanClass>(serialName: String, val createInstance: (Boolean) -> T) : KSerializer<T> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: T): Unit = encoder.encodeBoolean(value.value)
    override fun deserialize(decoder: Decoder): T = createInstance(decoder.decodeBoolean())
}
