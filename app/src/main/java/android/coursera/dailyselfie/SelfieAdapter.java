package android.coursera.dailyselfie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
public class SelfieAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Selfie> selfies;

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
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.selfie_list_layout, null);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);

        Selfie selfie = selfies.get(i);
        Bitmap bitmap = selfie.getSelfie();
        if (bitmap == null) {
            System.err.println("bitmap == null");
        } else {
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, 200,300,true);
            imageView.setImageBitmap(resized);
        }
        textView.setText(selfie.getName());
        return rowView;
    }

    public void addItem(Selfie selfie) {
        selfies.add(selfie);
        notifyDataSetChanged();
    }

}
