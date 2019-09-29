package com.alexey.digitalhackfinal.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alexey.digitalhackfinal.R
import com.here.android.mpa.search.Address
import kotlinx.android.synthetic.main.item_address.view.*

class AddressAdapter(var listener: OnItemClickListener) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    var list: MutableList<Address> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_address, parent, false)
        return ViewHolder(view)
    }

    fun setAddressList(addresses : MutableList<Address>) {
        this.list = addresses
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }


    interface OnItemClickListener{
        fun onItemClick(item: Address)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(address: Address) {
            itemView.txtAddress.text = address.text
            itemView.layoutItem.setOnClickListener {
                listener.onItemClick(address)
            }
        }
    }
}