package gr.kgdev.batmobile.fragments;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gr.kgdev.batmobile.R;
import gr.kgdev.batmobile.activities.MainActivity;
import gr.kgdev.batmobile.models.User;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    private final MainActivity activity;
    private List<User> usersDataset;
    private HashMap<Integer, Integer> notificationsMap = new HashMap<>();
    private MyViewHolder viewHolder ;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public RelativeLayout linearLayout;

        public MyViewHolder(RelativeLayout v) {
            super(v);
            linearLayout = v;
        }

        @Override
        public void onClick(View v) {
            System.out.println("@@@@@@@@@@@ Clicked!");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UsersAdapter(ArrayList<User> myDataset, Activity activity) {
        this.usersDataset = myDataset;
        this.activity = activity instanceof MainActivity ? (MainActivity) activity : null ;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        this.viewHolder = holder;
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        User user = usersDataset.get(position);
        ((TextView) holder.linearLayout.findViewById(R.id.username)).setText(user.getUsername());

        TextView notificationTextView = (TextView) viewHolder.linearLayout.findViewById(R.id.badge);
        if (notificationsMap.containsKey(user.getId())) {
            Integer count =  notificationsMap.get(user.getId());
            if (count > 0 ) {
                notificationTextView.setText(count.toString());
                notificationTextView.setVisibility(View.VISIBLE);
            }
            else {
                notificationTextView.setVisibility(View.GONE);
            }
        }
        else {
            notificationTextView.setVisibility(View.GONE);
        }

        if (user.isActive()) {
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setText("Active");
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setTextColor(Color.BLUE);
        } else {
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setText("Inactive");
            ((TextView) holder.linearLayout.findViewById(R.id.status)).setTextColor(Color.RED);
        }

        ((ImageView) holder.linearLayout.findViewById(R.id.image)).setImageResource(R.drawable.batmobile);
        holder.linearLayout.setOnClickListener(v -> loadChatFragment(user));
    }

    private void loadChatFragment(User user) {
        try {
            activity.loadChatFragment(user);
        } catch (JSONException e) {
            e.printStackTrace();
            activity.runOnUiThread(() -> Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    public User getItem(int pos) {
        return usersDataset.get(pos);
    }

    public synchronized void setNotificationsForUser(Integer userId, Integer num) {
        User targetUser = usersDataset.stream().filter(user -> user.getId() == userId).findAny().get();
        notificationsMap.put(targetUser.getId(), num);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return usersDataset.size();
    }

    public int getTotalUnreadMessagesCount() {
        return notificationsMap.values().stream().mapToInt(o -> o).sum();
    }
}