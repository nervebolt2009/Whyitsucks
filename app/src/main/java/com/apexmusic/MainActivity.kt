package com.apexmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.wear.compose.material3.AppScaffold
import com.apexmusic.ui.navigation.AppNavigation
import com.apexmusic.ui.theme.ApexBlack
import com.apexmusic.ui.theme.ApexMusicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApexMusicTheme {
                // AppScaffold: ONE per Activity
                // Keeps TimeText visible across screen transitions (swipe-to-dismiss)
                AppScaffold(modifier = Modifier.background(ApexBlack)) {
                    AppNavigation()
                }
            }
        }
    }
}
