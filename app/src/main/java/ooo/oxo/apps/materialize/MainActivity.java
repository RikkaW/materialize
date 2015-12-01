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

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import ooo.oxo.apps.materialize.databinding.MainActivityBinding;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends RxAppCompatActivity
        implements AppInfoAdapter.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private final Observable<AppInfo> loading = Observable
            .defer(() -> {
                Intent intent = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER);
                return Observable.from(getPackageManager().queryIntentActivities(intent, 0));
            })
            .map(resolve -> AppInfo.from(resolve.activityInfo, getPackageManager()))
            .filter(app -> app != null)
            .filter(app -> !app.component.getPackageName().equals(BuildConfig.APPLICATION_ID))
            .filter(app -> !app.component.getPackageName().startsWith("com.android."))
            .filter(app -> !app.component.getPackageName().startsWith("com.google."))
            .filter(app -> !app.component.getPackageName().startsWith("org.cyanogenmod."))
            .filter(app -> !app.component.getPackageName().startsWith("com.cyanogenmod."))
            .filter(app -> !app.component.getPackageName().startsWith("me.xingrz."))
            .filter(app -> !app.component.getPackageName().startsWith("ooo.oxo."))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .cache();

    private AppInfoAdapter apps;

    private SearchPanelController searchPanelController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.main_activity);

        setSupportActionBar(binding.toolbar);

        searchPanelController = new SearchPanelController(binding.searchBar);

        SearchMatcher searchMatcher = new SearchMatcher(() -> searchPanelController.getKeyword().getText().toString());

        binding.apps.setAdapter(apps = new AppInfoAdapter(this, Glide.with(this),
                searchMatcher, this));

        RxTextView.afterTextChangeEvents(searchPanelController.getKeyword())
                .compose(bindToLifecycle())
                .subscribe(avoid -> apps.data.applyFilter());

        loading.compose(bindToLifecycle())
                .doOnCompleted(() -> binding.apps.smoothScrollToPosition(0))
                .subscribe(apps.data::addWithIndex);
    }

    @Override
    public void onItemClick(AppInfoAdapter.ViewHolder holder) {
        AppInfo app = apps.data.get(holder.getLayoutPosition());

        Intent intent = new Intent(this, AdjustActivity.class);
        intent.putExtra("activity", app.activityInfo);

        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(
                holder.itemView, 0, 0, holder.itemView.getWidth(), holder.itemView.getHeight());

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.search:
                searchPanelController.open();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchPanelController.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
