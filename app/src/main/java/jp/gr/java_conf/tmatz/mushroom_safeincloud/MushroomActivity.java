package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MushroomActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListItemSelectedListener, LoginFragment.DialogListener {
    public static final String ACTION_INTERCEPT = "com.adamrocker.android.simeji.ACTION_INTERCEPT";
    public static final String EXTRA_REPLACE_KEY = "replace_key";

    public static final String STATE_DATABASE_URI = "database_uri";
    public static final String STATE_ENTRY_ID = "entry_id";
    public static final String STATE_GROUP_ID = "group_id";
    public static final String STATE_FRAGMENT_ARGUMENTS = "state_fragment_arguments";
    public static final String TAG = MushroomActivity.class.getSimpleName();

    private static final String ARG_DIGEST = "digest";
    private static final String ARG_POSITION = "position";
    private static final String ARG_TAG = "tag";
    private static final int LOADER_GROUP_LIST = 0;
    private static final int REQUEST_OPEN_DOCUMENT = 100;

    private Uri mDatabaseUri;
    private String mGroupId = "";
    private String mEntryId = "";
    private String mCallingPackage;
    private ArrayList<Bundle> mFragmentArguments = new ArrayList<>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private ArrayAdapter<GroupInfo> mGroupAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate", (savedInstanceState != null) ? "with state" : null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mushroom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


//        if (!PocketDatabase.isReadable()) {
//            Log.e(TAG, "pocket database is unreadable");
//            Toast.makeText(this, R.string.cant_open_pocket, Toast.LENGTH_SHORT).show();
//            setResult(RESULT_CANCELED);
//            finish();
//            return;
//        }

        mCallingPackage = getCallingPackage();

        if (mCallingPackage == null) {
            Intent intent = getIntent();
            if (ACTION_INTERCEPT.equals(intent.getAction())) {
                Log.w(TAG, "calling package unknown");
                {
                    setResult(RESULT_CANCELED);
                    finish();
                    return;
                }
            }
        }

        mGroupAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item);
        mGroupAdapter.setNotifyOnChange(true);

