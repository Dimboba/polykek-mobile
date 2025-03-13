package laz.dimboba.sounddetection.app.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private final val KEY_ACCESS_TOKEN = "acc_token"
    private final val KEY_REFRESH_TOKEN = "ref_token"
    private final val PREF_NAME = "auth_prefs"

    private val _tokenState = MutableStateFlow<TokenState>(TokenState.NoActiveTokens)
    val tokenState: StateFlow<TokenState> = _tokenState

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()

        _tokenState.value = TokenState.ActiveTokens
    }

    fun getAccessToken(): String {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "") ?: ""
    }

    fun getRefreshToken(): String {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, "") ?: ""
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()

        _tokenState.value = TokenState.NoActiveTokens
    }

}

sealed class TokenState{
    object ActiveTokens: TokenState()
    object NoActiveTokens: TokenState()
}