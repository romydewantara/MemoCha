package com.example.memocha.lib

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memocha.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_category.*

class SelectorItemsBottomSheet(private val title: String, private val any: Any) : BottomSheetDialogFragment() {

    private lateinit var categoryListener: CategoryBottomSheetListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewSelectorItemTitle.text = title
        recyclerviewSelectorItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = any as RecyclerView.Adapter<*>
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

    fun addCategoryListener(categoryListener: CategoryBottomSheetListener) : SelectorItemsBottomSheet {
        this.categoryListener = categoryListener
        return this@SelectorItemsBottomSheet
    }

    interface CategoryBottomSheetListener {
        fun onCategorySelected(category: String)
    }
}