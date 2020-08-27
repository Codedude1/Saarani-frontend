package com.example.calendarapp;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ProfileFragment extends Fragment implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private ProfileViewModel mViewModel;

    final int EVENT_INTERESTED=1, EVENT_GOING=2;
    int posIn=0,posGo=0;
    private int eventType=1;
    ActionBar toolbar;
    DatabaseHandler databaseHandler;
    ImageView imgProfileUserPhoto;
    ConstraintLayout constraintLayout;
    TextView tvUserName,tvUserEmailID, tvNoEvent;
    TabLayout tabLayout;
    Date date=new Date();
    final SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    RecyclerView rcvInterestedEvents, rcvGoingEvents;
    RecyclerView.Adapter interestedEventsAdapter, goingEventsAdapter;
    RecyclerView.LayoutManager interestedEventsLayoutManager, goingEventsLayoutManager;
    ArrayList<ListItems> listInterestedEvents, listGoingEvents;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.profile_fragment, container, false);
        databaseHandler=new DatabaseHandler(getContext());
        constraintLayout=view.findViewById(R.id.layout);
        imgProfileUserPhoto=view.findViewById(R.id.imgProfileFragmentUserPhoto);
        tvUserName=view.findViewById(R.id.tvProfileFragmentUserName);
        tvUserEmailID=view.findViewById(R.id.tvProfileFragmentUserEmailID);
        tvNoEvent=view.findViewById(R.id.tvNoEvent);
        tabLayout=view.findViewById(R.id.tabLayoutProfileFragment);

        rcvInterestedEvents=view.findViewById(R.id.rcvProfileFragmentInterestedEvents);
        rcvInterestedEvents.setHasFixedSize(true);
        rcvGoingEvents=view.findViewById(R.id.rcvProfileFragmentGoingEvents);
        rcvGoingEvents.setHasFixedSize(true);
        String name =FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if(name.contains(" ")){
            String[] sep= name.split(" ");
            String nameNew="";
            for(int i=0;i<sep.length;i++) {
                if(i!=0)
                    sep[i] = sep[i].trim();
                sep[i] = sep[i].toLowerCase();
                sep[i] = sep[i].substring(0, 1).toUpperCase() + sep[i].substring(1);
                if(i==sep.length-1)
                    nameNew=nameNew+sep[i];
                else
                    nameNew=nameNew+sep[i]+" ";
            }
            tvUserName.setText(nameNew);
        }
        else{
            name=name.toLowerCase();
            name=name.substring(0, 1).toUpperCase() + name.substring(1);
            tvUserName.setText(name);
        }
        tvUserEmailID.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()!=null){
            Picasso
                    .get()
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                    .resize(110,110)
                    .transform(new CropCircleTransformation())
                    .into(imgProfileUserPhoto);
        }

