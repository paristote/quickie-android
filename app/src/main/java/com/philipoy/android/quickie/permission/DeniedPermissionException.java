package com.philipoy.android.quickie.permission;

/**
 * Created by philippeexo on 11/2/16.
 */
public class DeniedPermissionException extends Exception {

    private String deniedPermission;

    public DeniedPermissionException(String permission) {
        this.deniedPermission = permission;
    }

    public String getDeniedPermission() {
        return deniedPermission;
    }

}