//        getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
//
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
//
//        actionBar.setListNavigationCallbacks(
//                mGroupAdapter,
//                new OnNavigationListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(int itemPosition, long itemId)
//                    {
//                        GroupInfo groupInfo = mGroupAdapter.getItem(itemPosition);
//                        if (groupInfo != null) {
//                            if (!mGroupId.equals(groupInfo.getId())) {
//                                mGroupId = groupInfo.getId();
//                                setPage(0, EntriesFragment.TAG, EntriesFragment.newArgument(mCallingPackage, mGroupId, null), false);
//                            }
//                        }
//                        return true;
//                    }
//                });
//
        restoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mushroom_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (item.getGroupId() == R.id.nav_group) {
            GroupInfo groupInfo = mGroupAdapter.getItem(id);
            if (groupInfo != null) {
                if (!mGroupId.equals(groupInfo.getId())) {
                    mGroupId = groupInfo.getId();
                    setPage(0, EntriesFragment.TAG, EntriesFragment.newArgument(mCallingPackage, mGroupId, null), false);
                }
            }
        } else {
            /*
            if (id == R.id.nav_camera) {
            } else if (id == R.id.nav_gallery) {

            } else if (id == R.id.nav_slideshow) {

            } else if (id == R.id.nav_manage) {

            } else */
            if (id == R.id.nav_share) {
            } else if (id == R.id.nav_send) {
                askDatabaseUri();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void saveInstanceStateIntoPref() {
        int currentItem = mPager.getCurrentItem();
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        pref.edit()
                .putString(STATE_DATABASE_URI, mDatabaseUri != null ? mDatabaseUri.toString() : "")
                .putString(STATE_GROUP_ID, mGroupId)
                .putString(STATE_ENTRY_ID, (currentItem >= 1) ? mEntryId : "")
                .commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.i(TAG, "onSaveInstanceState", "group", mGroupId, "entry", mEntryId);
        super.onSaveInstanceState(outState);

        outState.putString(STATE_DATABASE_URI, mDatabaseUri != null ? mDatabaseUri.toString() : "");
        outState.putString(STATE_GROUP_ID, mGroupId);
        outState.putString(STATE_ENTRY_ID, mEntryId);
        outState.putParcelableArrayList(STATE_FRAGMENT_ARGUMENTS, mFragmentArguments);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            setDatabase(savedInstanceState.getString(STATE_DATABASE_URI, ""));
            mGroupId = savedInstanceState.getString(STATE_GROUP_ID, "");
            mEntryId = savedInstanceState.getString(STATE_ENTRY_ID, "");

            mFragmentArguments = savedInstanceState.getParcelableArrayList(STATE_FRAGMENT_ARGUMENTS);
            if (mPagerAdapter != null) {
                mPagerAdapter.notifyDataSetChanged();
            }
        } else {
            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            setDatabase(pref.getString(STATE_DATABASE_URI, ""));
            mGroupId = pref.getString(STATE_GROUP_ID, "");
            mEntryId = pref.getString(STATE_ENTRY_ID, "");

            setPage(0, EntriesFragment.TAG, EntriesFragment.newArgument(mCallingPackage, mGroupId, null), false);
            if (!mEntryId.isEmpty()) {
                setPage(1, FieldsFragment.TAG, FieldsFragment.newArgument(mCallingPackage, mEntryId, null), false);
            }
        }

        Logger.i(TAG, "restoreInstanceState", "group", mGroupId, "entry", mEntryId);
    }

    private void setDatabase(String databaseUri) {
        if (databaseUri == null || databaseUri.isEmpty()) {
            mDatabaseUri = null;
        } else {
            mDatabaseUri = Uri.parse(databaseUri);
        }
    }

    private void askDatabaseUri() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_OPEN_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.i(TAG, "onActivityResult", "requestCode", requestCode, "resultCode", resultCode);
        switch (requestCode) {
            case REQUEST_OPEN_DOCUMENT:
                if (resultCode == RESULT_OK && data != null) {
                    openDatabase(data.getData());
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openDatabase(Uri uri) {
        if (mDatabaseUri != null) {
            if (mDatabaseUri.equals(uri)) {
                return;
            }

            getContentResolver().releasePersistableUriPermission(mDatabaseUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        File filesDir = getFilesDir();
        File databaseFile = new File(filesDir, "database.dat");

        try (InputStream is = getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(databaseFile)) {
            IOUtils.copy(is, os);
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mDatabaseUri = uri;

            // showLoginDialog();
        } catch (Exception ex) {
            Log.d(TAG, "safe database failed", ex);
            mDatabaseUri = null;
        }
    }

    private void onGetPocketLock() {
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(Math.max(0, mPagerAdapter.getCount() - 1));
        getSupportLoaderManager().initLoader(LOADER_GROUP_LIST, null, mGroupInfoLoaderCallbacks);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");

        super.onResume();
        LockTimer.resetTimer();

        if (mDatabaseUri == null) {
            Logger.i(TAG, "    invoke askDatabaseUri");
            askDatabaseUri();
        } else if (PocketLock.getPocketLock(mCallingPackage) != null) {
            Logger.i(TAG, "    invoke onGetPocketLock");
            onGetPocketLock();
        } else {
            Logger.i(TAG, "    invoke showLoginDialog");
            showLoginDialog();
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");

        super.onPause();
        saveInstanceStateIntoPref();
        LockTimer.startTimer();
    }

    private void showLoginDialog() {
        Log.i(TAG, "showLoginDialog");

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("login") == null) {
            LoginFragment login = LoginFragment.newInstance(mCallingPackage);
            login.show(fm, "login");
        }
    }

    @Override
    public void onListItemSelected(Fragment f, Object data) {
        String tag = f.getArguments().getString(ARG_TAG);

        if (EntriesFragment.TAG.equals(tag)) {
            EntryInfo item = (EntryInfo) data;
            Logger.i(TAG, "onListItemSelected", tag, item.getId());
            mEntryId = item.getId();
            setPage(1, FieldsFragment.TAG, FieldsFragment.newArgument(mCallingPackage, mEntryId, null), false);
        } else if (FieldsFragment.TAG.equals(tag)) {
            FieldInfo item = (FieldInfo) data;
            Logger.i(TAG, "onListItemSelected", tag, item.getId());
            replace(item.getValue());
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mPager.getCurrentItem() > 0) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int code, KeyEvent event) {
        // long press return button. finish app.
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }

        return super.onKeyLongPress(code, event);
    }

    // send back result string to calling package.
    private void replace(String result) {
        Log.i(TAG, "replace");

        Intent data = new Intent();
        data.putExtra(EXTRA_REPLACE_KEY, result);
        setResult(RESULT_OK, data);
        finish();
    }

    private void setPage(int position, String tag, Bundle arg, boolean pop) {
        if (arg == null) {
            throw new IllegalArgumentException("arg must not be null.");
        }

        if (mFragmentArguments.size() < position) {
            throw new IllegalStateException();
        }

        arg.putString(ARG_TAG, tag);
        arg.putInt(ARG_POSITION, position);

        Utilities.setDigest(arg, ARG_DIGEST);

        if (position == mFragmentArguments.size()) {
            mFragmentArguments.add(arg);
        } else {
            if (pop) {
                while (position + 1 < mFragmentArguments.size()) {
                    mFragmentArguments.remove(mFragmentArguments.size() - 1);
                }
            }
            mFragmentArguments.set(position, arg);
        }

        if (mPagerAdapter != null) {
            mPagerAdapter.notifyDataSetChanged();
        }

        if (mPager != null) {
            mPager.setCurrentItem(position);
        }
    }

    @Override
    public void onOK(LoginFragment fragment) {
        Logger.i(TAG, "onOK");

        if (PocketLock.getPocketLock(mCallingPackage) == null) {
            throw new IllegalStateException("cant unlock database.");
        } else {
            mPager.setAdapter(mPagerAdapter);
            getSupportLoaderManager().initLoader(LOADER_GROUP_LIST, null, mGroupInfoLoaderCallbacks);
        }
    }

    @Override
    public void onCancel(LoginFragment fragment) {
        Logger.i(TAG, "onCancel");
        finish();
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFragmentArguments.size();
        }

        @Override
        public Fragment getItem(int position) {
            if (position >= mFragmentArguments.size()) {
                return null;
            } else {
                Logger.i(TAG, "PagerAdapter.getItem");

                Fragment f;
                Bundle arg = mFragmentArguments.get(position);
                String tag = arg.getString(ARG_TAG);

                if (EntriesFragment.TAG.equals(tag)) {
                    f = new EntriesFragment();
                } else if (FieldsFragment.TAG.equals(tag)) {
                    f = new FieldsFragment();
                } else {
                    return null;
                }

                f.setArguments(arg);
                return f;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            Fragment f = (Fragment) object;
            Bundle arg1 = f.getArguments();
            int position = arg1.getInt(ARG_POSITION);
            Bundle arg2 = mFragmentArguments.get(position);
            String digest = arg1.getString(ARG_DIGEST);
            if (digest != null && digest.equals(arg2.getString(ARG_DIGEST))) {
                return POSITION_UNCHANGED;
            } else {
                return POSITION_NONE;
            }
        }
    }

    private final LoaderManager.LoaderCallbacks<List<GroupInfo>> mGroupInfoLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<GroupInfo>>() {
        @Override
        public Loader<List<GroupInfo>> onCreateLoader(int id, Bundle arg) {
            Logger.i(TAG, "LoaderCallbacks.onCreateLoader");
            switch (id) {
                case 0:
                    return new GroupInfoLoader(MushroomActivity.this, mCallingPackage);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<List<GroupInfo>> loader, List<GroupInfo> data) {
            Logger.i(TAG, "LoaderCallbacks.onLoadFinished");
            mGroupAdapter.clear();
            if (data != null) {
                for (GroupInfo item : data) {
                    mGroupAdapter.add(item);
                }
            }

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            Menu menu = navigationView.getMenu();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    GroupInfo item = data.get(i);
                    menu.add(R.id.nav_group, i, 0, item.getTitle());
                }
            }

//            int count = mGroupAdapter.getCount();
//            for (int i = 0; i < count; ++i) {
//                if (mGroupAdapter.getItem(i).getId() == mGroupId) {
//                    getSupportActionBar().setSelectedNavigationItem(i);
//                }
//            }
        }

        @Override
        public void onLoaderReset(Loader<List<GroupInfo>> loader) {
            Logger.i(TAG, "onLoaderReset");
            mGroupAdapter.clear();
        }
    };

    private static class GroupInfoLoader extends CachedAsyncTaskLoader<List<GroupInfo>> {
        private String mPackageName;

        GroupInfoLoader(Context context, String packageName) {
            super(context);
            mPackageName = packageName;
        }

        @Override
        public List<GroupInfo> loadInBackground() {
            PocketLock pocketLock = PocketLock.getPocketLock(mPackageName);
            if (pocketLock == null) {
                return null;
            }

            return PocketDatabase.readGroups(getContext(), pocketLock);
        }
    }
}
