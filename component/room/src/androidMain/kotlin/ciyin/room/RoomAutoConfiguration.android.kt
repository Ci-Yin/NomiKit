package ciyin.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.reflect.KClass

actual inline fun <reified T : RoomDatabase> Context.createDatabaseBuilder(
    databaseClass: KClass<T>,
    name: String
): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(
        context = applicationContext,
        klass = databaseClass.java,
        name = applicationContext.getDatabasePath("${name}.db").absolutePath,
    )
}

actual inline fun <reified T : RoomDatabase> Context.createInMemoryDatabaseBuilder(
    databaseClass: KClass<T>
): RoomDatabase.Builder<T> = Room.inMemoryDatabaseBuilder(
    context = applicationContext,
    klass = T::class.java,
)