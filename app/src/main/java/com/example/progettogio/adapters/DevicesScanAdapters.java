package com.example.progettogio.adapters;

import android.bluetooth.BluetoothDevice;
import android.telephony.mbms.MbmsErrors;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.example.progettogio.databinding.ThingyItemBinding;
import com.example.progettogio.models.GeneralDevice;
import com.example.progettogio.utils.DevicesEnum;

import java.util.List;

public class DevicesScanAdapters extends RecyclerView.Adapter<DevicesScanAdapters.NordicScanViewHolder>{

    private final static String TAG = "DeviceScanAdapters";
    private List<GeneralDevice> deviceList;

    private DevsScanListener devsScanListener;



    public DevicesScanAdapters(List<GeneralDevice> devices, DevsScanListener listener){
        this.deviceList = devices;
        this.devsScanListener = listener;
    }

    @NonNull
    @Override
    public NordicScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ThingyItemBinding itemBinding = ThingyItemBinding.inflate(layoutInflater , parent, false);
        return new NordicScanViewHolder(itemBinding.getRoot());
    }


    @Override
    public void onBindViewHolder(@NonNull NordicScanViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder: creo");
        holder.setItem(deviceList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        //THINGY
        if (deviceList.get(position).getType()== DevicesEnum.NORDIC)
            return 1;
        //WAGOO
        if (deviceList.get(position).getType()== DevicesEnum.WAGOOGLASSES)
            return 2;
        //
        if (deviceList.get(position).getType()== DevicesEnum.WAGOOGLASSES)
            return 3;
        return 0;
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }




    /**
     *
     */
    public class NordicScanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        private ThingyItemBinding binding;

        public NordicScanViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
        }


        public void setItem(GeneralDevice device){

            binding.deviceName.setText(device.getBluetoothDevice().getName());
            binding.deviceAddress.setText(device.getAddress());

        }


        @Override
        public void onClick(View v) {
            //Prova per risolvere problema:java.lang.ArrayIndexOutOfBoundsException: length=10; index=-1
            //Problema sembra essere così risolto, riscontrato quando si effetua la connessione di più dispositivi molto velocemente
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                Log.d(TAG, "onClick: getAdapterPosition() == RecyclerView.NO_POSITION");
                return;
            }
            GeneralDevice d=deviceList.get(this.getAdapterPosition());
            int position=deviceList.indexOf(d);
            Log.d(TAG, "onClick:\nadapter position:"+getAdapterPosition()+"\nobject position in devicelist: " +position);

        devsScanListener.onDeviceSelected(deviceList.get(this.getAdapterPosition()).getAddress());
        }
    }
}
