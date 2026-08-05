package com.phytotec.recepcion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.phytotec.recepcion.data.repository.AuthRepository
import com.phytotec.recepcion.ui.navigation.RecepcionNavHost
import com.phytotec.recepcion.ui.theme.RecepcionTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecepcionTheme {
                RecepcionNavHost(authRepository = authRepository)
            }
        }
    }
}
