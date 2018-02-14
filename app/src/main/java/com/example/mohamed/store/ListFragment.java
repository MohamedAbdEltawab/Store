package com.example.mohamed.store;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mohamed.store.data.ProductContract;
import com.example.mohamed.store.data.ProductContract.ProductEntry;
import com.example.mohamed.store.data.ProductDbHelper;

import java.util.List;


public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ProductAdapter.ProductAdapterOnClickListener, SearchView.OnQueryTextListener{

    private static final String TAG = ListFragment.class.getSimpleName();
    // Loader ID
    private final static int PRODUCT_LOADER_ID = 0;
    RecyclerView mRecyclerView;
    ProductAdapter mAdapter;
    SearchView searchView;

    /** Interface implemented in MainActivity to implement method " onProductSelected " */
    OnItemSelectedListener mCallback;

    /** Float Button to add new product */
    FloatingActionButton fab;

    Cursor mCursor;

    String cursorFilter;

    View emptyView;

    Uri currentUri;

    // Container Activity must implement this interface
    public interface OnItemSelectedListener {
        void onProductSelected(Uri uri);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /** Set Title to List Fragment */
        getActivity().setTitle("Store");

        /** Inflate fragment list  */
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        /** find recycler view which view product item */
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.rc_products_list);

        /** setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));

        /** this empty view will appear when not item found and recyclerview will invisible  */
        emptyView = rootView.findViewById(R.id.empty_view);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new ProductAdapter(getContext(), this);

        mRecyclerView.setAdapter(mAdapter);

        /** Can delete item when swiped Right or left */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int id = (int)viewHolder.itemView.getTag();

                String stringId = Integer.toString(id);
                Uri uri = ProductEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                getContext().getContentResolver().delete(uri, null, null);
            }
        }).attachToRecyclerView(mRecyclerView);


        /** when clicked will go to " productFragment " to add new item ( roduct ) */
        fab = (FloatingActionButton)getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);

        // to make menu appear
        setHasOptionsMenu(true);

        return rootView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);

    }

//    /**
//     *  Insert Dummy Data " name (New Product) - quantity (20) - quantity (3)
//     **/
//    private void insertProduct() {
//
//        // Create a ContentValues object where column names are the keys,
//
//        ContentValues values = new ContentValues();
//        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Product");
//        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, 40);
//        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 3);
//        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "mohamed");
//        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, R.drawable.ic_empty_shelter);
//        Uri newUri = getContext().getContentResolver().insert(ProductEntry.CONTENT_URI, values);
//        Log.i("ListFragment ", " :" + newUri);
//    }

    /**
     *  Helper method to delete all products in the database.
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContext().getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("ListFragment ", rowsDeleted + " rows deleted from products database");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.menu_search_view);
        searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.delete_all_data:
                deleteAllProducts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        cursorFilter = !TextUtils.isEmpty(newText) ? newText : null;


       getLoaderManager().restartLoader(PRODUCT_LOADER_ID, null, this);
        return true;
    }



    /**
     *  when Loader manager is start , first this method is called in thread
     * @param id
     * @param args
     * @return  ( CursorLoader )
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String selection = null;
        String [] selectionArgs = null;

       if (cursorFilter != null) {

           selection = ProductEntry.COLUMN_PRODUCT_NAME + " LIKE ?";
           selectionArgs = new String[] {"%" + cursorFilter + "%"};

        }
        // this column of table that will cursor get data from their
        String [] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE };

        // this CursorLoader make operation in background thread to get data from table
        return new CursorLoader(
                getContext(),
                ProductEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
               null

        );

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return;
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        if (mAdapter == null){

            mAdapter = new ProductAdapter(getContext(), this);
        }

        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /**
     *    this method from ProductAdapterOnClickListener
     *    on click  and implement  ( onProductSelected )
     *    from OnItemClickListener to take uri of item to main activity
     *    to go to " ProductFragment "
     */

    @Override
    public void onClick(int id) {

        currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

        mCallback.onProductSelected(currentUri);
//        Toast.makeText(getContext(), " : " +currentUri, Toast.LENGTH_SHORT).show();
    }


}
