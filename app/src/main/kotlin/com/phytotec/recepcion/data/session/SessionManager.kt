package com.phytotec.recepcion.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "session")

/** Stores the API token issued by POST /api/login. */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("api_token")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLES = stringPreferencesKey("user_roles")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }
    val userNameFlow: Flow<String?> = context.dataStore.data.map { it[Keys.USER_NAME] }
    val userRolesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_ROLES]
            ?.split('|')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    suspend fun currentToken(): String? = tokenFlow.first()

    suspend fun saveSession(token: String, userName: String, roles: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USER_NAME] = userName
            prefs[Keys.USER_ROLES] = roles.joinToString("|")
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
