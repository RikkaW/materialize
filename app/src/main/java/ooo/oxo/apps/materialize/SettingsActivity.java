package ooo.oxo.apps.materialize;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getFragmentManager().beginTransaction().replace(R.id.settings_container,
                new SettingsFragment()).commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }




    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        private SwitchPreference box;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (sharedPreferences.getBoolean("pef_save_file", false) && key.equals("pef_save_file")) {
                getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        private void getPermission(String permission)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                    {
                        requestPermissions(new String[]{permission}, 0);
                    }
                }
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case 0:
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(getContext(), R.string.write_file_denied, Toast.LENGTH_LONG)
                                    .show();
                        }

                        SharedPreferences.Editor editor = getPreferenceScreen().getSharedPreferences().edit();
                        editor.putBoolean("pef_save_file", false);
                        editor.apply();

                        box = (SwitchPreference) findPreference("pef_save_file");
                        box.setChecked(false);
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }

        /*@Override
        public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen, Preference preference)
        {
            String key = preference.getKey();

            switch (key) {
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }*/
    }


}
