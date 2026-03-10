package com.spesa.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val PRODUCT_CREATE = "product_create"
    const val CATEGORIES = "categories"
    const val SUPERMARKETS = "supermarkets"
    const val RECIPES = "recipes"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val RECIPE_CREATE = "recipe_create"
    const val WEEKLY_PLANS = "weekly_plans"
    const val WEEKLY_PLAN_DETAIL = "weekly_plan_detail/{planId}"
    const val WEEKLY_PLAN_CREATE = "weekly_plan_create"
    const val SHOPPING_LIST = "shopping_list/{planId}"

    fun productDetail(id: Int) = "product_detail/$id"
    fun recipeDetail(id: Int) = "recipe_detail/$id"
    fun weeklyPlanDetail(id: Int) = "weekly_plan_detail/$id"
    fun shoppingList(planId: Int) = "shopping_list/$planId"
}