//        fetchEventsData();
        listInterestedEvents = new ArrayList<ListItems>();
        listGoingEvents = new ArrayList<ListItems>();
        try {
            List<ListItems> allEvents=databaseHandler.getAllEvents();
            for(int i=0;i<allEvents.size();i++){
                ListItems item= allEvents.get(i);
                if(!item.getState().equals("cancelled")&&(f.parse(f.format(date)).compareTo(f.parse(item.getDate()))<0 ||(f.parse(f.format(date)).compareTo(f.parse(item.getDate()))==0&&LocalTime.now().isBefore(LocalTime.parse(item.getTime().split(" ")[1]))))) {
                    if (item.getMarker().equals("going")) {
                        listGoingEvents.add(item);
                    } else if (item.getMarker().equals("interested"))
                        listInterestedEvents.add(item);
                }
            }
            loadRecyclerView();
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        interestedEventsLayoutManager=new LinearLayoutManager(this.getActivity());
        rcvInterestedEvents.setLayoutManager(interestedEventsLayoutManager);
        goingEventsLayoutManager=new LinearLayoutManager(this.getActivity());
        rcvGoingEvents.setLayoutManager(goingEventsLayoutManager);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rcvInterestedEvents);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rcvGoingEvents);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    rcvInterestedEvents.setVisibility(View.VISIBLE);
                    rcvGoingEvents.setVisibility(View.GONE);
                    eventType=EVENT_INTERESTED;

                    if(listInterestedEvents.size()==0){
                        tvNoEvent.setVisibility(View.VISIBLE);
                    }
                    else {
                        tvNoEvent.setVisibility(View.GONE);
                    }

                }
                else if(tab.getPosition()==1){
                    rcvInterestedEvents.setVisibility(View.GONE);
                    rcvGoingEvents.setVisibility(View.VISIBLE);
                    eventType=EVENT_GOING;

                    if(listGoingEvents.size()==0){
                        tvNoEvent.setVisibility(View.VISIBLE);
                    }
                    else {
                        tvNoEvent.setVisibility(View.GONE);
                    }

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        // TODO: Use the ViewModel

    }
    ItemTouchHelper.SimpleCallback touchHelperCallback1,touchHelperCallback2;

    private void loadRecyclerView(){
        interestedEventsAdapter=new EventListAdaptor(this.getActivity(), listInterestedEvents);
        rcvInterestedEvents.setAdapter(interestedEventsAdapter);
        rcvInterestedEvents.setVisibility(View.VISIBLE);
        interestedEventsAdapter.notifyDataSetChanged();

        if(listInterestedEvents.size()==0){
            tvNoEvent.setVisibility(View.VISIBLE);
        }
        else {
            tvNoEvent.setVisibility(View.GONE);
        }

        goingEventsAdapter=new EventListAdaptor(this.getActivity(), listGoingEvents);
        rcvGoingEvents.setAdapter(goingEventsAdapter);
        rcvGoingEvents.setVisibility(View.GONE);
        goingEventsAdapter.notifyDataSetChanged();

        tabLayout.getTabAt(0).setText("Interested ("+listInterestedEvents.size()+")");
        tabLayout.getTabAt(1).setText("Going ("+listGoingEvents.size()+")");

    }

    private void fetchEventsData() {
        listInterestedEvents = new ArrayList<ListItems>();
        listGoingEvents = new ArrayList<ListItems>();

        final String urlPost = "https://socupdate.herokuapp.com/events/marked";
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading data....");
        progressDialog.show();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()) {
                        final HashMap<String, String> mapToken = new HashMap<String, String>();
                        mapToken.put("token", task.getResult().getToken());
                        RequestQueue requstQueue = Volley.newRequestQueue(requireContext());
                        progressDialog.dismiss();
                        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, urlPost, new JSONObject(mapToken),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Iterator<String> keys = response.keys();
                                        while (keys.hasNext()) {
                                            String eventId = keys.next();
                                            try {
                                                JSONObject jsonObject = response.getJSONObject(eventId);
                                                String marker = "none";
                                                if (jsonObject.has("markedAs")) {
                                                    marker = jsonObject.getString("markedAs");
                                                    ArrayList<String> list= new ArrayList<>();
                                                    ArrayList<String> nameList=new ArrayList<>();
                                                    if(jsonObject.has("attachments")) {
                                                        JSONObject jsonObject1 = jsonObject.getJSONObject("attachments");
                                                        Iterator iterator= jsonObject1.keys();
                                                        while(iterator.hasNext()){
                                                            String name= iterator.next().toString();
                                                            list.add(jsonObject1.getString(name));
                                                            nameList.add(name);
                                                        }
                                                    }
                                                    ListItems item = new ListItems(
                                                            jsonObject.getString("name"),
                                                            jsonObject.getString("desc"),
                                                            jsonObject.getString("byName"),
                                                            jsonObject.getString("date"),
                                                            "Time: " + jsonObject.getString("time"),
                                                            "Venue: " + jsonObject.getString("venue"), marker, eventId,list
                                                    );
                                                    item.setNameList(nameList);
                                                    item.setPhotoUrl(jsonObject.getString("photoURL"));
                                                    if(marker.equals("interested")) {
                                                        listInterestedEvents.add(item);
                                                    }
                                                    else if (marker.equals("going")){
                                                        listGoingEvents.add(item);
                                                    }

                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        loadRecyclerView();

                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                }
                        );
                        requstQueue.add(jsonobj);
                    }
                }
            });

        }
    }
    public void deleteRequest(String id) throws JSONException {
        databaseHandler.updateMarker(databaseHandler.getEvent(id),"none");
        Snackbar.make(constraintLayout,"Unmarked",Snackbar.LENGTH_LONG).show();
        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        final String url="https://socupdate.herokuapp.com/events/"+id+"/mark/delete";
        if(user!=null){
            user.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if(task.getResult().getToken()!=null){
                        HashMap<String,String> mapToken=new HashMap<String, String>();
                        mapToken.put("token",task.getResult().getToken());
                        Log.d("deleteToken", String.valueOf(new JSONObject(mapToken)));
                        final RequestQueue requstQueue = Volley.newRequestQueue(requireContext());
                        JsonObjectRequest jsonobj = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(mapToken),
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("respone",String.valueOf(response));
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("error",error.toString());
                                    }
                                }
                        );
                        requstQueue.add(jsonobj);
                    }
                }
            });
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        if (isOnline()) {
            if (eventType == EVENT_INTERESTED) {
                interestedEventsAdapter.notifyDataSetChanged();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
                builder.setMessage("Are you sure you want to Delete " + "\"" + listInterestedEvents.get(position).getName() + "\"" + " from your list of Interested Events")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    databaseHandler.updateCount(listInterestedEvents.get(position),"-","interested");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    deleteRequest(listInterestedEvents.get(position).getEventId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                listInterestedEvents.remove(position);
                                interestedEventsAdapter.notifyDataSetChanged();
                                if (listInterestedEvents.size() == 0) {
                                    tvNoEvent.setVisibility(View.VISIBLE);
                                }
                                tabLayout.getTabAt(0).setText("Interested (" + listInterestedEvents.size() + ")");

                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar.make(constraintLayout, "Marker Deletion Cancelled", Snackbar.LENGTH_SHORT).show();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                goingEventsAdapter.notifyDataSetChanged();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
                builder.setMessage("Are you sure you want to Delete " + "\"" + listGoingEvents.get(position).getName() + "\"" +" from your list of Going Events")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    databaseHandler.updateCount(listGoingEvents.get(position),"-","going");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    deleteRequest(listGoingEvents.get(position).getEventId());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                listGoingEvents.remove(position);
                                goingEventsAdapter.notifyDataSetChanged();
                                if (listGoingEvents.size() == 0) {
                                    tvNoEvent.setVisibility(View.VISIBLE);
                                }
                                tabLayout.getTabAt(1).setText("Going (" + listGoingEvents.size() + ")");
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar.make(constraintLayout, "Marker Deletion Cancelled", Snackbar.LENGTH_LONG).show();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else {
            Snackbar.make(constraintLayout,"Network not available! Cannot proceed request.",Snackbar.LENGTH_LONG).show();
        }
    }
}