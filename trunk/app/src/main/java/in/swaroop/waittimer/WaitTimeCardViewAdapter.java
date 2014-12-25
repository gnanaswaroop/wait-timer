package in.swaroop.waittimer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by swaroop on 12/22/2014.
 */
public class WaitTimeCardViewAdapter extends RecyclerView.Adapter<WaitTimeCardViewAdapter.ViewHolder> {

    // private String[] mDataset;
    private List<String> mDataset;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.text_row_item, parent, false);

        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    public WaitTimeCardViewAdapter(List<String> myDataset) {
        this.mDataset = myDataset;
    }

    public void insertMoreRecords(String newData) {
        mDataset.add(newData);
        notifyItemChanged(mDataset.size());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public TextView mTextView;
        public ViewHolder(View itemView) {
            super(itemView);

            mTextView = (TextView) itemView.findViewById(R.id.textView);
        }
    }

}
