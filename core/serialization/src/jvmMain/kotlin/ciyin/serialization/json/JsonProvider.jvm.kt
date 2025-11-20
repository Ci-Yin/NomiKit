package ciyin.serialization.json

actual fun JsonCodec(builder: JsonBuilder): JsonCodec = KotlinxJsonCodec(builder)