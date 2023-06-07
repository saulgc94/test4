package com.dam.project

import android.os.Bundle
import android.view.View
import android.content.Context
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.time.LocalTime
import android.app.AlertDialog
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



class ReservaSalaActivity : AppCompatActivity() {
    //declaración de variables
    private lateinit var database: DatabaseReference
    private lateinit var textHoraIn: TextView
    private lateinit var textHoraOut: TextView
    private lateinit var spinnerSala: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerDay: Spinner
    private lateinit var buttonHoraIn: Button
    private lateinit var buttonHoraOut: Button
    private lateinit var buttonReservar: Button

    private lateinit var salaSelected: String
    private lateinit var yearSelected: String
    private lateinit var monthSelected: String
    private lateinit var daySelected: String

    private var horasReserved:List<String> = emptyList()
    private val dataList = listOf(
        DataItem("10:00"),DataItem("11:00"),
        DataItem("12:00"),DataItem("13:00"),
        DataItem("14:00"),DataItem("15:00"),
        DataItem("16:00"),DataItem("17:00"),
        DataItem("18:00"),DataItem("19:00"), DataItem("20:00"),

        )

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TableAdapter
    private lateinit var user: String

    private val optionsMap: Map<String, List<String>> = mapOf(
        "Enero" to (1..31).map { it.toString() },
        "Febrero" to (1..28).map { it.toString() },
        "Marzo" to (1..31).map { it.toString() },
        "Abril" to (1..30).map { it.toString() },
        "Mayo" to (1..31).map { it.toString() },
        "Junio" to (1..30).map { it.toString() },
        "Julio" to (1..31).map { it.toString() },
        "Agosto" to (1..31).map { it.toString() },
        "Septiembre" to (1..30).map { it.toString() },
        "Octubre" to (1..31).map { it.toString() },
        "Noviembre" to (1..30).map { it.toString() },
        "Diciembre" to (1..31).map { it.toString() },
    )

    //función on create que se lanza junto con la creación de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva_sala)

        // Obtener referencias a las vistas
        textHoraIn = findViewById(R.id.textHoraIn)
        textHoraOut = findViewById(R.id.textHoraOut)
        buttonReservar = findViewById(R.id.reservarButton)
        buttonHoraIn = findViewById(R.id.buttonhoraIn)
        buttonHoraOut = findViewById(R.id.buttonhoraOut)
        spinnerSala = findViewById(R.id.spinnerSala)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerDay = findViewById(R.id.spinnerDay)
        recyclerView = findViewById(R.id.recyclerView_reserva)

