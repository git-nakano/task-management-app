package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ユーザーコントローラー
 * 
 * @RestController:
 *                  - このクラスがRESTful APIのコントローラーであることを示します
 *                  - @Controller + @ResponseBody の省略形
 *                  - すべてのメソッドの戻り値がJSONに変換されます
 * 
 *                  @RequestMapping("/api/users"):
 *                  - このコントローラーのベースパスを指定します
 *                  - すべてのエンドポイントは /api/users 以下になります
 * 
 *                  REST APIの設計原則：
 *                  - リソース指向：URLはリソース（ユーザー、タスク）を表す
 *                  - HTTPメソッド：CRUD操作をHTTPメソッドで表現
 *                  - GET: 取得
 *                  - POST: 作成
 *                  - PUT: 更新
 *                  - DELETE: 削除
 * 
 *                  エンドポイント一覧：
 *                  - GET /api/users/{id} - IDでユーザーを取得
 *                  - GET /api/users/email/{email} - メールアドレスでユーザーを取得
 *                  - POST /api/users - ユーザーを作成
 *                  - PUT /api/users/{id} - ユーザー情報を更新
 *                  - PUT /api/users/{id}/password - パスワードを更新
 *                  - DELETE /api/users/{id} - ユーザーを削除
 *                  - GET /api/users/count - ユーザー総数を取得
 * 
 *                  実務でのポイント：
 *                  - Controller層はHTTPリクエスト/レスポンスの処理のみを行う
 *                  - ビジネスロジックはService層に委譲する
 *                  - 例外処理はGlobalExceptionHandlerに任せる
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    /**
     * ユーザーサービス
     */
    private final UserService userService;

    /**
     * IDでユーザーを取得
     * 
     * エンドポイント：GET /api/users/{id}
     * 
     * パスパラメータ：
     * - id: ユーザーID
     * 
     * レスポンス：
     * - 200 OK: ユーザーが見つかった場合
     * - 404 Not Found: ユーザーが見つからない場合
     * 
     * 実務でのポイント：
     * - ResponseEntity<T>を使用することで、HTTPステータスコードを制御できます
     * - Optional<T>から値を取り出すために、map()とorElse()を使用します
     * 
     * 使用例：
     * GET http://localhost:8080/api/users/1
     * 
     * @param id ユーザーID
     * @return ResponseEntity<UserResponseDto>
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok) // 見つかった場合は200 OKを返す
                .orElse(ResponseEntity.notFound().build()); // 見つからない場合は404を返す
    }

    /**
     * メールアドレスでユーザーを取得
     * 
     * エンドポイント：GET /api/users/email/{email}
     * 
     * パスパラメータ：
     * - email: メールアドレス
     * 
     * レスポンス：
     * - 200 OK: ユーザーが見つかった場合
     * - 404 Not Found: ユーザーが見つからない場合
     * 
     * 実務での使用場面：
     * - ログイン処理
     * - ユーザー検索
     * 
     * 使用例：
     * GET http://localhost:8080/api/users/email/test@example.com
     * 
     * @param email メールアドレス
     * @return ResponseEntity<UserResponseDto>
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ユーザーを作成
     * 
     * エンドポイント：POST /api/users
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
     * 実務でのポイント：
     * - @RequestBodyでJSONをJavaオブジェクトに変換
     * - HTTPステータスコード201（Created）を返すことで、リソースが作成されたことを明示
     * - パスワードは平文で受け取りますが、Service層でハッシュ化する前提
     * 
     * セキュリティ上の注意：
     * - 今回は簡易実装のため、パスワードを平文で受け取っています
     * - 実務では、Spring Securityを使用してパスワードをハッシュ化します
     * - Phase 2-5で認証機能を実装する際に、適切に処理します
     * 
     * 使用例：
     * POST http://localhost:8080/api/users
     * Content-Type: application/json
     * 
     * {
     * "email": "test@example.com",
     * "password": "password123",
     * "username": "テストユーザー"
     * }
     * 
     * @param email    メールアドレス
     * @param password パスワード
     * @param username ユーザー名
     * @return ResponseEntity<UserResponseDto>
     */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String username) {

        UserResponseDto createdUser = userService.createUser(email, password, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * ユーザー情報を更新
     * 
     * エンドポイント：PUT /api/users/{id}
     * 
     * パスパラメータ：
     * - id: ユーザーID
     * 
     * リクエストパラメータ：
     * - username: 新しいユーザー名
     * 
     * レスポンス：
     * - 200 OK: ユーザー情報が更新された場合
     * - 404 Not Found: ユーザーが見つからない場合
     * 
     * 実務でのポイント：
     * - パスワードの更新は別エンドポイントで行います（セキュリティ対策）
     * - @RequestParamでクエリパラメータを受け取ります
     * 
     * 使用例：
     * PUT http://localhost:8080/api/users/1?username=新しいユーザー名
     * 
     * @param id       ユーザーID
     * @param username 新しいユーザー名
     * @return ResponseEntity<UserResponseDto>
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @RequestParam String username) {

        UserResponseDto updatedUser = userService.updateUser(id, username);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * パスワードを更新
     * 
     * エンドポイント：PUT /api/users/{id}/password
     * 
     * パスパラメータ：
     * - id: ユーザーID
     * 
     * リクエストパラメータ：
     * - newPassword: 新しいパスワード
     * 
     * レスポンス：
     * - 200 OK: パスワードが更新された場合
     * - 404 Not Found: ユーザーが見つからない場合
     * 
     * 実務でのポイント：
     * - パスワード更新は専用のエンドポイントを用意します
     * - 現在のパスワードの確認や、追加のセキュリティチェックを行うことを推奨
     * - 実務では、Spring Securityを使用してパスワードをハッシュ化します
     * 
     * セキュリティ上の注意：
     * - 今回は簡易実装のため、パスワードを平文で受け取っています
     * - Phase 2-5で認証機能を実装する際に、適切に処理します
     * 
     * 使用例：
     * PUT http://localhost:8080/api/users/1/password?newPassword=newPassword123
     * 
     * @param id          ユーザーID
     * @param newPassword 新しいパスワード
     * @return ResponseEntity<UserResponseDto>
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<UserResponseDto> updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {

        UserResponseDto updatedUser = userService.updatePassword(id, newPassword);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * ユーザーを削除
     * 
     * エンドポイント：DELETE /api/users/{id}
     * 
     * パスパラメータ：
     * - id: ユーザーID
     * 
     * レスポンス：
     * - 204 No Content: ユーザーが削除された場合
     * - 404 Not Found: ユーザーが見つからない場合
     * 
     * 実務でのポイント：
     * - HTTPステータスコード204（No Content）を返すことで、削除成功を示します
     * - レスポンスボディは空（null）です
     * 
     * 使用例：
     * DELETE http://localhost:8080/api/users/1
     * 
     * @param id ユーザーID
     * @return ResponseEntity<Void>
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ユーザー総数を取得
     * 
     * エンドポイント：GET /api/users/count
     * 
     * レスポンス：
     * - 200 OK: ユーザー総数
     * 
     * 実務での使用場面：
     * - 管理画面での統計情報表示
     * 
     * 使用例：
     * GET http://localhost:8080/api/users/count
     * 
     * レスポンス例：
     * 42
     * 
     * @return ResponseEntity<Long>
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        long count = userService.countUsers();
        return ResponseEntity.ok(count);
    }
}