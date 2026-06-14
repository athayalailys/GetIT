package com.mobile.getit.navigations

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.mobile.getit.data.local.PreferenceManager
import com.mobile.getit.navigations.Screens
import com.mobile.getit.ui.screens.*
import com.mobile.getit.ui.viewmodel.ProductViewModel
import com.mobile.getit.ui.viewmodel.ViewModelFactory
import com.mobile.getit.ui.viewmodel.WeatherViewModel
import com.mobile.getit.ui.viewmodel.WishlistViewModel

@Composable
fun SetupNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val factory = ViewModelFactory(context)

    val weatherVm: WeatherViewModel = viewModel(factory = factory)
    val productVm: ProductViewModel = viewModel(factory = factory)
    val wishlistVm: WishlistViewModel = viewModel(factory = factory)

    // Selalu mulai dari Splash untuk validasi session Firebase yang akurat
    val startDestination = Screens.Splash.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screens.Splash.route) { SplashScreen(navController) }
        composable(Screens.Login.route) { LoginScreen(navController) }
        composable(Screens.Register.route) { RegisterScreen(navController) }
        composable(Screens.Home.route) { HomeScreen(navController, weatherVm, productVm) }
        composable(Screens.Wishlist.route) { WishlistScreen(navController, wishlistVm, productVm) }
        composable(Screens.Settings.route) { SettingsScreen(navController) }
        composable(Screens.AddProduct.route) { AddProductScreen(navController, productVm) }
        composable(Screens.ManageProduct.route) { ManageProductScreen(navController, productVm) }
        composable(Screens.Notification.route) { NotificationScreen(navController) }

        composable(
            route = Screens.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductScreen(navController, id, productVm)
        }

        composable(
            route = Screens.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("productId") ?: ""
            DetailScreen(navController, id, productVm, wishlistVm)
        }

        composable(
            route = "payment_confirm/{productId}/{amount}/{method}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "getit://payment/confirm?id={productId}&amount={amount}&method={method}" }
            ),
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType },
                navArgument("method") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("productId") ?: ""
            val method = backStackEntry.arguments?.getString("method") ?: ""
            productVm.updateTransaction(id, isPaid = true, paymentMethod = method)
            
            navController.navigate(Screens.Home.route) {
                // PERBAIKAN: Bersihkan sampai root graph
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }

        composable(
            route = Screens.Category.route,
            arguments = listOf(
                navArgument("categoryName") { type = NavType.StringType },
                navArgument("searchQuery") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val catName = backStackEntry.arguments?.getString("categoryName") ?: "empty"
            val query = backStackEntry.arguments?.getString("searchQuery") ?: "empty"
            CategoryScreen(navController, catName, query, productVm)
        }

        composable(
            route = Screens.Profile.route,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            ProfileScreen(navController, sellerId, productVm)
        }
    }
}
