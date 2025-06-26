package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

class Income {
    private double amount;
    private String description;
    private LocalDate date;

    public Income(double amount, String description, LocalDate date) {
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return String.format("Доход: %.2f, Описание: %s, Дата: %s", amount, description, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}

class Expense {
    private double amount;
    private String description;
    private String category;
    private LocalDate date;

    public Expense(double amount, String description, String category, LocalDate date) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
    }

    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }

    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(LocalDate date) { this.date = date; }

    @Override
    public String toString() {
        return String.format("Расход: %.2f, Описание: %s, Категория: %s, Дата: %s", amount, description, category, date.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}

class RecurringIncome extends Income {
    private String frequency;
    private int repetitions;

    public RecurringIncome(double amount, String description, LocalDate startDate, String frequency, int repetitions) {
        super(amount, description, startDate);
        this.frequency = frequency;
        this.repetitions = repetitions;
    }

    public String getFrequency() { return frequency; }
    public int getRepetitions() { return repetitions; }

    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    @Override
    public String toString() {
        return String.format("Повторяющийся доход: %.2f, Описание: %s, Нач. дата: %s, Частота: %s, Повторений: %d",
                getAmount(), getDescription(), getDate().format(DateTimeFormatter.ISO_LOCAL_DATE), frequency, repetitions);
    }
}

class RecurringExpense extends Expense {
    private String frequency;
    private int repetitions;

    public RecurringExpense(double amount, String description, String category, LocalDate startDate, String frequency, int repetitions) {
        super(amount, description, category, startDate);
        this.frequency = frequency;
        this.repetitions = repetitions;
    }

    public String getFrequency() { return frequency; }
    public int getRepetitions() { return repetitions; }

    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setRepetitions(int repetitions) { this.repetitions = repetitions; }

    @Override
    public String toString() {
        return String.format("Повторяющийся расход: %.2f, Описание: %s, Категория: %s, Нач. дата: %s, Частота: %s, Повторений: %d",
                getAmount(), getDescription(), getCategory(), getDate().format(DateTimeFormatter.ISO_LOCAL_DATE), frequency, repetitions);
    }
}

class FinancialGoal {
    private String name;
    private double targetAmount;
    private double currentAmount;
    private LocalDate dueDate;

    public FinancialGoal(String name, double targetAmount, double currentAmount, LocalDate dueDate) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.dueDate = dueDate;
    }

    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public LocalDate getDueDate() { return dueDate; }

    public void setName(String name) { this.name = name; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public double getProgressPercentage() {
        return (targetAmount > 0) ? (currentAmount / targetAmount) * 100 : 0;
    }

    public double getRemainingAmount() {
        return targetAmount - currentAmount;
    }

    @Override
    public String toString() {
        return String.format("Цель: %s, Целевая сумма: %.2f, Текущая сумма: %.2f, Дата завершения: %s",
                name, targetAmount, currentAmount, dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}

public class BudgetApp extends JFrame {
    private static final String DATA_FILE = "budget_data.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private List<Income> incomes = new ArrayList<>();
    private List<Expense> expenses = new ArrayList<>();
    private Map<String, Double> budget = new HashMap<>();
    private List<RecurringIncome> recurringIncomes = new ArrayList<>();
    private List<RecurringExpense> recurringExpenses = new ArrayList<>();
    private List<FinancialGoal> goals = new ArrayList<>();
    private Set<String> categories = new HashSet<>(Arrays.asList("Еда", "Транспорт", "Развлечения", "Жилье", "Зарплата", "Подарки"));

    private JLabel statusLabel;
    private JTabbedPane tabbedPane;

    private JTextField incomeAmountField, incomeDescriptionField;
    private JCheckBox recurringIncomeCheckBox;
    private JComboBox<String> incomeFrequencyComboBox;
    private JTextField incomeRepetitionsField;
    private DatePicker incomeDatePicker;

    private JTextField expenseAmountField, expenseDescriptionField;
    private JComboBox<String> expenseCategoryComboBox;
    private JCheckBox recurringExpenseCheckBox;
    private JComboBox<String> expenseFrequencyComboBox;
    private JTextField expenseRepetitionsField;
    private DatePicker expenseDatePicker;

    private JComboBox<String> budgetCategoryComboBox;
    private JTextField budgetAmountField;

    private JTextField editIndexField;
    private JRadioButton incomeEditRadio, expenseEditRadio, recurringIncomeEditRadio, recurringExpenseEditRadio;
    private ButtonGroup editTypeGroup;
    private JButton saveEditButton;
    private Map<String, Object> editingRecordInfo = null;

    private JTextArea reportTextArea;
    private JTextField filterDescriptionField, filterCategoryField, filterMonthField, filterYearField;

    private JTextField goalNameField, goalTargetAmountField, goalCurrentAmountField;
    private DatePicker goalDueDateField;
    private JTable goalsTable;
    private DefaultTableModel goalsTableModel;

    public BudgetApp() {
        setTitle("Инструмент для бюджетирования и прогнозирования");
        setSize(1200, 850);
        setMinimumSize(new Dimension(900, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.err.println("Не удалось установить Nimbus LookAndFeel: " + e.getMessage());
        }

        initComponents();
        loadData();
        updateCategoriesComboBoxes();
        refreshUI();
    }

    private void initComponents() {
        statusLabel = new JLabel("Готов");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel.setPreferredSize(new Dimension(getWidth(), 25));
        add(statusLabel, BorderLayout.SOUTH);

        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        setupDataManagementTab();
        setupReportsTab();
        setupDashboardTab();
        setupGoalsTab();
        setupCategoryManagementTab();
    }

    private void setupDataManagementTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        tabbedPane.addTab("Управление данными", panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel incomePanel = createTitledPanel("Добавить доход");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.5;
        panel.add(incomePanel, gbc);
        setupIncomePanel(incomePanel);

        JPanel expensePanel = createTitledPanel("Добавить расход");
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(expensePanel, gbc);
        setupExpensePanel(expensePanel);

        JPanel budgetPanel = createTitledPanel("Установить/обновить бюджет");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weighty = 0.25;
        panel.add(budgetPanel, gbc);
        setupBudgetPanel(budgetPanel);

        JPanel editDeletePanel = createTitledPanel("Редактирование/удаление записей");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.5;
        gbc.weighty = 1;
        panel.add(editDeletePanel, gbc);
        setupEditDeletePanel(editDeletePanel);
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return panel;
    }

    private void setupIncomePanel(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Сумма:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; incomeAmountField = createPlaceholderTextField("Введите сумму"); panel.add(incomeAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; incomeDescriptionField = createPlaceholderTextField("Например: Зарплата, подарок"); panel.add(incomeDescriptionField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panel.add(new JLabel("Дата (ГГГГ-ММ-ДД):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; incomeDatePicker = new DatePicker(); panel.add(incomeDatePicker, gbc);
        incomeDatePicker.setDate(LocalDate.now());

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        recurringIncomeCheckBox = new JCheckBox("Повторяющаяся транзакция");
        recurringIncomeCheckBox.addActionListener(e -> toggleRecurringIncomeOptions());
        panel.add(recurringIncomeCheckBox, gbc);

        JPanel recurringOptionsPanel = new JPanel(new GridBagLayout());
        recurringOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(recurringOptionsPanel, gbc);

        GridBagConstraints subGbc = new GridBagConstraints();
        subGbc.insets = new Insets(2, 2, 2, 2);
        subGbc.fill = GridBagConstraints.HORIZONTAL;

        subGbc.gridx = 0; subGbc.gridy = 0; recurringOptionsPanel.add(new JLabel("Частота:"), subGbc);
        subGbc.gridx = 1; subGbc.weightx = 1; incomeFrequencyComboBox = new JComboBox<>(new String[]{"Ежемесячно"}); recurringOptionsPanel.add(incomeFrequencyComboBox, subGbc);

        subGbc.gridx = 0; subGbc.gridy = 1; subGbc.weightx = 0; recurringOptionsPanel.add(new JLabel("Повторений:"), subGbc);
        subGbc.gridx = 1; subGbc.weightx = 1; incomeRepetitionsField = createPlaceholderTextField("Количество повторений"); recurringOptionsPanel.add(incomeRepetitionsField, subGbc);

        toggleRecurringIncomeOptions();

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton addIncomeButton = new JButton("Добавить доход");
        addIncomeButton.addActionListener(e -> addIncome());
        panel.add(addIncomeButton, gbc);
    }

    private void setupExpensePanel(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Сумма:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; expenseAmountField = createPlaceholderTextField("Введите сумму"); panel.add(expenseAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Описание:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; expenseDescriptionField = createPlaceholderTextField("Например: Продукты, такси"); panel.add(expenseDescriptionField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; panel.add(new JLabel("Категория:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; expenseCategoryComboBox = new JComboBox<>(); panel.add(expenseCategoryComboBox, gbc);


        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; panel.add(new JLabel("Дата (ГГГГ-ММ-ДД):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; expenseDatePicker = new DatePicker(); panel.add(expenseDatePicker, gbc);
        expenseDatePicker.setDate(LocalDate.now());

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        recurringExpenseCheckBox = new JCheckBox("Повторяющаяся транзакция");
        recurringExpenseCheckBox.addActionListener(e -> toggleRecurringExpenseOptions());
        panel.add(recurringExpenseCheckBox, gbc);

        JPanel recurringOptionsPanel = new JPanel(new GridBagLayout());
        recurringOptionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(recurringOptionsPanel, gbc);

        GridBagConstraints subGbc = new GridBagConstraints();
        subGbc.insets = new Insets(2, 2, 2, 2);
        subGbc.fill = GridBagConstraints.HORIZONTAL;

        subGbc.gridx = 0; subGbc.gridy = 0; recurringOptionsPanel.add(new JLabel("Частота:"), subGbc);
        subGbc.gridx = 1; subGbc.weightx = 1; expenseFrequencyComboBox = new JComboBox<>(new String[]{"Ежемесячно"}); recurringOptionsPanel.add(expenseFrequencyComboBox, subGbc);

        subGbc.gridx = 0; subGbc.gridy = 1; subGbc.weightx = 0; recurringOptionsPanel.add(new JLabel("Повторений:"), subGbc);
        subGbc.gridx = 1; subGbc.weightx = 1; expenseRepetitionsField = createPlaceholderTextField("Количество повторений"); recurringOptionsPanel.add(expenseRepetitionsField, subGbc);

        toggleRecurringExpenseOptions();

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton addExpenseButton = new JButton("Добавить расход");
        addExpenseButton.addActionListener(e -> addExpense());
        panel.add(addExpenseButton, gbc);
    }

    private void setupBudgetPanel(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Категория:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; budgetCategoryComboBox = new JComboBox<>(); panel.add(budgetCategoryComboBox, gbc);


        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; panel.add(new JLabel("Сумма бюджета:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; budgetAmountField = createPlaceholderTextField("Например: 500.00"); panel.add(budgetAmountField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton setBudgetButton = new JButton("Установить бюджет");
        setBudgetButton.addActionListener(e -> setBudget());
        panel.add(setBudgetButton, gbc);
    }

    private void setupEditDeletePanel(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Индекс записи (из отчета):"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; editIndexField = createPlaceholderTextField("Введите индекс"); panel.add(editIndexField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Тип записи:"), gbc);
        editTypeGroup = new ButtonGroup();
        incomeEditRadio = new JRadioButton("Доход");
        expenseEditRadio = new JRadioButton("Расход");
        recurringIncomeEditRadio = new JRadioButton("Повторяющийся доход");
        recurringExpenseEditRadio = new JRadioButton("Повторяющийся расход");

        editTypeGroup.add(incomeEditRadio);
        editTypeGroup.add(expenseEditRadio);
        editTypeGroup.add(recurringIncomeEditRadio);
        editTypeGroup.add(recurringExpenseEditRadio);

        expenseEditRadio.setSelected(true);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(incomeEditRadio, gbc);
        gbc.gridy = 4; panel.add(expenseEditRadio, gbc);
        gbc.gridy = 5; panel.add(recurringIncomeEditRadio, gbc);
        gbc.gridy = 6; panel.add(recurringExpenseEditRadio, gbc);

        gbc.gridy = 7;
        JButton loadEditButton = new JButton("Загрузить для редактирования");
        loadEditButton.addActionListener(e -> loadEntryForEdit());
        panel.add(loadEditButton, gbc);

        gbc.gridy = 8;
        saveEditButton = new JButton("Сохранить изменения");
        saveEditButton.addActionListener(e -> saveEditedEntry());
        saveEditButton.setEnabled(false);
        panel.add(saveEditButton, gbc);

        gbc.gridy = 9;
        JButton deleteButton = new JButton("Удалить запись");
        deleteButton.addActionListener(e -> deleteEntry());
        panel.add(deleteButton, gbc);

        gbc.gridy = 10;
        JButton clearAllButton = new JButton("Очистить все данные");
        clearAllButton.setBackground(new Color(255, 82, 82));
        clearAllButton.setForeground(Color.WHITE);
        clearAllButton.setOpaque(true);
        clearAllButton.setBorderPainted(false);
        clearAllButton.addActionListener(e -> clearAllData());
        panel.add(clearAllButton, gbc);

        gbc.gridy = 11;
        JButton exportCsvButton = new JButton("Экспорт в CSV");
        exportCsvButton.addActionListener(e -> exportToCsv());
        panel.add(exportCsvButton, gbc);
    }

    private void setupReportsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        tabbedPane.addTab("Отчеты и аналитика", panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel filterPanel = createTitledPanel("Фильтры отчета");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        panel.add(filterPanel, gbc);

        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.insets = new Insets(2, 2, 2, 2);
        filterGbc.fill = GridBagConstraints.HORIZONTAL;

        filterGbc.gridx = 0; filterGbc.gridy = 0; filterPanel.add(new JLabel("Поиск по описанию:"), filterGbc);
        filterGbc.gridx = 1; filterGbc.weightx = 1; filterDescriptionField = createPlaceholderTextField("Ключевое слово"); filterPanel.add(filterDescriptionField, filterGbc);

        filterGbc.gridx = 2; filterGbc.weightx = 0; filterPanel.add(new JLabel("Категория:"), filterGbc);
        filterGbc.gridx = 3; filterGbc.weightx = 1; filterCategoryField = createPlaceholderTextField("Например: еда"); filterPanel.add(filterCategoryField, filterGbc);

        filterGbc.gridx = 0; filterGbc.gridy = 1; filterGbc.weightx = 0; filterPanel.add(new JLabel("Месяц (ГГГГ-ММ):"), filterGbc);
        filterGbc.gridx = 1; filterGbc.weightx = 1; filterMonthField = createPlaceholderTextField("Например: 2025-06"); filterPanel.add(filterMonthField, filterGbc);

        filterGbc.gridx = 2; filterGbc.weightx = 0; filterPanel.add(new JLabel("Год (ГГГГ):"), filterGbc);
        filterGbc.gridx = 3; filterGbc.weightx = 1; filterYearField = createPlaceholderTextField("Например: 2025"); filterPanel.add(filterYearField, filterGbc);

        filterGbc.gridx = 0; filterGbc.gridy = 2; filterGbc.gridwidth = 2;
        JButton applyFilterButton = new JButton("Применить фильтры");
        applyFilterButton.addActionListener(e -> applyFilters());
        filterPanel.add(applyFilterButton, filterGbc);

        filterGbc.gridx = 2; filterGbc.gridy = 2; filterGbc.gridwidth = 2;
        JButton resetFilterButton = new JButton("Сбросить фильтры");
        resetFilterButton.addActionListener(e -> resetFilters());
        filterPanel.add(resetFilterButton, filterGbc);

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 1; gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);
    }

    private void setupDashboardTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        tabbedPane.addTab("Панель инструментов", panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel chartPanel1 = createTitledPanel("Расходы по категориям");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 0.5;
        panel.add(chartPanel1, gbc);

        JPanel chartPanel2 = createTitledPanel("Динамика баланса (по месяцам)");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 0.5;
        panel.add(chartPanel2, gbc);

        JPanel summaryPanel = createTitledPanel("Ежемесячная/ежегодная сводка");
        gbc.gridx = 0; gbc.gridy = 2; gbc.weighty = 0.2;
        panel.add(summaryPanel, gbc);
        JTextArea monthlySummaryText = new JTextArea();
        monthlySummaryText.setEditable(false);
        monthlySummaryText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        summaryPanel.add(new JScrollPane(monthlySummaryText));

        JPanel forecastPanel = createTitledPanel("Прогноз баланса (на 6 месяцев)");
        gbc.gridx = 0; gbc.gridy = 3; gbc.weighty = 0.2;
        panel.add(forecastPanel, gbc);
        JTextArea forecastText = new JTextArea();
        forecastText.setEditable(false);
        forecastText.setFont(new Font("Monospaced", Font.PLAIN, 12));
        forecastPanel.add(new JScrollPane(forecastText));

        JButton updateChartsButton = new JButton("Обновить графики и прогноз");
        updateChartsButton.addActionListener(e -> plotCharts());
        gbc.gridx = 0; gbc.gridy = 4; gbc.weighty = 0;
        panel.add(updateChartsButton, gbc);
    }

    private void setupGoalsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        tabbedPane.addTab("Финансовые цели", panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        JPanel inputPanel = createTitledPanel("Добавить/Редактировать цель");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 0.3;
        panel.add(inputPanel, gbc);

        GridBagConstraints inputGbc = new GridBagConstraints();
        inputGbc.insets = new Insets(2, 2, 2, 2);
        inputGbc.fill = GridBagConstraints.HORIZONTAL;

        inputGbc.gridx = 0; inputGbc.gridy = 0; inputPanel.add(new JLabel("Название цели:"), inputGbc);
        inputGbc.gridx = 1; inputGbc.weightx = 1; goalNameField = createPlaceholderTextField("Название цели"); inputPanel.add(goalNameField, inputGbc);

        inputGbc.gridx = 0; inputGbc.gridy = 1; inputPanel.add(new JLabel("Целевая сумма:"), inputGbc);
        inputGbc.gridx = 1; inputGbc.weightx = 1; goalTargetAmountField = createPlaceholderTextField("Целевая сумма"); inputPanel.add(goalTargetAmountField, inputGbc);

        inputGbc.gridx = 0; inputGbc.gridy = 2; inputPanel.add(new JLabel("Текущая сумма:"), inputGbc);
        inputGbc.gridx = 1; inputGbc.weightx = 1; goalCurrentAmountField = createPlaceholderTextField("Текущая сумма"); inputPanel.add(goalCurrentAmountField, inputGbc);

        inputGbc.gridx = 0; inputGbc.gridy = 3; inputPanel.add(new JLabel("Дата завершения (ГГГГ-ММ-ДД):"), inputGbc);
        inputGbc.gridx = 1; inputGbc.weightx = 1; goalDueDateField = new DatePicker(); inputPanel.add(goalDueDateField, inputGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addGoalButton = new JButton("Добавить цель");
        addGoalButton.addActionListener(e -> addGoal());
        buttonPanel.add(addGoalButton);

        JButton updateGoalButton = new JButton("Обновить цель");
        updateGoalButton.addActionListener(e -> updateGoal());
        buttonPanel.add(updateGoalButton);

        JButton deleteGoalButton = new JButton("Удалить цель");
        deleteGoalButton.addActionListener(e -> deleteGoal());
        deleteGoalButton.setBackground(new Color(255, 82, 82));
        deleteGoalButton.setForeground(Color.WHITE);
        deleteGoalButton.setOpaque(true);
        deleteGoalButton.setBorderPainted(false);
        buttonPanel.add(deleteGoalButton);

        inputGbc.gridx = 0; inputGbc.gridy = 4; inputGbc.gridwidth = 2; inputPanel.add(buttonPanel, inputGbc);


        String[] columnNames = {"Название", "Цель", "Текущая", "Прогресс", "Осталось", "Крайний срок"};
        goalsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        goalsTable = new JTable(goalsTableModel);
        goalsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        goalsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && goalsTable.getSelectedRow() != -1) {
                loadGoalForEdit(goalsTable.getSelectedRow());
            }
        });

        JScrollPane scrollPane = new JScrollPane(goalsTable);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weighty = 0.7;
        panel.add(scrollPane, gbc);
    }

    private void setupCategoryManagementTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        tabbedPane.addTab("Управление категориями", panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel addCategoryPanel = createTitledPanel("Добавить новую категорию");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1;
        panel.add(addCategoryPanel, gbc);

        JTextField newCategoryField = createPlaceholderTextField("Название новой категории");
        JButton addCategoryButton = new JButton("Добавить категорию");
        addCategoryButton.addActionListener(e -> {
            String newCat = newCategoryField.getText().trim();
            if (!newCat.isEmpty() && !categories.contains(newCat.toLowerCase())) {
                categories.add(newCat.toLowerCase());
                updateCategoriesComboBoxes();
                newCategoryField.setText("");
                updateStatus("Категория '" + newCat + "' успешно добавлена.");
                saveData();
            } else if (newCat.isEmpty()) {
                updateStatus("Пожалуйста, введите название категории.", true);
            } else {
                updateStatus("Категория '" + newCat + "' уже существует.", true);
            }
        });

        GridBagConstraints addCatGbc = new GridBagConstraints();
        addCatGbc.insets = new Insets(2, 2, 2, 2);
        addCatGbc.fill = GridBagConstraints.HORIZONTAL;
        addCatGbc.gridx = 0; addCatGbc.gridy = 0; addCatGbc.weightx = 1; addCategoryPanel.add(newCategoryField, addCatGbc);
        addCatGbc.gridx = 1; addCatGbc.weightx = 0; addCategoryPanel.add(addCategoryButton, addCatGbc);

        JPanel viewDeleteCategoryPanel = createTitledPanel("Просмотр и удаление категорий");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1; gbc.weighty = 1;
        panel.add(viewDeleteCategoryPanel, gbc);

        DefaultListModel<String> categoryListModel = new DefaultListModel<>();
        JList<String> categoryJList = new JList<>(categoryListModel);
        JScrollPane categoryScrollPane = new JScrollPane(categoryJList);
        JButton deleteSelectedCategoryButton = new JButton("Удалить выбранную категорию");
        deleteSelectedCategoryButton.setBackground(new Color(255, 82, 82));
        deleteSelectedCategoryButton.setForeground(Color.WHITE);
        deleteSelectedCategoryButton.setOpaque(true);
        deleteSelectedCategoryButton.setBorderPainted(false);
        deleteSelectedCategoryButton.addActionListener(e -> {
            String selectedCat = categoryJList.getSelectedValue();
            if (selectedCat != null) {
                if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить категорию '" + selectedCat + "'?\nЭто не повлияет на уже существующие записи, но категория больше не будет доступна для выбора.", "Подтвердить удаление", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    categories.remove(selectedCat.toLowerCase());
                    updateCategoriesComboBoxes();
                    updateStatus("Категория '" + selectedCat + "' успешно удалена.");
                    saveData();
                }
            } else {
                updateStatus("Пожалуйста, выберите категорию для удаления.", true);
            }
        });

        viewDeleteCategoryPanel.setLayout(new BorderLayout());
        viewDeleteCategoryPanel.add(categoryScrollPane, BorderLayout.CENTER);
        viewDeleteCategoryPanel.add(deleteSelectedCategoryButton, BorderLayout.SOUTH);

        updateCategoryListModel(categoryListModel);
    }

    private void updateCategoryListModel(DefaultListModel<String> model) {
        model.clear();
        categories.stream().sorted(String.CASE_INSENSITIVE_ORDER).forEach(model::addElement);
    }


    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField textField = new JTextField();
        applyPlaceholderStyle(textField, placeholder);
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    applyPlaceholderStyle(textField, placeholder);
                }
            }
        });
        return textField;
    }

    private void applyPlaceholderStyle(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
    }

    private void toggleRecurringIncomeOptions() {
        boolean selected = recurringIncomeCheckBox.isSelected();
        incomeFrequencyComboBox.setEnabled(selected);
        incomeRepetitionsField.setEnabled(selected);
        if (!selected) {
            incomeRepetitionsField.setText("");
            applyPlaceholderStyle(incomeRepetitionsField, "Количество повторений");
        }
    }

    private void toggleRecurringExpenseOptions() {
        boolean selected = recurringExpenseCheckBox.isSelected();
        expenseFrequencyComboBox.setEnabled(selected);
        expenseRepetitionsField.setEnabled(selected);
        if (!selected) {
            expenseRepetitionsField.setText("");
            applyPlaceholderStyle(expenseRepetitionsField, "Количество повторений");
        }
    }

    private void updateStatus(String message) {
        updateStatus(message, false);
    }

    private void updateStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setForeground(isError ? Color.RED : Color.BLACK);
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> statusLabel.setText("Готов"));
        timer.setRepeats(false);
        timer.start();
    }

    private void clearEntries() {
        incomeAmountField.setText(""); applyPlaceholderStyle(incomeAmountField, "Введите сумму");
        incomeDescriptionField.setText(""); applyPlaceholderStyle(incomeDescriptionField, "Например: Зарплата, подарок");
        incomeDatePicker.setDate(LocalDate.now());
        recurringIncomeCheckBox.setSelected(false); toggleRecurringIncomeOptions();
        incomeRepetitionsField.setText(""); applyPlaceholderStyle(incomeRepetitionsField, "Количество повторений");

        expenseAmountField.setText(""); applyPlaceholderStyle(expenseAmountField, "Введите сумму");
        expenseDescriptionField.setText(""); applyPlaceholderStyle(expenseDescriptionField, "Например: Продукты, такси");
        expenseCategoryComboBox.setSelectedIndex(0);
        expenseDatePicker.setDate(LocalDate.now());
        recurringExpenseCheckBox.setSelected(false); toggleRecurringExpenseOptions();
        expenseRepetitionsField.setText(""); applyPlaceholderStyle(expenseRepetitionsField, "Количество повторений");

        budgetCategoryComboBox.setSelectedIndex(0);
        budgetAmountField.setText(""); applyPlaceholderStyle(budgetAmountField, "Например: 500.00");

        editIndexField.setText(""); applyPlaceholderStyle(editIndexField, "Введите индекс");
        editTypeGroup.clearSelection();
        expenseEditRadio.setSelected(true);

        editingRecordInfo = null;
        saveEditButton.setEnabled(false);

        goalNameField.setText(""); applyPlaceholderStyle(goalNameField, "Название цели");
        goalTargetAmountField.setText(""); applyPlaceholderStyle(goalTargetAmountField, "Целевая сумма");
        goalCurrentAmountField.setText(""); applyPlaceholderStyle(goalCurrentAmountField, "Текущая сумма");
        goalDueDateField.setDate(null);
        goalsTable.clearSelection();

        updateGoalsDisplay();
    }

    private void refreshUI() {
        generateReport(null, null, null, null);
        plotCharts();
        updateGoalsDisplay();
        Component component = tabbedPane.getComponentAt(4);
        if (component instanceof JPanel) {
            JPanel categoryPanel = (JPanel) component;
            for (Component subComponent : categoryPanel.getComponents()) {
                if (subComponent instanceof JPanel) {
                    JPanel titledSubPanel = (JPanel) subComponent;
                    if (titledSubPanel.getBorder() instanceof javax.swing.border.TitledBorder) {
                        javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) titledSubPanel.getBorder();
                        if ("Просмотр и удаление категорий".equals(border.getTitle())) {
                            for (Component scrollComp : titledSubPanel.getComponents()) {
                                if (scrollComp instanceof JScrollPane) {
                                    JScrollPane scrollPane = (JScrollPane) scrollComp;
                                    Component view = scrollPane.getViewport().getView();
                                    if (view instanceof JList) {
                                        JList<?> jList = (JList<?>) view;
                                        if (jList.getModel() instanceof DefaultListModel) {
                                            updateCategoryListModel((DefaultListModel<String>) jList.getModel());
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private void addIncome() {
        try {
            double amount = Double.parseDouble(incomeAmountField.getText().replace(",", "."));
            String description = incomeDescriptionField.getText();
            LocalDate date = incomeDatePicker.getDate();

            if (amount <= 0) {
                updateStatus("Сумма дохода должна быть положительным числом.", true);
                return;
            }
            if (description.isEmpty() || description.equals("Например: Зарплата, подарок")) {
                updateStatus("Пожалуйста, введите описание дохода.", true);
                return;
            }
            if (date == null) {
                updateStatus("Пожалуйста, выберите дату дохода.", true);
                return;
            }

            if (recurringIncomeCheckBox.isSelected()) {
                String frequency = (String) incomeFrequencyComboBox.getSelectedItem();
                int repetitions = Integer.parseInt(incomeRepetitionsField.getText());
                if (repetitions <= 0) {
                    updateStatus("Для повторяющихся доходов укажите корректное количество повторений (целое положительное число).", true);
                    return;
                }
                recurringIncomes.add(new RecurringIncome(amount, description, date, frequency, repetitions));
                updateStatus("Повторяющийся доход успешно добавлен.");
            } else {
                incomes.add(new Income(amount, description, date));
                updateStatus("Доход успешно добавлен.");
            }
            saveData();
            refreshUI();
            clearEntries();
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы или повторений. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при добавлении дохода: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void addExpense() {
        try {
            double amount = Double.parseDouble(expenseAmountField.getText().replace(",", "."));
            String description = expenseDescriptionField.getText();
            String category = (String) expenseCategoryComboBox.getSelectedItem();
            LocalDate date = expenseDatePicker.getDate();

            if (amount <= 0) {
                updateStatus("Сумма расхода должна быть положительным числом.", true);
                return;
            }
            if (description.isEmpty() || description.equals("Например: Продукты, такси")) {
                updateStatus("Пожалуйста, введите описание расхода.", true);
                return;
            }
            if (category == null || category.isEmpty()) {
                updateStatus("Пожалуйста, выберите категорию расхода.", true);
                return;
            }
            if (date == null) {
                updateStatus("Пожалуйста, выберите дату расхода.", true);
                return;
            }

            if (recurringExpenseCheckBox.isSelected()) {
                String frequency = (String) expenseFrequencyComboBox.getSelectedItem();
                int repetitions = Integer.parseInt(expenseRepetitionsField.getText());
                if (repetitions <= 0) {
                    updateStatus("Для повторяющихся расходов укажите корректное количество повторений (целое положительное число).", true);
                    return;
                }
                recurringExpenses.add(new RecurringExpense(amount, description, category, date, frequency, repetitions));
                updateStatus("Повторяющийся расход успешно добавлен.");
            } else {
                expenses.add(new Expense(amount, description, category, date));
                updateStatus("Расход успешно добавлен.");
            }
            saveData();
            refreshUI();
            clearEntries();
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы или повторений. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при добавлении расхода: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void setBudget() {
        try {
            String category = (String) budgetCategoryComboBox.getSelectedItem();
            double amount = Double.parseDouble(budgetAmountField.getText().replace(",", "."));

            if (category == null || category.isEmpty()) {
                updateStatus("Пожалуйста, выберите категорию бюджета.", true);
                return;
            }
            if (amount < 0) {
                updateStatus("Сумма бюджета не может быть отрицательной.", true);
                return;
            }

            budget.put(category, amount);
            saveData();
            refreshUI();
            clearEntries();
            updateStatus("Бюджет для категории '" + category + "' установлен на " + String.format("%.2f", amount) + ".");
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы бюджета. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при установке бюджета: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void loadEntryForEdit() {
        try {
            int index = Integer.parseInt(editIndexField.getText()) - 1;
            String type = getSelectedEditType();

            Object record = null;
            String listName = "";

            if (type.equals("income")) {
                if (index >= 0 && index < incomes.size()) record = incomes.get(index);
                listName = "доходов";
            } else if (type.equals("expense")) {
                if (index >= 0 && index < expenses.size()) record = expenses.get(index);
                listName = "расходов";
            } else if (type.equals("recurring_income")) {
                if (index >= 0 && index < recurringIncomes.size()) record = recurringIncomes.get(index);
                listName = "повторяющихся доходов";
            } else if (type.equals("recurring_expense")) {
                if (index >= 0 && index < recurringExpenses.size()) record = recurringExpenses.get(index);
                listName = "повторяющихся расходов";
            }

            if (record == null) {
                updateStatus("Неверный индекс записи " + listName + ".", true);
                return;
            }

            editingRecordInfo = new HashMap<>();
            editingRecordInfo.put("type", type);
            editingRecordInfo.put("index", index);

            clearEntries();

            if (record instanceof Income) {
                Income inc = (Income) record;
                incomeAmountField.setText(String.valueOf(inc.getAmount()));
                incomeDescriptionField.setText(inc.getDescription());
                incomeDatePicker.setDate(inc.getDate());
                applyDefaultStyle(incomeAmountField);
                applyDefaultStyle(incomeDescriptionField);
            } else if (record instanceof Expense) {
                Expense exp = (Expense) record;
                expenseAmountField.setText(String.valueOf(exp.getAmount()));
                expenseDescriptionField.setText(exp.getDescription());
                expenseCategoryComboBox.setSelectedItem(exp.getCategory());
                expenseDatePicker.setDate(exp.getDate());
                applyDefaultStyle(expenseAmountField);
                applyDefaultStyle(expenseDescriptionField);
            } else if (record instanceof RecurringIncome) {
                RecurringIncome rInc = (RecurringIncome) record;
                recurringIncomeCheckBox.setSelected(true);
                toggleRecurringIncomeOptions();
                incomeAmountField.setText(String.valueOf(rInc.getAmount()));
                incomeDescriptionField.setText(rInc.getDescription());
                incomeDatePicker.setDate(rInc.getDate());
                incomeFrequencyComboBox.setSelectedItem(rInc.getFrequency());
                incomeRepetitionsField.setText(String.valueOf(rInc.getRepetitions()));
                applyDefaultStyle(incomeAmountField);
                applyDefaultStyle(incomeDescriptionField);
                applyDefaultStyle(incomeRepetitionsField);
            } else if (record instanceof RecurringExpense) {
                RecurringExpense rExp = (RecurringExpense) record;
                recurringExpenseCheckBox.setSelected(true);
                toggleRecurringExpenseOptions();
                expenseAmountField.setText(String.valueOf(rExp.getAmount()));
                expenseDescriptionField.setText(rExp.getDescription());
                expenseCategoryComboBox.setSelectedItem(rExp.getCategory());
                expenseDatePicker.setDate(rExp.getDate());
                expenseFrequencyComboBox.setSelectedItem(rExp.getFrequency());
                expenseRepetitionsField.setText(String.valueOf(rExp.getRepetitions()));
                applyDefaultStyle(expenseAmountField);
                applyDefaultStyle(expenseDescriptionField);
                applyDefaultStyle(expenseRepetitionsField);
            }

            saveEditButton.setEnabled(true);
            updateStatus("Запись №" + (index + 1) + " (" + listName + ") загружена для редактирования.");
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат индекса. Используйте целые числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при загрузке записи для редактирования: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void applyDefaultStyle(JTextField field) {
        field.setForeground(Color.BLACK);
    }


    private String getSelectedEditType() {
        if (incomeEditRadio.isSelected()) return "income";
        if (expenseEditRadio.isSelected()) return "expense";
        if (recurringIncomeEditRadio.isSelected()) return "recurring_income";
        if (recurringExpenseEditRadio.isSelected()) return "recurring_expense";
        return "";
    }

    private void saveEditedEntry() {
        if (editingRecordInfo == null) {
            updateStatus("Нет записи для сохранения. Сначала загрузите запись для редактирования.", true);
            return;
        }

        String type = (String) editingRecordInfo.get("type");
        int index = (int) editingRecordInfo.get("index");

        try {
            if (type.equals("income")) {
                double newAmount = Double.parseDouble(incomeAmountField.getText().replace(",", "."));
                String newDescription = incomeDescriptionField.getText();
                LocalDate newDate = incomeDatePicker.getDate();

                if (newAmount <= 0 || newDescription.isEmpty() || newDate == null) {
                    updateStatus("Заполните все поля дохода для сохранения изменений.", true);
                    return;
                }
                incomes.get(index).setAmount(newAmount);
                incomes.get(index).setDescription(newDescription);
                incomes.get(index).setDate(newDate);
            } else if (type.equals("expense")) {
                double newAmount = Double.parseDouble(expenseAmountField.getText().replace(",", "."));
                String newDescription = expenseDescriptionField.getText();
                String newCategory = (String) expenseCategoryComboBox.getSelectedItem();
                LocalDate newDate = expenseDatePicker.getDate();

                if (newAmount <= 0 || newDescription.isEmpty() || newCategory == null || newCategory.isEmpty() || newDate == null) {
                    updateStatus("Заполните все поля расхода для сохранения изменений.", true);
                    return;
                }
                expenses.get(index).setAmount(newAmount);
                expenses.get(index).setDescription(newDescription);
                expenses.get(index).setCategory(newCategory);
                expenses.get(index).setDate(newDate);
            } else if (type.equals("recurring_income")) {
                double newAmount = Double.parseDouble(incomeAmountField.getText().replace(",", "."));
                String newDescription = incomeDescriptionField.getText();
                LocalDate newStartDate = incomeDatePicker.getDate();
                String newFrequency = (String) incomeFrequencyComboBox.getSelectedItem();
                int newRepetitions = Integer.parseInt(incomeRepetitionsField.getText());

                if (newAmount <= 0 || newDescription.isEmpty() || newStartDate == null || newFrequency == null || newRepetitions <= 0) {
                    updateStatus("Заполните все поля повторяющегося дохода для сохранения изменений.", true);
                    return;
                }
                recurringIncomes.get(index).setAmount(newAmount);
                recurringIncomes.get(index).setDescription(newDescription);
                recurringIncomes.get(index).setDate(newStartDate);
                recurringIncomes.get(index).setFrequency(newFrequency);
                recurringIncomes.get(index).setRepetitions(newRepetitions);
            } else if (type.equals("recurring_expense")) {
                double newAmount = Double.parseDouble(expenseAmountField.getText().replace(",", "."));
                String newDescription = expenseDescriptionField.getText();
                String newCategory = (String) expenseCategoryComboBox.getSelectedItem();
                LocalDate newStartDate = expenseDatePicker.getDate();
                String newFrequency = (String) expenseFrequencyComboBox.getSelectedItem();
                int newRepetitions = Integer.parseInt(expenseRepetitionsField.getText());

                if (newAmount <= 0 || newDescription.isEmpty() || newCategory == null || newCategory.isEmpty() || newStartDate == null || newFrequency == null || newRepetitions <= 0) {
                    updateStatus("Заполните все поля повторяющегося расхода для сохранения изменений.", true);
                    return;
                }
                recurringExpenses.get(index).setAmount(newAmount);
                recurringExpenses.get(index).setDescription(newDescription);
                recurringExpenses.get(index).setCategory(newCategory);
                recurringExpenses.get(index).setDate(newStartDate);
                recurringExpenses.get(index).setFrequency(newFrequency);
                recurringExpenses.get(index).setRepetitions(newRepetitions);
            }
            saveData();
            refreshUI();
            clearEntries();
            updateStatus("Запись типа '" + type + "' по индексу " + (index + 1) + " успешно отредактирована.");
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при сохранении отредактированной записи: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void deleteEntry() {
        try {
            int index = Integer.parseInt(editIndexField.getText()) - 1;
            String type = getSelectedEditType();

            String listName = "";
            boolean deleted = false;

            if (type.equals("income")) {
                listName = "доходов";
                if (index >= 0 && index < incomes.size()) {
                    if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить запись №" + (index + 1) + " из " + listName + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        incomes.remove(index);
                        deleted = true;
                    }
                }
            } else if (type.equals("expense")) {
                listName = "расходов";
                if (index >= 0 && index < expenses.size()) {
                    if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить запись №" + (index + 1) + " из " + listName + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        expenses.remove(index);
                        deleted = true;
                    }
                }
            } else if (type.equals("recurring_income")) {
                listName = "повторяющихся доходов";
                if (index >= 0 && index < recurringIncomes.size()) {
                    if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить запись №" + (index + 1) + " из " + listName + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        recurringIncomes.remove(index);
                        deleted = true;
                    }
                }
            } else if (type.equals("recurring_expense")) {
                listName = "повторяющихся расходов";
                if (index >= 0 && index < recurringExpenses.size()) {
                    if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить запись №" + (index + 1) + " из " + listName + "?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        recurringExpenses.remove(index);
                        deleted = true;
                    }
                }
            }

            if (deleted) {
                saveData();
                refreshUI();
                clearEntries();
                updateStatus("Запись №" + (index + 1) + " из " + listName + " успешно удалена.");
            } else {
                updateStatus("Удаление отменено или неверный индекс записи " + listName + ".", true);
            }
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат индекса. Используйте целые числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при удалении записи: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void clearAllData() {
        if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите полностью удалить ВСЕ данные (доходы, расходы, бюджет)? Это действие необратимо!", "Подтверждение очистки", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            incomes.clear();
            expenses.clear();
            budget.clear();
            recurringIncomes.clear();
            recurringExpenses.clear();
            goals.clear();
            categories.clear();
            categories.addAll(Arrays.asList("еда", "транспорт", "развлечения", "жилье", "зарплата", "подарки"));
            updateCategoriesComboBoxes();

            File file = new File(DATA_FILE);
            if (file.exists()) {
                if (!file.delete()) {
                    updateStatus("Не удалось удалить файл данных.", true);
                }
            }
            saveData();
            refreshUI();
            clearEntries();
            updateStatus("Все данные успешно очищены.");
        } else {
            updateStatus("Очистка данных отменена.");
        }
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Выберите папку для сохранения CSV");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File outputDir = fileChooser.getSelectedFile();

            try {
                try (PrintWriter pw = new PrintWriter(new File(outputDir, "incomes_onetime.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Amount,Description,Date");
                    for (Income inc : incomes) {
                        pw.println(String.format("%.2f,%s,%s", inc.getAmount(), inc.getDescription(), inc.getDate().format(DATE_FORMATTER)));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "expenses_onetime.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Amount,Description,Category,Date");
                    for (Expense exp : expenses) {
                        pw.println(String.format("%.2f,%s,%s,%s", exp.getAmount(), exp.getDescription(), exp.getCategory(), exp.getDate().format(DATE_FORMATTER)));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "incomes_recurring.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Amount,Description,StartDate,Frequency,Repetitions");
                    for (RecurringIncome rInc : recurringIncomes) {
                        pw.println(String.format("%.2f,%s,%s,%s,%d", rInc.getAmount(), rInc.getDescription(), rInc.getDate().format(DATE_FORMATTER), rInc.getFrequency(), rInc.getRepetitions()));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "expenses_recurring.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Amount,Description,Category,StartDate,Frequency,Repetitions");
                    for (RecurringExpense rExp : recurringExpenses) {
                        pw.println(String.format("%.2f,%s,%s,%s,%s,%d", rExp.getAmount(), rExp.getDescription(), rExp.getCategory(), rExp.getDate().format(DATE_FORMATTER), rExp.getFrequency(), rExp.getRepetitions()));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "goals.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Name,TargetAmount,CurrentAmount,DueDate");
                    for (FinancialGoal goal : goals) {
                        pw.println(String.format("%s,%.2f,%.2f,%s", goal.getName(), goal.getTargetAmount(), goal.getCurrentAmount(), goal.getDueDate().format(DATE_FORMATTER)));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "budget.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Category,Amount");
                    for (Map.Entry<String, Double> entry : budget.entrySet()) {
                        pw.println(String.format("%s,%.2f", entry.getKey(), entry.getValue()));
                    }
                }

                try (PrintWriter pw = new PrintWriter(new File(outputDir, "categories.csv"), StandardCharsets.UTF_8)) {
                    pw.println("Category");
                    for (String cat : categories) {
                        pw.println(cat);
                    }
                }


                updateStatus("Данные успешно экспортированы в " + outputDir.getAbsolutePath());
            } catch (IOException ex) {
                updateStatus("Ошибка при экспорте данных в CSV: " + ex.getMessage(), true);
                ex.printStackTrace();
            }
        } else {
            updateStatus("Экспорт отменен: Папка не выбрана.");
        }
    }


    private void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            updateStatus("Файл данных не найден, создан новый пустой файл.");
            saveData();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            String currentSection = "";

            incomes.clear();
            expenses.clear();
            budget.clear();
            recurringIncomes.clear();
            recurringExpenses.clear();
            goals.clear();
            categories.clear();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("###")) {
                    currentSection = line.substring(3).trim();
                    continue;
                }
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";", -1);

                try {
                    switch (currentSection) {
                        case "INCOMES":
                            if (parts.length == 3) {
                                incomes.add(new Income(Double.parseDouble(parts[0]), parts[1], LocalDate.parse(parts[2], DATE_FORMATTER)));
                            }
                            break;
                        case "EXPENSES":
                            if (parts.length == 4) {
                                expenses.add(new Expense(Double.parseDouble(parts[0]), parts[1], parts[2], LocalDate.parse(parts[3], DATE_FORMATTER)));
                                categories.add(parts[2].toLowerCase());
                            }
                            break;
                        case "BUDGET":
                            if (parts.length == 2) {
                                budget.put(parts[0], Double.parseDouble(parts[1]));
                                categories.add(parts[0].toLowerCase());
                            }
                            break;
                        case "RECURRING_INCOMES":
                            if (parts.length == 5) {
                                recurringIncomes.add(new RecurringIncome(Double.parseDouble(parts[0]), parts[1], LocalDate.parse(parts[2], DATE_FORMATTER), parts[3], Integer.parseInt(parts[4])));
                            }
                            break;
                        case "RECURRING_EXPENSES":
                            if (parts.length == 6) {
                                recurringExpenses.add(new RecurringExpense(Double.parseDouble(parts[0]), parts[1], parts[2], LocalDate.parse(parts[3], DATE_FORMATTER), parts[4], Integer.parseInt(parts[5])));
                                categories.add(parts[2].toLowerCase());
                            }
                            break;
                        case "GOALS":
                            if (parts.length == 4) {
                                goals.add(new FinancialGoal(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), LocalDate.parse(parts[3], DATE_FORMATTER)));
                            }
                            break;
                        case "CATEGORIES":
                            if (parts.length == 1 && !parts[0].isEmpty()) {
                                categories.add(parts[0].toLowerCase());
                            }
                            break;
                    }
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Ошибка при парсинге строки в разделе " + currentSection + ": " + line + " - " + e.getMessage());
                }
            }
            if (categories.isEmpty()) {
                categories.addAll(Arrays.asList("еда", "транспорт", "развлечения", "жилье", "зарплата", "подарки"));
            }
            updateStatus("Данные успешно загружены из " + DATA_FILE);
        } catch (IOException ex) {
            updateStatus("Не удалось прочитать файл данных: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }


    private void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE, StandardCharsets.UTF_8))) {
            pw.println("###INCOMES");
            for (Income inc : incomes) {
                pw.println(String.format("%.2f;%s;%s", inc.getAmount(), inc.getDescription(), inc.getDate().format(DATE_FORMATTER)));
            }
            pw.println("\n###EXPENSES");
            for (Expense exp : expenses) {
                pw.println(String.format("%.2f;%s;%s;%s", exp.getAmount(), exp.getDescription(), exp.getCategory(), exp.getDate().format(DATE_FORMATTER)));
            }
            pw.println("\n###BUDGET");
            for (Map.Entry<String, Double> entry : budget.entrySet()) {
                pw.println(String.format("%s;%.2f", entry.getKey(), entry.getValue()));
            }
            pw.println("\n###RECURRING_INCOMES");
            for (RecurringIncome rInc : recurringIncomes) {
                pw.println(String.format("%.2f;%s;%s;%s;%d", rInc.getAmount(), rInc.getDescription(), rInc.getDate().format(DATE_FORMATTER), rInc.getFrequency(), rInc.getRepetitions()));
            }
            pw.println("\n###RECURRING_EXPENSES");
            for (RecurringExpense rExp : recurringExpenses) {
                pw.println(String.format("%.2f;%s;%s;%s;%s;%d", rExp.getAmount(), rExp.getDescription(), rExp.getCategory(), rExp.getDate().format(DATE_FORMATTER), rExp.getFrequency(), rExp.getRepetitions()));
            }
            pw.println("\n###GOALS");
            for (FinancialGoal goal : goals) {
                pw.println(String.format("%s;%.2f;%.2f;%s", goal.getName(), goal.getTargetAmount(), goal.getCurrentAmount(), goal.getDueDate().format(DATE_FORMATTER)));
            }
            pw.println("\n###CATEGORIES");
            for (String category : categories) {
                pw.println(category);
            }
            updateStatus("Данные успешно сохранены в " + DATA_FILE);
        }
        catch (IOException ex) {
            updateStatus("Не удалось записать данные в файл: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void updateCategoriesComboBoxes() {
        String[] sortedCategories = categories.stream().sorted(String.CASE_INSENSITIVE_ORDER).toArray(String[]::new);

        expenseCategoryComboBox.removeAllItems();
        budgetCategoryComboBox.removeAllItems();

        for (String cat : sortedCategories) {
            expenseCategoryComboBox.addItem(cat);
            budgetCategoryComboBox.addItem(cat);
        }
    }

    private List<Income> getEffectiveIncomes(LocalDate untilDate) {
        List<Income> effectiveIncomes = new ArrayList<>(incomes);
        LocalDate today = LocalDate.now();
        if (untilDate == null) untilDate = today;

        for (RecurringIncome rInc : recurringIncomes) {
            LocalDate startDate = rInc.getDate();
            for (int i = 0; i < rInc.getRepetitions(); i++) {
                LocalDate transactionDate = startDate.plusMonths(i);
                if (transactionDate.isAfter(untilDate)) {
                    break;
                }
                effectiveIncomes.add(new Income(rInc.getAmount(), rInc.getDescription() + " (Повтор)", transactionDate));
            }
        }
        return effectiveIncomes;
    }

    private List<Expense> getEffectiveExpenses(LocalDate untilDate) {
        List<Expense> effectiveExpenses = new ArrayList<>(expenses);
        LocalDate today = LocalDate.now();
        if (untilDate == null) untilDate = today;

        for (RecurringExpense rExp : recurringExpenses) {
            LocalDate startDate = rExp.getDate();
            for (int i = 0; i < rExp.getRepetitions(); i++) {
                LocalDate transactionDate = startDate.plusMonths(i);
                if (transactionDate.isAfter(untilDate)) {
                    break;
                }
                effectiveExpenses.add(new Expense(rExp.getAmount(), rExp.getDescription() + " (Повтор)", rExp.getCategory(), transactionDate));
            }
        }
        return effectiveExpenses;
    }


    private void generateReport(String searchKeyword, String filterCategory, String filterMonth, String filterYear) {
        StringBuilder reportContent = new StringBuilder();
        reportContent.append("===== ОБЩИЙ ФИНАНСОВЫЙ ОТЧЕТ =====\n");
        reportContent.append("Дата отчета: ").append(LocalDate.now().format(DATE_FORMATTER)).append(" ").append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");

        List<Income> effectiveIncomes = getEffectiveIncomes(LocalDate.now());
        List<Expense> effectiveExpenses = getEffectiveExpenses(LocalDate.now());

        List<Income> filteredIncomes = effectiveIncomes.stream()
                .filter(inc -> (searchKeyword == null || searchKeyword.isEmpty() || inc.getDescription().toLowerCase().contains(searchKeyword.toLowerCase())) &&
                        (filterMonth == null || filterMonth.isEmpty() || inc.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(filterMonth)) &&
                        (filterYear == null || filterYear.isEmpty() || String.valueOf(inc.getDate().getYear()).equals(filterYear)))
                .collect(Collectors.toList());

        List<Expense> filteredExpenses = effectiveExpenses.stream()
                .filter(exp -> (searchKeyword == null || searchKeyword.isEmpty() || exp.getDescription().toLowerCase().contains(searchKeyword.toLowerCase())) &&
                        (filterCategory == null || filterCategory.isEmpty() || exp.getCategory().toLowerCase().equals(filterCategory.toLowerCase())) &&
                        (filterMonth == null || filterMonth.isEmpty() || exp.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")).equals(filterMonth)) &&
                        (filterYear == null || filterYear.isEmpty() || String.valueOf(exp.getDate().getYear()).equals(filterYear)))
                .collect(Collectors.toList());

        double totalIncome = filteredIncomes.stream().mapToDouble(Income::getAmount).sum();
        double totalExpense = filteredExpenses.stream().mapToDouble(Expense::getAmount).sum();
        double currentBalance = totalIncome - totalExpense;

        reportContent.append(String.format("Общая сумма доходов (отфильтровано): %.2f%n", totalIncome));
        reportContent.append(String.format("Общая сумма расходов (отфильтровано): %.2f%n", totalExpense));
        reportContent.append(String.format("Текущий баланс: %.2f%n%n", currentBalance));

        reportContent.append("===== РАСХОДЫ ПО КАТЕГОРИЯМ =====\n");
        Map<String, Double> expensesByCategory = filteredExpenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingDouble(Expense::getAmount)));

        if (!expensesByCategory.isEmpty()) {
            expensesByCategory.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> reportContent.append(String.format("- %s: %.2f%n", entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1), entry.getValue())));
        } else {
            reportContent.append("Нет отфильтрованных расходов по категориям.\n");
        }
        reportContent.append("\n");

        reportContent.append("===== СТАТУС БЮДЖЕТА =====\n");
        if (!budget.isEmpty()) {
            budget.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String category = entry.getKey();
                        double limit = entry.getValue();
                        double spent = expensesByCategory.getOrDefault(category, 0.0);
                        double remaining = limit - spent;
                        String status = (remaining >= 0) ? "осталось" : "превышен";
                        reportContent.append(String.format("- %s: Лимит: %.2f, Потрачено: %.2f, %s: %.2f%n",
                                category.substring(0, 1).toUpperCase() + category.substring(1), limit, spent, status, remaining));
                    });
        } else {
            reportContent.append("Бюджет не установлен для каких-либо категорий.\n");
        }
        reportContent.append("\n");

        reportContent.append("===== СПИСОК РАЗОВЫХ ДОХОДОВ =====\n");
        List<Income> onetimeIncomesFiltered = filteredIncomes.stream()
                .filter(inc -> !inc.getDescription().contains("(Повтор)"))
                .collect(Collectors.toList());
        if (!onetimeIncomesFiltered.isEmpty()) {
            for (int i = 0; i < onetimeIncomesFiltered.size(); i++) {
                Income inc = onetimeIncomesFiltered.get(i);
                reportContent.append(String.format("%d. Дата: %s, Сумма: %.2f, Описание: %s%n",
                        i + 1, inc.getDate().format(DATE_FORMATTER), inc.getAmount(), inc.getDescription()));
            }
        } else {
            reportContent.append("Нет отфильтрованных разовых записей о доходах.\n");
        }
        reportContent.append("\n");

        reportContent.append("===== СПИСОК РАЗОВЫХ РАСХОДОВ =====\n");
        List<Expense> onetimeExpensesFiltered = filteredExpenses.stream()
                .filter(exp -> !exp.getDescription().contains("(Повтор)"))
                .collect(Collectors.toList());
        if (!onetimeExpensesFiltered.isEmpty()) {
            for (int i = 0; i < onetimeExpensesFiltered.size(); i++) {
                Expense exp = onetimeExpensesFiltered.get(i);
                reportContent.append(String.format("%d. Дата: %s, Сумма: %.2f, Категория: %s, Описание: %s%n",
                        i + 1, exp.getDate().format(DATE_FORMATTER), exp.getAmount(), exp.getCategory().substring(0, 1).toUpperCase() + exp.getCategory().substring(1), exp.getDescription()));
            }
        } else {
            reportContent.append("Нет отфильтрованных разовых записей о расходах.\n");
        }
        reportContent.append("\n");

        reportContent.append("===== СПИСОК ПОВТОРЯЮЩИХСЯ ДОХОДОВ =====\n");
        if (!recurringIncomes.isEmpty()) {
            for (int i = 0; i < recurringIncomes.size(); i++) {
                RecurringIncome rInc = recurringIncomes.get(i);
                reportContent.append(String.format("%d. Нач. дата: %s, Сумма: %.2f, Описание: %s, Частота: %s, Повторений: %d%n",
                        i + 1, rInc.getDate().format(DATE_FORMATTER), rInc.getAmount(), rInc.getDescription(), rInc.getFrequency(), rInc.getRepetitions()));
            }
        } else {
            reportContent.append("Нет повторяющихся записей о доходах.\n");
        }
        reportContent.append("\n");

        reportContent.append("===== СПИСОК ПОВТОРЯЮЩИХСЯ РАСХОДОВ =====\n");
        if (!recurringExpenses.isEmpty()) {
            for (int i = 0; i < recurringExpenses.size(); i++) {
                RecurringExpense rExp = recurringExpenses.get(i);
                reportContent.append(String.format("%d. Нач. дата: %s, Сумма: %.2f, Категория: %s, Описание: %s, Частота: %s, Повторений: %d%n",
                        i + 1, rExp.getDate().format(DATE_FORMATTER), rExp.getAmount(), rExp.getDescription(), rExp.getCategory().substring(0, 1).toUpperCase() + rExp.getCategory().substring(1), rExp.getFrequency(), rExp.getRepetitions()));
            }
        } else {
            reportContent.append("Нет повторяющихся записей о расходах.\n");
        }
        reportContent.append("\n");

        reportTextArea.setText(reportContent.toString());
    }

    private void applyFilters() {
        String searchKeyword = filterDescriptionField.getText();
        if (searchKeyword.equals("Ключевое слово")) searchKeyword = "";

        String filterCategory = filterCategoryField.getText();
        if (filterCategory.equals("Например: еда")) filterCategory = "";

        String filterMonth = filterMonthField.getText();
        if (filterMonth.equals("Например: 2025-06")) filterMonth = "";

        String filterYear = filterYearField.getText();
        if (filterYear.equals("Например: 2025")) filterYear = "";

        generateReport(searchKeyword, filterCategory, filterMonth, filterYear);
        updateStatus("Отчет отфильтрован.");
    }

    private void resetFilters() {
        filterDescriptionField.setText(""); applyPlaceholderStyle(filterDescriptionField, "Ключевое слово");
        filterCategoryField.setText(""); applyPlaceholderStyle(filterCategoryField, "Например: еда");
        filterMonthField.setText(""); applyPlaceholderStyle(filterMonthField, "Например: 2025-06");
        filterYearField.setText(""); applyPlaceholderStyle(filterYearField, "Например: 2025");
        generateReport(null, null, null, null);
        updateStatus("Фильтры сброшены, отчет обновлен.");
    }

    private void plotCharts() {
        Component[] dashboardComponents = ((JPanel) tabbedPane.getComponentAt(2)).getComponents();
        JTextArea monthlySummaryText = null;
        JTextArea forecastText = null;

        for (Component comp : dashboardComponents) {
            if (comp instanceof JPanel) {
                JPanel subPanel = (JPanel) comp;
                if (subPanel.getBorder() instanceof javax.swing.border.TitledBorder) {
                    javax.swing.border.TitledBorder border = (javax.swing.border.TitledBorder) subPanel.getBorder();
                    if (border.getTitle().equals("Ежемесячная/ежегодная сводка")) {
                        for (Component innerComp : subPanel.getComponents()) {
                            if (innerComp instanceof JScrollPane) {
                                monthlySummaryText = (JTextArea) ((JScrollPane) innerComp).getViewport().getView();
                                break;
                            }
                        }
                    } else if (border.getTitle().equals("Прогноз баланса (на 6 месяцев)")) {
                        for (Component innerComp : subPanel.getComponents()) {
                            if (innerComp instanceof JScrollPane) {
                                forecastText = (JTextArea) ((JScrollPane) innerComp).getViewport().getView();
                                break;
                            }
                        }
                    }
                }
            }
        }


        Map<String, Map<String, Double>> monthlyData = calculateMonthlySummary();
        updateMonthlySummaryText(monthlySummaryText, monthlyData);

        generateForecast(forecastText, 6);

        updateStatus("Графики и прогноз обновлены (графики - заглушка).");
    }

    private Map<String, Map<String, Double>> calculateMonthlySummary() {
        Map<String, Map<String, Double>> monthlyData = new TreeMap<>();

        List<Income> effectiveIncomes = getEffectiveIncomes(LocalDate.now());
        List<Expense> effectiveExpenses = getEffectiveExpenses(LocalDate.now());

        for (Income income : effectiveIncomes) {
            String monthYear = income.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyData.computeIfAbsent(monthYear, k -> new HashMap<>()).merge("income", income.getAmount(), Double::sum);
        }

        for (Expense expense : effectiveExpenses) {
            String monthYear = expense.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            monthlyData.computeIfAbsent(monthYear, k -> new HashMap<>()).merge("expense", expense.getAmount(), Double::sum);
        }

        for (Map.Entry<String, Map<String, Double>> entry : monthlyData.entrySet()) {
            Map<String, Double> data = entry.getValue();
            double income = data.getOrDefault("income", 0.0);
            double expense = data.getOrDefault("expense", 0.0);
            data.put("balance", income - expense);
        }
        return monthlyData;
    }

    private void updateMonthlySummaryText(JTextArea textArea, Map<String, Map<String, Double>> monthlyData) {
        if (textArea == null) return;
        textArea.setText("");
        StringBuilder summaryLines = new StringBuilder();
        summaryLines.append("ЕЖЕМЕСЯЧНАЯ СВОДКА:\n-------------------\n");
        if (!monthlyData.isEmpty()) {
            monthlyData.forEach((month, data) ->
                    summaryLines.append(String.format("  %s: Доход: %.2f, Расход: %.2f, Баланс: %.2f%n",
                            month, data.getOrDefault("income", 0.0), data.getOrDefault("expense", 0.0), data.getOrDefault("balance", 0.0))));
        } else {
            summaryLines.append("  Нет данных для ежемесячной сводки.\n");
        }

        Map<String, Map<String, Double>> yearlyData = new TreeMap<>();
        monthlyData.forEach((monthYear, data) -> {
            String year = monthYear.substring(0, 4);
            yearlyData.computeIfAbsent(year, k -> new HashMap<>()).merge("income", data.getOrDefault("income", 0.0), Double::sum);
            yearlyData.get(year).merge("expense", data.getOrDefault("expense", 0.0), Double::sum);
            yearlyData.get(year).merge("balance", data.getOrDefault("balance", 0.0), Double::sum);
        });

        summaryLines.append("\nЕЖЕГОДНАЯ СВОДКА:\n-------------------\n");
        if (!yearlyData.isEmpty()) {
            yearlyData.forEach((year, data) ->
                    summaryLines.append(String.format("  %s: Доход: %.2f, Расход: %.2f, Баланс: %.2f%n",
                            year, data.getOrDefault("income", 0.0), data.getOrDefault("expense", 0.0), data.getOrDefault("balance", 0.0))));
        } else {
            summaryLines.append("  Нет данных для ежегодной сводки.\n");
        }

        textArea.setText(summaryLines.toString());
    }


    private void generateForecast(JTextArea textArea, int numMonths) {
        if (textArea == null) return;
        textArea.setText("");
        StringBuilder forecastContent = new StringBuilder();
        forecastContent.append("ПРОГНОЗ БАЛАНСА:\n-------------------\n\n");

        double currentBalance = (incomes.stream().mapToDouble(Income::getAmount).sum() + recurringIncomes.stream().mapToDouble(RecurringIncome::getAmount).sum())
                - (expenses.stream().mapToDouble(Expense::getAmount).sum() + recurringExpenses.stream().mapToDouble(RecurringExpense::getAmount).sum());


        forecastContent.append(String.format("Текущий баланс: %.2f%n%n", currentBalance));

        double projectedBalance = currentBalance;
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= numMonths; i++) {
            LocalDate futureDate = today.plusMonths(i);
            String forecastMonthYear = futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            double monthlyProjectedIncome = 0;
            double monthlyProjectedExpense = 0;

            for (RecurringIncome rInc : recurringIncomes) {
                LocalDate startDate = rInc.getDate();
                long monthsSinceStart = java.time.temporal.ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), futureDate.withDayOfMonth(1));

                if (monthsSinceStart >= 0 && monthsSinceStart < rInc.getRepetitions()) {
                    monthlyProjectedIncome += rInc.getAmount();
                }
            }

            for (RecurringExpense rExp : recurringExpenses) {
                LocalDate startDate = rExp.getDate();
                long monthsSinceStart = java.time.temporal.ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), futureDate.withDayOfMonth(1));

                if (monthsSinceStart >= 0 && monthsSinceStart < rExp.getRepetitions()) {
                    monthlyProjectedExpense += rExp.getAmount();
                }
            }
            projectedBalance += (monthlyProjectedIncome - monthlyProjectedExpense);
            forecastContent.append(String.format("  %s: Прогнозируемый баланс: %.2f (Доход: %.2f, Расход: %.2f)%n",
                    forecastMonthYear, projectedBalance, monthlyProjectedIncome, monthlyProjectedExpense));
        }
        textArea.setText(forecastContent.toString());
    }


    private void addGoal() {
        try {
            String name = goalNameField.getText().trim();
            double targetAmount = Double.parseDouble(goalTargetAmountField.getText().replace(",", "."));
            double currentAmount = Double.parseDouble(goalCurrentAmountField.getText().replace(",", "."));
            LocalDate dueDate = goalDueDateField.getDate();

            if (name.isEmpty() || name.equals("Название цели")) {
                updateStatus("Пожалуйста, введите название цели.", true);
                return;
            }
            if (targetAmount <= 0) {
                updateStatus("Целевая сумма должна быть положительным числом.", true);
                return;
            }
            if (currentAmount < 0) {
                updateStatus("Текущая сумма не может быть отрицательной.", true);
                return;
            }
            if (dueDate == null) {
                updateStatus("Пожалуйста, выберите дату завершения цели.", true);
                return;
            }

            if (goals.stream().anyMatch(g -> g.getName().equalsIgnoreCase(name))) {
                updateStatus("Цель с названием '" + name + "' уже существует. Используйте другое название или обновите существующую цель.", true);
                return;
            }

            goals.add(new FinancialGoal(name, targetAmount, currentAmount, dueDate));
            saveData();
            updateGoalsDisplay();
            clearEntries();
            updateStatus("Цель '" + name + "' успешно добавлена.");
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы для цели. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при добавлении цели: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void updateGoal() {
        int selectedRow = goalsTable.getSelectedRow();
        if (selectedRow == -1) {
            updateStatus("Выберите цель для обновления.", true);
            return;
        }

        String originalGoalName = (String) goalsTableModel.getValueAt(selectedRow, 0);
        FinancialGoal goalToUpdate = goals.stream()
                .filter(g -> g.getName().equals(originalGoalName))
                .findFirst()
                .orElse(null);

        if (goalToUpdate == null) {
            updateStatus("Выбранная цель не найдена.", true);
            return;
        }

        try {
            String newName = goalNameField.getText().trim();
            double newTargetAmount = Double.parseDouble(goalTargetAmountField.getText().replace(",", "."));
            double newCurrentAmount = Double.parseDouble(goalCurrentAmountField.getText().replace(",", "."));
            LocalDate newDueDate = goalDueDateField.getDate();

            if (newName.isEmpty() || newName.equals("Название цели")) {
                updateStatus("Пожалуйста, введите название цели.", true);
                return;
            }
            if (newTargetAmount <= 0) {
                updateStatus("Целевая сумма должна быть положительным числом.", true);
                return;
            }
            if (newCurrentAmount < 0) {
                updateStatus("Текущая сумма не может быть отрицательной.", true);
                return;
            }
            if (newDueDate == null) {
                updateStatus("Пожалуйста, выберите дату завершения цели.", true);
                return;
            }

            if (!newName.equalsIgnoreCase(originalGoalName) && goals.stream().anyMatch(g -> g.getName().equalsIgnoreCase(newName))) {
                updateStatus("Цель с названием '" + newName + "' уже существует. Используйте другое название.", true);
                return;
            }

            goalToUpdate.setName(newName);
            goalToUpdate.setTargetAmount(newTargetAmount);
            goalToUpdate.setCurrentAmount(newCurrentAmount);
            goalToUpdate.setDueDate(newDueDate);

            saveData();
            updateGoalsDisplay();
            clearEntries();
            updateStatus("Цель '" + newName + "' успешно обновлена.");
        } catch (NumberFormatException ex) {
            updateStatus("Неверный формат суммы для цели. Используйте числа.", true);
        } catch (Exception ex) {
            updateStatus("Произошла ошибка при обновлении цели: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void deleteGoal() {
        int selectedRow = goalsTable.getSelectedRow();
        if (selectedRow == -1) {
            updateStatus("Выберите цель для удаления.", true);
            return;
        }

        String goalName = (String) goalsTableModel.getValueAt(selectedRow, 0);

        if (JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить цель '" + goalName + "'?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            goals.removeIf(g -> g.getName().equals(goalName));
            saveData();
            updateGoalsDisplay();
            clearEntries();
            updateStatus("Цель '" + goalName + "' успешно удалена.");
        } else {
            updateStatus("Удаление цели отменено.");
        }
    }

    private void updateGoalsDisplay() {
        goalsTableModel.setRowCount(0);

        for (FinancialGoal goal : goals) {
            double progress = goal.getProgressPercentage();
            double remaining = goal.getRemainingAmount();
            String statusTag = "";
            if (progress >= 100) {
                statusTag = "Завершена";
            } else if (goal.getDueDate().isBefore(LocalDate.now())) {
                statusTag = "Просрочена";
            }

            goalsTableModel.addRow(new Object[]{
                    goal.getName(),
                    String.format("%.2f", goal.getTargetAmount()),
                    String.format("%.2f", goal.getCurrentAmount()),
                    String.format("%.1f%%", progress),
                    String.format("%.2f", remaining),
                    goal.getDueDate().format(DATE_FORMATTER),
                    statusTag
            });
        }
        goalsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getModel().getValueAt(row, 6);
                if (!isSelected) {
                    if ("Завершена".equals(status)) {
                        c.setBackground(new Color(212, 237, 218));
                        c.setForeground(new Color(21, 87, 36));
                    } else if ("Просрочена".equals(status)) {
                        c.setBackground(new Color(248, 215, 218));
                        c.setForeground(new Color(114, 28, 36));
                    } else {
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                    }
                } else {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                return c;
            }
        });
    }

    private void loadGoalForEdit(int rowIndex) {
        String goalName = (String) goalsTableModel.getValueAt(rowIndex, 0);
        FinancialGoal selectedGoal = goals.stream()
                .filter(g -> g.getName().equals(goalName))
                .findFirst()
                .orElse(null);

        if (selectedGoal != null) {
            goalNameField.setText(selectedGoal.getName());
            applyDefaultStyle(goalNameField);
            goalTargetAmountField.setText(String.valueOf(selectedGoal.getTargetAmount()));
            applyDefaultStyle(goalTargetAmountField);
            goalCurrentAmountField.setText(String.valueOf(selectedGoal.getCurrentAmount()));
            applyDefaultStyle(goalCurrentAmountField);
            goalDueDateField.setDate(selectedGoal.getDueDate());
            updateStatus("Цель '" + goalName + "' загружена для редактирования.");
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BudgetApp().setVisible(true);
        });
    }
}

class DatePicker extends JPanel {
    private JTextField dateField;
    private LocalDate selectedDate;
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private LocalDate currentDisplayMonth;

    public DatePicker() {
        super(new BorderLayout());
        dateField = new JTextField();
        dateField.setEditable(false);
        JButton pickButton = new JButton("...");
        pickButton.addActionListener(e -> showCalendarDialog());

        add(dateField, BorderLayout.CENTER);
        add(pickButton, BorderLayout.EAST);
    }

    public LocalDate getDate() {
        return selectedDate;
    }

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            dateField.setText(date.format(formatter));
            dateField.setForeground(Color.BLACK);
        } else {
            dateField.setText("");
        }
    }

    private void showCalendarDialog() {
        JDialog calendarDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Выберите дату", true);
        calendarDialog.setModal(true);
        calendarDialog.setLayout(new BorderLayout());

        JPanel calendarPanel = new JPanel(new GridLayout(0, 7));

        currentDisplayMonth = (selectedDate != null) ? selectedDate.withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);
        JLabel monthYearLabel = new JLabel(currentDisplayMonth.format(DateTimeFormatter.ofPattern("MMMM يَومُ اَلسَبْتِ")), SwingConstants.CENTER);

        JButton prevMonth = new JButton("<");
        JButton nextMonth = new JButton(">");

        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.add(prevMonth, BorderLayout.WEST);
        navPanel.add(monthYearLabel, BorderLayout.CENTER);
        navPanel.add(nextMonth, BorderLayout.EAST);

        Runnable updateCalendar = () -> {
            calendarPanel.removeAll();
            monthYearLabel.setText(currentDisplayMonth.format(DateTimeFormatter.ofPattern("MMMM يَومُ اَلسَبْتِ")));

            String[] dayNames = {"Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб"};
            for (String day : dayNames) {
                calendarPanel.add(new JLabel(day, SwingConstants.CENTER));
            }

            LocalDate firstDayOfMonth = currentDisplayMonth.withDayOfMonth(1);
            int dayOfWeekValue = firstDayOfMonth.getDayOfWeek().getValue();
            int startOffset = (dayOfWeekValue == 7) ? 0 : dayOfWeekValue;

            for (int i = 0; i < startOffset; i++) {
                calendarPanel.add(new JLabel(""));
            }

            LocalDate tempDate = firstDayOfMonth;
            while (tempDate.getMonth() == currentDisplayMonth.getMonth()) {
                JButton dayButton = new JButton(String.valueOf(tempDate.getDayOfMonth()));
                LocalDate currentDay = tempDate;
                dayButton.addActionListener(e -> {
                    setDate(currentDay);
                    calendarDialog.dispose();
                });
                if (currentDay.equals(selectedDate)) {
                    dayButton.setBackground(new Color(100, 150, 255));
                }
                calendarPanel.add(dayButton);
                tempDate = tempDate.plusDays(1);
            }
            calendarPanel.revalidate();
            calendarPanel.repaint();
        };

        prevMonth.addActionListener(e -> {
            currentDisplayMonth = currentDisplayMonth.minusMonths(1);
            updateCalendar.run();
        });
        nextMonth.addActionListener(e -> {
            currentDisplayMonth = currentDisplayMonth.plusMonths(1);
            updateCalendar.run();
        });

        calendarDialog.add(navPanel, BorderLayout.NORTH);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);

        calendarDialog.pack();
        calendarDialog.setLocationRelativeTo(this);
        updateCalendar.run();
        calendarDialog.setVisible(true);
    }
}
