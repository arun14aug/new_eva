package com.threepsoft.eva.view.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;
import com.threepsoft.eva.R;
import com.threepsoft.eva.model.Category;
import com.threepsoft.eva.utils.CustomVolleyRequestQueue;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.utils.Utils;
import com.threepsoft.eva.view.fragments.SpotsFragment;

public class CategoryViewHolder extends GroupViewHolder {

    private TextView osName;
    private NetworkImageView imageView;
    private Activity activity;
//    private View view;
    private ImageView arrow;
//    private boolean isExpanded = false;

    CategoryViewHolder(View itemView, Activity activity) {
        super(itemView);
        this.activity = activity;
        osName = itemView.findViewById(R.id.txt_title);
        imageView = itemView.findViewById(R.id.img_category);
        arrow = itemView.findViewById(R.id.img_arrow);
//        view = itemView;
    }

    @Override
    public void expand() {
//        osName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
        arrow.setImageResource(R.drawable.ic_expand_less);
//        isExpanded = true;
        Log.i("Adapter", "expand");
    }

    @Override
    public void collapse() {
        Log.i("Adapter", "collapse");
//        osName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
        arrow.setImageResource(R.drawable.ic_expand_more);
//        isExpanded = false;
    }

    void setGroupName(final ExpandableGroup group, final Category category) {
        arrow.setImageResource(R.drawable.ic_expand_less);
        osName.setText(group.getTitle());
        if (!Utils.isEmptyString(category.getImagePath())) {
            String url = ServiceApi.baseurl + category.getImagePath();
            Log.e(" URL : ", "" + url);
            ImageLoader mImageLoader;
            mImageLoader = new CustomVolleyRequestQueue(activity)
                    .getImageLoader();
////        if (!imageUrl.equalsIgnoreCase("null"))
            mImageLoader.get(url, ImageLoader.getImageListener(imageView,
                /*R.drawable.logo*/ 0, /*R.drawable.logo*/ 0));
            imageView.setImageUrl(url, mImageLoader);
            imageView.setTag(url);

        } else
            imageView.setImageResource(R.mipmap.ic_launcher_round);

        osName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(activity, SpotsActivity.class);
//                intent.putExtra("spot_id", category.getSpotId());
//                intent.putExtra("category_id", category.getSpotCategoryID());
//                intent.putExtra("name", category.getName());
//                activity.startActivity(intent);

                Fragment fragment = new SpotsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("spot_id", category.getSpotId());
                bundle.putString("category_id", category.getSpotCategoryID());
                bundle.putString("name", category.getName());
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = ((FragmentActivity) activity)
                        .getSupportFragmentManager();
                String title = "SpotsFragment";
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body, fragment, title);
                fragmentTransaction.addToBackStack(title);
                fragmentTransaction.commit();
            }
        });

    }
}
