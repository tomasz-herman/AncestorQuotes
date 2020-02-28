package com.therman.ancestorquotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> implements Filterable {
    private ArrayList<Quote> allQuotes;
    private ArrayList<Quote> quotes;
    private Context context;
    private MediaPlayer player;
    private View view;

    public QuoteAdapter(Context context, View view, ArrayList<Quote> quotes) {
        this.quotes = quotes;
        this.allQuotes = new ArrayList<>(quotes);
        this.context = context;
        this.view = view;
    }

    public static class Provider extends ContentProvider {

        public Provider(){

        }

        @Override
        public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
            AssetManager am = getContext().getAssets();
            String file_name = uri.getLastPathSegment();
            if(file_name == null)
                throw new FileNotFoundException();
            AssetFileDescriptor afd = null;
            try {
                afd = am.openFd(file_name);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return afd;//super.openAssetFile(uri, mode);
        }

        @Override
        public boolean onCreate() {
            return false;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDialogText;
        ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDialogText = itemView.findViewById(R.id.tvQuoteText);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
            itemView.setOnClickListener(this::playQuote);
            itemView.setOnLongClickListener(this::shareDialog);
            ivFavorite.setOnClickListener(this::setFavorite);
        }

        private void playQuote(View v) {
            Quote quote = (Quote) v.getTag();
            try {
                if(player != null) {
                    player.reset();
                    player.release();
                }
            } catch (IllegalStateException ignored) {}
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                AssetFileDescriptor fd = context.getAssets().openFd(quote.getSourceOrAltSource() + ".wav.mp3");
                player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                player.setOnCompletionListener(MediaPlayer::release);
                player.setOnPreparedListener(MediaPlayer::start);
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setFavorite(View v) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains((String) v.getTag())) {
                ivFavorite.setImageResource(R.drawable.torch);
                prefs.edit().remove((String) v.getTag()).apply();
                showToast("Removed from favorites!", R.drawable.torch);
            } else {
                ivFavorite.setImageResource(R.drawable.torch_lit);
                prefs.edit().putBoolean((String) v.getTag(), true).apply();
                showToast("Added to favorites!", R.drawable.torch_lit);
            }
        }

        private boolean shareDialog(View v) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Uri theUri = Uri.parse("content://com.therman.ancestorquotes/" + ((Quote)v.getTag()).getSourceOrAltSource() + ".wav.mp3");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, theUri);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,((Quote)v.getTag()).getSource());
            context.startActivity(shareIntent);
            return true;
        }
    }

    public void replaceData(ArrayList<Quote> quotes) {
        this.quotes = quotes;
        this.allQuotes = new ArrayList<>(quotes);
        notifyDataSetChanged();
    }

    public void showToast(String message, int image){
        View toastView = LayoutInflater.from(context).inflate(R.layout.toast, view.findViewById(R.id.llToast));
        TextView tvToast = toastView.findViewById(R.id.tvToast);
        ImageView ivToast = toastView.findViewById(R.id.ivToast);
        ivToast.setImageResource(image);
        tvToast.setText(message);
        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(toastView);
        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.show();
    }

    @NonNull
    @Override
    public QuoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quote_layout, parent, false);
        return new QuoteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuoteAdapter.ViewHolder holder, int position) {
        holder.itemView.setTag(quotes.get(position));
        holder.ivFavorite.setTag(quotes.get(position).getSource());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(quotes.get(position).getSource()))
            holder.ivFavorite.setImageResource(R.drawable.torch_lit);
        else holder.ivFavorite.setImageResource(R.drawable.torch);
        holder.tvDialogText.setText(quotes.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return quotes.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<Quote> filteredQuotes;
                if (constraint == null || constraint.length() == 0)
                    filteredQuotes = new ArrayList<>(allQuotes);
                else {
                    String pattern = constraint.toString().toLowerCase().trim();
                    filteredQuotes = new ArrayList<>();
                    for (Quote quote : allQuotes) {
                        if (quote.getText().toLowerCase().contains(pattern))
                            filteredQuotes.add(quote);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredQuotes;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                quotes = (ArrayList<Quote>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
