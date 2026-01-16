package ciyin.platform.context

import ciyin.io.File

data class CommonContextFiles(
    override val cacheDir: File,
    override val dataDir: File,
    override val defaultBaseMediaCacheDir: File
) : ContextFiles