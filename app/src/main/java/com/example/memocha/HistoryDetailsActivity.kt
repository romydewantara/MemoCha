package com.example.memocha

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memocha.adapter.HistoryDetailAdapter
import com.example.memocha.entity.History
import com.example.memocha.lib.HistoryDetailsEditor
import com.example.memocha.lib.MemoChaPopupDialog
import com.example.memocha.utility.AppUtil
import com.example.memocha.utility.Constant
import com.example.memocha.utility.MemoChaRoomDatabase
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_history_details.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigInteger
import java.util.Objects

@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility", "InlinedApi")
class HistoryDetailsActivity : AppCompatActivity() {

    companion object {
        const val TAG = "HistoryDetailsAct"
        const val PICK_TEXT_FILE = 2
    }
    private val database by lazy { MemoChaRoomDatabase(this) }
    private var historyDetailsEditor: HistoryDetailsEditor? = null

    private lateinit var month: String
    private lateinit var year: String
    private lateinit var history: List<History>
    private lateinit var historyDetailAdapter: HistoryDetailAdapter

    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_details)

        init()
        initListener()
        fetchHistoryDetails()
    }

    private fun init() {
        month = intent.extras?.getString("month").toString()
        year = intent.extras?.getString("year").toString()
        rootHistory.viewTreeObserver.addOnGlobalLayoutListener {
            if (!AppUtil.isKeyboardVisible(rootHistory)) editTextSearch.isFocusable = false
        }

        textViewMonthName.text = AppUtil.convertMonthNameFromCode(this, month)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewAmount,
                1, 26, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewImport,
                1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewExport,
                1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        setupRecyclerView()
    }

    private fun initListener() {
        imageMenu.setOnClickListener {
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        imageImport.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance()
            mcPopupDialog.setContent(getString(R.string.dialog_title_information), getString(R.string.dialog_message_import_history),
                    getString(R.string.button_yes), getString(R.string.button_cancel), object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {
                            importFile()
                        }
                        override fun onPositiveButton() {}
                    })
            mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
        }
        imageExport.setOnClickListener {
            val mcPopupDialog = MemoChaPopupDialog.newInstance()
            mcPopupDialog.setContent(String.format(getString(R.string.dialog_title_export_history),
                    AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month)),
                    getString(R.string.dialog_message_export_history), getString(R.string.button_yes), getString(R.string.button_cancel),
                    object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                        override fun onNegativeButton() {
                            exportFile()
                        }
                        override fun onPositiveButton() {}
                    })
            mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
        }
        editTextSearch.setOnTouchListener { _, _ ->
            editTextSearch.isFocusableInTouchMode = true
            false
        }
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isNotEmpty()) {
                    searchingHistory(s.toString())
                } else {
                    textViewSearchEmpty.visibility = View.GONE
                    historyDetailAdapter.setData(history)
                }
            }
        })
        historyDetailAdapter.addOnHistoryDetailListener(
                object : HistoryDetailAdapter.HistoryDetailListener {
                    override fun onItemExpensesClicked(history: History) {
                        showEditor(history)
                    }
                }
        )
    }

    private fun showEditor(history: History) {
        isEditing = true
        historyDetailsEditor = HistoryDetailsEditor(this@HistoryDetailsActivity, history, supportFragmentManager)
                .addMonthlyExpensesEditorListener(object : HistoryDetailsEditor.MonthlyExpensesEditorListener {
                    override fun onSaveClicked(id: Int, history: History) {
                        updateHistory(id, history)
                        closeEditorLayout()
                    }
                    override fun onDeleteClicked(id: Int) {
                        deleteFromHistory(id)
                        closeEditorLayout()
                    }
                })
        layoutHistoryDetailsEditor.removeAllViews()
        layoutHistoryDetailsEditor.addView(historyDetailsEditor)
        layoutHistoryDetailsEditor.visibility = View.VISIBLE
        layoutHistoryDetailsEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in))
        layoutHistoryDetailsEditor.setOnClickListener {
            AppUtil.hideSoftKeyboard(layoutHistoryDetailsEditor, this@HistoryDetailsActivity)
        }
    }

    private fun closeEditorLayout() {
        isEditing = false
        layoutHistoryDetailsEditor.visibility = View.GONE
        layoutHistoryDetailsEditor.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out))
        historyDetailsEditor = null
    }

    private fun updateHistory(id: Int, history: History) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().updateHistory(
                id,
                history.yearEdited,
                history.monthEdited,
                history.dateEdited,
                history.timeEdited,
                history.goods,
                history.amount,
                history.description,
                history.category,
                history.method
            )
            fetchHistoryDetails()
        }
    }

    private fun deleteFromHistory(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().deleteFromHistory(id)
            fetchHistoryDetails()
        }
    }

    private fun setupRecyclerView() {
        historyDetailAdapter = HistoryDetailAdapter(this, arrayListOf())
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(this@HistoryDetailsActivity)
            adapter = historyDetailAdapter
        }
    }

    private fun fetchHistoryDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            history = database.historyDao().getHistory(year, month)
            if (history.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    historyDetailAdapter.setData(history)
                    runOnUiThread {
                        enableExportButton()
                        disableImportButton()
                        var sum = BigInteger("0")
                        for (i in history.indices) {
                            val amount: BigInteger = history[i].amount.replace(",", "").toBigInteger()
                            sum += amount
                        }
                        val summary = "Rp ${String.format("%,d", sum)}"
                        textViewAmount.text = summary.replace(",", ".")
                        editTextSearch.background = resources.getDrawable(R.drawable.background_edit_text_search, null)
                        editTextSearch.isEnabled = true
                        editTextSearch.isFocusable = true
                        textViewHistoryEmpty.visibility = View.GONE
                        cardViewPaymentDetail.visibility = View.VISIBLE
                    }
                }
            } else {
                runOnUiThread {
                    disableExportButton()
                    enableImportButton()
                    val zero = "Rp 0"
                    textViewAmount.text = zero
                    editTextSearch.background = resources.getDrawable(R.drawable.background_edit_text_search_disabled, null)
                    editTextSearch.isEnabled = false
                    editTextSearch.isFocusable = false
                    textViewHistoryEmpty.visibility = View.VISIBLE
                    cardViewPaymentDetail.visibility = View.GONE
                }
            }
        }
    }

    private fun searchingHistory(query: String) {
        if (history.isNotEmpty()) {
            val temporaryList = arrayListOf<History>()
            for (i in history.indices) {
                if (history[i].amount.lowercase().contains(query.trim().lowercase()) ||
                    history[i].goods.lowercase().contains(query.trim().lowercase()) ||
                    history[i].description.lowercase().contains(query.trim().lowercase())) {
                    temporaryList.add(history[i])
                }
            }
            if (temporaryList.isEmpty()) textViewSearchEmpty.visibility = View.VISIBLE
            else textViewSearchEmpty.visibility = View.GONE
            historyDetailAdapter.setData(temporaryList)
        }
    }

    private fun disableExportButton() {
        imageExport.background =
            resources.getDrawable(R.drawable.background_button_export_circle_pressed, null)
        imageExport.isEnabled = false
        imageExport.isFocusable = false
        imageExport.isClickable = false
        imageExport.setImageResource(R.drawable.ic_export_white)
        TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularGrey12)
    }

    private fun disableImportButton() {
        imageImport.background =
            resources.getDrawable(R.drawable.background_button_export_circle_pressed, null)
        imageImport.isEnabled = false
        imageImport.isFocusable = false
        imageImport.isClickable = false
        imageImport.setImageResource(R.drawable.ic_import_white)
        TextViewCompat.setTextAppearance(textViewImport, R.style.TextRegularGrey12)
    }

    private fun enableExportButton() {
        imageExport.background =
            resources.getDrawable(R.drawable.selector_button_export, null)
        imageExport.isEnabled = true
        imageExport.isFocusable = true
        imageExport.isClickable = true
        imageExport.setImageResource(R.drawable.ic_export)
        TextViewCompat.setTextAppearance(textViewExport, R.style.TextRegularWhite12)
    }

    private fun enableImportButton() {
        imageImport.background =
            resources.getDrawable(R.drawable.selector_button_export, null)
        imageImport.isEnabled = true
        imageImport.isFocusable = true
        imageImport.isClickable = true
        imageImport.setImageResource(R.drawable.ic_import_teal_dark)
        TextViewCompat.setTextAppearance(textViewImport, R.style.TextRegularWhite12)
    }

    private fun exportFile() {
        val fileName = "monthly_expenses_${AppUtil.convertMonthNameFromCode(this, month).lowercase()}"
        val path = AppUtil.writeFileToStorage(this@HistoryDetailsActivity, Constant.FOLDER_NAME_HISTORY, fileName, Gson().toJson(history))
        val message = if (path.isNotEmpty()) String.format(getString(R.string.snackbar_monthly_expenses_exported),
            AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month), path)
        else String.format(getString(R.string.snackbar_monthly_expenses_exported_failed),
            AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month), path)
        val snackBar = Snackbar.make(rootHistory, message, 6500).setAction(getString(R.string.snackbar_button_open)) {
            //action to open folder based on path
            /*val intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri = Uri.parse(path)
            intent.setDataAndType(uri, "text/csv")
            startActivity(intent)*/
        }
        val view = snackBar.view
        val snackBarTextView = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        snackBarTextView.maxLines = 3
        snackBar.show()
    }

    private fun importFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/plain"
        }
        startActivityForResult(intent, PICK_TEXT_FILE)
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri).use { inputStream ->
            BufferedReader(
                InputStreamReader(Objects.requireNonNull(inputStream))
            ).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
            }
        }
        Log.d(TAG, "readTextFromUri - string: $stringBuilder")
        return stringBuilder.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_TEXT_FILE && resultCode == Activity.RESULT_OK) {
            val uri = resultData?.data as Uri
            val result = readTextFromUri(uri)
            val array = JSONArray(result)
            if (array.length() > 0) {
                historyDetailsLoading.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    for (i in 0 until array.length()) {
                        database.historyDao().insert(Gson().fromJson(array[i].toString(), History::class.java))
                        if (i == array.length() - 1) {
                            runOnUiThread {
                                historyDetailsLoading.visibility = View.GONE
                            }
                        }
                    }
                    fetchHistoryDetails()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (isEditing) {
            closeEditorLayout()
        } else {
            startActivity(Intent(this@HistoryDetailsActivity, DashboardActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
    }
}