/*
 * Copyright (c) 2019 Martin Pfeffer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.celox.android_toolbox.adapters;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pepperonas.aespreferences.AesPrefs;
import com.pepperonas.andbasx.base.ClipboardUtils;
import com.pepperonas.andbasx.base.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.celox.android_toolbox.R;
import io.celox.android_toolbox.models.ClipDataAdvanced;
import io.celox.android_toolbox.utils.Const;
import io.celox.android_toolbox.utils.Database;

/**
 * @author Martin Pfeffer (celox.io)
 * @see <a href="mailto:martin.pfeffer@celox.io">martin.pfeffer@celox.io</a>
 */
public class ClipDataAdvancedAdapter extends
        RecyclerView.Adapter<ClipDataAdvancedAdapter.ClipDataAdvancedViewHolder> {

    private Activity mActivity;
    private List<ClipDataAdvanced> mClips;

    private Database mDb;

    public ClipDataAdvancedAdapter(Activity activity, Database database, List<ClipDataAdvanced> clips) {
        mActivity = activity;
        this.mDb = database;
        this.mClips = clips;
    }

    @Override
    public int getItemCount() {
        return mClips.size();
    }

    @NotNull
    @Override
    public ClipDataAdvancedViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_clip_data, parent, false);
        return new ClipDataAdvancedViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final ClipDataAdvancedViewHolder holder, final int pos) {
        final boolean unlocked = AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals("")
                || AesPrefs.getLongRes(R.string.LOGOUT_TIME, 0) > System.currentTimeMillis();

        Drawable d = getDrawableByType(mClips.get(holder.getAdapterPosition()).getType());
        holder.icon.setImageDrawable(d);

        holder.tvClipDataText.setTextSize(mClips.get(holder.getAdapterPosition()).getSizedText().getTextSize());
        holder.tvClipDataText.setText(mClips.get(holder.getAdapterPosition()).getSizedText().getText());

        holder.tvTsDate.setText(mClips.get(holder.getAdapterPosition()).getCreationDate());
        holder.tvTsTime.setText(mClips.get(holder.getAdapterPosition()).getCreationTime());

        Typeface typeface;
        if (unlocked) {
            typeface = Typeface.DEFAULT;
        } else {
            typeface = Typeface.createFromAsset(mActivity.getAssets(), "fonts/monaco.ttf");
        }
        holder.tvClipDataText.setTypeface(typeface);
        holder.tvTsDate.setTypeface(typeface);
        holder.tvTsTime.setTypeface(typeface);

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unlocked) {
                    mDb.deleteClipData(mClips.get(holder.getAdapterPosition()).getTimestamp());
                    ClipboardUtils.setClipboard(mClips.get(holder.getAdapterPosition()).getContent());
                    ToastUtils.toastShort(mActivity.getString(R.string.copied_to_clipboard));
                    mActivity.finish();
                } else {
                    ToastUtils.toastShort(mActivity.getString(R.string.encrypted_text_cant_be_copied));
                }
            }
        });
    }

    private Drawable getDrawableByType(ClipDataAdvanced.Type type) {
        if (!(AesPrefs.getRes(R.string.ENCRYPTION_PASSWORD, "").equals("")
                || AesPrefs.getLongRes(R.string.LOGOUT_TIME, 0) > System.currentTimeMillis())) {
            return new IconicsDrawable(mActivity, CommunityMaterial.Icon.cmd_help)
                    .colorRes(R.color.clip_type_icon)
                    .sizeDp(Const.NAV_DRAWER_ICON_SIZE);
        }
        switch (type) {
            case URL:
                return new IconicsDrawable(mActivity, CommunityMaterial.Icon.cmd_link)
                        .colorRes(R.color.clip_type_icon)
                        .sizeDp(Const.NAV_DRAWER_ICON_SIZE);
            case GOOGLE_DRIVE:
                return new IconicsDrawable(mActivity, CommunityMaterial.Icon.cmd_google_drive)
                        .colorRes(R.color.clip_type_icon)
                        .sizeDp(Const.NAV_DRAWER_ICON_SIZE);
            default:
                return new IconicsDrawable(mActivity, CommunityMaterial.Icon.cmd_message_text_outline)
                        .colorRes(R.color.clip_type_icon)
                        .sizeDp(Const.NAV_DRAWER_ICON_SIZE);
        }
    }

    public class ClipDataAdvancedViewHolder extends RecyclerView.ViewHolder {

        private CardView cv;
        private ImageView icon;
        private TextView tvClipDataText;
        private TextView tvTsDate;
        private TextView tvTsTime;

        public ClipDataAdvancedViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.clip_data_card_container);
            icon = itemView.findViewById(R.id.iv_clip_data_card_icon);
            tvClipDataText = itemView.findViewById(R.id.tv_card_clip_data_text);
            tvTsDate = itemView.findViewById(R.id.tv_clip_data_created_date);
            tvTsTime = itemView.findViewById(R.id.tv_clip_data_created_time);
        }
    }
}
