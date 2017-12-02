package edu.wisc.ece454.hu_mon.Utilities;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import edu.wisc.ece454.hu_mon.Models.Move;
import edu.wisc.ece454.hu_mon.R;

/**
 * Created by Michael on 11/9/2017.
 */

public class MoveListAdapter extends BaseAdapter {

    private List<Move> moveList;
    private Activity activity;

    private TextView nameView;
    private TextView descView;
    private TextView effectView;
    private TextView damageView;
    private TextView selfCastView;

    public MoveListAdapter(Activity activity, List<Move> moveList) {
        super();
        this.activity = activity;
        this.moveList = moveList;
    }

    @Override
    public int getCount() {
        return moveList.size();
    }

    @Override
    public Object getItem(int position) {
        return moveList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.move_list_element,null);
        }

        nameView = (TextView) convertView.findViewById(R.id.nameTextView);
        descView = (TextView) convertView.findViewById(R.id.descTextView);
        damageView = (TextView) convertView.findViewById(R.id.damageTextView);
        effectView = (TextView) convertView.findViewById(R.id.effectTextView);
        selfCastView = (TextView) convertView.findViewById(R.id.selfCastTextView);

        Move move = moveList.get(position);

        if(nameView != null) {
            nameView.setText(move.getName());
        }

        if(descView != null) {
            descView.setText(move.getDescription());
        }

        if(damageView != null) {
            if(move.getDmg() < 0) {
                damageView.setText("Healing: " + (move.getDmg() * -1));
            }
            else {
                damageView.setText("Damage: " + move.getDmg());
            }
        }

        if(effectView != null) {
            if(move.isHasEffect()) {
                effectView.setText("Effect: " + move.getEffect().toString());
            }
            else {
                effectView.setText("Effect: None");
            }
        }

        if(selfCastView != null) {
            if(move.isSelfCast()) {
                selfCastView.setText("Target: Self");
            }
            else {
                selfCastView.setText("Target: Enemy");
            }
        }

        return convertView;
    }
}
