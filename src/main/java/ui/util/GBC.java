package ui.util;

import java.awt.*;

public class GBC extends GridBagConstraints {
    public GBC(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public GBC(int gridx, int gridy, int gridwidth, int gridheight) {
        this(gridx, gridy);
        this.gridwidth = gridwidth;
        this.gridheight = gridheight;
    }
}

