package com.taskmanagement.backend.model;

/**
 * タスクのステータスを定義するEnum
 * 
 * 実務では、ステータスを文字列ではなくEnumで管理することで、
 * タイポ（誤入力）を防ぎ、コードの保守性を高めることができます。
 * 
 * 使用例：
 * Task task = new Task();
 * task.setStatus(TaskStatus.TODO); // 安全に設定できる
 * task.setStatus("TODO"); // これは型エラーになるため、誤入力を防げる
 */
public enum TaskStatus {
    /**
     * 未着手
     * タスクが作成されたが、まだ作業を開始していない状態
     */
    TODO("未着手"),

    /**
     * 進行中
     * タスクの作業を開始し、現在進めている状態
     */
    IN_PROGRESS("進行中"),

    /**
     * 完了
     * タスクの作業が完了した状態
     */
    DONE("完了");

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
    TaskStatus(String displayName) {
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