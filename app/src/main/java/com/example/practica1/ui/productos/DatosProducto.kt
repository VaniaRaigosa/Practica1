package com.example.practica1.ui.productos

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica1.R
import com.example.practica1.base.dbHelper
import com.example.practica1.ui.categoria.CategoriasAdapter
import com.example.practica1.ui.categoria.CategoriasFragment
import com.example.practica1.ui.categorias.DatosCategoria
import com.example.practica1.ui.ventas.DatosVenta
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DatosProducto.newInstance] factory method to
 * create an instance of this fragment.
 */
class DatosProducto : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_datos_producto, container, false)


        var btnGuardarProd = view.findViewById<Button>(R.id.btn_guardar_prod)

        var category = view.findViewById<TextView>(R.id.txtNomCategory)
        var nombreProdu = view.findViewById<TextView>(R.id.txt_nombreP)
        var descripcionProdu = view.findViewById<TextView>(R.id.txt_descripcionP)
        var precioProdu = view.findViewById<TextView>(R.id.txt_precioP)
        var categorias = view.findViewById<TextView>(R.id.opCategorias)

        categorias.setOnClickListener {
            val navController = view.findNavController()
            navController.navigate(R.id.nav_selec_cate)
        }

        var objJson = Gson()

        var datosProd = objJson.fromJson(arguments?.getString("datosProduct"),
            ProductosFragment.datosProducto::class.java)

        var datosCate = objJson.fromJson(arguments?.getString("Id"),
            ReciclerCategorias.datosCategoria::class.java)


        category.text = datosCate?.nomcate
        nombreProdu.text = datosProd?.nombre
        descripcionProdu.text = datosProd?.descripcion
        precioProdu.text = datosProd?.precio


        btnGuardarProd.setOnClickListener{

            if(category.text.isNotEmpty() && nombreProdu.text.isNotEmpty() && descripcionProdu.text.isNotEmpty() && precioProdu.text.isNotEmpty()){
                var url = "http://192.168.100.27:8000/api/guardar_productos"

                val jSon = Gson()

                val tipoPet = "application/json; charset=utf-8".toMediaType()

                var datosJsonProd = jSon.toJson(datosProducto(
                    datosProd?.id,
                    nombreProdu.text.toString(),
                    descripcionProdu.text.toString(),
                    precioProdu.text.toString(),
                    datosCate?.id,
                ))

                var request = Request.Builder().url(url).post(datosJsonProd.toRequestBody(tipoPet))

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

                var client = OkHttpClient()

                client.newCall(request.build()).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val actMain = activity as Activity

                        actMain.runOnUiThread {
                            Snackbar.make(btnGuardarProd , "Alimento guardado", Snackbar.LENGTH_SHORT).show()

                            category.setText(null)
                            nombreProdu.setText(null)
                            descripcionProdu.setText(null)
                            precioProdu.setText(null)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        val actMain = activity as Activity
                        actMain.runOnUiThread{
                            Toast.makeText(context,"Algo salió mal" + e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            }else{
                Snackbar.make(btnGuardarProd , "Existen campos vacíos", Snackbar.LENGTH_SHORT).show()
            }
            }

        return view

    }

    data class datosProducto(
        val id: Int?,
        val nombre: String,
        val descripcion: String,
        val precio: String,
        val id_categoria: Int?,
    )

    data class datosCategoria(
        val nomcate: String,
    )



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DatosProducto.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DatosProducto().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}