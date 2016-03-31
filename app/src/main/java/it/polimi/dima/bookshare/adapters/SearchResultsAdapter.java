package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.polimi.dima.bookshare.Book;
import it.polimi.dima.bookshare.R;

/**
 * Created by matteo on 31/03/16.
 */
public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private ArrayList<Book> mBooks;
    private Context context;

    public SearchResultsAdapter(ArrayList<Book> mBooks, Context context) {
        this.mBooks = mBooks;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Typeface aller = Typeface.createFromAsset(context.getAssets(), "fonts/Aller_Rg.ttf");
        Book book = mBooks.get(position);
        Picasso.with(context).load(book.getImgURL()).into(holder.mImage);
        holder.mTitle.setText(book.getTitle());
        holder.mTitle.setTypeface(aller);
        holder.mAuthor.setText(book.getAuthor());
        holder.mAuthor.setTypeface(aller);

    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ImageView mImage;
        public final TextView mTitle,mAuthor;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);
            mTitle = (TextView) view.findViewById(R.id.card_title);
            mAuthor = (TextView) view.findViewById(R.id.card_author);

        }
    }
}
