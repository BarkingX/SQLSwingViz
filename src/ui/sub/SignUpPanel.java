package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.abs.SignPanel;
import util.*;


import java.awt.*;

public class SignUpPanel extends SignPanel {
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 400;

    public @NotNull Profile getProfile() {
        return Profile.of(getTextOf(NAME_COLUMN), getTextOf(EMAIL_COLUMN), getTextOf(PHONE_COLUMN),
                Address.of(getTextOf(PROVINCE_COLUMN), getTextOf(CITY_COLUMN),
                        getTextOf(DISTRICT_COLUMN), getTextOf(ADDRESS_COLUMN)));
    }

    @Override
    public @NotNull StrMetadata getMetadata() {
        return new StrMetadata(User.METADATA, Profile.METADATA);
    }

    @Override
    public @NotNull String getTitle() {
        return "注册";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
