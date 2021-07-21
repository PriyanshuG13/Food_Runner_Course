package com.priyanshu.foodrunner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.priyanshu.foodrunner.R
import com.priyanshu.foodrunner.model.OrderHistory
import org.json.JSONArray

class OrderHistoryAdapter(private var ordHis: ArrayList<OrderHistory>, val context: Context) :
    RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    private lateinit var orderHistoryItemsAdapter: OrderHistoryItemsAdapter

    override fun onCreateViewHolder(
        p0: ViewGroup,
        p1: Int
    ): OrderHistoryViewHolder {
        val itemView = LayoutInflater.from(p0.context)
            .inflate(R.layout.order_history_custom_row, p0, false)

        return OrderHistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(
        p0: OrderHistoryViewHolder,
        p1: Int
    ) {
        val ordHisObject = ordHis.get(p1)
        p0.resName.text = ordHisObject.restaurant_name
        p0.orderDate.text = ordHisObject.order_placed_at

        orderHistoryItemsAdapter = OrderHistoryItemsAdapter(ordHisObject.food_items, context)
        val mLayoutManager = LinearLayoutManager(context)
        p0.recyclerOrderHistoryItems.layoutManager = mLayoutManager
        p0.recyclerOrderHistoryItems.itemAnimator = DefaultItemAnimator()
        p0.recyclerOrderHistoryItems.adapter = orderHistoryItemsAdapter
        p0.recyclerOrderHistoryItems.setHasFixedSize(true)
    }

    override fun getItemCount(): Int {
        return ordHis.size
    }

    class OrderHistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val resName = view.findViewById(R.id.txtOrderHistoryResName) as TextView
        val orderDate = view.findViewById(R.id.txtOrderDate) as TextView
        val recyclerOrderHistoryItems =
            view.findViewById(R.id.recyclerOrderHistoryItems) as RecyclerView
    }

    class OrderHistoryItemsAdapter(private var ordHisItem: JSONArray, val context: Context) :
        RecyclerView.Adapter<OrderHistoryItemsAdapter.OrderHistoryItemsViewHolder>() {
        override fun onCreateViewHolder(
            p0: ViewGroup,
            p1: Int
        ): OrderHistoryItemsViewHolder {
            val itemView = LayoutInflater.from(p0.context)
                .inflate(R.layout.order_item_custom_row, p0, false)

            return OrderHistoryItemsViewHolder(itemView)
        }

        override fun onBindViewHolder(
            p0: OrderHistoryItemsViewHolder,
            p1: Int
        ) {
            val ordHisItemObject = ordHisItem.getJSONObject(p1)
            p0.itemName.text = ordHisItemObject.getString("name")
            val cost = "Rs. ${ordHisItemObject.getString("cost")}"
            p0.itemCost.text = cost
        }

        override fun getItemCount(): Int {
            return ordHisItem.length()
        }

        class OrderHistoryItemsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemName = view.findViewById(R.id.txtOrdItem) as TextView
            val itemCost = view.findViewById(R.id.txtItemCost) as TextView
        }
    }
}