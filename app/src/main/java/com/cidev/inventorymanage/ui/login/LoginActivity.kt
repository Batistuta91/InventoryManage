package com.cidev.inventorymanage.ui.login

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cidev.inventorymanage.data.AuthRepository
import com.cidev.inventorymanage.databinding.ActivityLoginBinding
import com.cidev.inventorymanage.network.SoapClient
import com.cidev.inventorymanage.ui.menu.MainMenuActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Remember the last IP the person typed, default to the one from
        // ServiceIP.txt (192.168.0.22) on first launch.
        val prefs = getSharedPreferences("inventory_manage_prefs", MODE_PRIVATE)
        binding.edtServerIp.setText(prefs.getString("server_ip", "192.168.0.22"))

        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val serverIp = binding.edtServerIp.text?.toString()?.trim().orEmpty()
        if (serverIp.isEmpty()) {
            showError("נא למלא כתובת שרת (IP)")
            return
        }
        getSharedPreferences("inventory_manage_prefs", MODE_PRIVATE)
            .edit().putString("server_ip", serverIp).apply()
        SoapClient.configureServer(serverIp)

        val username = binding.edtUsername.text?.toString()?.trim().orEmpty()
        val password = binding.edtPassword.text?.toString().orEmpty()

        if (username.isEmpty()) {
            showError("נא למלא שם משתמש")
            return
        }

        setLoading(true)
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        lifecycleScope.launch {
            val result = authRepository.login(username, password, deviceId)
            setLoading(false)

            result.onSuccess { user ->
                if (user.isValid) {
                    MainMenuActivity.currentUser = user
                    startActivity(Intent(this@LoginActivity, MainMenuActivity::class.java))
                    finish()
                } else {
                    val detail = "status=${user.responseStatus} message=${user.responseMessage} sessionID=${user.sessionID}"
                    showError("פרטי התחברות שגויים\n$detail")
                }
            }.onFailure { e ->
                showError("שגיאת תקשורת עם השרת: ${e.message}")
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun showError(msg: String) {
        binding.txtError.text = msg
        binding.txtError.visibility = View.VISIBLE
    }
}
