package com.threepsoft.eva.view.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;
import com.threepsoft.eva.R;
import com.threepsoft.eva.model.SubCategory;
import com.threepsoft.eva.utils.CustomVolleyRequestQueue;
import com.threepsoft.eva.utils.ServiceApi;
import com.threepsoft.eva.utils.Utils;
import com.threepsoft.eva.view.fragments.SpotsFragment;

class SubCategoryViewHolder extends ChildViewHolder {

    private TextView phoneName;
    private NetworkImageView imageView;
    private View view;
    private Activity activity;

    SubCategoryViewHolder(View itemView, Activity activity) {
        super(itemView);

        phoneName = itemView.findViewById(R.id.txt_sub_title);
        imageView = itemView.findViewById(R.id.img_sub_category);
        view = itemView;
        this.activity = activity;
    }

    void onBind(final SubCategory phone) {
        phoneName.setText(phone.getName());
        if (!Utils.isEmptyString(phone.getImagePath())) {
            String url = ServiceApi.baseurl + phone.getImagePath();
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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(activity, SpotsActivity.class);
//                intent.putExtra("category_id", phone.getCategoryID());
//                intent.putExtra("subCategory_id", phone.getSpotCategoryID());
//                intent.putExtra("spot_id", phone.getSpotId());
//                intent.putExtra("name", phone.getName());
//                activity.startActivity(intent);
                Fragment fragment = new SpotsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("category_id", phone.getCategoryID());
                bundle.putString("spot_id", phone.getSpotId());
                bundle.putString("subCategory_id", phone.getSpotCategoryID());
                bundle.putString("name", phone.getName());
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
