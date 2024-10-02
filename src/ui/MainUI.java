package ui;

import database.IDatabase;
import ui.sub.*;
import ui.util.IconSupplier;
import ui.util.Utils;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class MainUI extends JFrame implements IconSupplier {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomePanel welcomePanel;
    private final AccountInfoPanel accountPanel;
    private final DataImportPanel dataImportPanel;
    private final DataDisplayPanel dataDisplayPanel;
    private final OrderPanel orderPanel;
    private JMenuItem importData;

    public MainUI(IDatabase db, WindowListener l) {
        this.db = db;
        welcomePanel = new WelcomePanel(l);
        accountPanel = new AccountInfoPanel();
        dataImportPanel = new DataImportPanel();
        dataDisplayPanel = new DataDisplayPanel();
        orderPanel = new OrderPanel();

        addWindowListener(l);
        setIconImage(icons.get(IconType.GENERAL));
        add(dataDisplayPanel, BorderLayout.CENTER);

        showWelcomeDialog();
        configureJMenuBar();
        initializeConfiguration();
    }

    private void showWelcomeDialog() {
        while (!db.isConnected())
            showDialog(() -> welcomePanel.showDialog(MainUI.this),
                    Option.SIGNIN, this::signIn, Option.SIGNUP, this::signUp);
    }

    private void showDialog(Supplier<Option> showDialog, Option success, Runnable afterSuccess,
                            Option failed, Runnable afterFailed) {
        var result = showDialog.get();
        if (result == success) afterSuccess.run();
        else if (result == failed) afterFailed.run();
    }

    private void signIn() {
        try {
            var u = welcomePanel.getSignInUser();
            db.authenticate(u);
            db.authorize(u.account);
        }
        catch (RuntimeException e) {
            showErrorDialog(e.getMessage());
        }
    }

    private void showErrorDialog(String message) {
        Utils.showErrorDialog(MainUI.this, message, "Error!");
    }

    private void signUp() {
        try {
            var u = welcomePanel.getSingUpUser();
            var p = welcomePanel.getRegisteredProfile();
            var userInfo = List.of(u.account, u.password);
            var shippingInfo = packageShippingInfo(u.account, p);
            db.register(userInfo, shippingInfo);
            JOptionPane.showMessageDialog(MainUI.this, "账号注册成功");
        }
        catch (Exception e) {
            showErrorDialog(e.getMessage());
        }
    }

    private List<String> packageShippingInfo(String account, Profile p) {
        var shippingInfo = new LinkedList<>(p.toList());
        shippingInfo.addFirst(account);
        return shippingInfo;
    }

    private void configureJMenuBar() {
        var JMenuBar = new JMenuBar();
        populateJMenuBar(JMenuBar);
        setJMenuBar(JMenuBar);
    }

    private void populateJMenuBar(JMenuBar menuBar) {
        var modify = Utils.makeJMenuItem("添加信息", e -> showUserInfoModifyDialog());
        var signOut = Utils.makeJMenuItem("登出账户", e -> showSignOutDialog());
        var account = Utils.makeJMenu("账户", modify, signOut);
        var print = Utils.makeJMenuItem("电子版打印", e -> dataDisplayPanel.print());
        var delivery = Utils.makeJMenuItem("纸质版邮寄", e -> showFillOrderDialog());
        var statistics = Utils.makeJMenu("数据统计", print, delivery);
        importData = Utils.makeJMenuItem("数据导入", e -> showDataImportDialog());
        var functions = Utils.makeJMenu("功能", statistics, importData);
        Utils.addAll(menuBar, account, functions);
    }

    private void showUserInfoModifyDialog() {
        showDialog(() -> accountPanel.showDialog(MainUI.this),
                Option.OK, this::modifyUserInfo, Option.CANCEL, accountPanel::closeDialog);
    }

    private void modifyUserInfo() {
        var u = accountPanel.getUser();
        var p = accountPanel.getProfile();
        var shippingInfo = packageShippingInfo(u.account, p);
        db.insertShippingInfo(shippingInfo);
        JOptionPane.showMessageDialog(MainUI.this, "信息添加成功");
    }

    private void showSignOutDialog() {
        showDialog(() -> Utils.showConfirmDialog(MainUI.this, "请确认是否要退出登录", "退出登录",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE),
                Option.OK, this::signOutAndReEnterWelcomePage, Option.CANCEL, () -> {});
    }

    private void signOutAndReEnterWelcomePage() {
        signOut();
        showWelcomeDialog();
        initializeConfiguration();
    }

    private void signOut() {
        db.disconnect();
        setVisible(false);
    }

    private void showFillOrderDialog() {
        showDialog(() -> orderPanel.showDialog(MainUI.this),
                Option.OK, this::sendOrder, Option.ERROR, () -> showErrorDialog("订单填写错误！"));
    }

    private void sendOrder() {
        if (dataDisplayPanel.hasData())
            JOptionPane.showMessageDialog(MainUI.this, "您的包裹将在xx天后送达！");
        else showErrorDialog("请选择需要的数据！");
    }

    private void showDataImportDialog() {
        showDialog(() -> dataImportPanel.showDialog(MainUI.this),
                Option.OK, this::importData, Option.ERROR,
                () -> showErrorDialog("请选择需要导入的文件"));
    }

    private void importData() {
        try {
            var path = dataImportPanel.getSelectedFile().getPath();
            var tableName = dataImportPanel.getSelectedTableName();
            db.loadDataInfile(path, tableName);
            JOptionPane.showMessageDialog(MainUI.this, "数据导入成功");
        }
        catch (RuntimeException e) {
            Utils.showErrorDialog(MainUI.this, e.getMessage(), "Error!");
        }
    }

    private void initializeConfiguration() {
        authorize();
        configureDataDisplayPanel();
        configureDataImportPanelIfNeeded();
        setTitle("当前登录用户：" + welcomePanel.getSignInUser().account);
        setVisible(true);
        pack();
    }

    private void authorize() {
        importData.setEnabled(db.hasPrivilegeOfImportingData());
    }

    private void configureDataDisplayPanel() {
        dataDisplayPanel.reset();
        dataDisplayPanel.configureMainFilter(db.getQueryTypeFilterMap(), this::afterSelection);
        dataDisplayPanel.configureQueryAction(this::fireQueryAction);
    }

    private void afterSelection(FilterWrapper<String> mainFilter) {
        var filterMap = db.getFilterMap(mainFilter);
        dataDisplayPanel.configureFilters(filterMap);
        pack();
    }

    private void fireQueryAction(FilterWrapper<String> mainFilter) {
        var filter = dataDisplayPanel.getSelectedFilter();
        var model = db.queryWithFilter(mainFilter, filter);
        if (model == null) return;
        dataDisplayPanel.display(model);
    }

    private void configureDataImportPanelIfNeeded() {
        if (importData.isEnabled()) {
            dataImportPanel.resetAfterReEnter();
            var tableNames = db.getTableNames();
            tableNames.remove("user");
            tableNames.remove("shipping_info");
            dataImportPanel.populateTableNameComboBox(tableNames);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
