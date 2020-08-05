package gr.kgdev.batmobile.fragments;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.models.User;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    private ArrayList<User> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout linearLayout;

        public MyViewHolder(RelativeLayout v) {
            super(v);
            linearLayout = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UsersAdapter(ArrayList<User> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_list_item, parent, false);
//        ...
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        User user = mDataset.get(position);
        ((TextView) holder.linearLayout.findViewById(R.id.username)).setText(user.getUsername());
        TextView notificationTextView = (TextView) holder.linearLayout.findViewById(R.id.badge);
        notificationTextView.setText("1");
        if (user.isActive()) {
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setText("Active");
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setTextColor(Color.BLUE);
        } else {
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setText("Inactive");
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setTextColor(Color.RED);
        }
        ((ImageView) holder.linearLayout.findViewById(R.id.image)).setImageResource(R.drawable.batmobile);
        holder.linearLayout.setOnClickListener(v -> {
            System.out.println("Clicked!");
            try {
                MainActivity.loadChatFragment(user);
            } catch (JSONException e) {
                e.printStackTrace();
                MainActivity.getInstance().runOnUiThread(() -> Toast.makeText(MainActivity.getInstance(), e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}