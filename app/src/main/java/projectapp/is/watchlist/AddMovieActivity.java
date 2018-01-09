package projectapp.is.watchlist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AddMovieActivity extends AppCompatActivity {
    private android.support.v7.widget.Toolbar toolbar;

    EditText editTitle;
    EditText editDesc;
    TextView dateText;

    ImageView coverImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarSave);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Edit Movie");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTitle = (EditText) findViewById(R.id.imageCardTitle);
        editDesc = (EditText) findViewById(R.id.imageCardDesc);
        dateText = (TextView) findViewById(R.id.imageCardDate);
        coverImage = (ImageView) findViewById(R.id.editCardCover);
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

            returnIntent.putExtras(bundle);
            setResult(RESULT_OK, returnIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
