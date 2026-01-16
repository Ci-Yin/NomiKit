package ciyin.system.storage

import java.nio.file.Path

data class AppDataDirectories(
    val data: Path,
    val cache: Path
)