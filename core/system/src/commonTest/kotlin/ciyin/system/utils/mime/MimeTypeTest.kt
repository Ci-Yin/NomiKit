package ciyin.system.utils.mime

/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/1 15:00
 */
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MimeTypeTest {

    @BeforeTest
    fun setup() {
        // 清除自定义类型，确保测试独立
        MimeTypeManager.clearCustomTypes()
    }

    @AfterTest
    fun tearDown() {
        MimeTypeManager.clearCustomTypes()
    }

    // ========== 基础功能测试 ==========

    @Test
    fun testGetMime_commonImageTypes() {
        assertEquals("image/jpeg", "photo.jpg".getMime())
        assertEquals("image/jpeg", "photo.jpeg".getMime())
        assertEquals("image/png", "image.png".getMime())
        assertEquals("image/gif", "animation.gif".getMime())
        assertEquals("image/webp", "modern.webp".getMime())
        assertEquals("image/svg+xml", "icon.svg".getMime())
    }

    @Test
    fun testGetMime_commonVideoTypes() {
        assertEquals("video/mp4", "video.mp4".getMime())
        assertEquals("video/x-msvideo", "old.avi".getMime())
        assertEquals("video/x-matroska", "hd.mkv".getMime())
        assertEquals("video/quicktime", "apple.mov".getMime())
        assertEquals("video/webm", "web.webm".getMime())
    }

    @Test
    fun testGetMime_commonAudioTypes() {
        assertEquals("audio/mpeg", "song.mp3".getMime())
        assertEquals("audio/wav", "sound.wav".getMime())
        assertEquals("audio/ogg", "music.ogg".getMime())
        assertEquals("audio/mp4", "track.m4a".getMime())
        assertEquals("audio/flac", "hifi.flac".getMime())
    }

    @Test
    fun testGetMime_documentTypes() {
        assertEquals("application/pdf", "document.pdf".getMime())
        assertEquals("application/msword", "old.doc".getMime())
        assertEquals(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "new.docx".getMime()
        )
        assertEquals("application/vnd.ms-excel", "spreadsheet.xls".getMime())
        assertEquals(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "data.xlsx".getMime()
        )
    }

    @Test
    fun testGetMime_archiveTypes() {
        assertEquals("application/zip", "archive.zip".getMime())
        assertEquals("application/x-rar-compressed", "compressed.rar".getMime())
        assertEquals("application/x-7z-compressed", "packed.7z".getMime())
        assertEquals("application/x-tar", "backup.tar".getMime())
        assertEquals("application/gzip", "compressed.gz".getMime())
    }

    @Test
    fun testGetMime_textTypes() {
        assertEquals("text/plain", "readme.txt".getMime())
        assertEquals("text/html", "page.html".getMime())
        assertEquals("text/css", "style.css".getMime())
        assertEquals("text/javascript", "script.js".getMime())
        assertEquals("application/json", "data.json".getMime())
        assertEquals("text/markdown", "doc.md".getMime())
    }

    // ========== 边界情况测试 ==========

    @Test
    fun testGetMime_unknownExtension() {
        assertEquals("application/octet-stream", "file.unknown".getMime())
        assertEquals("application/octet-stream", "file.xyz123".getMime())
    }

    @Test
    fun testGetMime_customDefaultMime() {
        assertEquals("text/plain", "file.unknown".getMime("text/plain"))
        assertEquals("custom/type", "file.xyz".getMime("custom/type"))
    }

    @Test
    fun testGetMime_noExtension() {
        assertEquals("application/octet-stream", "filename".getMime())
        assertEquals("application/octet-stream", "README".getMime())
    }

    @Test
    fun testGetMime_emptyString() {
        assertEquals("application/octet-stream", "".getMime())
    }

    @Test
    fun testGetMime_onlyDot() {
        assertEquals("application/octet-stream", ".".getMime())
        assertEquals("application/octet-stream", "..".getMime())
    }

    @Test
    fun testGetMime_multipleDots() {
        assertEquals("application/zip", "archive.tar.gz.zip".getMime())
        assertEquals("application/json", "config.backup.json".getMime())
    }

    @Test
    fun testGetMime_caseInsensitive() {
        assertEquals("image/jpeg", "PHOTO.JPG".getMime())
        assertEquals("image/jpeg", "Photo.JpG".getMime())
        assertEquals("image/png", "IMAGE.PNG".getMime())
        assertEquals("video/mp4", "Video.MP4".getMime())
    }

    @Test
    fun testGetMime_withPath() {
        assertEquals("image/jpeg", "/path/to/photo.jpg".getMime())
        assertEquals("video/mp4", "C:\\Users\\user\\video.mp4".getMime())
        assertEquals("application/pdf", "../documents/file.pdf".getMime())
    }

    @Test
    fun testGetMime_specialCharacters() {
        assertEquals("image/jpeg", "my photo (1).jpg".getMime())
        assertEquals("video/mp4", "video-2024.mp4".getMime())
        assertEquals("text/plain", "file_name.txt".getMime())
    }

    // ========== 类型检查测试 ==========

    @Test
    fun testIsImageFile() {
        assertTrue("photo.jpg".isImageFile())
        assertTrue("image.png".isImageFile())
        assertTrue("icon.svg".isImageFile())
        assertTrue("picture.webp".isImageFile())

        assertFalse("video.mp4".isImageFile())
        assertFalse("song.mp3".isImageFile())
        assertFalse("document.pdf".isImageFile())
    }

    @Test
    fun testIsVideoFile() {
        assertTrue("video.mp4".isVideoFile())
        assertTrue("movie.avi".isVideoFile())
        assertTrue("clip.mkv".isVideoFile())
        assertTrue("recording.mov".isVideoFile())

        assertFalse("photo.jpg".isVideoFile())
        assertFalse("song.mp3".isVideoFile())
        assertFalse("document.pdf".isVideoFile())
    }

    @Test
    fun testIsAudioFile() {
        assertTrue("song.mp3".isAudioFile())
        assertTrue("sound.wav".isAudioFile())
        assertTrue("music.ogg".isAudioFile())
        assertTrue("track.m4a".isAudioFile())

        assertFalse("photo.jpg".isAudioFile())
        assertFalse("video.mp4".isAudioFile())
        assertFalse("document.pdf".isAudioFile())
    }

    @Test
    fun testIsDocumentFile() {
        assertTrue("document.pdf".isDocumentFile())
        assertTrue("file.doc".isDocumentFile())
        assertTrue("file.docx".isDocumentFile())
        assertTrue("sheet.xlsx".isDocumentFile())
        assertTrue("text.odt".isDocumentFile())

        assertFalse("photo.jpg".isDocumentFile())
        assertFalse("video.mp4".isDocumentFile())
        assertFalse("readme.txt".isDocumentFile())
    }

    @Test
    fun testIsArchiveFile() {
        assertTrue("archive.zip".isArchiveFile())
        assertTrue("compressed.rar".isArchiveFile())
        assertTrue("packed.7z".isArchiveFile())
        assertTrue("backup.tar".isArchiveFile())
        assertTrue("file.gz".isArchiveFile())

        assertFalse("photo.jpg".isArchiveFile())
        assertFalse("video.mp4".isArchiveFile())
        assertFalse("document.pdf".isArchiveFile())
    }

    // ========== 反向查询测试 ==========

    @Test
    fun testGetExtensionFromMime() {
        assertEquals("jpg", "image/jpeg".getExtensionFromMime())
        assertEquals("png", "image/png".getExtensionFromMime())
        assertEquals("mp4", "video/mp4".getExtensionFromMime())
        assertEquals("mp3", "audio/mpeg".getExtensionFromMime())
        assertEquals("pdf", "application/pdf".getExtensionFromMime())
    }

    @Test
    fun testGetExtensionFromMime_unknownMimeType() {
        assertNull("unknown/type".getExtensionFromMime())
        assertNull("custom/mime".getExtensionFromMime())
    }
}