package com.example.mohamed.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.store.data.ProductContract.ProductEntry;
import com.example.mohamed.store.data.ProductDbHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductAdapterViewHolder>{

    /* The context we use to utility methods, app resources and layout inflaters */
    private Context mContext;

    final private ProductAdapterOnClickListener mClickListener;

    private Cursor mCursor;


    /**
     * Below, we've defined an interface to handle clicks on items within this Adapter. In the
     * constructor of our ForecastAdapter, we receive an instance of a class that has implemented
     * said interface. We store that instance in this variable to call the onClick method whenever
     * an item is clicked in the list.
     */

    public interface ProductAdapterOnClickListener{
        void onClick(int id);
    }

    public ProductAdapter(Context mContext, ProductAdapterOnClickListener mClickListener) {
        this.mContext = mContext;
        this.mClickListener = mClickListener;
    }

    @Override
    public ProductAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                                .from(mContext)
                                .inflate(R.layout.product_list_item, parent, false);
        view.setFocusable(true);
        return new ProductAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the prodcut
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param  holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ProductAdapterViewHolder holder, final int position) {

        // Move the cursor to the appropriate position
        mCursor.moveToPosition(position);

        int idColumnIndex = mCursor.getColumnIndex(ProductEntry._ID);
        int nameColumnIndex = mCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = mCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = mCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        int productImage = mCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);


        final int id = mCursor.getInt(idColumnIndex);
        final String name = mCursor.getString(nameColumnIndex);
        int  price = mCursor.getInt(priceColumnIndex);
        final int quantity = mCursor.getInt(quantityColumnIndex);

        String image = mCursor.getString(productImage);

        holder.itemView.setTag(id);
        holder.nameTextView.setText(name);
        holder.priceTextView.setText(Integer.toString(price)+ " $");
        holder.quantityTextView.setText(Integer.toString(quantity));

        if (!TextUtils.isEmpty(image))
        Picasso.with(mContext)
                .load(Uri.parse(image))
                .resize(150, 150)
                .centerCrop()
                .into(holder.itemImage);
        else
            holder.itemImage.setImageResource(R.drawable.ic_empty_shelter);


        holder.saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int newQuantity = 0;
                if (quantity > 0) {

                    newQuantity = quantity -1;
                }
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                Uri currentUri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, String.valueOf(id));
                int rowupdated = mContext.getContentResolver().update(currentUri, values, null, null);


            }
        });

    }




    @Override
    public int getItemCount() {
        // If mCursor is null, return 0. Otherwise, return the count of mCursor
        if (null == mCursor) return 0;
        return mCursor.getCount();

    }

    //  This  method that allows you to swap Cursors.
    /**
     * Swaps the cursor used by the ProductAdapter . This method is called by
     * ListFragment after a load has finished, as well as when the Loader responsible for loading
     * the data is reset. When this method is called, we assume we have a completely new
     * set of data, so we call notifyDataSetChanged to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as ProductAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;

        notifyDataSetChanged();
    }


    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a product item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    public class ProductAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        ImageView itemImage;
        View emptyView;
        Button saleBtn;


        public ProductAdapterViewHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView)itemView.findViewById(R.id.tv_product_name);
            priceTextView = (TextView)itemView.findViewById(R.id.tv_product_price);
            quantityTextView = (TextView)itemView.findViewById(R.id.tv_product_quantity);
            itemImage = (ImageView)itemView.findViewById(R.id.iv_item_image);
            emptyView = itemView.findViewById(R.id.empty_view);
            saleBtn = (Button)itemView.findViewById(R.id.btn_sale);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            mCursor.moveToPosition(position);
            int id = mCursor.getInt(mCursor.getColumnIndex(ProductEntry._ID));

            mClickListener.onClick(id);
        }

    }


}
