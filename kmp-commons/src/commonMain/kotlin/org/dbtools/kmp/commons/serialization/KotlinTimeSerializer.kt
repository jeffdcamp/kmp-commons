package org.dbtools.kmp.commons.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Workaround serializer for Instant until we can use the built-in one in Kotlin 2.2.xx
 * see https://github.com/Kotlin/kotlinx.serialization/issues/3026#issuecomment-3018700470
 */
@Serializer(forClass = Instant::class)
@Deprecated("This should go away when we can use the built-in Instant serializer in a upcoming Kotlin 2.2.xx release")
object InstantIso8601Serializer : KSerializer<Instant> {
    private val delegate = Instant.serializer()

    override val descriptor: SerialDescriptor get() = delegate.descriptor

    override fun serialize(encoder: Encoder, value: Instant) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Instant {
        return delegate.deserialize(decoder)
    }
}

@Serializer(forClass = Instant::class)
object InstantEpochSerializer: KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("InstantEpochSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.toEpochMilliseconds())
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.fromEpochMilliseconds(decoder.decodeLong())
    }
}
