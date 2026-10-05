package com.creativesit.roommanager.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.creativesit.roommanager.AppContainer
import com.creativesit.roommanager.data.model.User
import com.creativesit.roommanager.ui.common.LoadingBox
import com.creativesit.roommanager.ui.dashboard.DashboardScreen
import com.creativesit.roommanager.ui.login.LoginScreen
import kotlinx.coroutines.launch

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val sessionExpired by AppContainer.sessionManager.sessionExpired.collectAsState()

    // যেকোনো স্ক্রিন থেকে API 401 (টোকেন এক্সপায়ার) পেলে স্বয়ংক্রিয়ভাবে লগইনে ফিরিয়ে দেওয়া হয়
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            AppContainer.authRepository.logout()
            AppContainer.sessionManager.consumeSessionExpiredFlag()
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashCheck(navController)
        }
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(onLogout = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.DASHBOARD) { inclusive = true }
                }
            })
        }
    }
}

/** অ্যাপ চালু হলে আগে থেকে সেভ করা সেশন আছে কিনা চেক করে সরাসরি ড্যাশবোর্ড বা লগইনে পাঠায় */
@Composable
private fun SplashCheck(navController: NavHostController) {
    var checked by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val user: User? = AppContainer.sessionManager.getUserOnce()
        val dest = if (user != null) Routes.DASHBOARD else Routes.LOGIN
        navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
        checked = true
    }
    if (!checked) LoadingBox()
}
