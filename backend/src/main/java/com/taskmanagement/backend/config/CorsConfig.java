package com.taskmanagement.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS設定
 * 
 * CORSとは（Cross-Origin Resource Sharing）：
 * - 異なるオリジン（ドメイン、プロトコル、ポート）からのリソースへのアクセスを制御するセキュリティ機能
 * - ブラウザが実装しているセキュリティ機能
 * 
 * なぜCORS設定が必要か：
 * - フロントエンド（http://localhost:5173）とバックエンド（http://localhost:8080）は異なるポート
 * - ブラウザのセキュリティ機能により、デフォルトでは異なるオリジンからのリクエストはブロックされる
 * - CORS設定により、特定のオリジンからのリクエストを許可する
 * 
 * CORS設定が必要な場面：
 * 1. 開発環境：フロントエンドとバックエンドが異なるポートで動作
 * 2. 本番環境：フロントエンドとバックエンドが異なるドメインで動作
 * 
 * 実務でのポイント：
 * - 開発環境と本番環境で異なる設定を使用する
 * - セキュリティのため、必要最小限のオリジンのみを許可する
 * - 本番環境では、ワイルドカード（*）を使用しない
 * 
 * この設定で許可すること：
 * - 特定のオリジン（http://localhost:5173）からのリクエスト
 * - すべてのHTTPメソッド（GET、POST、PUT、DELETE など）
 * - すべてのヘッダー
 * - クレデンシャル（Cookie、認証情報）の送信
 */
@Configuration
public class CorsConfig {

    /**
     * 許可するオリジン（フロントエンドのURL）
     * 
     * application.propertiesから読み込み：
     * app.cors.allowed-origins=http://localhost:5173
     * 
     * 実務でのポイント：
     * - 環境変数や設定ファイルから読み込むことで、環境ごとに異なる設定を使用できる
     * - 開発環境：http://localhost:5173
     * - 本番環境：https://your-app.com
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * CORS設定を行うBean
     * 
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * CORSマッピングを追加
             * 
             * 設定内容：
             * 1. addMapping("/**")：すべてのパスに対してCORS設定を適用
             * 2. allowedOrigins(allowedOrigins)：特定のオリジンからのリクエストを許可
             * 3. allowedMethods("*")：すべてのHTTPメソッドを許可
             * 4. allowedHeaders("*")：すべてのヘッダーを許可
             * 5. allowCredentials(true)：クレデンシャル（Cookie、認証情報）を許可
             * 
             * 実務での注意点：
             * - allowCredentials(true)を使用する場合、allowedOrigins("*")は使用できません
             * - セキュリティのため、必要最小限の設定にする
             * - 本番環境では、allowedMethods()とallowedHeaders()も具体的に指定することを推奨
             * 
             * @param registry CorsRegistry
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // すべてのパスに対して
                        .allowedOrigins(allowedOrigins) // 特定のオリジンからのリクエストを許可
                        .allowedMethods("*") // すべてのHTTPメソッドを許可
                        .allowedHeaders("*") // すべてのヘッダーを許可
                        .allowCredentials(true) // クレデンシャルを許可
                        .maxAge(3600); // プリフライトリクエストのキャッシュ時間（秒）
            }
        };
    }

    /**
     * CORS設定の説明（コメント）
     * 
     * プリフライトリクエストとは：
     * - ブラウザが実際のリクエストを送信する前に、OPTIONSメソッドでサーバーに確認リクエストを送る
     * - サーバーがCORSを許可しているか確認するため
     * - maxAge()は、このプリフライトリクエストの結果をキャッシュする時間
     * 
     * 開発時のトラブルシューティング：
     * - CORSエラーが発生した場合：
     * 1. allowedOriginsの設定が正しいか確認
     * 2. フロントエンドのURLが設定と一致しているか確認
     * 3. ブラウザの開発者ツールでCORSヘッダーを確認
     * 
     * - "Access-Control-Allow-Origin" エラーが出る場合：
     * 1. allowedOrigins()の設定を確認
     * 2. allowCredentials(true)とallowedOrigins("*")を同時に使用していないか確認
     * 
     * 本番環境での設定例：
     * - allowedOrigins("https://your-app.com")
     * - allowedMethods("GET", "POST", "PUT", "DELETE")
     * - allowedHeaders("Content-Type", "Authorization")
     */
}