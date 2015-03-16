package vn.hcmut.routine.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.List;

import vn.hcmut.routine.R;
import vn.hcmut.routine.activity.TaskScreen;

public class TodoAdapter extends BaseAdapter {

    private TaskScreen activity;
    private List<String> todo;

    public TodoAdapter(List<String> todo, TaskScreen activity) {
        this.todo = todo;
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return todo.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        int count = todo.size();
        if (position < count) {
            return todo.get(position);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            Context context = parent.getContext();
            convertView = View.inflate(context, R.layout.todo_item, null);

            ViewHolder viewHolder = new ViewHolder(convertView, position);
            convertView.setTag(viewHolder);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.position = position;

        final String data = (String) getItem(position);
        if (data != null) {
            viewHolder.txtTodoItem.setText(data);
            viewHolder.txtTodoItem.setEnabled(false);
            viewHolder.imageAddTodoItem.setOnClickListener(null);
            viewHolder.imageAddTodoItem.setImageResource(R.drawable.ic_action_remove);
        } else {
            viewHolder.txtTodoItem.setEnabled(true);
            viewHolder.txtTodoItem.setText("");
            viewHolder.txtTodoItem.setOnEditorActionListener(activity);
            viewHolder.imageAddTodoItem.setImageResource(R.drawable.ic_action_new);
        }

        viewHolder.imageAddTodoItem.setTag(viewHolder);
        viewHolder.imageAddTodoItem.setOnClickListener(activity);

        return convertView;
    }

    public static class ViewHolder {

        public EditText txtTodoItem;
        public ImageView imageAddTodoItem;
        public int position;

        public ViewHolder(View view, int position) {
            txtTodoItem = (EditText) view.findViewById(R.id.txtTodoItem);
            imageAddTodoItem = (ImageView) view.findViewById(R.id.imgAddTodoItem);
            this.position = position;
        }

    }
}
