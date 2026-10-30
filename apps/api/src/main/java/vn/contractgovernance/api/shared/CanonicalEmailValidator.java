package vn.contractgovernance.api.shared;

import com.networknt.org.apache.commons.validator.routines.EmailValidator;
import java.net.IDN;

/** Mailbox syntax only: canonical synthetic domains do not need a registered TLD. */
final class CanonicalEmailValidator extends EmailValidator {
    private static final long serialVersionUID = 1L;

    CanonicalEmailValidator() {
        super(true, true);
    }

    @Override
    protected boolean isValidDomain(String domain) {
        if (domain == null || domain.isEmpty()) return false;
        if (domain.startsWith("[")) {
            // Preserve networknt's IPv6AwareEmailValidator normalization and literal validation.
            String literal = domain.startsWith("[IPv6:") ? "[" + domain.substring(6) : domain;
            return super.isValidDomain(literal);
        }
        final String ascii;
        try {
            ascii = IDN.toASCII(domain, IDN.USE_STD3_ASCII_RULES);
        } catch (IllegalArgumentException invalidDomain) {
            return false;
        }
        if (ascii.isEmpty() || ascii.length() > 253) return false;
        for (String label : ascii.split("\\.", -1)) {
            if (label.isEmpty() || label.length() > 63
                    || label.charAt(0) == '-' || label.charAt(label.length() - 1) == '-') return false;
            for (int index = 0; index < label.length(); index++) {
                char value = label.charAt(index);
                if (!((value >= 'a' && value <= 'z') || (value >= 'A' && value <= 'Z')
                        || (value >= '0' && value <= '9') || value == '-')) return false;
            }
        }
        return true;
    }
}
