package ui;

import database.IDatabase;
import lombok.NonNull;
import ui.sub.*;
import ui.util.Utils;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static ui.util.Utils.addAll;
import static ui.util.Utils.showDialog;
import static ui.util.Utils.makeJMenu;
import static ui.util.Utils.makeJMenuItem;

public class MainUI extends JFrame {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomeDialog welcomeDialog = new WelcomeDialog();
    private final DataImportDialog dataImportDialog = new DataImportDialog();
    private final DataDisplayPanel dataDisplayPanel = new DataDisplayPanel();

    public MainUI(@NonNull IDatabase db) {
        this.db = db;
        setIconImage(Utils.getIcon(IconType.GENERAL));
        add(dataDisplayPanel, BorderLayout.CENTER);

        showWelcomeDialogWhile(db::isDisconnected);
        configureJMenuBar();
        initializeConfiguration();
    }

    private void showWelcomeDialogWhile(@NonNull BooleanSupplier condition) {
        while (condition.getAsBoolean()) {
            showDialog(() -> welcomeDialog.showDialog(MainUI.this),
                       Option.SIGNIN, this::signIn, Option.SIGNUP, this::signUp);
        }
    }

    private void signIn() {
        try {
            db.authenticate(welcomeDialog.getSignInUser());
        }
        catch (RuntimeException e) {
            showErrorDialog("账号或密码错误，请检查后重新输入！").run();
        }
    }

    private @NonNull Runnable showErrorDialog(String message) {
        return () -> Utils.showErrorDialog(MainUI.this, message, "登录失败");
    }

    private void signUp() {
        try {
            db.register(welcomeDialog.getSingUpUser());
            JOptionPane.showMessageDialog(MainUI.this, "账号注册成功");
        }
        catch (Exception e) {
            showErrorDialog(e.getMessage()).run();
        }
    }

    private void configureJMenuBar() {
        var account = makeJMenu("账户", makeJMenuItem("登出账户", this::showSignOutDialog));
        var functions = makeJMenu("功能",
                                  makeJMenuItem("数据打印", dataDisplayPanel::print),
                                  makeJMenuItem("数据导入", this::showDataImportDialog));
        setJMenuBar((JMenuBar) addAll(new JMenuBar(), account, functions));
    }

    private void showSignOutDialog() {
        Runnable signOutAndReEnterWelcomePage = () -> {
            db.disconnect();
            setVisible(false);
            showWelcomeDialogWhile(db::isDisconnected);
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
                db.loadDataInfile(dataImportDialog.getSelectedFile().getPath(),
                                  dataImportDialog.getSelectedTableName());
                JOptionPane.showMessageDialog(MainUI.this, "数据导入成功");
            }
            catch (RuntimeException e) {
                Utils.showErrorDialog(MainUI.this, e.getMessage(), "Error!");
            }
        };

        showDialog(() -> dataImportDialog.showDialog(MainUI.this),
                Option.OK, loadData,
                Option.ERROR, showErrorDialog("请选择需要导入的文件"));
    }

    private void initializeConfiguration() {
        configDataDisplayPanel();
        configDataImporting();
        setTitle("当前登录用户：" + welcomeDialog.getSignInUser().account);
        setVisible(true);
        pack();
    }

    private void configDataDisplayPanel() {
        Consumer<FilterWrapper<String>> afterSelection = filter -> {
            dataDisplayPanel.configureFilters(db.getFilterMap(filter.getValue()));
            pack();
        };

        Consumer<FilterWrapper<String>> fireQuery = filter -> {
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
        if (importer.isEnabled() && dataImportDialog.notInitiated()) {
            var tableNames = db.getTableNames();
//            tableNames.remove("user");
            dataImportDialog.populateTableNameComboBox(tableNames);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
