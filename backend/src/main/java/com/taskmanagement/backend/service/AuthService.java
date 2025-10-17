package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.AuthResponseDto;
import com.taskmanagement.backend.dto.LoginRequestDto;
import com.taskmanagement.backend.dto.RegisterRequestDto;
import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.model.User;
import com.taskmanagement.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認証サービス
 * 
 * このサービスの役割：
 * - ユーザーの新規登録（パスワードのハッシュ化）
 * - ログイン（パスワードの検証）
 * 
 * 実務でのポイント：
 * - パスワードは必ずハッシュ化して保存します
 * - ログイン時は、ハッシュ化されたパスワードと比較します
 * - Phase 2-5では、シンプルな認証機能を実装します
 * - Phase 2-6以降で、JWTトークンベースの認証に移行します
 * 
 * @Service:
 *           - このクラスがServiceレイヤーのコンポーネントであることを示します
 * 
 * @RequiredArgsConstructor (Lombok):
 *                          - finalフィールドを引数に持つコンストラクタを自動生成します
 * 
 * @Transactional:
 *                 - トランザクション管理を自動化します
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    /**
     * ユーザーリポジトリ
     */
    private final UserRepository userRepository;

    /**
     * パスワードエンコーダー
     * 
     * PasswordEncoderとは：
     * - パスワードをハッシュ化するためのインターフェース
     * - SecurityConfigで定義したBCryptPasswordEncoderが注入されます
     * 
     * 実務でのポイント：
     * - パスワードは絶対に平文で保存してはいけません
     * - BCryptは、ソルト付きハッシュ化を自動で行います
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * ユーザーを登録
     * 
     * 処理の流れ：
     * 1. メールアドレスの重複チェック
     * 2. パスワードをハッシュ化
     * 3. ユーザーをデータベースに保存
     * 4. UserResponseDtoを返す（Phase 2-5）
     * 
     * Phase 2-6以降での変更予定：
     * - JWTトークンを生成して返す
     * - AuthResponseDtoを返す
     * 
     * @param registerDto 新規登録リクエストDTO
     * @return UserResponseDto
     * @throws IllegalArgumentException メールアドレスが既に使用されている場合
     */
    @Transactional
    public UserResponseDto register(RegisterRequestDto registerDto) {
        // メールアドレスの重複チェック
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("このメールアドレスは既に使用されています");
        }

        // ユーザーエンティティを作成
        User user = new User();
        user.setEmail(registerDto.getEmail().toLowerCase()); // メールアドレスを小文字に変換

        // パスワードをハッシュ化
        // 実務でのポイント：
        // - passwordEncoder.encode()は、ソルト付きハッシュ化を行います
        // - 同じパスワードでも、毎回異なるハッシュ値が生成されます（ソルトがランダムなため）
        // - ハッシュ値には、ソルトも含まれています
        String hashedPassword = passwordEncoder.encode(registerDto.getPassword());
        user.setPassword(hashedPassword);

        user.setUsername(registerDto.getUsername());

        // データベースに保存
        User savedUser = userRepository.save(user);

        // DTOに変換して返す
        // Phase 2-5では、UserResponseDtoを返します
        // Phase 2-6以降では、JWTトークンを含むAuthResponseDtoを返す予定です
        return UserResponseDto.fromEntity(savedUser);
    }

    /**
     * ログイン
     * 
     * 処理の流れ：
     * 1. メールアドレスでユーザーを検索
     * 2. パスワードを検証
     * 3. UserResponseDtoを返す（Phase 2-5）
     * 
     * Phase 2-6以降での変更予定：
     * - JWTトークンを生成して返す
     * - AuthResponseDtoを返す
     * 
     * @param loginDto ログインリクエストDTO
     * @return UserResponseDto
     * @throws IllegalArgumentException メールアドレスまたはパスワードが正しくない場合
     */
    public UserResponseDto login(LoginRequestDto loginDto) {
        // メールアドレスでユーザーを検索
        User user = userRepository.findByEmail(loginDto.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません"));

        // パスワードを検証
        // 実務でのポイント：
        // - passwordEncoder.matches()は、平文のパスワードとハッシュ化されたパスワードを比較します
        // - ソルトを含めて比較するため、セキュアです
        // - エラーメッセージは、「メールアドレスまたはパスワードが正しくありません」とします
        // （どちらが間違っているか特定できないようにする：セキュリティ対策）
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("メールアドレスまたはパスワードが正しくありません");
        }

        // DTOに変換して返す
        // Phase 2-5では、UserResponseDtoを返します
        // Phase 2-6以降では、JWTトークンを含むAuthResponseDtoを返す予定です
        return UserResponseDto.fromEntity(user);
    }

    /**
     * メールアドレスの存在確認
     * 
     * 実務での使用場面：
     * - 新規登録前のメールアドレス重複チェック
     * - フロントエンドでリアルタイムバリデーション
     * 
     * @param email メールアドレス
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    /**
     * AuthServiceの説明（コメント）
     * 
     * Phase 2-5での実装方針：
     * - パスワードのハッシュ化（BCryptPasswordEncoder）
     * - シンプルな認証機能（UserResponseDtoを返す）
     * 
     * Phase 2-6以降での実装予定：
     * - JWTトークンの生成
     * - AuthResponseDtoを返す（トークン + ユーザー情報）
     * 
     * セキュリティのポイント：
     * - パスワードは必ずハッシュ化して保存
     * - エラーメッセージは曖昧にする（どちらが間違っているか特定できないようにする）
     * - メールアドレスは小文字に変換して保存・検索
     */
}