package com.moko.mokoplugpre.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.mokoplugpre.BuildConfig;
import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.R2;
import com.moko.mokoplugpre.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AboutActivity extends BaseActivity {

    @BindView(R2.id.tv_soft_version)
    TextView tvSoftVersion;
    @BindView(R2.id.iv_logo)
    ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_pre);
        ButterKnife.bind(this);
        int deviceType = getIntent().getIntExtra("deviceType", 0);
        if (deviceType == 1) {
            ivLogo.setImageResource(R.drawable.ic_115_pre);
        }
        if (deviceType == 2) {
            ivLogo.setImageResource(R.drawable.ic_116_pre);
        }
        if (!BuildConfig.IS_LIBRARY) {
            tvSoftVersion.setText(String.format(getString(R.string.version_info), Utils.getVersionInfo(this)));
        }
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
