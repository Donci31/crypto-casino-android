package hu.bme.aut.cryptocasino.data.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(
        out: JsonWriter,
        value: LocalDateTime?,
    ) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(formatter.format(value))
    }

    override fun read(reader: JsonReader): LocalDateTime? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        val dateString = reader.nextString()
        return try {
            LocalDateTime.parse(dateString, formatter)
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(dateString)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
