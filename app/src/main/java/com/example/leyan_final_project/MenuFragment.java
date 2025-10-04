package com.example.leyan_final_project;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // ✅ جديد
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuFragment extends Fragment {

    private static final String CAT_ALL     = "ALL";
    private static final String CAT_FAST    = "FAST";
    private static final String CAT_HEAVY   = "HEAVY";
    private static final String CAT_STARTER = "STARTER";
    private static final String CAT_DRINKS  = "DRINKS";
    private static final String CAT_DESSERT = "DESSERT";

    private final List<Dish> full  = new ArrayList<>();
    private final List<Dish> shown = new ArrayList<>();
    private DishAdapter adapter;

    private String selectedKey = CAT_ALL;
    private String query = "";
    private LinkedHashMap<String, String> catLabels;

    private boolean isAdmin;

    private DishViewModel dishViewModel;

    public MenuFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        RecyclerView rv = root.findViewById(R.id.rvDishes);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rv.setHasFixedSize(true);

        initCategoryLabels();

        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", 0);
        boolean prefIsAdmin = prefs.getBoolean("is_admin", false);
        String role = prefs.getString("user_role", prefIsAdmin ? "admin" : "user");
        isAdmin = prefIsAdmin || "admin".equals(role);

        adapter = new DishAdapter(shown, isAdmin, new DishAdapter.OnDishAction() {
            @Override public void onAddToCart(Dish dish) {
                if (isAdmin) return;
                showQtyDialogAndAdd(dish);
            }

            @Override public void onEdit(Dish dish) {
                if (!isAdmin) return;
                AddEditDishDialog.newEdit(dish.id, dish.name, dish.price, dish.imageUrl, dish.category)
                        .show(getParentFragmentManager(), "edit");
            }

            @Override public void onDelete(Dish dish) {
                if (!isAdmin) return;
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.dlg_confirm_title)
                        .setMessage("Delete \"" + (dish.name == null ? "" : dish.name) + "\"?")
                        .setPositiveButton(R.string.dlg_yes, (d, w) ->
                                dishViewModel.deleteById(dish.id)
                        )
                        .setNegativeButton(R.string.dlg_no, null)
                        .show();
            }

            @Override public void onClick(Dish dish) {  }
        });
        rv.setAdapter(adapter);

        FloatingActionButton fab = root.findViewById(R.id.fabAddDish);
        if (fab != null) {
            fab.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            if (isAdmin) {
                fab.setOnClickListener(v ->
                        AddEditDishDialog.newAdd().show(getParentFragmentManager(), "add"));
            }
        }

        EditText et = root.findViewById(R.id.etSearch);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                query = (s == null) ? "" : s.toString();
                applyFilters(root);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        LinearLayout rowCats = root.findViewById(R.id.rowCats);
        buildCategoryButtons(rowCats);

        dishViewModel = new ViewModelProvider(this).get(DishViewModel.class);

        dishViewModel.getAllDishes().observe(getViewLifecycleOwner(), list -> {
            full.clear();
            if (list != null && !list.isEmpty()) {
                for (DishEntity e : list) {
                    if (e.imageUrl != null && !e.imageUrl.isEmpty()) {
                        full.add(new Dish(e.id, e.name, e.price, e.imageUrl, e.category));
                    } else {
                        int res = (e.imageRes == null) ? 0 : e.imageRes;
                        full.add(new Dish(e.id, e.name, e.price, res, e.category));
                    }
                }
            } else {
            }
            applyFilters(root);
        });

        observeDishesAndSeedIfNeeded(root);

        return root;
    }

    private void showQtyDialogAndAdd(Dish dish) {
        QtyDialog dlg = new QtyDialog().setOnPick(qty -> addDishToCart(dish, qty));
        dlg.show(getParentFragmentManager(), "qty");
    }

    private void addDishToCart(Dish dish, int qty) {
        if (qty <= 0) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase db = AppDatabase.get(requireContext());
            CartDao dao = db.cartDao();

            CartItem exist = dao.findById(dish.id);
            if (exist == null) {
                CartItem ci = new CartItem(
                        dish.id,
                        dish.name,
                        dish.price,
                        (dish.imageRes == null || dish.imageRes == 0) ? null : dish.imageRes,
                        (dish.imageUrl == null || dish.imageUrl.isEmpty()) ? null : dish.imageUrl,
                        qty
                );
                dao.upsert(ci);
            } else {
                dao.updateQty(dish.id, exist.qty + qty);
            }
        });
        Toast.makeText(getContext(), "Added x" + qty + " to cart", Toast.LENGTH_SHORT).show();
    }

    private void observeDishesAndSeedIfNeeded(View root) {
        DishDao dao = AppDatabase.get(requireContext()).dishDao();

        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                if (dao.count() == 0) {
                    List<DishEntity> seeds = new ArrayList<>();
                    for (Dish d : seedDishes()) {
                        Integer res = (d.imageRes == null || d.imageRes == 0) ? null : d.imageRes;
                        seeds.add(new DishEntity(d.name, d.price, res, d.imageUrl, d.category));
                    }
                    dao.insertAll(seeds);
                }
            } catch (Exception ignored) {}
        });
    }

    private void initCategoryLabels() {
        catLabels = new LinkedHashMap<>();
        catLabels.put(CAT_ALL,getString(R.string.cat_all));
        catLabels.put(CAT_STARTER, getString(R.string.cat_starter));
        catLabels.put(CAT_HEAVY, getString(R.string.cat_heavy));
        catLabels.put(CAT_FAST, getString(R.string.cat_fast));
        catLabels.put(CAT_DRINKS, getString(R.string.cat_drinks));
        catLabels.put(CAT_DESSERT,getString(R.string.cat_dessert));
    }

    private List<Dish> seedDishes() {
        List<Dish> list = new ArrayList<>();
        list.add(new Dish(1,  getString(R.string.dish_pepperoni_pizza), 9.90, R.drawable.food_pizza,  CAT_FAST));
        list.add(new Dish(2,  getString(R.string.dish_cheese_burger), 7.50, R.drawable.food_burger, CAT_FAST));
        list.add(new Dish(3,  getString(R.string.dish_chicken_shawarma), 6.90, R.drawable.food_shawarma, CAT_FAST));
        list.add(new Dish(4,  getString(R.string.dish_fries_box),  3.50, R.drawable.food_fries,  CAT_FAST));

        list.add(new Dish(10, getString(R.string.dish_beef_steak), 15.90, R.drawable.food_steak,     CAT_HEAVY));
        list.add(new Dish(11, getString(R.string.dish_grilled_chicken), 12.50, R.drawable.food_grill, CAT_HEAVY));
        list.add(new Dish(12, getString(R.string.dish_meat_pasta), 9.90, R.drawable.food_pasta,       CAT_HEAVY));
        list.add(new Dish(13, getString(R.string.dish_lasagna),    10.50, R.drawable.food_lasagna,    CAT_HEAVY));

        list.add(new Dish(20, getString(R.string.dish_bruschetta), 5.90, R.drawable.food_bruschetta,  CAT_STARTER));
        list.add(new Dish(21, getString(R.string.dish_greek_salad),6.50, R.drawable.food_salad,       CAT_STARTER));
        list.add(new Dish(22, getString(R.string.dish_spring_rolls),4.90, R.drawable.food_rolls,      CAT_STARTER));
        list.add(new Dish(23, getString(R.string.dish_soup_of_day),4.50, R.drawable.food_soup,        CAT_STARTER));

        list.add(new Dish(30, getString(R.string.dish_iced_coffee), 3.90, R.drawable.drink_icedcoffee, CAT_DRINKS));
        list.add(new Dish(31, getString(R.string.dish_fresh_orange),3.50, R.drawable.drink_orange,     CAT_DRINKS));
        list.add(new Dish(32, getString(R.string.dish_lemon_mint),  3.50, R.drawable.drink_lemon,      CAT_DRINKS));
        list.add(new Dish(33, getString(R.string.dish_milkshake),   4.50, R.drawable.drink_milkshake,  CAT_DRINKS));

        list.add(new Dish(40, getString(R.string.dish_cheesecake),  4.90, R.drawable.dessert_cheesecake, CAT_DESSERT));
        list.add(new Dish(41, getString(R.string.dish_chocolate_cake),5.50, R.drawable.dessert_choco,     CAT_DESSERT));
        list.add(new Dish(42, getString(R.string.dish_ice_cream),   3.90, R.drawable.dessert_icecream,    CAT_DESSERT));
        list.add(new Dish(43, getString(R.string.dish_tiramisu),    5.20, R.drawable.dessert_tiramisu,    CAT_DESSERT));

        list.add(new Dish(601, "Sushi Platter", 14.90, "https://images.pexels.com/photos/3298181/pexels-photo-3298181.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_HEAVY));
        list.add(new Dish(602, "Kebab Skewers", 11.50, "https://images.pexels.com/photos/1640777/pexels-photo-1640777.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_HEAVY));
        list.add(new Dish(603, "Fried Chicken Bucket", 9.90, "https://images.pexels.com/photos/4109138/pexels-photo-4109138.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_FAST));
        list.add(new Dish(604, "Asian Noodles", 8.50, "https://images.pexels.com/photos/2474658/pexels-photo-2474658.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_FAST));
        list.add(new Dish(605, "Veggie Sushi Rolls", 7.20, "https://images.pexels.com/photos/2098085/pexels-photo-2098085.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_STARTER));
        list.add(new Dish(606, "Burger", 4.50, "https://images.pexels.com/photos/139746/pexels-photo-139746.jpeg?auto=compress&cs=tinysrgb&w=800",CAT_FAST ));
        list.add(new Dish(607, "Fruit Cocktail", 5.00, "https://images.pexels.com/photos/140831/pexels-photo-140831.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DRINKS));
        list.add(new Dish(608, "Honey Pancakes", 6.20, "https://images.pexels.com/photos/376464/pexels-photo-376464.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DESSERT));
        list.add(new Dish(609, "Chocolate Chip Cookies", 3.50, "https://images.pexels.com/photos/230325/pexels-photo-230325.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DESSERT));
        list.add(new Dish(612, "Fish & Chips", 8.20, "https://images.pexels.com/photos/3756523/pexels-photo-3756523.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_FAST));
        list.add(new Dish(613, "Club Sandwich",  7.40, "https://images.pexels.com/photos/1600711/pexels-photo-1600711.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_FAST));
        list.add(new Dish(618, "Grilled Salmon", 14.90, "https://images.pexels.com/photos/3296279/pexels-photo-3296279.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_HEAVY));
        list.add(new Dish(620, "Caprese Salad", 5.80, "https://images.pexels.com/photos/1435907/pexels-photo-1435907.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_STARTER));
        list.add(new Dish(621, "Caesar Salad", 6.70, "https://images.pexels.com/photos/1640773/pexels-photo-1640773.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_STARTER));
        list.add(new Dish(626, "Cold Brew Coffee",3.90, "https://images.pexels.com/photos/373888/pexels-photo-373888.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DRINKS));
        list.add(new Dish(628, "Blueberry Cheesecake", 5.20, "https://images.pexels.com/photos/291528/pexels-photo-291528.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DESSERT));
        list.add(new Dish(630, "Chocolate Brownie", 4.40, "https://images.pexels.com/photos/3026807/pexels-photo-3026807.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DESSERT));
        list.add(new Dish(632, "Fruit Waffle", 5.10, "https://images.pexels.com/photos/239581/pexels-photo-239581.jpeg?auto=compress&cs=tinysrgb&w=800", CAT_DESSERT));

        return list;
    }

    private void buildCategoryButtons(LinearLayout row) {
        row.removeAllViews();
        for (Map.Entry<String, String> e : catLabels.entrySet()) {
            final String key   = e.getKey();
            final String label = e.getValue();

            Button b = new Button(requireContext(), null, 0, android.R.style.Widget_Material_Button_Borderless);
            b.setAllCaps(false);
            b.setText(label);
            b.setBackgroundResource(R.drawable.bg_category_chip_selector);
            b.setTextColor(ContextCompat.getColorStateList(requireContext(), R.drawable.chip_text_selector));

            int hp = dp(14), vp = dp(8);
            b.setPadding(hp, vp, hp, vp);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            lp.topMargin = dp(6);
            lp.bottomMargin = dp(6);
            b.setLayoutParams(lp);

            boolean selected = key.equals(selectedKey);
            b.setSelected(selected);
            b.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);

            b.setOnClickListener(v -> {
                selectedKey = key;
                applyFilters(row);
            });

            row.addView(b);
        }
    }

    private void applyFilters(View root) {
        shown.clear();
        for (Dish d : full) {
            String nm = (d.name == null) ? "" : d.name;
            boolean okCat = selectedKey.equals(CAT_ALL) || (d.category != null && d.category.equals(selectedKey));
            boolean okQ   = query.isEmpty() || nm.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
            if (okCat && okQ) shown.add(d);
        }

        if (adapter != null) adapter.notifyDataSetChanged();

        LinearLayout row = root.findViewById(R.id.rowCats);
        if (row != null) {
            String selectedLabel = catLabels.get(selectedKey);
            for (int i = 0; i < row.getChildCount(); i++) {
                View v = row.getChildAt(i);
                if (v instanceof Button) {
                    Button b = (Button) v;
                    boolean sel = (selectedLabel != null) && b.getText().toString().contentEquals(selectedLabel);
                    b.setSelected(sel);
                    b.setTypeface(null, sel ? Typeface.BOLD : Typeface.NORMAL);
                }
            }
        }
    }

    private int dp(int v) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(v * d);
    }
}
