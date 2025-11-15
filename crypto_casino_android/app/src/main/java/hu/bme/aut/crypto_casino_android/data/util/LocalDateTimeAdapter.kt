package hu.bme.aut.crypto_casino_android.data.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun write(out: JsonWriter, value: LocalDateTime?) {
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
            // If standard ISO format fails, try with specific format
            // This handles formats like "2025-04-27T11:38:35" (no milliseconds)
            try {
                LocalDateTime.parse(dateString)
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
