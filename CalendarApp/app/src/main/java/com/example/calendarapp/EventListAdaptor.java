package com.example.calendarapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class EventListAdaptor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<ListItems> eventList;
    Context context;
    public EventListAdaptor(Context context, ArrayList<ListItems> eventList){
        this.context=context;
        this.eventList=eventList;
    }

    public class EventListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imgSocietyLogo;
        TextView tvEventName, tvSocietyName, tvEventDate, tvEventTime;
        RelativeLayout viewBackground, viewForeground;

        public EventListViewHolder(@NonNull View itemView) {
            super(itemView);

            imgSocietyLogo=itemView.findViewById(R.id.imgEventListSocietyLogo);
            tvEventName=itemView.findViewById(R.id.tvEventListName);
            tvSocietyName=itemView.findViewById(R.id.tvEventListSociety);
            tvEventDate=itemView.findViewById(R.id.tvEventListDate);
            tvEventTime=itemView.findViewById(R.id.tvEventListTime);
            viewBackground=itemView.findViewById(R.id.viewEventListBackground);
            viewForeground=itemView.findViewById(R.id.viewEventListForeground);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent =new Intent(context,EventActivity.class);
            ListItems items =eventList.get(this.getAdapterPosition());
            intent.putExtra("name",items.getName());
            intent.putExtra("byName",items.getByName());
            intent.putExtra("desc",items.getDesc());
            intent.putExtra("time",items.getTime());
            intent.putExtra("venue",items.getVenue());
            intent.putExtra("date",items.getDate());
            intent.putExtra("marker",items.getMarker());
            intent.putExtra("eventId",items.getEventId());
            intent.putExtra("type","event");
            intent.putExtra("screen","profile");
            intent.putStringArrayListExtra("attachments", items.getArrayList());
            intent.putStringArrayListExtra("attachments_name",items.getNameList());
            context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
            return new EventListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof EventListViewHolder) {
            Picasso
                    .get()
                    .load(Uri.parse(eventList.get(position).getPhotoUrl()))
                    .resize(65, 65)
                    .transform(new CropCircleTransformation())
                    .into(((EventListViewHolder) holder).imgSocietyLogo);

            ((EventListViewHolder)holder).tvEventName.setText(eventList.get(position).getName());
            ((EventListViewHolder)holder).tvSocietyName.setText(eventList.get(position).getByName());
            ((EventListViewHolder)holder).tvEventDate.setText(eventList.get(position).getDate());
            ((EventListViewHolder)holder).tvEventTime.setText(eventList.get(position).getTime());
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
