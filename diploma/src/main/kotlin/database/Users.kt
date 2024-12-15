package database

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val canAccessClosed = bool("can_access_closed")
    val sex = integer("sex")
    val online = bool("online")
    val bdate = varchar("bdate", 10).nullable()
    val city = integer("city")
    val country = integer("country").nullable()
    val hasPhoto = bool("has_photo")
    val hasMobile = bool("has_mobile")
    val followersCount = integer("followers_count")
    val career = bool("career")
    val university = integer("university")
    val faculty = integer("faculty")
    val graduation = integer("graduation")
    val relation = integer("relation")
    val political = integer("political")
    val peopleMain = integer("people_main")
    val lifeMain = integer("life_main")
    val smoking = integer("smoking")
    val alcohol = integer("alcohol")
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val isClosed = bool("is_closed")
    val activities = array<Int>("activities") // Массив активностей сохраняем как JSON или строку
    override val primaryKey = PrimaryKey(id)
}