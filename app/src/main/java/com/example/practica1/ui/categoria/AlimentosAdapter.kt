package com.example.practica1.ui.categoria

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica1.R
import com.example.practica1.base.dbHelper
import com.example.practica1.ui.menu.Menu
import com.example.practica1.ui.menu.MenuAdapter
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AlimentosAdapter (val datos:Array<MostrarCategorias.datosCategorias>): RecyclerView.Adapter<CustomView2>() {

    override fun getItemCount(): Int {
        return datos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomView2 {
        var layoutInflater = LayoutInflater.from(parent?.context)
        var cellForRow = layoutInflater.inflate(R.layout.opciones_alimentos, parent, false)
        return CustomView2(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomView2, position: Int) {

        var nombre = holder?.itemView.findViewById(R.id.textNombre) as TextView
        var card = holder?.itemView.findViewById<CardView>(R.id.card)

        nombre.text = datos[position].nomcate

        card.setOnClickListener {
            val navController = holder?.itemView?.findNavController()

            var objJson = Gson()

            var datos = objJson.toJson(datos[position])

            val bundle = bundleOf("Id" to datos)

            navController.navigate(R.id.nav_opciones, bundle)
        }
    }
}

class CustomView2(varV: View):RecyclerView.ViewHolder(varV){

}