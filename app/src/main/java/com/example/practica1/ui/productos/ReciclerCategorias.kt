package com.example.practica1.ui.productos

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica1.R
import com.example.practica1.base.dbHelper
import com.example.practica1.ui.categoria.CategoriasFragment
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class ReciclerCategorias : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_recicler_categorias, container, false)

        //Mostrar categorías
        //Toast.makeText(context,"Sincronizando datos", Toast.LENGTH_SHORT).show()
        var categorias = view.findViewById<RecyclerView>(R.id.rvCategorias)

        var urlDatos = "http://192.168.100.27:8000/api/lista_categorias"

        val tipoPeticion = "application/json; charset=utf-8".toMediaType()

        var njson = Gson()

        var datosJsonCat = njson.toJson(CategoriasFragment.datosPeticion("%"))

        var request = Request.Builder().url(urlDatos).post(datosJsonCat.toRequestBody(tipoPeticion))

        val dbHelp = dbHelper(context as Context)
        val dbRead = dbHelp.readableDatabase
        val cursor = dbRead.query(
            dbHelper.FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
            null,             // The array of columns to return (pass null to get all)
            null,              // The columns for the WHERE clause
            null,          // The values for the WHERE clause
            null,             // don't group the rows
            null,              // don't filter by row groups
            null               // The sort order
        )

        var token = ""

        with(cursor) {
            moveToNext()

            token = getString(getColumnIndexOrThrow(dbHelper.FeedReaderContract.FeedEntry.COLUMN_NAME_TOKEN))
        }

        request.addHeader("Accept","application/json")
        request.addHeader("Authorization","Bearer " + token)

        var cliente = OkHttpClient()

        cliente.newCall(request.build()).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                var textoJson = response?.body?.string()

                //print(textoJson)

                val actMain = activity as Activity

                actMain.runOnUiThread{
                    var datosJson = Gson()

                    var clientes = datosJson?.fromJson(textoJson, Array<datosCategoria>::class.java)

                    categorias.adapter = NuevaCategorias(clientes)

                    //Toast.makeText(context,"¡Sincronización completa!", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call, e: IOException) {
                val actMain = activity as Activity

                actMain.runOnUiThread{
                    Toast.makeText(context,"Falló la petición" + e.message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        categorias.layoutManager = LinearLayoutManager(context)
        //

        return view
    }

    class datosCategoria(
        val id: Int?,
        val nomcate: String,
    )
}