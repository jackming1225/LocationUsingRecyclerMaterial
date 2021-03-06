package com.example.ming.locationusingrecyclermaterial;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MarketHolder> {


    ArrayList<Market> marketDetails = new ArrayList();

    public RecyclerAdapter(ArrayList<Market> marketDetails) {
        this.marketDetails = marketDetails;
    }

    public void add(Market object) {
        marketDetails.add(object);
    }

    @Override
    public MarketHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_item, parent, false);

        MarketHolder marketHolder = new MarketHolder(view);
        return marketHolder;
    }

    @Override
    public void onBindViewHolder(final MarketHolder holder, int position) {

        Market market = marketDetails.get(position);

        holder.name.setText("NAME   : " + market.getName());
        holder.state.setText("STATE : " + market.getState());
        holder.district.setText("DISTRICT   : " + market.getDistrict());
        holder.distance.setText("DISTANCE   : " + market.getDistance());
    }

    @Override
    public int getItemCount() {
        return marketDetails.size();
    }


    public static class MarketHolder extends RecyclerView.ViewHolder {
        TextView name, state, district, distance;

        public MarketHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tNameDetails);
            state = (TextView) itemView.findViewById(R.id.tStateDetails);
            distance = (TextView) itemView.findViewById(R.id.tDistanceDetails);
            district = (TextView) itemView.findViewById(R.id.tDistrictDetails);
        }
    }
}
