/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.List;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;

    private LinearLayout root;
    private LinearLayout linearLayout;
    private RecyclerView recyclerView;

    private void edgeToEdge(boolean windowLightStatusBar) {
        if (windowLightStatusBar) {
            EdgeToEdge.enable(
                    this,
                    SystemBarStyle.light(
                            ContextCompat.getColor(this, android.R.color.transparent),
                            ContextCompat.getColor(this, android.R.color.transparent)
                    )
            );
        } else {
            EdgeToEdge.enable(
                    this,
                    SystemBarStyle.dark(
                            ContextCompat.getColor(this, android.R.color.transparent)
                    )
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        edgeToEdge(false);

        setContentView(R.layout.activity_main);

        this.root = this.findViewById(R.id.root);
        this.linearLayout = this.findViewById(R.id.linear_layout);
        this.recyclerView = findViewById(R.id.recyclerview);

        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);
        findViewById(R.id.only_gif).setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());

        setOnApplyWindowInsetsListener();
    }

    private void setOnApplyWindowInsetsListener() {
        final Rect initialLinearLayoutPadding = new Rect(
                linearLayout.getPaddingLeft(),
                linearLayout.getPaddingTop(),
                linearLayout.getPaddingRight(),
                linearLayout.getPaddingBottom()
        );

        final Rect initialRecyclerViewPadding = new Rect(
                recyclerView.getPaddingLeft(),
                recyclerView.getPaddingTop(),
                recyclerView.getPaddingRight(),
                recyclerView.getPaddingBottom()
        );

        // 2. Apply a listener to handle window insets for all orientations
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            // Get the insets for the system bars (status bar, navigation bar)
            Insets theInsets = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            linearLayout.setPadding(
                    initialLinearLayoutPadding.left + theInsets.left,
                    initialLinearLayoutPadding.top + theInsets.top,
                    initialLinearLayoutPadding.right + theInsets.right,
                    initialLinearLayoutPadding.bottom + 0
            );

            recyclerView.setPadding(
                    initialRecyclerViewPadding.left + theInsets.left,
                    initialRecyclerViewPadding.top + 0,
                    initialRecyclerViewPadding.right + theInsets.right,
                    initialRecyclerViewPadding.bottom + theInsets.bottom
            );

            // Return the insets to allow the system to continue processing them
            return insets;
        });
    }

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
        final String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                    .show();
        }

        startAction(v);
    }
    // </editor-fold>

    private void startAction(View v) {
        final int id = v.getId();

       if (id == R.id.zhihu) {
           Matisse.from(SampleActivity.this)
                   .choose(MimeType.ofImage(), false)
                   .countable(true)
                   .capture(true)
                   .captureStrategy(
                           new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test"))
                   .maxSelectable(9)
                   .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                   .gridExpectedSize(
                           getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                   .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                   .thumbnailScale(0.85f)
                   .imageEngine(new GlideEngine())
                   .setOnSelectedListener((uriList, pathList) -> {
                       Log.e("onSelected", "onSelected: pathList=" + pathList);
                   })
                   .showSingleMediaType(true)
                   .originalEnable(true)
                   .maxOriginalSize(10)
                   .autoHideToolbarOnSingleTap(true)
                   .setOnCheckedListener(isChecked -> {
                       Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                   })
                   .forResult(REQUEST_CODE_CHOOSE);
       } else if (id == R.id.dracula) {
           Matisse.from(SampleActivity.this)
                   .choose(MimeType.ofImage())
                   .theme(com.zhihu.matisse.R.style.Matisse_Dracula)
                   .countable(false)
                   .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                   .maxSelectable(9)
                   .originalEnable(true)
                   .maxOriginalSize(10)
                   .imageEngine(new PicassoEngine())
                   .forResult(REQUEST_CODE_CHOOSE);
       } else if (id == R.id.only_gif) {
           Matisse.from(SampleActivity.this)
                   .choose(MimeType.of(MimeType.GIF), false)
                   .countable(true)
                   .maxSelectable(9)
                   .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                   .gridExpectedSize(
                           getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                   .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                   .thumbnailScale(0.85f)
                   .imageEngine(new GlideEngine())
                   .showSingleMediaType(true)
                   .originalEnable(true)
                   .maxOriginalSize(10)
                   .autoHideToolbarOnSingleTap(true)
                   .forResult(REQUEST_CODE_CHOOSE);
       }
        mAdapter.setData(null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Matisse.obtainOriginalState(data)));
        }
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = (TextView) contentView.findViewById(R.id.uri);
                mPath = (TextView) contentView.findViewById(R.id.path);
            }
        }
    }

}
