package com.ciyin.app.ui.screen.medialibrary

import ciyin.media.library.MediaCollection
import kotlin.test.Test
import kotlin.test.assertEquals

/** 四类内置媒体测试定义测试。 */
class MediaLibraryDemoModelTest {
    /** 测试定义应完整覆盖四类 Collection 且元数据互不重复。 */
    @Test
    fun samplesCoverEveryCollectionWithUniqueMetadata() {
        assertEquals(MediaLibraryDemoSampleId.entries, mediaLibraryDemoSamples.map { sample -> sample.id })
        assertEquals(MediaCollection.entries, mediaLibraryDemoSamples.map { sample -> sample.collection })
        assertEquals(4, mediaLibraryDemoSamples.map { sample -> sample.resourcePath }.distinct().size)
        assertEquals(4, mediaLibraryDemoSamples.map { sample -> sample.sourceFileName }.distinct().size)
        assertEquals(4, mediaLibraryDemoSamples.map { sample -> sample.mimeType }.distinct().size)
    }
}
