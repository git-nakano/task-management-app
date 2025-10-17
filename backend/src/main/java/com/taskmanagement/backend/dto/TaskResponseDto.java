package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.model.Task;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * タスクレスポンスDTO（Data Transfer Object）
 * 
 * このDTOの役割：
 * - サーバーからクライアントへタスク情報を返す際に使用
 * - エンティティから必要な情報のみを抽出して送信
 * 
 * なぜエンティティをそのまま返さないのか：
 * 1. セキュリティ：エンティティには内部情報が含まれる可能性がある
 * 2. 柔軟性：APIの形式を自由に変更できる
 * 3. パフォーマンス：必要な情報のみを送信することで、データ量を削減
 * 
 * 実務でのポイント：
 * - エンティティとDTOの変換は、Serviceレイヤーで行います
 * - MapStructやModelMapperなどのライブラリを使用することもありますが、
 * 今回はシンプルに手動で変換します
 * 
 * このDTOに含まれる情報：
 * - ID、タイトル、詳細、期日、ステータス、優先度
 * - ユーザーID、ユーザー名（関連情報）
 * - 作成日時、更新日時
 * 
 * このDTOに含まれない情報：
 * - Userオブジェクト全体（必要な情報のみを含める）
 * - パスワードなどの機密情報
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {

    /**
     * タスクID
     */
    private Long id;

    /**
     * タスクタイトル
     */
    private String title;

    /**
     * タスクの詳細説明
     */
    private String description;

    /**
     * タスクの期日
     */
    private LocalDate dueDate;

    /**
     * タスクのステータス
     */
    private TaskStatus status;

    /**
     * タスクの優先度
     */
    private TaskPriority priority;

    /**
     * タスクの所有者のユーザーID
     * 
     * 実務でのポイント：
     * - Userオブジェクト全体ではなく、IDのみを含めることが多いです
     * - フロントエンドで必要な情報のみを送信することで、データ量を削減
     */
    private Long userId;

    /**
     * タスクの所有者のユーザー名
     * 
     * 実務でのポイント：
     * - UIに表示するために、ユーザー名も含めます
     * - これにより、フロントエンドで追加のAPIリクエストを行う必要がありません
     */
    private String username;

    /**
     * タスク作成日時
     */
    private LocalDateTime createdAt;

    /**
     * 最終更新日時
     */
    private LocalDateTime updatedAt;

    /**
     * Taskエンティティから TaskResponseDtoへの変換
     * 
     * 実務でのポイント：
     * - エンティティからDTOへの変換ロジックを、DTOクラス内に持たせることがあります
     * - これにより、変換ロジックが一箇所にまとまり、保守性が向上します
     * - または、Mapperクラスを別途作成することもあります
     * 
     * 使用例：
     * Task task = taskRepository.findById(1L).orElseThrow();
     * TaskResponseDto dto = TaskResponseDto.fromEntity(task);
     * 
     * @param task Taskエンティティ
     * @return TaskResponseDto
     */
    public static TaskResponseDto fromEntity(Task task) {
        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setUserId(task.getUser().getId());
        dto.setUsername(task.getUser().getUsername());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    /**
     * TaskRequestDtoとTaskエンティティから TaskResponseDtoを作成
     * 
     * 実務でのポイント：
     * - リクエストDTOとエンティティを組み合わせて、レスポンスDTOを作成
     * - 主に作成・更新処理の後に使用します
     * 
     * @param requestDto TaskRequestDto
     * @param task       Taskエンティティ
     * @return TaskResponseDto
     */
    public static TaskResponseDto fromRequestAndEntity(TaskRequestDto requestDto, Task task) {
        return fromEntity(task);
    }
}