        // Obtén una referencia al objeto SharedPreferences
        val sharedPreferences = getSharedPreferences("MySession", Context.MODE_PRIVATE)
        // Obtén el nombre de usuario almacenado en SharedPreferences
        val username = sharedPreferences.getString("username", "")
        // Verifica si el nombre de usuario no está vacío
        if (username != null) {
            if (username.isNotEmpty()) {
                // El nombre de usuario está disponible, puedes utilizarlo
                user = username
            }
        }
        //listeners a los botones para llamar a la función correspondiente
        buttonHoraIn.setOnClickListener{
            selHora("entrada", textHoraIn)
        }
        buttonHoraOut.setOnClickListener{
            selHora("salida", textHoraOut)
        }


    }
    //función para montar el recycler
    private fun cargaRecycler(){
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = TableAdapter(dataList,horasReserved)
        recyclerView.adapter = adapter
    }


    //funcion onstart se lanza cuando se hace visible para el usuario
    override fun onStart() {
        super.onStart()
        spinnersHoy()
        setUpSpinners()


    }

    //funcion onresume se lanza cuando se la act está en primer plano para el usuario
    override fun onResume() {
        super.onResume()
        getReservas()
        cargaRecycler()
        reservar()
    }



    private fun reservar(){
        //listener del botón de reservar
        buttonReservar.setOnClickListener {
            //Comprobación de que los datos de las horas estén completos
            if (textHoraIn.text.isEmpty() || textHoraOut.text.isEmpty()) {
                Toast.makeText(this, "Selecciona hora de entrada y salida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener;
            }
            //Recoger y preparar variables
            val calendar = Calendar.getInstance()
            val diaActual = calendar.get(Calendar.DAY_OF_MONTH)
            val diaActualFormat = String.format("%02d", diaActual)
            val mesActual = calendar.get(Calendar.MONTH) + 1
            val mesActualFormat = String.format("%02d", mesActual)
            val anioActual = calendar.get(Calendar.YEAR)
            val horaActual = calendar.get(Calendar.HOUR_OF_DAY)
            val fechaActual = "$anioActual/$mesActualFormat/$diaActualFormat"

            var salaSelected: String = spinnerSala.selectedItem as String
            salaSelected = salaSelected.lowercase()
            val yearSelected: String = spinnerYear.selectedItem as String
            val monthSelected: String = spinnerMonth.selectedItem as String
            val monthOptions = optionsMap.keys.toList()
            val monthNumSelected = monthOptions.indexOf(monthSelected)+1
            val monthSelectedFormat = String.format("%02d", monthNumSelected)
            var daySelected: String = spinnerDay.selectedItem as String
            daySelected = daySelected.padStart(2, '0')
            val fechaSelected = "$yearSelected/$monthSelectedFormat/$daySelected"

            val horaIn = textHoraIn.text.toString()
            val horaSelected = horaIn.split(":")[0].toInt()
            val horaOut = textHoraOut.text.toString()
            val horasReserv:List<Int> = horasReserved.map { hora -> hora.split(":")[0].toInt() }

            //comprobación de variables para validar la reserva
            if (fechaActual > fechaSelected || (fechaActual == fechaSelected && horaActual >= horaSelected)) {
                Toast.makeText(
                    this,
                    "No se puede realizar reserva en la fecha o hora pasadas",
                    Toast.LENGTH_LONG
                ).show()
            }else if(horaIn >= horaOut || horaIn == "20:00"){
                Toast.makeText(
                    this,
                    "Error en las horas seleccionadas",
                    Toast.LENGTH_LONG
                ).show()

            }else if(horaSelected+1 in horasReserv) {
                Toast.makeText(
                    this,
                    "No se puede reservar en horas no disponibles",
                    Toast.LENGTH_LONG
                ).show()
            }else{
                //Se crea el diccionario a insertar en la bbd
                val diccionario = HashMap<String, String>()
                diccionario["in"] = horaIn
                diccionario["out"] = horaOut
                //Recoger el nodo de bbdd e insertar datos
                database = Firebase.database.getReference("Reservas")
                database.child(salaSelected).child(fechaSelected).child(user).setValue(diccionario)
                //Lanzar toast de información
                val mensaje = "Reserva realizada:, $horaIn, $horaOut"
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
            }


        }


    }

    private fun getReservas(){
        //comprobar que todos los datos tengan valor
        if (salaSelected.isEmpty() || yearSelected.isEmpty() || monthSelected.isEmpty() || daySelected.isEmpty()){
            return
        }
        //preparar variables
        var horasReservadas: MutableList<String> = mutableListOf<String>()
        val fechaSelected = "$yearSelected/$monthSelected/$daySelected"

        //llamar a la bbdd con el nodo Reservas
        database = Firebase.database.getReference("Reservas")
        //filtrar de la bbdd por sala y fecha
        database = database.child(salaSelected).child(fechaSelected)

        //preparar un listener ligado a la función ondatachange para que
        //sea ejecutado cada vez que los datos de la bbdd sean alterados
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                horasReserved = emptyList()
                horasReservadas.clear()
                //al realizar cambios actualizar las variables necesarias para el recycler
                if (dataSnapshot.exists()) {
                    val hashMap = dataSnapshot.value as? HashMap<*, *>
                    if (hashMap != null) {

                        for ((key,value) in hashMap.entries){
                            val reserva:HashMap<*,*> = value as HashMap<*,*>
                            val listaHoras= horasReservadas(reserva["in"]as String,reserva["out"] as String)
                            horasReservadas.addAll(listaHoras)
                        }
                        horasReservadas.sortBy { LocalTime.parse(it) }
                        horasReserved = horasReservadas.toSet().toList()
                        adapter.updateData(dataList,horasReserved)

                    }
                }else{
                    horasReserved = horasReservadas
                    adapter.updateData(dataList,horasReserved)
                }

            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Error", "Error al leer el valor.", error.toException())
            }
        })


    }

    private fun selHora(inOut:String, textView: TextView){
        //Preparamos las variables
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        //instanciamos numberpicker con determinados valores
        val hourPicker = NumberPicker(this)
        hourPicker.minValue = 10
        hourPicker.maxValue = 20
        if (inOut == "salida"){
            if (hour+5 <=20){
                hourPicker.value = hour+5
            }else{
                hourPicker.value = 20
            }
        }else{
            if (hour+1 <=20){
                hourPicker.value = hour+1
            }else{
                hourPicker.value = 20
            }
        }
        //Creación de alert dialog
        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleccionar hora de ${inOut}")
            .setView(hourPicker)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val selectedHour = hourPicker.value
                textView.text = "$selectedHour:00"
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        dialog.show()

    }

    private fun spinnersHoy(){
        //Preparamos las variables
        val calendar = Calendar.getInstance()
        val monthNum = calendar.get(Calendar.MONTH)
        val dayNum = calendar.get(Calendar.DAY_OF_MONTH)-1
        val yearNum = calendar.get(Calendar.YEAR)

        //Asignamos valor por defecto del spinner año con la posición seleccionada
        val yearOptions = resources.getStringArray(R.array.spinner_year_options)
        val yearIndex = yearOptions.indexOf(yearNum.toString())
        val adapterYear = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearOptions)
        adapterYear.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapterYear
        spinnerYear.setSelection(yearIndex)


        val spinnerMonthOptions = optionsMap.keys.toList()
        val adapterMonth = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerMonthOptions)
        adapterMonth.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = adapterMonth

        spinnerMonth.setSelection(monthNum)
        val spinnerMPosition = spinnerMonth.getSelectedItemPosition()
        val selectedOption = spinnerMonthOptions[spinnerMPosition]
        updateSpinnerDay(optionsMap[selectedOption], dayNum)
        spinnerMonth.performItemClick(spinnerMonth.selectedView, monthNum, spinnerMonth.selectedItemId)

    }

    private fun setUpSpinners(){
        //preparación de variables
        val calendar = Calendar.getInstance()
        val monthNum = calendar.get(Calendar.MONTH)+1
        val dayNum = calendar.get(Calendar.DAY_OF_MONTH)
        val yearNum = calendar.get(Calendar.YEAR)

        salaSelected = spinnerSala.selectedItem as String
        salaSelected = salaSelected.lowercase()
        yearSelected = yearNum.toString()
        monthSelected = String.format("%02d", monthNum)
        daySelected = String.format("%02d", dayNum)

        //listeners para los spinners, definiendo que reescriban las variables
        spinnerSala.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                salaSelected = spinnerSala.selectedItem as String
                salaSelected = salaSelected.lowercase()
                getReservas()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                yearSelected = spinnerYear.selectedItem as String
                getReservas()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //Arrayadapter para las opciones del spinner de meses y seleccionar el mes por defecto
        val spinnerMonthOptions = optionsMap.keys.toList()
        //Cuando cambie la selección del spinner de meses recogemos variables y las lanzamos al updatespinnerday
        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                id: Long) {
                var todayNumb: Int? = null
                val monthSel = spinnerMonth.selectedItem as String
                var monthNumSelected = spinnerMonthOptions.indexOf(monthSel)+1
                monthSelected = String.format("%02d", monthNumSelected)
                monthNumSelected = monthNumSelected-1
                if ((monthNum-1) == position){
                    todayNumb = dayNum-1
                }
                val selectedOption = spinnerMonthOptions[position]
                //Se le pasa los valores del optionmap en función del mes, es decir, una lista de días
                updateSpinnerDay(optionsMap[selectedOption], todayNumb)
                getReservas()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        spinnerDay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int,
                                        id: Long) {
                daySelected = spinnerDay.selectedItem as String
                daySelected = daySelected.padStart(2, '0')
                getReservas()

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }


    private fun updateSpinnerDay(options: List<String>?,dayNum:Int? = null) {
       //Arrayadapter para el spinner day y en función de si estamos en el mes actual asiganará el día actual o el primer día
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, options.orEmpty())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDay.adapter = adapter
        if (dayNum != null){
            spinnerDay.setSelection(dayNum)
        }else{
            spinnerDay.setSelection(0)
        }

    }

    //función para formatear las horas entre la hora de entrada y salida en el formato deseado
    fun horasReservadas(horaIn: String, horaOut: String): List<String> {
        val listHoras = mutableListOf<String>()

        val start = LocalTime.parse(horaIn)
        val end = LocalTime.parse(horaOut)

        var horaStart = start
        while (!horaStart.isAfter(end)) {
            listHoras.add(horaStart.toString())
            horaStart = horaStart.plusHours(1)
        }

        return listHoras
    }

}


