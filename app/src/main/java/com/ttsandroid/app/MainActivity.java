package com.ttsandroid.app;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ttsandroid.domain.RenderStyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MainActivity extends Activity {
    private static final int POLL_INTERVAL_MS = 200;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private RenderViewModel viewModel;

    private EditText textInput;
    private Spinner styleSelector;
    private Button generateButton;
    private Button cancelButton;
    private TextView statusView;
    private TextView progressView;
    private TextView outputPathView;
    private ProgressBar progressBar;

    private final Runnable statePoller = new Runnable() {
        @Override
        public void run() {
            if (viewModel == null) {
                return;
            }

            RenderUiState state = viewModel.getState();
            renderState(state);

            if (state.isRendering()) {
                mainHandler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = AppContainer.create(resolveOutputDirectory(), this::isDeviceCharging);

        setContentView(createContentView());
        bindEvents();
        renderState(viewModel.getState());
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(statePoller);
        if (viewModel != null) {
            viewModel.shutdown();
        }
        super.onDestroy();
    }

    private LinearLayout createContentView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dpToPx(16);
        container.setPadding(padding, padding, padding, padding);

        textInput = new EditText(this);
        textInput.setHint("German text");
        textInput.setMinLines(8);
        textInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        container.addView(textInput, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        styleSelector = new Spinner(this);
        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Neutral", "Expressive"}
        );
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        styleSelector.setAdapter(styleAdapter);
        container.addView(styleSelector, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        generateButton = new Button(this);
        generateButton.setText("Generate");
        container.addView(generateButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        cancelButton = new Button(this);
        cancelButton.setText("Cancel");
        container.addView(cancelButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        statusView = new TextView(this);
        container.addView(statusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(1);
        container.addView(progressBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        progressView = new TextView(this);
        container.addView(progressView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        outputPathView = new TextView(this);
        container.addView(outputPathView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return container;
    }

    private void bindEvents() {
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.onTextChanged(s == null ? "" : s.toString());
                renderState(viewModel.getState());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        styleSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                RenderStyle style = position == 1 ? RenderStyle.EXPRESSIVE : RenderStyle.NEUTRAL;
                viewModel.onStyleSelected(style);
                renderState(viewModel.getState());
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        generateButton.setOnClickListener(v -> {
            viewModel.onGenerateClicked();
            startStatePolling();
            renderState(viewModel.getState());
        });

        cancelButton.setOnClickListener(v -> {
            viewModel.onCancelClicked();
            startStatePolling();
            renderState(viewModel.getState());
        });
    }

    private void startStatePolling() {
        mainHandler.removeCallbacks(statePoller);
        mainHandler.post(statePoller);
    }

    private void renderState(RenderUiState state) {
        boolean hasText = !state.text().trim().isEmpty();

        generateButton.setEnabled(!state.isRendering() && hasText);
        cancelButton.setEnabled(state.isRendering());
        textInput.setEnabled(!state.isRendering());
        styleSelector.setEnabled(!state.isRendering());

        if (styleSelector.getSelectedItemPosition() != styleToIndex(state.style())) {
            styleSelector.setSelection(styleToIndex(state.style()));
        }

        statusView.setText("Status: " + state.statusMessage());

        int totalChunks = Math.max(0, state.totalChunks());
        int completedChunks = Math.max(0, state.completedChunks());

        progressBar.setMax(Math.max(1, totalChunks));
        progressBar.setProgress(Math.min(progressBar.getMax(), completedChunks));
        progressView.setText("Progress: " + completedChunks + "/" + totalChunks);

        if (state.outputPath() == null || state.outputPath().isBlank()) {
            outputPathView.setText("Output: -");
        } else {
            outputPathView.setText("Output: " + state.outputPath());
        }
    }

    private int styleToIndex(RenderStyle style) {
        return style == RenderStyle.EXPRESSIVE ? 1 : 0;
    }

    private Path resolveOutputDirectory() {
        Path outputDirectory = getFilesDir().toPath().resolve("renders");
        try {
            return Files.createDirectories(outputDirectory);
        } catch (IOException ignored) {
            return outputDirectory;
        }
    }

    private boolean isDeviceCharging() {
        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryStatus == null) {
            return false;
        }

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
