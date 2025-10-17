package com.taskmanagement.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.backend.dto.LoginRequestDto;
import com.taskmanagement.backend.dto.RegisterRequestDto;
import com.taskmanagement.backend.dto.UserResponseDto;
import com.taskmanagement.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 認証APIの統合テスト
 * 
 * @SpringBootTest:
 *                  - Spring Bootアプリケーション全体を起動してテストします
 *                  - 実際のデータベース（H2）を使用します
 *                  - すべての層（Controller + Service + Repository）が統合されます
 * 
 * @AutoConfigureMockMvc:
 *                        - MockMvcを使用してHTTPリクエストをシミュレートします
 *                        - addFilters = false: Spring
 *                        Securityのフィルタを無効化（認証APIのテストのため）
 * 
 *                        統合テストとは：
 *                        - 複数の層を組み合わせてテストします
 *                        - 実際のデータベースを使用します
 *                        - アプリケーション全体の動作を確認します
 * 
 *                        Controller層のテストとの違い：
 *                        - Controller層のテスト: Controller層のみをテスト、Service層はモック化
 *                        - 統合テスト: すべての層を統合してテスト、実際のデータベースを使用
 * 
 *                        実務でのポイント：
 *                        - 統合テストは、実際のアプリケーションの動作を確認するために重要です
 *                        - Controller層のテストより実行時間が長いですが、より信頼性が高いです
 *                        - 統合テストとController層のテストを組み合わせることで、効率的にテストできます
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    /**
     * 各テストの前に実行される初期化処理
     * 
     * @BeforeEach:
     *              - 各テストメソッドの前に実行されます
     *              - データベースをクリーンな状態にします
     * 
     *              実務でのポイント：
     *              - 統合テストでは、テスト間でデータが残らないように初期化が重要です
     *              - H2データベースはメモリ上に存在するため、アプリケーションを再起動すると消えます
     */
    @BeforeEach
    void setUp() {
        // データベースをクリア
        userRepository.deleteAll();
    }

    /**
     * 新規登録が成功するテスト
     * 
     * テストシナリオ：
     * 1. 新規登録APIにリクエストを送信
     * 2. HTTPステータスコード201（Created）が返される
     * 3. レスポンスに正しいユーザー情報が含まれる
     * 4. パスワードがレスポンスに含まれない（セキュリティ対策）
     * 5. データベースにユーザーが保存される
     * 6. パスワードがハッシュ化されて保存される
     */
    @Test
    void testRegisterSuccess() throws Exception {
        // 新規登録リクエストを作成
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("テストユーザー");

        // APIにリクエストを送信
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated()) // HTTPステータスコード201
                .andExpect(jsonPath("$.id").exists()) // IDが存在する
                .andExpect(jsonPath("$.email").value("test@example.com")) // メールアドレスが正しい
                .andExpect(jsonPath("$.username").value("テストユーザー")) // ユーザー名が正しい
                .andExpect(jsonPath("$.password").doesNotExist()) // パスワードがレスポンスに含まれない
                .andExpect(jsonPath("$.createdAt").exists()) // 作成日時が存在する
                .andExpect(jsonPath("$.updatedAt").exists()) // 更新日時が存在する
                .andReturn();

        // レスポンスをUserResponseDtoに変換
        String responseBody = result.getResponse().getContentAsString();
        UserResponseDto userResponse = objectMapper.readValue(responseBody, UserResponseDto.class);

        // データベースに保存されたユーザーを確認
        assertThat(userRepository.findById(userResponse.getId())).isPresent();

        // パスワードがハッシュ化されていることを確認
        userRepository.findById(userResponse.getId()).ifPresent(user -> {
            assertThat(user.getPassword()).isNotEqualTo("password123"); // パスワードがハッシュ化されている
            assertThat(user.getPassword()).startsWith("$2a$"); // BCryptのハッシュ形式
        });
    }

    /**
     * メールアドレスが重複している場合のテスト
     * 
     * テストシナリオ：
     * 1. 1回目の新規登録が成功
     * 2. 同じメールアドレスで2回目の新規登録を試みる
     * 3. HTTPステータスコード400（Bad Request）が返される
     * 4. エラーメッセージが正しい
     */
    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // 1回目の新規登録
        RegisterRequestDto registerDto1 = new RegisterRequestDto();
        registerDto1.setEmail("test@example.com");
        registerDto1.setPassword("password123");
        registerDto1.setUsername("テストユーザー1");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto1)))
                .andExpect(status().isCreated());

        // 2回目の新規登録（同じメールアドレス）
        RegisterRequestDto registerDto2 = new RegisterRequestDto();
        registerDto2.setEmail("test@example.com");
        registerDto2.setPassword("password456");
        registerDto2.setUsername("テストユーザー2");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto2)))
                .andExpect(status().isBadRequest()) // HTTPステータスコード400
                .andExpect(jsonPath("$.message").value("このメールアドレスは既に使用されています"));
    }

    /**
     * バリデーションエラーのテスト（メールアドレスが空）
     * 
     * テストシナリオ：
     * 1. メールアドレスが空の新規登録リクエストを送信
     * 2. HTTPステータスコード400（Bad Request）が返される
     * 3. バリデーションエラーメッセージが含まれる
     */
    @Test
    void testRegisterValidationErrorEmptyEmail() throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail(""); // 空のメールアドレス
        registerDto.setPassword("password123");
        registerDto.setUsername("テストユーザー");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * バリデーションエラーのテスト（パスワードが短い）
     * 
     * テストシナリオ：
     * 1. パスワードが8文字未満の新規登録リクエストを送信
     * 2. HTTPステータスコード400（Bad Request）が返される
     * 3. バリデーションエラーメッセージが含まれる
     */
    @Test
    void testRegisterValidationErrorShortPassword() throws Exception {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("pass"); // 8文字未満
        registerDto.setUsername("テストユーザー");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    /**
     * ログインが成功するテスト
     * 
     * テストシナリオ：
     * 1. 新規登録を行う
     * 2. ログインAPIにリクエストを送信
     * 3. HTTPステータスコード200（OK）が返される
     * 4. レスポンスに正しいユーザー情報が含まれる
     */
    @Test
    void testLoginSuccess() throws Exception {
        // 新規登録
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("テストユーザー");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        // ログイン
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk()) // HTTPステータスコード200
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("テストユーザー"))
                .andExpect(jsonPath("$.password").doesNotExist()); // パスワードがレスポンスに含まれない
    }

    /**
     * ログインが失敗するテスト（パスワードが間違っている）
     * 
     * テストシナリオ：
     * 1. 新規登録を行う
     * 2. 間違ったパスワードでログインを試みる
     * 3. HTTPステータスコード400（Bad Request）が返される
     * 4. エラーメッセージが曖昧である（セキュリティ対策）
     */
    @Test
    void testLoginFailureWrongPassword() throws Exception {
        // 新規登録
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("test@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("テストユーザー");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        // ログイン（パスワードが間違っている）
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest()) // HTTPステータスコード400
                .andExpect(jsonPath("$.message").value("メールアドレスまたはパスワードが正しくありません"));
    }

    /**
     * ログインが失敗するテスト（ユーザーが存在しない）
     * 
     * テストシナリオ：
     * 1. 存在しないメールアドレスでログインを試みる
     * 2. HTTPステータスコード400（Bad Request）が返される
     * 3. エラーメッセージが曖昧である（セキュリティ対策）
     */
    @Test
    void testLoginFailureUserNotFound() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest()) // HTTPステータスコード400
                .andExpect(jsonPath("$.message").value("メールアドレスまたはパスワードが正しくありません"));
    }
}