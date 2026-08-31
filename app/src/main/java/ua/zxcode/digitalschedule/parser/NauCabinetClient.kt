package ua.zxcode.digitalschedule.parser

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import ua.zxcode.digitalschedule.model.Grade
import java.util.concurrent.TimeUnit

class NauCabinetClient(
    private val baseUrl: String = "https://cabinet.nau.edu.ua"
) {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val existing = cookieStore.getOrPut(url.host) { mutableListOf() }
            cookies.forEach { newCookie ->
                existing.removeAll { it.name == newCookie.name }
                existing.add(newCookie)
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun login(username: String, password: String): Boolean {
        val loginUrl = "$baseUrl/login"

        val loginPageHtml = client.newCall(Request.Builder().url(loginUrl).build()).execute().use { resp ->
            if (!resp.isSuccessful) return false
            resp.body?.string() ?: return false
        }

        val loginDoc = Jsoup.parse(loginPageHtml)
        val csrfToken = loginDoc.select("meta[name=csrf-token]").attr("content")
        if (csrfToken.isBlank()) return false

        val formBody = FormBody.Builder()
            .add("_csrf-frontend", csrfToken)
            .add("LoginForm[username]", username)
            .add("LoginForm[password]", password)
            .build()

        val postRequest = Request.Builder()
            .url(loginUrl)
            .post(formBody)
            .header("Referer", loginUrl)
            .header("Origin", baseUrl)
            .build()

        client.newCall(postRequest).execute().use { resp ->
            val finalUrl = resp.request.url.toString()
            return resp.isSuccessful && !finalUrl.contains("/login")
        }
    }

    fun fetchAllGrades(): Map<String, List<Grade>> {
        val url = "$baseUrl/student/session/get-points"
        val body = client.newCall(
            Request.Builder().url(url).header("Accept", "application/json").build()
        ).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("Не вдалося отримати оцінки: ${resp.code}")
            resp.body?.string() ?: throw Exception("Порожня відповідь від сервера")
        }

        val json = JSONObject(body)
        if (json.optString("status") != "success") {
            throw Exception("API повернув помилку: ${json.toString().take(300)}")
        }

        val data = json.getJSONObject("data")
        val result = LinkedHashMap<String, List<Grade>>()
        val keys = data.keys()
        while (keys.hasNext()) {
            val semesterKey = keys.next()
            val arr = data.getJSONArray(semesterKey)
            result[semesterKey] = (0 until arr.length()).map { i -> parseGradeEntry(arr.getJSONObject(i)) }
        }
        return result
    }

    private fun parseGradeEntry(entry: JSONObject): Grade {
        val point = entry.getJSONObject("point")
        val educationalComponent = entry.getJSONObject("educationalComponent")
        val examSheet = entry.getJSONObject("examSheet")
        val formControl = entry.getJSONObject("formControl")
        val pointsNameArr: JSONArray = entry.optJSONArray("pointsName") ?: JSONArray()
        val agreementArr: JSONArray = entry.optJSONArray("agreement") ?: JSONArray()

        val pointsFinal: Int? = if (point.isNull("points_final")) null else point.optInt("points_final")

        var letterGrade: String? = null
        var verbalGrade: String? = null
        if (pointsFinal != null) {
            for (i in 0 until pointsNameArr.length()) {
                val pn = pointsNameArr.getJSONObject(i)
                val from = pn.getInt("points_from")
                val to = pn.getInt("points_to")
                if (pointsFinal in from..to) {
                    when (pn.getInt("rating_scale_id")) {
                        1 -> letterGrade = pn.getString("name")
                        2 -> verbalGrade = pn.getString("name")
                    }
                }
            }
        }

        val eduComponentType = entry.optJSONObject("eduComponentType")
        val componentTypeAbbr = eduComponentType?.optString("abbr_name")
        val componentTypeName = eduComponentType?.optString("name")
        val isSelective = examSheet.optInt("is_selective", 0) == 1

        val teachersArr = entry.optJSONArray("teachers")
        val teacherNames = if (teachersArr != null && teachersArr.length() > 0) {
            (0 until teachersArr.length()).joinToString(", ") { teachersArr.getJSONObject(it).getString("full_name") }
        } else {
            point.optString("staff_name", "")
        }

        return Grade(
            subject = educationalComponent.getString("name"),
            teacher = teacherNames,
            examType = formControl.getString("name"),
            componentTypeAbbr = componentTypeAbbr,
            componentTypeName = componentTypeName,
            isSelective = isSelective,
            points = pointsFinal,
            letterGrade = letterGrade,
            verbalGrade = verbalGrade,
            date = examSheet.optString("control_date").takeIf { it.isNotBlank() },
            approved = agreementArr.length() > 0
        )
    }
}
