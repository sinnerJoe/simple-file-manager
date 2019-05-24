package ro.uaic.radu.basicfileexplorer

import android.app.Activity
import android.os.Environment
import android.util.Log
import java.io.File
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.attribute.FileAttribute
import java.util.*
import kotlin.collections.ArrayList

object NavigatorManager {
    val tag = "NAV_MANAGER"

    var activity: MainActivity? = null

    var contents: ArrayList<FolderEntry> = ArrayList<FolderEntry>()

    private var buffer = ArrayList<FolderEntry>()



    var currentDirectory: FolderEntry? = null
    private set(value){
        if(field != null){
            previousDirectories.push(field)
        }
        field = value
        verifyIfInRootFolder()
    }
    private var previousDirectories = Stack<FolderEntry>()
    var isOnRoot: Boolean = true
    private set(value){
        field = value
    }

    var moving: Boolean = false

    private set(value) {
        field = value
    }

    var copying: Boolean

    get() = !moving
    private set(value) {
        moving = !value
    }

    fun firstBufferElement(): FolderEntry?{
        return if(buffer.size > 0) buffer[0] else null
    }


    fun initFolder(){
          cd(FolderEntry(Environment.getExternalStorageDirectory()))

    }

    fun cdBack(): Boolean{
        if(!isOnRoot){
            currentDirectory?.let {
                cd(it.parent)
                return true
            }
        }

        return false
    }





    fun cd(entry: FolderEntry): Boolean{
        if(entry.isDirectory()){
            val childFiles = entry.listFiles()
            if (childFiles == null){
                return false
            }
            currentDirectory = entry


            contents.clear()
            contents.add(entry.parent)
            val sortedEntries = sortEntries(childFiles!!.map {file -> FolderEntry(file) })
            contents.addAll(sortedEntries)

        } else {
            return false
        }

        return true

    }

    fun refresh(){
        val entries = currentDirectory?.listFiles()
        contents.clear()
        if(!isOnRoot)
            contents.add(currentDirectory?.parent!!)
        val sortedEntries = sortEntries(entries!!.map {file -> FolderEntry(file) })
        contents.addAll(sortedEntries)
    }

    private fun sortEntries(entries: List<FolderEntry>): List<FolderEntry>{
        val folders = entries.filter { entry-> entry.isDirectory() }
        val files = entries.filter { entry -> entry.isFile() }
        val sortedFolders = folders.sortedBy { e -> e.name }
        val sortedFiles = files.sortedBy { e -> e.name }
        return sortedFolders + sortedFiles
    }

    fun delete(entry: FolderEntry): Boolean {

        val deleted = entry.delete()
        if(deleted){
            contents.remove(entry)
        }
        return deleted
    }

    fun rename(entry: FolderEntry, name: String): Boolean{
        if(!checkIfSameNameFileExists(FolderEntry(File(entry.parent.file, name))))
            return entry.renameTo(name)
        else
            return false
    }

    fun paste(writeOver: Boolean = false, name:String? = null){
        currentDirectory?.let {

            for(entry in buffer){
                val usedName = if (name == null) buffer[0].name else name
                    if(copying){
                        if (!writeOver)
                            entry.copyTo(it, usedName)
                        else
                            entry.copyToForced(it, usedName)
                    } else{
                        if (writeOver)
                            entry.moveToForced(it, usedName)
                        else
                            entry.moveTo(it, usedName)
                    }
                    refresh()
            }
        }

    }

    fun copy(entry: FolderEntry){
        buffer = arrayListOf(entry)
        moving = false
    }

    fun move(entry: FolderEntry){
        buffer = arrayListOf(entry)
        moving = true
    }


    private fun verifyIfInRootFolder(){
        isOnRoot = Environment.getExternalStorageDirectory().absolutePath.equals(currentDirectory?.absolutePath)
    }

    fun createFolder(name: String): Boolean{
        if(checkIfSameNameFileExists(name))
            throw FileAlreadyExists()
        currentDirectory?.let {
            val success = File(it.absolutePath, name).mkdirs()
            if(success)
                refresh()
            return success
        }
        throw FileCreationFailed()
    }

    fun createFile(name: String): File?{
        if(checkIfSameNameFileExists(name))
            throw FileAlreadyExists()
        currentDirectory?.let {
            val file = File(it.absolutePath, name)
            val success = file.createNewFile()
            if(success){
                refresh()
                return file
            }
        }
        throw FileCreationFailed()
    }

    private fun checkIfSameNameFileExists(name: String): Boolean{
        return checkIfSameNameFileExists(FolderEntry(File(currentDirectory?.file, name)))
    }

    private fun checkIfSameNameFileExists(entry: FolderEntry): Boolean{
        var sameNameEntry: FolderEntry? = null
        for (e in contents){
            if (e.name.equals(entry.name)){
                sameNameEntry = e
                break
            }
        }

        return sameNameEntry != null && sameNameEntry.absolutePath.equals(entry.absolutePath)

    }

    fun hasActiveBuffer() = buffer.isNotEmpty()

}