package com.taskmanagement.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * タスクエンティティ
 * 
 * このクラスはデータベースの「tasks」テーブルと対応します。
 * ユーザーとタスクは「1対多（One-to-Many）」の関係です。
 * - 1人のユーザーは複数のタスクを持つことができます
 * - 1つのタスクは1人のユーザーに属します
 * 
 * 実務でのポイント：
 * - @ManyToOneアノテーションで、多対一の関係を定義します
 * - @Enumerated(EnumType.STRING)で、Enumを文字列としてデータベースに保存します
 * （ENUMType.ORDINALは使用しない - 順序が変わるとデータが壊れるため）
 * - タスクにはタイトル、詳細、期日、ステータス、優先度などの属性があります
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    /**
     * タスクID（主キー）
     * 
     * @Id: このフィールドが主キーであることを示します
     * @GeneratedValue: 主キーの値を自動生成します
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * タスクタイトル
     * 
     * 実務では、タイトルは必須で、適切な文字数制限を設けます
     * 
     * @NotBlank: 空文字やnullを許可しない
     * @Size: 最大文字数を設定（データベースのVARCHAR長と合わせる）
     */
    @Column(nullable = false)
    @NotBlank(message = "タイトルは必須です")
    @Size(max = 200, message = "タイトルは200文字以内である必要があります")
    private String title;

    /**
     * タスクの詳細説明
     * 
     * 実務では、詳細説明は任意（nullable = true）にすることが多いです
     * 
     * @Column(columnDefinition = "TEXT"): 長いテキストを保存できるようにする
     * 
     *                          注意点：
     *                          - TEXTカラムは検索が遅くなる可能性があるため、検索対象にする場合は全文検索インデックスの追加を検討
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "詳細は5000文字以内である必要があります")
    private String description;

    /**
     * タスクの期日
     * 
     * 実務では、期日は任意にすることが多いです（期日のないタスクも存在するため）
     * LocalDate型を使用することで、日付のみを扱います（時刻は含まない）
     */
    private LocalDate dueDate;

    /**
     * タスクのステータス
     * 
     * @Enumerated(EnumType.STRING): Enumを文字列としてデータベースに保存
     * - EnumType.STRING: "TODO", "IN_PROGRESS", "DONE" のように保存される
     * - EnumType.ORDINAL: 0, 1, 2 のように保存される（非推奨）
     * 
     * なぜENUMType.ORDINALは避けるべきか：
     * - Enumの順序が変わると、既存のデータが壊れる
     * - 例: TODOとIN_PROGRESSの順序を入れ替えると、0と1の意味が逆転してしまう
     * 
     * デフォルト値の設定：
     * - 新しいタスクは「未着手（TODO）」から始まる
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "ステータスは必須です")
    private TaskStatus status = TaskStatus.TODO;

    /**
     * タスクの優先度
     * 
     * デフォルト値は「中（MEDIUM）」とします
     * ユーザーが優先度を指定しない場合、自動的にMEDIUMが設定されます
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "優先度は必須です")
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * タスクの所有者（ユーザー）
     * 
     * @ManyToOne: 多対一の関係を定義
     *             - 複数のタスク（Many）が1人のユーザー（One）に属する
     * 
     * @JoinColumn: 外部キーのカラム名を指定
     *              - name = "user_id": データベースのカラム名
     *              - nullable = false: ユーザーIDは必須（タスクは必ずユーザーに属する）
     * 
     *              実務での注意点：
     *              - fetchType =
     *              FetchType.LAZYを指定すると、ユーザー情報が必要になるまで取得されない（パフォーマンス向上）
     *              - しかし、今回はシンプルにするため、デフォルト（EAGER）を使用します
     * 
     *              落とし穴：
     *              - @ManyToOne側で双方向関係を作る場合、User側に@OneToMany(mappedBy = "user")が必要
     *              - しかし、今回は単方向関係（Task → User）のみで実装します（シンプルさ優先）
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーは必須です")
    private User user;

    /**
     * タスク作成日時
     * 
     * @Column(nullable = false, updatable = false):
     *                  - nullable = false: nullを許可しない
     *                  - updatable = false: 一度設定したら更新できない
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最終更新日時
     * 
     * タスクが更新されるたびに、この値も自動的に更新されます
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
     *              - デフォルト値の設定もここで行います
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // ステータスと優先度のデフォルト値設定
        // フィールドレベルでの初期化でも同じ効果がありますが、
        // @PrePersistで明示的に設定することで、意図が明確になります
        if (this.status == null) {
            this.status = TaskStatus.TODO;
        }
        if (this.priority == null) {
            this.priority = TaskPriority.MEDIUM;
        }
    }

    /**
     * エンティティが更新される直前に自動実行されるメソッド
     * 
     * @PreUpdate: JPAがエンティティをデータベースで更新する直前に実行されます
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}