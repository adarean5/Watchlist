package projectapp.is.watchlist;

import android.app.SearchManager;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.media.ThumbnailUtils;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity{
    public final int EDIT_CARD = 3001;
    public final int REQUEST_LOGIN = 0001;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    private final String getUrl = "http://ismoviesrest.azurewebsites.net/Movies.svc/Movies";
    private final String postUrl = "http://ismoviesrest.azurewebsites.net/Movies.svc/Movie";
    private String username;
    private String password;

    RecyclerView recyclerViewMain;
    LinearLayoutManager llm;
    FloatingActionButton fab;
    RVAdapter rvAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RequestQueue requestQueue;

    ArrayList<MainMovieCard> mainMovieCards;
    Stack<MainMovieCard> moviesToSync;
    ArrayList<Integer> IDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (username == null || password == null){
            Intent intent = new Intent(MainActivity.this, LoginActivityMain.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        moviesToSync = new Stack<MainMovieCard>();

        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            mainMovieCards = new ArrayList<>();
            Log.v("t", "NEW");
            mainMovieCards = readFromInternalStorage();
        }

        else{
            mainMovieCards = savedInstanceState.getParcelableArrayList("cardlist");
            Log.v("r", "RESTORED");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("IDs")){
            IDs = new ArrayList<>();
            IDs = readIDs();
        }
        else {
            IDs = savedInstanceState.getIntegerArrayList("IDs");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("moviesToSync")){
            moviesToSync = new Stack<MainMovieCard>();
            moviesToSync = readToSync();
        }
        else {
            moviesToSync = (Stack<MainMovieCard>) savedInstanceState.getSerializable("moviesToSync");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("username")){
            username = null;
        }
        else {
            username = savedInstanceState.getString("username");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("pass")){
            password = null;
        }
        else {
            password = savedInstanceState.getString("pass");
        }

        requestQueue = Volley.newRequestQueue(getApplicationContext());


        swipeRefreshLayout = findViewById(R.id.swipeMainRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                JsonArrayRequest request = new JsonArrayRequest(getUrl, jsonArrayListener, errorListener);
                requestQueue.add(request);
                while (!moviesToSync.empty()){
                    Log.e("Pop", "popped");
                    final Map<String, String> movieMap = new HashMap<String, String>();
                    final MainMovieCard movieCard = moviesToSync.pop();
                    int id = movieCard.getId();
                    if (IDs.contains(id)){
                        Log.e("Pop", "UPDATING");
                        updateMovie(movieCard, id);
                    }
                    else{
                        Log.e("Pop", "ADDING");
                        addMovie(movieCard, id);
                    }

                    /*
                    int id = movieCard.getId();
                    IDs.add(id);
                    movieMap.put("id", String.valueOf(id));
                    movieMap.put("movieTitle", movieCard.getMovieTitle());
                    movieMap.put("movieDesc", movieCard.getMovieTitle());
                    movieMap.put("movieDate", movieCard.getDateText());
                    movieMap.put("movieRating", String.valueOf(movieCard.getRating()));

                    JsonObjectRequest postRequest = new JsonObjectRequest(postUrl, new JSONObject(movieMap), jsonResponseListener, errorListener);

                    requestQueue.add(postRequest);
                    */
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

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

    public void addMovie(final MainMovieCard movieCard, final int id) {
        try {
            JSONObject jsonBody = new JSONObject();

            jsonBody.put("id", id);
            jsonBody.put("movieTitle", movieCard.getMovieTitle());
            jsonBody.put("movieDesc", movieCard.getMovieDescription());
            jsonBody.put("movieDate", movieCard.getDateText());
            jsonBody.put("movieRating", movieCard.getRating());

            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, postUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    IDs.add(id);
                    Log.i("LOG_VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    moviesToSync.push(movieCard);
                    Log.e("LOG_VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    //String credentials = "admin:test";
                    String credentials = username + ":" + password;
                    Log.e("LOGIN INFO", credentials);
                    String auth = credentials;//Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    //headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void updateMovie(final MainMovieCard movieCard, int id) {
        try {
            JSONObject jsonBody = new JSONObject();
            //jsonBody.put("id", id);
            jsonBody.put("movieTitle", movieCard.getMovieTitle());
            jsonBody.put("movieDesc", movieCard.getMovieDescription());
            jsonBody.put("movieDate", movieCard.getDateText());
            jsonBody.put("movieRating", movieCard.getRating());

            final String mRequestBody = jsonBody.toString();
            String url = postUrl + "/" + String.valueOf(id);

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //IDs.add(id);
                    Log.i("LOG_VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    moviesToSync.push(movieCard);
                    Log.e("LOG_VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    super.getHeaders();
                    Map<String, String> headers = new HashMap<>();
                    //String credentials = "admin:test";
                    String credentials = username + ":" + password;
                    Log.e("LOGIN INFO", credentials);
                    String auth = credentials;//Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                    //headers.put("Content-Type", "application/json");
                    headers.put("Authorization", auth);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<JSONObject> jsonResponseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.e("POST RESPONSE", response.toString());
        }
    };

    private Response.Listener<JSONArray> jsonArrayListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
            ArrayList<HashMap<String, String>> data = new ArrayList<>();
            for (int i = 0; i < response.length(); i++) {
                try {
                    JSONObject object = response.getJSONObject(i);
                    int id = object.getInt("id");
                    if (!IDs.contains(id)){
                        String movieName = object.getString("movieTitle");
                        String movieDesc = object.getString("movieDesc");
                        String movieDate = object.getString("movieDate");
                        float rating = (float) object.getDouble("movieRating");

                        mainMovieCards.add(new MainMovieCard(id, movieName, movieDesc, movieDate, rating));
                        IDs.add(id);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    break;
                }
            }
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            NetworkResponse response = error.networkResponse;
            if (error instanceof ServerError && response != null) {
                try {
                    String res = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    // Now you can use any deserializer to make sense of data
                    JSONObject obj = new JSONObject(res);
                } catch (UnsupportedEncodingException e1) {
                    // Couldn't properly decode data to string
                    e1.printStackTrace();
                } catch (JSONException e2) {
                    // returned data is not JSONObject?
                    e2.printStackTrace();
                }
            }
        }
    };

    public void onSearchClick(View v)
    {
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            String term = "Avengers"; //editTextInput.getText().toString();
            intent.putExtra(SearchManager.QUERY, term);
            startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void saveToInternalStorage() {
        try {
            FileOutputStream fos = this.openFileOutput("MainMovieCards", Context.MODE_PRIVATE);
            ObjectOutputStream of = new ObjectOutputStream(fos);
            of.writeObject(mainMovieCards);
            of.flush();
            of.close();
            fos.close();

            fos = this.openFileOutput("IDs", Context.MODE_PRIVATE);
            of = new ObjectOutputStream(fos);
            of.writeObject(IDs);
            of.flush();
            of.close();
            fos.close();
            Log.v("iw", "Written to internal storage");

            fos = this.openFileOutput("moviesToSync", Context.MODE_PRIVATE);
            of = new ObjectOutputStream(fos);
            of.writeObject(moviesToSync);
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

    public ArrayList<Integer> readIDs() {
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        FileInputStream fis;
        try {
            fis = this.openFileInput("IDs");
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (ArrayList<Integer>) oi.readObject();
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

    public Stack<MainMovieCard> readToSync() {
        Stack<MainMovieCard> toReturn = new Stack<MainMovieCard>();
        FileInputStream fis;
        try {
            fis = this.openFileInput("moviesToSync");
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn = (Stack<MainMovieCard>) oi.readObject();
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
            float rating = bundle.getFloat("Edit.rating");
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
            //IDs.add(randomNum);
            MainMovieCard movieCard = new MainMovieCard(randomNum,title, description, currentDate, rating);
            mainMovieCards.add(0, movieCard);
            moviesToSync.push(movieCard);
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();

            saveToInternalStorage();
        }

        if (resultCode == RESULT_OK  && requestCode == REQUEST_LOGIN) {
            Bundle bundle = data.getExtras();
            username = bundle.getString("Login.username");
            password = bundle.getString("Login.pass");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("cardlist", mainMovieCards);
        outState.putIntegerArrayList("IDs", IDs);
        outState.putSerializable("moviesToSync", moviesToSync);
        outState.putSerializable("username", username);
        outState.putSerializable("pass", password);
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

        if (savedInstanceState == null || !savedInstanceState.containsKey("IDs")){
            IDs = new ArrayList<>();
        }
        else {
            IDs = savedInstanceState.getIntegerArrayList("IDs");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("moviesToSync")){
            moviesToSync = new Stack<MainMovieCard>();
        }
        else {
            moviesToSync = (Stack<MainMovieCard>) savedInstanceState.getSerializable("moviesToSync");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("username")){
            username = null;
        }
        else {
            username = savedInstanceState.getString("username");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("pass")){
            password = null;
        }
        else {
            password = savedInstanceState.getString("pass");
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
