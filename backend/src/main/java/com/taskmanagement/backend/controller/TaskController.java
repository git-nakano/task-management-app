package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.TaskRequestDto;
import com.taskmanagement.backend.dto.TaskResponseDto;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import com.taskmanagement.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * タスクコントローラー
 * 
 * @RestController:
 *                  - このクラスがRESTful APIのコントローラーであることを示します
 *                  - すべてのメソッドの戻り値がJSONに変換されます
 * 
 *                  @RequestMapping("/api/tasks"):
 *                  - このコントローラーのベースパスを指定します
 *                  - すべてのエンドポイントは /api/tasks 以下になります
 * 
 *                  エンドポイント一覧：
 *                  - POST /api/tasks - タスクを作成
 *                  - GET /api/tasks/{id} - IDでタスクを取得
 *                  - GET /api/tasks - 全タスクを取得
 *                  - GET /api/tasks/status/{status} - ステータスでフィルタ
 *                  - GET /api/tasks/priority/{priority} - 優先度でフィルタ
 *                  - GET /api/tasks/search - キーワード検索
 *                  - GET /api/tasks/overdue - 期限切れタスクを取得
 *                  - GET /api/tasks/future - 今後のタスクを取得
 *                  - GET /api/tasks/filter - 複合条件で検索
 *                  - PUT /api/tasks/{id} - タスクを更新
 *                  - PUT /api/tasks/{id}/status - ステータスのみを更新
 *                  - DELETE /api/tasks/{id} - タスクを削除
 *                  - GET /api/tasks/count - タスク総数を取得
 *                  - GET /api/tasks/count/status/{status} - ステータス別タスク数を取得
 * 
 *                  実務でのポイント：
 *                  - RESTful APIの設計原則に従う
 *                  - HTTPメソッドでCRUD操作を表現
 *                  - 適切なHTTPステータスコードを返す
 *                  - バリデーションエラーは自動的にGlobalExceptionHandlerで処理される
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    /**
     * タスクサービス
     */
    private final TaskService taskService;

    /**
     * タスクを作成
     * 
     * エンドポイント：POST /api/tasks
     * 
     * リクエストボディ：
     * {
     * "title": "買い物に行く",
     * "description": "スーパーで食材を買う",
     * "dueDate": "2025-10-20",
     * "status": "TODO",
     * "priority": "HIGH"
     * }
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID（クエリパラメータ）
     * 
     * レスポンス：
     * - 201 Created: タスクが作成された場合
     * - 400 Bad Request: バリデーションエラーの場合
     * 
     * 実務でのポイント：
     * - @Valid: リクエストボディのバリデーションを自動実行
     * - @RequestBody: JSONをJavaオブジェクトに変換
     * - @RequestParam: クエリパラメータを受け取る
     * - HTTPステータスコード201（Created）を返す
     * 
     * 使用例：
     * POST http://localhost:8080/api/tasks?userId=1
     * Content-Type: application/json
     * 
     * {
     * "title": "買い物に行く",
     * "description": "スーパーで食材を買う",
     * "dueDate": "2025-10-20",
     * "status": "TODO",
     * "priority": "HIGH"
     * }
     * 
     * @param requestDto TaskRequestDto
     * @param userId     ユーザーID
     * @return ResponseEntity<TaskResponseDto>
     */
    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @Valid @RequestBody TaskRequestDto requestDto,
            @RequestParam Long userId) {

        TaskResponseDto createdTask = taskService.createTask(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    /**
     * IDでタスクを取得
     * 
     * エンドポイント：GET /api/tasks/{id}
     * 
     * パスパラメータ：
     * - id: タスクID
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID（権限チェック用）
     * 
     * レスポンス：
     * - 200 OK: タスクが見つかった場合
     * - 404 Not Found: タスクが見つからない、または権限がない場合
     * 
     * 実務でのポイント：
     * - userIdを受け取り、権限チェックを行います
     * - 他人のタスクを取得できないようにします
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/1?userId=1
     * 
     * @param id     タスクID
     * @param userId ユーザーID
     * @return ResponseEntity<TaskResponseDto>
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id,
            @RequestParam Long userId) {

        TaskResponseDto task = taskService.findById(id, userId);
        return ResponseEntity.ok(task);
    }

    /**
     * 全タスクを取得（作成日時の降順）
     * 
     * エンドポイント：GET /api/tasks
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - ダッシュボードでのタスク一覧表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks?userId=1
     * 
     * レスポンス例：
     * [
     * {
     * "id": 1,
     * "title": "買い物に行く",
     * "status": "TODO",
     * ...
     * },
     * ...
     * ]
     * 
     * @param userId ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getAllTasks(@RequestParam Long userId) {
        List<TaskResponseDto> tasks = taskService.findAllByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * ステータスでフィルタ
     * 
     * エンドポイント：GET /api/tasks/status/{status}
     * 
     * パスパラメータ：
     * - status: タスクのステータス（TODO、IN_PROGRESS、DONE）
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - 「未着手のタスク」のみを表示
     * - 「完了したタスク」のみを表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/status/TODO?userId=1
     * 
     * @param status タスクのステータス
     * @param userId ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam Long userId) {

        List<TaskResponseDto> tasks = taskService.findByStatus(userId, status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 優先度でフィルタ
     * 
     * エンドポイント：GET /api/tasks/priority/{priority}
     * 
     * パスパラメータ：
     * - priority: タスクの優先度（HIGH、MEDIUM、LOW）
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - 「高優先度のタスク」のみを表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/priority/HIGH?userId=1
     * 
     * @param priority タスクの優先度
     * @param userId   ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TaskResponseDto>> getTasksByPriority(
            @PathVariable TaskPriority priority,
            @RequestParam Long userId) {

        List<TaskResponseDto> tasks = taskService.findByPriority(userId, priority);
        return ResponseEntity.ok(tasks);
    }

    /**
     * キーワード検索
     * 
     * エンドポイント：GET /api/tasks/search
     * 
     * リクエストパラメータ：
     * - keyword: 検索キーワード
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - タスク検索機能
     * - タイトルや詳細で検索
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/search?keyword=買い物&userId=1
     * 
     * @param keyword 検索キーワード
     * @param userId  ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponseDto>> searchTasks(
            @RequestParam String keyword,
            @RequestParam Long userId) {

        List<TaskResponseDto> tasks = taskService.searchByKeyword(userId, keyword);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 期限切れタスクを取得
     * 
     * エンドポイント：GET /api/tasks/overdue
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - 期限切れのタスクを一覧表示
     * - リマインダー機能
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/overdue?userId=1
     * 
     * @param userId ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<TaskResponseDto>> getOverdueTasks(@RequestParam Long userId) {
        List<TaskResponseDto> tasks = taskService.findOverdueTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 今後のタスクを取得
     * 
     * エンドポイント：GET /api/tasks/future
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - 今後のタスクを一覧表示
     * - スケジュール管理
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/future?userId=1
     * 
     * @param userId ユーザーID
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/future")
    public ResponseEntity<List<TaskResponseDto>> getFutureTasks(@RequestParam Long userId) {
        List<TaskResponseDto> tasks = taskService.findFutureTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 複合条件で検索
     * 
     * エンドポイント：GET /api/tasks/filter
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID（必須）
     * - status: タスクのステータス（任意）
     * - priority: タスクの優先度（任意）
     * - keyword: 検索キーワード（任意）
     * 
     * レスポンス：
     * - 200 OK: タスクのリスト
     * 
     * 実務での使用場面：
     * - 高度なフィルタリング機能
     * - ステータス + 優先度 + キーワードの組み合わせ
     * 
     * 使用例：
     * GET
     * http://localhost:8080/api/tasks/filter?userId=1&status=TODO&priority=HIGH&keyword=買い物
     * 
     * @param userId   ユーザーID
     * @param status   タスクのステータス（任意）
     * @param priority タスクの優先度（任意）
     * @param keyword  検索キーワード（任意）
     * @return ResponseEntity<List<TaskResponseDto>>
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TaskResponseDto>> filterTasks(
            @RequestParam Long userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword) {

        List<TaskResponseDto> tasks = taskService.findWithFilters(userId, status, priority, keyword);
        return ResponseEntity.ok(tasks);
    }

    /**
     * タスクを更新
     * 
     * エンドポイント：PUT /api/tasks/{id}
     * 
     * パスパラメータ：
     * - id: タスクID
     * 
     * リクエストボディ：
     * {
     * "title": "更新されたタスク",
     * "description": "更新されました",
     * "dueDate": "2025-10-25",
     * "status": "IN_PROGRESS",
     * "priority": "MEDIUM"
     * }
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID（権限チェック用）
     * 
     * レスポンス：
     * - 200 OK: タスクが更新された場合
     * - 400 Bad Request: バリデーションエラーの場合
     * - 404 Not Found: タスクが見つからない、または権限がない場合
     * 
     * 実務でのポイント：
     * - 権限チェックを行い、他人のタスクを更新できないようにします
     * 
     * 使用例：
     * PUT http://localhost:8080/api/tasks/1?userId=1
     * Content-Type: application/json
     * 
     * {
     * "title": "更新されたタスク",
     * "status": "IN_PROGRESS",
     * "priority": "MEDIUM"
     * }
     * 
     * @param id         タスクID
     * @param requestDto TaskRequestDto
     * @param userId     ユーザーID
     * @return ResponseEntity<TaskResponseDto>
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto,
            @RequestParam Long userId) {

        TaskResponseDto updatedTask = taskService.updateTask(id, requestDto, userId);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * ステータスのみを更新
     * 
     * エンドポイント：PUT /api/tasks/{id}/status
     * 
     * パスパラメータ：
     * - id: タスクID
     * 
     * リクエストパラメータ：
     * - status: 新しいステータス
     * - userId: ユーザーID（権限チェック用）
     * 
     * レスポンス：
     * - 200 OK: タスクのステータスが更新された場合
     * - 404 Not Found: タスクが見つからない、または権限がない場合
     * 
     * 実務での使用場面：
     * - タスク完了時に、ステータスのみを更新
     * - UIでドラッグ&ドロップによるステータス変更
     * 
     * 使用例：
     * PUT http://localhost:8080/api/tasks/1/status?status=DONE&userId=1
     * 
     * @param id     タスクID
     * @param status 新しいステータス
     * @param userId ユーザーID
     * @return ResponseEntity<TaskResponseDto>
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status,
            @RequestParam Long userId) {

        TaskResponseDto updatedTask = taskService.updateTaskStatus(id, status, userId);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * タスクを削除
     * 
     * エンドポイント：DELETE /api/tasks/{id}
     * 
     * パスパラメータ：
     * - id: タスクID
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID（権限チェック用）
     * 
     * レスポンス：
     * - 204 No Content: タスクが削除された場合
     * - 404 Not Found: タスクが見つからない、または権限がない場合
     * 
     * 実務でのポイント：
     * - HTTPステータスコード204（No Content）を返す
     * - レスポンスボディは空
     * - 権限チェックを行い、他人のタスクを削除できないようにします
     * 
     * 使用例：
     * DELETE http://localhost:8080/api/tasks/1?userId=1
     * 
     * @param id     タスクID
     * @param userId ユーザーID
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @RequestParam Long userId) {

        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * タスク総数を取得
     * 
     * エンドポイント：GET /api/tasks/count
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: タスク総数
     * 
     * 実務での使用場面：
     * - ダッシュボードでの統計情報表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/count?userId=1
     * 
     * レスポンス例：
     * 42
     * 
     * @param userId ユーザーID
     * @return ResponseEntity<Long>
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countTasks(@RequestParam Long userId) {
        long count = taskService.countTasksByUserId(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * ステータス別タスク数を取得
     * 
     * エンドポイント：GET /api/tasks/count/status/{status}
     * 
     * パスパラメータ：
     * - status: タスクのステータス
     * 
     * リクエストパラメータ：
     * - userId: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: ステータス別タスク数
     * 
     * 実務での使用場面：
     * - ダッシュボードでのステータス別統計表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/tasks/count/status/TODO?userId=1
     * 
     * レスポンス例：
     * 10
     * 
     * @param status タスクのステータス
     * @param userId ユーザーID
     * @return ResponseEntity<Long>
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countTasksByStatus(
            @PathVariable TaskStatus status,
            @RequestParam Long userId) {

        long count = taskService.countTasksByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(count);
    }
}