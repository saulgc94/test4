package com.dam.project
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class DataItem(val horaColumn: String)

class TableAdapter(private var dataList: List<DataItem>, private var horasReservadas: List<String> ) :
    RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hour, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.columnText.text = item.horaColumn
        // Asigna datos a más columnas si es necesario

        if (holder.columnText.text as String in horasReservadas){
            val a = holder.columnText.text
            holder.itemView.setBackgroundColor(Color.RED)
        }else{
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = dataList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val columnText: TextView = itemView.findViewById(R.id.textViewHour)
        // Agrega vistas para más columnas si es necesario
    }

    fun updateData(newData: List<DataItem>, newHoras:List<String>) {
        dataList = newData
        horasReservadas = newHoras
        notifyDataSetChanged()
    }
}

