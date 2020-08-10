package com.example.osmdroid.Modelo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.osmdroid.R;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestAdapter<T> extends ArrayAdapter<String> {

    private Context context;
    private List<String> items, tempItems;

    public AutoSuggestAdapter(Context context, int resource, int
            textViewResourceId, List<String> items) {
        super(context, resource, textViewResourceId, items);
        this.context = context;
        this.items = items;
        tempItems = new ArrayList<>(items);// this makes the difference.
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.autocompletestreets_layout, parent, false);
        }
        String name = items.get(position);
        if (name != null) {
            TextView lblName = view.findViewById(R.id.autoCompleteStreets);
            if (lblName != null)
                lblName.setText(name);
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return containsFilter;
    }
    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter containsFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return (String) resultValue;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {

                //suggestions.clear();
                final List<String> suggestions = new ArrayList<>();
                FilterResults filterResults = new FilterResults();

                for (String name : tempItems) {
                    if (name.toLowerCase().contains(constraint.toString().toLowerCase())) {

                        suggestions.add(name);
                    }
                }

                filterResults.values = suggestions;
                filterResults.count = suggestions.size();


                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults
                results) {
            if (results != null && results.count > 0) {
                clear();
                addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };
}