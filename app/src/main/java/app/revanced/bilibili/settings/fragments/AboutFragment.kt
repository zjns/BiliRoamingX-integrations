package app.revanced.bilibili.settings.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import app.revanced.bilibili.utils.Constants
import app.revanced.bilibili.utils.Utils
import app.revanced.bilibili.utils.logFile
import app.revanced.bilibili.utils.onClick

class AboutFragment : BiliRoamingBaseSettingFragment("biliroaming_setting_about") {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>("version")?.summary = Constants.VERSION
        findPreference<Preference>("debug")?.summary =
            Utils.getString("biliroaming_debug_summary", logFile.absolutePath)
        findPreference<Preference>("feedback")?.onClick {
            val uri = Uri.parse(Utils.getString("biliroaming_feedback_url"))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        }
        findPreference<Preference>("project_url")?.onClick {
            val uri = Uri.parse(Utils.getString("biliroaming_project_url"))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        }
        findPreference<Preference>("tg_link")?.onClick {
            val uri = Uri.parse(Utils.getString("biliroaming_tg_url"))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            true
        }
    }
}
