package com.meet.phonebook.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.EditorInfo;

import com.meet.phonebook.Adapter.DetailsListAdapter;
import com.meet.phonebook.Data.PersonData;
import com.meet.phonebook.Misc.DatabaseHelper;
import com.meet.phonebook.R;

import java.io.IOException;
import java.util.ArrayList;

import static com.meet.phonebook.Misc.Constants.DATABASE_URL;
import static com.meet.phonebook.Misc.Constants.IS_DATABASE_UPDATED;
import static com.meet.phonebook.Misc.Constants.SHARED_PREFS;

public class ListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    public SharedPreferences sharedPreferences;

    public DetailsListAdapter detailsListAdapter;
    public RecyclerView recyclerView;
    public ArrayList<PersonData> personDataList = new ArrayList<>();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        recyclerView = findViewById(R.id.dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        personDataList = getIntent().getParcelableArrayListExtra("personDataList");
        detailsListAdapter= new DetailsListAdapter(this,personDataList);
        recyclerView.setAdapter(detailsListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem  menuItem = menu.findItem(R.id.searchButton);
        android.support.v7.widget.SearchView searchView = (android.support.v7.widget.SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(this);

        MenuItem downloadDataMenu = menu.findItem(R.id.updateData);
        if(sharedPreferences.getBoolean(IS_DATABASE_UPDATED, true)){
            downloadDataMenu.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.updateData){
            Intent intent = new Intent(ListActivity.this,UpdateActivity.class);
            intent.putExtra("dataUrl",sharedPreferences.getString(DATABASE_URL,null));
            startActivity(intent);
            overridePendingTransition(0,0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String inputString) {
        String nameInput = inputString.toLowerCase();
        ArrayList<PersonData> personDataNewList = new ArrayList<>();

        for(PersonData personData : personDataList){
            if (personData.getName().toLowerCase().contains(nameInput)){
                personDataNewList.add(personData);
            }
        }
        detailsListAdapter.updateList(personDataNewList);
        return false;
    }
}