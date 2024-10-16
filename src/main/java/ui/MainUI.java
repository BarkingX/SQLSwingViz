package ui;

import database.IDatabase;
import lombok.NonNull;
import ui.sub.*;
import ui.util.IconType;
import ui.util.Option;
import ui.util.UiUtil;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static ui.util.UiUtil.addAll;
import static ui.util.UiUtil.showDialog;
import static ui.util.UiUtil.makeJMenu;
import static ui.util.UiUtil.makeJMenuItem;

public class MainUI extends JFrame {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomeDialog welcomeDialog = new WelcomeDialog();
    private final DataImportDialog dataImportDialog = new DataImportDialog();
    private final DataDisplayPanel dataDisplayPanel = new DataDisplayPanel();

    public MainUI(@NonNull IDatabase db) {
        this.db = db;
        setIconImage(UiUtil.getIcon(IconType.GENERAL));
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
        catch (Exception e) {
            showErrorDialog("账号或密码错误，请检查后重新输入！").run();
        }
    }

    private @NonNull Runnable showErrorDialog(String message) {
        return () -> UiUtil.showErrorDialog(MainUI.this, message, "错误");
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

        showDialog(() -> UiUtil.showConfirmDialog(MainUI.this, "请确认是否要退出登录", "退出登录",
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
                showErrorDialog(e.getMessage()).run();
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
            dataDisplayPanel.display(db.queryWithFilter(filter.getValue(), selections)
                                       .orElse(new DefaultTableModel()));
        };

        Runnable fireDelete = () -> {
            try {
                db.deleteFromWheres(dataDisplayPanel.getTableName(), dataDisplayPanel.getSelectedRows());
                dataDisplayPanel.updateStatus(dataDisplayPanel.getMessageAfterDeletion());
            }
            catch (Exception e) {
                showErrorDialog(e.getMessage()).run();
                dataDisplayPanel.updateStatus("删除失败: 请检查条件或重试");
            }
        };

        dataDisplayPanel.reset();
        dataDisplayPanel.configureMainFilter(db.getQueryTypeFilterMap(), afterSelection);
        dataDisplayPanel.configureQueryAction(fireQuery);
        dataDisplayPanel.configureDeleteAction(fireDelete);
    }

    private void configDataImporting() {
        var importer = getJMenuBar().getMenu(1).getItem(1);
        importer.setEnabled(db.hasPrivilegeOfImportingData());
        if (importer.isEnabled() && dataImportDialog.notInitiated()) {
            dataImportDialog.populateTableNameComboBox(db.getTableNames());
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
