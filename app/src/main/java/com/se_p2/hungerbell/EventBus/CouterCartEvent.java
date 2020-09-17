package com.se_p2.hungerbell.EventBus;

public class CouterCartEvent {
    private boolean success;

    public CouterCartEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
