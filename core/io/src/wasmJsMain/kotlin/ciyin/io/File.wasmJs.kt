package ciyin.io

import okio.FileSystem

// Wasm-JS 互操作
@JsFun("() => globalThis.localStorage")
private external fun getLocalStorage(): JsAny

@JsFun("(storage, key) => storage.getItem(key)")
private external fun jsGetItem(storage: JsAny, key: String): JsString?

@JsFun("(storage, key, value) => storage.setItem(key, value)")
private external fun jsSetItem(storage: JsAny, key: String, value: String)

@JsFun("(storage, key) => storage.removeItem(key)")
private external fun jsRemoveItem(storage: JsAny, key: String)

actual val SystemFileSystem: FileSystem = BaseWebFileSystem(
    object : WebStorageAdapter {
        private val storage = getLocalStorage()

        override fun getItem(key: String): String? {
            return jsGetItem(storage, key)?.toString()
        }

        override fun setItem(key: String, value: String) {
            jsSetItem(storage, key, value)
        }

        override fun removeItem(key: String) {
            jsRemoveItem(storage, key)
        }
    }
)