package exceptions;

import static defines.Errors.INVALID_SEARCH_OPTION;

public class InvalidSearchOption extends Exception {
    public InvalidSearchOption() {
        super(INVALID_SEARCH_OPTION);
    }
}
