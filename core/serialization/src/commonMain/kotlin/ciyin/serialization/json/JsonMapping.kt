package ciyin.serialization.json

import kotlinx.serialization.json.JsonBuilder as JsonBuilderSource

fun JsonBuilder.map(jsonBuilder: JsonBuilderSource) {
    jsonBuilder.encodeDefaults = encodeDefaults
    jsonBuilder.explicitNulls = explicitNulls
    jsonBuilder.ignoreUnknownKeys = ignoreUnknownKeys
    jsonBuilder.isLenient = isLenient
    jsonBuilder.prettyPrint = prettyPrint
    jsonBuilder.prettyPrintIndent = prettyPrintIndent
    jsonBuilder.coerceInputValues = coerceInputValues
    jsonBuilder.classDiscriminator = classDiscriminator
    jsonBuilder.classDiscriminatorMode = classDiscriminatorMode
    jsonBuilder.useAlternativeNames = useAlternativeNames
    jsonBuilder.namingStrategy = namingStrategy
    jsonBuilder.decodeEnumsCaseInsensitive = decodeEnumsCaseInsensitive
    jsonBuilder.allowTrailingComma = allowTrailingComma
    jsonBuilder.allowComments = allowComments
    jsonBuilder.allowSpecialFloatingPointValues = allowSpecialFloatingPointValues
    jsonBuilder.allowStructuredMapKeys = allowStructuredMapKeys
    jsonBuilder.useArrayPolymorphism = useArrayPolymorphism
    jsonBuilder.serializersModule = serializersModule
}