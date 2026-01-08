package ciyin.ui.foundation.savedstate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.serialization.saved
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

@Suppress(names = ["UNCHECKED_CAST"])
@OptIn(markerClass = [InternalSerializationApi::class])
actual fun <T : Any> SavedStateHandle.createSavedMutableStateFlow(
    scope: CoroutineScope,
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    var value by saved(
        serializer = initialValue::class.serializer() as KSerializer<T>,
        key = key,
        init = { initialValue }
    )
    return MutableStateFlow(value).apply {
        onEach { value = it }.launchIn(scope)
    }
}
