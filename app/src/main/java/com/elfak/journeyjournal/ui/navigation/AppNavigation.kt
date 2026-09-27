package com.elfak.journeyjournal.ui.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.elfak.journeyjournal.di.ServiceLocator
import com.elfak.journeyjournal.location.LocationService
import com.elfak.journeyjournal.ui.auth.LoginScreen
import com.elfak.journeyjournal.ui.auth.RegisterScreen
import com.elfak.journeyjournal.ui.leaderboard.LeaderboardScreen
import com.elfak.journeyjournal.ui.map.MapScreen
import com.elfak.journeyjournal.ui.places.AddPlaceScreen
import com.elfak.journeyjournal.ui.places.PlaceDetailScreen
import com.elfak.journeyjournal.ui.places.PlaceListScreen
import com.elfak.journeyjournal.ui.profile.ProfileScreen
import com.elfak.journeyjournal.util.Permissions

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAP = "map"
    const val LIST = "list"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"
    const val ADD_PLACE = "add_place"
    const val PLACE_DETAIL = "place/{placeId}"

    fun placeDetail(placeId: String) = "place/$placeId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.MAP, "Mapa", Icons.Filled.Map),
    Tab(Routes.LIST, "Tabela", Icons.AutoMirrored.Filled.List),
    Tab(Routes.LEADERBOARD, "Rang", Icons.Filled.EmojiEvents),
    Tab(Routes.PROFILE, "Profil", Icons.Filled.Person),
)

/** Chooses between the authentication flow and the application itself. */
@Composable
fun AppRoot(pendingPlaceId: String?, onPlaceIdHandled: () -> Unit) {
    val uid by ServiceLocator.authRepository.authState()
        .collectAsStateWithLifecycle(initialValue = ServiceLocator.auth.currentUser?.uid)

    if (uid == null) {
        AuthNavigation()
    } else {
        MainScreen(pendingPlaceId = pendingPlaceId, onPlaceIdHandled = onPlaceIdHandled)
    }
}

@Composable
private fun AuthNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onRegisterClick = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainScreen(
    pendingPlaceId: String?,
    onPlaceIdHandled: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val tracking by ServiceLocator.locationRepository.tracking.collectAsStateWithLifecycle()
    var hasLocationPermission by remember { mutableStateOf(Permissions.hasLocation(context)) }
    var wantsTracking by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasLocationPermission = Permissions.hasLocation(context)
        if (hasLocationPermission && wantsTracking) LocationService.start(context)
    }

    // Location tracking runs the whole time the user is logged in (requirement 2).
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            LocationService.start(context)
        } else {
            permissionLauncher.launch(Permissions.locationAndNotifications)
        }
    }

    LaunchedEffect(pendingPlaceId) {
        pendingPlaceId?.let {
            navController.navigate(Routes.placeDetail(it))
            onPlaceIdHandled()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = tabs.any { it.route == currentRoute }
    val systemBars = WindowInsets.systemBars.asPaddingValues()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.MAP) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val tabPadding = PaddingValues(
            top = systemBars.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding(),
        )

        NavHost(
            navController = navController,
            startDestination = Routes.MAP,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.MAP) {
                MapScreen(
                    hasLocationPermission = hasLocationPermission,
                    onPlaceClick = { navController.navigate(Routes.placeDetail(it)) },
                    onAddPlace = { navController.navigate(Routes.ADD_PLACE) },
                    contentPadding = tabPadding,
                )
            }
            composable(Routes.LIST) {
                PlaceListScreen(
                    onPlaceClick = { navController.navigate(Routes.placeDetail(it)) },
                    contentPadding = tabPadding,
                )
            }
            composable(Routes.LEADERBOARD) {
                LeaderboardScreen(contentPadding = tabPadding)
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    contentPadding = tabPadding,
                    trackingEnabled = tracking,
                    onTrackingChange = { enabled ->
                        wantsTracking = enabled
                        when {
                            !enabled -> LocationService.stop(context)
                            hasLocationPermission -> LocationService.start(context)
                            else -> permissionLauncher.launch(Permissions.locationAndNotifications)
                        }
                    },
                )
            }
            composable(Routes.ADD_PLACE) {
                AddPlaceScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { placeId ->
                        navController.popBackStack()
                        navController.navigate(Routes.placeDetail(placeId))
                    },
                )
            }
            composable(Routes.PLACE_DETAIL) { entry ->
                val placeId = entry.arguments?.getString("placeId").orEmpty()
                PlaceDetailScreen(
                    placeId = placeId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
