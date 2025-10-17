package com.taskmanagement.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.backend.dto.*;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import com.taskmanagement.backend.repository.TaskRepository;
import com.taskmanagement.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * E2Eテストシナリオ
 * 
 * E2Eテスト（End-to-End Test）とは：
 * - ユーザーの視点からアプリケーション全体をテストします
 * - 一連の操作フローを確認します
 * - 実際のユースケースに基づいたテストシナリオを実行します
 * 
 * このテストクラスの目的：
 * - ユーザーが新規登録からタスク管理までの一連の操作を行うシナリオをテストします
 * - 実際のアプリケーションの使用方法を確認します
 * - すべての機能が統合して正しく動作することを確認します
 * 
 * テストシナリオ：
 * 1. 新規登録
 * 2. ログイン
 * 3. タスク作成
 * 4. タスク一覧取得
 * 5. タスク更新
 * 6. タスク削除
 * 
 * 実務でのポイント：
 * - E2Eテストは、実際のユーザーの操作をシミュレートします
 * - 重要なユースケースを網羅することで、アプリケーション全体の品質を保証します
 * - E2Eテストは実行時間が長いですが、最も信頼性の高いテストです
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class E2ETestScenario {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 各テストの前に実行される初期化処理
     */
    @BeforeEach
    void setUp() {
        // データベースをクリア
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * E2Eテストシナリオ: ユーザー登録からタスク管理までの一連の流れ
     * 
     * テストシナリオ：
     * 1. 新規登録（POST /api/auth/register）
     * 2. ログイン（POST /api/auth/login）
     * 3. タスク作成（POST /api/tasks）
     * 4. タスク一覧取得（GET /api/tasks）
     * 5. タスク詳細取得（GET /api/tasks/{id}）
     * 6. タスクのステータス更新（PATCH /api/tasks/{id}/status）
     * 7. タスク更新（PUT /api/tasks/{id}）
     * 8. タスク削除（DELETE /api/tasks/{id}）
     * 
     * このテストの意義：
     * - 実際のユーザーがアプリケーションを使用する際の流れを確認します
     * - すべての機能が統合して正しく動作することを保証します
     * - リグレッション（機能の退行）を防ぎます
     */
    @Test
    void testCompleteUserJourney() throws Exception {
        // ========================================
        // Step 1: 新規登録
        // ========================================
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setEmail("user@example.com");
        registerDto.setPassword("password123");
        registerDto.setUsername("山田太郎");

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.username").value("山田太郎"))
                .andReturn();

        String registerResponseBody = registerResult.getResponse().getContentAsString();
        UserResponseDto user = objectMapper.readValue(registerResponseBody, UserResponseDto.class);
        Long userId = user.getId();

        // データベースに保存されたユーザーを確認
        assertThat(userRepository.findById(userId)).isPresent();

        // ========================================
        // Step 2: ログイン
        // ========================================
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("user@example.com");
        loginDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        // ========================================
        // Step 3: タスク作成
        // ========================================
        TaskRequestDto taskDto1 = new TaskRequestDto();
        taskDto1.setTitle("買い物に行く");
        taskDto1.setDescription("スーパーで食材を買う");
        taskDto1.setDueDate(LocalDate.now().plusDays(1));
        taskDto1.setStatus(TaskStatus.TODO);
        taskDto1.setPriority(TaskPriority.HIGH);

        MvcResult createTaskResult = mockMvc.perform(post("/api/tasks")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDto1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("買い物に行く"))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value("山田太郎"))
                .andReturn();

        String createTaskResponseBody = createTaskResult.getResponse().getContentAsString();
        TaskResponseDto task1 = objectMapper.readValue(createTaskResponseBody, TaskResponseDto.class);
        Long task1Id = task1.getId();

        // 2つ目のタスクを作成
        TaskRequestDto taskDto2 = new TaskRequestDto();
        taskDto2.setTitle("レポートを書く");
        taskDto2.setDescription("月次レポートを作成する");
        taskDto2.setDueDate(LocalDate.now().plusDays(3));
        taskDto2.setStatus(TaskStatus.TODO);
        taskDto2.setPriority(TaskPriority.MEDIUM);

        MvcResult createTask2Result = mockMvc.perform(post("/api/tasks")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskDto2)))
                .andExpect(status().isCreated())
                .andReturn();

        String createTask2ResponseBody = createTask2Result.getResponse().getContentAsString();
        TaskResponseDto task2 = objectMapper.readValue(createTask2ResponseBody, TaskResponseDto.class);
        Long task2Id = task2.getId();

        // データベースに保存されたタスクを確認
        assertThat(taskRepository.findById(task1Id)).isPresent();
        assertThat(taskRepository.findById(task2Id)).isPresent();

        // ========================================
        // Step 4: タスク一覧取得
        // ========================================
        mockMvc.perform(get("/api/tasks")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // 2つのタスクが返される
                .andExpect(jsonPath("$[0].title").value("買い物に行く"))
                .andExpect(jsonPath("$[1].title").value("レポートを書く"));

        // ========================================
        // Step 5: タスク詳細取得
        // ========================================
        mockMvc.perform(get("/api/tasks/" + task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task1Id))
                .andExpect(jsonPath("$.title").value("買い物に行く"))
                .andExpect(jsonPath("$.description").value("スーパーで食材を買う"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        // ========================================
        // Step 6: タスクのステータス更新
        // ========================================
        TaskStatusUpdateDto statusUpdateDto = new TaskStatusUpdateDto();
        statusUpdateDto.setStatus(TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/tasks/" + task1Id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task1Id))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // ステータスが更新されたことを確認
        mockMvc.perform(get("/api/tasks/" + task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // ========================================
        // Step 7: タスク更新
        // ========================================
        TaskRequestDto updateDto = new TaskRequestDto();
        updateDto.setTitle("買い物に行く（更新）");
        updateDto.setDescription("スーパーと薬局で買い物する");
        updateDto.setDueDate(LocalDate.now().plusDays(2));
        updateDto.setStatus(TaskStatus.IN_PROGRESS);
        updateDto.setPriority(TaskPriority.MEDIUM);

        mockMvc.perform(put("/api/tasks/" + task1Id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task1Id))
                .andExpect(jsonPath("$.title").value("買い物に行く（更新）"))
                .andExpect(jsonPath("$.description").value("スーパーと薬局で買い物する"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"));

        // タスクが更新されたことを確認
        mockMvc.perform(get("/api/tasks/" + task1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("買い物に行く（更新）"))
                .andExpect(jsonPath("$.description").value("スーパーと薬局で買い物する"));

        // ========================================
        // Step 8: タスク削除
        // ========================================
        mockMvc.perform(delete("/api/tasks/" + task2Id))
                .andExpect(status().isNoContent());

        // タスクが削除されたことを確認
        mockMvc.perform(get("/api/tasks/" + task2Id))
                .andExpect(status().isNotFound());

        // タスク一覧を取得して、1つのタスクだけが残っていることを確認
        mockMvc.perform(get("/api/tasks")
                .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("買い物に行く（更新）"));

        // ========================================
        // テスト完了
        // ========================================
        // すべての機能が統合して正しく動作することを確認しました！
    }

    /**
     * E2Eテストシナリオ: 複数ユーザーのタスク管理
     * 
     * テストシナリオ：
     * 1. ユーザー1を登録
     * 2. ユーザー2を登録
     * 3. ユーザー1がタスクを作成
     * 4. ユーザー2がタスクを作成
     * 5. ユーザー1のタスク一覧を取得（ユーザー1のタスクのみ表示される）
     * 6. ユーザー2のタスク一覧を取得（ユーザー2のタスクのみ表示される）
     * 
     * このテストの意義：
     * - ユーザー間でタスクが分離されていることを確認します
     * - データの整合性を保証します
     */
    @Test
    void testMultipleUsersTaskManagement() throws Exception {
        // ========================================
        // ユーザー1を登録
        // ========================================
        RegisterRequestDto user1RegisterDto = new RegisterRequestDto();
        user1RegisterDto.setEmail("user1@example.com");
        user1RegisterDto.setPassword("password123");
        user1RegisterDto.setUsername("ユーザー1");

        MvcResult user1RegisterResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1RegisterDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String user1ResponseBody = user1RegisterResult.getResponse().getContentAsString();
        UserResponseDto user1 = objectMapper.readValue(user1ResponseBody, UserResponseDto.class);
        Long user1Id = user1.getId();

        // ========================================
        // ユーザー2を登録
        // ========================================
        RegisterRequestDto user2RegisterDto = new RegisterRequestDto();
        user2RegisterDto.setEmail("user2@example.com");
        user2RegisterDto.setPassword("password123");
        user2RegisterDto.setUsername("ユーザー2");

        MvcResult user2RegisterResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2RegisterDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String user2ResponseBody = user2RegisterResult.getResponse().getContentAsString();
        UserResponseDto user2 = objectMapper.readValue(user2ResponseBody, UserResponseDto.class);
        Long user2Id = user2.getId();

        // ========================================
        // ユーザー1がタスクを作成
        // ========================================
        TaskRequestDto user1TaskDto = new TaskRequestDto();
        user1TaskDto.setTitle("ユーザー1のタスク");
        user1TaskDto.setDescription("これはユーザー1のタスクです");
        user1TaskDto.setDueDate(LocalDate.now().plusDays(1));
        user1TaskDto.setStatus(TaskStatus.TODO);
        user1TaskDto.setPriority(TaskPriority.HIGH);

        mockMvc.perform(post("/api/tasks")
                .param("userId", user1Id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1TaskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user1Id))
                .andExpect(jsonPath("$.username").value("ユーザー1"));

        // ========================================
        // ユーザー2がタスクを作成
        // ========================================
        TaskRequestDto user2TaskDto = new TaskRequestDto();
        user2TaskDto.setTitle("ユーザー2のタスク");
        user2TaskDto.setDescription("これはユーザー2のタスクです");
        user2TaskDto.setDueDate(LocalDate.now().plusDays(1));
        user2TaskDto.setStatus(TaskStatus.TODO);
        user2TaskDto.setPriority(TaskPriority.MEDIUM);

        mockMvc.perform(post("/api/tasks")
                .param("userId", user2Id.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2TaskDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user2Id))
                .andExpect(jsonPath("$.username").value("ユーザー2"));

        // ========================================
        // ユーザー1のタスク一覧を取得
        // ========================================
        mockMvc.perform(get("/api/tasks")
                .param("userId", user1Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // ユーザー1のタスクのみ
                .andExpect(jsonPath("$[0].title").value("ユーザー1のタスク"))
                .andExpect(jsonPath("$[0].userId").value(user1Id));

        // ========================================
        // ユーザー2のタスク一覧を取得
        // ========================================
        mockMvc.perform(get("/api/tasks")
                .param("userId", user2Id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // ユーザー2のタスクのみ
                .andExpect(jsonPath("$[0].title").value("ユーザー2のタスク"))
                .andExpect(jsonPath("$[0].userId").value(user2Id));

        // ========================================
        // テスト完了
        // ========================================
        // ユーザー間でタスクが分離されていることを確認しました！
    }

    /**
     * E2Eテストのポイント（コメント）
     * 
     * E2Eテストで確認すること：
     * 1. ユーザーの実際の操作フローが正しく動作する
     * 2. すべての機能が統合して動作する
     * 3. データの整合性が保たれる
     * 4. エラーハンドリングが正しく動作する
     * 
     * E2Eテストの利点：
     * - 実際のアプリケーションの使用方法を確認できる
     * - リグレッション（機能の退行）を防げる
     * - すべての層が統合して正しく動作することを保証できる
     * 
     * E2Eテストの欠点：
     * - 実行時間が長い
     * - メンテナンスコストが高い
     * - テストが失敗した際に、どの層で問題が発生したか特定しにくい
     * 
     * 実務でのポイント：
     * - E2Eテストは、重要なユースケースに絞って実装します
     * - すべての機能をE2Eテストでカバーする必要はありません
     * - Controller層のテスト、Service層のテスト、E2Eテストを組み合わせることで、
     * 効率的にテストカバレッジを向上させます
     */
}