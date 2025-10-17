package com.taskmanagement.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security設定
 * 
 * @Configuration:
 *                 - このクラスが設定クラスであることを示します
 * 
 * @EnableWebSecurity:
 *                     - Spring Securityを有効にします
 * 
 *                     Spring Securityとは：
 *                     - Javaアプリケーションのセキュリティフレームワーク
 *                     - 認証（Authentication）と認可（Authorization）を提供
 *                     - CSRF保護、セッション管理、パスワードハッシュ化などの機能を提供
 * 
 *                     認証（Authentication）：
 *                     - 「あなたは誰ですか？」を確認
 *                     - ログイン時にユーザー名とパスワードで確認
 * 
 *                     認可（Authorization）：
 *                     - 「あなたは何ができますか？」を確認
 *                     - ログイン後、リソースへのアクセス権限を確認
 * 
 *                     実務でのポイント：
 *                     - セキュリティ設定は、アプリケーションの要件に応じてカスタマイズします
 *                     - 開発環境と本番環境で異なる設定を使用することが一般的です
 *                     - CSRF保護は、本番環境では有効にすることを推奨します
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードエンコーダーのBean定義
     * 
     * BCryptPasswordEncoderとは：
     * - パスワードをハッシュ化するためのエンコーダー
     * - ソルト付きハッシュ化を自動で行う
     * - レインボーテーブル攻撃に強い
     * 
     * BCryptの仕組み：
     * 1. ソルト（ランダムな文字列）を生成
     * 2. パスワードとソルトを組み合わせてハッシュ化
     * 3. ソルトとハッシュ値を一緒に保存
     * 
     * なぜBCryptか：
     * - ソルト付きハッシュ化により、レインボーテーブル攻撃に強い
     * - 計算コストが高く、ブルートフォース攻撃に時間がかかる
     * - Spring Securityが標準でサポート
     * 
     * 実務でのポイント：
     * - パスワードは絶対に平文で保存してはいけません
     * - BCryptは、Spring Securityで推奨されるハッシュ化方式です
     * - 他の選択肢：Argon2、PBKDF2など
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * SecurityFilterChainのBean定義
     * 
     * SecurityFilterChainとは：
     * - HTTPリクエストに対するセキュリティフィルタのチェーン
     * - リクエストごとに、認証・認可を行う
     * 
     * この設定で行うこと：
     * 1. CSRF保護を無効化（開発環境のみ）
     * 2. エンドポイントごとに認証の要否を設定
     * 3. HTTP Basic認証を有効化
     * 
     * 実務でのポイント：
     * - 開発環境では、CSRF保護を無効化することが多いです
     * - 本番環境では、CSRF保護を有効にすることを推奨します
     * - エンドポイントごとに、適切な認可設定を行います
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 設定エラー
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF保護を無効化
                // CSRF（Cross-Site Request Forgery）とは：
                // - 悪意のあるサイトから、ユーザーの意図しないリクエストを送信する攻撃
                // - Spring Securityは、デフォルトでCSRF保護を有効にします
                //
                // なぜ無効化するのか：
                // - REST APIでは、CSRFトークンを使用しない認証方式（JWT等）を使用することが多い
                // - フロントエンドとバックエンドが分離している場合、CSRF保護は不要
                // - 開発環境では、CSRF保護を無効化することでテストが容易になる
                //
                // 実務でのポイント：
                // - セッションベースの認証（Cookie使用）では、CSRF保護を有効にすることを推奨
                // - JWTトークンベースの認証では、CSRF保護は不要
                // - 本番環境の要件に応じて、適切に設定してください
                .csrf(csrf -> csrf.disable())

                // エンドポイントごとに認証の要否を設定
                .authorizeHttpRequests(auth -> auth
                        // 認証エンドポイントは誰でもアクセス可能
                        // /api/auth/register: 新規登録
                        // /api/auth/login: ログイン
                        .requestMatchers("/api/auth/**").permitAll()

                        // H2コンソールへのアクセスを許可（開発環境のみ）
                        .requestMatchers("/h2-console/**").permitAll()

                        // その他のすべてのリクエストは認証が必要
                        // 実務でのポイント：
                        // - より細かい認可設定が必要な場合は、ロールベースのアクセス制御（RBAC）を使用します
                        // - 例：.requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                // H2コンソールのフレーム表示を許可（開発環境のみ）
                // H2コンソールは、iframeを使用するため、フレーム表示を許可する必要があります
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()))

                // HTTP Basic認証を有効化
                // HTTP Basic認証とは：
                // - ユーザー名とパスワードをBase64エンコードして送信する認証方式
                // - シンプルで実装が容易
                // - HTTPS通信が必須（平文で送信されるため）
                //
                // なぜHTTP Basic認証を使用するのか：
                // - Phase 2-5では、シンプルな認証機能を実装します
                // - JWTトークンベースの認証は、Phase 2-6以降で実装します
                //
                // 実務でのポイント：
                // - HTTP Basic認証は、開発環境やAPIテストで使用されます
                // - 本番環境では、JWTトークンベースの認証を使用することが一般的です
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    /**
     * SecurityConfigの説明（コメント）
     * 
     * Phase 2-5での実装方針：
     * - パスワードのハッシュ化（BCryptPasswordEncoder）
     * - 基本的な認証機能（HTTP Basic認証）
     * - エンドポイントごとの認可設定
     * 
     * Phase 2-6以降での実装予定：
     * - JWTトークンベースの認証
     * - JwtAuthenticationFilterの追加
     * - リフレッシュトークン機能
     * 
     * 段階的な実装の利点：
     * - 各Phaseで動作する状態を維持
     * - Spring Securityの基本を理解
     * - デバッグが容易
     */
}