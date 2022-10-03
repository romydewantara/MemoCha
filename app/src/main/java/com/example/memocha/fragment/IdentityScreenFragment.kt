package com.example.memocha.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.R
import com.example.memocha.adapter.IdentityAdapter
import com.example.memocha.utility.MemoChaRoomDatabase
import kotlinx.android.synthetic.main.layout_identity_screen.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ClickableViewAccessibility", "UseCompatLoadingForDrawables")
class IdentityScreenFragment(context: Context) : Fragment(R.layout.layout_identity_screen) {

    private val database by lazy { MemoChaRoomDatabase(context) }
    private lateinit var identityListener: IdentityListener
    private lateinit var identityAdapter: IdentityAdapter
    var isContainerShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityListener.onIdentityScreenCreated()

        init()
        setupRecyclerView()
        initListener()
    }

    private fun init() {
        CoroutineScope(Dispatchers.IO).launch {
            val identity = database.identityDao().getIdentity()
            if (identity.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    identityAdapter.setData(identity)
                }
            }
        }
    }

    private fun initListener() {
        imageHamburgerMenu.setOnClickListener {
            Toast.makeText(context, "Burger Menu Clicked", Toast.LENGTH_SHORT).show()
        }
        imageAdd.setOnClickListener {
            showContainerIdentity()
        }
        editTextName.addTextChangedListener(onTextChangedListener())
        editTextNIK.addTextChangedListener(onTextChangedListener())
        editTextAddress.addTextChangedListener(onTextChangedListener())
        editTextBOD.addTextChangedListener(onTextChangedListener())
        editTextPhone.addTextChangedListener(onTextChangedListener())
        editTextName.setOnTouchListener { _, _ ->
            editTextName.isFocusableInTouchMode = true
            false
        }
        editTextNIK.setOnTouchListener { _, _ ->
            editTextNIK.isFocusableInTouchMode = true
            false
        }
        editTextAddress.setOnTouchListener { _, _ ->
            editTextAddress.isFocusableInTouchMode = true
            false
        }
        editTextBOD.setOnTouchListener { _, _ ->
            editTextBOD.isFocusableInTouchMode = true
            false
        }
        editTextPhone.setOnTouchListener { _, _ ->
            editTextPhone.isFocusableInTouchMode = true
            false
        }
        textViewAdd.setOnClickListener {

        }
    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (editTextName.text.toString().isNotEmpty() &&
                    editTextNIK.text.toString().isNotEmpty() &&
                    editTextAddress.text.toString().isNotEmpty() &&
                    editTextBOD.text.toString().isNotEmpty() &&
                    editTextPhone.text.toString().isNotEmpty()) {
                    textViewAdd.isEnabled = true
                    textViewAdd.background = requireContext().resources.getDrawable(R.drawable.selector_button_apply_settings, null)
                } else {
                    textViewAdd.isEnabled = false
                    textViewAdd.background = requireContext().resources.getDrawable(R.drawable.background_button_apply_disabled, null)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        identityAdapter = IdentityAdapter(requireContext(), arrayListOf(),
            object : IdentityAdapter.IdentityListener {
            override fun onSelectedId(id: String) {
                Toast.makeText(context, "Id: $id", Toast.LENGTH_SHORT).show()
            }
        })
        recyclerViewIdentity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = identityAdapter
        }
    }

    private fun showContainerIdentity() {
        isContainerShown = true
        identityOverlay.visibility = View.VISIBLE
        identityOverlay.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.white_overlay_fade_in))
        containerIdentity.x = requireContext().resources.displayMetrics.widthPixels.toFloat()
        containerIdentity.removeAllViews()
        val containerParams = RelativeLayout.LayoutParams(requireContext().resources.displayMetrics.widthPixels, requireContext().resources.displayMetrics.heightPixels)
        containerIdentity.layoutParams = containerParams

        val identityForm = LayoutInflater.from(requireContext()).inflate(R.layout.layout_form_identity, containerIdentity, false)
        containerIdentity.addView(identityForm)
        containerIdentity.visibility = View.VISIBLE
        val objectAnimator = ObjectAnimator.ofFloat(containerIdentity, "translationX", 0f)
        objectAnimator.duration = 300L
        objectAnimator.start()
    }

    fun hideContainerIdentity() {
        val objectAnimator = ObjectAnimator.ofFloat(containerIdentity, "translationX", requireContext().resources.displayMetrics.widthPixels.toFloat())
        objectAnimator.duration = 150L
        objectAnimator.start()
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                identityOverlay.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.white_overlay_fade_out))
                identityOverlay.visibility = View.GONE
                containerIdentity.visibility = View.GONE
                containerIdentity.removeAllViews()
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
    }

    fun addIdentityListener(identityListener: IdentityListener): Fragment {
        this.identityListener = identityListener
        return this@IdentityScreenFragment
    }

    interface IdentityListener {
        fun onIdentityScreenCreated()
    }
}