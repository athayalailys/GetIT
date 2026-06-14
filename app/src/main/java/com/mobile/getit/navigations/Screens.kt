package com.mobile.getit.navigations

sealed class Screens(val route: String) {
    object Splash : Screens("welcome")
    object Login : Screens("login")
    object Register : Screens("register")
    object Home : Screens("home")
    object Wishlist : Screens("wishlist")
    object Settings : Screens("setting")
    object AddProduct : Screens("add_product")
    object ManageProduct : Screens("manage_product")
    object Notification : Screens("notification") // Point 10

    object EditProduct : Screens("edit_product/{productId}") {
        fun createRoute(id: String) = "edit_product/$id"
    }

    object Detail : Screens("detail/{productId}") {
        fun createRoute(id: String) = "detail/$id"
    }

    object Category : Screens("category/{categoryName}/{searchQuery}") {
        fun createRoute(name: String, query: String = "empty") = "category/$name/$query"
    }

    object Profile : Screens("profile/{sellerId}") {
        fun createRoute(id: String) = "profile/$id"
    }
}
