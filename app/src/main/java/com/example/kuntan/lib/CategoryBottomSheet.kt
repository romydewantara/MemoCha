package com.example.kuntan.lib

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kuntan.R
import com.example.kuntan.adapter.CategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_category.*

class CategoryBottomSheet : BottomSheetDialogFragment(), CategoryAdapter.CategoryAdapterListener {

    private lateinit var categoryListener: CategoryListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryAdapter = CategoryAdapter(requireContext(), this)
        recyclerviewCategory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
        textViewCancel.setOnClickListener { dismiss() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState)
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.from(bottomSheet).skipCollapsed = false
                BottomSheetBehavior.from(bottomSheet).isDraggable = false
            }
        }
        return bottomSheetDialog
    }

    fun addCategoryListener(categoryListener: CategoryListener) : CategoryBottomSheet {
        this.categoryListener = categoryListener
        return this@CategoryBottomSheet
    }
    override fun onCategoryClicked(category: String) {
        categoryListener.onCategorySelected(category)
        dismiss()
    }

    interface CategoryListener {
        fun onCategorySelected(category: String)
    }
}