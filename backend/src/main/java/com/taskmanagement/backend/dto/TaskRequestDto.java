package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * タスクリクエストDTO（Data Transfer Object）
 * 
 * DTOとは：
 * - クライアント（フロントエンド）とサーバー（バックエンド）の間でやり取りするデータの形式
 * - エンティティ（Entity）とは異なり、APIのリクエスト/レスポンス専用のクラス
 * 
 * なぜDTOを使うのか：
 * 1. セキュリティ：エンティティには公開したくない情報（内部ID、タイムスタンプ等）が含まれる
 * 2. 柔軟性：APIの形式とデータベースの構造を独立させることができる
 * 3. バリデーション：API固有のバリデーションルールを適用できる
 * 
 * 実務でのポイント：
 * - リクエスト用とレスポンス用でDTOを分けることが一般的
 * - TaskRequestDto：クライアントからサーバーへのデータ（作成・更新時）
 * - TaskResponseDto：サーバーからクライアントへのデータ（取得時）
 * 
 * このDTOに含まれる情報：
 * - タイトル（必須）
 * - 詳細（任意）
 * - 期日（任意）
 * - ステータス（必須）
 * - 優先度（必須）
 * 
 * このDTOに含まれない情報：
 * - ID（自動採番されるため）
 * - ユーザー（認証情報から取得するため）
 * - 作成日時・更新日時（自動設定されるため）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {

    /**
     * タスクタイトル
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * - @Size：最大200文字
     * 
     * 実務でのポイント：
     * - フロントエンドでもバリデーションを行いますが、
     * バックエンドでも必ずバリデーションを行います（セキュリティ対策）
     */
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内である必要があります")
    private String title;

    /**
     * タスクの詳細説明
     * 
     * バリデーション：
     * - @Size：最大5000文字
     * - nullを許可（詳細は任意項目）
     * 
     * 実務でのポイント：
     * - 任意項目でも、最大文字数の制限は必要です
     * - データベースのカラム定義と一致させることが重要
     */
    @Size(max = 5000, message = "詳細は5000文字以内である必要があります")
    private String description;

    /**
     * タスクの期日
     * 
     * バリデーション：
     * - nullを許可（期日は任意項目）
     * 
     * 実務でのポイント：
     * - LocalDate型を使用することで、日付のみを扱います（時刻は含まない）
     * - JSONでは "2025-12-31" の形式で送受信されます
     */
    private LocalDate dueDate;

    /**
     * タスクのステータス
     * 
     * バリデーション：
     * - @NotNull：nullを許可しない（Enumなので@NotBlankは使用しない）
     * 
     * 実務でのポイント：
     * - Enumを使用することで、不正な値の入力を防ぎます
     * - JSONでは "TODO", "IN_PROGRESS", "DONE" の形式で送受信されます
     */
    @NotNull(message = "ステータスは必須です")
    private TaskStatus status;

    /**
     * タスクの優先度
     * 
     * バリデーション：
     * - @NotNull：nullを許可しない
     * 
     * 実務でのポイント：
     * - デフォルト値をフロントエンドで設定することも可能ですが、
     * バックエンドでもデフォルト値を持つことで、より安全です
     */
    @NotNull(message = "優先度は必須です")
    private TaskPriority priority;

    /**
     * タスクID（更新時のみ使用）
     * 
     * 注意点：
     * - 新規作成時はnull
     * - 更新時のみIDを設定する
     * 
     * 実務でのポイント：
     * - 作成と更新で同じDTOを使用する場合、IDフィールドを含めることがあります
     * - または、別々のDTO（TaskCreateDto、TaskUpdateDto）を作成することもあります
     */
    private Long id;
}