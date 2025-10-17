package com.taskmanagement.backend.model;

/**
 * タスクの優先度を定義するEnum
 * 
 * 実務では、優先度も文字列ではなくEnumで管理します。
 * これにより、誤った値の設定を防ぎ、コードの可読性を向上させます。
 * 
 * 使用例：
 * Task task = new Task();
 * task.setPriority(TaskPriority.HIGH); // 安全に設定できる
 * 
 * 注意点：
 * - Enumは順序を持つため、compareTo()で比較が可能です
 * - 例: TaskPriority.HIGH.compareTo(TaskPriority.LOW) > 0 （HIGHの方が高い）
 */
public enum TaskPriority {
    /**
     * 低優先度
     * 緊急性が低く、余裕があるときに取り組めばよいタスク
     */
    LOW("低"),

    /**
     * 中優先度
     * 通常の優先度で、期日内に完了すればよいタスク
     */
    MEDIUM("中"),

    /**
     * 高優先度
     * 緊急性が高く、優先的に取り組むべきタスク
     */
    HIGH("高");

    /**
     * 日本語表記
     * UIに表示する際に使用します
     */
    private final String displayName;

    /**
     * コンストラクタ
     * 
     * @param displayName 日本語表記
     */
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 日本語表記を取得
     * 
     * @return 日本語表記
     */
    public String getDisplayName() {
        return displayName;
    }
}