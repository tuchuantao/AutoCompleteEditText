package com.kevin.autocompleteedittext

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

/**
 * Create by KevinTu on 2018/10/15
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initEmail()
    }

    /**
     * 初始化常用的邮箱
     */
    private fun initEmail() {
        val userInputs = resources.getStringArray(R.array.auto_complete_emails_input)
        val notify = resources.getStringArray(R.array.auto_complete_emails_notify)
        if (userInputs.size == notify.size && userInputs.isNotEmpty()) {
            var autoData = HashMap<String, String>()
            for (i in userInputs.indices) {
                autoData[userInputs[i]] = notify[i]
            }
            email.initEmail(autoData)
        }
    }
}
