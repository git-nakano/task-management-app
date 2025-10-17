package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.model.Task;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * TaskRepositoryインターフェース
 * 
 * Spring Data JPAのJpaRepositoryを継承し、タスク管理に必要な
 * 様々なクエリメソッドを提供します。
 * 
 * 実務でのポイント：
 * - メソッド名から自動的にクエリを生成する機能を活用
 * - 複雑なクエリは@Queryアノテーションで明示的に定義
 * - フィルタリングと検索機能を提供することで、実用的なタスク管理を実現
 * 
 * 自動提供されるメソッド（一部）：
 * - save(Task task): タスクを保存または更新
 * - findById(Long id): IDでタスクを検索
 * - findAll(): すべてのタスクを取得
 * - deleteById(Long id): IDでタスクを削除
 * 
 * カスタムクエリメソッド：
 * - フィルタリング機能（ステータス、優先度、ユーザー）
 * - 検索機能（キーワード検索）
 * - 複合条件での検索
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * ユーザーIDでタスクを検索
     * 
     * 実務での使用場面：
     * - ログインユーザーの全タスクを取得
     * - ユーザーごとのタスク一覧表示
     * 
     * メソッド名のルール：
     * - findByUser_Id → "SELECT t FROM Task t WHERE t.user.id = ?"
     * - User_Idの"_"は、Userオブジェクトのidフィールドを指します
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（見つからない場合は空のリスト）
     */
    List<Task> findByUserId(Long userId);

    /**
     * ユーザーIDとステータスでタスクを検索
     * 
     * 実務での使用場面：
     * - 「未着手のタスク」のみを表示
     * - 「完了したタスク」のみを表示
     * 
     * メソッド名のルール：
     * - findByUser_IdAndStatus → "WHERE user.id = ? AND status = ?"
     * 
     * 使用例：
     * // ユーザーID 1 の未着手タスクを取得
     * List<Task> todoTasks = taskRepository.findByUserIdAndStatus(1L,
     * TaskStatus.TODO);
     * 
     * @param userId ユーザーID
     * @param status タスクのステータス
     * @return タスクのリスト
     */
    List<Task> findByUserIdAndStatus(Long userId, TaskStatus status);

    /**
     * ユーザーIDと優先度でタスクを検索
     * 
     * 実務での使用場面：
     * - 「高優先度のタスク」のみを表示
     * - 優先度でタスクを絞り込む
     * 
     * @param userId   ユーザーID
     * @param priority タスクの優先度
     * @return タスクのリスト
     */
    List<Task> findByUserIdAndPriority(Long userId, TaskPriority priority);

    /**
     * ユーザーIDでタスクを取得（作成日時の降順でソート）
     * 
     * 実務での使用場面：
     * - 最新のタスクを上に表示
     * - タスク一覧のデフォルト表示
     * 
     * メソッド名のルール：
     * - OrderByCreatedAtDesc → "ORDER BY created_at DESC"
     * - Descは降順（新しい順）、Ascは昇順（古い順）
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（作成日時の降順）
     */
    List<Task> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * ユーザーIDでタスクを取得（優先度の降順でソート）
     * 
     * 実務での使用場面：
     * - 優先度が高いタスクを上に表示
     * - 重要なタスクを見逃さないようにする
     * 
     * 優先度のソート順：
     * - HIGH（高） → MEDIUM（中） → LOW（低）
     * 
     * @param userId ユーザーID
     * @return タスクのリスト（優先度の降順）
     */
     List<Task> findByUserIdOrderByPriorityDesc(Long userId);

    /**
     * キーワード検索（タイトルまたは詳細に含まれるタスクを検索）
     * 
     * 実務での使用場面：
     * - タスク検索機能
     * - タイトルや詳細でタスクを検索
     * 
     * @Queryアノテーションの使用：
     * - 複雑なクエリは、メソッド名だけでは表現できません
     * - そのため、@Queryアノテーションで明示的にJPQLを定義します
     * 
     * JPQLとは：
     * - Java Persistence Query Language
     * - SQLに似ていますが、テーブルではなくエンティティを操作します
     * - "SELECT t FROM Task t" → Taskエンティティを検索
     * 
     * LIKEとLOWER()の使用：
     * - LOWER(): 大文字小文字を区別しない検索
     * - LIKE '%keyword%': 部分一致検索
     * - 例: "買い物" → "買い物に行く"、"スーパーで買い物"もヒット
     * 
     * @param userId  ユーザーID
     * @param keyword 検索キーワード
     * @return タスクのリスト
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
            "AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByKeyword(@Param("userId") Long userId,
            @Param("keyword") String keyword);

    /**
     * 期日が指定日以前のタスクを検索（期限切れタスクの検出に使用）
     * 
     * 実務での使用場面：
     * - 期限切れのタスクを一覧表示
     * - リマインダー機能
     * - タスクの遅延状況を把握
     * 
     * メソッド名のルール：
     * - findByUser_IdAndDueDateBefore → "WHERE user.id = ? AND due_date < ?"
     * - Beforeは「より前」、Afterは「より後」
     * 
     * 使用例：
     * // 今日より前（期限切れ）のタスクを取得
     * List<Task> overdueTasks = taskRepository.findByUserIdAndDueDateBefore(1L,
     * LocalDate.now());
     * 
     * @param userId ユーザーID
     * @param date   基準日
     * @return タスクのリスト
     */
    List<Task> findByUserIdAndDueDateBefore(Long userId, LocalDate date);

    /**
     * 期日が指定日以降のタスクを検索
     * 
     * 実務での使用場面：
     * - 今後のタスクを一覧表示
     * - スケジュール管理
     * 
     * @param userId ユーザーID
     * @param date   基準日
     * @return タスクのリスト
     */
    List<Task> findByUserIdAndDueDateAfter(Long userId, LocalDate date);

    /**
     * 複合条件でタスクを検索（ステータス、優先度、キーワード）
     * 
     * 実務での使用場面：
     * - 高度なフィルタリング機能
     * - 複数条件を組み合わせたタスク検索
     * 
     * 動的クエリの実装：
     * - statusがnullの場合は、ステータスでフィルタしない
     * - priorityがnullの場合は、優先度でフィルタしない
     * - keywordが空の場合は、キーワード検索しない
     * 
     * ORとANDの使い分け：
     * - (status IS NULL OR t.status = :status): statusがnullまたは一致
     * - AND: すべての条件を満たす必要がある
     * 
     * 使用例：
     * // 未着手で高優先度のタスクを検索
     * List<Task> tasks = taskRepository.findByUserIdWithFilters(
     * 1L,
     * TaskStatus.TODO,
     * TaskPriority.HIGH,
     * ""
     * );
     * 
     * @param userId   ユーザーID
     * @param status   タスクのステータス（nullの場合はフィルタしない）
     * @param priority タスクの優先度（nullの場合はフィルタしない）
     * @param keyword  検索キーワード（空の場合は検索しない）
     * @return タスクのリスト
     */
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:keyword = '' OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> findByUserIdWithFilters(@Param("userId") Long userId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("keyword") String keyword);
}