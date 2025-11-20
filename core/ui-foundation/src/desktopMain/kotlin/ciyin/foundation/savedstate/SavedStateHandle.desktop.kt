package ciyin.foundation.savedstate

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.InternalSerializationApi

@Suppress(names = ["UNCHECKED_CAST"])
@OptIn(markerClass = [InternalSerializationApi::class])
actual fun <T : Any> SavedStateHandle.createSavedMutableStateFlow(
    scope: CoroutineScope,
    key: String,
    initialValue: T
): MutableStateFlow<T> {
    return MutableStateFlow(initialValue)
}