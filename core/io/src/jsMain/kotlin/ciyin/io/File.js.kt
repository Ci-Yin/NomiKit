package ciyin.io

import kotlinx.browser.window
import okio.FileSystem


actual val SystemFileSystem: FileSystem = BaseWebFileSystem(
    object : WebStorageAdapter {
        private val storage = window.localStorage

        override fun getItem(key: String) = storage.getItem(key)
        override fun setItem(key: String, value: String) = storage.setItem(key, value)
        override fun removeItem(key: String) = storage.removeItem(key)
    }
)
