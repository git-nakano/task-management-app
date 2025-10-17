package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新規登録リクエストDTO
 * 
 * このDTOの役割：
 * - ユーザーの新規登録情報を受け取る
 * - バリデーションを実施
 * 
 * なぜDTOを使うのか：
 * 1. バリデーション：不正なデータを早期に検出
 * 2. セキュリティ：必要な情報のみを受け取る
 * 3. ドキュメント化：APIの仕様を明確にする
 * 
 * 実務でのポイント：
 * - パスワードは平文で受け取りますが、サーバー側でハッシュ化します
 * - メールアドレスの重複チェックはService層で行います
 * - 登録成功時は、JWTトークンを返します（自動ログイン）
 * 
 * リクエスト例：
 * POST /api/auth/register
 * {
 * "email": "test@example.com",
 * "password": "password123",
 * "username": "テストユーザー"
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
public class RegisterRequestDto {

    /**
     * メールアドレス
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * - @Email：メールアドレスの形式であることを確認
     * 
     * 実務でのポイント：
     * - メールアドレスの重複チェックはService層で行います
     * - 大文字小文字を区別しないため、小文字に変換して保存します
     */
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;

    /**
     * パスワード
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * - @Size：最低8文字、最大100文字
     * 
     * 実務でのポイント：
     * - パスワードは平文で受け取りますが、サーバー側でBCryptでハッシュ化します
     * - 最小文字数は8文字を推奨（セキュリティ対策）
     * - 最大文字数は100文字程度（BCryptの制限を考慮）
     * - より強固なパスワードポリシー（大文字・小文字・数字・記号の組み合わせ）は、
     * フロントエンドで実装することを推奨
     */
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内である必要があります")
    private String password;

    /**
     * ユーザー名
     * 
     * バリデーション：
     * - @NotBlank：空文字やnullを許可しない
     * - @Size：最大50文字
     * 
     * 実務でのポイント：
     * - ユーザー名は表示名として使用します
     * - メールアドレスとは異なり、重複を許可することが多いです
     */
    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以内である必要があります")
    private String username;
}