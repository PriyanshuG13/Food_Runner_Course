package com.priyanshu.foodrunner.model

import org.json.JSONArray

data class OrderHistory(
    val order_id: Int,
    val restaurant_name: String,
    val total_cost: Int,
    val order_placed_at: String,
    val food_items: JSONArray,
)
