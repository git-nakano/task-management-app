package com.taskmanagement.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Taskエンティティの単体テスト
 * 
 * 実務では、エンティティのテストで以下を確認します：
 * 1. フィールドのgetterとsetterが正しく動作するか
 * 2. Enumフィールド（status、priority）が正しく動作するか
 * 3. 関連エンティティ（User）との関係が正しく設定できるか
 * 4. @PrePersist、@PreUpdateメソッドが正しく動作するか
 */
class TaskTest {

    private Task task;
    private User user;

    /**
     * 各テストメソッド実行前に呼ばれる初期化メソッド
     * 
     * 実務でのポイント：
     * - テストデータの初期化を@BeforeEachで行うことで、各テストが独立して実行できます
     * - Taskはユーザーに属するため、テスト用のUserオブジェクトも作成します
     */
    @BeforeEach
    void setUp() {
        // テスト用のユーザーを作成
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("テストユーザー");

        // テスト用のタスクを作成
        task = new Task();
        task.setId(1L);
        task.setTitle("テストタスク");
        task.setDescription("これはテスト用のタスクです");
        task.setDueDate(LocalDate.of(2025, 12, 31));
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setUser(user);
    }

    /**
     * Taskエンティティの基本的なフィールドが正しく設定・取得できることを確認
     */
    @Test
    void testTaskFields() {
        // ID
        assertEquals(1L, task.getId());

        // タイトル
        assertEquals("テストタスク", task.getTitle());

        // 詳細
        assertEquals("これはテスト用のタスクです", task.getDescription());

        // 期日
        assertEquals(LocalDate.of(2025, 12, 31), task.getDueDate());

        // ステータス
        assertEquals(TaskStatus.TODO, task.getStatus());

        // 優先度
        assertEquals(TaskPriority.HIGH, task.getPriority());

        // ユーザー
        assertEquals(user, task.getUser());
        assertEquals("テストユーザー", task.getUser().getUsername());
    }

    /**
     * TaskStatusのEnumが正しく動作することを確認
     * 
     * 実務でのポイント：
     * - Enumを使用することで、タイポを防ぎ、コードの安全性を高めます
     * - getDisplayName()で日本語表記が取得できることを確認します
     */
    @Test
    void testTaskStatus() {
        // TODO（未着手）
        task.setStatus(TaskStatus.TODO);
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals("未着手", task.getStatus().getDisplayName());

        // IN_PROGRESS（進行中）
        task.setStatus(TaskStatus.IN_PROGRESS);
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertEquals("進行中", task.getStatus().getDisplayName());

        // DONE（完了）
        task.setStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals("完了", task.getStatus().getDisplayName());
    }

    /**
     * TaskPriorityのEnumが正しく動作することを確認
     * 
     * 実務でのポイント：
     * - 優先度もEnumで管理することで、コードの安全性を高めます
     * - getDisplayName()で日本語表記が取得できることを確認します
     */
    @Test
    void testTaskPriority() {
        // LOW（低）
        task.setPriority(TaskPriority.LOW);
        assertEquals(TaskPriority.LOW, task.getPriority());
        assertEquals("低", task.getPriority().getDisplayName());

        // MEDIUM（中）
        task.setPriority(TaskPriority.MEDIUM);
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals("中", task.getPriority().getDisplayName());

        // HIGH（高）
        task.setPriority(TaskPriority.HIGH);
        assertEquals(TaskPriority.HIGH, task.getPriority());
        assertEquals("高", task.getPriority().getDisplayName());
    }

    /**
     * 優先度の比較が正しく動作することを確認
     * 
     * 実務でのポイント：
     * - Enumは順序を持つため、compareTo()で比較が可能です
     * - この機能を使って、タスクを優先度順にソートできます
     */
    @Test
    void testPriorityComparison() {
        // HIGHはMEDIUMより大きい（優先度が高い）
        assertTrue(TaskPriority.HIGH.compareTo(TaskPriority.MEDIUM) > 0);

        // MEDIUMはLOWより大きい（優先度が高い）
        assertTrue(TaskPriority.MEDIUM.compareTo(TaskPriority.LOW) > 0);

        // HIGHはLOWより大きい（優先度が高い）
        assertTrue(TaskPriority.HIGH.compareTo(TaskPriority.LOW) > 0);

        // 同じ優先度は等しい
        assertEquals(0, TaskPriority.HIGH.compareTo(TaskPriority.HIGH));
    }

