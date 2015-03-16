package vn.hcmut.routine.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.TaskScreen;
import vn.hcmut.routine.model.RepeatItem;
import vn.hcmut.routine.model.TimeItem;

public class RepeatAdapter extends BaseAdapter {

    private List<RepeatItem> repeat;
    private TaskScreen activity;

    public RepeatAdapter(List<RepeatItem> repeat, TaskScreen activity) {
        this.repeat = repeat;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return repeat.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        int count = repeat.size();
        if (position < count) {
            return repeat.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.repeat_item, null);

            ViewHolder viewHolder = new ViewHolder(convertView, position);
            convertView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.position = position;

        Object item = getItem(position);

        if (item != null) {
            RepeatItem repeatItem = (RepeatItem) item;
            String start = repeatItem.start.toString();
            viewHolder.txtStart.setText(start);
            viewHolder.txtStart.setOnClickListener(null);

            TimeItem finish = repeatItem.finish;
            viewHolder.txtFinish.setText(finish != null ? finish.toString() : "-");
            viewHolder.txtFinish.setOnClickListener(null);

            TimeItem interval = repeatItem.interval;
            viewHolder.txtInterval.setText(interval != null ? interval.toString() : "-");
            viewHolder.txtInterval.setOnClickListener(null);

            viewHolder.imgAddRepeatItem.setImageResource(R.drawable.ic_action_remove);
        } else {
            viewHolder.txtStart.setText("");
            viewHolder.txtStart.setOnClickListener(activity);
            viewHolder.txtStart.setTag(viewHolder);

            viewHolder.txtFinish.setText("");
            viewHolder.txtFinish.setOnClickListener(activity);
            viewHolder.txtFinish.setTag(viewHolder);

            viewHolder.txtInterval.setText("");
            viewHolder.txtInterval.setOnClickListener(activity);
            viewHolder.txtInterval.setTag(viewHolder);

            viewHolder.imgAddRepeatItem.setImageResource(R.drawable.ic_action_new);
        }

        viewHolder.imgAddRepeatItem.setTag(viewHolder);
        viewHolder.imgAddRepeatItem.setOnClickListener(activity);

        return convertView;
    }

    public static class ViewHolder {

        public TextView txtStart, txtFinish, txtInterval;
        public ImageView imgAddRepeatItem;
        public int position;

        public ViewHolder(View view, int position) {
            txtStart = (TextView) view.findViewById(R.id.txtStart);
            txtFinish = (TextView) view.findViewById(R.id.txtFinish);
            txtInterval = (TextView) view.findViewById(R.id.txtInterval);
            imgAddRepeatItem = (ImageView) view.findViewById(R.id.imgAddRepeatItem);
            this.position = position;
        }

    }
}
