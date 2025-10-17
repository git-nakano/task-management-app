package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.TaskRequestDto;
import com.taskmanagement.backend.dto.TaskResponseDto;
import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TaskServiceの統合テスト
 * 
 * @SpringBootTest:
 *                  - Spring Bootアプリケーション全体をロードしてテストします
 *                  - Service層、Repository層を含めてテストできます
 * 
 * @Transactional:
 *                 - 各テストメソッド実行後、データベースがロールバックされます
 * 
 *                 テストの目的：
 *                 - CRUD操作の動作確認
 *                 - フィルタリング機能の動作確認
 *                 - 検索機能の動作確認
 *                 - 権限チェックの動作確認
 */
@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private Long testUserId;
    private Long testTaskId;

    /**
     * 各テストメソッド実行前に呼ばれる初期化メソッド
     */
    @BeforeEach
    void setUp() {
        // テスト用のユーザーを作成
        UserResponseDto user = userService.createUser(
                "test@example.com",
                "password",
                "テストユーザー");
        testUserId = user.getId();

        // テスト用のタスクを作成
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("買い物に行く");
        requestDto.setDescription("スーパーで食材を買う");
        requestDto.setDueDate(LocalDate.now().plusDays(1));
        requestDto.setStatus(TaskStatus.TODO);
        requestDto.setPriority(TaskPriority.HIGH);

        TaskResponseDto task = taskService.createTask(requestDto, testUserId);
        testTaskId = task.getId();
    }

    /**
     * タスク作成のテスト
     */
    @Test
    void testCreateTask() {
        // タスクを作成
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("新しいタスク");
        requestDto.setDescription("テスト用のタスク");
        requestDto.setDueDate(LocalDate.now().plusDays(3));
        requestDto.setStatus(TaskStatus.TODO);
        requestDto.setPriority(TaskPriority.MEDIUM);

        TaskResponseDto task = taskService.createTask(requestDto, testUserId);

        // 検証
        assertNotNull(task.getId());
        assertEquals("新しいタスク", task.getTitle());
        assertEquals("テスト用のタスク", task.getDescription());
        assertEquals(TaskStatus.TODO, task.getStatus());
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
        assertEquals(testUserId, task.getUserId());
    }

    /**
     * 存在しないユーザーでタスク作成を試みるテスト
     */
    @Test
    void testCreateTaskWithInvalidUser() {
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("タスク");
        requestDto.setStatus(TaskStatus.TODO);
        requestDto.setPriority(TaskPriority.MEDIUM);

        // 存在しないユーザーIDでタスクを作成しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.createTask(requestDto, 999L);
        });

        assertEquals("ユーザーが見つかりません", exception.getMessage());
    }

    /**
     * IDでタスクを取得するテスト
     */
    @Test
    void testFindById() {
        // タスクを取得
        TaskResponseDto task = taskService.findById(testTaskId, testUserId);

        // 検証
        assertEquals(testTaskId, task.getId());
        assertEquals("買い物に行く", task.getTitle());
    }

    /**
     * 権限のないタスクを取得しようとするテスト
     */
    @Test
    void testFindByIdWithoutPermission() {
        // 別のユーザーを作成
        UserResponseDto anotherUser = userService.createUser(
                "another@example.com",
                "password",
                "別のユーザー");

        // 別のユーザーで、testUserのタスクを取得しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findById(testTaskId, anotherUser.getId());
        });

        assertEquals("このタスクにアクセスする権限がありません", exception.getMessage());
    }

    /**
     * 全タスクを取得するテスト
     */
    @Test
    void testFindAllByUserId() {
        // 追加でタスクを作成
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("プロジェクトの資料作成");
        requestDto2.setStatus(TaskStatus.IN_PROGRESS);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // 全タスクを取得
        List<TaskResponseDto> tasks = taskService.findAllByUserId(testUserId);

        // 検証
        assertEquals(2, tasks.size());
    }

    /**
     * ステータスでフィルタするテスト
     */
    @Test
    void testFindByStatus() {
        // 追加でタスクを作成（進行中）
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("資料作成");
        requestDto2.setStatus(TaskStatus.IN_PROGRESS);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // 未着手のタスクを取得
        List<TaskResponseDto> todoTasks = taskService.findByStatus(testUserId, TaskStatus.TODO);
        assertEquals(1, todoTasks.size());
        assertEquals("買い物に行く", todoTasks.get(0).getTitle());

        // 進行中のタスクを取得
        List<TaskResponseDto> inProgressTasks = taskService.findByStatus(testUserId, TaskStatus.IN_PROGRESS);
        assertEquals(1, inProgressTasks.size());
        assertEquals("資料作成", inProgressTasks.get(0).getTitle());
    }

    /**
     * 優先度でフィルタするテスト
     */
    @Test
    void testFindByPriority() {
        // 高優先度のタスクを取得
        List<TaskResponseDto> highPriorityTasks = taskService.findByPriority(testUserId, TaskPriority.HIGH);
        assertEquals(1, highPriorityTasks.size());
        assertEquals("買い物に行く", highPriorityTasks.get(0).getTitle());
    }

    /**
     * キーワード検索のテスト
     */
    @Test
    void testSearchByKeyword() {
        // 追加でタスクを作成
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("プロジェクト資料作成");
        requestDto2.setDescription("来週のプレゼン用");
        requestDto2.setStatus(TaskStatus.TODO);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // 「買い物」で検索
        List<TaskResponseDto> tasks1 = taskService.searchByKeyword(testUserId, "買い物");
        assertEquals(1, tasks1.size());
        assertEquals("買い物に行く", tasks1.get(0).getTitle());

        // 「プロジェクト」で検索
        List<TaskResponseDto> tasks2 = taskService.searchByKeyword(testUserId, "プロジェクト");
        assertEquals(1, tasks2.size());
        assertEquals("プロジェクト資料作成", tasks2.get(0).getTitle());
    }

    /**
     * 複合条件で検索するテスト
     */
    @Test
    void testFindWithFilters() {
        // 追加でタスクを作成
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("資料作成");
        requestDto2.setStatus(TaskStatus.IN_PROGRESS);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // ステータスのみでフィルタ
        List<TaskResponseDto> tasks1 = taskService.findWithFilters(testUserId, TaskStatus.TODO, null, "");
        assertEquals(1, tasks1.size());

        // 優先度のみでフィルタ
        List<TaskResponseDto> tasks2 = taskService.findWithFilters(testUserId, null, TaskPriority.HIGH, "");
        assertEquals(1, tasks2.size());

        // キーワードのみで検索
        List<TaskResponseDto> tasks3 = taskService.findWithFilters(testUserId, null, null, "買い物");
        assertEquals(1, tasks3.size());

        // 全条件を組み合わせ
        List<TaskResponseDto> tasks4 = taskService.findWithFilters(
                testUserId,
                TaskStatus.TODO,
                TaskPriority.HIGH,
                "買い物");
        assertEquals(1, tasks4.size());
    }

    /**
     * タスク更新のテスト
     */
    @Test
    void testUpdateTask() {
        // タスクを更新
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("更新されたタスク");
        requestDto.setDescription("更新されました");
        requestDto.setDueDate(LocalDate.now().plusDays(5));
        requestDto.setStatus(TaskStatus.IN_PROGRESS);
        requestDto.setPriority(TaskPriority.LOW);

        TaskResponseDto updatedTask = taskService.updateTask(testTaskId, requestDto, testUserId);

        // 検証
        assertEquals("更新されたタスク", updatedTask.getTitle());
        assertEquals("更新されました", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
        assertEquals(TaskPriority.LOW, updatedTask.getPriority());
    }

    /**
     * ステータスのみを更新するテスト
     */
    @Test
    void testUpdateTaskStatus() {
        // ステータスを更新
        TaskResponseDto updatedTask = taskService.updateTaskStatus(testTaskId, TaskStatus.DONE, testUserId);

        // 検証
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertEquals("買い物に行く", updatedTask.getTitle()); // タイトルは変更されていない
    }

    /**
     * 権限のないタスクを更新しようとするテスト
     */
    @Test
    void testUpdateTaskWithoutPermission() {
        // 別のユーザーを作成
        UserResponseDto anotherUser = userService.createUser(
                "another@example.com",
                "password",
                "別のユーザー");

        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("更新");
        requestDto.setStatus(TaskStatus.DONE);
        requestDto.setPriority(TaskPriority.HIGH);

        // 別のユーザーで、testUserのタスクを更新しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(testTaskId, requestDto, anotherUser.getId());
        });

        assertEquals("このタスクを更新する権限がありません", exception.getMessage());
    }

    /**
     * タスク削除のテスト
     */
    @Test
    void testDeleteTask() {
        // タスクを削除
        taskService.deleteTask(testTaskId, testUserId);

        // 検証：削除されたタスクを取得しようとするとエラー
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.findById(testTaskId, testUserId);
        });

        assertEquals("タスクが見つかりません", exception.getMessage());
    }

    /**
     * 権限のないタスクを削除しようとするテスト
     */
    @Test
    void testDeleteTaskWithoutPermission() {
        // 別のユーザーを作成
        UserResponseDto anotherUser = userService.createUser(
                "another@example.com",
                "password",
                "別のユーザー");

        // 別のユーザーで、testUserのタスクを削除しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(testTaskId, anotherUser.getId());
        });

        assertEquals("このタスクを削除する権限がありません", exception.getMessage());
    }

    /**
     * タスク総数のテスト
     */
    @Test
    void testCountTasksByUserId() {
        // 初期状態（setUp()で1件登録済み）
        long count = taskService.countTasksByUserId(testUserId);
        assertEquals(1, count);

        // 追加でタスクを登録
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("タスク2");
        requestDto2.setStatus(TaskStatus.TODO);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // カウントが増えていることを確認
        count = taskService.countTasksByUserId(testUserId);
        assertEquals(2, count);
    }

    /**
     * ステータス別タスク数のテスト
     */
    @Test
    void testCountTasksByUserIdAndStatus() {
        // 追加でタスクを作成
        TaskRequestDto requestDto2 = new TaskRequestDto();
        requestDto2.setTitle("タスク2");
        requestDto2.setStatus(TaskStatus.IN_PROGRESS);
        requestDto2.setPriority(TaskPriority.MEDIUM);
        taskService.createTask(requestDto2, testUserId);

        // 未着手のタスク数
        long todoCount = taskService.countTasksByUserIdAndStatus(testUserId, TaskStatus.TODO);
        assertEquals(1, todoCount);

        // 進行中のタスク数
        long inProgressCount = taskService.countTasksByUserIdAndStatus(testUserId, TaskStatus.IN_PROGRESS);
        assertEquals(1, inProgressCount);

        // 完了のタスク数
        long doneCount = taskService.countTasksByUserIdAndStatus(testUserId, TaskStatus.DONE);
        assertEquals(0, doneCount);
    }
}