package com.elfak.journeyjournal.navigation

sealed class Screens(val route: String) {
    data object SplashScreen: Screens("splash_screen")
    data object WelcomeScreen: Screens("welcome_screen")
    data object InfoScreen: Screens("info_screen")
    data object LoginScreen: Screens("login_screen")
    data object RegisterScreen: Screens("register_screen")
    data object ForgotPasswordScreen: Screens("forgot_password_screen")

    data object MainScreen: Screens("main_screen")
}