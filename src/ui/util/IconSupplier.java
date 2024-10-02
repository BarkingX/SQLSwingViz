package ui.util;

import util.IconType;

import java.awt.*;
import java.util.Map;

public interface IconSupplier {
    Map<IconType, Image> icons = Utils.getIcons();
}
