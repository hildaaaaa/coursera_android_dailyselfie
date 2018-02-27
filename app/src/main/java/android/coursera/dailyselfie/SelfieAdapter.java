package android.coursera.dailyselfie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hildachung on 5/2/2018.
 */

public class SelfieAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<Selfie> selfies;

    public SelfieAdapter(Context context, ArrayList<Selfie> selfies) {
        this.mContext = context;
        this.selfies = selfies;
    }

    @Override
    public int getCount() {
        return selfies.size();
    }

    @Override
    public Object getItem(int i) {
        return selfies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return selfies.indexOf(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            System.out.println("view == null, assigning");
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.selfie_list_layout, null);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
            TextView textView = (TextView) rowView.findViewById(R.id.textView);

            imageView.setMaxHeight(500);
            imageView.setImageBitmap(selfies.get(i).getSelfie());
            textView.setText(selfies.get(i).getName());
            return rowView;
        }
        return view;
    }

    public void addItem(Selfie selfie) {
        selfies.add(selfie);
        notifyDataSetChanged();
    }
}
