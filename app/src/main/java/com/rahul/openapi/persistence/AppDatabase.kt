package com.rahul.openapi.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rahul.openapi.models.AccountProperties
import com.rahul.openapi.models.AuthToken
import com.rahul.openapi.models.BlogPost

@Database(
    entities = [AuthToken::class, AccountProperties::class, BlogPost::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract val authTokenDao: AuthTokenDao

    abstract val accountPropertiesDao: AccountPropertiesDao
    abstract val blogPostDao: BlogPostDao

    companion object {
        const val DATABASE_NAME = "app_db"
    }
}