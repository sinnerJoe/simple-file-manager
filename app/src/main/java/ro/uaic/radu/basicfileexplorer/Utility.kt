package ro.uaic.radu.basicfileexplorer

import java.io.File
import java.lang.Exception

fun getSDPath() : String{
    val strPaths = arrayOf("/storage/sdcard1", "/storage/extsdcard",
        "/storage/sdcard0/external_sdcard", "/mnt/extsdcard",
        "/mnt/sdcard/external_sd", "/mnt/external_sd",
        "/mnt/media_rw/sdcard1", "/removable/microsd", "/mnt/emmc",
        "/storage/external_SD", "/storage/ext_sd",
        "/storage/removable/sdcard1", "/data/sdext", "/data/sdext2",
        "/data/sdext3", "/data/sdext4", "/emmc", "/sdcard/sd",
        "/mnt/sdcard/bpemmctest", "/mnt/sdcard/_ExternalSD",
        "/mnt/sdcard-ext", "/mnt/Removable/MicroSD",
        "/Removable/MicroSD", "/mnt/external1", "/mnt/extSdCard",
        "/mnt/extsd", "/mnt/usb_storage", "/mnt/extSdCard",
        "/mnt/UsbDriveA", "/mnt/UsbDriveB")

    for (path in strPaths){
        val f = File(path)
        if (f.exists() && f.isDirectory){
            return path
        }
    }

    throw Exception("No external storage!")
}