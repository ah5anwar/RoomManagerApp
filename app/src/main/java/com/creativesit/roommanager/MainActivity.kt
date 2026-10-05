package com.creativesit.roommanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.creativesit.roommanager.ui.navigation.AppNavHost
import com.creativesit.roommanager.ui.theme.RoomManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoomManagerTheme {
                AppNavHost()
            }
        }
    }
}
