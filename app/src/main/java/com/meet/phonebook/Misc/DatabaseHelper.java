package com.meet.phonebook.Misc;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.View;
import android.widget.ActionMenuView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meet.phonebook.Data.PersonData;
import com.meet.phonebook.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static com.meet.phonebook.Misc.Constants.DATABASE_NAME;

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;

    public DatabaseHelper(Context context) throws IOException {
        super(context, DATABASE_NAME, null, 1);
        this.context=context;
        boolean dbExist = checkDatabase();
        if (dbExist) {
            openDatabase();
            this.close();
        } else {
            System.out.println("Database doesn't exist");
            createDatabase();
        }
    }

    private void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if(!dbExist) {
            this.getReadableDatabase();
            this.close();
            try {
                copyDatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDatabase() throws IOException {
        InputStream inputStream = context.getAssets().open("phone_book.db");

        String db_filename = getPath();
        OutputStream outputStream = new FileOutputStream(db_filename);
        byte[] buffer = new byte[2048];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.d("data stored","Data stored at " + db_filename);
    }

    private boolean checkDatabase() {

        boolean checkdb = false;
        try {
            String myPath = getPath();
            File dbFile = new File(myPath);
            checkdb = dbFile.exists();
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    private String getPath(){
        String db_path = context.getApplicationInfo().dataDir;
        File file = new File(db_path,"databases");
        return file.getAbsolutePath() + "/" + DATABASE_NAME;
    }

    private void openDatabase() throws SQLException {
        String myPath = getPath();
        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion)
        {
            Log.v("Database Upgrade", "Database version higher than old.");
            db_delete();
        }

    }

    private void db_delete()
    {
        File file = new File(getPath());
        if(file.exists())
        {
            file.delete();
            System.out.println("delete database file.");
        }
    }

    public ArrayList<PersonData> getAllData(){
        
        ArrayList<PersonData> personDataArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM records",null);
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                long mobileNo = cursor.getLong(cursor.getColumnIndex("mobile_no"));
                long phoneNo = cursor.getLong(cursor.getColumnIndex("phone_no"));

                PersonData personData = new PersonData(name, address, mobileNo, phoneNo);
                personDataArrayList.add(personData);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

//        progressBar.setVisibility(View.GONE);
//        textView.setVisibility(View.GONE);

        return personDataArrayList;
    }
}
