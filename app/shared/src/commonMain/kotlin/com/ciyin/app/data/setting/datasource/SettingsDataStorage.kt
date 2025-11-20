package com.ciyin.app.data.setting.datasource

import ciyin.datastore.DataStorage
import com.ciyin.app.data.setting.model.SettingLocalData
import com.ciyin.app.util.FilePath


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/11/6 14:50
 * @version: 1.0
 */
class SettingsDataStorage : DataStorage<SettingLocalData>(
    defaultData = SettingLocalData(),
    file = FilePath.ConfigDataFile
)
