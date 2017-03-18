package jp.gr.java_conf.tmatz.mushroom_safeincloud;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class FieldsFragment extends CustomListFragment implements LoaderCallbacks<List<FieldInfo>> {
    public static final String TAG = FieldsFragment.class.getSimpleName();
    public static final String ARG_PACKAGE_NAME = "package_name";
    public static final String ARG_ENTRY_ID = "entry_id";

    private ArrayAdapter<FieldInfo> mAdapter;

    public static Bundle newArgument(String packageName, String entryId, Bundle extra) {
        if (extra == null) {
            extra = new Bundle();
        }

        extra.putString(ARG_PACKAGE_NAME, packageName);
        extra.putString(ARG_ENTRY_ID, entryId);

        return extra;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Logger.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ArrayAdapter<FieldInfo>(getActivity(), 0) {
            private final TransformationMethod mPasswordTransformationMethod = new PasswordTransformationMethod();
            private final TransformationMethod mNormalTransformationMethod = new HideReturnsTransformationMethod();

            @Override
            public @NonNull View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
                if (view == null) {
                    LayoutInflater inflater = (LayoutInflater) getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    view = inflater.inflate(R.layout.field_list_item, parent);
                }

                FieldInfo item = getItem(position);
                if (item != null) {
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);

                    text1.setText(item.getTitle());
                    text2.setText(item.getValue());
                    if (item.isHidden()) {
                        text2.setTransformationMethod(mPasswordTransformationMethod);
                    } else {
                        text2.setTransformationMethod(mNormalTransformationMethod);
                    }
                }

                return view;
            }
        };

        mAdapter.setNotifyOnChange(true);

        setEmptyText(getResources().getString(R.string.no_fields));

        LoaderManager manager = getLoaderManager();
        manager.initLoader(0, getArguments(), this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Logger.i(TAG, "onCreateOptionsMenu");
        inflater.inflate(R.menu.menu_entries_fragment, menu);

        setupSearchView(menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Logger.i(TAG, "onListItemClick");
        super.onListItemClick(l, v, position, id);

        if (getActivity() instanceof OnListItemSelectedListener) {
            FieldInfo data = mAdapter.getItem(position);
            ((OnListItemSelectedListener) getActivity()).onListItemSelected(this, data);
        }
    }

    private static class ItemsLoader extends CachedAsyncTaskLoader<List<FieldInfo>> {
        private Bundle mArgs;

        ItemsLoader(Context context, Bundle args) {
            super(context);
            mArgs = args;
        }

        @Override
        public List<FieldInfo> loadInBackground() {
            String packageName = mArgs.getString(ARG_PACKAGE_NAME);
            PocketLock pocketLock = PocketLock.getPocketLock(packageName);

            if (pocketLock == null) {
                return null;
            }

            String entryId = mArgs.getString(ARG_ENTRY_ID);

            return PocketDatabase.readFields(getContext(), pocketLock, entryId);
        }
    }

    @Override
    public Loader<List<FieldInfo>> onCreateLoader(int id, Bundle args) {
        Logger.i(TAG, "onCreateLoader");
        Loader<List<FieldInfo>> loader = new ItemsLoader(getActivity(), args);
        loader.forceLoad();
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<FieldInfo>> loader, List<FieldInfo> items) {
        Logger.i(TAG, "onLoadFinished");
        if (items != null) {
            for (FieldInfo i : items) {
                mAdapter.add(i);
            }
        }
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<List<FieldInfo>> loader) {
        Logger.i(TAG, "onLoaderReset");
        setListAdapter(mAdapter);
    }
}
