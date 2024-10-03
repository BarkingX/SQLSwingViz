package ui;

import database.IDatabase;
import ui.sub.*;
import ui.util.Utils;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static ui.util.Utils.showDialog;

public class MainUI extends JFrame {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomePanel welcomePanel = new WelcomePanel();
    private final DataImportPanel dataImportPanel = new DataImportPanel();
    private final DataDisplayPanel dataDisplayPanel = new DataDisplayPanel();

    public MainUI(IDatabase db) {
        this.db = db;
        setIconImage(Utils.getIcon(IconType.GENERAL));
        add(dataDisplayPanel, BorderLayout.CENTER);

        showWelcomeDialog();
        configureJMenuBar();
        initializeConfiguration();
    }

    private void showWelcomeDialog() {
        while (db.isClosed()) {
            showDialog(() -> welcomePanel.showDialog(MainUI.this),
                       Option.SIGNIN, this::signIn, Option.SIGNUP, this::signUp);
        }
    }

    private void signIn() {
        try {
            db.authenticate(welcomePanel.getSignInUser());
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
            db.register(welcomePanel.getSingUpUser());
            JOptionPane.showMessageDialog(MainUI.this, "账号注册成功");
        }
        catch (Exception e) {
            showErrorDialog(e.getMessage());
        }
    }

    private void configureJMenuBar() {
        var menuBar = new JMenuBar();
        var signOut = Utils.makeJMenuItem("登出账户", e -> showSignOutDialog());
        var account = Utils.makeJMenu("账户", signOut);
        var printer = Utils.makeJMenuItem("数据打印", e -> dataDisplayPanel.print());
        var importer = Utils.makeJMenuItem("数据导入", e -> showDataImportDialog());
        var functions = Utils.makeJMenu("功能", printer, importer);
        Utils.addAll(menuBar, account, functions);
        setJMenuBar(menuBar);
    }

    private void showSignOutDialog() {
        showDialog(() -> Utils.showConfirmDialog(MainUI.this, "请确认是否要退出登录", "退出登录",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE),
                Option.OK, this::signOutAndReEnterWelcomePage,
                Option.CANCEL, null);
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

    private void showDataImportDialog() {
        showDialog(() -> dataImportPanel.showDialog(MainUI.this),
                Option.OK, this::importData,
                Option.ERROR, () -> showErrorDialog("请选择需要导入的文件"));
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
        configureDataDisplayPanel();
        var canImportData = db.hasPrivilegeOfImportingData();
        getJMenuBar().getMenu(1).getItem(1).setEnabled(canImportData);
        if (canImportData && dataImportPanel.notInitiated()) {
            initDataImportPanel();
        }
        setTitle("当前登录用户：" + welcomePanel.getSignInUser().account);
        setVisible(true);
        pack();
    }

    private void configureDataDisplayPanel() {
        dataDisplayPanel.reset();
        dataDisplayPanel.configureMainFilter(db.getQueryTypeFilterMap(), this::afterSelection);
        dataDisplayPanel.configureQueryAction(this::fireQueryAction);
    }

    private void initDataImportPanel() {
        var tableNames = db.getTableNames();
        //TODO how to deal with user?
        tableNames.remove("user");
        dataImportPanel.populateTableNameComboBox(tableNames);
    }

    private void afterSelection(FilterWrapper<String> mainFilter) {
        dataDisplayPanel.configureFilters(db.getFilterMap(mainFilter));
        pack();
    }

    private void fireQueryAction(FilterWrapper<String> mainFilter) {
        var selections = dataDisplayPanel.getSelectedFilter();
        dataDisplayPanel.display(db.queryWithFilter(mainFilter, selections)
                                   .orElse(new DefaultTableModel()));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
