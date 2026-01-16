package com.ciyin.app.data.core.datasource

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import kotlinx.coroutines.flow.Flow

/*
 * 因为目前需求业务不复杂，所以把 AppDatabase 直接耦合到 `shared` 模块里了
 * 这会有个问题，如果后续不使用Room了，那么需要手动移除这部分代码
 * 如果又抽一层会增加其复杂性, 并且这部分代码具有业务性（实体类具有业务性）暂时不抽离了
 * 后续如需要解耦可以抽离成:
 *  `app:shared:data` -> `app:shared:data-room` / `app:shared:data-sqldelight` -> `app:shared`
 *
 * data 模块定义其实体与查询接口函数，实现由 `app:shared:data-xxx` 模块提供
 *
 */

/**
 * app默认数据库
 * 编译时会自动生成子类实现
 */
@Database(
    entities = [AppEntity::class],
    version = 1,
    autoMigrations = [
//        AutoMigration(from = 1, to = 2)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val appDao: AppDao
}


/**
 * app默认数据库的构造函数
 * 编译时会自动生成 `actual` 的实现
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * 以下是给的默认占位的实体类，否则编译会报错
 * 开发时请先清除 `app/shared/schemas` 下的文件并执行 `clean` 任务
 */

@Entity
data class AppEntity(
    @PrimaryKey
    val id: Long,
)

@Dao
interface AppDao {
    @Insert
    suspend fun insert(appEntity: AppEntity): Long

    @Query("SELECT * FROM AppEntity")
    fun getAll(): Flow<List<AppEntity>>
}
