package database

import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import vk.models.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

fun insertUser(user: User) {
    transaction {
        Users.batchInsert(data = listOf(user), ignore = true) { user ->
            this[Users.id] = user.id
            this[Users.canAccessClosed] = user.canAccessClosed
            this[Users.sex] = user.sex
            this[Users.online] = user.online
            this[Users.bdate] = user.bdate
            this[Users.city] = user.city.id
            this[Users.country] = user.country?.id
            this[Users.hasPhoto] = user.hasPhoto
            this[Users.hasMobile] = user.hasMobile
            this[Users.followersCount] = user.followersCount
            this[Users.career] = !user.career.isNullOrEmpty()
            this[Users.university] = user.university
            this[Users.faculty] = user.faculty
            this[Users.graduation] = user.graduation
            this[Users.relation] = user.relation
            this[Users.political] = user.personal?.political ?: 0
            this[Users.peopleMain] = user.personal?.peopleMain ?: 0
            this[Users.lifeMain] = user.personal?.lifeMain ?: 0
            this[Users.smoking] = user.personal?.smoking ?: 0
            this[Users.alcohol] = user.personal?.alcohol ?: 0
            this[Users.firstName] = user.firstName
            this[Users.lastName] = user.lastName
            this[Users.isClosed] = user.isClosed
            this[Users.activities] = user.activities.map { it.id }
        }
    }
}

fun readUsers(): MutableList<MutableList<Double>> {
    return transaction {
        Users.selectAll().map { row ->
            mutableListOf(
                row[Users.sex].toDouble(),
                if (row[Users.online]) 1.0 else 0.0,
                row[Users.city].toDouble(),
                row[Users.country]?.toDouble() ?: 0.0,
                if (row[Users.hasPhoto]) 1.0 else 0.0,
                if (row[Users.hasMobile]) 1.0 else 0.0,
                row[Users.followersCount].toDouble(),
                if (row[Users.career]) 1.0 else 0.0,
                row[Users.university].toDouble(),
                row[Users.faculty].toDouble(),
                row[Users.graduation].toDouble(),
                row[Users.relation].toDouble(),
                row[Users.political].toDouble(),
                row[Users.peopleMain].toDouble(),
                row[Users.lifeMain].toDouble(),
                row[Users.smoking].toDouble(),
                row[Users.alcohol].toDouble(),
                calculateAge(row[Users.bdate]), // Возраст
                mostFrequentActivity(row[Users.activities]) // Самая частая активность
            )
        }
            .toMutableList()
    }
}

fun readUsersWithIndexes(indexes: List<Int>): MutableList<MutableList<Double>> {
    return transaction {
        Users.selectAll().filterIndexed { index, _ -> indexes.contains(index) }.map { row ->
            mutableListOf(
                row[Users.sex].toDouble(),
                if (row[Users.online]) 1.0 else 0.0,
                row[Users.city].toDouble(),
                row[Users.country]?.toDouble() ?: 0.0,
                if (row[Users.hasPhoto]) 1.0 else 0.0,
                if (row[Users.hasMobile]) 1.0 else 0.0,
                row[Users.followersCount].toDouble(),
                if (row[Users.career]) 1.0 else 0.0,
                row[Users.university].toDouble(),
                row[Users.faculty].toDouble(),
                row[Users.graduation].toDouble(),
                row[Users.relation].toDouble(),
                row[Users.political].toDouble(),
                row[Users.peopleMain].toDouble(),
                row[Users.lifeMain].toDouble(),
                row[Users.smoking].toDouble(),
                row[Users.alcohol].toDouble(),
                calculateAge(row[Users.bdate]), // Возраст
                mostFrequentActivity(row[Users.activities]) // Самая частая активность
            )
        }
            .toMutableList()
    }
}


// Метод для вычисления возраста
fun calculateAge(bdate: String?): Double {
    if (bdate.isNullOrBlank()) return 0.0
    return try {
        val formatter = DateTimeFormatter.ofPattern("d.M.yyyy")
        val birthDate = LocalDate.parse(bdate, formatter)
        val currentDate = LocalDate.now()
        ChronoUnit.YEARS.between(birthDate, currentDate).toDouble()
    } catch (e: Exception) {
        0.0 // Возраст равен 0, если дата некорректна
    }
}

// Метод для выбора наиболее часто встречающейся активности
fun mostFrequentActivity(activities: List<Int>?): Double {
    if (activities.isNullOrEmpty()) return 0.0
    val mostFrequent = activities
        .groupBy { it }  // Группируем по значениям
        .maxByOrNull { it.value.size }  // Находим самое частое значение
        ?.key?.toDouble() ?: 0.0 // Возвращаем ключ (частое значение)
    return (mostFrequent / 1000000).roundToInt().toDouble()
}

fun main() {
    DatabaseFactory.init()
    readUsers().forEach { aboba -> println(aboba) }
}
