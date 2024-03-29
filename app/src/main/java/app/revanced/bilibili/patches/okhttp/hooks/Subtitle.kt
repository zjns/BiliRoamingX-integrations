package app.revanced.bilibili.patches.okhttp.hooks

import android.net.Uri
import app.revanced.bilibili.patches.okhttp.ApiHook
import app.revanced.bilibili.utils.LogHelper
import app.revanced.bilibili.utils.SubtitleHelper
import app.revanced.bilibili.utils.Utils
import app.revanced.bilibili.utils.runCatchingOrNull
import java.util.concurrent.TimeUnit

object Subtitle : ApiHook() {
    override fun shouldHook(url: String, code: Int): Boolean {
        return code.isOk && (url.contains("zh_converter")
                || url.substringBefore('?').endsWith(".ass"))
    }

    override fun hook(url: String, code: Int, request: String, response: String): String {
        var uri = Uri.parse(url)
        if (!uri.getQueryParameter("url").isNullOrEmpty())
            uri = Uri.parse(uri.getQueryParameter("url"))
        val converter = uri.getQueryParameter("zh_converter")
        val ass = uri.lastPathSegment?.endsWith(".ass") ?: false
        var newResponse = response
        if (ass) {
            newResponse = runCatching {
                SubtitleHelper.ass2BJson(response)
            }.onFailure {
                LogHelper.error({ "Ass subtitle convert failed" }, it)
            }.getOrNull() ?: return response
        }
        if (converter == "t2cn") {
            val dictReady = if (!SubtitleHelper.dictExist) {
                runCatchingOrNull {
                    Utils.submitTask {
                        SubtitleHelper.checkDictUpdate()
                    }.get(60, TimeUnit.SECONDS)
                } == true || SubtitleHelper.dictExist
            } else true
            newResponse = if (!dictReady) {
                SubtitleHelper.errorResponse("转换字典下载失败，请重试")
            } else {
                runCatching {
                    SubtitleHelper.convert(newResponse)
                }.onFailure {
                    LogHelper.error({ "Subtitle convert from t to cn failed" }, it)
                }.getOrDefault(SubtitleHelper.errorResponse("字幕转换失败，请重试"))
            }
            Utils.async {
                runCatchingOrNull {
                    if (SubtitleHelper.checkDictUpdate())
                        SubtitleHelper.reloadDict()
                }
            }
        } else if (converter == "en2cn") {
            newResponse = runCatching {
                SubtitleHelper.translate(newResponse)
            }.onFailure {
                LogHelper.error({ "Subtitle translate from en to cn failed" }, it)
            }.getOrDefault(SubtitleHelper.errorResponse("字幕翻译失败，请重试"))
        }
        return newResponse
    }
}
