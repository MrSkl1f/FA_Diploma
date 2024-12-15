package vk.network

import com.fasterxml.jackson.databind.ObjectMapper
import vk.models.Group
import vk.models.User
import vk.service.VkHttpClient

class VkApiService(
    private val accessToken: String,
    private val apiVersion: String
) {
    private val objectMapper = ObjectMapper()

    suspend fun getUsers(count: Int, city: Int): List<User> {
        try {
            val response = VkHttpClient.get(
                endpoint = "users.search",
                params = mapOf(
                    "count" to count.toString(),
                    "age_from" to "18",
                    "age_to" to "40",
                    "city" to city.toString(),
                    "fields" to FIELDS,
                ),
                accessToken = accessToken,
                apiVersion = apiVersion
            )
            return filterUsers(parseResponse(response, User::class.java))
        } catch (e: Exception) {
            println("exception, $e")
        }
        return emptyList()
    }

    private fun filterUsers(users: List<User>) =
        users.filter { !it.isClosed }

    suspend fun getUserGroups(userId: Int): List<Group> {
        try {
            val response = VkHttpClient.get(
                endpoint = "groups.get",
                params = mapOf(
                    "user_id" to userId.toString(),
                    "count" to "100",
                    "fields" to GROUP_FIELDS,
                    "extended" to "1"
                ),
                accessToken = accessToken,
                apiVersion = apiVersion
            )

            return filterGroups(parseResponse(response, Group::class.java))
        } catch (e: Exception) {
            println("exception, $e")
        }
        return emptyList()
    }

    private fun filterGroups(groups: List<Group>) =
        groups.filter {
            it.activity != null && !it.activity.contains("Открытая группа") && !it.activity.contains("Юмор")
                    && !it.activity.contains("Закрытая группа")
                    && !it.activity.contains("Этот материал заблокирован на территории РФ")
        }

    private fun <T> parseResponse(response: String, clazz: Class<T>): List<T> {
        val node = objectMapper.readTree(response)
        try {
            val itemsNode = node["response"]["items"]
            if (itemsNode == null || !itemsNode.isArray) {
                return emptyList()
            }

            return itemsNode.map { itemNode ->
                objectMapper.treeToValue(itemNode, clazz)
            }
        } catch (e: Exception) {
            println("exception, $response")
        }
        return emptyList()
    }

    companion object {
        private const val FIELDS =
            "activities,bdate,career,city,country,education,followers_count,counters,has_mobile,has_photo,home_town,military,online,personal,relation,sex,timezone,is_closed"

        private const val GROUP_FIELDS = "id,activity"
    }
}