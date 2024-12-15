import com.typesafe.config.ConfigFactory
import database.DatabaseFactory
import database.insertUser
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import vk.network.VkApiService
import kotlin.time.measureTime

suspend fun main() {
    val (accessToken, apiVersion) = loadConfig()
    val vkApiService = VkApiService(accessToken, apiVersion)
    DatabaseFactory.init()

    fetchUsersAndGroupsByCities(
        vkApiService = vkApiService,
        maxCityId = 200,
        userCount = 999,
        maxRequestsPerSecond = 6,
        firstCityId = 140
    )
}

suspend fun fetchUsersAndGroupsByCities(
    vkApiService: VkApiService,
    maxCityId: Int,
    userCount: Int,
    maxRequestsPerSecond: Int,
    firstCityId: Int
) {
    val delayBetweenRequests = 1000L / maxRequestsPerSecond // Задержка между запросами (в миллисекундах)

    coroutineScope {
        for (cityId in firstCityId..maxCityId) {
            val time = measureTime {
                System.currentTimeMillis()
                println("Processing city: $cityId")

                // Запрашиваем пользователей для текущего города
                val users = vkApiService.getUsers(count = userCount, city = cityId)
                delay(delayBetweenRequests) // Задержка между запросами
                println("Get ${users.size} users from city $cityId")

                // Последовательно обрабатываем запросы к группам
                users.forEachIndexed { index, user ->
                    // Ограничиваем запросы по задержке
                    user.activities = vkApiService.getUserGroups(user.id)
                    delay(delayBetweenRequests) // Задержка между запросами
                    println("$index. Get activities for ${user.id}")
                    if (user.activities.isNotEmpty()) {
                        insertUser(user)
                    }
                }
            }
            println(String.format("%02d", time.inWholeSeconds))
            println()
        }
    }
}

fun loadConfig(): Pair<String, String> {
    val config = ConfigFactory.load()
    val accessToken = config.getString("vk.accessToken")
    val apiVersion = config.getString("vk.version")
    return Pair(accessToken, apiVersion)
}
