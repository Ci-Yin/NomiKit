package ciyin.foundation.savedstate

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress(names = ["UNCHECKED_CAST"])
@OptIn(markerClass = [kotlinx.serialization.InternalSerializationApi::class])
actual fun <T : Any> SavedStateHandle.createSavedMutableStateFlow(
    scope: kotlinx.coroutines.CoroutineScope,
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return MutableStateFlow(initialValue)
}