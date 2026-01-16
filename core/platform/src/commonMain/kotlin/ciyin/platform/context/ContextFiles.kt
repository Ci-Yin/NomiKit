package ciyin.platform.context

import ciyin.io.File

interface ContextFiles {

    /**
     * cacheDir on Android.
     */
    val cacheDir: File

    /**
     * filesDir on Android.
     */
    val dataDir: File

    /**
     * Base directory of media cache downloads.
     *
     * * Android: external private storage or internal private if external is unavailable.
     * * Desktop: [dataDir]`/media-downloads` by default, can be changed by settings.
     * * iOS: [dataDir]
     */
    val defaultBaseMediaCacheDir: File

}

