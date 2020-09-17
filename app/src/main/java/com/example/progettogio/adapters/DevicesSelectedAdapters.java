package com.example.progettogio.adapters;

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

public class DevicesSelectedAdapters extends  RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = "DeviesSelectedAdapter";
    private List<GeneralDevice> selectedDevices;
    private DevsSelectedListener devsSelectedListener;

    public DevicesSelectedAdapters(List<GeneralDevice> devices, DevsSelectedListener listener){
        this.selectedDevices = devices;
        this.devsSelectedListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==1){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ThingyItemBinding itemBinding = ThingyItemBinding.inflate(layoutInflater , parent, false);
            return new DevicesSelectedAdapters.NordicViewHolder(itemBinding.getRoot());
        }
        //WAGOO
        else if (viewType == 2){
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            WagooItemBinding wagooBinding = WagooItemBinding.inflate(layoutInflater, parent, false);
            return new DevicesSelectedAdapters.WagooViewHolder(wagooBinding.getRoot());
        }
        else{
            //FOR WATCH, FOR NOW LEAVING THE WAGGO LAYOUT
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            WagooItemBinding wagooBinding = WagooItemBinding.inflate(layoutInflater, parent, false);
            return new DevicesSelectedAdapters.WagooViewHolder(wagooBinding.getRoot());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 1){
            ((DevicesSelectedAdapters.NordicViewHolder)holder).setItem(selectedDevices.get(position));

        }
        //WAGOO
        if(getItemViewType(position) == 2) {
            ((DevicesSelectedAdapters.WagooViewHolder) holder).setItem(selectedDevices.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return selectedDevices.size();
    }

    @Override
    public int getItemViewType(int position) {
        //THINGY
        if (selectedDevices.get(position).getType()== DevicesEnum.NORDIC)
            return 1;
        //WAGOO
        if (selectedDevices.get(position).getType()== DevicesEnum.WAGOOGLASSES)
            return 2;
        //
        if (selectedDevices.get(position).getType()== DevicesEnum.SMARTWATCH)
            return 3;
        return 0;
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


        private void setItem(GeneralDevice device){

            binding.deviceName.setText(device.getDeviceName());
            binding.deviceAddress.setText(device.getAddress());
//            binding.deviceBattery.setText(device.getBatteryLevel()+"%");
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            devsSelectedListener.onSelectedDeviceClick(selectedDevices.get(this.getAdapterPosition()).getAddress());
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                Log.d(TAG, "onClick: getAdapterPosition() == RecyclerView.NO_POSITION");
                return false;
            }
            devsSelectedListener.onSelectedDeviceLongClick(selectedDevices.get(this.getAdapterPosition()).getAddress());
            return false;
        }
    }

    public class WagooViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        private GeneralDevice mWagooDevice;
        private WagooItemBinding binding;
//        private ItemClickListener mItemClickListener;

        public WagooViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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
                return;
            }
            devsSelectedListener.onSelectedDeviceClick(selectedDevices.get(this.getAdapterPosition()).getAddress());
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                return false;
            }
            Log.d(TAG, "onLongClick: ");
            devsSelectedListener.onSelectedDeviceLongClick(selectedDevices.get(this.getAdapterPosition()).getAddress());
            return false;
        }
    }
}
