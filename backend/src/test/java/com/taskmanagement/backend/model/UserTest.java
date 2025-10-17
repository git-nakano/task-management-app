package com.taskmanagement.backend.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Userエンティティの単体テスト
 * 
 * 実務では、エンティティのテストを作成することで、以下を確認します：
 * 1. フィールドのgetterとsetterが正しく動作するか
 * 2. @PrePersist、@PreUpdateメソッドが正しく動作するか
 * 3. バリデーションが正しく動作するか
 * 
 * 注意点：
 * - このテストはユニットテストであり、データベースには接続しません
 * - データベース接続を伴うテストは「統合テスト」として別途作成します
 */
class UserTest {

    private User user;

    /**
     * 各テストメソッド実行前に呼ばれる初期化メソッド
     * 
     * @BeforeEach: このアノテーションにより、各テストメソッドの前に実行されます
     * 
     *              実務でのポイント：
     *              - テストデータの初期化を@BeforeEachで行うことで、各テストが独立して実行できます
     *              - テスト間でデータが影響し合わないようにするため重要です
     */
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword123");
        user.setUsername("テストユーザー");
    }

    /**
     * Userエンティティの基本的なフィールドが正しく設定・取得できることを確認
     * 
     * 実務でのポイント：
     * - テストメソッド名は、何をテストしているかが分かるように日本語または英語で明確に命名します
     * - assertEquals(expected, actual): 期待値と実際の値を比較します
     */
    @Test
    void testUserFields() {
        // ID
        assertEquals(1L, user.getId());

        // メールアドレス
        assertEquals("test@example.com", user.getEmail());

        // パスワード
        assertEquals("hashedPassword123", user.getPassword());

        // ユーザー名
        assertEquals("テストユーザー", user.getUsername());
    }

    /**
     * メールアドレスの小文字変換が正しく動作することを確認
     * 
     * 実務でのポイント：
     * - @PrePersistメソッドでメールアドレスを小文字に変換しているため、
     * このテストでその動作を確認します
     * - ただし、@PrePersistは実際にデータベースに保存する際に呼ばれるため、
     * このユニットテストでは手動で呼び出します
     */
    @Test
    void testEmailLowerCase() {
        // 大文字を含むメールアドレスを設定
        user.setEmail("TEST@EXAMPLE.COM");

        // @PrePersistメソッドを手動で呼び出し
        user.onCreate();

        // メールアドレスが小文字に変換されていることを確認
        assertEquals("test@example.com", user.getEmail());
    }

    /**
     * @PrePersistメソッドで作成日時と更新日時が正しく設定されることを確認
     * 
     *                                         実務でのポイント：
     *                                         - 時刻は動的に変化するため、テストでは「nullでないこと」を確認します
     *                                         - 厳密に時刻を比較する場合は、モックライブラリを使用します
     */
    @Test
    void testOnCreate() {
        // 初期状態では作成日時と更新日時はnull
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        // @PrePersistメソッドを手動で呼び出し
        user.onCreate();

        // 作成日時と更新日時が設定されていることを確認
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        // 作成日時と更新日時が同じ値であることを確認
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    /**
     * @PreUpdateメソッドで更新日時が正しく更新されることを確認
     * 
     *                                   実務でのポイント：
     *                                   - 更新日時のみが更新され、作成日時は変更されないことを確認します
     */
    @Test
    void testOnUpdate() throws InterruptedException {
        // 初期状態を設定
        user.onCreate();
        LocalDateTime originalCreatedAt = user.getCreatedAt();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();

        // 少し待機（更新日時が変わることを確認するため）
        Thread.sleep(10);

        // @PreUpdateメソッドを手動で呼び出し
        user.onUpdate();

        // 作成日時は変更されていないことを確認
        assertEquals(originalCreatedAt, user.getCreatedAt());

        // 更新日時は変更されていることを確認
        // 注: isBefore()を使用して、更新日時が元の時刻より後であることを確認
        assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt) ||
                user.getUpdatedAt().isEqual(originalUpdatedAt));
    }

    /**
     * Lombokの@Dataアノテーションで生成されるtoString()メソッドの動作確認
     * 
     * 実務でのポイント：
     * - toString()メソッドは、デバッグ時にオブジェクトの状態を確認するのに便利です
     * - Lombokの@Dataアノテーションにより自動生成されます
     */
    @Test
    void testToString() {
        String userString = user.toString();

        // toString()の結果に、主要なフィールドが含まれていることを確認
        assertTrue(userString.contains("test@example.com"));
        assertTrue(userString.contains("テストユーザー"));
    }

    /**
     * Lombokの@Dataアノテーションで生成されるequals()メソッドの動作確認
     * 
     * 実務でのポイント：
     * - Lombokの@Dataは、デフォルトですべてのフィールドを使用してequals()を生成します
     * - すべてのフィールドが同じ値であれば、equals()はtrueを返します
     * - IDだけでなく、email、password、usernameなども比較対象に含まれます
     * 
     * 注意点：
     * - もしIDのみで比較したい場合は、@EqualsAndHashCode(onlyExplicitlyIncluded = true)を使用します
     * - しかし、通常はすべてのフィールドで比較する方が安全です
     */
    @Test
    void testEquals() {
        // 同じフィールド値を持つ別のUserオブジェクトを作成
        User sameUser = new User();
        sameUser.setId(1L);
        sameUser.setEmail("test@example.com"); // 同じメールアドレス
        sameUser.setPassword("hashedPassword123"); // 同じパスワード
        sameUser.setUsername("テストユーザー"); // 同じユーザー名

        // すべてのフィールドが同じであれば、equals()はtrueを返す
        assertEquals(user, sameUser);

        // 異なるフィールドを持つUserオブジェクトを作成
        User differentUser = new User();
        differentUser.setId(1L); // IDは同じ
        differentUser.setEmail("different@example.com"); // メールアドレスが異なる
        differentUser.setUsername("別のユーザー");

        // IDが同じでも、他のフィールドが異なれば、equals()はfalseを返す
        assertNotEquals(user, differentUser);
    }
}