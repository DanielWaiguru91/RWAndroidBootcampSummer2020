package tech.danielwaiguru.moviesapp.ui.movie

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_movie.*
import org.koin.android.viewmodel.ext.android.viewModel
import tech.danielwaiguru.moviesapp.R
import tech.danielwaiguru.moviesapp.adapters.LazyLoadingListener
import tech.danielwaiguru.moviesapp.adapters.MovieAdapter
import tech.danielwaiguru.moviesapp.database.Movie
import tech.danielwaiguru.moviesapp.ui.details.DetailsActivity
import tech.danielwaiguru.moviesapp.viewmodels.MovieViewModel


class MovieFragment : Fragment(), MovieAdapter.MovieItemListener {
    private val movieAdapter by lazy {
        MovieAdapter(this)
    }
    private val movieViewModel by viewModel<MovieViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        setHasOptionsMenu(true)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            movieViewModel.getAllMovies().observe(it, Observer { movieList ->
                movieList?.let {movies ->
                    recyclerViewSetup()
                    movies_rv.adapter = movieAdapter
                    movieAdapter.setMovies(movies)
                    movieAdapter.movieFilterList = movies
                }
            })
        }
        movieViewModel.fetchMovies()
    }

    private fun recyclerViewSetup(){
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                movies_rv.layoutManager = GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {

                movies_rv.layoutManager = GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            }
        }
        movies_rv.addOnScrollListener(LazyLoadingListener{
            movieViewModel.fetchMovies()
        })
        movieViewModel.getAllMovies()
    }

    override fun onMovieItemClick(movie: Movie, itemView: View) {
        val intent = Intent(requireContext(), DetailsActivity::class.java)
        intent.putExtra(getString(R.string.movie_extra), movie)
        val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            Pair.create<View, String>(itemView.findViewById(R.id.posterImageView),
                getString(R.string.transition_image)),
            Pair.create<View, String>(itemView.findViewById(R.id.movie_title),
                getString(R.string.transition_movie_title))
        )
        startActivity(intent, activityOptions.toBundle())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        movieSearchView(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    private fun movieSearchView(menu: Menu?){
        val searchItem = menu?.findItem(R.id.app_bar_search)
        if (searchItem != null){
            val searchView = searchItem.actionView as? SearchView
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    movieAdapter.filter.filter(newText)
                    return true
                }
            })
        }
    }
}