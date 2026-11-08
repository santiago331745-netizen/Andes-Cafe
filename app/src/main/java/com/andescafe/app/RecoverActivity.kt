package com.andescafe.app

import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.andescafe.app.databinding.ActivityRecoverBinding

class RecoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityRecoverBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupBar(b.toolbar, getString(R.string.recover_title))
        b.btnSend.setOnClickListener {
            val ok = Patterns.EMAIL_ADDRESS.matcher(b.etEmail.text.toString().trim()).matches()
            b.tilEmail.error = if (ok) null else getString(R.string.err_email)
            if (ok) { toast(getString(R.string.recover_sent)); finish() }
        }
    }
}
