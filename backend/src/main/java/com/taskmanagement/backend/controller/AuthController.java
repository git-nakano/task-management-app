package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.LoginRequestDto;
import com.taskmanagement.backend.dto.RegisterRequestDto;
import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認証コントローラー
 * 
 * @RestController:
 *                  - このクラスがRESTful APIのコントローラーであることを示します
 * 
 *                  @RequestMapping("/api/auth"):
 *                  - このコントローラーのベースパスを指定します
 *                  - すべてのエンドポイントは /api/auth 以下になります
 * 
 *                  エンドポイント一覧：
 *                  - POST /api/auth/register - 新規登録
 *                  - POST /api/auth/login - ログイン
 * 
 *                  実務でのポイント：
 *                  - 認証エンドポイントは、SecurityConfigで認証不要に設定されています
 *                  - バリデーションエラーは、GlobalExceptionHandlerで自動処理されます
 *                  - Phase 2-5では、UserResponseDtoを返します
 *                  - Phase 2-6以降では、AuthResponseDto（JWTトークン + ユーザー情報）を返す予定です
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 認証サービス
     */
    private final AuthService authService;

    /**
     * ユーザーを登録
     * 
     * エンドポイント：POST /api/auth/register
     * 
     * リクエストボディ：
     * {
     * "email": "test@example.com",
     * "password": "password123",
     * "username": "テストユーザー"
     * }
     * 
     * レスポンス：
     * - 201 Created: ユーザーが作成された場合
     * - 400 Bad Request: バリデーションエラーまたはメールアドレスが重複している場合
     * 
     * Phase 2-5のレスポンス例：
     * {
     * "id": 1,
     * "email": "test@example.com",
     * "username": "テストユーザー",
     * "createdAt": "2025-10-17T10:30:00",
     * "updatedAt": "2025-10-17T10:30:00"
     * }
     * 
     * Phase 2-6以降のレスポンス例：
     * {
     * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "user": {
     * "id": 1,
     * "email": "test@example.com",
     * "username": "テストユーザー",
     * "createdAt": "2025-10-17T10:30:00",
     * "updatedAt": "2025-10-17T10:30:00"
     * }
     * }
     * 
     * 実務でのポイント：
     * - @Validアノテーションにより、リクエストボディのバリデーションが自動実行されます
     * - バリデーションエラーは、GlobalExceptionHandlerで処理されます
     * - HTTPステータスコード201（Created）を返します
     * - 新規登録成功後は、自動的にログインする（Phase 2-6以降でJWTトークンを返す）
     * 
     * セキュリティのポイント：
     * - パスワードは、Service層で自動的にハッシュ化されます
     * - メールアドレスの重複チェックは、Service層で行います
     * 
     * @param registerDto 新規登録リクエストDTO
     * @return ResponseEntity<UserResponseDto>
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto registerDto) {
        UserResponseDto user = authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * ログイン
     * 
     * エンドポイント：POST /api/auth/login
     * 
     * リクエストボディ：
     * {
     * "email": "test@example.com",
     * "password": "password123"
     * }
     * 
     * レスポンス：
     * - 200 OK: ログイン成功
     * - 400 Bad Request: メールアドレスまたはパスワードが正しくない場合
     * 
     * Phase 2-5のレスポンス例：
     * {
     * "id": 1,
     * "email": "test@example.com",
     * "username": "テストユーザー",
     * "createdAt": "2025-10-17T10:30:00",
     * "updatedAt": "2025-10-17T10:30:00"
     * }
     * 
     * Phase 2-6以降のレスポンス例：
     * {
     * "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     * "user": {
     * "id": 1,
     * "email": "test@example.com",
     * "username": "テストユーザー",
     * "createdAt": "2025-10-17T10:30:00",
     * "updatedAt": "2025-10-17T10:30:00"
     * }
     * }
     * 
     * 実務でのポイント：
     * - @Validアノテーションにより、リクエストボディのバリデーションが自動実行されます
     * - バリデーションエラーは、GlobalExceptionHandlerで処理されます
     * - HTTPステータスコード200（OK）を返します
     * - ログイン成功後は、JWTトークンを返します（Phase 2-6以降）
     * 
     * セキュリティのポイント：
     * - パスワードの検証は、Service層で行います
     * - エラーメッセージは曖昧にします（「メールアドレスまたはパスワードが正しくありません」）
     * 
     * @param loginDto ログインリクエストDTO
     * @return ResponseEntity<UserResponseDto>
     */
    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody LoginRequestDto loginDto) {
        UserResponseDto user = authService.login(loginDto);
        return ResponseEntity.ok(user);
    }

    /**
     * AuthControllerの説明（コメント）
     * 
     * Phase 2-5での実装方針：
     * - シンプルな認証機能（UserResponseDtoを返す）
     * - パスワードのハッシュ化（Service層で自動処理）
     * 
     * Phase 2-6以降での実装予定：
     * - JWTトークンの生成
     * - AuthResponseDtoを返す（トークン + ユーザー情報）
     * 
     * 認証フロー（Phase 2-5）：
     * 1. ユーザーが新規登録 → POST /api/auth/register
     * 2. パスワードがハッシュ化されてデータベースに保存
     * 3. UserResponseDtoが返される
     * 4. ユーザーがログイン → POST /api/auth/login
     * 5. パスワードが検証される
     * 6. UserResponseDtoが返される
     * 
     * 認証フロー（Phase 2-6以降）：
     * 1. ユーザーが新規登録 → POST /api/auth/register
     * 2. パスワードがハッシュ化されてデータベースに保存
     * 3. JWTトークンが生成される
     * 4. AuthResponseDto（トークン + ユーザー情報）が返される
     * 5. フロントエンドがトークンを保存
     * 6. 以降のAPIリクエストで、トークンをAuthorizationヘッダーに含める
     */
}