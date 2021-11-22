package com.example.practica1.ui.menu

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.practica1.R
import com.example.practica1.base.dbHelper
import com.example.practica1.ui.menu.CustomView
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class MenuAdapter(val datos: Array<Menu.datosProducto>): RecyclerView.Adapter<CustomView>() {

    var contar: Int = 1;
    private lateinit var sumar: Button
    private lateinit var restar: Button
    private lateinit var resultado: TextView

    override fun getItemCount(): Int {
        return datos.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomView {
        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.opciones_menu, parent,false)
        return CustomView(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomView, position: Int) {

        var finalizarProd = holder?.itemView.findViewById(R.id.btnComprar) as TextView
        var agregarProd = holder?.itemView.findViewById(R.id.btnAgregarCarro) as TextView
        var idProd = holder?.itemView.findViewById(R.id.textIdProducto) as TextView
        var nombreProd = holder?.itemView.findViewById(R.id.txt_nombrePro) as TextView
        var descripcionProd = holder?.itemView.findViewById(R.id.txt_descripcionPro) as TextView
        var precioProd = holder?.itemView.findViewById(R.id.txt_precioPro) as TextView

        //Implementación del contador
        var contar = 1
        var sumar = holder?.itemView.findViewById<Button>(R.id.max)
        var restar = holder?.itemView.findViewById<Button>(R.id.min)
        restar.setEnabled(false)
        var resultado = holder?.itemView.findViewById<TextView>(R.id.res)

        resultado.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if(s.toString().equals("1")){
                    restar.setEnabled(false);
                }else{
                    restar.setEnabled(true);
                }
                if(s.toString().equals("10")){
                    sumar.setEnabled(false);
                }else{
                    sumar.setEnabled(true);
                }
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                if(s.toString().equals("1")){
                    restar.setEnabled(false);
                }else{
                    restar.setEnabled(true);
                }
                if(s.toString().equals("10")){
                    sumar.setEnabled(false);
                }else{
                    sumar.setEnabled(true);
                }
            }
        });
        sumar.setOnClickListener{
            contar++
            resultado.text = "$contar"
        }
        restar.setOnClickListener{
            contar--
            resultado.text = "$contar"
        }
        resultado.text = "$contar"


        //agregar a carrito
        agregarProd.setOnClickListener{

            var url = "http://192.168.100.27:8000/api/guardar_opciones"

            val jSon = Gson()

            val tipoPet = "application/json; charset=utf-8".toMediaType()

            var datosJsonProd = jSon.toJson(
                datosCarritos(
                    idProd.text.toString().toInt(),
                    resultado.text.toString().toInt(),
                )
            )

            var request = Request.Builder().url(url).post(datosJsonProd.toRequestBody(tipoPet))

            val dbHelp = dbHelper(holder?.itemView?.context as Context)
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

            Toast.makeText(holder?.itemView?.context, "Agregado al carrito", Toast.LENGTH_LONG).show()

            var client = OkHttpClient()

            client.newCall(request.build()).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    println("OK")
                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Algo salió mal")
                }
            })
        }

        idProd.text = datos[position].id.toString()
        nombreProd.text = datos[position].nombre
        descripcionProd.text = datos[position].descripcion
        precioProd.text = datos[position].precio

        //comprar ahora
        finalizarProd.setOnClickListener{
            val navController = holder?.itemView?.findNavController()

            var objJson = Gson()

            var datos = objJson.toJson(datos[position])

            val bundle = bundleOf("datosVenta" to datos)

            bundle.putString("datosVenta2", resultado.text.toString())

            navController.navigate(R.id.nav_comprar, bundle)
        }
    }
}

data class datosCarritos(
    val id_producto: Int?,
    val cantidad: Int,
)

class CustomView(varV: View): RecyclerView.ViewHolder(varV){

}