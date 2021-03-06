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

package ooo.oxo.apps.materialize;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.HashMap;

import ooo.oxo.apps.materialize.databinding.AdjustActivityBinding;
import ooo.oxo.apps.materialize.graphics.InfiniteDrawable;
import ooo.oxo.apps.materialize.graphics.TransparencyDrawable;
import ooo.oxo.apps.materialize.util.LauncherUtil;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AdjustActivity extends RxAppCompatActivity {

    private static final boolean SUPPORT_MIPMAP = Build.VERSION.SDK_INT >= 18;

    /**
     * Icon size in pixel since Android 4.3 (18) with mipmaps support
     * It's the actual size of drawables in drawable-anydpi-v18 folder
     */
    private static final int LAUNCHER_SIZE_MIPMAP = 192;

    private final Observable<AppInfo> resolving = Observable
            .defer(() -> Observable.just((ActivityInfo) getIntent().getParcelableExtra("activity")))
            .map(activity -> AppInfo.from(activity, getPackageManager()))
            .filter(app -> app != null)
            .filter(AppInfo::resolveIcon)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .cache();

    private AdjustActivityBinding binding;

    private AdjustViewModel adjustment = new AdjustViewModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.adjust_activity);

        binding.setAdjust(adjustment);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        ActivityInfo activityInfo = getIntent().getParcelableExtra("activity");

        if (sharedPref.getBoolean("pef_save_file_name", false)) {
            binding.tableRowEdit.setVisibility(View.GONE);
        }
        else
        {
            String packageName = activityInfo.packageName;
            String[] packageNameArray = packageName.split("\\.");

            if (packageNameArray.length > 1)
                binding.editText.setText(packageNameArray[packageNameArray.length - 1]);
            else
                binding.editText.setText(packageNameArray[packageNameArray.length]);

            binding.tableRowEdit.setVisibility(View.VISIBLE);
        }

        if (!sharedPref.getBoolean("pef_save_file", false)) {
            binding.ok.setText(R.string.adjust_ok);
            binding.tableRowEdit.setVisibility(View.GONE);
        }
        else {
            binding.ok.setText(R.string.adjust_save_file);
            binding.tableRowEdit.setVisibility(View.VISIBLE);
        }


        binding.setTransparency(new TransparencyDrawable(
                getResources(), R.dimen.transparency_grid_size));

        resolving.compose(bindToLifecycle())
                .subscribe(binding::setApp);

        resolving.subscribeOn(Schedulers.computation())
                .map(app -> InfiniteDrawable.from(app.icon))
                .filter(infinite -> infinite != null)
                .compose(bindToLifecycle())
                .subscribe(adjustment::setInfinite);

        RxView.clicks(binding.cancel)
                .compose(bindToLifecycle())
                .subscribe(avoid -> {
                    setResult(RESULT_CANCELED);
                    supportFinishAfterTransition();
                });


        int size = SUPPORT_MIPMAP ? LAUNCHER_SIZE_MIPMAP
                : getResources().getDimensionPixelSize(R.dimen.launcher_size);

        Observable<Bitmap> renders = Observable.just(binding.result.getComposite())
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .map(compose -> {
                    Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                    compose.drawTo(new Canvas(result), SUPPORT_MIPMAP);
                    return result;
                });

        RxView.clicks(binding.ok)
                .filter(avoid -> binding.getApp() != null)
                .flatMap(avoid -> renders)
                .compose(bindToLifecycle())
                .subscribe(result -> {

                    boolean saveFile = sharedPref.getBoolean("pef_save_file", false);
                    boolean saveUseAppName = sharedPref.getBoolean("pef_save_file_name", false);

                    if (!saveFile)
                    {
                        LauncherUtil.installShortcut(this,
                                binding.getApp().getIntent(), binding.getApp().label, result);

                        Toast.makeText(this, R.string.done, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String folder = sharedPref.getString("pef_save_loc", "/icons");
                        String fileName = binding.editText.getText().toString();

                        if (saveUseAppName)
                            fileName = binding.getApp().label;

                        String toast = LauncherUtil.saveIconFile(folder, fileName, result);

                        if (!toast.equals("")) {
                            if (sharedPref.getBoolean("pef_save_txt", false)) {
                                if (saveUseAppName)
                                    fileName = "";

                                LauncherUtil.saveComponentName(folder, binding.getApp().component, fileName);
                            }

                            Toast.makeText(this, String.format(getString(R.string.done_save_file), toast), Toast.LENGTH_SHORT).show();
                        }
                    }

                    setResult(RESULT_OK);
                    supportFinishAfterTransition();
                });
    }

    private HashMap<String, String> makeEvent() {
        HashMap<String, String> event = new HashMap<>();
        event.put("shape", adjustment.getShape().name());
        event.put("color", mapColorName(binding.colors.getCheckedRadioButtonId()));
        return event;
    }

    private String mapColorName(@IdRes int radio) {
        switch (radio) {
            case R.id.color_white:
                return "white";
            case R.id.color_infinite:
                return "infinite";
            default:
                return null;
        }
    }
}
