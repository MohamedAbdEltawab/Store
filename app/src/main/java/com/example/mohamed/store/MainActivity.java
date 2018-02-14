package com.example.mohamed.store;


import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class MainActivity extends AppCompatActivity implements ListFragment.OnItemSelectedListener,
                            FragmentManager.OnBackStackChangedListener{

    FloatingActionButton fab;
    FragmentManager fragmentManager = getSupportFragmentManager();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        fab = (FloatingActionButton) findViewById(R.id.fab);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    startProductFragment();

                }

            });

            if (savedInstanceState == null) {
                // Start ListFragment
                startListFragment();
            }

    }


    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("pro");
        if (fragment != null && fragment.isVisible()) {

            if (fragment instanceof ProductFragment){

                ((ProductFragment)fragment).onBack();
            }

        }else {
            super.onBackPressed();
        }


    }


    /** Start ProductFragment */
    private void startProductFragment(){

        fragmentManager.beginTransaction()
                .addToBackStack("productFragment")
                .replace(R.id.fl_main_activity, ProductFragment.newInstance(), "pro")
                .commit();
        fab.setVisibility(View.GONE);
    }


    /** This method to start " ListFragment " */
    void startListFragment(){

        Fragment listFragment = new ListFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fl_main_activity, listFragment)
                .commit();

    }

    /**
     *  this method implemented from interface "ListFragment.OnItemSelectedListener"
     *  to start Edit fragment on item clicked
     */
    @Override
    public void onProductSelected(Uri uri) {

        //Fragment productFragment = new ProductFragment();

        String uriString = uri.toString();

        fragmentManager.beginTransaction()
                        .addToBackStack("productFragment")
                        .replace(R.id.fl_main_activity,
                                ProductFragment.newInstance(uriString), "pro")
                        .commit();
        fab.setVisibility(View.GONE);

    }


    @Override
    public void onBackStackChanged() {

        shouldDisplayHomeUp();
    }

    // to display home up to previous screen
    public void shouldDisplayHomeUp(){
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
    }


    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }
}
