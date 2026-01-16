package ciyin.room

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.util.findAndInstantiateDatabaseImpl
import ciyin.io.resolve
import ciyin.platform.Context
import ciyin.platform.DesktopContext
import ciyin.platform.files
import kotlin.reflect.KClass

actual inline fun <reified T : RoomDatabase> Context.createDatabaseBuilder(
    databaseClass: KClass<T>,
    name: String
): RoomDatabase.Builder<T> {
    return Room.databaseBuilder<T>(
        name = files.dataDir.resolve("${name}.db").toString(),
    ) {
        findAndInstantiateDatabaseImpl(databaseClass.java)
    }
}

actual inline fun <reified T : RoomDatabase> Context.createInMemoryDatabaseBuilder(
    databaseClass: KClass<T>
): RoomDatabase.Builder<T> {
    this as DesktopContext
    return Room.inMemoryDatabaseBuilder<T> {
        findAndInstantiateDatabaseImpl(databaseClass.java)
    }
}
