package ro.uaic.radu.basicfileexplorer

import android.os.Environment
import java.io.File
import java.net.FileNameMap

class FolderEntry{
    var file: File
    private set(value){
        field = value
    }

    val name: String
    get() = file.name


    constructor(file: File){
        this.file = file
    }

    fun isDirectory(): Boolean {
        return file.isDirectory
    }

    fun isFile(): Boolean {
        return file.isFile
    }

    fun exists(): Boolean {
        return file.exists()
    }

    fun delete(): Boolean  {
        return file.delete()
    }

    fun renameTo(name: String): Boolean{
        val target = File(file.parentFile, name)
        val result = file.renameTo(target)
        if (result) {
            this.file = target
        }
        return result
    }

    fun moveTo(folder: FolderEntry, name:String =  file.name): Boolean {
        return file.renameTo(File(folder.file, name))
    }

    fun copyTo(folder: FolderEntry, name: String = file.name): Boolean{
        return file.copyTo(File(folder.file, name)).exists()
    }

    fun copyToForced(folder:FolderEntry, name:String): Boolean{
        return file.copyTo(File(folder.file, name), true).exists()
    }

    fun moveToForced(folder:FolderEntry, name:String): Boolean{
        val target = File(folder.file, name)
        if(target.exists()) target.delete()
        return file.renameTo(target)
    }

    fun listFiles() = file.listFiles()

    val absolutePath: String
    get() = file.absolutePath

    val parent: FolderEntry
    get() = FolderEntry(file.parentFile)

}