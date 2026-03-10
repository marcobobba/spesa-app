package com.spesa.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.spesa.app.ui.screens.auth.LoginScreen
import com.spesa.app.ui.screens.auth.RegisterScreen
import com.spesa.app.ui.screens.products.*
import com.spesa.app.ui.screens.recipes.RecipeDetailScreen
import com.spesa.app.ui.screens.recipes.RecipesScreen
import com.spesa.app.ui.screens.shoppinglist.ShoppingListScreen
import com.spesa.app.ui.screens.weeklyplan.WeeklyPlanDetailScreen
import com.spesa.app.ui.screens.weeklyplan.WeeklyPlansScreen
import com.spesa.app.viewmodel.AuthViewModel

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.PRODUCTS, "Prodotti", Icons.Default.Inventory),
    BottomNavItem(Routes.RECIPES, "Ricette", Icons.Default.MenuBook),
    BottomNavItem(Routes.WEEKLY_PLANS, "Piani", Icons.Default.CalendarMonth),
)

@Composable
fun SpesaNavGraph(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn == null) {
        // Splash / loading
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Routes.HOME else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            MainScaffold(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.PRODUCT_CREATE) {
            ProductDetailScreen(productId = null, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId")
            ProductDetailScreen(productId = productId, onBack = { navController.popBackStack() })
        }

        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SUPERMARKETS) {
            SupermarketsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.RECIPE_CREATE) {
            RecipeDetailScreen(recipeId = null, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.RECIPE_DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId")
            RecipeDetailScreen(recipeId = recipeId, onBack = { navController.popBackStack() })
        }

        composable(Routes.WEEKLY_PLAN_CREATE) {
            WeeklyPlanDetailScreen(
                planId = null,
                onBack = { navController.popBackStack() },
                onSuccess = { id ->
                    navController.navigate(Routes.weeklyPlanDetail(id)) {
                        popUpTo(Routes.WEEKLY_PLAN_CREATE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            Routes.WEEKLY_PLAN_DETAIL,
            arguments = listOf(navArgument("planId") { type = NavType.IntType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("planId")
            WeeklyPlanDetailScreen(
                planId = planId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(
            Routes.SHOPPING_LIST,
            arguments = listOf(navArgument("planId") { type = NavType.IntType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("planId") ?: return@composable
            ShoppingListScreen(planId = planId, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun MainScaffold(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            innerNavController.navigate(item.route) {
                                popUpTo(innerNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Routes.PRODUCTS,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    onNavigateToDetail = { navController.navigate(Routes.productDetail(it)) },
                    onNavigateToCreate = { navController.navigate(Routes.PRODUCT_CREATE) },
                    onNavigateToCategories = { navController.navigate(Routes.CATEGORIES) },
                    onNavigateToSupermarkets = { navController.navigate(Routes.SUPERMARKETS) }
                )
            }

            composable(Routes.RECIPES) {
                RecipesScreen(
                    onNavigateToDetail = { navController.navigate(Routes.recipeDetail(it)) },
                    onNavigateToCreate = { navController.navigate(Routes.RECIPE_CREATE) }
                )
            }

            composable(Routes.WEEKLY_PLANS) {
                WeeklyPlansScreen(
                    onNavigateToDetail = { navController.navigate(Routes.weeklyPlanDetail(it)) },
                    onNavigateToCreate = { navController.navigate(Routes.WEEKLY_PLAN_CREATE) },
                    onNavigateToShopping = { navController.navigate(Routes.shoppingList(it)) }
                )
            }
        }
    }
}
