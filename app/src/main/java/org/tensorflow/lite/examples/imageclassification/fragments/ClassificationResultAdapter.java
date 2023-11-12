package org.tensorflow.lite.examples.imageclassification.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.tensorflow.lite.examples.imageclassification.databinding.ItemClassificationResultBinding;
import org.tensorflow.lite.support.label.Category;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ClassificationResultAdapter
        extends RecyclerView.Adapter<ClassificationResultAdapter.ViewHolder> {
    private static final String NO_VALUE = "--";
    private final List<Category> categories = new ArrayList<>();
    private int adapterSize = 0;
    private final Context context;
    private TextToSpeech textToSpeech;
    private String lastSpokenLabel = "";

    public ClassificationResultAdapter(Context context) {
        this.context = context;

        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TextToSpeech", "Language is not supported.");
                }
            } else {
                Log.e("TextToSpeech", "Initialization failed.");
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateResults(List<Category> categories) {
        List<Category> sortedCategories = new ArrayList<>(categories);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Collections.sort(sortedCategories, Comparator.comparingInt(Category::getIndex));
        }
        this.categories.clear();
        int min = Math.min(sortedCategories.size(), adapterSize);
        this.categories.addAll(sortedCategories.subList(0, min));
        if (!this.categories.isEmpty() && this.categories.get(0) != null) {
            String currentLabel = this.categories.get(0).getLabel();
            if (!currentLabel.equals(lastSpokenLabel)) {
                speak(currentLabel);
                lastSpokenLabel = currentLabel;
            }
        }
        notifyDataSetChanged();
    }

    public void updateAdapterSize(int size) {
        adapterSize = size;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemClassificationResultBinding binding = ItemClassificationResultBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLabel;
        private final TextView tvScore;

        public ViewHolder(@NonNull ItemClassificationResultBinding binding) {
            super(binding.getRoot());
            tvLabel = binding.tvLabel;
            tvScore = binding.tvScore;
        }

        public void bind(Category category) {
            if (category != null) {
                tvLabel.setText(category.getLabel());
                tvScore.setText(String.format(Locale.US, "%.2f", category.getScore()));
            } else {
                tvLabel.setText(NO_VALUE);
                tvScore.setText(NO_VALUE);
            }
        }
    }

    private void speak(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
