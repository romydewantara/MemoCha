package com.example.memocha.fragment

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.R
import com.example.memocha.adapter.IdentityAdapter
import com.example.memocha.entity.Identity
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.utility.MemoChaRoomDatabase
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var identity: Identity
    var isContainerShown = false
    var isEdit = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        identityListener.onIdentityScreenCreated()

        init()
        setupRecyclerView()
        initListener()
    }

    private fun init() {
        val containerParams = RelativeLayout.LayoutParams(requireContext().resources.displayMetrics.widthPixels, requireContext().resources.displayMetrics.heightPixels)
        containerIdentity.layoutParams = containerParams
        containerIdentity.x = requireContext().resources.displayMetrics.widthPixels.toFloat()
        CoroutineScope(Dispatchers.IO).launch {
            val identity = database.identityDao().getIdentity()
            if (identity.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    identityAdapter.setData(identity)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        identityAdapter = IdentityAdapter(requireContext(), arrayListOf(),
            object : IdentityAdapter.IdentityListener {
                override fun onSelectedId(identity: Identity) {
                    isEdit = true
                    this@IdentityScreenFragment.identity = identity
                    showContainerIdentity()
                }
            })
        recyclerViewIdentity.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = identityAdapter
        }
    }

    private fun initListener() {
        editTextName.addTextChangedListener(onTextChangedListener())
        editTextNIK.addTextChangedListener(onTextChangedListener())
        editTextAddress.addTextChangedListener(onTextChangedListener())
        editTextBOD.addTextChangedListener(onTextChangedListener())
        editTextPhone.addTextChangedListener(onTextChangedListener())

        imageAdd.setOnClickListener {
            isEdit = false
            showContainerIdentity()
        }
        imageSubmit.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                if (isEdit) {
                    database.identityDao().updateIdentity(identity.idNumber, editTextName.text.toString(),
                        editTextAddress.text.toString(), editTextBOD.text.toString(), editTextPhone.text.toString())
                    this@IdentityScreenFragment.identity.name = editTextName.text.toString()
                    this@IdentityScreenFragment.identity.address = editTextAddress.text.toString()
                    this@IdentityScreenFragment.identity.dob = editTextBOD.text.toString()
                    this@IdentityScreenFragment.identity.phone = editTextPhone.text.toString()
                    Snackbar.make(containerIdentity, requireContext().getString(R.string.snackbar_identity_updated), Snackbar.LENGTH_SHORT)
                        .setAction(requireContext().getString(R.string.snackbar_button_dismiss), {}).show()
                } else {
                    val identity = Identity(
                        0,
                        editTextNIK.text.toString(),
                        editTextName.text.toString(),
                        editTextAddress.text.toString(),
                        editTextBOD.text.toString(),
                        editTextPhone.text.toString())
                    database.identityDao().insert(identity)
                    clearTextFromEditText()
                    Snackbar.make(containerIdentity, requireContext().getString(R.string.snackbar_identity_added), Snackbar.LENGTH_SHORT)
                        .setAction(requireContext().getString(R.string.snackbar_button_dismiss), {}).show()
                }
                imageSubmit.isClickable = false
                imageSubmit.background = requireContext().resources.getDrawable(R.drawable.background_button_send_disabled, null)
                refreshIdentity()
            }
        }
        imageDelete.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance()
            mcPopupDialog.setContent(String.format(getString(R.string.dialog_title_delete_identity), identity.name), getString(R.string.dialog_message_delete_identity),
                getString(R.string.button_yes), getString(R.string.button_cancel), object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                    override fun onNegativeButton() {
                        clearTextFromEditText()
                        CoroutineScope(Dispatchers.IO).launch {
                            database.identityDao().deleteIdentity(identity.idNumber)
                            refreshIdentity()
                        }
                        Snackbar.make(rootIdentity, requireContext().getString(R.string.snackbar_identity_deleted), Snackbar.LENGTH_SHORT)
                            .setAction(requireContext().getString(R.string.snackbar_button_dismiss), {}).show()
                        hideContainerIdentity()
                    }
                    override fun onPositiveButton() {}
                })
            mcPopupDialog.show(childFragmentManager, mcPopupDialog.tag)
        }
    }

    private fun onTextChangedListener() : TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEdit) {
                    if ((editTextName.text.toString().isNotEmpty() && editTextName.text.toString() != identity.name) ||
                        (editTextNIK.text.toString().isNotEmpty() && editTextNIK.text.toString() != identity.idNumber) ||
                        (editTextAddress.text.toString().isNotEmpty() && editTextAddress.text.toString() != identity.address) ||
                        (editTextBOD.text.toString().isNotEmpty() && editTextBOD.text.toString() != identity.dob) ||
                        (editTextPhone.text.toString().isNotEmpty() && editTextPhone.text.toString() != identity.phone)) {
                        imageSubmit.isClickable = true
                        imageSubmit.background = requireContext().resources.getDrawable(R.drawable.selector_button_send_needs_item, null)
                    } else {
                        imageSubmit.isClickable = false
                        imageSubmit.background = requireContext().resources.getDrawable(R.drawable.background_button_send_disabled, null)
                    }
                } else {
                    if (editTextName.text.toString().isNotEmpty() && editTextNIK.text.toString().isNotEmpty() &&
                        editTextAddress.text.toString().isNotEmpty() && editTextBOD.text.toString().isNotEmpty() &&
                        editTextPhone.text.toString().isNotEmpty()) {
                        imageSubmit.isClickable = true
                        imageSubmit.background = requireContext().resources.getDrawable(R.drawable.selector_button_send_needs_item, null)
                    } else {
                        imageSubmit.isClickable = false
                        imageSubmit.background = requireContext().resources.getDrawable(R.drawable.background_button_send_disabled, null)
                    }
                }
            }
        }
    }

    private fun showContainerIdentity() {
        isContainerShown = true
        identityOverlay.visibility = View.VISIBLE
        identityOverlay.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.white_overlay_fade_in))
        val objectAnimator = ObjectAnimator.ofFloat(containerIdentity, "translationX", 0f)
        objectAnimator.duration = 300L
        objectAnimator.start()
        clearTextFromEditText()

        if (isEdit) {
            textViewIdentityFormTitle.text = requireContext().getString(R.string.identity_title_update_data)
            editTextNIK.background = requireContext().getDrawable(R.drawable.background_edit_text_search_disabled)
            imageSubmit.setImageResource(R.drawable.ic_save)
            imageDelete.visibility = View.VISIBLE

            editTextNIK.isFocusableInTouchMode = false
            editTextNIK.isEnabled = false
            editTextNIK.setText(identity.idNumber)
            editTextName.setText(identity.name)
            editTextAddress.setText(identity.address)
            editTextBOD.setText(identity.dob)
            editTextPhone.setText(identity.phone)
        } else {
            textViewIdentityFormTitle.text = requireContext().getString(R.string.identity_title_add_data)
            editTextNIK.background = requireContext().getDrawable(R.drawable.background_edit_text_search)
            editTextNIK.isEnabled = true
            editTextNIK.isFocusableInTouchMode = true
            imageSubmit.setImageResource(R.drawable.ic_send)
            imageDelete.visibility = View.GONE
        }
    }

    fun hideContainerIdentity() {
        isContainerShown = false
        val objectAnimator = ObjectAnimator.ofFloat(containerIdentity, "translationX", requireContext().resources.displayMetrics.widthPixels.toFloat())
        objectAnimator.duration = 150L
        objectAnimator.start()
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                identityOverlay.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.white_overlay_fade_out))
                identityOverlay.visibility = View.GONE
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
    }

    fun refreshIdentity() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = database.identityDao().getIdentity()
            withContext(Dispatchers.Main) {
                identityAdapter.setData(list)
            }
            (requireContext() as Activity).runOnUiThread {
                if (list.isNotEmpty()) recyclerViewIdentity.visibility = View.VISIBLE
                else recyclerViewIdentity.visibility = View.GONE
            }
        }
    }

    private fun clearTextFromEditText() {
        editTextNIK.setText("")
        editTextName.setText("")
        editTextAddress.setText("")
        editTextBOD.setText("")
        editTextPhone.setText("")
    }

    fun addIdentityListener(identityListener: IdentityListener): Fragment {
        this.identityListener = identityListener
        return this@IdentityScreenFragment
    }

    interface IdentityListener {
        fun onIdentityScreenCreated()
    }
}