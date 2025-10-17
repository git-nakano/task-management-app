package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserServiceの統合テスト
 * 
 * @SpringBootTest:
 *                  - Spring Bootアプリケーション全体をロードしてテストします
 *                  - @DataJpaTestとは異なり、Service層も含めてテストできます
 *                  - 実際のデータベース（H2）を使用してテストします
 * 
 * @Transactional:
 *                 - 各テストメソッド実行後、データベースがロールバックされます
 *                 - テスト間でデータが影響し合わないようにするため重要です
 * 
 *                 実務でのポイント：
 *                 - Service層のテストは、ビジネスロジックの動作を確認します
 *                 - Repository層のテストとは異なり、複数のRepositoryを使用する場合もあります
 */
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    /**
     * 各テストメソッド実行前に呼ばれる初期化メソッド
     * 
     * Phase 2-6での更新：
     * - userRepository.deleteAll()を追加しました
     * - これにより、統合テストで作成されたデータをクリアします
     * 
     * なぜこの修正が必要なのか：
     * - 統合テスト（AuthControllerIntegrationTest、E2ETestScenario）と
     * 単体テスト（UserServiceTest）が同じH2データベースインスタンスを共有しています
     * - 統合テストで作成されたユーザーが残っている状態で、UserServiceTestが実行されると、
     * setUp()メソッドで同じメールアドレスのユーザーを作成しようとしてエラーになります
     * - deleteAll()を追加することで、各テスト前にデータベースをクリーンな状態にします
     * 
     * 実務でのポイント：
     * - @Transactionalがあるので、各テスト後にロールバックされますが、
     * テスト実行順序によっては、統合テストのデータが残る可能性があります
     * - テスト間でデータが影響し合わないように、setUp()でデータベースをクリアすることが推奨されます
     */
    @BeforeEach
    void setUp() {
        // データベースをクリア（統合テストで作成されたデータを削除）
        userRepository.deleteAll();

        // テスト用のユーザーを作成
        UserResponseDto createdUser = userService.createUser(
                "test@example.com",
                "hashedPassword123",
                "テストユーザー");
        testUserId = createdUser.getId();
    }

    /**
     * ユーザー作成のテスト
     */
    @Test
    void testCreateUser() {
        // 新しいユーザーを作成
        UserResponseDto user = userService.createUser(
                "new@example.com",
                "password456",
                "新しいユーザー");

        // 検証
        assertNotNull(user.getId());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("新しいユーザー", user.getUsername());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    /**
     * メールアドレス重複チェックのテスト
     */
    @Test
    void testCreateUserWithDuplicateEmail() {
        // 既に存在するメールアドレスでユーザーを作成しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(
                    "test@example.com", // 既に存在するメールアドレス
                    "password",
                    "重複ユーザー");
        });

        // エラーメッセージを確認
        assertEquals("このメールアドレスは既に使用されています", exception.getMessage());
    }

    /**
     * IDでユーザーを取得するテスト
     */
    @Test
    void testFindById() {
        // IDでユーザーを取得
        Optional<UserResponseDto> user = userService.findById(testUserId);

        // 検証
        assertTrue(user.isPresent());
        assertEquals("test@example.com", user.get().getEmail());
        assertEquals("テストユーザー", user.get().getUsername());
    }

    /**
     * 存在しないIDでユーザーを取得するテスト
     */
    @Test
    void testFindByIdNotFound() {
        // 存在しないIDでユーザーを取得
        Optional<UserResponseDto> user = userService.findById(999L);

        // 検証
        assertFalse(user.isPresent());
    }

    /**
     * メールアドレスでユーザーを取得するテスト
     */
    @Test
    void testFindByEmail() {
        // メールアドレスでユーザーを取得
        Optional<UserResponseDto> user = userService.findByEmail("test@example.com");

        // 検証
        assertTrue(user.isPresent());
        assertEquals("テストユーザー", user.get().getUsername());
    }

    /**
     * メールアドレスの存在確認テスト
     */
    @Test
    void testExistsByEmail() {
        // 存在するメールアドレス
        assertTrue(userService.existsByEmail("test@example.com"));

        // 存在しないメールアドレス
        assertFalse(userService.existsByEmail("notfound@example.com"));
    }

    /**
     * ユーザー情報更新のテスト
     */
    @Test
    void testUpdateUser() {
        // ユーザー名を更新
        UserResponseDto updatedUser = userService.updateUser(testUserId, "更新されたユーザー");

        // 検証
        assertEquals("更新されたユーザー", updatedUser.getUsername());
        assertEquals("test@example.com", updatedUser.getEmail()); // メールアドレスは変更されていない
    }

    /**
     * パスワード更新のテスト
     */
    @Test
    void testUpdatePassword() {
        // パスワードを更新
        UserResponseDto updatedUser = userService.updatePassword(testUserId, "newHashedPassword");

        // 検証（パスワードはレスポンスに含まれないため、直接確認できない）
        assertNotNull(updatedUser);
        assertEquals(testUserId, updatedUser.getId());

        // データベースから直接取得して確認
        var user = userRepository.findById(testUserId).orElseThrow();
        assertEquals("newHashedPassword", user.getPassword());
    }

    /**
     * ユーザー削除のテスト
     */
    @Test
    void testDeleteUser() {
        // ユーザーを削除
        userService.deleteUser(testUserId);

        // 検証
        Optional<UserResponseDto> deletedUser = userService.findById(testUserId);
        assertFalse(deletedUser.isPresent());
    }

    /**
     * 存在しないユーザーを削除しようとするテスト
     */
    @Test
    void testDeleteUserNotFound() {
        // 存在しないIDでユーザーを削除しようとする
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(999L);
        });

        // エラーメッセージを確認
        assertEquals("ユーザーが見つかりません", exception.getMessage());
    }

    /**
     * ユーザー総数のテスト
     */
    @Test
    void testCountUsers() {
        // 初期状態（setUp()で1件登録済み）
        long count = userService.countUsers();
        assertEquals(1, count);

        // 追加でユーザーを登録
        userService.createUser("another@example.com", "password", "別のユーザー");

        // カウントが増えていることを確認
        count = userService.countUsers();
        assertEquals(2, count);
    }
}