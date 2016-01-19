package com.asadmshah.drawnearby.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.asadmshah.drawnearby.R;

public class HorizontalColorPickerView extends RecyclerView {

    private Adapter adapter;
    private OnColorSelectedListener onColorSelectedListener;

    public HorizontalColorPickerView(Context context) {
        super(context);
        init();
    }

    public HorizontalColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        setAdapter(adapter = new Adapter(getContext().getResources().getIntArray(R.array.colors)));
    }

    public void setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
        this.onColorSelectedListener = onColorSelectedListener;
    }

    private final class Adapter extends RecyclerView.Adapter<ColorViewHolder> {

        private final int[] colors;

        private Adapter(int[] colors) {
            this.colors = colors;
        }

        @Override
        public ColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.setIntrinsicHeight(parent.getHeight() * 2 / 3);
            drawable.setIntrinsicWidth(parent.getHeight() * 2 / 3);
            drawable.getPaint().setColor(Color.WHITE);
            drawable.getPaint().setAntiAlias(true);

            ImageView view = new ImageView(parent.getContext());
            view.setLayoutParams(new ViewGroup.LayoutParams(parent.getHeight(), parent.getHeight()));
            view.setImageDrawable(drawable);
            view.setScaleType(ImageView.ScaleType.CENTER);
            parent.addView(view);

            final ColorViewHolder vh = new ColorViewHolder(view);
            vh.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onColorSelectedListener != null) {
                        onColorSelectedListener.onColorSelected(colors[vh.getAdapterPosition()]);
                    }
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ColorViewHolder holder, int position) {
            holder.imageView.setColorFilter(colors[position]);
        }

        @Override
        public int getItemCount() {
            return colors.length;
        }
    }

    private static final class ColorViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;

        public ColorViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

}
