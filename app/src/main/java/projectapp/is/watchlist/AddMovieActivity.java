package projectapp.is.watchlist;

import android.content.Intent;
import android.content.RestrictionsManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AddMovieActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    EditText editTitle;
    EditText editDesc;
    TextView dateText;
    ImageView coverImage;
    RatingBar ratingBar;

    Button coverImageButton;

    String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarSave);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Edit Movie");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTitle = (EditText) findViewById(R.id.imageCardTitle);
        editDesc = (EditText) findViewById(R.id.imageCardDesc);
        dateText = (TextView) findViewById(R.id.imageCardDate);
        coverImage = (ImageView) findViewById(R.id.editCardCover);
        ratingBar = (RatingBar) findViewById(R.id.addRatingBar);
        coverImageButton = (Button) findViewById(R.id.buttonSelectCover);

        date = DATE_FORMAT.format(new Date());
        dateText.setText(date);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save){
            if (editTitle.getText().toString().equals("")){
                Toast.makeText(getBaseContext(),"Please enter a title", Toast.LENGTH_SHORT).show();
                return false;
            }
            /*if (!imagePicked){
                Toast.makeText(getBaseContext(),"Please select a cover image", Toast.LENGTH_SHORT).show();
                return false;
            }*/

            Log.v("d", "CLICKED SAVE ON MENU!!!!");
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

        return super.onOptionsItemSelected(item);
    }
}
