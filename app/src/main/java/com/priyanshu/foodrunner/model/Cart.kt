package com.priyanshu.foodrunner.model


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Cart(
    val user_Id: String,
    val restaurant_id: Int,
    val restaurant_name: String,
    val total_cost: Int,
    val foodList: ArrayList<ArrayList<String>>
) : Parcelable
