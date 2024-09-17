package ai.mlc.mlcchat.utils.benchmark

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

enum class LogStatus {
    INTERRUPTED, NOT_RUN, RUN
}

class ModelStatusLog(context: Context) {

    private val startedModelsSharedPreferences = context.getSharedPreferences("startedModels", Context.MODE_PRIVATE)
    private val finishedModelsSharedPreferences = context.getSharedPreferences("finishedModels", Context.MODE_PRIVATE)

    private val selectionStatus = context.getSharedPreferences("selectionStatus", Context.MODE_PRIVATE)

    private fun generateTimeStamp(): String {
        val currentDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z").format(currentDateTime)
    }

    private fun parseTimeStampToDate(timeStamp: String): Date {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val zonedDateTime = ZonedDateTime.parse(timeStamp, formatter)

        val instant = zonedDateTime.toInstant()
        return Date.from(instant)
    }

    private fun parseAndFormatTimeStamp(timeStamp: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val zonedDateTime = ZonedDateTime.parse(timeStamp, formatter)

        val humanReadableFormatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm")
        return zonedDateTime.format(humanReadableFormatter)
    }

    private fun getStoredStartTime(modelName: String): String? {
        return startedModelsSharedPreferences.getString(modelName, null)
    }

    private fun getStoredEndTime(modelName: String): String? {
        return finishedModelsSharedPreferences.getString(modelName, null)
    }

    fun getStatus(modelName: String): LogStatus {
        val startTime = getStoredStartTime(modelName)
        val endTime = getStoredEndTime(modelName)

        if(startTime == null) return LogStatus.NOT_RUN
        if(endTime == null) return LogStatus.INTERRUPTED

        val startDate = parseTimeStampToDate(startTime)
        val endDate = parseTimeStampToDate(endTime)

        return if(endDate > startDate) LogStatus.RUN else LogStatus.INTERRUPTED
    }

    fun getLastExecutionDateReadable(modelName: String): String? {
        val status = getStatus(modelName)
        if(status !== LogStatus.RUN) return null

        val startTime = getStoredStartTime(modelName) ?: return null

        return parseAndFormatTimeStamp(startTime)
    }

    fun logStartModel(modelName: String) {
        val editor = startedModelsSharedPreferences.edit()
        editor.putString(modelName, generateTimeStamp())
        editor.apply()
    }

    fun logEndModel(modelName: String) {
        val editor = finishedModelsSharedPreferences.edit()
        editor.putString(modelName, generateTimeStamp())
        editor.apply()
    }

    fun checkWasModelInterrupted(modelName: String): Boolean{
        return getStatus(modelName) == LogStatus.INTERRUPTED
    }

    fun getIsSelected(modelName: String): Boolean {
        return selectionStatus.getBoolean(modelName, true)
    }

    fun setIsSelected(modelName: String, value: Boolean) {
        val editor = selectionStatus.edit()
        editor.putBoolean(modelName, value)
        editor.apply()
    }


}