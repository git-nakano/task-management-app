package com.taskmanagement.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

/**
 * グローバル例外ハンドラー
 * 
 * @ControllerAdvice:
 *                    - アプリケーション全体の例外を一箇所で処理します
 *                    - すべてのControllerで発生した例外をキャッチします
 * 
 *                    なぜグローバル例外ハンドラーが必要か：
 *                    1. 例外処理のコードをController内に書かなくて済む
 *                    2. 統一されたエラーレスポンス形式を保証
 *                    3. 例外処理のロジックを一箇所で管理できる
 * 
 *                    実務でのポイント：
 *                    - 各例外タイプに応じて、適切なHTTPステータスコードを返す
 *                    - ユーザーフレンドリーなエラーメッセージを返す
 *                    - セキュリティ上の理由で、技術的な詳細は含めない
 *                    - ログには詳細を記録するが、レスポンスには含めない
 * 
 *                    処理する例外の種類：
 *                    1. IllegalArgumentException（400 Bad Request）
 *                    2. MethodArgumentNotValidException（400 Bad Request）
 *                    3. その他の例外（500 Internal Server Error）
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentExceptionのハンドリング
     * 
     * IllegalArgumentExceptionが発生する場面：
     * - ユーザーが見つからない
     * - タスクが見つからない
     * - 権限がない
     * - 不正なパラメータ
     * 
     * 実務でのポイント：
     * - Service層でthrowされたIllegalArgumentExceptionをキャッチ
     * - HTTPステータスコード400（Bad Request）を返す
     * - エラーメッセージをそのままクライアントに返す
     * 
     * @param ex      IllegalArgumentException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * バリデーションエラーのハンドリング
     * 
     * MethodArgumentNotValidExceptionが発生する場面：
     * - @Validアノテーションによるバリデーションエラー
     * - @NotBlank、@NotNull、@Sizeなどのバリデーションに違反
     * 
     * 実務でのポイント：
     * - 複数のバリデーションエラーをまとめて返す
     * - フィールド名とエラーメッセージを含める
     * - フロントエンド側でフォームのどのフィールドにエラーがあるか特定できる
     * 
     * エラーメッセージの例：
     * "title: タイトルは必須です, priority: 優先度は必須です"
     * 
     * @param ex      MethodArgumentNotValidException
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // すべてのバリデーションエラーをまとめる
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    return fieldName + ": " + message;
                })
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                errorMessage,
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * その他すべての例外のハンドリング
     * 
     * Exceptionが発生する場面：
     * - 予期しないエラー
     * - データベース接続エラー
     * - その他のシステムエラー
     * 
     * 実務でのポイント：
     * - HTTPステータスコード500（Internal Server Error）を返す
     * - 詳細なエラーメッセージはログに記録するが、クライアントには返さない
     * - セキュリティ上の理由で、汎用的なメッセージを返す
     * 
     * 注意点：
     * - 本番環境では、詳細なエラーメッセージを返してはいけません
     * - ログには詳細を記録し、クライアントには汎用メッセージを返します
     * 
     * @param ex      Exception
     * @param request HttpServletRequest
     * @return ResponseEntity<ErrorResponse>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request) {

        // ログに詳細を記録（実際にはLoggerを使用）
        System.err.println("予期しないエラーが発生しました: " + ex.getMessage());
        ex.printStackTrace();

        // クライアントには汎用的なメッセージを返す
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "サーバーエラーが発生しました。しばらくしてから再度お試しください。",
                request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    /**
     * リソースが見つからない例外のハンドリング（将来の拡張用）
     * 
     * 実務での使用例：
     * - Service層で独自の ResourceNotFoundExceptionをthrowする
     * - より詳細なエラーハンドリングが必要な場合
     * 
     * 注：今回は使用しませんが、実務でよく使うパターンです
     * 
     * 使用例：
     * throw new ResourceNotFoundException("タスクID: " + id + "が見つかりません");
     */
    /*
     * @ExceptionHandler(ResourceNotFoundException.class)
     * public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
     * ResourceNotFoundException ex,
     * HttpServletRequest request) {
     * 
     * ErrorResponse errorResponse = ErrorResponse.of(
     * HttpStatus.NOT_FOUND.value(),
     * HttpStatus.NOT_FOUND.getReasonPhrase(),
     * ex.getMessage(),
     * request.getRequestURI()
     * );
     * 
     * return ResponseEntity
     * .status(HttpStatus.NOT_FOUND)
     * .body(errorResponse);
     * }
     */
}