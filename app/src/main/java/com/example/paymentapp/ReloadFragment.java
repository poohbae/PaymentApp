package com.example.paymentapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

public class ReloadFragment extends Fragment {

    private EditText inputAmount;
    private Button rm100Button, rm200Button, rm300Button, rm500Button, payNowButton;
    private String selectedAmount = "";
    private ImageView bankImageView, dropDownArrow;
    private TextView balanceAmountTextView, bankNameTextView, topUpAmountTextView, totalAmountTextView;

    private DatabaseReference paymentMethodsRef;
    private List<HashMap<String, String>> paymentMethodsList = new ArrayList<>();
    private List<String> bankNames = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reload, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inputAmount = view.findViewById(R.id.input_amount);
        rm100Button = view.findViewById(R.id.rm100_button);
        rm200Button = view.findViewById(R.id.rm200_button);
        rm300Button = view.findViewById(R.id.rm300_button);
        rm500Button = view.findViewById(R.id.rm500_button);
        payNowButton = view.findViewById(R.id.pay_now_button);

        bankImageView = view.findViewById(R.id.bank_image);
        balanceAmountTextView = view.findViewById(R.id.balance_amount);
        bankNameTextView = view.findViewById(R.id.bank_name);
        topUpAmountTextView = view.findViewById(R.id.top_up_amount);
        totalAmountTextView = view.findViewById(R.id.total_amount);
        dropDownArrow = view.findViewById(R.id.drop_down_arrow);

        paymentMethodsRef = FirebaseDatabase.getInstance().getReference("PaymentMethods");

        if (getArguments() != null) {
            double walletAmt = getArguments().getDouble("walletAmt", 0.0);
            balanceAmountTextView.setText(String.format("RM %.2f", walletAmt));
        }

        // Load Payment Methods from Firebase
        loadPaymentMethods();

        // Set up a PopupMenu to show when the drop-down arrow is clicked
        dropDownArrow.setOnClickListener(v -> showPopupMenu(v));

        rm100Button.setOnClickListener(v -> {
            selectedAmount = "100";
            inputAmount.setText("100");
            updateAmount("100");
        });

        rm200Button.setOnClickListener(v -> {
            selectedAmount = "200";
            inputAmount.setText("200");
            updateAmount("200");
        });

        rm300Button.setOnClickListener(v -> {
            selectedAmount = "300";
            inputAmount.setText("300");
            updateAmount("300");
        });

        rm500Button.setOnClickListener(v -> {
            selectedAmount = "500";
            inputAmount.setText("500");
            updateAmount("500");
        });

        // Set a TextWatcher on the inputAmount EditText to update the top-up and total amounts dynamically
        inputAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to do anything before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the top-up and total amounts as the user types
                if (!s.toString().isEmpty()) {
                    updateAmount(s.toString());
                } else {
                    updateAmount("0"); // Reset the amount to 0 if the EditText is empty
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // No need to do anything after the text is changed
            }
        });

        payNowButton.setOnClickListener(v -> {
            String manualAmount = inputAmount.getText().toString().trim();
            String amountToSend;
            String bankName = bankNameTextView.getText().toString();
            int bankImageResId = (int) bankImageView.getTag();

            if (!manualAmount.isEmpty()) {
                amountToSend = manualAmount;
                updateAmount(manualAmount);
            } else if (!selectedAmount.isEmpty()) {
                amountToSend = selectedAmount;
            } else {
                inputAmount.setError("Please enter or select an amount");
                inputAmount.requestFocus();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("amount", amountToSend);
            bundle.putInt("bank_image_res", bankImageResId);
            bundle.putString("bank_name", bankName);

            AddMoneyFragment addMoneyFragment = new AddMoneyFragment();
            addMoneyFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, addMoneyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateAmount(String amount) {
        topUpAmountTextView.setText("RM " + amount);
        totalAmountTextView.setText("RM " + amount);
    }

    // Method to load Payment Methods from Firebase
    private void loadPaymentMethods() {
        // Clear the lists before loading to avoid duplicates
        paymentMethodsList.clear();
        bankNames.clear();

        paymentMethodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot methodSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> paymentMethod = (HashMap<String, String>) methodSnapshot.getValue();
                        paymentMethodsList.add(paymentMethod);  // Add each payment method to the list
                        bankNames.add(paymentMethod.get("name"));  // Add bank names to the popup list
                    }

                    // Update UI with the first payment method (Maybank)
                    if (!paymentMethodsList.isEmpty()) {
                        HashMap<String, String> firstMethod = paymentMethodsList.get(0);  // Get the first payment method
                        String bankName = firstMethod.get("name");
                        String imageResourceName = firstMethod.get("image");

                        int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                        bankNameTextView.setText(bankName);
                        bankImageView.setImageResource(resID);
                        bankImageView.setTag(resID);  // Save the resource ID in the tag for future use
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Show PopupMenu anchored to the arrow
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        for (String bankName : bankNames) {
            popupMenu.getMenu().add(bankName);  // Add bank names to the popup menu
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            for (HashMap<String, String> method : paymentMethodsList) {
                if (method.get("name").equals(item.getTitle())) {
                    String bankName = method.get("name");
                    String imageResourceName = method.get("image");
                    int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                    bankNameTextView.setText(bankName);
                    bankImageView.setImageResource(resID);
                    bankImageView.setTag(resID);

                    break;
                }
            }
            return true;
        });

        popupMenu.show();  // Show the popup menu below the drop-down arrow
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
            fab.hide();
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
            fab.show();
        }
    }
}
