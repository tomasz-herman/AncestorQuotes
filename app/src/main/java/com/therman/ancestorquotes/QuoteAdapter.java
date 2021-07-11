package com.therman.ancestorquotes;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> implements Filterable {
    private ArrayList<Quote> allQuotes;
    private ArrayList<Quote> quotes;
    private final Context context;
    private final View view;

    public QuoteAdapter(Context context, View view, ArrayList<Quote> quotes) {
        this.quotes = quotes;
        this.allQuotes = new ArrayList<>(quotes);
        this.context = context;
        this.view = view;
    }

    public static class Provider extends ContentProvider {

        private final static String LOG_TAG = "CustomContentProvider";

        private static final String[] COLUMNS = {
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            /**
             * Source: {@link FileProvider#query(Uri, String[], String, String[], String)} .
             */
            if (projection == null) {
                projection = COLUMNS;
            }

            final AssetManager am = getContext().getAssets();
            final String path = getRelativePath(uri);
            long fileSize = 0;
            try {
                final AssetFileDescriptor afd = am.openFd(path);
                fileSize = afd.getLength();
                afd.close();
            } catch(IOException e) {
                Log.e(LOG_TAG, "Can't open asset file", e);
            }

            final String[] cols = new String[projection.length];
            final Object[] values = new Object[projection.length];
            int i = 0;
            for (String col : projection) {
                if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                    cols[i] = OpenableColumns.DISPLAY_NAME;
                    values[i++] = uri.getLastPathSegment();
                } else if (OpenableColumns.SIZE.equals(col)) {
                    cols[i] = OpenableColumns.SIZE;
                    values[i++] = fileSize;
                }
            }

            final MatrixCursor cursor = new MatrixCursor(cols, 1);
            cursor.addRow(values);
            return cursor;
        }

        @Override
        public String getType(Uri uri) {
            /**
             * Source: {@link FileProvider#getType(Uri)} .
             */
            final String file_name = uri.getLastPathSegment();
            final int lastDot = file_name.lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = file_name.substring(lastDot + 1);
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) {
                    return mime;
                }
            }

            return "application/octet-stream";
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
            final AssetManager am = getContext().getAssets();
            final String path = getRelativePath(uri);
            if(path == null) {
                throw new FileNotFoundException();
            }
            AssetFileDescriptor afd = null;
            try {
                afd = am.openFd(path);
            } catch(IOException e) {
                Log.e(LOG_TAG, "Can't open asset file", e);
            }
            return afd;
        }

        private String getRelativePath(Uri uri) {
            String path = uri.getPath();
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }
            return path;
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
            itemView.setOnLongClickListener(this::showOptionsMenu);
            ivFavorite.setOnClickListener(this::setFavorite);
        }

        private void playQuote(View v) {
            Quote quote = (Quote) v.getTag();
            AncestorQuotes.playQuote(quote);
        }

        private void setRingtone(View v, int type) {
            Uri theUri = Uri.parse("content://com.therman.ancestorquotes/" + ((Quote)v.getTag()).getSourceOrAltSource() + ".wav.mp3");

            RingtoneManager.setActualDefaultRingtoneUri(context,
                    type, theUri);
        }

        private void saveLocally(View v) {
            Quote quote = (Quote)v.getTag();
            Uri sourceUri = Uri.parse("content://com.therman.ancestorquotes/" + quote.getSourceOrAltSource() + ".wav.mp3");

            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.AudioColumns.DISPLAY_NAME, quote.getText().substring(0, Math.min(quote.getText().length(), 28)) + ".mp3");
            values.put(MediaStore.Audio.AudioColumns.MIME_TYPE, "audio/mpeg");
            values.put(MediaStore.Audio.AudioColumns.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES);
            Uri destUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                bis = new BufferedInputStream(resolver.openInputStream(sourceUri));
                bos = new BufferedOutputStream(resolver.openOutputStream(destUri));
                byte[] buf = new byte[1024];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while(bis.read(buf) != -1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void setFavorite(View v) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            ArrayList<Quote> favorites = AncestorQuotes.database.getFavorites();
            String source = (String) v.getTag();
            if (prefs.contains(source)) {
                ivFavorite.setImageResource(R.drawable.torch);
                prefs.edit().remove(source).apply();
                for (int i = 0; i < favorites.size(); i++) {
                    if (favorites.get(i).getSource().equals(source)) {
                        favorites.remove(i);
                        break;
                    }
                }
                showToast("Removed from favorites!", R.drawable.torch);
            } else {
                ivFavorite.setImageResource(R.drawable.torch_lit);
                prefs.edit().putBoolean(source, true).apply();
                for (int i = 0; i < allQuotes.size(); i++) {
                    if(allQuotes.get(i).getSource().equals(source)){
                        favorites.add(allQuotes.get(i));
                        break;
                    }
                }
                showToast("Added to favorites!", R.drawable.torch_lit);
            }
        }

        private boolean showOptionsMenu(View v) {
            PopupMenu menu = new PopupMenu(context, v);
            MenuInflater inflater = menu.getMenuInflater();
            inflater.inflate(R.menu.menu_quote, menu.getMenu());
            menu.show();
            menu.setOnMenuItemClickListener(item -> onOptionsMenuItemClick(item, v));
            return true;
        }

        public boolean onOptionsMenuItemClick(MenuItem item, View v) {
            int itemId = item.getItemId();
            if (itemId == R.id.iPlayQuote) {
                playQuote(v);
                return true;
            } else if (itemId == R.id.iShareQuote) {
                shareDialog(v);
                return true;
            } else if (itemId == R.id.iShareQuoteUrl) {
                shareDialogUrl(v);
                return true;
            } else if (itemId == R.id.iSetAsRingtone) {
                setRingtone(v, RingtoneManager.TYPE_RINGTONE);
                return true;
            } else if (itemId == R.id.iSetAsNotification) {
                setRingtone(v, RingtoneManager.TYPE_NOTIFICATION);
                return true;
            } else if (itemId == R.id.iSetAsAlarm) {
                setRingtone(v, RingtoneManager.TYPE_ALARM);
                return true;
            } else if (itemId == R.id.iSaveLocally) {
                saveLocally(v);
                return true;
            }
            return false;
        }

        private void shareDialog(View v) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Uri theUri = Uri.parse("content://com.therman.ancestorquotes/" + ((Quote)v.getTag()).getSourceOrAltSource() + ".wav.mp3");
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, theUri);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, ((Quote)v.getTag()).getSource());
            context.startActivity(shareIntent);
        }

        private void shareDialogUrl(View v) {
            Quote quote = (Quote) v.getTag();
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Sharing URL");
            i.putExtra(Intent.EXTRA_TEXT, "https://raw.githubusercontent.com/tomasz-herman/AncestorQuotes/master/app/src/main/assets/" + quote.getSource() + ".wav.mp3");
            context.startActivity(Intent.createChooser(i, "Share URL"));
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
