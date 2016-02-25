package org.motechproject.uitest.exception;

/**
 * Signals an issue encountered by the test framework itself.
 */
public class UITestFrameworkException extends RuntimeException {

    public UITestFrameworkException(String message) {
        super(message);
    }

    public UITestFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
