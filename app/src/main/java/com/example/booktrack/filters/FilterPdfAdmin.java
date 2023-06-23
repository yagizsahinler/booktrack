package com.example.booktrack.filters;
import android.widget.Filter;

import com.example.booktrack.adapters.AdapterCategory;
import com.example.booktrack.adapters.AdapterPdfAdmin;
import com.example.booktrack.models.ModelCategory;
import com.example.booktrack.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {
    //aramak istediğimiz arraylist
    ArrayList<ModelPdf> filterList;
    //filtrenin uygulanması gereken adapter
    AdapterPdfAdmin adapterPdfAdmin;

    //constructor
    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = filterList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value boş ya da null olmamalı
        if (constraint != null && constraint.length() > 0){
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels = new ArrayList<>();
            for (int i=0; i< filterList.size(); i++){
                //doğrulama
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;

        //değişimleri bildirme
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
