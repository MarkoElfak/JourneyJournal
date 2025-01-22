package com.elfak.journeyjournal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elfak.journeyjournal.screens.forgot_password.ForgotPasswordScreen
import com.elfak.journeyjournal.screens.login.LoginScreen
import com.elfak.journeyjournal.screens.main.MainScreen
import com.elfak.journeyjournal.screens.register.RegisterScreen
import com.elfak.journeyjournal.screens.splash.SplashScreen
import com.elfak.journeyjournal.screens.welcome.WelcomeScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screens.SplashScreen.route
    ) {
        composable(Screens.SplashScreen.route) {
            SplashScreen(
                navigateToWelcomeScreen = {
                    navController.navigate(Screens.WelcomeScreen.route)
                },
                navigateToMainScreen = {
                    navController.navigate(Screens.MainScreen.route)
                }
            )
        }
        composable(Screens.WelcomeScreen.route) {
            WelcomeScreen(
                navigateToLoginScreen = {
                    navController.navigate(Screens.LoginScreen.route)
                },
                navigateToRegisterScreen = {
                    navController.navigate(Screens.RegisterScreen.route)
                }
            )
        }
        composable(Screens.LoginScreen.route) {
            LoginScreen(
                navigateToForgotPasswordScreen = {
                    navController.navigate(Screens.ForgotPasswordScreen.route)
                },
                navigateToMainScreen = {
                    navController.navigate(Screens.MainScreen.route)
                }
            )
        }
        composable(Screens.ForgotPasswordScreen.route) {
            ForgotPasswordScreen(
                navigateToLoginScreen = {
                    navController.navigate(Screens.LoginScreen.route)
                }
            )
        }
        composable(Screens.RegisterScreen.route) {
            RegisterScreen(
                navigateToLoginScreen = {
                    navController.navigate(Screens.LoginScreen.route)
                }
            )
        }
        composable(Screens.MainScreen.route) {
            MainScreen()
        }
    }
}