package com.priyanshu.foodrunner.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.model.Cart
import com.priyanshu.foodrunner.util.ConnectionManager
import com.priyanshu.foodrunner.util.PLACE_ORDER
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CartActivity : AppCompatActivity() {

    lateinit var btnPlaceOrder: Button
    private lateinit var toolbar: Toolbar
    private lateinit var coordinatorCart: CoordinatorLayout
    private lateinit var rlOrderSuccessful: RelativeLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var rlLoading: RelativeLayout
    private lateinit var recyclerCart: RecyclerView
    private lateinit var btnOk: Button
    private lateinit var txtListHeaderValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)
        txtListHeaderValue = findViewById(R.id.txtListHeaderValue)
        coordinatorCart = findViewById(R.id.coordinatorCart)
        rlOrderSuccessful = findViewById(R.id.rlOrderSuccessful)
        progressBar = findViewById(R.id.progressBar)
        rlLoading = findViewById(R.id.rlLoading)
        toolbar = findViewById(R.id.toolbar)
        btnOk = findViewById(R.id.btnOk)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
            finish()
        }

        val order: Cart = intent.getParcelableExtra("order")!!

        txtListHeaderValue.text = order.restaurant_name

        val foodList = JSONArray()
        val food = JSONArray()

        for (foodItem in order.foodList){
            val foodId = JSONObject()
            foodId.put("food_item_id", foodItem[0])
            food.put(foodId)
            val item = JSONObject()
            item.put("itemName", foodItem[1])
            item.put("itemCost", foodItem[2])
            foodList.put(item)
        }

        setUpRecycler(foodList)
        val btnText = "Place Order(Total: Rs. ${order.total_cost})"
        btnPlaceOrder.text = btnText

        val jsonParam = JSONObject()

        jsonParam.put("user_Id", order.user_Id)
        jsonParam.put("restaurant_id", order.restaurant_id)
        jsonParam.put("total_cost", order.total_cost)
        jsonParam.put("food", food)

        println("Cart: ${jsonParam}")

        btnPlaceOrder.setOnClickListener {
            rlLoading.visibility = View.VISIBLE
            setUpVolley(jsonParam)
        }
    }

    private fun setUpRecycler(list: JSONArray) {
        recyclerCart = findViewById(R.id.recyclerCart)

        recyclerCart.layoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = CartItemsAdapter(list, this@CartActivity)
        recyclerCart.setHasFixedSize(true)
    }

    private fun setUpVolley(jsonParam: JSONObject) {
        val queue = Volley.newRequestQueue(this@CartActivity)

        if (ConnectionManager().isNetworkAvailable(this@CartActivity)) {

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.POST,
                PLACE_ORDER,
                jsonParam,
                Response.Listener { response ->
                    rlLoading.visibility = View.GONE
                    val intent = Intent(this@CartActivity, HomeActivity::class.java)

                    try {
                        val data = response.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            coordinatorCart.visibility = View.GONE
                            rlOrderSuccessful.visibility = View.VISIBLE

                            btnOk.setOnClickListener {
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(this@CartActivity as Context,
                                        "Order was not Successfully Placed. Please Try Again",
                                            Toast.LENGTH_LONG)
                                            .show()
                            startActivity(intent)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    Toast.makeText(this@CartActivity as Context, error?.message, Toast.LENGTH_SHORT)
                        .show()
                }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "67b301f68b6810"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        } else {
            val builder = AlertDialog.Builder(this@CartActivity as Context)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(this@CartActivity as Activity)
            }
            builder.create()
            builder.show()
        }
    }

    class CartItemsAdapter(private var Items: JSONArray, val context: Context) :
        RecyclerView.Adapter<CartItemsAdapter.CartItemsViewHolder>() {
        override fun onCreateViewHolder(
            p0: ViewGroup,
            p1: Int
        ): CartItemsViewHolder {
            val itemView = LayoutInflater.from(p0.context)
                .inflate(R.layout.order_item_custom_row, p0, false)

            return CartItemsViewHolder(itemView)
        }

        override fun onBindViewHolder(
            p0: CartItemsViewHolder,
            p1: Int
        ) {
            val ItemObject = Items.getJSONObject(p1)
            p0.itemName.text = ItemObject.getString("itemName")
            p0.itemCost.text = ItemObject.getString("itemCost")
        }

        override fun getItemCount(): Int {
            return Items.length()
        }

        class CartItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName = view.findViewById(R.id.txtOrdItem) as TextView
            val itemCost = view.findViewById(R.id.txtItemCost) as TextView
        }
    }
}