package com.example.booktrack.filters;
import android.widget.Filter;

import com.example.booktrack.adapters.AdapterCategory;
import com.example.booktrack.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {
    //aramak istediğimiz arraylist
    ArrayList<ModelCategory> filterList;
    //filtrenin uygulanması gereken adapter
    AdapterCategory adapterCategory;

    //constructor
    public FilterCategory(ArrayList<ModelCategory> filterList, AdapterCategory adapterCategory) {
        this.filterList = filterList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value boş ya da null olmamalı
        if (constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels = new ArrayList<>();
            for (int i=0; i< filterList.size(); i++){
                //doğrulama
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //filtre değişimlerini uygulama
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;

        //değişimleri bildirme
        adapterCategory.notifyDataSetChanged();
    }
}
