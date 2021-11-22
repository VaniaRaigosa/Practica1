package com.example.practica1.ui.categorias

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.practica1.R
import com.example.practica1.base.dbHelper
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.content.Intent


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private var imageUri: Uri? = null
private const val PICK_IMAGE = 100
/**
 * A simple [Fragment] subclass.
 * Use the [DatosCategoria.newInstance] factory method to
 * create an instance of this fragment.
 */


 class DatosCategoria : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var imagen: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == RESULT_OK -> {
                imageUri = data!!.data
                imagen?.setImageURI(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_datos_categoria, container, false)


        var btnGuardar = view.findViewById<Button>(R.id.btn_guardar)
        var btnSele = view.findViewById<Button>(R.id.selecImagen)
        imagen = view.findViewById(R.id.imagenProducto)
        var nombre = view.findViewById<TextView>(R.id.txt_nombreC)
        var descripcion = view.findViewById<TextView>(R.id.txt_descripcionC)
        var estado = view.findViewById<TextView>(R.id.txt_estadoC)
        var es = view.findViewById<Spinner>(R.id.spCate)

        val opciones = resources.getStringArray(R.array.opcionesArray)

        ArrayAdapter.createFromResource(context as Context,R.array.opcionesArray,android.R.layout.simple_spinner_item).also {
                adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            es.adapter = adapter
        }
        es.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                 estado.text =  opciones[position]
            }
        }

        btnSele.setOnClickListener {
            //startActivity(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/"))
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/"),PICK_IMAGE)
        }

        var objJson = Gson()

        var datosCat = objJson.fromJson(arguments?.getString("datosCategori"),datosCategoria::class.java)


        nombre.text = datosCat?.nomcate
        descripcion.text = datosCat?.descate
        estado.text = datosCat?.estado

        btnGuardar.setOnClickListener{

            if(nombre.text.isNotEmpty() && descripcion.text.isNotEmpty()){
                var url = "http://192.168.100.27:8000/api/guarda_categorias"

                val jSon = Gson()

                val tipoPet = "application/json; charset=utf-8".toMediaType()

                var datosJsonCate = jSon.toJson(datosCategoria(
                    datosCat?.id,
                    nombre.text.toString(),
                    descripcion.text.toString(),
                    estado.text.toString(),
                    imageUri?.path.toString()
                ))

                var request = Request.Builder().url(url).post(datosJsonCate.toRequestBody(tipoPet))

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
                            Toast.makeText(context, "Datos guardados", Toast.LENGTH_LONG).show()

                            nombre.setText(null)
                            descripcion.setText(null)
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
                Snackbar.make(btnGuardar , "Existen campos vacíos", Snackbar.LENGTH_SHORT).show()
            }
        }

        return view
    }

    data class datosCategoria(
        val id: Int?,
        val nomcate: String,
        val descate: String,
        val estado: String,
        val imagen: String,
    )



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DatosCategoria.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DatosCategoria().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}