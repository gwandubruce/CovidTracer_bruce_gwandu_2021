package app.bandemic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import app.bandemic.R;
import app.bandemic.ui.InfectedUUIDsAdapter;
import app.bandemic.viewmodel.InfectionCheckViewModel;
import app.bandemic.viewmodel.MainActivityViewModel;
// handles infected ids from the the the db  gets the using getPossiblyInfectedEncounters(), if there there are matches it shows danger if not then nothing
public class InfectionCheckFragment extends Fragment {

    private InfectionCheckViewModel mViewModel;
    private MainActivityViewModel mainActivityViewModel;

    private RecyclerView recyclerView;
    private InfectedUUIDsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LinearLayout noInfectionInformation;
    private CardView cardView;

    public static InfectionCheckFragment newInstance() {
        return new InfectionCheckFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.infection_check_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView is flexible view providing a limited window of or into a large data set
        // It has a subclass called RecyclerView.Adapter is responsible for providing views that represent items in a data set

        recyclerView = view.findViewById(R.id.infection_check_list_recycler_view);
        noInfectionInformation = view.findViewById(R.id.layout_not_infected1);  // MULTIPLE IMPLEMENTATIONS
        recyclerView.setHasFixedSize(true);

        //layout manager is responsible for measuring and positioning item views within a recyclerView

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new InfectedUUIDsAdapter();
        recyclerView.setAdapter(mAdapter);
        cardView = view.findViewById(R.id.infectionCheckFragment);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // ViewModel is a class responsible for preparing and managing data for an Activity or Fragment.
        //It handles the communication of a fragment or activity with the the rest of the app.//Application is a base class for maintaining global application state
        // So AndroidViewModel is an application context aware ViewModel

        mViewModel = new ViewModelProvider(this).get(InfectionCheckViewModel.class);
        mainActivityViewModel = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        mainActivityViewModel.eventRefresh().observe(getViewLifecycleOwner(), refreshing -> {
            if(refreshing) {
                mViewModel.refreshInfectedUUIDs();
            }
        });

        mViewModel.getPossiblyInfectedEncounters().observe(getViewLifecycleOwner(), infectedUUIDS -> {
            mainActivityViewModel.finishRefresh();
            if(infectedUUIDS.size() != 0) {
                mAdapter.setInfectedUUIDs(infectedUUIDS);
                noInfectionInformation.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);


                cardView.setCardBackgroundColor(getResources().getColor(R.color.colorDanger));
            }
            else {
                noInfectionInformation.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                cardView.setCardBackgroundColor(getResources().getColor(R.color.colorNoDanger));
            }
        });

        mViewModel.refreshInfectedUUIDs();
    }
}
