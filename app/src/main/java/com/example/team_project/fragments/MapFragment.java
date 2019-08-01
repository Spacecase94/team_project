package com.example.team_project.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.team_project.BottomNavActivity;
import com.example.team_project.PublicVariables;
import com.example.team_project.R;
import com.example.team_project.api.DirectionsApi;
import com.example.team_project.details.DetailsActivity;
import com.example.team_project.model.PlaceEvent;
import com.example.team_project.model.Post;
import com.example.team_project.model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
// TODO add comment
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private Unbinder unbinder;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mgoogleMap;
    private ImageButton mMapIcon;
    private ParseUser user = ParseUser.getCurrentUser();
    private ArrayList<String> likedEvents = (ArrayList<String>) user.get(User.KEY_LIKED_EVENTS);
    private int maxLimit = 1000;
    private int minZoom = 3;
    private String TAG = "MapFragment";
    private String apiId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        Drawable loginActivityBackground = view.findViewById(R.id.mapicon).getBackground();
        loginActivityBackground.setAlpha(230);

        mMapIcon = view.findViewById(R.id.mapicon);
        mMapIcon.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               enableMyLocationIfPermitted();
               mgoogleMap.setMinZoomPreference(minZoom);
           }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mgoogleMap = map;
        mgoogleMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mgoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mgoogleMap.getUiSettings().setCompassEnabled(true);
        mgoogleMap.setMinZoomPreference(minZoom);
        enableMyLocationIfPermitted();
        queryReviews();
        queryLikedEvents();
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {
        apiId = marker.getTag().toString();
        DirectionsApi api = new DirectionsApi(MapFragment.this);
        api.setOrigin(BottomNavActivity.currentLat, BottomNavActivity.currentLng);
        PlaceEvent mParseEvent = query(apiId);
        api.addDestination(mParseEvent.getCoordinates().replace(" ", ","));
        api.getDistance();
    }

    public void gotDistance(String distanceApi) {
        Boolean spotType = getSpotType(apiId);
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("eventID", apiId);
        intent.putExtra("type", spotType);
        intent.putExtra("distance", distanceApi);
        startActivity(intent);
    }

    public PlaceEvent query(String id) {
        ParseQuery<PlaceEvent> query = new ParseQuery("PlaceEvent");
        query.whereContains("apiId", id);
        PlaceEvent mPlaceEvent = null;
        try {
            mPlaceEvent = (PlaceEvent) query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mPlaceEvent;
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mgoogleMap != null) {
            mgoogleMap.setMyLocationEnabled(true);
            mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(BottomNavActivity.currentLat, BottomNavActivity.currentLng) , 8));

        }
    }

    private void showDefaultLocation() {
        Toast.makeText(getContext(),R.string.location_permission_denied,
                Toast.LENGTH_SHORT).show();
        mgoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.8283, -98.5795) , 0));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    showDefaultLocation();
                }
                return;
            }
        }
    }

    public GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
        new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mgoogleMap.setMinZoomPreference(minZoom);
                return false;
            }
        };

    protected void queryReviews(){
        ParseQuery<Post> reviewQuery = new ParseQuery<Post>(Post.class);
        reviewQuery.setLimit(maxLimit);
        reviewQuery.include(Post.KEY_EVENT_PLACE);

        reviewQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
            if (e != null) {
                Log.e(TAG, "error with query: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            for(int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                String review = post.getReview();
                String name = post.getEventPlace().getName();
                String apiId = post.getEventPlace().getAppId();
                String coordinates = post.getCoordinates();
                Float color = BitmapDescriptorFactory.HUE_RED;
                if (post.getCoordinates() != null){
                    makeMapMarker(coordinates, apiId, name, review, color);
            }}
            }
        });
    }

    protected void queryLikedEvents(){
        ParseQuery placeEventQuery = new ParseQuery("PlaceEvent");
        placeEventQuery.setLimit(maxLimit);
        ArrayList<String> likedEventApis = new ArrayList<>();
        for (int i= 0; i < likedEvents.size();i++){
            String api = likedEvents.get(i).split(PublicVariables.splitindicator)[0];
            likedEventApis.add(api);
        }
        placeEventQuery.whereContainedIn(PlaceEvent.KEY_API, likedEventApis);

        placeEventQuery.findInBackground(new FindCallback<PlaceEvent>() {
            @Override
            public void done(List<PlaceEvent> placeEvents, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error with query: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                for (int i = 0; i < placeEvents.size(); i++) {
                    String placeEventCoord = placeEvents.get(i).getCoordinates();
                    String placeEventName = placeEvents.get(i).getName();
                    String apiId = placeEvents.get(i).getAppId();
                    Float color = BitmapDescriptorFactory.HUE_ROSE;
                    String snippet = getResources().getString(R.string.liked_event_snippet);
                    makeMapMarker(placeEventCoord, apiId, placeEventName, snippet, color);
                }
            }
        });
    }

    protected void makeMapMarker(String coordinateString, String apiId, String placeEventName, String snippet, Float color) {
        String[] coordinates = coordinateString.split("\\s+");
        double latitude = Double.parseDouble(coordinates[0]);
        double longitude = Double.parseDouble(coordinates[1]);
        Marker marker = mgoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(placeEventName)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(color)));
        marker.setTag(apiId);
        mgoogleMap.setOnInfoWindowClickListener(MapFragment.this);
    }

    protected Boolean getSpotType(String apiId){
        if ('E' != apiId.charAt(0)) {
            PublicVariables.spotType = true;
        } else {
            PublicVariables.spotType = false;
        }
        return PublicVariables.spotType;
    }
}
