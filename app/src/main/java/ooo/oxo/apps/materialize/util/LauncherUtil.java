/*
 * Materialize - Materialize all those not material
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.apps.materialize.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

public class LauncherUtil {
    public static String saveIconFile(String folder, String name, Bitmap icon) {
        String result = "";

        FileOutputStream fileOutputStream = null;

        String path = Environment.getExternalStorageDirectory().toString();
        path += folder + "/";

        try {
            String iconPath = path + "/.nomedia";

            File file = new File(iconPath);

            if (file.exists())
                file.delete();

            file.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File filePath = new File(path);

            if (!filePath.exists())
                filePath.mkdir();

            String iconPath = path + name + ".png";

            File file = new File(iconPath);

            if (file.exists())
                file.delete();

            file.createNewFile();

            fileOutputStream = new FileOutputStream(file);
            icon.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            result = iconPath;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null)
                {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public static void saveComponentName(String folder, ComponentName component, String fileName) {
        String path = Environment.getExternalStorageDirectory().toString();
        path += folder + "/appfilter.txt";

        try {
            FileWriter fw = new FileWriter(path ,true);
            fw.write(String.format("    <item component=\"%s\" drawable=\"%s\" />\n", component.toString(), fileName));
            fw.close();
        } catch(IOException e)
        {
            e.printStackTrace();

            //Toast.makeText(act,e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void installShortcut(Context context, Intent shortcut, String label, Bitmap icon) {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcut);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, label);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon);
        context.sendBroadcast(intent);
    }

    @Nullable
    public static String resolveLauncherApp(Context context) {
        try {
            return context.getPackageManager().resolveActivity(
                    new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                    PackageManager.MATCH_DEFAULT_ONLY).activityInfo.applicationInfo.packageName;
        } catch (Exception e) {
            return null;    // 日了狗了
        }
    }

}
