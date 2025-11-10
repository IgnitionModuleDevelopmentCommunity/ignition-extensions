package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.QualifiedPathUtils
import com.inductiveautomation.ignition.common.StreamingDatasetWriter
import com.inductiveautomation.ignition.common.gson.stream.JsonWriter
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.sqltags.history.AggregationMode
import com.inductiveautomation.ignition.common.sqltags.history.BasicTagHistoryQueryParams
import com.inductiveautomation.ignition.common.sqltags.history.ReturnFormat
import com.inductiveautomation.ignition.common.util.LoggerEx
import com.inductiveautomation.ignition.gateway.dataroutes.AccessControlStrategy
import com.inductiveautomation.ignition.gateway.dataroutes.PermissionType
import com.inductiveautomation.ignition.gateway.dataroutes.PermissionType.getStrategies
import com.inductiveautomation.ignition.gateway.dataroutes.RequestContext
import com.inductiveautomation.ignition.gateway.dataroutes.RouteAccess
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.http.entity.ContentType
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

class HistoryServlet : HttpServlet() {
    private lateinit var context: GatewayContext

    override fun init() {
        context = servletContext.getAttribute(GatewayContext.SERVLET_CONTEXT_KEY) as GatewayContext
    }

    private val strategies = PermissionType.getStrategies(PermissionType.READ)

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val requestContext = RequestContext(req, req.servletPath)
        val routeAccess = AccessControlStrategy.or(strategies).canAccess(requestContext)
        if (routeAccess != RouteAccess.GRANTED) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        resp.contentType = ContentType.APPLICATION_JSON.toString()
        resp.writer.use { writer ->
            val historyQuery: BasicTagHistoryQueryParams =
                try {
                    val paths =
                        req.getParameterValues("path")
                            ?: throw IllegalArgumentException("Must specify at least one path")
                    val startDate =
                        req.getParameter("startDate")?.toDate() ?: Date.from(Instant.now().minus(8, ChronoUnit.HOURS))
                    val endDate = req.getParameter("endDate")?.toDate() ?: Date()
                    val returnSize = req.getParameter("returnSize")?.toInt() ?: -1
                    val aggregationMode =
                        req.getParameter("aggregationMode")?.let(AggregationMode::valueOf) ?: AggregationMode.Average
                    val aliases = req.getParameter("aliases")?.split(',')

                    BasicTagHistoryQueryParams(
                        paths.map(QualifiedPathUtils::toPathFromHistoricalString),
                        startDate,
                        endDate,
                        returnSize,
                        aggregationMode,
                        ReturnFormat.Wide,
                        aliases,
                        emptyList(),
                    )
                } catch (e: Exception) {
                    resp.status = HttpServletResponse.SC_BAD_REQUEST
                    e.printStackTrace(PrintWriter(writer))
                    return
                }

            try {
                context.tagHistoryManager.queryHistory(
                    historyQuery,
                    StreamingJsonWriter(
                        JsonWriter(writer),
                    ),
                )
            } catch (e: TrialExpiredException) {
                resp.status = HttpServletResponse.SC_PAYMENT_REQUIRED
                logger.error("Tag historian module reported trial expired", e)
            } catch (e: Exception) {
                resp.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                logger.error("Unexpected exception writing JSON content to servlet", e)
            }
        }
    }

    class TrialExpiredException : Exception()

    class StreamingJsonWriter(private val jsonWriter: JsonWriter) : StreamingDatasetWriter {
        private lateinit var names: Array<String>
        private lateinit var types: Array<Class<*>>

        private val test = DateTimeFormatter.ISO_INSTANT

        override fun initialize(
            columnNames: Array<String>,
            columnTypes: Array<Class<*>>,
            hasQuality: Boolean,
            expectedRows: Int,
        ) {
            this.names = columnNames
            this.types = columnTypes

            jsonWriter.beginArray()
        }

        override fun write(data: Array<out Any?>, quality: Array<out QualityCode>): Unit = jsonWriter.run {
            if (quality.any { it.`is`(QualityCode.Bad_TrialExpired) }) {
                throw TrialExpiredException()
            }

            writeObject {
                for (index in data.indices) {
                    name(names[index])
                    when (val value = data[index]) {
                        is Number -> {
                            when (types[index]) {
                                Float::class.java, Double::class.java -> value(value.toDouble())
                                else -> value(value.toLong())
                            }
                        }

                        is Date -> {
                            value(test.format(value.toInstant()))
                        }

                        is String -> value(value)
                        is Boolean -> value(value)

                        null -> nullValue()
                    }
                }
            }
        }

        override fun finish() {
            jsonWriter.endArray()
        }

        override fun finishWithError(exception: java.lang.Exception) = throw exception

        private inline fun JsonWriter.writeObject(block: JsonWriter.() -> Unit) {
            beginObject()
            block()
            endObject()
        }
    }

    companion object {
        const val PATH = "history-extension"

        private val logger = LoggerEx.newBuilder().build(HistoryServlet::class.java)

        private val parsingStrategies = listOf<(String) -> Date>(
            SimpleDateFormat.getDateTimeInstance()::parse,
            SimpleDateFormat.getInstance()::parse,
            { Date(it.toLong()) },
        )

        private fun String.toDate(): Date? {
            return parsingStrategies.firstNotNullOfOrNull { strategy ->
                try {
                    strategy.invoke(this)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
}
