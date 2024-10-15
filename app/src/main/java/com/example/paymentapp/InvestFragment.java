package com.example.paymentapp;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InvestFragment extends Fragment {

    String userId;

    private DatabaseReference cryptosRef;
    private List<HashMap<String, String>> cryptosList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invest, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        cryptosRef = FirebaseDatabase.getInstance().getReference("Cryptos");
        loadCryptos();

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
        }

        return view;
    }

    private void loadCryptos() {
        // Clear the lists before loading to avoid duplicates
        cryptosList.clear();

        cryptosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot methodSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> crypto = (HashMap<String, String>) methodSnapshot.getValue();
                        cryptosList.add(crypto);  // Add each payment method to the list
                    }
                    populateCryptoCards();
                }
            }

            private void populateCryptoCards() {
                if (cryptosList.isEmpty()) {
                    return;
                }

                LinearLayout cardContainer = getView().findViewById(R.id.card_container); // Add this ID in your XML layout

                for (int i = 0; i < cryptosList.size(); i++) {
                    HashMap<String, String> crypto = cryptosList.get(i);
                    String cryptoName = crypto.get("name");
                    String cryptoAbbre = crypto.get("abbre");
                    String imageResourceName = crypto.get("image");

                    // Retrieve image resource ID based on the drawable resource name
                    int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                    CardView cardView = new CardView(getContext());
                    LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    cardLayoutParams.setMargins(dpToPx(getContext(), 0), dpToPx(getContext(), 25), dpToPx(getContext(), 0), dpToPx(getContext(), 15));
                    cardView.setLayoutParams(cardLayoutParams);
                    cardView.setCardElevation(0);
                    cardView.setCardBackgroundColor(getResources().getColor(android.R.color.transparent));

                    LinearLayout cardLayout = new LinearLayout(getContext());
                    cardLayout.setOrientation(LinearLayout.HORIZONTAL);
                    cardLayout.setGravity(Gravity.CENTER_VERTICAL);

                    ImageView cryptoImageView = new ImageView(getContext());
                    LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(dpToPx(getContext(), 60), dpToPx(getContext(), 60));
                    cryptoImageView.setLayoutParams(imageLayoutParams);
                    cryptoImageView.setImageResource(resID);  // Set image using the resource ID
                    cryptoImageView.setTag(resID);  // Save the resource ID in the tag for future use

                    LinearLayout textLayout = new LinearLayout(getContext());
                    textLayout.setOrientation(LinearLayout.VERTICAL);
                    textLayout.setPadding(dpToPx(getContext(), 15), 0, 0, 0);

                    TextView cryptoNameTextView = new TextView(getContext());
                    cryptoNameTextView.setText(cryptoName);
                    cryptoNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    cryptoNameTextView.setTextColor(getResources().getColor(R.color.black));
                    cryptoNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);

                    TextView cryptoSymbolTextView = new TextView(getContext());
                    cryptoSymbolTextView.setText(cryptoAbbre);
                    cryptoSymbolTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    cryptoSymbolTextView.setTextColor(getResources().getColor(R.color.black));

                    textLayout.addView(cryptoNameTextView);
                    textLayout.addView(cryptoSymbolTextView);
                    cardLayout.addView(cryptoImageView);
                    cardLayout.addView(textLayout);
                    cardView.addView(cardLayout);
                    cardContainer.addView(cardView);

                    // Set an onClick listener to handle the card click
                    /*cardView.setOnClickListener(v -> {
                        InvestMoneyFragment investMoneyFragment = new InvestMoneyFragment();

                        Bundle bundle = new Bundle();
                        bundle.putString("userId", userId);
                        bundle.putString("cryptoImage", imageResourceName);
                        bundle.putString("cryptoName", cryptoName);

                        investMoneyFragment.setArguments(bundle);

                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.frameLayout, investMoneyFragment)
                                .addToBackStack(null)
                                .commit();
                    });*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Hide Toolbar and BottomAppBar when this fragment is visible
    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.GONE);
        }

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.hide();  // Hide FAB using the hide method
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        }

        BottomAppBar bottomAppBar = getActivity().findViewById(R.id.bottomAppBar);
        if (bottomAppBar != null) {
            bottomAppBar.setVisibility(View.VISIBLE);
        }

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.show();  // Show FAB using the show method
        }
    }
}