package ui.sub;

import ui.abs.DataIODialogWrapper;
import util.Metadata;
import util.Profile;

public class OrderPanel extends DataIODialogWrapper {
    @Override
    public boolean checkIntegrity() {
        return haveText(NAME_COLUMN, ADDRESS_COLUMN);
    }

    @Override
    public String getTitle() {
        return "订单";
    }

    @Override
    public Metadata<String> getMetadata() {
        return Profile.METADATA;
    }
}
