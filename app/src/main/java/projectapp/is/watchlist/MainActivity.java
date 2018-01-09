package projectapp.is.watchlist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public final int EDIT_CARD = 3001;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    RecyclerView recyclerViewMain;
    LinearLayoutManager llm;
    FloatingActionButton fab;
    RVAdapter rvAdapter;

    ArrayList<MainMovieCard> mainMovieCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            mainMovieCards = new ArrayList<>();
            Log.v("t", "NEW");
            mainMovieCards = readFromInternalStorage();
        }

        else{
            mainMovieCards = savedInstanceState.getParcelableArrayList("cardlist");
            Log.v("r", "RESTORED");
        }

        recyclerViewMain = (RecyclerView)findViewById(R.id.recyclerViewMain);
        llm = new LinearLayoutManager(this);
        rvAdapter = new RVAdapter(mainMovieCards);
        recyclerViewMain.setLayoutManager(llm);
        recyclerViewMain.setAdapter(rvAdapter);
        fab = findViewById(R.id.fab);

        recyclerViewMain.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fab.getVisibility() == View.VISIBLE) {
                    fab.hide();
                } else if (dy < -10 && fab.getVisibility() != View.VISIBLE) {
                    fab.show();
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, AddMovieActivity.class);
                startActivityForResult(intent, EDIT_CARD);
            }
        });
    }

    public void saveToInternalStorage() {
        try {
            FileOutputStream fos = this.openFileOutput("MainMovieCards", Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(mainMovieCards);
            of.flush();
            of.close();
            fos.close();
            Log.v("iw", "Written to internal storage");
        }
        catch (Exception e) {
            Log.e("InternalStorage", e.getMessage());
        }
    }

    public ArrayList<MainMovieCard> readFromInternalStorage() {
        ArrayList<MainMovieCard> toReturn = new ArrayList<MainMovieCard>();
        FileInputStream fis;
        try {
            fis = this.openFileInput("MainMovieCards");
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (ArrayList<MainMovieCard>) oi.readObject();
            oi.close();
            Log.v("ir", "Read from internal storage");
        } catch (FileNotFoundException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (IOException e) {
            Log.e("InternalStorage", e.getMessage());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK  && requestCode == EDIT_CARD) {
            Bundle bundle = data.getExtras();
            String title = bundle.getString("Edit.editTitle");
            String description = bundle.getString("Edit.editDesc");
            String currentDate = bundle.getString("Edit.dateText");

            mainMovieCards.add(0, new MainMovieCard(title, description, currentDate));
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();

            saveToInternalStorage();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("cardlist", mainMovieCards);
        super.onSaveInstanceState(outState);
        Log.v("s", "SAVED");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            mainMovieCards = new ArrayList<>();
        }

        else{
            mainMovieCards = savedInstanceState.getParcelableArrayList("cardlist");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToInternalStorage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
