package com.example.mohamed.store;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohamed.store.data.ProductContract.ProductEntry;


public class ProductFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static int PRODUCT_LOADER_ID = 0;

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;
    Uri uri;
    String getArgs;

    /** EditText field for enter a product name */
    EditText nameEditText;
    /** EditText field for enter a product price */
    EditText priceEditText;
    /** EditText field for enter a product quantity */
    EditText quantityEditText;
    /** EditText field for enter a supplier name */
    EditText suppNameEditText;
    /** EditText field for enter a supplier phone */
    EditText suppPhoneEditText;
    /** EditText field for enter a supplier email  */
    EditText suppEmailEditText;

    /** Button to save new product or update product */
    Button saveBtn;
    /** ImageView to show image of product */
    ImageView imageView;
    /** Uri of Product attached */
//    Uri actualUri ;


    ImageButton increaseBtn;
    ImageView decreaseBtn;

    String imgString;
    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private static boolean mProductChanged;

    Uri currentUri;

    public ProductFragment() {
    }

    public static ProductFragment newInstance() {
        return new ProductFragment();
    }

    public static ProductFragment newInstance(String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("UriString", uri);
        ProductFragment productFragment = new ProductFragment();
        productFragment.setArguments(bundle);
        return productFragment;
    }


    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mProductChanged = true ;
            return false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_product, container, false);

        setRetainInstance(true);

        mProductChanged = false;
        /**
         *  find EditText for product info
         * ( Name - price - quantity - supplierName - supplierEmail -supplierPhone)
         * */
        nameEditText = rootView.findViewById(R.id.ed_name);
        priceEditText = rootView.findViewById(R.id.ed_price);
        quantityEditText = rootView.findViewById(R.id.ed_quantity);
        suppNameEditText = rootView.findViewById(R.id.ed_supplier_name);
        suppPhoneEditText = rootView.findViewById(R.id.ed_supplier_phone);
        suppEmailEditText = rootView.findViewById(R.id.ed_supplier_email);

        /** make Every EditText listen to TouchListener */

        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        quantityEditText.setOnTouchListener(mTouchListener);
        suppNameEditText.setOnTouchListener(mTouchListener);
        suppPhoneEditText.setOnTouchListener(mTouchListener);
        suppEmailEditText.setOnTouchListener(mTouchListener);

        /** find increaseButton will increase quantity */
        increaseBtn = (ImageButton)rootView.findViewById(R.id.btn_increase);
        /** find decreaseButton will decrease quantity */
        decreaseBtn = (ImageButton)rootView.findViewById(R.id.btn_decrease);

        /** find saveButton its save product */
        saveBtn = rootView.findViewById(R.id.btn_save);

        /** find ImageView will show product image */
        imageView = (ImageView)rootView.findViewById(R.id.iv_image);

        return rootView;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            // Uri for item clicked if not null
            getArgs = getArguments().getString("UriString");
            currentUri = Uri.parse(getArgs);

        }

        // to make menu appear
        setHasOptionsMenu(true);

            if (currentUri == null){

                getActivity().setTitle(getResources().getString(R.string.add_a_Product));

            }else {


                getActivity().setTitle((getResources().getString(R.string.edit_a_produt)));

                getLoaderManager().initLoader(PRODUCT_LOADER_ID, null, this);
            }


        // when click on button will get image
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToOpenImageSelector();
                mProductChanged = true;
            }
        });
        // When clicked on Increase Button execute  function ( sumOneToQuantity )
        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sumOneToQuantity();
                mProductChanged = true;
            }
        });
        // When clicked on Decrease Button execute  function ( decreaseOneFromQuantity )
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseOneFromQuantity();
                mProductChanged = true;
            }
        });

    }

    /* Increase one to Quantity EditText  */
    private void sumOneToQuantity(){

        String previousValueString = quantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        quantityEditText.setText(String.valueOf(previousValue + 1));
    }

    /* Decrease one in Quantity EditText  */
    private void decreaseOneFromQuantity(){
        String previousValueString = quantityEditText.getText().toString();
        int previousValue;
        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            quantityEditText.setText(String.valueOf(previousValue - 1));
        }
    }

    public void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                Uri actualUri = resultData.getData();
                imgString = actualUri.toString();
                imageView.setImageURI(actualUri);
                //imageView.invalidate();

            }


        }


    }

    /**
     * Save new product andcheck if EditText ( name - price - quantity - image ) not empty
     * or
     * Update current product if currentUri not null
     * @return
     */
    private boolean saveProduct(){

        boolean isAllOk = true;
        // Read from input fields
        String name =  nameEditText.getText().toString().trim();
        String priceString =  priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString();
        String suppName = suppNameEditText.getText().toString().trim();
        String suppPhone = suppPhoneEditText.getText().toString().trim();
        String suppEmail = suppEmailEditText.getText().toString().trim();

        ContentValues values = new ContentValues();

        if (currentUri == null){

            if (!checkIfValueSet(nameEditText, "name")) {
                isAllOk = false;
            }

            if (!checkIfValueSet(priceEditText, "price")) {
                isAllOk = false;
            }

            if (!checkIfValueSet(quantityEditText, "quantity")) {
                isAllOk = false;
            }


//            if (!TextUtils.isEmpty(imgString)) {
//                isAllOk = false;
//
//            }

            if (!isAllOk) {
                return false;
            }
        }


        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)){
            price = Integer.parseInt(priceString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)){
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME, suppName);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, suppPhone);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL, suppEmail);

        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imgString);

        if (currentUri == null){
            Uri newUri = getContext().getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null){
                Toast.makeText(getContext(), "Failed to save Product ", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getContext(), "Product saved successfully", Toast.LENGTH_SHORT).show();

            }
        }else{

            //values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imgString);
            int rowsAffected = getContext().getContentResolver().update(currentUri, values, null, null);

            if (rowsAffected == 0){
                Toast.makeText(getContext(), "Updated Failed", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getContext(), "Updated success", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    /**
     *  check if EditText ( name - price - quantity - image ) not empty
     */
    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError("Missing product " + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String [] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(
                getContext(),
                currentUri,
                projection,
                null,
                null,
                null
        );

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int suppNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int suppPhoneColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            int suppEmailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            int productImage = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            int  price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String suppName = cursor.getString(suppNameColumnIndex);
            String suppPhone = cursor.getString(suppPhoneColumnIndex);
            String suppEmail = cursor.getString(suppEmailColumnIndex);
            imgString = cursor.getString(productImage);



            nameEditText.setText(name);
            priceEditText.setText(Integer.toString(price));
            quantityEditText.setText(Integer.toString(quantity));
            suppNameEditText.setText(suppName);
            suppPhoneEditText.setText(suppPhone);
            suppEmailEditText.setText(suppEmail);
            if (!TextUtils.isEmpty(imgString))
                imageView.setImageURI(Uri.parse(imgString));

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        suppNameEditText.setText("");
        suppPhoneEditText.setText("");
        suppEmailEditText.setText("");
        imageView.setImageURI(null);
    }



    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.unsaved_changed_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void onBack() {

        if (mProductChanged == false) {

            getFragmentManager().popBackStack();
        }else{

            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getFragmentManager().popBackStack();

                        }
                    };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }

    }


    /**
     *  this method will display Dialog when click order more
     */
    private void showOrderMoreDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.select_to_order_more);
        builder.setPositiveButton(R.string.phone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + suppPhoneEditText.getText().toString().trim()));
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                }
        }
        });


        builder.setNegativeButton(R.string.email, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + suppEmailEditText.getText().toString().trim())); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, suppEmailEditText.getText().toString().trim());
                String bodyMessage = "Please send us as soon as possible more " +
                        nameEditText.getText().toString().trim() +
                        "!!!";
                intent.putExtra(Intent.EXTRA_SUBJECT, bodyMessage);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private int deleteOneItem(){
        String [] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        int rowDeleted = getContext().getContentResolver().delete(currentUri, null, projection);

        return rowDeleted;
    }
    private void showDeleteItemDiallog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.delete_one_item);
        builder.setPositiveButton(R.string.accept_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteOneItem();
                getFragmentManager().popBackStack();

            }
        });


        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *  inflate menu item
     *
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_product, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    /**
     *  Prepare Menu if insert new product screen just show save button
     *  else show all option menu
     */

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentUri == null){
            MenuItem order = menu.findItem(R.id.order_more);
            MenuItem delete = menu.findItem(R.id.delete_item);
            order.setVisible(false);
            delete.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.btn_save:
                if (!saveProduct()){
                    return true;
                }

                getFragmentManager().popBackStack();
                return true;
            case R.id.order_more :
               showOrderMoreDialog();
                return true;
            case R.id.delete_item:
                showDeleteItemDiallog();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


}
