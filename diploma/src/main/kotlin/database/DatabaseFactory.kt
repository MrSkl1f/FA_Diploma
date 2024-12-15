package database

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:postgresql://localhost:5432/diploma",
            driver = "org.postgresql.Driver",
            user = "dsklifasovskiy",
            password = ""
        )
    }
}