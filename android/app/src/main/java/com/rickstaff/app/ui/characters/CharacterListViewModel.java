package com.rickstaff.app.ui.characters;

import static com.rickstaff.app.util.Constants.MAX_PAGES;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.rickstaff.app.data.model.Character;
import com.rickstaff.app.data.model.CharacterResponse;
import com.rickstaff.app.data.remote.CharacterRepository;
import com.rickstaff.app.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

public class CharacterListViewModel extends ViewModel {

    private final CharacterRepository repository = new CharacterRepository();
    private final MutableLiveData<List<Character>> characters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(false);

    private int currentPage = 1;
    private boolean isLastPage = false;
    private boolean isCurrentlyLoading = false;
    private String filterStatus = "";
    private String filterGender = "";
    private String filterSpecies = "";

    public LiveData<List<Character>> getCharacters() { return characters; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getIsEmpty() { return isEmpty; }

    public void loadFirstPage(Context context) {
        if(checkNetworkConnection(context)){
            return;
        }

        currentPage = 1;
        isLastPage = false;
        characters.setValue(new ArrayList<>());
        loadPage(1);
    }

    public void loadNextPage(Context context) {
        if(checkNetworkConnection(context)){
            return;
        }

        if (isCurrentlyLoading || isLastPage || currentPage >= MAX_PAGES) return;
        loadPage(currentPage + 1);
    }

    private void loadPage(int page) {
        if (isCurrentlyLoading) return;
        isCurrentlyLoading = true;
        isLoading.postValue(true);

        repository.getCharacters(page, filterStatus, filterGender, filterSpecies,
                new CharacterRepository.CharacterCallback() {
                    @Override
                    public void onSuccess(CharacterResponse response) {
                        isCurrentlyLoading = false;
                        isLoading.postValue(false);
                        currentPage = page;
                        isLastPage = response.getInfo().getNext() == null || currentPage >= MAX_PAGES;

                        List<Character> current = characters.getValue();
                        if (current == null) current = new ArrayList<>();
                        current.addAll(response.getResults());
                        characters.postValue(new ArrayList<>(current));
                        isEmpty.postValue(current.isEmpty());
                    }

                    @Override
                    public void onError(String message) {
                        isCurrentlyLoading = false;
                        isLoading.postValue(false);
                        List<Character> current = characters.getValue();
                        if (current == null || current.isEmpty()) {
                            error.postValue(message);
                            isEmpty.postValue(true);
                        }
                    }
                }
        );
    }

    public void applyFilters(
        Context context,
        String status,
        String gender,
        String species
    ) {
        this.filterStatus = status;
        this.filterGender = gender;
        this.filterSpecies = species;
        loadFirstPage(context);
    }

    private boolean checkNetworkConnection(Context context) {
        if (!NetworkUtils.isConnected(context)) {
            error.postValue("Sem conexão com a internet");
            return true;
        }
        return false;
    }
}