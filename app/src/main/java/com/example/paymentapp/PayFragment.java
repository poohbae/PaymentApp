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

public class PayFragment extends Fragment {

    private EditText inputAmountEditText;
    private Button rm20Button, rm50Button, rm100Button, payNowButton;
    private String selectedAmount = "";
    private ImageView platformImageView, dropDownArrow;
    private TextView balanceAmountTextView, platformNameTextView, totalAmountTextView;

    String userId;
    double walletAmt;

    private DatabaseReference platformsRef;
    private List<HashMap<String, String>> platformsList = new ArrayList<>();
    private List<String> platformNames = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pay, container, false);

        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inputAmountEditText = view.findViewById(R.id.input_amount);
        rm20Button = view.findViewById(R.id.rm20_button);
        rm50Button = view.findViewById(R.id.rm50_button);
        rm100Button = view.findViewById(R.id.rm100_button);
        payNowButton = view.findViewById(R.id.pay_now_button);

        platformImageView = view.findViewById(R.id.platform_image);
        balanceAmountTextView = view.findViewById(R.id.balance_amount);
        platformNameTextView = view.findViewById(R.id.platform_name);
        totalAmountTextView = view.findViewById(R.id.total_amount);
        dropDownArrow = view.findViewById(R.id.drop_down_arrow);

        platformsRef = FirebaseDatabase.getInstance().getReference("Platforms");

        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            walletAmt = arguments.getDouble("walletAmt");
            balanceAmountTextView.setText(String.format("RM %.2f", walletAmt));
        }

        // Load Payment Methods from Firebase
        loadPlatforms();

        // Set up a PopupMenu to show when the drop-down arrow is clicked
        dropDownArrow.setOnClickListener(v -> showPopupMenu(v));

        rm20Button.setOnClickListener(v -> {
            selectedAmount = "20";
            inputAmountEditText.setText("20");
            updateAmount("20");
        });

        rm50Button.setOnClickListener(v -> {
            selectedAmount = "50";
            inputAmountEditText.setText("50");
            updateAmount("50");
        });

        rm100Button.setOnClickListener(v -> {
            selectedAmount = "100";
            inputAmountEditText.setText("100");
            updateAmount("100");
        });

        // Set a TextWatcher on the inputAmount EditText to update the top-up and total amounts dynamically
        inputAmountEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No need to do anything before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (!input.isEmpty()) {
                    try {
                        double inputValue = Double.parseDouble(input);

                        // Check if input value exceeds the maximum allowed amount
                        if (inputValue > 1000) {
                            inputAmountEditText.setText(input.substring(0, start));  // Truncate the input at the last valid point
                            inputAmountEditText.setSelection(inputAmountEditText.getText().length());  // Move cursor to the end
                            return;
                        }

                        if (input.contains(".")) {
                            int decimalIndex = input.indexOf(".");

                            // Check if there are more than 2 digits after the decimal point
                            if (input.length() - decimalIndex > 3) { // Allow 1 for the decimal point and 2 for decimal places
                                // If more than 2 digits are entered after the decimal, truncate the input
                                input = input.substring(0, decimalIndex + 3);
                                inputAmountEditText.setText(input);
                                inputAmountEditText.setSelection(input.length());  // Move cursor to the end
                            }
                        }

                        // Update the amount to display in other TextViews without rounding
                        updateAmount(input);

                    } catch (NumberFormatException e) {
                        // Handle exception if the input amount is not valid
                        updateAmount("0");
                    }
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
            String manualAmount = inputAmountEditText.getText().toString().trim();
            String amountToSend;
            String platformName = platformNameTextView.getText().toString();
            int platformImageResId = (int) platformImageView.getTag();

            if (!manualAmount.isEmpty()) {
                amountToSend = manualAmount;
                updateAmount(manualAmount);
            } else if (!selectedAmount.isEmpty()) {
                amountToSend = selectedAmount;
            } else {
                inputAmountEditText.setError("Please enter or select an amount");
                inputAmountEditText.requestFocus();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putString("amount", amountToSend);
            bundle.putInt("platformImageRes", platformImageResId);
            bundle.putString("platformName", platformName);

            PayDoneFragment payDoneFragment = new PayDoneFragment();
            payDoneFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, payDoneFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateAmount(String amount) {
        double parsedAmount = 0;
        try {
            parsedAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            // Handle exception if the input amount is not valid
        }

        // Truncate the amount to two decimal places without rounding
        String formattedAmount = String.format("RM %.2f", Math.floor(parsedAmount * 100) / 100);
        totalAmountTextView.setText(formattedAmount);
    }

    // Method to load Payment Methods from Firebase
    private void loadPlatforms() {
        // Clear the lists before loading to avoid duplicates
        platformsList.clear();
        platformNames.clear();

        platformsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot methodSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> platform = (HashMap<String, String>) methodSnapshot.getValue();
                        platformsList.add(platform);  // Add each payment method to the list
                        platformNames.add(platform.get("name"));  // Add bank names to the popup list
                    }

                    // Update UI with the first platform (Netflix)
                    if (!platformsList.isEmpty()) {
                        HashMap<String, String> firstMethod = platformsList.get(0);  // Get the first payment method
                        String platformName = firstMethod.get("name");
                        String imageResourceName = firstMethod.get("image");

                        int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                        platformNameTextView.setText(platformName);
                        platformImageView.setImageResource(resID);
                        platformImageView.setTag(resID);  // Save the resource ID in the tag for future use
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
        for (String bankName : platformNames) {
            popupMenu.getMenu().add(bankName);  // Add bank names to the popup menu
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            for (HashMap<String, String> method : platformsList) {
                if (method.get("name").equals(item.getTitle())) {
                    String bankName = method.get("name");
                    String imageResourceName = method.get("image");
                    int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                    platformNameTextView.setText(bankName);
                    platformImageView.setImageResource(resID);
                    platformImageView.setTag(resID);

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
