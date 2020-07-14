package com.example.progettogio.adapters;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.example.progettogio.databinding.ThingyItemBinding;

import java.util.List;

public class DevicesSelectedAdapters extends RecyclerView.Adapter<DevicesSelectedAdapters.NordicViewHolder> {

    private final static String TAG = "DeviesSelectedAdapter";
    private List<BluetoothDevice> selectedDevies;
    private DevsSelectedListener devsSelectedListener;

    public DevicesSelectedAdapters(List<BluetoothDevice> devices, DevsSelectedListener listener){
        this.selectedDevies = devices;
        this.devsSelectedListener = listener;
    }

    @NonNull
    @Override
    public NordicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ThingyItemBinding itemBinding = ThingyItemBinding.inflate(layoutInflater, parent, false);
        return new NordicViewHolder(itemBinding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull NordicViewHolder holder, int position) {
        holder.setItem(selectedDevies.get(position));
    }

    @Override
    public int getItemCount() {
        return selectedDevies.size();
    }

    /**
     *
     */
    public class NordicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{


        private ThingyItemBinding binding;

        public NordicViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        private void setItem(BluetoothDevice device){

            binding.deviceName.setText(device.getName());
            binding.deviceAddress.setText(device.getAddress());
        }

        @Override
        public void onClick(View v) {
            devsSelectedListener.onDevSelectecClick(selectedDevies.get(this.getAdapterPosition()).getAddress());
        }

        @Override
        public boolean onLongClick(View v) {
            devsSelectedListener.onDevSelectedLongClick(selectedDevies.get(this.getAdapterPosition()).getAddress());
            return false;
        }
    }
}
