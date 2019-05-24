package ro.uaic.radu.basicfileexplorer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ListView
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    val tag = "MAIN"
    val CURRENT_DIR_FILE = "PATH_FILE"
    val PERMISSION_READ_WRITE_REQUEST = 20

    val listView: ListView
    get() = findViewById(R.id.entry_list)
    lateinit var entryAdapter: FolderEntryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.v("MAIN", "ONCREATE")
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_READ_WRITE_REQUEST)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.v("MAIN", "onRequestPermissionsResult")
        when(requestCode){
            PERMISSION_READ_WRITE_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults.all { code -> code == PackageManager.PERMISSION_GRANTED }){
                    viewRootFolders()
                    NavigatorManager.activity = this
                    initialiseEntryList()
                } else {
                    throw Exception("NO PERMISSIONS GRANTED, SORRY")
                }
            }
        }
    }

    fun initialiseEntryList(){
        moveToFirstDir()
        val adapter = FolderEntryAdapter(NavigatorManager.contents, this)
        this.entryAdapter = adapter
        val listView = this.listView
        listView.adapter = adapter
        listView.onItemClickListener = adapter
        listView.onItemLongClickListener = adapter

        Log.v("MAIN", "list view initialised")
        Log.v("MAIN", NavigatorManager.contents.joinToString { entry-> entry.name })
    }


    fun viewRootFolders(){
        val path = Environment.getExternalStorageDirectory().toString()
        val dir = File(path)
        val files = dir.listFiles()
        for(file in files){
            Log.v("MAIN", file.absolutePath)
        }
    }

    fun deleteEntry(entry: FolderEntry): Boolean{
        return NavigatorManager.delete(entry)
    }

    fun renameEntry(entry: FolderEntry){
        val builder = AlertDialog.Builder(this)

        val title = if(entry.isDirectory()) "Rename Folder" else "Rename File"
        val editText = EditText(this)
        editText.text.insert(0, entry.name)
        builder.setTitle(title)
        builder.setView(editText)
        builder.setPositiveButton("Save", {
            dialog, which ->
            val isRenamed = NavigatorManager.rename(entry, editText.text.toString())
            if(isRenamed)
                entryAdapter.notifyDataSetChanged()
            else
                createSimpleAlert("File with the same name already exists in the current folder")
        })
        builder.setNegativeButton("Cancel", {
            dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    fun moveEntry(entry: FolderEntry){
        invalidateOptionsMenu()
        NavigatorManager.move(entry)
    }

    fun copyEntry(entry: FolderEntry){
        invalidateOptionsMenu()
        NavigatorManager.copy(entry)
    }

    fun updateTitlePath(){
        var curPath = NavigatorManager.currentDirectory?.absolutePath
        curPath?.let {


            if (it.length > 30) {
                curPath = "..." + it.substring(it.length - 30)
            }
            title = curPath
        }
    }

    fun cd(entry: FolderEntry): Boolean{
        Log.v(tag, "CHOSEN ENTRY " + entry.name)
        val success = NavigatorManager.cd(entry)
        if (success) {
            updateTitlePath()
        }
        return success
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.upper_bar_menu, menu)
        menu?.findItem(R.id.paste)?.isEnabled = NavigatorManager.hasActiveBuffer()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.paste -> {
                try {
                        NavigatorManager.paste()
                        entryAdapter.notifyDataSetChanged()
                    }catch (e: FileAlreadyExistsException){

                    val firstElement = NavigatorManager.firstBufferElement()
                    if(firstElement != null) {
                        val builder = AlertDialog.Builder(this)
                        builder
                            .setTitle("Error on paste")
                            .setMessage("File or folder with the same name exists")
                            .setPositiveButton("Replace"){
                                    dialog, which ->
                                NavigatorManager.paste(true)
                                entryAdapter.notifyDataSetChanged()
                            }
                            .setNeutralButton("Rename"){
                                    dialog, which ->
                                dialog.cancel()
                                createPrompt("New name",
                                    hint = "File name",
                                    defaultVal = firstElement.name,
                                    positiveButton = "Save"){
                                        NavigatorManager.paste(true, it)
                                        entryAdapter.notifyDataSetChanged()
                                    }
                            }
                            .setNegativeButton("Cancel"){
                                    dialog, which ->
                                dialog.cancel()
                            }.show()
                    }
                }
            }

            R.id.create_folder -> createPrompt("Create new folder", "Create", "", "Folder name"
            ) {
                try{

                    if(it.isNotBlank() && NavigatorManager.createFolder(it)){
                        entryAdapter.notifyDataSetChanged()
                    } else {
                        createSimpleAlert("Folder name is empty!"){
                            onOptionsItemSelected(item)
                        }
                    }
                }catch (e: FileCreationFailed){
                createSimpleAlert("Folder creation failed!")
            }catch (e: FileAlreadyExists){
                createSimpleAlert("File or folder with the same name already exists!"){
                    onOptionsItemSelected(item)
                }
            }
            }

            R.id.create_file -> createPrompt("Create new file", "Create", "", "File name"
            ) {
                if(it.isNotBlank()){
                    try {
                        val file = NavigatorManager.createFile(it)
                        file?.let{file ->
                            editFile(FolderEntry(file))
                            entryAdapter.notifyDataSetChanged()
                        }
                    }catch(e: FileCreationFailed){
                        createSimpleAlert("File creation failed!")
                    }catch (e: FileAlreadyExists){
                        createSimpleAlert("File or folder with the same name already exists!")
                    }
                } else {
                    createSimpleAlert("File name is empty!")
                    onOptionsItemSelected(item)
                }
            }
        }
        return true
    }

    private fun createPrompt(title:String, positiveButton: String, defaultVal: String, hint: String, callback: (output: String) -> Unit){
        val builder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.text.insert(0, defaultVal)
        editText.hint = hint
        builder.setTitle(title)
        builder.setView(editText)
        builder.setPositiveButton(positiveButton, {
                dialog, which ->
            callback(editText.text.toString())
        })
        builder.setNegativeButton("Cancel", {
                dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun createSimpleAlert(message: String, callback: (() -> Unit)? = null){
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.show().setOnDismissListener {
            if(callback != null){
                callback()
            }
        }

    }

    fun editFile(fileEntry: FolderEntry){
        val intent = Intent(applicationContext, TextFileEditActivity::class.java)
        intent.putExtra(TextFileEditActivity.DATA_LABEL, fileEntry.file)
        startActivity(intent)
    }

    private fun saveState(){
        if(NavigatorManager.currentDirectory == null || NavigatorManager.currentDirectory?.absolutePath!!.isBlank()){
            File(filesDir, CURRENT_DIR_FILE).delete()
            return
        }
        val outFile = openFileOutput(CURRENT_DIR_FILE, Context.MODE_PRIVATE)
        outFile.write(NavigatorManager.currentDirectory?.absolutePath?.toByteArray())
        outFile.close()
    }

    private fun loadState(): String?{
        try{
            val stateFile = openFileInput(CURRENT_DIR_FILE)
            val arr = ByteArray(256)
            val bytesRead = stateFile.read(arr)
            return if(bytesRead == 0) null else arr.toString()
        } catch (e: FileNotFoundException){
            return null
        }
    }

    private fun moveToFirstDir(){
        val state = loadState()
        if(state == null){
            NavigatorManager.initFolder()
        }else{
            cd(FolderEntry(File(state)))
        }
    }

    override fun onPause() {
        saveState()
        super.onPause()
    }

    override fun onStop() {
        saveState()
        super.onStop()
    }

    override fun onRestart() {
        moveToFirstDir()
        super.onRestart()
    }

    override fun onResume() {
        moveToFirstDir()
        super.onResume()
    }

    override fun onBackPressed() {
        if (NavigatorManager.isOnRoot){
            super.onBackPressed()
        }
        else if(NavigatorManager.cdBack()){
            updateTitlePath()
            entryAdapter.notifyDataSetChanged()
        }
    }



}
