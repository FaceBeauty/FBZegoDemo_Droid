package im.zego.commontools;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ZegoFileUtil {
    /**
     * Determine whether the folder of the specified directory exists,
     * if it does not exist, you need to create a new folder
     * 判断指定目录的文件夹是否存在，如果不存在则需要创建新的文件夹
     * @param fileName 指定目录  Specified directory
     * @return 返回创建结果 Return result: TRUE or FALSE
     */
    public static boolean fileIsExist(String fileName)
    {
        // Check whether the file exists
        File file=new File(fileName);
        if (file.exists())
            return true;
        else{
            // Make directory if it doesn't exist
            return file.mkdirs();
        }
    }

    /**
     * Save Bitmap
     *
     * @param name file name
     * @param bm  picture to save
     */
    public static void saveBitmap(String name, Bitmap bm, Context mContext) {
        Log.d("Save Bitmap", "Ready to save picture");
        // Specify the path to store the file
        // 指定我们想要存储文件的地址
        String TargetPath = mContext.getExternalFilesDir(null) + "/images/";
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            File saveFile = new File(TargetPath, name);

            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture has been saved to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
