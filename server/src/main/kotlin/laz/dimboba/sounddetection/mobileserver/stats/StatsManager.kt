package laz.dimboba.sounddetection.mobileserver.stats

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import laz.dimboba.sounddetection.mobileserver.record.RecordManager
import laz.dimboba.sounddetection.mobileserver.user.UserManager
import org.springframework.stereotype.Component

@Component
class StatsManager(
    private val recordManager: RecordManager,
    private val userManager: UserManager
) {
    fun getStatsForUser(userId: Long): StatsDto = runBlocking {
        return@runBlocking getStatsForUserSuspend(userId)
    }

    private suspend fun getStatsForUserSuspend(userId: Long): StatsDto = coroutineScope {
        val getUser = async { userManager.getUserById(userId) }
        val getMostPopularNotes = async { recordManager.getMostPopularNotes(userId) }
        val getRecordCount = async { recordManager.getRecordCountForUser(userId) }
        val getLastRecord = async { recordManager.getLastRecordForUser(userId) }
        val getMostPopularOctaves = async { recordManager.getMostPopularOctaves(userId) }

        val user = getUser.await()
        val mostPopular = getMostPopularNotes.await()
        val recordCount = getRecordCount.await()
        val lastRecord = getLastRecord.await()
        val mostPopularOctaves = getMostPopularOctaves.await()

        return@coroutineScope StatsDto(
            user.username,
            user.createdAt,
            recordCount,
            mostPopular,
            lastRecord?.createdAt,
            mostPopularOctaves,
        )
    }

}