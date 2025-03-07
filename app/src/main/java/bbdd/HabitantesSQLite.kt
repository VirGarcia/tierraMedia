package bbdd

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.elsda.tierramedia_pac.habitantes.HabitanteTierraMedia

/**
 * Clase HabitantesSQLite para crear el CRUD de la Base de Datos
 *
 * Vamos a recordar que es un CRUD:
 * - C de Create => Crear registro / insertar registro en la BBDD
 * - R de Read => leer registro de la BBDD
 * - U de Update => actualizar un registro en la BBDD
 * - D de Delete => borrar un registro en la BBDD
 */
class HabitantesSQLite(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Nombre y versión de la Base de Datos
    companion object {
        private const val DATABASE_NAME = "tierraMedia.db"
        private const val DATABASE_VERSION = 1
    }

    // Método crear => establece las esctructuras de tablas de la Base de Datos
    override fun onCreate(db: SQLiteDatabase?) {
        // Creamos la tabla habitantes
        val crearTablaHabitantes = """
            CREATE TABLE habitantes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL,
                apellidos TEXT NOT NULL,
                edad INTEGER NOT NULL,
                raza TEXT NOT NULL,
                ubicacion TEXT NOT NULL,
                profesion TEXT NOT NULL,
                festivo TEXT NOT NULL
            )
        """.trimIndent()

        // Creamos la tabla de habitantes
        db?.execSQL(crearTablaHabitantes)

    }

    /**
     * Método para actualizar la versión de la BBDD
     */
    override fun onUpgrade(db: SQLiteDatabase?, viejaVersion: Int, nuevaVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS habitantes")
        onCreate(db)
    }

    /**
     * Inserta un habitante en la BBDD
     * @param habitante datos del nuevo habitante
     * @return el identificador del habitante
     */
    fun insertarHabitante(habitante: HabitanteTierraMedia): Long {
        // TODO completar el método insertar habitante
        val db = getWritableDatabase()
        val values = ContentValues ()
        values.put("nombre", habitante.nombre)
        values.put("apellidos", habitante.apellidos)
        values.put("edad", habitante.edad)
        values.put("raza", habitante.raza)
        values.put("ubicacion", habitante.ubicacion)
        values.put("profesion", habitante.profesion)
        values.put("festivo", habitante.festivo)

        val newIdHab = db.insert("habitantes", null, values)
        db.close()
        return newIdHab
    }

    /**
     * Actualiza los datos de un Habitante
     * @param idHabitante el identificador del habitante
     * @param habitante los datos del habitante
     */
    fun actualizarHabitante(idHabitante: Int, habitante: HabitanteTierraMedia) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("nombre", habitante.nombre)
            put("apellidos", habitante.apellidos)
            put("edad", habitante.edad)
            put("raza", habitante.raza)
            put("ubicacion", habitante.ubicacion)
            put("profesion", habitante.profesion)
            put("festivo", habitante.festivo)
        }

        db.update("habitantes", values, "id = ?", arrayOf(idHabitante.toString()))
        db.close()
    }

    /**
     * Borra un habitante de la BBDD
     * @param idHabitante el identificador del habitante
     */
    fun borrarHabitante(idHabitante: Int) : Int {
        // TODO completar el método borrar habitante
        val db = writableDatabase
        val affectedRows = db.delete("habitantes", "id=?", arrayOf(idHabitante.toString()))
        db.close()
        return affectedRows
    }

    /**
     * Obtenemos el número total de habitantes de la Tierra Media
     * @return el número total de habitantes de la Tierra Media
     */
    fun getNumeroHabitantes(): Int {
        val db = readableDatabase // Accedemos a la BBDD en sólo lectura
        val consulta = "SELECT count(*) as numHabitantes FROM habitantes"
        val cursor = db.rawQuery(consulta, null)

        // Accedemos a los datos de la consulta
        cursor.use {
            if (it.moveToFirst())
                return it.getInt(it.getColumnIndexOrThrow("numHabitantes"))
        }

        // Si no obtenemos el número de habitantes => devuelve -1
        return -1
    }

    /**
     * Obtenemos el número de habitantes de una raza de la Tierra Media
     * @param raza la raza que se quiere revisar
     * @return el número de habitantes de una raza de la Tierra Media
     */
    fun getNumeroHabitantesPorRaza(raza: String): Int {
        // TODO crear el código para obtener el número de habitantes por raza
        val db = readableDatabase
        val consulta = "SELECT count(*) as numHabitantesRaza from habitantes WHERE raza =?"
        val cursor = db.rawQuery(consulta, arrayOf(raza))
        var num = -1
        
        //Accedemos a los datos de la consulta
        cursor.use {
            if (it.moveToFirst()) {
                num = it.getInt(it.getColumnIndexOrThrow("numHabitantesRaza"))
            }
        }
        return num
        
    }

    /**
     * Método para listar los habitantes por raza
     * @param raza la raza escogida para listar
     * @return listado de habitantes por raza
     */
    fun getListadoPorRaza(raza: String): List<HabitanteTierraMedia> {
        val db = readableDatabase // Accedemos a la BBDD en modo lectura
        val listaHabitantes = mutableListOf<HabitanteTierraMedia>()

        // Recorremos los valores de la consulta
        try {
            val consulta = "SELECT * FROM habitantes WHERE raza=? ORDER BY id DESC"
            db.rawQuery(consulta, arrayOf(raza)).use { cursor ->
                // Recorremos los valores de la consulta
                if (cursor.moveToFirst()) {
                    do {
                        // Cogemos los valores de un habitante
                        val nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
                        val apellidos = cursor.getString(cursor.getColumnIndexOrThrow("apellidos"))
                        val edad = cursor.getInt(cursor.getColumnIndexOrThrow("edad"))
                        val razaObtenida = cursor.getString(cursor.getColumnIndexOrThrow("raza"))
                        val ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("ubicacion"))
                        val profesion = cursor.getString(cursor.getColumnIndexOrThrow("profesion"))
                        val festivo = cursor.getString(cursor.getColumnIndexOrThrow("festivo"))
                        // Creamos el objeto habitante con los datos y lo añadimos a la lista
                        val habitante = HabitanteTierraMedia(
                            nombre,
                            apellidos,
                            edad,
                            razaObtenida,
                            ubicacion,
                            profesion,
                            festivo
                        )
                        listaHabitantes.add(habitante)
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            // Lanzar una RuntimeException para no requerir manejo explícito
            throw RuntimeException("Error al obtener el listado por raza: ${e.message}", e)
        } finally {
            // Cerramos la BBDD aquí si no usamos el bloque use{}
            db.close()
        }
        return listaHabitantes
    }

    /**
     * Método para listar los habitantes por profesión
     * @param profesion la raza escogida para listar
     * @return listado de habitantes por profesión
     */
    fun getListadoPorProfesion(profesion: String): List<HabitanteTierraMedia> {
        //TODO crear el listado de habitantes por profesión
        val db = readableDatabase
        val res = db.rawQuery("SELECT * FROM habitantes WHERE profesion = ?", arrayOf(profesion))
        //Creamos la variable listado
        var listado = mutableListOf<HabitanteTierraMedia>()
        // Variables para recorrer los datos
        var nombre: String
        var apellidos: String
        var edad: Int
        var raza: String
        var ubicacion: String
        var profesion: String
        var festivo: String

        //Recorremos la consulta de profesiones que pertenecen a la Profesion Buscada
        while (res.moveToNext()) {
            //Obtenemos los valores de profesiones o de la consulta
            nombre = res.getString(1)
            apellidos = res.getString(2)
            edad = res.getInt(3)
            raza = res.getString(4)
            ubicacion = res.getString(5)
            profesion = res.getString(6)
            festivo = res.getString(7)

            //Creamos el habitante con los datos
            val habitante = HabitanteTierraMedia(
                nombre, apellidos, edad, raza, ubicacion, profesion, festivo
            )
            listado.add(habitante)
        }
        res.close()
        db.close()
        return listado
    }

    /**
     * Método para listar los habitantes por festivo
     * @param festivo  para listar
     * @return listado de habitantes por festivo
     */
    fun getListadoPorFestivo(festivo: String): List<HabitanteTierraMedia> {
        //Listado habitantes por Festivo
        val db = readableDatabase
        val res = db.rawQuery("SELECT * FROM habitantes WHERE festivo = ?", arrayOf(festivo))
        //Creamos la variable listado
        var listado = mutableListOf<HabitanteTierraMedia>()
        // Variables para recorrer los datos
        var nombre: String
        var apellidos: String
        var edad: Int
        var raza: String
        var ubicacion: String
        var profesion: String
        var festivoNom: String

        //Recorremos la consulta de profesiones que pertenecen al Festivo Buscado
        while (res.moveToNext()) {
            //Obtenemos los valores de profesiones o de la consulta
            nombre = res.getString(1)
            apellidos = res.getString(2)
            edad = res.getInt(3)
            raza = res.getString(4)
            ubicacion = res.getString(5)
            profesion = res.getString(6)
            festivoNom = res.getString(7)

            //Creamos el habitante con los datos
            val habitante = HabitanteTierraMedia(
                nombre, apellidos, edad, raza, ubicacion, profesion, festivoNom
            )
            listado.add(habitante)
        }
        res.close()
        db.close()
        return listado
    }
}