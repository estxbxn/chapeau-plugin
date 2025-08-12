package com.whimsy.chapeau.messaging.handlers;

/**
 * Callback interface for command execution results.
 */
public interface CommandCallback {
    /**
     * Called when command execution is complete.
     *
     * @param success True if the command executed successfully
     * @param error   Error message if command failed (null if successful)
     * @param output  Any output or additional information from the command
     */
    void onComplete(boolean success, String error, String output);
}
