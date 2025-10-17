package com.taskmanagement.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * エラーレスポンス
 * 
 * このクラスの役割：
 * - API例外発生時に、統一されたエラーレスポンス形式をクライアントに返す
 * - フロントエンドでエラーメッセージを表示しやすくする
 * 
 * なぜ統一されたエラーレスポンスが必要か：
 * 1. フロントエンド側で一貫したエラーハンドリングができる
 * 2. デバッグが容易になる（タイムスタンプ、パス、メッセージを含む）
 * 3. ユーザーに適切なエラーメッセージを表示できる
 * 
 * エラーレスポンスの例：
 * {
 * "timestamp": "2025-10-17T10:30:00",
 * "status": 404,
 * "error": "Not Found",
 * "message": "タスクが見つかりません",
 * "path": "/api/tasks/999"
 * }
 * 
 * 実務でのポイント：
 * - エラーメッセージはユーザーフレンドリーにする
 * - 技術的な詳細は含めない（セキュリティ対策）
 * - タイムスタンプを含めることで、ログとの照合が容易
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * エラー発生日時
     * 
     * 実務での用途：
     * - ログとの照合に使用
     * - ユーザーからの問い合わせ時に特定しやすくする
     */
    private LocalDateTime timestamp;

    /**
     * HTTPステータスコード
     * 
     * 一般的なステータスコード：
     * - 400: Bad Request（リクエストが不正）
     * - 401: Unauthorized（認証が必要）
     * - 403: Forbidden（権限がない）
     * - 404: Not Found（リソースが見つからない）
     * - 500: Internal Server Error（サーバーエラー）
     */
    private int status;

    /**
     * エラーの種類
     * 
     * 例：
     * - "Bad Request"
     * - "Not Found"
     * - "Internal Server Error"
     */
    private String error;

    /**
     * エラーメッセージ
     * 
     * 実務でのポイント：
     * - ユーザーが理解できる日本語メッセージ
     * - 具体的な対処方法を含めることが望ましい
     * - 例: "タスクが見つかりません" → "ID: 999のタスクは存在しません"
     */
    private String message;

    /**
     * リクエストパス
     * 
     * 実務での用途：
     * - どのエンドポイントでエラーが発生したかを特定
     * - フロントエンド側のデバッグに役立つ
     */
    private String path;

    /**
     * エラーレスポンスを生成する便利メソッド
     * 
     * 使用例：
     * ErrorResponse errorResponse = ErrorResponse.of(
     * 404,
     * "Not Found",
     * "タスクが見つかりません",
     * "/api/tasks/999"
     * );
     * 
     * @param status  HTTPステータスコード
     * @param error   エラーの種類
     * @param message エラーメッセージ
     * @param path    リクエストパス
     * @return ErrorResponse
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path);
    }
}