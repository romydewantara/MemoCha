package com.example.memocha.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.R
import com.example.memocha.adapter.IdentityAdapter
import com.example.memocha.entity.Identity
import com.example.memocha.utility.MemoChaRoomDatabase
import kotlinx.android.synthetic.main.layout_identity_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IdentityScreenFragment(context: Context) : Fragment(R.layout.layout_identity_screen) {

    private val database by lazy { MemoChaRoomDatabase(context) }
    private val listOfIdentity = ArrayList<Identity>()
    private lateinit var identityListener: IdentityListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityListener.onIdentityScreenCreated()

        init()
        initListener()
        setupRecyclerView()
    }

    private fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            val identity = database.identityDao().getIdentity()
            Log.d("Identity", "init - identity: $identity")
            if (identity.isNotEmpty()) {
                listOfIdentity.addAll(identity)
            } else {
                val identity1 = Identity(0, "3173022012950005", "Romy Dewantara", "Bunga Rampai 1 Jakarta Timur")
                val identity2 = Identity(1, "3175022505950005", "Rosyadah Assa\'diyah", "Bunga Rampai 1 Jakarta Timur")
                val identity3 = Identity(2, "3175021710210001", "Shindra Haryaka", "Bunga Rampai 1 Jakarta Timur")
                database.identityDao().insert(identity1)
                database.identityDao().insert(identity2)
                database.identityDao().insert(identity3)
                listOfIdentity.add(identity1)
                listOfIdentity.add(identity2)
                listOfIdentity.add(identity3)
            }
        }
    }

    private fun initListener() {
        imageHamburgerMenu.setOnClickListener {
            Toast.makeText(context, "Burger Menu Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        Log.d("Identity", "setupRecyclerView - listOfIdentity: $listOfIdentity")
        val identityAdapter = IdentityAdapter(requireContext(), listOfIdentity,
            object : IdentityAdapter.IdentityListener {
            override fun onSelectedId(id: String) {
                //open the detail identity
                Toast.makeText(context, "Id: $id", Toast.LENGTH_SHORT).show()
            }
        })
        recyclerViewIdentity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = identityAdapter
        }
    }

    fun addIdentityListener(identityListener: IdentityListener): Fragment {
        this.identityListener = identityListener
        return this@IdentityScreenFragment
    }

    interface IdentityListener {
        fun onIdentityScreenCreated()
    }
}