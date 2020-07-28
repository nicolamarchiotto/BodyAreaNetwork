package com.example.progettogio.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.progettogio.databinding.SensorItemBinding;
import com.example.progettogio.models.NordicSensorList;
import com.example.progettogio.models.NordicSensorList.NordicSensor;

public class SensorsAdapter extends RecyclerView.Adapter<SensorsAdapter.ThingySensorViewHolder> {

    private static final String TAG = "SensorsAdapter";

    public NordicSensorList sensorList;

    public SensorsAdapter(NordicSensorList list){
        sensorList=list;
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


    public class ThingySensorViewHolder extends RecyclerView.ViewHolder {

        private SensorItemBinding binding;

        public ThingySensorViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
//            itemView.setOnClickListener(this);
        }

        public void setItem(NordicSensor nordicSensor){
            binding.itemDeviceSensorCheckbox.setChecked(nordicSensor.getState());
            binding.itemDeviceSensorName.setText(nordicSensor.getName());
        }
    }
}
