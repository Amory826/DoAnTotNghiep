package dev.edu.doctorappointment.Model;

public class ManagementItem {
    private String title;
    private int iconResId;
    private OnClickListener onClickListener;

    public ManagementItem(String title, int iconResId, OnClickListener onClickListener) {
        this.title = title;
        this.iconResId = iconResId;
        this.onClickListener = onClickListener;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick();
    }
} 