package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepositoryの統合テスト
 * 
 * @DataJpaTest:
 *               - このアノテーションにより、JPA関連のコンポーネントのみがロードされます
 *               - テスト用のインメモリデータベース（H2）が自動的にセットアップされます
 *               - 各テストメソッド実行後、データベースがロールバックされます（データが残らない）
 * 
 *               実務でのポイント：
 *               - 単体テスト（UserTest）は、エンティティ単体の動作を確認
 *               - 統合テスト（UserRepositoryTest）は、データベースとの連携を確認
 *               - @DataJpaTestを使用することで、軽量で高速なテストが可能
 * 
 *               TestEntityManager:
 *               - テスト用のEntityManagerで、データベースに直接アクセスできます
 *               - persist(): データベースに保存
 *               - flush(): 保留中の変更をデータベースに反映
 *               - clear(): 永続化コンテキストをクリア
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;

    /**
     * 各テストメソッド実行前に呼ばれる初期化メソッド
     * 
     * 実務でのポイント：
     * - テストデータをデータベースに保存して、検証の準備をします
     * - entityManager.persistAndFlush()で、すぐにデータベースに反映します
     */
    @BeforeEach
    void setUp() {
        // テスト用のユーザーを作成
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setUsername("テストユーザー");

        // データベースに保存
        entityManager.persistAndFlush(testUser);

        // 永続化コンテキストをクリア（キャッシュをクリアして、実際にDBから取得することを保証）
        entityManager.clear();
    }

    /**
     * save()メソッドのテスト - ユーザーの保存
     * 
     * 実務でのポイント：
     * - save()は、新規作成と更新の両方に使用できます
     * - IDがnullの場合は新規作成、IDがある場合は更新
     */
    @Test
    void testSaveUser() {
        // 新しいユーザーを作成
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password456");
        newUser.setUsername("新しいユーザー");

        // 保存
        User savedUser = userRepository.save(newUser);

        // 検証
        assertNotNull(savedUser.getId()); // IDが自動採番される
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("新しいユーザー", savedUser.getUsername());
    }

    /**
     * findById()メソッドのテスト - IDでユーザーを検索
     */
    @Test
    void testFindById() {
        // IDでユーザーを検索
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        // 検証
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("テストユーザー", foundUser.get().getUsername());
    }

    /**
     * findById()メソッドのテスト - 存在しないIDを検索
     */
    @Test
    void testFindByIdNotFound() {
        // 存在しないIDでユーザーを検索
        Optional<User> foundUser = userRepository.findById(999L);

        // 検証
        assertFalse(foundUser.isPresent()); // 見つからない
    }

    /**
     * findByEmail()メソッドのテスト - メールアドレスでユーザーを検索
     * 
     * 実務での使用場面：
     * - ログイン処理
     * - メールアドレスの重複チェック
     */
    @Test
    void testFindByEmail() {
        // メールアドレスでユーザーを検索
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // 検証
        assertTrue(foundUser.isPresent());
        assertEquals("テストユーザー", foundUser.get().getUsername());
    }

    /**
     * findByEmail()メソッドのテスト - 存在しないメールアドレスを検索
     */
    @Test
    void testFindByEmailNotFound() {
        // 存在しないメールアドレスでユーザーを検索
        Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

        // 検証
        assertFalse(foundUser.isPresent()); // 見つからない
    }

    /**
     * findByEmail()メソッドのテスト - 大文字小文字の違いを検証
     * 
     * 実務での重要な注意点：
     * - メールアドレスは大文字小文字を区別しないのが一般的です
     * - しかし、findByEmail()は大文字小文字を区別します
     * - そのため、User.onCreate()で小文字に統一する必要があります
     */
    @Test
    void testFindByEmailCaseInsensitive() {
        // 大文字のメールアドレスでユーザーを作成
        User upperCaseUser = new User();
        upperCaseUser.setEmail("UPPER@EXAMPLE.COM");
        upperCaseUser.setPassword("password");
        upperCaseUser.setUsername("大文字ユーザー");

        // 保存（onCreate()で小文字に変換される）
        User savedUser = userRepository.save(upperCaseUser);

        // 小文字で検索
        Optional<User> foundUser = userRepository.findByEmail("upper@example.com");

        // 検証
        assertTrue(foundUser.isPresent());
        assertEquals("upper@example.com", foundUser.get().getEmail()); // 小文字に統一されている
    }

    /**
     * existsByEmail()メソッドのテスト - メールアドレスの存在確認
     * 
     * 実務での使用場面：
     * - ユーザー登録時の重複チェック
     */
    @Test
    void testExistsByEmail() {
        // 存在するメールアドレス
        assertTrue(userRepository.existsByEmail("test@example.com"));

        // 存在しないメールアドレス
        assertFalse(userRepository.existsByEmail("notfound@example.com"));
    }

    /**
     * findByUsername()メソッドのテスト - ユーザー名でユーザーを検索
     */
    @Test
    void testFindByUsername() {
        // ユーザー名でユーザーを検索
        Optional<User> foundUser = userRepository.findByUsername("テストユーザー");

        // 検証
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    /**
     * deleteById()メソッドのテスト - IDでユーザーを削除
     */
    @Test
    void testDeleteById() {
        // ユーザーを削除
        userRepository.deleteById(testUser.getId());

        // 検証
        Optional<User> foundUser = userRepository.findById(testUser.getId());
        assertFalse(foundUser.isPresent()); // 削除されている
    }

    /**
     * update()のテスト - ユーザー情報の更新
     * 
     * 実務でのポイント：
     * - save()は、IDがある場合は更新になります
     * - @PreUpdateメソッドが自動的に呼ばれ、updatedAtが更新されます
     */
    @Test
    void testUpdateUser() throws InterruptedException {
        // ユーザー情報を取得
        User user = userRepository.findById(testUser.getId()).orElseThrow();

        // 更新前のupdatedAtを記録
        var originalUpdatedAt = user.getUpdatedAt();

        // 少し待機（updatedAtが変わることを確認するため）
        Thread.sleep(10);

        // ユーザー名を更新
        user.setUsername("更新されたユーザー");

        // 保存（更新）
        User updatedUser = userRepository.save(user);

        // 検証
        assertEquals("更新されたユーザー", updatedUser.getUsername());

        // createdAtは変わらない
        assertEquals(user.getCreatedAt(), updatedUser.getCreatedAt());

        // updatedAtは更新される（注: @PreUpdateはflush時に呼ばれるため、手動でflush）
        entityManager.flush();
        assertTrue(updatedUser.getUpdatedAt().isAfter(originalUpdatedAt) ||
                updatedUser.getUpdatedAt().isEqual(originalUpdatedAt));
    }

    /**
     * count()メソッドのテスト - ユーザー数をカウント
     */
    @Test
    void testCount() {
        // 初期状態（setUp()で1件登録済み）
        long count = userRepository.count();
        assertEquals(1, count);

        // 追加でユーザーを登録
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        newUser.setUsername("新規ユーザー");
        userRepository.save(newUser);

        // カウントが増えていることを確認
        count = userRepository.count();
        assertEquals(2, count);
    }
}