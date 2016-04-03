package it.polimi.dima.bookshare.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import it.polimi.dima.bookshare.tables.Book;
import it.polimi.dima.bookshare.R;
import it.polimi.dima.bookshare.activities.MyBookDetail;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private List<Book> mBooks;
    private Context context;

    public LibraryAdapter(List<Book> mBooks, Context context) {

        this.mBooks = mBooks;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_library, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final Book book = mBooks.get(position);

        Picasso.with(context).load(book.getImgURL()).into(holder.mImage);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, MyBookDetail.class);

                intent.putExtra("title",book.getTitle());
                intent.putExtra("author",book.getAuthor());
                intent.putExtra("description",book.getDescription());
                intent.putExtra("pageCount",book.getPageCount());
                intent.putExtra("imgURL",book.getImgURL());
                intent.putExtra("isbn",book.getIsbn());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ImageView mImage;
        public final TextView mTitle;

        public ViewHolder(View view) {

            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.card_image);
            mTitle = (TextView) view.findViewById(R.id.card_title);

        }
    }
}
