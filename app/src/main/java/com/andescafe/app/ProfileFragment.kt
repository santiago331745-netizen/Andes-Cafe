package com.andescafe.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andescafe.app.databinding.FragmentProfileBinding

/** Perfil compartido por los tres roles. */
class ProfileFragment : Fragment() {
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        val b = FragmentProfileBinding.inflate(i, c, false)
        fun paint() {
            b.tvAvatar.text = Repo.userName.take(1).uppercase()
            b.tvEmail.text = Repo.userEmail
            b.tvRole.text = Repo.role
        }
        paint()
        b.etName.setText(Repo.userName)
        b.btnSave.setOnClickListener {
            val n = b.etName.text.toString().trim()
            if (n.isEmpty()) b.tilName.error = getString(R.string.required)
            else { b.tilName.error = null; Repo.userName = n; paint(); requireContext().toast("Perfil actualizado") }
        }
        b.btnLogout.setOnClickListener { requireActivity().logout() }
        return b.root
    }
}
