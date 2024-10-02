package ui.util;

import util.IconType;

import java.awt.*;
import java.util.EnumMap;

public interface IconSupplier {
    EnumMap<IconType, Image> icons = Utils.getIcons();
}
