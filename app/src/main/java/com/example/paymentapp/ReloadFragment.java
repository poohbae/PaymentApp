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

    private EditText inputAmountEditText;
    private Button rm100Button, rm200Button, rm300Button, rm500Button, payNowButton;
    private String selectedAmount = "";
    private ImageView bankImageView, dropDownArrow;
    private TextView balanceAmountTextView, bankNameTextView, topUpAmountTextView, totalAmountTextView;

    String userId;
    double walletAmt;

    private DatabaseReference paymentMethodsRef; // Reference to payment methods in Firebase
    private List<HashMap<String, String>> paymentMethodsList = new ArrayList<>();
    private List<String> bankNames = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reload, container, false);

        // Back button to navigate to the previous fragment
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        inputAmountEditText = view.findViewById(R.id.input_amount);
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

        // Retrieve userId and wallet amount from the arguments
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            walletAmt = arguments.getDouble("walletAmt");
            balanceAmountTextView.setText(String.format("RM %.2f", walletAmt));
        }

        // Load available payment methods from Firebase
        loadPaymentMethods();

        // Set up a PopupMenu to display payment methods when the drop-down arrow is clicked
        dropDownArrow.setOnClickListener(v -> showPopupMenu(v));

        // Set amount buttons to populate amount field when clicked
        rm100Button.setOnClickListener(v -> {
            selectedAmount = "100";
            inputAmountEditText.setText("100");
            updateAmount("100");
        });

        rm200Button.setOnClickListener(v -> {
            selectedAmount = "200";
            inputAmountEditText.setText("200");
            updateAmount("200");
        });

        rm300Button.setOnClickListener(v -> {
            selectedAmount = "300";
            inputAmountEditText.setText("300");
            updateAmount("300");
        });

        rm500Button.setOnClickListener(v -> {
            selectedAmount = "500";
            inputAmountEditText.setText("500");
            updateAmount("500");
        });

        // Listen for manual changes in the inputAmount field
        inputAmountEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();

                if (!input.isEmpty()) {
                    try {
                        // Limit the maximum input value to 10,000
                        double inputValue = Double.parseDouble(input);
                        if (inputValue > 10000) {
                            inputAmountEditText.setText(input.substring(0, start));  // Truncate to last valid point
                            inputAmountEditText.setSelection(inputAmountEditText.getText().length());  // Move cursor to the end
                            return;
                        }

                        // Limit decimal places to two digits
                        if (input.contains(".")) {
                            int decimalIndex = input.indexOf(".");
                            if (input.length() - decimalIndex > 3) {
                                input = input.substring(0, decimalIndex + 3);
                                inputAmountEditText.setText(input);
                                inputAmountEditText.setSelection(input.length());
                            }
                        }

                        // Update displayed amounts
                        updateAmount(input);

                    } catch (NumberFormatException e) {
                        updateAmount("0"); // Reset on invalid input
                    }
                } else {
                    updateAmount("0"); // Reset if input is empty
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // No action required after text changes
            }
        });

        // Set up the 'Pay Now' button
        payNowButton.setOnClickListener(v -> {
            String manualAmount = inputAmountEditText.getText().toString().trim();
            String amountToSend;
            String bankName = bankNameTextView.getText().toString();
            int bankImageResId = (int) bankImageView.getTag();

            // Determine the amount to send
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

            // Pass data to ReloadMoneyFragment
            Bundle bundle = new Bundle();
            bundle.putString("userId", userId);
            bundle.putString("amount", amountToSend);
            bundle.putInt("bankImageRes", bankImageResId);
            bundle.putString("bankName", bankName);

            ReloadMoneyFragment addMoneyFragment = new ReloadMoneyFragment();
            addMoneyFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, addMoneyFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    // Updates the amount fields based on the specified input amount
    private void updateAmount(String amount) {
        double parsedAmount = 0;
        try {
            parsedAmount = Double.parseDouble(amount);
        } catch (NumberFormatException e) {
            // Handle invalid input by setting parsedAmount to 0
            parsedAmount = 0;
        }

        // Format and display amount without rounding
        String formattedAmount = String.format("RM %.2f", Math.floor(parsedAmount * 100) / 100);
        topUpAmountTextView.setText(formattedAmount);
        totalAmountTextView.setText(formattedAmount);
    }

    // Loads available payment methods from Firebase and sets the initial display
    private void loadPaymentMethods() {
        // Clear previous data to avoid duplicates
        paymentMethodsList.clear();
        bankNames.clear();

        paymentMethodsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot methodSnapshot : dataSnapshot.getChildren()) {
                        HashMap<String, String> paymentMethod = (HashMap<String, String>) methodSnapshot.getValue();
                        paymentMethodsList.add(paymentMethod);  // Add each payment method
                        bankNames.add(paymentMethod.get("name"));  // Add bank name to list
                    }

                    // Display the first payment method
                    if (!paymentMethodsList.isEmpty()) {
                        HashMap<String, String> firstMethod = paymentMethodsList.get(0);  // Get the first payment method
                        String bankName = firstMethod.get("name");
                        String imageResourceName = firstMethod.get("image");

                        int resID = getResources().getIdentifier(imageResourceName, "drawable", getActivity().getPackageName());

                        bankNameTextView.setText(bankName);
                        bankImageView.setImageResource(resID);
                        bankImageView.setTag(resID); // Store the resource ID for future use
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    // Displays a PopupMenu anchored to the drop-down arrow to show available payment methods
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        for (String bankName : bankNames) {
            popupMenu.getMenu().add(bankName);  // Add bank names to popup menu
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
        popupMenu.show(); // Display the popup menu
    }

    // Hide the ActionBar, BottomAppBar, and FloatingActionButton in this fragment
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

    // Show the ActionBar, BottomAppBar, and FloatingActionButton when leaving this fragment
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
