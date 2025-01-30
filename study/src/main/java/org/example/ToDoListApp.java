package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

class Task implements Serializable {
    String description;
    boolean completed;
    int priority;

    Task(String description, int priority) {
        this.description = description;
        this.completed = false;
        this.priority = priority;
    }
}

class ToDoList {
    private ArrayList<Task> tasks;
    private static final String FILE_NAME = "tasks.ser";

    ToDoList() {
        tasks = loadTasks();
    }

    void addTask(String description, int priority) {
        tasks.add(new Task(description, priority));
        saveTasks();
    }

    ArrayList<Task> getTasks() {
        tasks.sort(Comparator.comparing((Task t) -> t.completed).thenComparingInt(t -> t.priority));
        return tasks;
    }

    void completeTask(int taskIndex) {
        if (taskIndex >= 0 && taskIndex < tasks.size()) {
            tasks.get(taskIndex).completed = true;
            saveTasks();
        }
    }

    void uncompleteTask(int taskIndex) {
        if (taskIndex >= 0 && taskIndex < tasks.size()) {
            tasks.get(taskIndex).completed = false;
            saveTasks();
        }
    }

    void deleteTask(int taskIndex) {
        if (taskIndex >= 0 && taskIndex < tasks.size()) {
            tasks.remove(taskIndex);
            saveTasks();
        }
    }

    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Failed to save tasks: " + e.getMessage());
        }
    }

    private ArrayList<Task> loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (ArrayList<Task>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}

public class ToDoListApp extends JFrame {
    private final ToDoList toDoList;
    private final DefaultListModel<String> listModel;
    private final JList<String> taskList;

    public ToDoListApp() {
        toDoList = new ToDoList();
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);

        setTitle("To-Do List Application");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Task list panel
        JScrollPane scrollPane = new JScrollPane(taskList);
        taskList.setCellRenderer(new TaskRenderer());
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));

        JButton addButton = new JButton("Add Task");
        JButton completeButton = new JButton("Complete Task");
        JButton uncompleteButton = new JButton("Uncomplete Task");
        JButton deleteButton = new JButton("Delete Task");

        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(uncompleteButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add button action
        addButton.addActionListener(e -> {
            JTextField taskField = new JTextField();
            SpinnerNumberModel priorityModel = new SpinnerNumberModel(1, 1, 10, 1);
            JSpinner prioritySpinner = new JSpinner(priorityModel);
            JPanel panel = new JPanel(new GridLayout(2, 2));
            panel.add(new JLabel("Task Description:"));
            panel.add(taskField);
            panel.add(new JLabel("Priority (1-10):"));
            panel.add(prioritySpinner);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add Task", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String taskDescription = taskField.getText().trim();
                int priority = (int) prioritySpinner.getValue();
                if (!taskDescription.isEmpty()) {
                    toDoList.addTask(taskDescription, priority);
                    refreshTaskList();
                }
            }
        });

        // Complete button action
        completeButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                toDoList.completeTask(selectedIndex);
                refreshTaskList();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to complete.");
            }
        });

        // Uncomplete button action
        uncompleteButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                toDoList.uncompleteTask(selectedIndex);
                refreshTaskList();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to uncomplete.");
            }
        });

        // Delete button action
        deleteButton.addActionListener(e -> {
            int selectedIndex = taskList.getSelectedIndex();
            if (selectedIndex != -1) {
                toDoList.deleteTask(selectedIndex);
                refreshTaskList();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a task to delete.");
            }
        });

        refreshTaskList();
    }

    private void refreshTaskList() {
        listModel.clear();
        for (Task task : toDoList.getTasks()) {
            String status = task.completed ? "[x] " : "[ ] ";
            listModel.addElement(status + "(P" + task.priority + ") " + task.description);
        }
    }

    private static class TaskRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String task = (String) value;
            if (task.startsWith("[x]")) {
                label.setBackground(Color.YELLOW);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            } else {
                label.setBackground(Color.RED);
                label.setFont(label.getFont().deriveFont(Font.BOLD));
            }
            label.setOpaque(true);
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ToDoListApp app = new ToDoListApp();
            app.setVisible(true);
        });
    }
}
