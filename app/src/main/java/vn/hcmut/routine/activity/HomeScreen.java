package vn.hcmut.routine.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ShareActionProvider;

import org.apache.http.protocol.HTTP;

import vn.hcmut.routine.R;
import vn.hcmut.routine.fragment.DetailFragment;
import vn.hcmut.routine.fragment.HomeFragment;

public class HomeScreen extends Activity implements HomeFragment.Callback {

    private boolean isTwoPane;

    @Override
    public void onItemSelected(long taskId) {
        if (isTwoPane) {
            FragmentManager supportFragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();

            DetailFragment detailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putLong(DetailScreen.EXTRA_TASK_ID, taskId);
            detailFragment.setArguments(args);

            fragmentTransaction.replace(R.id.detail_container, detailFragment);
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(this, DetailScreen.class);
            intent.putExtra(DetailScreen.EXTRA_TASK_ID, taskId);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        View detailFragment = findViewById(R.id.detail_container);
        isTwoPane = detailFragment != null;

        FragmentManager fragmentManager = getFragmentManager();
        HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentById(R.id.home_container);
        homeFragment.setIsTwoPane(isTwoPane);
    }

    private ShareActionProvider shareProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem item = menu.findItem(R.id.share);
        shareProvider = (ShareActionProvider) item.getActionProvider();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            sendIntent.setType(HTTP.PLAIN_TEXT_TYPE);

            String content = getString(R.string.content_share);
            sendIntent.putExtra(Intent.EXTRA_TEXT, content);
            String title = getString(R.string.title_share);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);

            shareProvider.setShareIntent(sendIntent);
        }

        return super.onOptionsItemSelected(item);
    }
}
