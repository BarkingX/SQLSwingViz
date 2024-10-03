package ui;

import database.IDatabase;
import org.jetbrains.annotations.NotNull;
import ui.sub.*;
import ui.util.Utils;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static ui.util.Utils.showDialog;

public class MainUI extends JFrame {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomePanel welcomePanel = new WelcomePanel();
    private final DataImportPanel dataImportPanel = new DataImportPanel();
    private final DataDisplayPanel dataDisplayPanel = new DataDisplayPanel();

    public MainUI(@NotNull IDatabase db) {
        this.db = db;
        setIconImage(Utils.getIcon(IconType.GENERAL));
        add(dataDisplayPanel, BorderLayout.CENTER);

        showWelcomeDialogWhile(db::isClosed);
        configureJMenuBar();
        initializeConfiguration();
    }

    private void showWelcomeDialogWhile(@NotNull BooleanSupplier condition) {
        while (condition.getAsBoolean()) {
            showDialog(() -> welcomePanel.showDialog(MainUI.this),
                       Option.SIGNIN, this::signIn, Option.SIGNUP, this::signUp);
        }
    }

    private void signIn() {
        try {
            db.authenticate(welcomePanel.getSignInUser());
        }
        catch (RuntimeException e) {
            showErrorDialog("账号或密码错误，请检查后重新输入！").run();
        }
    }

    private @NotNull Runnable showErrorDialog(String message) {
        return () -> Utils.showErrorDialog(MainUI.this, message, "登录失败");
    }

    private void signUp() {
        try {
            db.register(welcomePanel.getSingUpUser());
            JOptionPane.showMessageDialog(MainUI.this, "账号注册成功");
        }
        catch (Exception e) {
            showErrorDialog(e.getMessage()).run();
        }
    }

    private void configureJMenuBar() {
        var menuBar = new JMenuBar();
        var signOut = Utils.makeJMenuItem("登出账户", this::showSignOutDialog);
        var account = Utils.makeJMenu("账户", signOut);
        var printer = Utils.makeJMenuItem("数据打印", dataDisplayPanel::print);
        var importer = Utils.makeJMenuItem("数据导入", this::showDataImportDialog);
        var functions = Utils.makeJMenu("功能", printer, importer);
        Utils.addAll(menuBar, account, functions);
        setJMenuBar(menuBar);
    }

    private void showSignOutDialog() {
        Runnable signOut = () -> {
            db.disconnect();
            setVisible(false);
        };
        Runnable signOutAndReEnterWelcomePage = () -> {
            signOut.run();
            showWelcomeDialogWhile(db::isClosed);
            initializeConfiguration();
        };

        showDialog(() -> Utils.showConfirmDialog(MainUI.this, "请确认是否要退出登录", "退出登录",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE),
                Option.OK, signOutAndReEnterWelcomePage,
                Option.CANCEL, null);
    }

    private void showDataImportDialog() {
        Runnable loadData = () -> {
            try {
                db.loadDataInfile(dataImportPanel.getSelectedFile().getPath(),
                                  dataImportPanel.getSelectedTableName());
                JOptionPane.showMessageDialog(MainUI.this, "数据导入成功");
            }
            catch (RuntimeException e) {
                Utils.showErrorDialog(MainUI.this, e.getMessage(), "Error!");
            }
        };

        showDialog(() -> dataImportPanel.showDialog(MainUI.this),
                Option.OK, loadData,
                Option.ERROR, showErrorDialog("请选择需要导入的文件"));
    }

    private void initializeConfiguration() {
        configDataDisplayPanel();
        configDataImporting();
        setTitle("当前登录用户：" + welcomePanel.getSignInUser().account);
        setVisible(true);
        pack();
    }

    private void configDataDisplayPanel() {
        Consumer<FilterWrapper<String>> afterSelection = (filter) -> {
            dataDisplayPanel.configureFilters(db.getFilterMap(filter));
            pack();
        };

        Consumer<FilterWrapper<String>> fireQuery = (filter) -> {
            var selections = dataDisplayPanel.getSelectedFilter();
            dataDisplayPanel.display(db.queryWithFilter(filter, selections)
                                       .orElse(new DefaultTableModel()));
        };

        dataDisplayPanel.reset();
        dataDisplayPanel.configureMainFilter(db.getQueryTypeFilterMap(), afterSelection);
        dataDisplayPanel.configureQueryAction(fireQuery);
    }

    private void configDataImporting() {
        var importer = getJMenuBar().getMenu(1).getItem(1);
        importer.setEnabled(db.hasPrivilegeOfImportingData());
        if (importer.isEnabled() && dataImportPanel.notInitiated()) {
            var tableNames = db.getTableNames();
            tableNames.remove("user");
            dataImportPanel.populateTableNameComboBox(tableNames);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
