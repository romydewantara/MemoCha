package com.example.memocha

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigInteger
import java.util.Objects


@SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility", "InlinedApi")
class HistoryDetailsActivity : AppCompatActivity() {

    private val database by lazy { MemoChaRoomDatabase(this) }

    private lateinit var month: String
    private lateinit var year: String
    private lateinit var history: List<History>
    private lateinit var historyDetailAdapter: HistoryDetailAdapter
    var historyDetailsEditor: HistoryDetailsEditor? = null

    private var isEditing = false
    private var isExport = false

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
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textViewExportImport,
                1, 12, 1, TypedValue.COMPLEX_UNIT_SP)
        setupRecyclerView()
    }

    private fun initListener() {
        imageMenu.setOnClickListener {
            startActivity(Intent(this@HistoryDetailsActivity, HistoryActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
            finish()
        }
        layoutExportImport.setOnClickListener {
            imageExportImport.startAnimation(AnimationUtils.loadAnimation(this@HistoryDetailsActivity, R.anim.bounched_show))
            if (isExport) {
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
            } else {
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

    private fun fetchHistoryDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            history = database.historyDao().getHistory(year, month)
            if (history.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    historyDetailAdapter.setData(history)
                    val date = "${history[0].month}${history[0].year}"
                    AppUtil.writeFileToCache(this@HistoryDetailsActivity, Constant.FOLDER_NAME_HISTORY, "backup_$date", Gson().toJson(history))
                    runOnUiThread {
                        setAsExport()
                        var sum = BigInteger("0")
                        for (i in history.indices) {
                            val amount: BigInteger = history[i].amount.replace(".", "").toBigInteger()
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
                    setAsImport()
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

    private fun setupRecyclerView() {
        historyDetailAdapter = HistoryDetailAdapter(this, arrayListOf())
        recyclerviewPaymentDetail.apply {
            layoutManager = LinearLayoutManager(this@HistoryDetailsActivity)
            adapter = historyDetailAdapter
        }
    }

    private fun updateHistory(id: Int, history: History) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().updateHistory(id, history.yearEdited, history.monthEdited,
                history.dateEdited, history.timeEdited, history.goods, history.amount,
                history.description, history.category, history.method, true)
            fetchHistoryDetails()
        }
    }

    private fun deleteFromHistory(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.historyDao().deleteFromHistory(id)
            fetchHistoryDetails()
        }
    }

    private fun searchingHistory(query: String) {
        if (history.isNotEmpty()) {
            val temporaryList = arrayListOf<History>()
            for (i in history.indices) {
                if (history[i].amount.replace(".", "").lowercase().contains(query.trim().lowercase()) ||
                    history[i].goods.lowercase().contains(query.trim().lowercase()) ||
                    history[i].description.lowercase().contains(query.trim().lowercase()) ||
                    history[i].method.lowercase().contains(query.trim().lowercase()) ||
                    history[i].date.lowercase().contains(query.trim().lowercase())) {
                    temporaryList.add(history[i])
                }
            }
            if (temporaryList.isEmpty()) textViewSearchEmpty.visibility = View.VISIBLE
            else textViewSearchEmpty.visibility = View.GONE
            historyDetailAdapter.setData(temporaryList)
        }
    }

    private fun setAsImport() {
        imageExportImport.setImageResource(R.drawable.ic_import_teal_dark)
        textViewExportImport.text = getString(R.string.button_import)
    }

    private fun setAsExport() {
        imageExportImport.setImageResource(R.drawable.ic_export)
        textViewExportImport.text = getString(R.string.button_export)
    }

    private fun exportFile() {
        val fileName = "${AppUtil.convertMonthNameFromCode(this, month).lowercase()}_monthly_expenses"
        val path = AppUtil.writeFileToDownloadsFolder(fileName, Gson().toJson(history))
        val message = if (path.isNotEmpty()) String.format(getString(R.string.snackbar_monthly_expenses_exported),
            AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month), path)
        else String.format(getString(R.string.snackbar_monthly_expenses_exported_failed),
            AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity, month))
        val snackBar = Snackbar.make(rootHistory, message, 6500).setAction(getString(R.string.snackbar_button_open)) {
            //action to open folder based on path
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }
        val view = snackBar.view
        val snackBarTextView = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        snackBarTextView.maxLines = 3
        snackBar.show()
    }

    private fun importFile() {
        resultLauncher.launch(Intent(Intent.ACTION_GET_CONTENT).apply { 
            type = "text/plain"
        })
    }

    private fun showWrongMonthData(monthImported: String) {
        val mcPopupDialog = MemoChaPopupDialog.newInstance()
        mcPopupDialog.setContent(getString(R.string.dialog_title_data_error), String.
        format(getString(R.string.dialog_message_data_error, monthImported, AppUtil.
        convertMonthNameFromCode(this@HistoryDetailsActivity, month))),
            getString(R.string.button_try_again), getString(R.string.button_cancel),
            object : MemoChaPopupDialog.MemoChaPopupDialogListener {
                override fun onNegativeButton() {
                    importFile()
                }
                override fun onPositiveButton() {
                    val snackBar = Snackbar.make(rootHistory, String.
                    format(getString(R.string.snackbar_monthly_expenses_canceled), AppUtil.
                    convertMonthNameFromCode(this@HistoryDetailsActivity, month)), 6500).
                    setAction(getString(R.string.snackbar_button_dismiss)) {}
                    val view = snackBar.view
                    val snackBarTextView = view.findViewById(com.google.android.material.R.id.snackbar_text) as AppCompatTextView
                    TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(snackBarTextView,
                        1, 14, 1, TypedValue.COMPLEX_UNIT_SP)
                    snackBarTextView.maxLines = 2
                    snackBar.show()
                }
            })
        mcPopupDialog.show(supportFragmentManager, mcPopupDialog.tag)
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
        return stringBuilder.toString()
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val uri = it.data?.data as Uri
            val result = readTextFromUri(uri)
            val array = JSONArray(result)
            if (array.length() > 0) {
                if (Gson().fromJson(array[0].toString(), History::class.java).month == month) {
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
                } else showWrongMonthData(AppUtil.convertMonthNameFromCode(this@HistoryDetailsActivity,
                    Gson().fromJson(array[0].toString(), History::class.java).month))
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