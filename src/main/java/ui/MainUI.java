package ui;

import database.IDatabase;
import lombok.NonNull;
import model.Role;
import model.User;
import ui.sub.*;
import ui.util.Option;
import ui.util.UiUtil;
import util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.BooleanSupplier;

import static ui.util.UiUtil.*;

public class MainUI extends JFrame {
    private final static int DEFAULT_WIDTH = 1200;
    private final static int DEFAULT_HEIGHT = 800;
    private final IDatabase db;
    private final WelcomeDialog welcomeDialog = new WelcomeDialog();
    private final DataDisplayPanel dataDisplayPanel = new DataDisplayPanel();
    private DataImportDialog dataImportDialog;
    private RootSignUpPanel rootSignUpPanel;

    public MainUI(@NonNull IDatabase db) {
        this.db = db;
        setIconImage(UiUtil.getIcon(Role.NULL));
        add(dataDisplayPanel, BorderLayout.CENTER);
        showWelcomeDialogWhile(db::isDisconnected);
        configureJMenuBar();
        initializeConfiguration();
    }

    private void showWelcomeDialogWhile(@NonNull BooleanSupplier condition) {
        while (condition.getAsBoolean()) {
            showDialog(() -> welcomeDialog.showDialog(this),
                       Option.SIGNIN, this::signIn, Option.SIGNUP, this::userSignUp);
        }
    }

    private void signIn() {
        try {
            var signInUser = welcomeDialog.getSignInUser();
            var role = db.authenticate(signInUser);
            welcomeDialog.updateUser(signInUser.withRole(role));
        }
        catch (Exception e) {
            showErrorDialog(this, welcomeDialog.getSignInErrorMessage(), "登录失败");
        }
    }

    private void userSignUp() {
        signUp(welcomeDialog.getSingUpUser(), welcomeDialog.getSignUpErrorMessage());
    }

    private void signUp(@NonNull User user, @NonNull String errorMessage) {
        try {
            db.register(user);
            JOptionPane.showMessageDialog(this, "注册成功");
        }
        catch (Exception e) {
            showErrorDialog(this, errorMessage, "注册失败");
        }
    }

    private void configureJMenuBar() {
        var account = makeJMenu("账户",
                makeJMenuItem("登出账户", this::showSignOutDialog),
                makeJMenuItem("注册新账户", this::showRootSignUpDialog));
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

        showDialog(() -> showConfirmDialog(this, "请确认是否要退出登录", "退出登录",
                          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE),
                Option.OK, signOutAndReEnterWelcomePage,
                Option.CANCEL, null);
    }

    private void showRootSignUpDialog() {
        showDialog(() -> rootSignUpPanel.showDialog(this),
                Option.OK, this::rootSignUp,
                Option.CANCEL, null);
    }

    private void rootSignUp() {
        signUp(rootSignUpPanel.getUser(), rootSignUpPanel.getErrorMessage());
    }

    private void showDataImportDialog() {
        showDialog(() -> dataImportDialog.showDialog(this),
                Option.OK, this::loadDataInfile,
                Option.ERROR, () -> showErrorDialog(this, "请选择需要导入的文件", "导入失败"));
    }

    private void loadDataInfile() {
        try {
            db.loadDataInfile(dataImportDialog.getSelectedFile().getPath(),
                              dataImportDialog.getSelectedTableName());
            JOptionPane.showMessageDialog(MainUI.this, "数据导入成功");
        }
        catch (RuntimeException e) {
            showErrorDialog(this, e.getMessage(), "数据导入失败");
        }
    }

    private void initializeConfiguration() {
        configDataDisplayPanel();
        configDataImport();
        configRootSignUp();
        setTitle("当前登录用户：" + welcomeDialog.getSignInUser().account);
        setVisible(true);
        pack();
    }

    private void configDataDisplayPanel() {
        dataDisplayPanel.reset();
        dataDisplayPanel.configureMainFilter(db.getQueryTypeFilterMap(), this::afterMainFilterSelected);
        dataDisplayPanel.configureQueryAction(this::afterQuery);
        dataDisplayPanel.configureDeleteAction(this::afterDelete);
        dataDisplayPanel.notifyListeners();
    }

    private void afterMainFilterSelected(@NonNull FilterWrapper<String> filter) {
        dataDisplayPanel.configureFilters(db.getFilterMap(filter.getValue()));
        pack();
    }

    private void afterQuery(@NonNull FilterWrapper<String> filter) {
        dataDisplayPanel.display(db.queryWithFilter(filter.getValue(), dataDisplayPanel.getSelectedFilter())
                                   .orElse(new DefaultTableModel()));
    }

    private void afterDelete() {
        try {
            db.deleteFromWheres(dataDisplayPanel.getTableName(), dataDisplayPanel.getSelectedRows());
            dataDisplayPanel.updateStatus(dataDisplayPanel.getMessageAfterDeletion());
        }
        catch (Exception e) {
            showErrorDialog(this, e.getMessage(), "删除失败");
            dataDisplayPanel.updateStatus("删除失败: 请检查条件或重试");
        }
    }

    private void configDataImport() {
        var authorized = db.hasPrivilegeOfImportingData(welcomeDialog.getSignInUser());
        getJMenuBar().getMenu(1).getItem(1).setEnabled(authorized);
        if (authorized && dataImportDialog == null) {
            dataImportDialog = new DataImportDialog();
            dataImportDialog.populateTableNameComboBox(db.getTableNames());
        }
    }

    private void configRootSignUp() {
        var authorized = db.hasPrivilegeOfRegisteringUser(welcomeDialog.getSignInUser());
        getJMenuBar().getMenu(0).getItem(1).setEnabled(authorized);
        if (authorized && rootSignUpPanel == null) {
            rootSignUpPanel = new RootSignUpPanel();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}
