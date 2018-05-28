package com.example.android.moviecollections;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.moviecollections.data.DatabaseDescription.Movie;

public class DetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // callback methods implemented by MainActivity
    public interface DetailFragmentListener {
        void onMovieDeleted(); // called when a movie is deleted

        // pass Uri of movie to edit to the DetailFragmentListener
        void onEditMovie(Uri contactUri);
    }

    private static final int MOVIE_LOADER = 0; // identifies the Loader

    private DetailFragmentListener listener; // MainActivity
    private Uri movieUri; // Uri of selected movie

    private TextView titleTextView; // displays movies's title
    private TextView yearTextView; // displays movies's year
    private TextView directorTextView; // displays movies's director
    private TextView genreTextView; // displays movies's genre
    private TextView ratingTextView; // displays movies's rating

    // set DetailFragmentListener when fragment attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DetailFragmentListener) context;
    }

    // remove DetailFragmentListener when fragment detached
    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // called when DetailFragmentListener's view needs to be created
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true); // this fragment has menu items to display

        // get Bundle of arguments then extract the movies's Uri
        Bundle arguments = getArguments();

        if (arguments != null)
            movieUri = arguments.getParcelable(MainActivity.MOVIE_URI);

        // inflate DetailFragment's layout
        View view =
                inflater.inflate(R.layout.fragment_detail, container, false);

        // get the EditTexts
        titleTextView =  view.findViewById(R.id.title_text_view);
        yearTextView =  view.findViewById(R.id.year_text_view);
        directorTextView =  view.findViewById(R.id.director_text_view);
        genreTextView =  view.findViewById(R.id.genre_text_view);
        ratingTextView =  view.findViewById(R.id.rating_text_view);

        // load the contact
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        return view;
    }

    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details_menu, menu);
    }

    // handle menu item selections
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                listener.onEditMovie(movieUri); // pass Uri to listener
                return true;
            case R.id.action_delete:
                deleteMovie();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // delete a movie
    private void deleteMovie() {
        // use FragmentManager to display the confirmDelete DialogFragment
        confirmDelete.show(getFragmentManager(), "confirm delete");
    }

    // DialogFragment to confirm deletion of movie
    private final DialogFragment confirmDelete =
            new DialogFragment() {
            // create an AlertDialog and return it
            @Override
        public Dialog onCreateDialog(Bundle bundle) {
            // create a new AlertDialog Builder
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.confirm_title);
            builder.setMessage(R.string.confirm_message);

            // provide an OK button that simply dismisses the dialog
            builder.setPositiveButton(R.string.button_delete,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog, int button) {

                            // use Activity's ContentResolver to invoke
                            // delete on the MovieContentProvider
                            getActivity().getContentResolver().delete(
                                    movieUri, null, null);
                            listener.onMovieDeleted(); // notify listener
                        }
                    }
            );

            builder.setNegativeButton(R.string.button_cancel, null);
            return builder.create(); // return the AlertDialog
        }
        };

    // called by LoaderManager to create a Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // create an appropriate CursorLoader based on the id argument;
        // only one Loader in this fragment, so the switch is unnecessary
        CursorLoader cursorLoader;

        switch (id) {
            case MOVIE_LOADER:
                cursorLoader = new CursorLoader(getActivity(),
                        movieUri, // Uri of movie to display
                        null, // null projection returns all columns
                        null, // null selection returns all rows
                        null, // no selection arguments
                        null); // sort order
                break;
            default:
                cursorLoader = null;
                break;
        }

        return cursorLoader;
    }

    // called by LoaderManager when loading completes
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if the movie exists in the database, display its data
        if (data != null && data.moveToFirst()) {
            // get the column index for each data item
            int titleIndex = data.getColumnIndex(Movie.COLUMN_TITLE);
            int yearIndex = data.getColumnIndex(Movie.COLUMN_YEAR);
            int directorIndex = data.getColumnIndex(Movie.COLUMN_DIRECTOR);
            int genreIndex = data.getColumnIndex(Movie.COLUMN_GENRE);
            int ratingIndex = data.getColumnIndex(Movie.COLUMN_RATING);

            // fill TextViews with the retrieved data
            titleTextView.setText(data.getString(titleIndex));
            yearTextView.setText(data.getString(yearIndex));
            directorTextView.setText(data.getString(directorIndex));
            genreTextView.setText(data.getString(genreIndex));
            ratingTextView.setText(data.getString(ratingIndex));
        }
    }

    // called by LoaderManager when the Loader is being reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}