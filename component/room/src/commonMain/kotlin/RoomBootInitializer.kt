package ciyin

import ciyin.koin.KoinBootInitializer
import ciyin.room.RoomAutoConfiguration


val RoomBootInitializer: KoinBootInitializer = {
    autoConfigurations(RoomAutoConfiguration)
}


