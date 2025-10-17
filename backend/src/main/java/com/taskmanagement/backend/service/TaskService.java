package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.TaskRequestDto;
import com.taskmanagement.backend.dto.TaskResponseDto;
import com.taskmanagement.backend.model.Task;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import com.taskmanagement.backend.model.User;
import com.taskmanagement.backend.repository.TaskRepository;
import com.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * タスクサービス
 * 
 * Serviceレイヤーの役割：
 * - ビジネスロジックを実装
 * - Repository層とController層の橋渡し
 * - DTOとエンティティの変換
 * - トランザクション管理
 * 
 * このServiceが提供する機能：
 * 1. CRUD操作（作成・読取・更新・削除）
 * 2. フィルタリング（ステータス、優先度）
 * 3. 検索（キーワード検索）
 * 4. ソート（作成日時、優先度）
 * 5. 複合条件での検索
 * 
 * 実務でのポイント：
 * - Streamを活用して、リストの変換を簡潔に記述します
 * - Optional<T>を使用して、nullセーフに実装します
 * - @Transactionalを使用して、トランザクション管理を自動化します
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    /**
     * タスクリポジトリ
     */
    private final TaskRepository taskRepository;

    /**
     * ユーザーリポジトリ
     * 
     * 実務でのポイント：
     * - タスクはユーザーに属するため、UserRepositoryも必要です
     * - タスク作成時にユーザーの存在確認を行います
     */
    private final UserRepository userRepository;

    /**
     * タスクを作成
     * 
     * 実務でのポイント：
     * - RequestDtoを受け取り、ResponseDtoを返します
     * - エンティティはServiceレイヤー内部でのみ扱います
     * - ユーザーIDからUserエンティティを取得し、Taskに設定します
     * 
     * @param requestDto TaskRequestDto
     * @param userId     ユーザーID
     * @return 作成されたタスク（TaskResponseDto）
     * @throws IllegalArgumentException ユーザーが見つからない場合
     */
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto, Long userId) {
        // ユーザーを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

        // Taskエンティティを作成
        Task task = new Task();
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setDueDate(requestDto.getDueDate());
        task.setStatus(requestDto.getStatus());
        task.setPriority(requestDto.getPriority());
        task.setUser(user);

        // データベースに保存
        Task savedTask = taskRepository.save(task);

        // DTOに変換して返す
        return TaskResponseDto.fromEntity(savedTask);
    }

    /**
     * IDでタスクを取得
     * 
     * 実務でのポイント：
     * - ユーザーIDも受け取り、権限チェックを行います
     * - これにより、他人のタスクを取得できないようにします
     * 
     * @param taskId タスクID
     * @param userId ユーザーID
     * @return TaskResponseDto
     * @throws IllegalArgumentException タスクが見つからない、または権限がない場合
     */
    public TaskResponseDto findById(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("タスクが見つかりません"));

        // 権限チェック：タスクの所有者であることを確認
        if (!task.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("このタスクにアクセスする権限がありません");
        }

        return TaskResponseDto.fromEntity(task);
    }

    /**
     * ユーザーIDで全タスクを取得（作成日時の降順）
     * 
     * 実務でのポイント：
     * - Streamを使用して、List<Task>をList<TaskResponseDto>に変換します
     * - map(TaskResponseDto::fromEntity)はラムダ式の省略形です
     * - collect(Collectors.toList())でリストに変換します
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findAllByUserId(Long userId) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ステータスでフィルタ
     * 
     * 実務での使用場面：
     * - 「未着手のタスク」のみを表示
     * - 「完了したタスク」のみを表示
     * 
     * @param userId ユーザーID
     * @param status タスクのステータス
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findByStatus(Long userId, TaskStatus status) {
        return taskRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 優先度でフィルタ
     * 
     * 実務での使用場面：
     * - 「高優先度のタスク」のみを表示
     * 
     * @param userId   ユーザーID
     * @param priority タスクの優先度
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findByPriority(Long userId, TaskPriority priority) {
        return taskRepository.findByUserIdAndPriority(userId, priority)
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * キーワード検索
     * 
     * 実務での使用場面：
     * - タスク検索機能
     * - タイトルや詳細で検索
     * 
     * @param userId  ユーザーID
     * @param keyword 検索キーワード
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> searchByKeyword(Long userId, String keyword) {
        return taskRepository.searchByKeyword(userId, keyword)
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 期限切れタスクを取得
     * 
     * 実務での使用場面：
     * - 期限切れのタスクを一覧表示
     * - リマインダー機能
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findOverdueTasks(Long userId) {
        return taskRepository.findByUserIdAndDueDateBefore(userId, LocalDate.now())
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 今後のタスクを取得
     * 
     * 実務での使用場面：
     * - 今後のタスクを一覧表示
     * - スケジュール管理
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findFutureTasks(Long userId) {
        return taskRepository.findByUserIdAndDueDateAfter(userId, LocalDate.now())
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 複合条件で検索
     * 
     * 実務での使用場面：
     * - 高度なフィルタリング機能
     * - ステータス + 優先度 + キーワードの組み合わせ
     * 
     * @param userId   ユーザーID
     * @param status   タスクのステータス（nullの場合はフィルタしない）
     * @param priority タスクの優先度（nullの場合はフィルタしない）
     * @param keyword  検索キーワード（空の場合は検索しない）
     * @return タスクのリスト（TaskResponseDto）
     */
    public List<TaskResponseDto> findWithFilters(Long userId,
            TaskStatus status,
            TaskPriority priority,
            String keyword) {
        // keywordがnullの場合は空文字に変換
        String safeKeyword = (keyword == null) ? "" : keyword;

        return taskRepository.findByUserIdWithFilters(userId, status, priority, safeKeyword)
                .stream()
                .map(TaskResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * タスクを更新
     * 
     * 実務でのポイント：
     * - 権限チェックを行い、他人のタスクを更新できないようにします
     * - @PreUpdateが自動実行され、updatedAtが更新されます
     * 
     * @param taskId     タスクID
     * @param requestDto TaskRequestDto
     * @param userId     ユーザーID
     * @return 更新されたタスク（TaskResponseDto）
     * @throws IllegalArgumentException タスクが見つからない、または権限がない場合
     */
    @Transactional
    public TaskResponseDto updateTask(Long taskId, TaskRequestDto requestDto, Long userId) {
        // タスクを取得
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("タスクが見つかりません"));

        // 権限チェック：タスクの所有者であることを確認
        if (!task.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("このタスクを更新する権限がありません");
        }

        // タスクを更新
        task.setTitle(requestDto.getTitle());
        task.setDescription(requestDto.getDescription());
        task.setDueDate(requestDto.getDueDate());
        task.setStatus(requestDto.getStatus());
        task.setPriority(requestDto.getPriority());

        // データベースに保存
        Task updatedTask = taskRepository.save(task);

        // DTOに変換して返す
        return TaskResponseDto.fromEntity(updatedTask);
    }

    /**
     * タスクのステータスのみを更新
     * 
     * 実務での使用場面：
     * - タスク完了時に、ステータスのみを更新
     * - UIでドラッグ&ドロップによるステータス変更
     * 
     * @param taskId タスクID
     * @param status 新しいステータス
     * @param userId ユーザーID
     * @return 更新されたタスク（TaskResponseDto）
     * @throws IllegalArgumentException タスクが見つからない、または権限がない場合
     */
    @Transactional
    public TaskResponseDto updateTaskStatus(Long taskId, TaskStatus status, Long userId) {
        // タスクを取得
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("タスクが見つかりません"));

        // 権限チェック
        if (!task.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("このタスクを更新する権限がありません");
        }

        // ステータスのみを更新
        task.setStatus(status);

        // データベースに保存
        Task updatedTask = taskRepository.save(task);

        // DTOに変換して返す
        return TaskResponseDto.fromEntity(updatedTask);
    }

    /**
     * タスクを削除
     * 
     * 実務でのポイント：
     * - 権限チェックを行い、他人のタスクを削除できないようにします
     * - 論理削除と物理削除の選択（今回は物理削除）
     * 
     * @param taskId タスクID
     * @param userId ユーザーID
     * @throws IllegalArgumentException タスクが見つからない、または権限がない場合
     */
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        // タスクを取得
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("タスクが見つかりません"));

        // 権限チェック
        if (!task.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("このタスクを削除する権限がありません");
        }

        // タスクを削除
        taskRepository.deleteById(taskId);
    }

    /**
     * ユーザーのタスク総数を取得
     * 
     * 実務での使用場面：
     * - ダッシュボードでの統計情報表示
     * 
     * @param userId ユーザーID
     * @return タスク総数
     */
    public long countTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId).size();
    }

    /**
     * ユーザーのステータス別タスク数を取得
     * 
     * 実務での使用場面：
     * - ダッシュボードでのステータス別統計表示
     * 
     * @param userId ユーザーID
     * @param status タスクのステータス
     * @return ステータス別タスク数
     */
    public long countTasksByUserIdAndStatus(Long userId, TaskStatus status) {
        return taskRepository.findByUserIdAndStatus(userId, status).size();
    }
}