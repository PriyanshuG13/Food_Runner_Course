package com.priyanshu.foodrunner.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.activity.CartActivity
import com.priyanshu.foodrunner.model.Menu
import org.json.JSONObject

class RestaurantsMenuAdapter(private var menu: ArrayList<Menu>,
                             val context: Context,
                             val user_id: String, val resName: String,
                             val btnProceedToCart: Button) :
    RecyclerView.Adapter<RestaurantsMenuAdapter.RestaurantsMenuViewHolder>() {

    val cart = arrayListOf<JSONObject>()
    var ttCost: Int = 0
    val jsonCart = JSONObject()

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RestaurantsMenuViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.restaurant_detail_custom_row, p0, false)

        return RestaurantsMenuViewHolder(itemView)
    }

    override fun onBindViewHolder(p0: RestaurantsMenuViewHolder, p1: Int) {
        val menuObject = menu.get(p1)

        p0.itemIndex.text = (p1 + 1).toString()
        p0.itemName.text = menuObject.name
        val cost = "Rs. ${menuObject.itemPrice}"
        p0.itemPrice.text = cost
        val remove = "Remove"
        val add = "add"

        val jsonParam = JSONObject()
        jsonParam.put("food_item_id", menuObject.id)

        if(cart.isNotEmpty() && cart.contains(jsonParam)){
            p0.btnAdd.text = remove
            p0.btnAdd.setBackgroundColor(Color.parseColor("#ffca28"))
        } else{
            p0.btnAdd.text = add
            p0.btnAdd.setBackgroundColor(Color.parseColor("#ff5039"))
        }

        p0.btnAdd.setOnClickListener {
            if(!cart.contains(jsonParam)){
                p0.btnAdd.text = remove
                p0.btnAdd.setBackgroundColor(Color.parseColor("#ffca28"))
                ttCost += menuObject.itemPrice
                cart.add(jsonParam)
            } else{
                p0.btnAdd.text = add
                p0.btnAdd.setBackgroundColor(Color.parseColor("#ff5039"))
                ttCost -= menuObject.itemPrice
                cart.remove(jsonParam)
            }

            if(cart.isNotEmpty()){
                btnProceedToCart.visibility = View.VISIBLE
            } else {
                btnProceedToCart.visibility = View.GONE
            }

            jsonCart.put("user_Id", user_id)
            jsonCart.put("restaurant_id", menuObject.restaurant_id)
            jsonCart.put("total_cost", ttCost)
            jsonCart.put("food", cart)
        }

        btnProceedToCart.setOnClickListener {
            val intent = Intent(context, CartActivity::class.java)

            intent.putExtra("resName", resName)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return menu.size
    }

    class RestaurantsMenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemIndex = view.findViewById(R.id.txtItemIndex) as TextView
        val itemName = view.findViewById(R.id.txtMenuItem) as TextView
        val itemPrice = view.findViewById(R.id.txtItemCost) as TextView
        val btnAdd = view.findViewById(R.id.btnAddToCart) as Button
    }
}