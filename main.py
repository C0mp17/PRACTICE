import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import json
import os
from datetime import datetime, timedelta
from collections import defaultdict
import matplotlib.pyplot as plt
from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg, NavigationToolbar2Tk
import csv

DATA_FILE = "budget_data.json"


class BudgetApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Инструмент для бюджетирования и прогнозирования")
        self.root.geometry("1200x850")
        self.root.minsize(900, 750)

        self.style = ttk.Style()
        self.style.theme_use('clam')

        self.style.configure("TFrame", background="#f8f8f8")
        self.style.configure("TLabel", background="#f8f8f8", font=("Inter", 10))
        self.style.configure("TButton", font=("Inter", 10, "bold"), padding=8, background="#4CAF50", foreground="white",
                             borderwidth=0, relief="flat")
        self.style.map("TButton",
                       background=[('pressed', '#4CAF50'), ('active', '#66BB6A')],
                       foreground=[('pressed', 'white'), ('active', 'white')],
                       relief=[('pressed', 'sunken'), ('!pressed', 'flat')])
        self.style.configure("Danger.TButton", background="#FF5252", foreground="white")
        self.style.map("Danger.TButton",
                       background=[('pressed', '#FF5252'), ('active', '#FF8A80')],
                       foreground=[('pressed', 'white'), ('active', 'white')])

        self.style.configure("TEntry", font=("Inter", 10), padding=5, fieldbackground="white", foreground="black")
        self.style.configure("Placeholder.TEntry", font=("Inter", 10), padding=5, fieldbackground="white",
                             foreground="grey")

        self.style.configure("TText", font=("Inter", 10), padding=5, background="white")
        self.style.configure("TNotebook.Tab", font=("Inter", 10, "bold"), padding=[15, 7], background="#e0e0e0")
        self.style.map("TNotebook.Tab", background=[('selected', '#ffffff')], foreground=[('selected', '#333333')])
        self.style.configure("TLabelframe", background="#f8f8f8", bordercolor="#cccccc", relief="solid", borderwidth=1)
        self.style.configure("TLabelframe.Label", background="#f8f8f8", font=("Inter", 11, "bold"))

        self.style.configure("Treeview", background="white", foreground="black", rowheight=25, font=("Inter", 9))
        self.style.map('Treeview', background=[('selected', '#aed581')])
        self.style.configure("Treeview.Heading", font=("Inter", 10, "bold"))

        self.incomes = []
        self.expenses = []
        self.budget = {}
        self.recurring_incomes = []
        self.recurring_expenses = []
        self.goals = []

        self.editing_record_info = None

        self.status_label = ttk.Label(root, text="Готов", anchor="w", relief=tk.SUNKEN, font=("Inter", 9),
                                      background="#e0e0e0")
        self.status_label.pack(side=tk.BOTTOM, fill=tk.X, ipady=3)

        self.load_data()

        self.notebook = ttk.Notebook(root)
        self.notebook.pack(expand=True, fill="both", padx=10, pady=10)

        self.data_management_frame = ttk.Frame(self.notebook, padding="15 15 15 15")
        self.notebook.add(self.data_management_frame, text="Управление данными")

        self.reports_frame = ttk.Frame(self.notebook, padding="15 15 15 15")
        self.notebook.add(self.reports_frame, text="Отчеты и аналитика")

        self.dashboard_frame = ttk.Frame(self.notebook, padding="15 15 15 15")
        self.notebook.add(self.dashboard_frame, text="Панель инструментов")

        self.goals_frame = ttk.Frame(self.notebook, padding="15 15 15 15")
        self.notebook.add(self.goals_frame, text="Финансовые цели")

        self.create_data_management_widgets(self.data_management_frame)
        self.create_reports_widgets(self.reports_frame)
        self.create_dashboard_widgets(self.dashboard_frame)
        self.create_goals_widgets(self.goals_frame)

        # Вызовы для настройки адаптивной разметки и обновления данных перемещены сюда
        self.root.bind("<Configure>", self.setup_adaptive_layout)
        self.generate_report()
        self.plot_charts()
        self.update_goals_display()

    def setup_adaptive_layout(self, event=None):
        self.data_management_frame.grid_columnconfigure(0, weight=1)
        self.data_management_frame.grid_columnconfigure(1, weight=1)
        self.data_management_frame.grid_columnconfigure(2, weight=1)
        self.data_management_frame.grid_rowconfigure(0, weight=1)
        self.data_management_frame.grid_rowconfigure(1, weight=1)

        self.reports_frame.grid_columnconfigure(0, weight=1)
        self.reports_frame.grid_rowconfigure(1, weight=1)

        self.dashboard_frame.grid_columnconfigure(0, weight=1)
        self.dashboard_frame.grid_rowconfigure(0, weight=1)
        self.dashboard_frame.grid_rowconfigure(1, weight=1)
        self.dashboard_frame.grid_rowconfigure(2, weight=0)
        self.dashboard_frame.grid_rowconfigure(3, weight=0)
        self.dashboard_frame.grid_rowconfigure(4, weight=0)

        self.goals_frame.grid_columnconfigure(0, weight=1)
        self.goals_frame.grid_rowconfigure(1, weight=1)

    def load_data(self):
        if os.path.exists(DATA_FILE):
            try:
                with open(DATA_FILE, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    self.incomes = data.get('incomes', [])
                    self.expenses = data.get('expenses', [])
                    self.budget = data.get('budget', {})
                    self.recurring_incomes = data.get('recurring_incomes', [])
                    self.recurring_expenses = data.get('recurring_expenses', [])
                    self.goals = data.get('goals', [])
                self.update_status(f"Данные успешно загружены из {DATA_FILE}")
            except json.JSONDecodeError:
                messagebox.showerror("Ошибка загрузки",
                                     "Файл данных поврежден или имеет неверный формат. Созданы новые пустые данные.")
                self.incomes = []
                self.expenses = []
                self.budget = {}
                self.recurring_incomes = []
                self.recurring_expenses = []
                self.goals = []
                self.save_data()
                self.update_status("Файл данных поврежден, созданы новые пустые данные", is_error=True)
            except IOError as e:
                messagebox.showerror("Ошибка загрузки", f"Не удалось прочитать файл данных: {e}")
                self.incomes = []
                self.expenses = []
                self.budget = {}
                self.recurring_incomes = []
                self.recurring_expenses = []
                self.goals = []
                self.update_status(f"Ошибка чтения файла данных: {e}", is_error=True)
        else:
            self.incomes = []
            self.expenses = []
            self.budget = {}
            self.recurring_incomes = []
            self.recurring_expenses = []
            self.goals = []
            self.save_data()
            self.update_status(f"Файл {DATA_FILE} не найден, создан новый пустой файл.")

    def save_data(self):
        data = {
            "incomes": self.incomes,
            "expenses": self.expenses,
            "budget": self.budget,
            "recurring_incomes": self.recurring_incomes,
            "recurring_expenses": self.recurring_expenses,
            "goals": self.goals
        }
        try:
            with open(DATA_FILE, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=4, ensure_ascii=False)
            self.update_status(f"Данные успешно сохранены в {DATA_FILE}")
        except IOError as e:
            messagebox.showerror("Ошибка сохранения", f"Не удалось записать данные в файл: {e}")
            self.update_status(f"Ошибка записи файла данных: {e}", is_error=True)

    def validate_date(self, date_str):
        try:
            datetime.strptime(date_str, '%Y-%m-%d')
            return True
        except ValueError:
            return False

    def update_status(self, message, is_error=False):
        if is_error:
            self.status_label.config(text=f"Ошибка: {message}", foreground="red")
        else:
            self.status_label.config(text=message, foreground="black")
        self.root.after(5000, lambda: self.status_label.config(text="Готов", foreground="black"))

    def clear_entries(self):
        self.income_amount_entry.delete(0, tk.END)
        self.add_placeholder(self.income_amount_entry, "Введите сумму")
        self.income_description_entry.delete(0, tk.END)
        self.add_placeholder(self.income_description_entry, "Например: Зарплата, подарок")
        self.income_date_entry.delete(0, tk.END)
        self.income_date_entry.insert(0, datetime.now().strftime('%Y-%m-%d'))
        self.income_date_entry.config(style='TEntry')
        self.is_recurring_income.set(False)
        self.toggle_recurring_income_options()
        self.income_repetitions_entry.delete(0, tk.END)
        self.add_placeholder(self.income_repetitions_entry, "Количество повторений")

        self.expense_amount_entry.delete(0, tk.END)
        self.add_placeholder(self.expense_amount_entry, "Введите сумму")
        self.expense_description_entry.delete(0, tk.END)
        self.add_placeholder(self.expense_description_entry, "Например: Продукты, такси")
        self.expense_category_entry.delete(0, tk.END)
        self.add_placeholder(self.expense_category_entry, "Например: еда, транспорт")
        self.expense_date_entry.delete(0, tk.END)
        self.expense_date_entry.insert(0, datetime.now().strftime('%Y-%m-%d'))
        self.expense_date_entry.config(style='TEntry')
        self.is_recurring_expense.set(False)
        self.toggle_recurring_expense_options()
        self.expense_repetitions_entry.delete(0, tk.END)
        self.add_placeholder(self.expense_repetitions_entry, "Количество повторений")

        self.budget_category_entry.delete(0, tk.END)
        self.add_placeholder(self.budget_category_entry, "Например: еда, транспорт")
        self.budget_amount_entry.delete(0, tk.END)
        self.add_placeholder(self.budget_amount_entry, "Например: 500.00")

        self.edit_index_entry.delete(0, tk.END)
        self.add_placeholder(self.edit_index_entry, "Введите индекс из отчета")

        self.editing_record_info = None
        self.save_edit_button.config(state=tk.DISABLED)
        self.load_edit_button.config(state=tk.NORMAL)

        self.goal_name_entry.delete(0, tk.END)
        self.add_placeholder(self.goal_name_entry, "Название цели")
        self.goal_target_amount_entry.delete(0, tk.END)
        self.add_placeholder(self.goal_target_amount_entry, "Целевая сумма")
        self.goal_current_amount_entry.delete(0, tk.END)
        self.add_placeholder(self.goal_current_amount_entry, "Текущая сумма")
        self.goal_due_date_entry.delete(0, tk.END)
        self.add_placeholder(self.goal_due_date_entry, "Дата завершения (ГГГГ-ММ-ДД)")
        self.update_goals_display()

    def create_data_management_widgets(self, parent_frame):
        income_frame = ttk.LabelFrame(parent_frame, text="Добавить доход", padding="10 10 10 10")
        income_frame.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)
        income_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(income_frame, text="Сумма:").grid(row=0, column=0, sticky="w", pady=2)
        self.income_amount_entry = ttk.Entry(income_frame)
        self.income_amount_entry.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.income_amount_entry, "Введите сумму")

        ttk.Label(income_frame, text="Описание:").grid(row=1, column=0, sticky="w", pady=2)
        self.income_description_entry = ttk.Entry(income_frame)
        self.income_description_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.income_description_entry, "Например: Зарплата, подарок")

        ttk.Label(income_frame, text="Дата (ГГГГ-ММ-ДД):").grid(row=2, column=0, sticky="w", pady=2)
        self.income_date_entry = ttk.Entry(income_frame)
        self.income_date_entry.grid(row=2, column=1, sticky="ew", pady=2, padx=5)
        self.income_date_entry.insert(0, datetime.now().strftime('%Y-%m-%d'))
        self.add_placeholder(self.income_date_entry, "Например: 2025-06-21")

        self.is_recurring_income = tk.BooleanVar(value=False)
        ttk.Checkbutton(income_frame, text="Повторяющаяся транзакция", variable=self.is_recurring_income,
                        command=self.toggle_recurring_income_options).grid(row=3, column=0, columnspan=2, sticky="w",
                                                                           pady=5)

        self.income_recurring_options_frame = ttk.Frame(income_frame)
        self.income_recurring_options_frame.grid(row=4, column=0, columnspan=2, sticky="ew", padx=0, pady=0)
        self.income_recurring_options_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(self.income_recurring_options_frame, text="Частота:").grid(row=0, column=0, sticky="w", pady=2)
        self.income_frequency_combobox = ttk.Combobox(self.income_recurring_options_frame, values=["Ежемесячно"])
        self.income_frequency_combobox.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.income_frequency_combobox.set("Ежемесячно")

        ttk.Label(self.income_recurring_options_frame, text="Повторений:").grid(row=1, column=0, sticky="w", pady=2)
        self.income_repetitions_entry = ttk.Entry(self.income_recurring_options_frame)
        self.income_repetitions_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.income_repetitions_entry, "Количество повторений")

        self.toggle_recurring_income_options()

        ttk.Button(income_frame, text="Добавить доход", command=self.add_income).grid(row=5, column=0, columnspan=2,
                                                                                      pady=10)

        expense_frame = ttk.LabelFrame(parent_frame, text="Добавить расход", padding="10 10 10 10")
        expense_frame.grid(row=0, column=1, sticky="nsew", padx=5, pady=5)
        expense_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(expense_frame, text="Сумма:").grid(row=0, column=0, sticky="w", pady=2)
        self.expense_amount_entry = ttk.Entry(expense_frame)
        self.expense_amount_entry.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.expense_amount_entry, "Введите сумму")

        ttk.Label(expense_frame, text="Описание:").grid(row=1, column=0, sticky="w", pady=2)
        self.expense_description_entry = ttk.Entry(expense_frame)
        self.expense_description_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.expense_description_entry, "Например: Продукты, такси")

        ttk.Label(expense_frame, text="Категория:").grid(row=2, column=0, sticky="w", pady=2)
        self.expense_category_entry = ttk.Entry(expense_frame)
        self.expense_category_entry.grid(row=2, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.expense_category_entry, "Например: еда, транспорт")

        ttk.Label(expense_frame, text="Дата (ГГГГ-ММ-ДД):").grid(row=3, column=0, sticky="w", pady=2)
        self.expense_date_entry = ttk.Entry(expense_frame)
        self.expense_date_entry.grid(row=3, column=1, sticky="ew", pady=2, padx=5)
        self.expense_date_entry.insert(0, datetime.now().strftime('%Y-%m-%d'))
        self.add_placeholder(self.expense_date_entry, "Например: 2025-06-21")

        self.is_recurring_expense = tk.BooleanVar(value=False)
        ttk.Checkbutton(expense_frame, text="Повторяющаяся транзакция", variable=self.is_recurring_expense,
                        command=self.toggle_recurring_expense_options).grid(row=4, column=0, columnspan=2, sticky="w",
                                                                            pady=5)

        self.expense_recurring_options_frame = ttk.Frame(expense_frame)
        self.expense_recurring_options_frame.grid(row=5, column=0, columnspan=2, sticky="ew", padx=0, pady=0)
        self.expense_recurring_options_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(self.expense_recurring_options_frame, text="Частота:").grid(row=0, column=0, sticky="w", pady=2)
        self.expense_frequency_combobox = ttk.Combobox(self.expense_recurring_options_frame, values=["Ежемесячно"])
        self.expense_frequency_combobox.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.expense_frequency_combobox.set("Ежемесячно")

        ttk.Label(self.expense_recurring_options_frame, text="Повторений:").grid(row=1, column=0, sticky="w", pady=2)
        self.expense_repetitions_entry = ttk.Entry(self.expense_recurring_options_frame)
        self.expense_repetitions_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.expense_repetitions_entry, "Количество повторений")

        self.toggle_recurring_expense_options()

        ttk.Button(expense_frame, text="Добавить расход", command=self.add_expense).grid(row=6, column=0, columnspan=2,
                                                                                         pady=10)

        budget_frame = ttk.LabelFrame(parent_frame, text="Установить/обновить бюджет", padding="10 10 10 10")
        budget_frame.grid(row=1, column=0, columnspan=2, sticky="nsew", padx=5, pady=5)
        budget_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(budget_frame, text="Категория:").grid(row=0, column=0, sticky="w", pady=2)
        self.budget_category_entry = ttk.Entry(budget_frame)
        self.budget_category_entry.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.budget_category_entry, "Например: еда, транспорт")

        ttk.Label(budget_frame, text="Сумма бюджета:").grid(row=1, column=0, sticky="w", pady=2)
        self.budget_amount_entry = ttk.Entry(budget_frame)
        self.budget_amount_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.budget_amount_entry, "Например: 500.00")

        ttk.Button(budget_frame, text="Установить бюджет", command=self.set_budget).grid(row=2, column=0, columnspan=2,
                                                                                         pady=10)

        edit_delete_frame = ttk.LabelFrame(parent_frame, text="Редактирование/удаление записей", padding="10 10 10 10")
        edit_delete_frame.grid(row=0, column=2, rowspan=2, sticky="nsew", padx=5, pady=5)
        edit_delete_frame.grid_columnconfigure(0, weight=1)

        ttk.Label(edit_delete_frame, text="Индекс записи:").grid(row=0, column=0, sticky="w", pady=2)
        self.edit_index_entry = ttk.Entry(edit_delete_frame)
        self.edit_index_entry.grid(row=1, column=0, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.edit_index_entry, "Введите индекс из отчета")

        self.edit_type_var = tk.StringVar(value="expense")
        ttk.Radiobutton(edit_delete_frame, text="Доход", variable=self.edit_type_var, value="income").grid(row=2,
                                                                                                           column=0,
                                                                                                           sticky="w",
                                                                                                           pady=2)
        ttk.Radiobutton(edit_delete_frame, text="Расход", variable=self.edit_type_var, value="expense").grid(row=3,
                                                                                                             column=0,
                                                                                                             sticky="w",
                                                                                                             pady=2)
        ttk.Radiobutton(edit_delete_frame, text="Повторяющийся доход", variable=self.edit_type_var,
                        value="recurring_income").grid(row=4, column=0, sticky="w", pady=2)
        ttk.Radiobutton(edit_delete_frame, text="Повторяющийся расход", variable=self.edit_type_var,
                        value="recurring_expense").grid(row=5, column=0, sticky="w", pady=2)

        self.load_edit_button = ttk.Button(edit_delete_frame, text="Загрузить для редактирования",
                                           command=self.load_entry_for_edit)
        self.load_edit_button.grid(row=6, column=0, pady=5)

        self.save_edit_button = ttk.Button(edit_delete_frame, text="Сохранить изменения",
                                           command=self.save_edited_entry, state=tk.DISABLED)
        self.save_edit_button.grid(row=7, column=0, pady=5)

        ttk.Button(edit_delete_frame, text="Удалить запись", command=self.delete_entry).grid(row=8, column=0, pady=5)
        ttk.Button(edit_delete_frame, text="Очистить все данные", command=self.clear_all_data,
                   style="Danger.TButton").grid(row=9, column=0, pady=20)
        ttk.Button(edit_delete_frame, text="Экспорт в CSV", command=self.export_to_csv).grid(row=10, column=0, pady=5)

    def toggle_recurring_income_options(self):
        if self.is_recurring_income.get():
            self.income_recurring_options_frame.grid()
        else:
            self.income_recurring_options_frame.grid_remove()

    def toggle_recurring_expense_options(self):
        if self.is_recurring_expense.get():
            self.expense_recurring_options_frame.grid()
        else:
            self.expense_recurring_options_frame.grid_remove()

    def create_reports_widgets(self, parent_frame):
        filter_frame = ttk.LabelFrame(parent_frame, text="Фильтры отчета", padding="10 10 10 10")
        filter_frame.grid(row=0, column=0, sticky="ew", padx=5, pady=5)
        filter_frame.grid_columnconfigure(1, weight=1)
        filter_frame.grid_columnconfigure(3, weight=1)

        ttk.Label(filter_frame, text="Поиск по описанию:").grid(row=0, column=0, sticky="w", pady=2)
        self.filter_description_entry = ttk.Entry(filter_frame)
        self.filter_description_entry.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.filter_description_entry, "Ключевое слово")

        ttk.Label(filter_frame, text="Категория:").grid(row=0, column=2, sticky="w", pady=2, padx=(10, 0))
        self.filter_category_entry = ttk.Entry(filter_frame)
        self.filter_category_entry.grid(row=0, column=3, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.filter_category_entry, "Например: еда")

        ttk.Label(filter_frame, text="Месяц (ГГГГ-ММ):").grid(row=1, column=0, sticky="w", pady=2)
        self.filter_month_entry = ttk.Entry(filter_frame)
        self.filter_month_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.filter_month_entry, "Например: 2025-06")

        ttk.Label(filter_frame, text="Год (ГГГГ):").grid(row=1, column=2, sticky="w", pady=2, padx=(10, 0))
        self.filter_year_entry = ttk.Entry(filter_frame)
        self.filter_year_entry.grid(row=1, column=3, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.filter_year_entry, "Например: 2025")

        ttk.Button(filter_frame, text="Применить фильтры", command=self.apply_filters).grid(row=2, column=0,
                                                                                            columnspan=2, pady=10)
        ttk.Button(filter_frame, text="Сбросить фильтры", command=self.reset_filters).grid(row=2, column=2,
                                                                                           columnspan=2, pady=10)

        self.report_text = tk.Text(parent_frame, wrap="word", font=("Courier New", 9), relief="flat", borderwidth=1,
                                   highlightbackground="#cccccc", highlightthickness=1)
        self.report_text.grid(row=1, column=0, sticky="nsew", padx=5, pady=5)
        scrollbar = ttk.Scrollbar(parent_frame, command=self.report_text.yview)
        scrollbar.grid(row=1, column=1, sticky="ns", pady=5)
        self.report_text.config(yscrollcommand=scrollbar.set)

    def create_dashboard_widgets(self, parent_frame):
        self.fig, (self.ax1, self.ax2) = plt.subplots(2, 1, figsize=(10, 8), layout='constrained', facecolor='#f8f8f8')
        self.fig.patch.set_facecolor('#f8f8f8')
        plt.style.use('ggplot')

        plt.rcParams.update({'font.size': 9})
        self.ax1.set_title("Расходы по категориям", fontsize=12)
        self.ax2.set_title("Динамика баланса (по месяцам)", fontsize=12)

        self.chart_canvas = FigureCanvasTkAgg(self.fig, master=parent_frame)
        self.chart_canvas_widget = self.chart_canvas.get_tk_widget()
        self.chart_canvas_widget.grid(row=0, column=0, sticky="nsew", padx=5, pady=5)

        toolbar_frame = ttk.Frame(parent_frame)
        toolbar_frame.grid(row=1, column=0, sticky="ew", padx=5, pady=5)
        toolbar = NavigationToolbar2Tk(self.chart_canvas, toolbar_frame)
        toolbar.update()

        summary_frame = ttk.LabelFrame(parent_frame, text="Ежемесячная/ежегодная сводка", padding="10 10 10 10")
        summary_frame.grid(row=2, column=0, sticky="ew", padx=5, pady=5)
        summary_frame.grid_columnconfigure(0, weight=1)

        self.monthly_summary_text = tk.Text(summary_frame, wrap="word", font=("Courier New", 9), height=10,
                                            relief="flat", borderwidth=1, highlightbackground="#cccccc",
                                            highlightthickness=1)
        self.monthly_summary_text.pack(expand=True, fill="both", padx=5, pady=5)
        self.monthly_summary_text.config(state="disabled")

        forecast_frame = ttk.LabelFrame(parent_frame, text="Прогноз баланса (на 6 месяцев)", padding="10 10 10 10")
        forecast_frame.grid(row=3, column=0, sticky="ew", padx=5, pady=5)
        forecast_frame.grid_columnconfigure(0, weight=1)

        self.forecast_text = tk.Text(forecast_frame, wrap="word", font=("Courier New", 9), height=8, relief="flat",
                                     borderwidth=1, highlightbackground="#cccccc", highlightthickness=1)
        self.forecast_text.pack(expand=True, fill="both", padx=5, pady=5)
        self.forecast_text.config(state="disabled")

        ttk.Button(parent_frame, text="Обновить графики и прогноз", command=self.plot_charts).grid(row=4, column=0,
                                                                                                   pady=10)

    def create_goals_widgets(self, parent_frame):
        input_frame = ttk.LabelFrame(parent_frame, text="Добавить/Редактировать цель", padding="10 10 10 10")
        input_frame.grid(row=0, column=0, sticky="ew", padx=5, pady=5)
        input_frame.grid_columnconfigure(1, weight=1)

        ttk.Label(input_frame, text="Название цели:").grid(row=0, column=0, sticky="w", pady=2)
        self.goal_name_entry = ttk.Entry(input_frame)
        self.goal_name_entry.grid(row=0, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.goal_name_entry, "Название цели")

        ttk.Label(input_frame, text="Целевая сумма:").grid(row=1, column=0, sticky="w", pady=2)
        self.goal_target_amount_entry = ttk.Entry(input_frame)
        self.goal_target_amount_entry.grid(row=1, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.goal_target_amount_entry, "Целевая сумма")

        ttk.Label(input_frame, text="Текущая сумма:").grid(row=2, column=0, sticky="w", pady=2)
        self.goal_current_amount_entry = ttk.Entry(input_frame)
        self.goal_current_amount_entry.grid(row=2, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.goal_current_amount_entry, "Текущая сумма")

        ttk.Label(input_frame, text="Дата завершения (ГГГГ-ММ-ДД):").grid(row=3, column=0, sticky="w", pady=2)
        self.goal_due_date_entry = ttk.Entry(input_frame)
        self.goal_due_date_entry.grid(row=3, column=1, sticky="ew", pady=2, padx=5)
        self.add_placeholder(self.goal_due_date_entry, "Дата завершения (ГГГГ-ММ-ДД)")

        button_frame = ttk.Frame(input_frame)
        button_frame.grid(row=4, column=0, columnspan=2, pady=10)

        ttk.Button(button_frame, text="Добавить цель", command=self.add_goal).pack(side=tk.LEFT, padx=5)
        ttk.Button(button_frame, text="Обновить цель", command=self.update_goal).pack(side=tk.LEFT, padx=5)
        ttk.Button(button_frame, text="Удалить цель", command=self.delete_goal, style="Danger.TButton").pack(
            side=tk.LEFT, padx=5)

        self.goals_tree = ttk.Treeview(parent_frame,
                                       columns=("Название", "Цель", "Текущая", "Прогресс", "Осталось", "Крайний срок"),
                                       show="headings", selectmode="browse")
        self.goals_tree.grid(row=1, column=0, sticky="nsew", padx=5, pady=5)

        self.goals_tree.heading("Название", text="Название")
        self.goals_tree.heading("Цель", text="Цель")
        self.goals_tree.heading("Текущая", text="Текущая")
        self.goals_tree.heading("Прогресс", text="Прогресс")
        self.goals_tree.heading("Осталось", text="Осталось")
        self.goals_tree.heading("Крайний срок", text="Крайний срок")

        self.goals_tree.column("Название", width=150, anchor="w")
        self.goals_tree.column("Цель", width=100, anchor="e")
        self.goals_tree.column("Текущая", width=100, anchor="e")
        self.goals_tree.column("Прогресс", width=80, anchor="center")
        self.goals_tree.column("Осталось", width=100, anchor="e")
        self.goals_tree.column("Крайний срок", width=120, anchor="center")

        goals_scrollbar = ttk.Scrollbar(parent_frame, orient="vertical", command=self.goals_tree.yview)
        goals_scrollbar.grid(row=1, column=1, sticky="ns")
        self.goals_tree.configure(yscrollcommand=goals_scrollbar.set)

        self.goals_tree.bind("<<TreeviewSelect>>", self.load_goal_for_edit)

    def add_placeholder(self, entry, text):
        def on_focus_in(event):
            if entry.get() == text and entry.cget('style') == 'Placeholder.TEntry':
                entry.delete(0, tk.END)
                entry.config(style='TEntry')
                entry.config(foreground="black")

        def on_focus_out(event):
            if not entry.get():
                entry.insert(0, text)
                entry.config(style='Placeholder.TEntry')

        entry.delete(0, tk.END)
        entry.insert(0, text)
        entry.config(style='Placeholder.TEntry')

        entry.bind('<FocusIn>', on_focus_in)
        entry.bind('<FocusOut>', on_focus_out)

    def add_income(self):
        try:
            amount_str = self.income_amount_entry.get()
            description = self.income_description_entry.get()
            date = self.income_date_entry.get()

            if amount_str == "Введите сумму" or description == "Например: Зарплата, подарок" or date == "Например: 2025-06-21":
                self.update_status("Пожалуйста, заполните все поля дохода.", is_error=True)
                return

            amount = float(amount_str)
            if amount <= 0:
                self.update_status("Сумма дохода должна быть положительным числом.", is_error=True)
                return

            if not self.validate_date(date):
                self.update_status("Неверный формат даты дохода. Используйте ГГГГ-ММ-ДД.", is_error=True)
                return

            if self.is_recurring_income.get():
                frequency = self.income_frequency_combobox.get()
                repetitions_str = self.income_repetitions_entry.get()

                if repetitions_str == "Количество повторений" or not repetitions_str.isdigit() or int(
                        repetitions_str) <= 0:
                    self.update_status(
                        "Для повторяющихся доходов укажите корректное количество повторений (целое положительное число).",
                        is_error=True)
                    return
                repetitions = int(repetitions_str)

                self.recurring_incomes.append({
                    "amount": amount,
                    "description": description,
                    "start_date": date,
                    "frequency": frequency,
                    "repetitions": repetitions
                })
                self.update_status("Повторяющийся доход успешно добавлен.")
            else:
                self.incomes.append({"amount": amount, "description": description, "date": date})
                self.update_status("Доход успешно добавлен.")

            self.save_data()
            self.generate_report()
            self.plot_charts()
            self.clear_entries()

        except ValueError:
            self.update_status("Неверный формат суммы дохода или повторений. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при добавлении дохода: {e}", is_error=True)

    def add_expense(self):
        try:
            amount_str = self.expense_amount_entry.get()
            description = self.expense_description_entry.get()
            category = self.expense_category_entry.get().lower()
            date = self.expense_date_entry.get()

            if amount_str == "Введите сумму" or description == "Например: Продукты, такси" or category == "Например: еда, транспорт" or date == "Например: 2025-06-21":
                self.update_status("Пожалуйста, заполните все поля расхода.", is_error=True)
                return

            amount = float(amount_str)
            if amount <= 0:
                self.update_status("Сумма расхода должна быть положительным числом.", is_error=True)
                return

            if not self.validate_date(date):
                self.update_status("Неверный формат даты расхода. Используйте ГГГГ-ММ-ДД.", is_error=True)
                return

            if self.is_recurring_expense.get():
                frequency = self.expense_frequency_combobox.get()
                repetitions_str = self.expense_repetitions_entry.get()

                if repetitions_str == "Количество повторений" or not repetitions_str.isdigit() or int(
                        repetitions_str) <= 0:
                    self.update_status(
                        "Для повторяющихся расходов укажите корректное количество повторений (целое положительное число).",
                        is_error=True)
                    return
                repetitions = int(repetitions_str)

                self.recurring_expenses.append({
                    "amount": amount,
                    "description": description,
                    "category": category,
                    "start_date": date,
                    "frequency": frequency,
                    "repetitions": repetitions
                })
                self.update_status("Повторяющийся расход успешно добавлен.")
            else:
                self.expenses.append({"amount": amount, "description": description, "category": category, "date": date})
                self.update_status("Расход успешно добавлен.")

            self.save_data()
            self.generate_report()
            self.plot_charts()
            self.clear_entries()

        except ValueError:
            self.update_status("Неверный формат суммы расхода или повторений. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при добавлении расхода: {e}", is_error=True)

    def set_budget(self):
        try:
            category = self.budget_category_entry.get().lower()
            amount_str = self.budget_amount_entry.get()

            if category == "например: еда, транспорт" or amount_str == "Например: 500.00":
                self.update_status("Пожалуйста, заполните все поля бюджета.", is_error=True)
                return

            amount = float(amount_str)
            if amount < 0:
                self.update_status("Сумма бюджета не может быть отрицательной.", is_error=True)
                return

            self.budget[category] = amount
            self.save_data()
            self.generate_report()
            self.plot_charts()
            self.clear_entries()
            self.update_status(f"Бюджет для категории '{category}' установлен на {amount:.2f}.")
        except ValueError:
            self.update_status("Неверный формат суммы бюджета. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при установке бюджета: {e}", is_error=True)

    def load_entry_for_edit(self):
        try:
            index_str = self.edit_index_entry.get()
            entry_type = self.edit_type_var.get()

            if index_str == "Введите индекс из отчета":
                self.update_status("Пожалуйста, введите индекс записи для редактирования.", is_error=True)
                return

            index = int(index_str) - 1

            data_list = []
            list_name = ""

            if entry_type == "income":
                data_list = self.incomes
                list_name = "доходов"
            elif entry_type == "expense":
                data_list = self.expenses
                list_name = "расходов"
            elif entry_type == "recurring_income":
                data_list = self.recurring_incomes
                list_name = "повторяющихся доходов"
            elif entry_type == "recurring_expense":
                data_list = self.recurring_expenses
                list_name = "повторяющихся расходов"

            if not (0 <= index < len(data_list)):
                self.update_status(f"Неверный индекс записи {list_name}.", is_error=True)
                return

            record = data_list[index]
            self.editing_record_info = {'type': entry_type, 'index': index}

            self.clear_entries()

            if entry_type == "income":
                self.income_amount_entry.delete(0, tk.END)
                self.income_amount_entry.insert(0, str(record['amount']))
                self.income_amount_entry.config(style='TEntry')

                self.income_description_entry.delete(0, tk.END)
                self.income_description_entry.insert(0, record['description'])
                self.income_description_entry.config(style='TEntry')

                self.income_date_entry.delete(0, tk.END)
                self.income_date_entry.insert(0, record['date'])
                self.income_date_entry.config(style='TEntry')

            elif entry_type == "expense":
                self.expense_amount_entry.delete(0, tk.END)
                self.expense_amount_entry.insert(0, str(record['amount']))
                self.expense_amount_entry.config(style='TEntry')

                self.expense_description_entry.delete(0, tk.END)
                self.expense_description_entry.insert(0, record['description'])
                self.expense_description_entry.config(style='TEntry')

                self.expense_category_entry.delete(0, tk.END)
                self.expense_category_entry.insert(0, record['category'])
                self.expense_category_entry.config(style='TEntry')

                self.expense_date_entry.delete(0, tk.END)
                self.expense_date_entry.insert(0, record['date'])
                self.expense_date_entry.config(style='TEntry')

            elif entry_type == "recurring_income":
                self.is_recurring_income.set(True)
                self.toggle_recurring_income_options()

                self.income_amount_entry.delete(0, tk.END)
                self.income_amount_entry.insert(0, str(record['amount']))
                self.income_amount_entry.config(style='TEntry')

                self.income_description_entry.delete(0, tk.END)
                self.income_description_entry.insert(0, record['description'])
                self.income_description_entry.config(style='TEntry')

                self.income_date_entry.delete(0, tk.END)
                self.income_date_entry.insert(0, record['start_date'])
                self.income_date_entry.config(style='TEntry')

                self.income_frequency_combobox.set(record['frequency'])
                self.income_repetitions_entry.delete(0, tk.END)
                self.income_repetitions_entry.insert(0, str(record['repetitions']))
                self.income_repetitions_entry.config(style='TEntry')

            elif entry_type == "recurring_expense":
                self.is_recurring_expense.set(True)
                self.toggle_recurring_expense_options()

                self.expense_amount_entry.delete(0, tk.END)
                self.expense_amount_entry.insert(0, str(record['amount']))
                self.expense_amount_entry.config(style='TEntry')

                self.expense_description_entry.delete(0, tk.END)
                self.expense_description_entry.insert(0, record['description'])
                self.expense_description_entry.config(style='TEntry')

                self.expense_category_entry.delete(0, tk.END)
                self.expense_category_entry.insert(0, record['category'])
                self.expense_category_entry.config(style='TEntry')

                self.expense_date_entry.delete(0, tk.END)
                self.expense_date_entry.insert(0, record['start_date'])
                self.expense_date_entry.config(style='TEntry')

                self.expense_frequency_combobox.set(record['frequency'])
                self.expense_repetitions_entry.delete(0, tk.END)
                self.expense_repetitions_entry.insert(0, str(record['repetitions']))
                self.expense_repetitions_entry.config(style='TEntry')

            self.save_edit_button.config(state=tk.NORMAL)
            self.load_edit_button.config(state=tk.DISABLED)
            self.update_status(f"Запись №{index + 1} ({list_name}) загружена для редактирования.")

        except ValueError:
            self.update_status("Неверный формат индекса. Используйте целые числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при загрузке записи для редактирования: {e}", is_error=True)

    def save_edited_entry(self):
        if not self.editing_record_info:
            self.update_status("Нет записи для сохранения. Сначала загрузите запись для редактирования.", is_error=True)
            return

        entry_type = self.editing_record_info['type']
        index = self.editing_record_info['index']

        try:
            if entry_type == "income":
                new_amount_str = self.income_amount_entry.get()
                new_description = self.income_description_entry.get()
                new_date = self.income_date_entry.get()

                if new_amount_str == "Введите сумму" or new_description == "Например: Зарплата, подарок" or new_date == "Например: 2025-06-21":
                    self.update_status("Заполните все поля дохода для сохранения изменений.", is_error=True)
                    return

                new_amount = float(new_amount_str)
                if new_amount <= 0:
                    self.update_status("Новая сумма дохода должна быть положительным числом.", is_error=True)
                    return
                if not self.validate_date(new_date):
                    self.update_status("Новая дата дохода имеет неверный формат.", is_error=True)
                    return

                self.incomes[index]["amount"] = new_amount
                self.incomes[index]["description"] = new_description
                self.incomes[index]["date"] = new_date
                list_name = "доходов"
            elif entry_type == "expense":
                new_amount_str = self.expense_amount_entry.get()
                new_description = self.expense_description_entry.get()
                new_category = self.expense_category_entry.get().lower()
                new_date = self.expense_date_entry.get()

                if new_amount_str == "Введите сумму" or new_description == "Например: Продукты, такси" or new_category == "Например: еда, транспорт" or new_date == "Например: 2025-06-21":
                    self.update_status("Заполните все поля расхода для сохранения изменений.", is_error=True)
                    return

                new_amount = float(new_amount_str)
                if new_amount <= 0:
                    self.update_status("Новая сумма расхода должна быть положительным числом.", is_error=True)
                    return
                if not self.validate_date(new_date):
                    self.update_status("Новая дата расхода имеет неверный формат.", is_error=True)
                    return

                self.expenses[index]["amount"] = new_amount
                self.expenses[index]["description"] = new_description
                self.expenses[index]["category"] = new_category
                self.expenses[index]["date"] = new_date
                list_name = "расходов"
            elif entry_type == "recurring_income":
                new_amount_str = self.income_amount_entry.get()
                new_description = self.income_description_entry.get()
                new_start_date = self.income_date_entry.get()
                new_frequency = self.income_frequency_combobox.get()
                new_repetitions_str = self.income_repetitions_entry.get()

                if new_amount_str == "Введите сумму" or new_description == "Например: Зарплата, подарок" or new_start_date == "Например: 2025-06-21" or new_repetitions_str == "Количество повторений":
                    self.update_status("Заполните все поля повторяющегося дохода для сохранения изменений.",
                                       is_error=True)
                    return

                new_amount = float(new_amount_str)
                if new_amount <= 0:
                    self.update_status("Новая сумма повторяющегося дохода должна быть положительным числом.",
                                       is_error=True)
                    return
                if not self.validate_date(new_start_date):
                    self.update_status("Новая начальная дата повторяющегося дохода имеет неверный формат.",
                                       is_error=True)
                    return
                if not new_repetitions_str.isdigit() or int(new_repetitions_str) <= 0:
                    self.update_status("Новое количество повторений должно быть целым положительным числом.",
                                       is_error=True)
                    return
                new_repetitions = int(new_repetitions_str)

                self.recurring_incomes[index]["amount"] = new_amount
                self.recurring_incomes[index]["description"] = new_description
                self.recurring_incomes[index]["start_date"] = new_start_date
                self.recurring_incomes[index]["frequency"] = new_frequency
                self.recurring_incomes[index]["repetitions"] = new_repetitions
                list_name = "повторяющихся доходов"

            elif entry_type == "recurring_expense":
                new_amount_str = self.expense_amount_entry.get()
                new_description = self.expense_description_entry.get()
                new_category = self.expense_category_entry.get().lower()
                new_start_date = self.expense_date_entry.get()
                new_frequency = self.expense_frequency_combobox.get()
                new_repetitions_str = self.expense_repetitions_entry.get()

                if new_amount_str == "Введите сумму" or new_description == "Например: Продукты, такси" or new_category == "Например: еда, транспорт" or new_start_date == "Например: 2025-06-21" or new_repetitions_str == "Количество повторений":
                    self.update_status("Заполните все поля повторяющегося расхода для сохранения изменений.",
                                       is_error=True)
                    return

                new_amount = float(new_amount_str)
                if new_amount <= 0:
                    self.update_status("Новая сумма повторяющегося расхода должна быть положительным числом.",
                                       is_error=True)
                    return
                if not self.validate_date(new_start_date):
                    self.update_status("Новая начальная дата повторяющегося расхода имеет неверный формат.",
                                       is_error=True)
                    return
                if not new_repetitions_str.isdigit() or int(new_repetitions_str) <= 0:
                    self.update_status("Новое количество повторений должно быть целым положительным числом.",
                                       is_error=True)
                    return
                new_repetitions = int(new_repetitions_str)

                self.recurring_expenses[index]["amount"] = new_amount
                self.recurring_expenses[index]["description"] = new_description
                self.recurring_expenses[index]["category"] = new_category
                self.recurring_expenses[index]["start_date"] = new_start_date
                self.recurring_expenses[index]["frequency"] = new_frequency
                self.recurring_expenses[index]["repetitions"] = new_repetitions
                list_name = "повторяющихся расходов"

            self.save_data()
            self.generate_report()
            self.plot_charts()
            self.clear_entries()
            self.update_status(f"Запись типа '{entry_type}' по индексу {index + 1} успешно отредактирована.")

        except ValueError:
            self.update_status("Неверный формат суммы. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при сохранении отредактированной записи: {e}", is_error=True)

    def delete_entry(self):
        try:
            index_str = self.edit_index_entry.get()
            entry_type = self.edit_type_var.get()

            if index_str == "Введите индекс из отчета":
                self.update_status("Пожалуйста, введите индекс записи для удаления.", is_error=True)
                return

            index = int(index_str) - 1

            data_list = []
            list_name = ""

            if entry_type == "income":
                data_list = self.incomes
                list_name = "доходов"
            elif entry_type == "expense":
                data_list = self.expenses
                list_name = "расходов"
            elif entry_type == "recurring_income":
                data_list = self.recurring_incomes
                list_name = "повторяющихся доходов"
            elif entry_type == "recurring_expense":
                data_list = self.recurring_expenses
                list_name = "повторяющихся расходов"

            if not (0 <= index < len(data_list)):
                self.update_status(f"Неверный индекс записи {list_name}.", is_error=True)
                return

            if messagebox.askyesno("Подтверждение удаления",
                                   f"Вы уверены, что хотите удалить запись №{index + 1} из {list_name}?"):
                del data_list[index]
                self.save_data()
                self.generate_report()
                self.plot_charts()
                self.clear_entries()
                self.update_status(f"Запись №{index + 1} из {list_name} успешно удалена.")
            else:
                self.update_status("Удаление отменено.")

        except ValueError:
            self.update_status("Неверный формат индекса. Используйте целые числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при удалении записи: {e}", is_error=True)

    def clear_all_data(self):
        if messagebox.askyesno("Подтверждение очистки",
                               "Вы уверены, что хотите полностью удалить ВСЕ данные (доходы, расходы, бюджет)? Это действие необратимо!"):
            self.incomes = []
            self.expenses = []
            self.budget = {}
            self.recurring_incomes = []
            self.recurring_expenses = []
            self.goals = []
            if os.path.exists(DATA_FILE):
                try:
                    os.remove(DATA_FILE)
                except OSError as e:
                    messagebox.showerror("Ошибка", f"Не удалось удалить файл данных: {e}")
                    self.update_status(f"Ошибка при удалении файла данных: {e}", is_error=True)
            self.save_data()
            self.generate_report()
            self.plot_charts()
            self.clear_entries()
            self.update_goals_display()
            self.update_status("Все данные успешно очищены.")
        else:
            self.update_status("Очистка данных отменена.")

    def export_to_csv(self):
        try:
            output_dir = filedialog.askdirectory(title="Выберите папку для сохранения CSV")
            if not output_dir:
                self.update_status("Экспорт отменен: Папка не выбрана.", is_error=True)
                return

            incomes_csv_path = os.path.join(output_dir, "incomes_onetime.csv")
            with open(incomes_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = ['amount', 'description', 'date']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                for income in self.incomes:
                    writer.writerow(income)

            expenses_csv_path = os.path.join(output_dir, "expenses_onetime.csv")
            with open(expenses_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = ['amount', 'description', 'category', 'date']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                for expense in self.expenses:
                    writer.writerow(expense)

            recurring_incomes_csv_path = os.path.join(output_dir, "incomes_recurring.csv")
            with open(recurring_incomes_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = ['amount', 'description', 'start_date', 'frequency', 'repetitions']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                for rec_income in self.recurring_incomes:
                    writer.writerow(rec_income)

            recurring_expenses_csv_path = os.path.join(output_dir, "expenses_recurring.csv")
            with open(recurring_expenses_csv_path, 'w', newline='', encoding='utf-8') as csvfile:
                fieldnames = ['amount', 'description', 'category', 'start_date', 'frequency', 'repetitions']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                for rec_expense in self.recurring_expenses:
                    writer.writerow(rec_expense)

            self.update_status(
                f"Данные успешно экспортированы в:\n{incomes_csv_path}\n{expenses_csv_path}\n{recurring_incomes_csv_path}\n{recurring_expenses_csv_path}")

        except Exception as e:
            self.update_status(f"Ошибка при экспорте данных в CSV: {e}", is_error=True)

    def apply_filters(self):
        search_keyword = self.filter_description_entry.get()
        if search_keyword == "Ключевое слово": search_keyword = ""

        filter_category = self.filter_category_entry.get().lower()
        if filter_category == "например: еда": filter_category = ""

        filter_month = self.filter_month_entry.get()
        if filter_month == "Например: 2025-06": filter_month = ""

        filter_year = self.filter_year_entry.get()
        if filter_year == "Например: 2025": filter_year = ""

        self.generate_report(search_keyword, filter_category, filter_month, filter_year)
        self.update_status("Отчет отфильтрован.")

    def reset_filters(self):
        self.filter_description_entry.delete(0, tk.END)
        self.add_placeholder(self.filter_description_entry, "Ключевое слово")
        self.filter_category_entry.delete(0, tk.END)
        self.add_placeholder(self.filter_category_entry, "Например: еда")
        self.filter_month_entry.delete(0, tk.END)
        self.add_placeholder(self.filter_month_entry, "Например: 2025-06")
        self.filter_year_entry.delete(0, tk.END)
        self.add_placeholder(self.filter_year_entry, "Например: 2025")
        self.generate_report()
        self.update_status("Фильтры сброшены, отчет обновлен.")

    def get_effective_transactions(self, month_year_filter=None, year_filter=None):
        effective_incomes = list(self.incomes)
        effective_expenses = list(self.expenses)

        today = datetime.now()

        for rec_income in self.recurring_incomes:
            start_date = datetime.strptime(rec_income['start_date'], '%Y-%m-%d')
            for i in range(rec_income['repetitions']):
                transaction_date = start_date + timedelta(days=30 * i)
                if transaction_date.year > today.year or (
                        transaction_date.year == today.year and transaction_date.month > today.month):
                    break

                if month_year_filter and not transaction_date.strftime('%Y-%m').startswith(month_year_filter):
                    continue
                if year_filter and not str(transaction_date.year).startswith(year_filter):
                    continue

                effective_incomes.append({
                    "amount": rec_income['amount'],
                    "description": f"{rec_income['description']} (Повтор)",
                    "date": transaction_date.strftime('%Y-%m-%d')
                })

        for rec_expense in self.recurring_expenses:
            start_date = datetime.strptime(rec_expense['start_date'], '%Y-%m-%d')
            for i in range(rec_expense['repetitions']):
                transaction_date = start_date + timedelta(days=30 * i)
                if transaction_date.year > today.year or (
                        transaction_date.year == today.year and transaction_date.month > today.month):
                    break

                if month_year_filter and not transaction_date.strftime('%Y-%m').startswith(month_year_filter):
                    continue
                if year_filter and not str(transaction_date.year).startswith(year_filter):
                    continue

                effective_expenses.append({
                    "amount": rec_expense['amount'],
                    "description": f"{rec_expense['description']} (Повтор)",
                    "category": rec_expense['category'],
                    "date": transaction_date.strftime('%Y-%m-%d')
                })

        return effective_incomes, effective_expenses

    def generate_report(self, search_keyword="", filter_category="", filter_month="", filter_year=""):
        self.report_text.config(state="normal")
        self.report_text.delete(1.0, tk.END)

        report_content = []
        report_content.append("===== ОБЩИЙ ФИНАНСОВЫЙ ОТЧЕТ =====")
        report_content.append(f"Дата отчета: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")

        effective_incomes, effective_expenses = self.get_effective_transactions()

        filtered_incomes = []
        for income in effective_incomes:
            match = True
            if search_keyword and search_keyword.lower() not in income['description'].lower():
                match = False
            if filter_month and not income['date'].startswith(filter_month):
                match = False
            if filter_year and not income['date'].startswith(filter_year):
                match = False
            if match:
                filtered_incomes.append(income)

        filtered_expenses = []
        for expense in effective_expenses:
            match = True
            if search_keyword and search_keyword.lower() not in expense['description'].lower():
                match = False
            if filter_category and expense['category'].lower() != filter_category.lower():
                match = False
            if filter_month and not expense['date'].startswith(filter_month):
                match = False
            if filter_year and not expense['date'].startswith(filter_year):
                match = False
            if match:
                filtered_expenses.append(expense)

        total_income = sum(item['amount'] for item in filtered_incomes)
        total_expense = sum(item['amount'] for item in filtered_expenses)
        current_balance = total_income - total_expense

        report_content.append(f"Общая сумма доходов (отфильтровано): {total_income:.2f}")
        report_content.append(f"Общая сумма расходов (отфильтровано): {total_expense:.2f}")
        report_content.append(f"Текущий баланс: {current_balance:.2f}\n")

        report_content.append("===== РАСХОДЫ ПО КАТЕГОРИЯМ =====")
        expenses_by_category = defaultdict(float)
        for expense in filtered_expenses:
            expenses_by_category[expense['category']] += expense['amount']

        if expenses_by_category:
            for category, amount in sorted(expenses_by_category.items()):
                report_content.append(f"- {category.capitalize()}: {amount:.2f}")
        else:
            report_content.append("Нет отфильтрованных расходов по категориям.")
        report_content.append("\n")

        report_content.append("===== СТАТУС БЮДЖЕТА =====")
        if self.budget:
            for category, limit in sorted(self.budget.items()):
                spent = expenses_by_category.get(category, 0.0)
                remaining = limit - spent
                status = "осталось" if remaining >= 0 else "превышен"
                report_content.append(
                    f"- {category.capitalize()}: Лимит: {limit:.2f}, Потрачено: {spent:.2f}, {status}: {remaining:.2f}")
        else:
            report_content.append("Бюджет не установлен для каких-либо категорий.")
        report_content.append("\n")

        report_content.append("===== СПИСОК РАЗОВЫХ ДОХОДОВ =====")
        onetime_incomes_filtered = [inc for inc in filtered_incomes if "(Повтор)" not in inc.get('description', '')]
        if onetime_incomes_filtered:
            for i, income in enumerate(onetime_incomes_filtered):
                report_content.append(
                    f"{i + 1}. Дата: {income['date']}, Сумма: {income['amount']:.2f}, Описание: {income['description']}")
        else:
            report_content.append("Нет отфильтрованных разовых записей о доходах.")
        report_content.append("\n")

        report_content.append("===== СПИСОК РАЗОВЫХ РАСХОДОВ =====")
        onetime_expenses_filtered = [exp for exp in filtered_expenses if "(Повтор)" not in exp.get('description', '')]
        if onetime_expenses_filtered:
            for i, expense in enumerate(onetime_expenses_filtered):
                report_content.append(
                    f"{i + 1}. Дата: {expense['date']}, Сумма: {expense['amount']:.2f}, Категория: {expense['category'].capitalize()}, Описание: {expense['description']}")
        else:
            report_content.append("Нет отфильтрованных разовых записей о расходах.")
        report_content.append("\n")

        report_content.append("===== СПИСОК ПОВТОРЯЮЩИХСЯ ДОХОДОВ =====")
        if self.recurring_incomes:
            for i, rec_income in enumerate(self.recurring_incomes):
                report_content.append(
                    f"{i + 1}. Нач. дата: {rec_income['start_date']}, Сумма: {rec_income['amount']:.2f}, Описание: {rec_income['description']}, Частота: {rec_income['frequency']}, Повторений: {rec_income['repetitions']}")
        else:
            report_content.append("Нет повторяющихся записей о доходах.")
        report_content.append("\n")

        report_content.append("===== СПИСОК ПОВТОРЯЮЩИХСЯ РАСХОДОВ =====")
        if self.recurring_expenses:
            for i, rec_expense in enumerate(self.recurring_expenses):
                report_content.append(
                    f"{i + 1}. Нач. дата: {rec_expense['start_date']}, Сумма: {rec_expense['amount']:.2f}, Категория: {rec_expense['category'].capitalize()}, Описание: {rec_expense['description']}, Частота: {rec_expense['frequency']}, Повторений: {rec_expense['repetitions']}")
        else:
            report_content.append("Нет повторяющихся записей о расходах.")
        report_content.append("\n")

        self.report_text.insert(tk.END, "\n".join(report_content))
        self.report_text.config(state="disabled")

    def calculate_monthly_summary(self):
        monthly_data = defaultdict(lambda: {'income': 0.0, 'expense': 0.0, 'balance': 0.0})

        effective_incomes, effective_expenses = self.get_effective_transactions()

        for income in effective_incomes:
            month_year = income['date'][:7]
            monthly_data[month_year]['income'] += income['amount']

        for expense in effective_expenses:
            month_year = expense['date'][:7]
            monthly_data[month_year]['expense'] += expense['amount']

        sorted_months = sorted(monthly_data.keys())
        for month in sorted_months:
            monthly_data[month]['balance'] = monthly_data[month]['income'] - monthly_data[month]['expense']

        return sorted_months, monthly_data

    def plot_charts(self):
        self.ax1.clear()
        self.ax2.clear()

        effective_incomes, effective_expenses = self.get_effective_transactions()
        expenses_by_category = defaultdict(float)
        for expense in effective_expenses:
            expenses_by_category[expense['category']] += expense['amount']

        if expenses_by_category:
            categories = list(expenses_by_category.keys())
            amounts = list(expenses_by_category.values())

            sorted_data = sorted(zip(categories, amounts), key=lambda x: x[1], reverse=True)
            sorted_categories = [x[0].capitalize() for x in sorted_data]
            sorted_amounts = [x[1] for x in sorted_data]

            self.ax1.bar(sorted_categories, sorted_amounts, color='#66BB6A')
            self.ax1.set_title("Расходы по категориям (включая повторяющиеся)", fontsize=12)
            self.ax1.set_ylabel("Сумма", fontsize=10)
            self.ax1.tick_params(axis='x', rotation=45)
            self.ax1.grid(axis='y', linestyle='--', alpha=0.7)
        else:
            self.ax1.text(0.5, 0.5, "Нет данных о расходах для отображения",
                          horizontalalignment='center', verticalalignment='center',
                          transform=self.ax1.transAxes, fontsize=10, color='grey')
            self.ax1.set_title("Расходы по категориям", fontsize=12)

        sorted_months, monthly_data = self.calculate_monthly_summary()

        if monthly_data:
            dates = []
            balances = []
            for month in sorted_months:
                dates.append(month)
                balances.append(monthly_data[month]['balance'])

            self.ax2.plot(dates, balances, marker='o', linestyle='-', color='#2196F3')
            self.ax2.axhline(0, color='grey', linestyle='--', linewidth=0.8)
            self.ax2.set_title("Динамика баланса (по месяцам, включая повторяющиеся)", fontsize=12)
            self.ax2.set_xlabel("Месяц", fontsize=10)
            self.ax2.set_ylabel("Баланс", fontsize=10)
            self.ax2.tick_params(axis='x', rotation=45)
            self.ax2.grid(True, linestyle='--', alpha=0.7)
        else:
            self.ax2.text(0.5, 0.5, "Нет данных о доходах/расходах для динамики",
                          horizontalalignment='center', verticalalignment='center',
                          transform=self.ax2.transAxes, fontsize=10, color='grey')
            self.ax2.set_title("Динамика баланса (по месяцам)", fontsize=12)

        self.fig.tight_layout()
        self.chart_canvas.draw()

        self.update_monthly_summary_text(sorted_months, monthly_data)
        self.generate_forecast()

    def update_monthly_summary_text(self, sorted_months, monthly_data):
        self.monthly_summary_text.config(state="normal")
        self.monthly_summary_text.delete(1.0, tk.END)

        summary_lines = ["", "ЕЖЕМЕСЯЧНАЯ СВОДКА:", "-------------------"]
        if monthly_data:
            for month in sorted_months:
                data = monthly_data[month]
                summary_lines.append(
                    f"  {month}: Доход: {data['income']:.2f}, Расход: {data['expense']:.2f}, Баланс: {data['balance']:.2f}")
        else:
            summary_lines.append("  Нет данных для ежемесячной сводки.")

        yearly_data = defaultdict(lambda: {'income': 0.0, 'expense': 0.0, 'balance': 0.0})
        for month_year, data in monthly_data.items():
            year = month_year[:4]
            yearly_data[year]['income'] += data['income']
            yearly_data[year]['expense'] += data['expense']
            yearly_data[year]['balance'] += data['balance']

        summary_lines.append("\nЕЖЕГОДНАЯ СВОДКА:")
        summary_lines.append("-------------------")
        sorted_years = sorted(yearly_data.keys())
        if yearly_data:
            for year in sorted_years:
                data = yearly_data[year]
                summary_lines.append(
                    f"  {year}: Доход: {data['income']:.2f}, Расход: {data['expense']:.2f}, Баланс: {data['balance']:.2f}")
        else:
            summary_lines.append("  Нет данных для ежегодной сводки.")

        self.monthly_summary_text.insert(tk.END, "\n".join(summary_lines))
        self.monthly_summary_text.config(state="disabled")

    def generate_forecast(self, num_months=6):
        self.forecast_text.config(state="normal")
        self.forecast_text.delete(1.0, tk.END)

        forecast_content = ["", "ПРОГНОЗ БАЛАНСА:", "-------------------\n"]

        total_income = sum(item['amount'] for item in self.incomes)
        total_expense = sum(item['amount'] for item in self.expenses)
        current_balance = total_income - total_expense

        forecast_content.append(f"Текущий баланс: {current_balance:.2f}\n")

        projected_balance = current_balance
        today = datetime.now()

        for i in range(1, num_months + 1):
            future_date = today + timedelta(days=30 * i)
            forecast_month_year = future_date.strftime('%Y-%m')

            monthly_projected_income = 0
            monthly_projected_expense = 0

            for rec_income in self.recurring_incomes:
                start_date = datetime.strptime(rec_income['start_date'], '%Y-%m-%d')

                if (start_date.year < future_date.year or
                        (start_date.year == future_date.year and start_date.month <= future_date.month)):

                    months_since_start = (future_date.year - start_date.year) * 12 + (
                                future_date.month - start_date.month)

                    if months_since_start < rec_income['repetitions']:
                        monthly_projected_income += rec_income['amount']

            for rec_expense in self.recurring_expenses:
                start_date = datetime.strptime(rec_expense['start_date'], '%Y-%m-%d')

                if (start_date.year < future_date.year or
                        (start_date.year == future_date.year and start_date.month <= future_date.month)):

                    months_since_start = (future_date.year - start_date.year) * 12 + (
                                future_date.month - start_date.month)

                    if months_since_start < rec_expense['repetitions']:
                        monthly_projected_expense += rec_expense['amount']

            projected_balance += (monthly_projected_income - monthly_projected_expense)
            forecast_content.append(
                f"  {forecast_month_year}: Прогнозируемый баланс: {projected_balance:.2f} (Доход: {monthly_projected_income:.2f}, Расход: {monthly_projected_expense:.2f})")

        self.forecast_text.insert(tk.END, "\n".join(forecast_content))
        self.forecast_text.config(state="disabled")

    def add_goal(self):
        try:
            name = self.goal_name_entry.get()
            target_amount_str = self.goal_target_amount_entry.get()
            current_amount_str = self.goal_current_amount_entry.get()
            due_date = self.goal_due_date_entry.get()

            if name == "Название цели" or not name:
                self.update_status("Пожалуйста, введите название цели.", is_error=True)
                return
            if target_amount_str == "Целевая сумма" or not target_amount_str:
                self.update_status("Пожалуйста, введите целевую сумму.", is_error=True)
                return
            if current_amount_str == "Текущая сумма" or not current_amount_str:
                self.update_status("Пожалуйста, введите текущую сумму.", is_error=True)
                return
            if due_date == "Дата завершения (ГГГГ-ММ-ДД)" or not due_date:
                self.update_status("Пожалуйста, введите дату завершения цели.", is_error=True)
                return

            target_amount = float(target_amount_str)
            current_amount = float(current_amount_str)

            if target_amount <= 0 or current_amount < 0:
                self.update_status("Целевая и текущая суммы должны быть положительными числами (текущая может быть 0).",
                                   is_error=True)
                return
            if not self.validate_date(due_date):
                self.update_status("Неверный формат даты завершения. Используйте ГГГГ-ММ-ДД.", is_error=True)
                return

            if any(g['name'].lower() == name.lower() for g in self.goals):
                self.update_status(
                    f"Цель с названием '{name}' уже существует. Используйте другое название или обновите существующую цель.",
                    is_error=True)
                return

            self.goals.append({
                "name": name,
                "target_amount": target_amount,
                "current_amount": current_amount,
                "due_date": due_date
            })
            self.save_data()
            self.update_goals_display()
            self.clear_entries()
            self.update_status(f"Цель '{name}' успешно добавлена.")
        except ValueError:
            self.update_status("Неверный формат суммы для цели. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при добавлении цели: {e}", is_error=True)

    def update_goal(self):
        selected_item = self.goals_tree.selection()
        if not selected_item:
            self.update_status("Выберите цель для обновления.", is_error=True)
            return

        selected_goal_name = self.goals_tree.item(selected_item, 'values')[0]

        try:
            name = self.goal_name_entry.get()
            target_amount_str = self.goal_target_amount_entry.get()
            current_amount_str = self.goal_current_amount_entry.get()
            due_date = self.goal_due_date_entry.get()

            if name == "Название цели" or not name:
                self.update_status("Пожалуйста, введите название цели.", is_error=True)
                return
            if target_amount_str == "Целевая сумма" or not target_amount_str:
                self.update_status("Пожалуйста, введите целевую сумму.", is_error=True)
                return
            if current_amount_str == "Текущая сумма" or not current_amount_str:
                self.update_status("Пожалуйста, введите текущую сумму.", is_error=True)
                return
            if due_date == "Дата завершения (ГГГГ-ММ-ДД)" or not due_date:
                self.update_status("Пожалуйста, введите дату завершения цели.", is_error=True)
                return

            target_amount = float(target_amount_str)
            current_amount = float(current_amount_str)

            if target_amount <= 0 or current_amount < 0:
                self.update_status("Целевая и текущая суммы должны быть положительными числами (текущая может быть 0).",
                                   is_error=True)
                return
            if not self.validate_date(due_date):
                self.update_status("Неверный формат даты завершения. Используйте ГГГГ-ММ-ДД.", is_error=True)
                return

            found = False
            for i, goal in enumerate(self.goals):
                if goal['name'] == selected_goal_name:
                    self.goals[i]["name"] = name
                    self.goals[i]["target_amount"] = target_amount
                    self.goals[i]["current_amount"] = current_amount
                    self.goals[i]["due_date"] = due_date
                    found = True
                    break

            if not found:
                self.update_status(f"Цель '{selected_goal_name}' не найдена.", is_error=True)
                return

            self.save_data()
            self.update_goals_display()
            self.clear_entries()
            self.update_status(f"Цель '{name}' успешно обновлена.")
        except ValueError:
            self.update_status("Неверный формат суммы для цели. Используйте числа.", is_error=True)
        except Exception as e:
            self.update_status(f"Произошла ошибка при обновлении цели: {e}", is_error=True)

    def delete_goal(self):
        selected_item = self.goals_tree.selection()
        if not selected_item:
            self.update_status("Выберите цель для удаления.", is_error=True)
            return

        selected_goal_name = self.goals_tree.item(selected_item, 'values')[0]

        if messagebox.askyesno("Подтверждение удаления",
                               f"Вы уверены, что хотите удалить цель '{selected_goal_name}'?"):
            self.goals = [goal for goal in self.goals if goal['name'] != selected_goal_name]
            self.save_data()
            self.update_goals_display()
            self.clear_entries()
            self.update_status(f"Цель '{selected_goal_name}' успешно удалена.")
        else:
            self.update_status("Удаление цели отменено.")

    def update_goals_display(self):
        for i in self.goals_tree.get_children():
            self.goals_tree.delete(i)

        for goal in self.goals:
            progress = (goal['current_amount'] / goal['target_amount']) * 100 if goal['target_amount'] > 0 else 0
            remaining = goal['target_amount'] - goal['current_amount']

            tag = ''
            if progress >= 100:
                tag = 'completed'
            elif datetime.strptime(goal['due_date'], '%Y-%m-%d') < datetime.now():
                tag = 'overdue'

            self.goals_tree.insert("", "end", values=(
                goal['name'],
                f"{goal['target_amount']:.2f}",
                f"{goal['current_amount']:.2f}",
                f"{progress:.1f}%",
                f"{remaining:.2f}",
                goal['due_date']
            ), tags=(tag,))

        self.goals_tree.tag_configure('completed', background='#d4edda', foreground='#155724')
        self.goals_tree.tag_configure('overdue', background='#f8d7da', foreground='#721c24')

    def load_goal_for_edit(self, event):
        selected_item = self.goals_tree.selection()
        if selected_item:
            goal_name = self.goals_tree.item(selected_item, 'values')[0]
            selected_goal = next((g for g in self.goals if g['name'] == goal_name), None)
            if selected_goal:
                self.goal_name_entry.delete(0, tk.END)
                self.goal_name_entry.insert(0, selected_goal['name'])
                self.goal_name_entry.config(style='TEntry')

                self.goal_target_amount_entry.delete(0, tk.END)
                self.goal_target_amount_entry.insert(0, str(selected_goal['target_amount']))
                self.goal_target_amount_entry.config(style='TEntry')

                self.goal_current_amount_entry.delete(0, tk.END)
                self.goal_current_amount_entry.insert(0, str(selected_goal['current_amount']))
                self.goal_current_amount_entry.config(style='TEntry')

                self.goal_due_date_entry.delete(0, tk.END)
                self.goal_due_date_entry.insert(0, selected_goal['due_date'])
                self.goal_due_date_entry.config(style='TEntry')
                self.update_status(f"Цель '{goal_name}' загружена для редактирования.")


if __name__ == "__main__":
    root = tk.Tk()
    app = BudgetApp(root)
    root.mainloop()
