package com.example.team_project.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.team_project.CalendarAdapter;
import com.example.team_project.R;
import com.example.team_project.model.User;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.parse.ParseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.parse.Parse.getApplicationContext;

public class MyCalendarFragment extends Fragment {
    Context context;
    private Unbinder unbinder;
    CompactCalendarView compactCalendar;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    TextView CurrentDate;
    TextView tvEventName;
    RecyclerView rvCal;
    CalendarAdapter calendarAdapter;
    Long epochTime;
    ParseUser user = ParseUser.getCurrentUser();
    ArrayList<String> addedEvents = (ArrayList<String>) user.get(User.KEY_ADDED_EVENTS);
    ArrayList<String> theDaysEvents;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_calendar, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCal = view.findViewById(R.id.rvCal);
        tvEventName = view.findViewById(R.id.tvEventName);
        theDaysEvents = new ArrayList<>();

        CurrentDate = view.findViewById(R.id.current_Date);
        String currentDate = dateFormat.format(calendar.getTime());
        CurrentDate.setText(currentDate);

        compactCalendar = view.findViewById(R.id.compactcalendar_view);
        compactCalendar.setUseThreeLetterAbbreviation(true);

        //example and testing on adding an event
        Event ev1 = new Event(Color.BLUE, 1564167600000L);
        compactCalendar.addEvent(ev1);

        // add all events to calendar
        if (addedEvents != null) {
            for (int x = 0; x < addedEvents.size(); x++) {
                Event ev = null;
                try {
                    ev = new Event(Color.BLACK, myMilliSecConvert(addedEvents.get(x).substring(0, 9)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                compactCalendar.addEvent(ev);
            }
        }

        // retrieve events on clicked on day and display in recycler view
        // multiple Log outputs are present for testing purposes
        compactCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                Context context = getApplicationContext();

                Log.d("MyCalendarFragment", "Array List(Added Events:" + addedEvents);

                String numberDate = simpleDateFormat.format(dateClicked);
                Log.d("MyCalendarFragment", "new date:" + numberDate);

                if (addedEvents != null) {
                    for (int x = 0; x < addedEvents.size(); x++) {
                        if (numberDate.equals(addedEvents.get(x).substring(0, 9))) {
                            String eventName = addedEvents.get(x).substring(11);
                            Log.d("MyCalendarFragment", "Same Day event:" + eventName);
                            //make list with only events on clicked date
                            theDaysEvents.add(eventName);
                        }
                    }
                }

                // Extra information for testing
                List<Event> events = compactCalendar.getEvents(dateClicked);
                Log.d("MyCalendarFragment", "Day was clicked: " + dateClicked + " with events " + events);
                // get the exact name and id of the event
                Toast.makeText(context, "teatt" + events, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                CurrentDate.setText(dateFormat.format(firstDayOfNewMonth));
            }
        });

        if (theDaysEvents != null) {
            setRecyclerView(view);
        }
    }


    private void setRecyclerView(View view) {
        calendarAdapter = new CalendarAdapter(theDaysEvents);
        rvCal.setAdapter(calendarAdapter);
        rvCal.setLayoutManager(new LinearLayoutManager(getContext()));

        //create endless recycling view
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

        // RecyclerView setup (layout manager, use adapter)
        rvCal.setLayoutManager(linearLayoutManager);
        // set the adapter
        rvCal.setAdapter(calendarAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

        public long myMilliSecConvert (String date) throws ParseException {
            // convert string to date
            Date milliDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            //convert date to Milliseconds
            epochTime = milliDate.getTime();
            return epochTime;
        }
}
