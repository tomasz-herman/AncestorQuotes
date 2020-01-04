package com.therman.ancestorquotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> implements Filterable {
    private ArrayList<Quote> allQuotes;
    private ArrayList<Quote> quotes;
    private Context context;
    private MediaPlayer player;

    public QuoteAdapter(Context context, ArrayList<Quote> quotes) {
        this.quotes = quotes;
        this.allQuotes = new ArrayList<>(quotes);
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDialogFrom, tvDialogText;
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
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(quote.getSource());
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.setOnPreparedListener(MediaPlayer::start);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setFavorite(View v) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains((String) v.getTag())) {
                ivFavorite.setImageResource(R.drawable.unfavorite);
                prefs.edit().remove((String) v.getTag()).apply();
            } else {
                ivFavorite.setImageResource(R.drawable.favorite);
                prefs.edit().putBoolean((String) v.getTag(), true).apply();
            }
        }

        private boolean shareDialog(View v) {
            Quote quote = (Quote) v.getTag();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
            i.putExtra(Intent.EXTRA_TEXT, quote.getSource());
            context.startActivity(Intent.createChooser(i, "Share URL"));
            return true;
        }
    }

    public void replaceData(ArrayList<Quote> quotes) {
        this.quotes = quotes;
        this.allQuotes = new ArrayList<>(quotes);
        notifyDataSetChanged();
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
            holder.ivFavorite.setImageResource(R.drawable.favorite);
        else holder.ivFavorite.setImageResource(R.drawable.unfavorite);
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
