package com.example.imagestore

import android.util.Log
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.FileInputStream

object FtpUploader {
    fun uploadImageToFtp(filePath: String) {
        val ftp = FTPClient()
        try {
            ftp.connect("grasimchemicals.xxatsolution.com")
            val success = ftp.login("grasimchemicals", "556Phn1*l")
            ftp.enterLocalPassiveMode()
            ftp.setFileType(FTP.BINARY_FILE_TYPE)

            if (!success) {
                Log.e("FTP", "Login failed!")
                return
            }

            val input = FileInputStream(filePath)
            val remotePath = "/ImageUpload/" + File(filePath).name
            val uploaded = ftp.storeFile(remotePath, input)

            input.close()
            ftp.logout()
            ftp.disconnect()

            if (uploaded) {
                Log.d("FTP", "✅ Image uploaded: $remotePath")
            } else {
                Log.e("FTP", "❌ Upload failed.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FTP", "Error: ${e.message}")
        }
    }
}