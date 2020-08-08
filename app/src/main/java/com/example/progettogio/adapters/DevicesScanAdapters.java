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
import com.example.progettogio.databinding.WagooItemBinding;
import com.example.progettogio.models.GeneralDevice;
import com.example.progettogio.utils.DevicesEnum;

import java.util.List;

public class DevicesScanAdapters extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final static String TAG = "DeviceScanAdapters";
    private List<GeneralDevice> deviceList;

    private DevsScanListener devsScanListener;



    public DevicesScanAdapters(List<GeneralDevice> devices, DevsScanListener listener){
        this.deviceList = devices;
        this.devsScanListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        if(viewType==1){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ThingyItemBinding itemBinding = ThingyItemBinding.inflate(layoutInflater , parent, false);
            return new NordicScanViewHolder(itemBinding.getRoot());
        }
        //WAGOO
        else if (viewType == 2){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            WagooItemBinding wagooBinding = WagooItemBinding.inflate(layoutInflater, parent, false);
            return new WagooScanViewHolder(wagooBinding.getRoot());
        }
        else{
            //FOR WATCH, FOR NOW LEAVING THE WAGGO LAYOUT
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            WagooItemBinding wagooBinding = WagooItemBinding.inflate(layoutInflater, parent, false);
            return new WagooScanViewHolder(wagooBinding.getRoot());
        }



    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1){
            ((NordicScanViewHolder)holder).setItem(deviceList.get(position));

        }

        //WAGOO
        if(getItemViewType(position) == 2) {
            ((WagooScanViewHolder) holder).setItem(deviceList.get(position));
        }
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

    public class WagooScanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private GeneralDevice mWagooDevice;
        private WagooItemBinding binding;
//        private ItemClickListener mItemClickListener;

        public WagooScanViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
        }

        public void setItem(GeneralDevice device){
            mWagooDevice=device;
            binding.wagooAddress.setText(device.getAddress());
            binding.wagooName.setText(device.getBluetoothDevice().getName());

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
