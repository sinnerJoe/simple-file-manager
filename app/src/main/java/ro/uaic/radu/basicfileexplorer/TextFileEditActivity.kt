package ro.uaic.radu.basicfileexplorer

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_text_file_edit.*
import java.io.File

class TextFileEditActivity : AppCompatActivity() {
    companion object{
        val DATA_LABEL = "FILE_DATA"
    }

    private lateinit var editedFile: File

    private val fileContentView
    get() = findViewById<EditText>(R.id.file_content)

    private lateinit var initialContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_file_edit)
//        setSupportActionBar(toolbar)

        fab.setOnClickListener { v -> saveFile(v) }
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.edit_text_action_bar_layout)
        val actionBarView  = supportActionBar?.customView

        editedFile = intent.extras.get(DATA_LABEL) as File
        title = editedFile.name
        loadFile()
    }

    private fun loadFile(){
        val textView = fileContentView
        initialContent = editedFile.readText()
        textView.setText(initialContent, TextView.BufferType.EDITABLE )

    }

    private fun textChanged(): Boolean{
        return !initialContent.equals(fileContentView.text.toString())
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when(keyCode){
            KeyEvent.KEYCODE_BACK -> {
                return backPress()
            }
        }
        return false

    }

    override fun onBackPressed() {
        backPress()
    }

    fun backPress(): Boolean{
        if(!textChanged()){
            finish()
            return true
        }

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle(R.string.editing_file_exit)
        dialog.setNegativeButton(R.string.cancel) {
                dialog, which ->
            dialog.cancel()
        }
        dialog.setPositiveButton(R.string.save) {
                dialog, which ->
            editedFile.writeText(fileContentView.text.toString())
            dialog.cancel()
            finish()
        }
        dialog.setNeutralButton(R.string.discard){
                dialog, which ->
            dialog.cancel()
            finish()
        }
        dialog.show()
        return true
    }


    fun saveFile(view: View?){
        editedFile.writeText(fileContentView.text.toString())
        finish()
    }
}
