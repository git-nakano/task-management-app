package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ログインリクエストDTO
 * 
 * このDTOの役割：
 * - ユーザーのログイン情報（メールアドレス、パスワード）を受け取る
 * - バリデーションを実施
 * 
 * なぜDTOを使うのか：
 * 1. バリデーション：不正なデータを早期に検出
 * 2. セキュリティ：必要な情報のみを受け取る
 * 3. ドキュメント化：APIの仕様を明確にする
 * 
 * 実務でのポイント：
 * - パスワードは平文で受け取りますが、通信はHTTPSで暗号化されます
 * - サーバー側でパスワードをハッシュ化して、データベースの値と比較します
 * - ログイン成功時は、JWTトークンを返します
 * 
 * リクエスト例：
 * POST /api/auth/login
 * {
 * "email": "test@example.com",
 * "password": "password123"
 * }
 * 
 * レスポンス例：
 * {
 * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 * "user": {
 * "id": 1,
 * "email": "test@example.com",
 * "username": "テストユーザー"
 * }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    /**
     * メールアドレス
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * - @Email：メールアドレスの形式であることを確認
     * 
     * 実務でのポイント：
     * - メールアドレスは大文字小文字を区別しないことが一般的
     * - ログイン時には、小文字に変換してから検索することを推奨
     */
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    /**
     * パスワード
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * 
     * 実務でのポイント：
     * - パスワードは平文で受け取りますが、通信はHTTPSで暗号化されます
     * - サーバー側でBCryptなどでハッシュ化されたパスワードと比較します
     * - パスワードの最小文字数などのバリデーションは、新規登録時に行います
     */
    @NotBlank(message = "パスワードは必須です")
    private String password;
}