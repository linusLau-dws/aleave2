package hk.com.dataworld.leaveapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventListFragment extends Fragment {
    private ListView mListView = null;
    private EventListAdapter mEventListAdapter= null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_event_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inflate event_fragment here.
        mListView = view.findViewById(R.id.eventListView);
//        mEventListAdapter = new EventListAdapter(getContext(), new JSONArray());
        mListView.setAdapter(mEventListAdapter);
    }
}
