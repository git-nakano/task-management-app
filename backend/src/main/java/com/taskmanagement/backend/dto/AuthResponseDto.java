package com.taskmanagement.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 認証レスポンスDTO
 * 
 * このDTOの役割：
 * - ログインまたは新規登録成功時に、JWTトークンとユーザー情報を返す
 * 
 * なぜDTOを使うのか：
 * 1. セキュリティ：パスワードを含めない
 * 2. 柔軟性：APIの形式を自由に変更できる
 * 3. ドキュメント化：APIの仕様を明確にする
 * 
 * 実務でのポイント：
 * - JWTトークンは、フロントエンド側で保存します（LocalStorageまたはCookie）
 * - 以降のAPIリクエストでは、このトークンをAuthorizationヘッダーに含めます
 * - トークンの有効期限は、通常1時間〜24時間程度です
 * 
 * レスポンス例：
 * {
 * "token":
 * "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
 * "user": {
 * "id": 1,
 * "email": "test@example.com",
 * "username": "テストユーザー",
 * "createdAt": "2025-10-17T10:30:00",
 * "updatedAt": "2025-10-17T10:30:00"
 * }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {

    /**
     * JWTトークン
     * 
     * JWTとは（JSON Web Token）：
     * - ユーザーの認証情報を含む署名付きトークン
     * - サーバーが発行し、クライアントが保存する
     * - 以降のリクエストで、このトークンを含めることで認証を行う
     * 
     * JWTの構造：
     * - ヘッダー：トークンのタイプとアルゴリズム
     * - ペイロード：ユーザーID、メールアドレス、有効期限など
     * - 署名：ヘッダーとペイロードをシークレットキーで署名
     * 
     * 実務でのポイント：
     * - トークンは、フロントエンド側でLocalStorageまたはCookieに保存します
     * - Authorizationヘッダーに "Bearer {token}" の形式で含めます
     * - トークンの有効期限が切れたら、再度ログインが必要です
     * - リフレッシュトークンを使用することで、自動的に新しいトークンを取得できます
     */
    private String token;

    /**
     * ユーザー情報
     * 
     * 実務でのポイント：
     * - パスワードは含めません（セキュリティ対策）
     * - フロントエンド側で、ユーザー情報を表示するために使用します
     */
    private UserResponseDto user;

    /**
     * 認証レスポンスを生成する便利メソッド
     * 
     * 使用例：
     * AuthResponseDto response = AuthResponseDto.of(token, userDto);
     * 
     * @param token JWTトークン
     * @param user  ユーザー情報
     * @return AuthResponseDto
     */
    public static AuthResponseDto of(String token, UserResponseDto user) {
        return new AuthResponseDto(token, user);
    }
}