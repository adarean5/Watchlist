package projectapp.is.watchlist;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity{
    public final int ADD_CARD = 3001;
    private final int EDIT_REQUEST = 5001;
    public final int REQUEST_LOGIN = 0001;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("dd. MMM, YYYY");

    private final String getUrl = "http://ismoviesrest.azurewebsites.net/Movies.svc/Movies";
    private final String postUrl = "http://ismoviesrest.azurewebsites.net/Movies.svc/Movie";
    private String username;
    private String password;

    private android.support.v7.widget.Toolbar toolbar;

    RecyclerView recyclerViewMain;
    LinearLayoutManager llm;
    FloatingActionButton fab;
    RVAdapter rvAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    RequestQueue requestQueue;

    ArrayList<MainMovieCard> mainMovieCards;
    Stack<MainMovieCard> moviesToSync;
    Stack<MainMovieCard> moviesToDelete;
    ArrayList<Integer> IDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onRestore(savedInstanceState);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        swipeRefreshLayout = findViewById(R.id.swipeMainRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                JsonArrayRequest request = new JsonArrayRequest(getUrl, jsonArrayListener, errorListener);
                requestQueue.add(request);
                while (!moviesToSync.empty()){
                    Log.e("Pop", "popped");
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
                }
                while (!moviesToDelete.empty()){
                    Log.e("Pop", "popped delete");
                    final MainMovieCard movieCard = moviesToDelete.pop();
                    int id = movieCard.getId();
                    deleteMovie(movieCard, id);
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        recyclerViewMain = (RecyclerView)findViewById(R.id.recyclerViewMain);
        llm = new LinearLayoutManager(this);
        rvAdapter = new RVAdapter(mainMovieCards, new OnDeleteClick() {
            @Override
            public void onDeleteClick(MainMovieCard movieCard) {
                moviesToDelete.push(movieCard);
                Log.e("ON DELETE CLICK", "ON DELETE CLICK TRIGGERED " + movieCard.getMovieTitle());
            }
        });
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
                startActivityForResult(intent, ADD_CARD);
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
                    String credentials = username + ":" + password;
                    Log.e("LOGIN INFO", credentials);
                    String auth = credentials;
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

    public void deleteMovie(final MainMovieCard movieCard, final int id) {
        String url = postUrl + "/" + String.valueOf(id);

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                IDs.remove(new Integer(id));
                Log.i("LOG_VOLLEY", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                moviesToDelete.push(movieCard);
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
                String credentials = username + ":" + password;
                Log.e("LOGIN INFO", credentials);
                String auth = credentials;
                headers.put("Authorization", auth);
                return headers;
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
    }

    public void updateMovie(final MainMovieCard movieCard, int id) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("movieTitle", movieCard.getMovieTitle());
            jsonBody.put("movieDesc", movieCard.getMovieDescription());
            jsonBody.put("movieDate", movieCard.getDateText());
            jsonBody.put("movieRating", movieCard.getRating());

            final String mRequestBody = jsonBody.toString();
            String url = postUrl + "/" + String.valueOf(id);

            StringRequest stringRequest = new StringRequest(Request.Method.PUT, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
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
                    String credentials = username + ":" + password;
                    Log.e("LOGIN INFO", credentials);
                    String auth = credentials;
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
                    JSONObject obj = new JSONObject(res);
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                } catch (JSONException e2) {
                    e2.printStackTrace();
                }
            }
        }
    };

    private void onRestore(Bundle savedInstanceState){
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbarMain);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        moviesToSync = new Stack<MainMovieCard>();

        if (savedInstanceState == null || !savedInstanceState.containsKey("cardlist")){
            Log.v("t", "NEW");
            mainMovieCards = readFromInternalStorage();
        }
        else{
            mainMovieCards = savedInstanceState.getParcelableArrayList("cardlist");
            Log.v("r", "RESTORED");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("IDs")){
            IDs = readIDs();
        }
        else {
            IDs = savedInstanceState.getIntegerArrayList("IDs");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("moviesToSync")){
            moviesToSync = readToSync();
        }
        else {
            moviesToSync = (Stack<MainMovieCard>) savedInstanceState.getSerializable("moviesToSync");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("moviesToDelete")){
            moviesToDelete = readToDelete();
        }
        else {
            moviesToDelete = (Stack<MainMovieCard>) savedInstanceState.getSerializable("moviesToSync");
        }

        if (savedInstanceState == null || !savedInstanceState.containsKey("username") || !savedInstanceState.containsKey("pass")){
            String[] loginInfo = readLogin();
            username = loginInfo[0];
            password = loginInfo[1];
        }
        else {
            username = savedInstanceState.getString("username");
            password = savedInstanceState.getString("pass");
        }

        if (username == null || password == null){
            Intent intent = new Intent(MainActivity.this, LoginActivityMain.class);
            startActivityForResult(intent, REQUEST_LOGIN);
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

            fos = this.openFileOutput("moviesToDelete", Context.MODE_PRIVATE);
            of = new ObjectOutputStream(fos);
            of.writeObject(moviesToDelete);
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

    public Stack<MainMovieCard> readToDelete() {
        Stack<MainMovieCard> toReturn = new Stack<MainMovieCard>();
        FileInputStream fis;
        try {
            fis = this.openFileInput("moviesToDelete");
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


    public String[] readLogin() {
        String[] toReturn = new String[2];
        FileInputStream fis;
        try {
            fis = this.openFileInput("LoginData");
            ObjectInputStream oi = new ObjectInputStream(fis);
            toReturn[0] = (String) oi.readObject();
            toReturn[1] = (String) oi.readObject();
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

        if (resultCode == RESULT_OK  && requestCode == ADD_CARD) {
            Bundle bundle = data.getExtras();
            String title = bundle.getString("Edit.editTitle");
            String description = bundle.getString("Edit.editDesc");
            String currentDate = bundle.getString("Edit.dateText");
            float rating = bundle.getFloat("Edit.rating");
            int randomNum = ThreadLocalRandom.current().nextInt(1000, 9999);
            MainMovieCard movieCard = new MainMovieCard(randomNum,title, description, currentDate, rating);
            mainMovieCards.add(0, movieCard);
            moviesToSync.push(movieCard);
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();

            saveToInternalStorage();
        }

        if (resultCode == RESULT_OK  && requestCode == EDIT_REQUEST) {
            Bundle bundle = data.getExtras();
            String title = bundle.getString("Edit.editTitle");
            String description = bundle.getString("Edit.editDesc");
            float rating = bundle.getFloat("Edit.rating");
            int position = bundle.getInt("position");

            mainMovieCards.get(position).setMovieTitle(title);
            mainMovieCards.get(position).setMovieDescription(description);
            mainMovieCards.get(position).setRating(rating);

            moviesToSync.push(mainMovieCards.get(position));
            (recyclerViewMain.getAdapter()).notifyDataSetChanged();

            saveToInternalStorage();
        }

        if (resultCode == RESULT_OK  && requestCode == REQUEST_LOGIN) {
            Bundle bundle = data.getExtras();
            username = bundle.getString("Login.username");
            password = bundle.getString("Login.pass");
            try {
                FileOutputStream fos = this.openFileOutput("LoginData", Context.MODE_PRIVATE);
                ObjectOutputStream of = new ObjectOutputStream(fos);
                of.writeObject(username);
                of.writeObject(password);
                of.flush();
                of.close();
                fos.close();
                Log.v("iw", "Written to internal storage");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivityMain.class);
            startActivityForResult(intent, REQUEST_LOGIN);
        }

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("cardlist", mainMovieCards);
        outState.putIntegerArrayList("IDs", IDs);
        outState.putSerializable("moviesToSync", moviesToSync);
        outState.putSerializable("moviesToDelete", moviesToDelete);
        outState.putSerializable("username", username);
        outState.putSerializable("pass", password);
        super.onSaveInstanceState(outState);
        Log.v("s", "SAVED");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onRestore(savedInstanceState);
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

        if (id == R.id.action_logOut){
            username = null;
            password = null;

            deleteFile("LoginData");

            Intent intent = new Intent(MainActivity.this, LoginActivityMain.class);
            startActivityForResult(intent, REQUEST_LOGIN);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