    /**
     * @PrePersistメソッドで作成日時と更新日時が正しく設定されることを確認
     * 
     *                                         実務でのポイント：
     *                                         - デフォルト値（status = TODO、priority =
     *                                         MEDIUM）も自動設定されます
     */
    @Test
    void testOnCreate() {
        // デフォルト値を持たない新しいタスクを作成
        Task newTask = new Task();
        newTask.setTitle("新しいタスク");
        newTask.setUser(user);

        // 初期状態では作成日時と更新日時はnull
        assertNull(newTask.getCreatedAt());
        assertNull(newTask.getUpdatedAt());

        // 初期状態ではステータスと優先度はデフォルト値
        // （クラス定義で = TaskStatus.TODO、= TaskPriority.MEDIUM と設定済み）
        assertEquals(TaskStatus.TODO, newTask.getStatus());
        assertEquals(TaskPriority.MEDIUM, newTask.getPriority());

        // @PrePersistメソッドを手動で呼び出し
        newTask.onCreate();

        // 作成日時と更新日時が設定されていることを確認
        assertNotNull(newTask.getCreatedAt());
        assertNotNull(newTask.getUpdatedAt());

        // デフォルト値が保持されていることを確認
        assertEquals(TaskStatus.TODO, newTask.getStatus());
        assertEquals(TaskPriority.MEDIUM, newTask.getPriority());
    }

    /**
     * @PreUpdateメソッドで更新日時が正しく更新されることを確認
     */
    @Test
    void testOnUpdate() throws InterruptedException {
        // 初期状態を設定
        task.onCreate();
        LocalDateTime originalCreatedAt = task.getCreatedAt();
        LocalDateTime originalUpdatedAt = task.getUpdatedAt();

        // 少し待機（更新日時が変わることを確認するため）
        Thread.sleep(10);

        // @PreUpdateメソッドを手動で呼び出し
        task.onUpdate();

        // 作成日時は変更されていないことを確認
        assertEquals(originalCreatedAt, task.getCreatedAt());

        // 更新日時は変更されていることを確認
        assertTrue(task.getUpdatedAt().isAfter(originalUpdatedAt) ||
                task.getUpdatedAt().isEqual(originalUpdatedAt));
    }

    /**
     * 期日がnullでも問題なく動作することを確認
     * 
     * 実務でのポイント：
     * - 期日は任意項目なので、nullでも正常に動作する必要があります
     */
    @Test
    void testNullDueDate() {
        task.setDueDate(null);
        assertNull(task.getDueDate());

        // nullでもエラーにならないことを確認
        assertDoesNotThrow(() -> {
            task.onCreate();
            task.onUpdate();
        });
    }

    /**
     * 詳細がnullでも問題なく動作することを確認
     * 
     * 実務でのポイント：
     * - 詳細も任意項目なので、nullでも正常に動作する必要があります
     */
    @Test
    void testNullDescription() {
        task.setDescription(null);
        assertNull(task.getDescription());

        // nullでもエラーにならないことを確認
        assertDoesNotThrow(() -> {
            task.onCreate();
            task.onUpdate();
        });
    }

    /**
     * Userとの関連が正しく設定できることを確認
     * 
     * 実務でのポイント：
     * - @ManyToOne関係により、タスクは必ずユーザーに属します
     * - この関係が正しく設定できることを確認します
     */
    @Test
    void testUserRelation() {
        // 別のユーザーを作成
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@example.com");
        anotherUser.setUsername("別のユーザー");

        // タスクのユーザーを変更
        task.setUser(anotherUser);

        // ユーザーが正しく変更されていることを確認
        assertEquals(anotherUser, task.getUser());
        assertEquals("別のユーザー", task.getUser().getUsername());
    }
}