package com.ajulabs.composesafeargs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.ajulabs.composesafeargs.ui.theme.ComposeSafeArgsTheme
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeSafeArgsTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = remember(backStackEntry) {
                            Screen.fromRoute(
                                route = backStackEntry?.destination?.route ?: "",
                                args = backStackEntry?.arguments
                            )
                        }
                        val shouldShowNavigationIcon = currentRoute !is Screen.Home

                        TopBar(shouldShowNavigationIcon = shouldShowNavigationIcon) {
                            navController.navigateUp()
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startDestination = Screen.Home
                    ) {
                        composable<Screen.Home> {
                            HomeScreen(navController)
                        }
                        composable<Screen.Detail> { backStackEntry ->
                            val detail = backStackEntry.toRoute<Screen.Detail>()
                            DetailScreen(id = detail.id)
                        }
                    }
                }
            }
        }
    }
}

@Serializable
sealed class Screen {
    companion object {
        fun fromRoute(route: String, args: Bundle?): Screen? {
            val subclass = Screen::class.sealedSubclasses.firstOrNull {
                route.contains(it.qualifiedName.toString())
            }
            return subclass?.let { createInstance(it, args) }
        }

        private fun <T : Any> createInstance(kClass: KClass<T>, bundle: Bundle?): T? {
            val constructor = kClass.primaryConstructor
            return constructor?.let {
                val args = it.parameters.associateWith { param -> bundle?.get(param.name) }
                it.callBy(args)
            } ?: kClass.objectInstance
        }
    }

    @Serializable
    data object Home : Screen()

    @Serializable
    data class Detail(val id: Int) : Screen()
}

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(100) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .clickable {
                        navController.navigate(Screen.Detail(index))
                    }
            ) {
                Text(
                    text = "Item $index",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun DetailScreen(id: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Center
    ) {
        Text(text = "Detail Screen: $id")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(shouldShowNavigationIcon: Boolean, onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = "Compose Safe Args")
        },
        navigationIcon = {
            if (shouldShowNavigationIcon) {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}