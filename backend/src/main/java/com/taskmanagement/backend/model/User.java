package com.taskmanagement.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ユーザーエンティティ
 * 
 * このクラスはデータベースの「users」テーブルと対応します。
 * @Entityアノテーションにより、JPAがこのクラスをデータベーステーブルとして認識します。
 * 
 * 実務でのポイント：
 * - @Dataアノテーション（Lombok）により、getter/setter/toString/equals/hashCodeが自動生成されます
 * - @NoArgsConstructorと@AllArgsConstructorで、引数なしコンストラクタと全引数コンストラクタが自動生成されます
 * - バリデーションアノテーション（@NotBlank、@Email等）により、不正なデータの登録を防ぎます
 * 
 * 注意点：
 * - パスワードはハッシュ化して保存する必要があります（平文で保存してはいけません）
 * - createdAtとupdatedAtは自動的に設定されます（@PrePersist、@PreUpdate使用）
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * ユーザーID（主キー）
     * 
     * @Id: このフィールドが主キーであることを示します
     * @GeneratedValue: 主キーの値を自動生成します
     *                  strategy = GenerationType.IDENTITY: データベースの自動採番機能を使用します
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * メールアドレス
     * 
     * 実務では、メールアドレスは一意である必要があります（重複登録を防ぐ）
     * 
     * @Column(unique = true): データベースレベルで一意制約を設定
     * @Email: メールアドレスの形式をバリデーション
     * @NotBlank: 空文字やnullを許可しない
     * 
     *            落とし穴：
     *            - メールアドレスは大文字小文字を区別しないため、保存前に小文字に統一することが推奨されます
     */
    @Column(unique = true, nullable = false)
    @Email(message = "メールアドレスの形式が正しくありません")
    @NotBlank(message = "メールアドレスは必須です")
    private String email;

    /**
     * パスワード（ハッシュ化済み）
     * 
     * 実務での重要な注意点：
     * - パスワードは必ずハッシュ化してから保存します（BCryptなどを使用）
     * - 平文で保存すると、データベースが漏洩した際に大きなセキュリティリスクになります
     * - @NotBlank: パスワードは必須
     * - @Size: パスワードの最小文字数を設定（セキュリティ強化）
     */
    @Column(nullable = false)
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, message = "パスワードは8文字以上である必要があります")
    private String password;

    /**
     * ユーザー名
     * 
     * 実務では、表示用の名前を別途持つことが一般的です
     * メールアドレスだけでは、UIで表示する際に不便なためです
     */
    @Column(nullable = false)
    @NotBlank(message = "ユーザー名は必須です")
    @Size(max = 50, message = "ユーザー名は50文字以内である必要があります")
    private String username;

    /**
     * アカウント作成日時
     * 
     * 実務では、データがいつ作成されたかを記録することが重要です
     * - 監査（誰がいつデータを作成したか）
     * - トラブルシューティング（問題が発生した時期の特定）
     * - 統計分析（ユーザー登録数の推移など）
     * 
     * @Column(nullable = false, updatable = false):
     *                  - nullable = false: nullを許可しない
     *                  - updatable = false: 一度設定したら更新できない（作成日時は変更されないため）
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最終更新日時
     * 
     * 実務では、データがいつ更新されたかを記録することも重要です
     * - ユーザーが最後にいつアクティブだったかを把握
     * - データの鮮度を確認
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * エンティティが初めて保存される直前に自動実行されるメソッド
     * 
     * @PrePersist: JPAがエンティティをデータベースに保存する直前に実行されます
     * 
     *              実務でのポイント：
     *              - 作成日時と更新日時を自動的に設定することで、手動での設定忘れを防ぎます
     *              - メールアドレスを小文字に統一することで、重複チェックを正確に行えます
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // メールアドレスを小文字に統一
        // 例: "User@Example.com" → "user@example.com"
        if (this.email != null) {
            this.email = this.email.toLowerCase();
        }
    }

    /**
     * エンティティが更新される直前に自動実行されるメソッド
     * 
     * @PreUpdate: JPAがエンティティをデータベースで更新する直前に実行されます
     * 
     *             実務でのポイント：
     *             - 更新日時を自動的に設定することで、常に最新の更新時刻を記録できます
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}