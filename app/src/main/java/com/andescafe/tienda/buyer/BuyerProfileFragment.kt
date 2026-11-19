package com.andescafe.tienda.buyer

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.andescafe.tienda.R
import com.andescafe.tienda.common.ProfileBaseFragment

/** Perfil del comprador: hereda la edición de datos y agrega el acceso a "Mis pedidos". */
class BuyerProfileFragment : ProfileBaseFragment(R.layout.fragment_buyer_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.btnMyOrders).setOnClickListener { findNavController().navigate(R.id.buyerOrders) }
    }
}
