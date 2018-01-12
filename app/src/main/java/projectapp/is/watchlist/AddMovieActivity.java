package projectapp.is.watchlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddMovieActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");
    private int position;
    private boolean editMode = false;

    EditText editTitle;
    EditText editDesc;
    TextView dateText;
    //ImageView coverImage;
    RatingBar ratingBar;

    Button addMovieButton;

    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarSave);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTitle = (EditText) findViewById(R.id.imageCardTitle);
        editDesc = (EditText) findViewById(R.id.imageCardDesc);
        dateText = (TextView) findViewById(R.id.imageCardDate);
        ratingBar = (RatingBar) findViewById(R.id.addRatingBar);
        addMovieButton = (Button) findViewById(R.id.buttonAddMovie);

        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null){
                editMode = true;
                addMovieButton.setText("Edit Movie");
                editTitle.setText(bundle.getString("movieTitle"));
                editDesc.setText(bundle.getString("movieDesc"));
                dateText.setText(bundle.getString("dateText"));
                ratingBar.setRating(bundle.getFloat("movieRaring"));
                position = bundle.getInt("position");
            }
            else {
                date = DATE_FORMAT.format(new Date());
                dateText.setText(date);
            }
        }

        addMovieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editTitle.getText().toString().equals("")){
                    Toast.makeText(getBaseContext(),"Please enter a title", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editMode){
                    Intent returnIntent = new Intent();

                    Bundle bundle = new Bundle();
                    bundle.putString("Edit.editTitle", editTitle.getText().toString());
                    bundle.putString("Edit.editDesc", editDesc.getText().toString());
                    bundle.putFloat("Edit.rating", ratingBar.getRating());
                    bundle.putInt("position", position);

                    returnIntent.putExtras(bundle);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
                else {
                    Intent returnIntent = new Intent();

                    Bundle bundle = new Bundle();
                    bundle.putString("Edit.editTitle", editTitle.getText().toString());
                    bundle.putString("Edit.editDesc", editDesc.getText().toString());
                    bundle.putFloat("Edit.rating", ratingBar.getRating());
                    bundle.putString("Edit.dateText", date);

                    returnIntent.putExtras(bundle);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }
}
