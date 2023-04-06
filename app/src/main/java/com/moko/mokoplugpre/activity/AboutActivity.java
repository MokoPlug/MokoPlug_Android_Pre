package com.moko.mokoplugpre.activity;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.moko.mokoplugpre.BuildConfig;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.databinding.ActivityAboutPreBinding;
import com.moko.mokoplugpre.utils.Utils;


public class AboutActivity extends BaseActivity<ActivityAboutPreBinding> {
    @Override
    protected void onCreate() {
        int deviceType = getIntent().getIntExtra("deviceType", 0);
        if (deviceType == 1) {
            mBind.ivLogo.setImageResource(R.drawable.ic_115_pre);
        }
        if (deviceType == 2) {
            mBind.ivLogo.setImageResource(R.drawable.ic_116_pre);
        }
        if (!BuildConfig.IS_LIBRARY) {
            mBind.tvSoftVersion.setText(String.format(getString(R.string.version_info), Utils.getVersionInfo(this)));
        }
    }

    @Override
    protected ActivityAboutPreBinding getViewBinding() {
        return ActivityAboutPreBinding.inflate(getLayoutInflater());
    }

    public void openURL(View view) {
        if (isWindowLocked())
            return;
        Uri uri = Uri.parse("https://" + getString(R.string.company_website));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void back(View view) {
        finish();
    }
}
