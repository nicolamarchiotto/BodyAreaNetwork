package com.example.progettogio.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettogio.databinding.SensorItemBinding;
import com.example.progettogio.models.NordicSensorList;
import com.example.progettogio.models.NordicSensorList.NordicSensor;

public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.ThingySensorViewHolder> {

    private static final String TAG = "SensorsAdapter";

    private NordicSensorList sensorList;
    private DevsSensorsListener sensorsListener;

    public SensorsAdapter(NordicSensorList list, DevsSensorsListener listener){
        sensorList=list;
        sensorsListener=listener;
    }

    @NonNull
    @Override
    public ThingySensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        SensorItemBinding sensorItemBinding= SensorItemBinding.inflate(layoutInflater , parent, false);
        return new SensorsAdapter.ThingySensorViewHolder(sensorItemBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull ThingySensorViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: creo");
        holder.setItem(sensorList.get(position));
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }


    public class ThingySensorViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        private static final String TAG = "ThingySensorViewHolder";

        private SensorItemBinding binding;
        private TextView itemTextView;
        private CheckBox itemCheckBox;


        public ThingySensorViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemTextView=binding.itemDeviceSensorName;
            itemCheckBox=binding.itemDeviceSensorCheckbox;
            itemView.setOnClickListener(this);
        }

        public void setItem(NordicSensor nordicSensor){
            itemCheckBox.setChecked(nordicSensor.getState());
            itemTextView.setText(nordicSensor.getName());
        }

        @Override
        public void onClick(View v) {
            if(itemCheckBox.isChecked()){
                Log.d(TAG, "onClick: "+getAdapterPosition());
                itemCheckBox.setChecked(false);
                sensorsListener.onCheckBoxClicked(getAdapterPosition(),false);
            }
            else{
                Log.d(TAG, "onClick: "+getAdapterPosition());
                itemCheckBox.setChecked(true);
                sensorsListener.onCheckBoxClicked(getAdapterPosition(),true);
            }
        }
    }
}
