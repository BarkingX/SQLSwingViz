package ui.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IoTypeConstants {
    ACCOUNT(1),
    PASSWORD(2),
    ROLE(3);
    public final int value;
}
