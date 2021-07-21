package com.priyanshu.foodrunner.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.adapter.OrderHistoryAdapter
import com.priyanshu.foodrunner.model.OrderHistory
import com.priyanshu.foodrunner.util.ConnectionManager
import com.priyanshu.foodrunner.util.FETCH_PREVIOUS_ORDERS
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.HashMap

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerOrderHistory: RecyclerView
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private var orderHistoryList = arrayListOf<OrderHistory>()
    private lateinit var progressBar: ProgressBar
    private lateinit var rlLoading: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var rlOrdHis: RelativeLayout
    private lateinit var rlNoOrdHis: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_order_history, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        rlOrdHis = view.findViewById(R.id.rlOrderHistory)
        rlNoOrdHis = view.findViewById(R.id.rlNoOrderHistory)
        progressBar = view?.findViewById(R.id.progressBar) as ProgressBar
        rlLoading = view.findViewById(R.id.rlLoading) as RelativeLayout
        rlLoading.visibility = View.VISIBLE


        setUpRecycler(view)

        return view
    }

    private fun setUpRecycler(view: View) {
        recyclerOrderHistory = view.findViewById(R.id.recyclerOrderHistory) as RecyclerView
        val userId = sharedPreferences.getString("user_id", "1")

        val queue = Volley.newRequestQueue(activity as Context)

        if (ConnectionManager().isNetworkAvailable(activity as Context)) {

            val jsonObjectRequest = object : JsonObjectRequest(
                Request.Method.GET,
                "$FETCH_PREVIOUS_ORDERS$userId",
                null,
                Response.Listener<JSONObject> { response ->
                    rlLoading.visibility = View.GONE

                    try {
                        val data = response.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val resObject = resArray.getJSONObject(i)
                                val orderHistory = OrderHistory(
                                    resObject.getString("order_id").toInt(),
                                    resObject.getString("restaurant_name"),
                                    resObject.getString("total_cost").toInt(),
                                    resObject.getString("order_placed_at"),
                                    resObject.getJSONArray("food_items")
                                )
                                orderHistoryList.add(orderHistory)
                                if (activity != null) {
                                    orderHistoryAdapter =
                                        OrderHistoryAdapter(orderHistoryList, activity as Context)
                                    val mLayoutManager = LinearLayoutManager(activity)
                                    recyclerOrderHistory.layoutManager = mLayoutManager
                                    recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                    recyclerOrderHistory.adapter = orderHistoryAdapter
                                    recyclerOrderHistory.setHasFixedSize(true)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    rlLoading.visibility = View.GONE
                    rlOrdHis.visibility = View.GONE
                    rlNoOrdHis.visibility = View.VISIBLE
//                    Toast.makeText(activity as Context, error?.message, Toast.LENGTH_SHORT).show()
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
            val builder = AlertDialog.Builder(activity as Context)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            builder.create()
            builder.show()
        }

    }
}