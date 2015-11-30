/*
 * Materialize - Materialize all those not material
 * Copyright (C) 2015  XiNGRZ <xxx@oxo.ooo>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ooo.oxo.apps.materialize;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.xml.transform.Source;

import ooo.oxo.apps.materialize.databinding.AboutActivityBinding;
import ooo.oxo.apps.materialize.databinding.AboutHeaderBinding;
import ooo.oxo.apps.materialize.databinding.AboutLibraryItemBinding;
import ooo.oxo.library.databinding.support.widget.BindingRecyclerView;

public class AboutActivity extends AppCompatActivity {

    private final ArrayMap<String, String> libraries = new ArrayMap<>();
    private final ArrayList<String> source = new ArrayList<>();
    private final ArrayList<String> source_link = new ArrayList<>();

    @Override
    @SuppressWarnings("SpellCheckingInspection")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AboutActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.about_activity);

        binding.toolbar.setNavigationOnClickListener(v -> supportFinishAfterTransition());

        source.add("oxoooo / materialize");
        source_link.add("https://github.com/oxoooo/materialize");
        source.add("RikkaW / materialize (materialize-mod)");
        source_link.add("https://github.com/RikkaW/materialize");

        libraries.put("romannurik / AndroidAssetStudio", "https://github.com/romannurik/AndroidAssetStudio");

        binding.libraries.setAdapter(new LibrariesAdapter());
    }

    private void open(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    class LibrariesAdapter extends RecyclerView.Adapter<BindingRecyclerView.ViewHolder> {

        private final LayoutInflater inflater = getLayoutInflater();

        @Override
        public BindingRecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == 0
                    ? new HeaderViewHolder(AboutHeaderBinding.inflate(inflater, parent, false))
                    : new ItemViewHolder(AboutLibraryItemBinding.inflate(inflater, parent, false));
        }

        @Override
        public void onBindViewHolder(BindingRecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == 0) {
                ((HeaderViewHolder) holder).binding.setName(position == 0
                        ? R.string.fork_me_on_github
                        : R.string.libraries_used);
            } else {
                ItemViewHolder itemHolder = (ItemViewHolder) holder;
                if (position / 2 < source.size()) {
                    itemHolder.binding.setName(source.get(position / 2));
                } else {
                    itemHolder.binding.setName(libraries.keyAt(position - 4));
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return (position == 0 || position == 3) ? 0 : 1;
    }

        @Override
        public int getItemCount() {
            return libraries.size() + source.size() + 2;
        }

        private void handleItemClick(int position) {
            if (position <= 2) {
                open(source_link.get(position - 1));
            } else {
                open(libraries.valueAt(position - 4));
            }
        }

        class HeaderViewHolder extends BindingRecyclerView.ViewHolder<AboutHeaderBinding> {

            public HeaderViewHolder(AboutHeaderBinding binding) {
                super(binding);
            }

        }

        class ItemViewHolder extends BindingRecyclerView.ViewHolder<AboutLibraryItemBinding> {

            public ItemViewHolder(AboutLibraryItemBinding binding) {
                super(binding);
                itemView.setOnClickListener(v -> handleItemClick(getAdapterPosition()));
            }

        }

    }

}
