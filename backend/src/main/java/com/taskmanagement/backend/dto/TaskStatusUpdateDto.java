package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * タスクのステータス更新用DTO
 * 
 * このDTOの役割：
 * - タスクのステータスのみを更新するためのリクエストボディ
 * - 部分的な更新（Partial Update）を実現します
 * 
 * なぜ専用のDTOが必要なのか：
 * 1. 明確性：ステータス更新であることがAPIの仕様から明確になる
 * 2. バリデーション：ステータスのみを受け取るため、他のフィールドの検証が不要
 * 3. セキュリティ：必要なフィールド以外を受け取らない
 * 4. 保守性：APIの意図が明確で、将来の変更が容易
 * 
 * TaskRequestDtoとの違い：
 * - TaskRequestDto：タスクのすべてのフィールドを更新（PUT）
 * - TaskStatusUpdateDto：ステータスのみを更新（PATCH）
 * 
 * HTTPメソッドの使い分け：
 * - PUT：リソース全体を更新（すべてのフィールドを送信）
 * - PATCH：リソースの一部を更新（変更するフィールドのみを送信）
 * 
 * 実務でのポイント：
 * - 部分的な更新には、専用のDTOを作成することが推奨されます
 * - ステータス更新、優先度更新など、個別の操作ごとにDTOを作成します
 * - これにより、APIの意図が明確になり、保守性が向上します
 * 
 * リクエスト例：
 * PATCH /api/tasks/1/status
 * {
 * "status": "IN_PROGRESS"
 * }
 * 
 * レスポンス例：
 * {
 * "id": 1,
 * "title": "買い物に行く",
 * "description": "スーパーで食材を買う",
 * "dueDate": "2025-10-20",
 * "status": "IN_PROGRESS",
 * "priority": "HIGH",
 * "userId": 1,
 * "username": "テストユーザー",
 * "createdAt": "2025-10-17T10:30:00",
 * "updatedAt": "2025-10-17T14:45:00"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateDto {

    /**
     * タスクのステータス
     * 
     * バリデーション：
     * - @NotNull：nullを許可しない
     * 
     * 実務でのポイント：
     * - TaskStatusはENUM型なので、無効な値を送信するとバリデーションエラーになります
     * - 有効な値：TODO, IN_PROGRESS, DONE
     * - 大文字小文字は区別されます
     * 
     * リクエスト例：
     * {
     * "status": "IN_PROGRESS" // 有効
     * }
     * 
     * 無効なリクエスト例：
     * {
     * "status": "in_progress" // 無効（小文字）
     * }
     * {
     * "status": "INVALID" // 無効（存在しない値）
     * }
     * {
     * "status": null // 無効（nullは許可しない）
     * }
     */
    @NotNull(message = "ステータスは必須です")
    private TaskStatus status;

    /**
     * 便利メソッド：TaskStatusUpdateDtoを生成
     * 
     * 使用例：
     * TaskStatusUpdateDto dto = TaskStatusUpdateDto.of(TaskStatus.IN_PROGRESS);
     * 
     * @param status タスクのステータス
     * @return TaskStatusUpdateDto
     */
    public static TaskStatusUpdateDto of(TaskStatus status) {
        return new TaskStatusUpdateDto(status);
    }

    /**
     * TaskStatusUpdateDtoの説明（コメント）
     * 
     * 実務での使い分け：
     * 
     * 1. 完全更新（PUT）：TaskRequestDtoを使用
     * - すべてのフィールドを更新
     * - エンドポイント：PUT /api/tasks/{id}
     * 
     * 2. 部分更新（PATCH）：専用DTOを使用
     * - ステータス更新：TaskStatusUpdateDto
     * - エンドポイント：PATCH /api/tasks/{id}/status
     * 
     * なぜ部分更新が必要なのか：
     * - ユーザビリティ：ステータスのみを変更したい場合、他のフィールドを送信する必要がない
     * - パフォーマンス：必要最小限のデータのみを送信
     * - セキュリティ：意図しないフィールドの更新を防ぐ
     * 
     * RESTful APIの設計原則：
     * - PUT：リソース全体を置き換える
     * - PATCH：リソースの一部を変更する
     * 
     * 実務でのベストプラクティス：
     * - 部分更新には、専用のDTOを作成する
     * - DTOの名前は、操作の意図を明確にする（例：TaskStatusUpdateDto）
     * - バリデーションは、DTOレベルで行う
     */
}