package com.taskmanagement.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.backend.dto.TaskRequestDto;
import com.taskmanagement.backend.dto.TaskResponseDto;
import com.taskmanagement.backend.model.TaskPriority;
import com.taskmanagement.backend.model.TaskStatus;
import com.taskmanagement.backend.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskControllerのテスト
 * 
 * @WebMvcTest(TaskController.class):
 * - TaskControllerのみをテストします
 * - Service層はモック化されます
 * 
 * @AutoConfigureMockMvc(addFilters = false):
 *                                  - Spring Securityのフィルタを無効化します
 *                                  - Phase 2-4の段階では、認証機能をまだ実装していないため、
 *                                  テストでSpring Securityのデフォルト設定を無効化します
 *                                  - Phase
 *                                  2-5で認証機能を実装した後は、@WithMockUserなどを使用します
 * 
 *                                  MockMvcとは：
 *                                  - HTTPリクエストをシミュレートするためのツール
 *                                  - 実際のHTTPサーバーを起動せずにテストできます
 * 
 *                                  ObjectMapperとは：
 *                                  - JavaオブジェクトをJSON文字列に変換するためのツール
 *                                  - リクエストボディを作成する際に使用します
 * 
 *                                  実務でのポイント：
 *                                  - perform(): HTTPリクエストを送信
 *                                  - andExpect(): レスポンスを検証
 *                                  - jsonPath(): JSONレスポンスの特定のフィールドを検証
 * 
 *                                  Spring Securityとテストの注意点：
 *                                  - Spring
 *                                  Securityが有効な場合、デフォルトですべてのエンドポイントが認証を要求します
 *                                  - テストでは、@AutoConfigureMockMvc(addFilters =
 *                                  false)でフィルタを無効化するか、
 * @WithMockUserで認証情報を含める必要があります
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    /**
     * タスクを作成するテスト
     */
    @Test
    void testCreateTask() throws Exception {
        // リクエストボディを作成
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("買い物に行く");
        requestDto.setDescription("スーパーで食材を買う");
        requestDto.setDueDate(LocalDate.now().plusDays(1));
        requestDto.setStatus(TaskStatus.TODO);
        requestDto.setPriority(TaskPriority.HIGH);

        // モックのレスポンスを作成
        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("買い物に行く");
        responseDto.setDescription("スーパーで食材を買う");
        responseDto.setDueDate(LocalDate.now().plusDays(1));
        responseDto.setStatus(TaskStatus.TODO);
        responseDto.setPriority(TaskPriority.HIGH);
        responseDto.setUserId(1L);
        responseDto.setUsername("テストユーザー");
        responseDto.setCreatedAt(LocalDateTime.now());
        responseDto.setUpdatedAt(LocalDateTime.now());

        // モックの動作を定義
        when(taskService.createTask(any(TaskRequestDto.class), eq(1L))).thenReturn(responseDto);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(post("/api/tasks")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // HTTPステータスコード201
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("買い物に行く"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    /**
     * IDでタスクを取得するテスト
     */
    @Test
    void testGetTaskById() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("買い物に行く");
        responseDto.setStatus(TaskStatus.TODO);
        responseDto.setPriority(TaskPriority.HIGH);
        responseDto.setUserId(1L);
        responseDto.setUsername("テストユーザー");

        // モックの動作を定義
        when(taskService.findById(1L, 1L)).thenReturn(responseDto);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/1")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("買い物に行く"));
    }

    /**
     * 全タスクを取得するテスト
     */
    @Test
    void testGetAllTasks() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto task1 = new TaskResponseDto();
        task1.setId(1L);
        task1.setTitle("買い物に行く");
        task1.setStatus(TaskStatus.TODO);

        TaskResponseDto task2 = new TaskResponseDto();
        task2.setId(2L);
        task2.setTitle("資料作成");
        task2.setStatus(TaskStatus.IN_PROGRESS);

        List<TaskResponseDto> tasks = Arrays.asList(task1, task2);

        // モックの動作を定義
        when(taskService.findAllByUserId(1L)).thenReturn(tasks);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("買い物に行く"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("資料作成"));
    }

    /**
     * ステータスでフィルタするテスト
     */
    @Test
    void testGetTasksByStatus() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto task1 = new TaskResponseDto();
        task1.setId(1L);
        task1.setTitle("買い物に行く");
        task1.setStatus(TaskStatus.TODO);

        List<TaskResponseDto> tasks = Arrays.asList(task1);

        // モックの動作を定義
        when(taskService.findByStatus(1L, TaskStatus.TODO)).thenReturn(tasks);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/status/TODO")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    /**
     * 優先度でフィルタするテスト
     */
    @Test
    void testGetTasksByPriority() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto task1 = new TaskResponseDto();
        task1.setId(1L);
        task1.setTitle("買い物に行く");
        task1.setPriority(TaskPriority.HIGH);

        List<TaskResponseDto> tasks = Arrays.asList(task1);

        // モックの動作を定義
        when(taskService.findByPriority(1L, TaskPriority.HIGH)).thenReturn(tasks);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/priority/HIGH")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    /**
     * キーワード検索のテスト
     */
    @Test
    void testSearchTasks() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto task1 = new TaskResponseDto();
        task1.setId(1L);
        task1.setTitle("買い物に行く");

        List<TaskResponseDto> tasks = Arrays.asList(task1);

        // モックの動作を定義
        when(taskService.searchByKeyword(1L, "買い物")).thenReturn(tasks);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/search")
                .param("keyword", "買い物")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("買い物に行く"));
    }

    /**
     * 複合条件で検索するテスト
     */
    @Test
    void testFilterTasks() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto task1 = new TaskResponseDto();
        task1.setId(1L);
        task1.setTitle("買い物に行く");
        task1.setStatus(TaskStatus.TODO);
        task1.setPriority(TaskPriority.HIGH);

        List<TaskResponseDto> tasks = Arrays.asList(task1);

        // モックの動作を定義
        when(taskService.findWithFilters(1L, TaskStatus.TODO, TaskPriority.HIGH, "買い物"))
                .thenReturn(tasks);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/filter")
                .param("userId", "1")
                .param("status", "TODO")
                .param("priority", "HIGH")
                .param("keyword", "買い物"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    /**
     * タスクを更新するテスト
     */
    @Test
    void testUpdateTask() throws Exception {
        // リクエストボディを作成
        TaskRequestDto requestDto = new TaskRequestDto();
        requestDto.setTitle("更新されたタスク");
        requestDto.setDescription("更新されました");
        requestDto.setDueDate(LocalDate.now().plusDays(5));
        requestDto.setStatus(TaskStatus.IN_PROGRESS);
        requestDto.setPriority(TaskPriority.MEDIUM);

        // モックのレスポンスを作成
        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("更新されたタスク");
        responseDto.setDescription("更新されました");
        responseDto.setStatus(TaskStatus.IN_PROGRESS);
        responseDto.setPriority(TaskPriority.MEDIUM);

        // モックの動作を定義
        when(taskService.updateTask(eq(1L), any(TaskRequestDto.class), eq(1L)))
                .thenReturn(responseDto);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(put("/api/tasks/1")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新されたタスク"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    /**
     * ステータスのみを更新するテスト
     */
    @Test
    void testUpdateTaskStatus() throws Exception {
        // モックのレスポンスを作成
        TaskResponseDto responseDto = new TaskResponseDto();
        responseDto.setId(1L);
        responseDto.setTitle("買い物に行く");
        responseDto.setStatus(TaskStatus.DONE);

        // モックの動作を定義
        when(taskService.updateTaskStatus(1L, TaskStatus.DONE, 1L)).thenReturn(responseDto);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(put("/api/tasks/1/status")
                .param("status", "DONE")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    /**
     * タスクを削除するテスト
     */
    @Test
    void testDeleteTask() throws Exception {
        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(delete("/api/tasks/1")
                .param("userId", "1"))
                .andExpect(status().isNoContent()); // HTTPステータスコード204
    }

    /**
     * タスク総数を取得するテスト
     */
    @Test
    void testCountTasks() throws Exception {
        // モックの動作を定義
        when(taskService.countTasksByUserId(1L)).thenReturn(42L);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/count")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("42"));
    }

    /**
     * ステータス別タスク数を取得するテスト
     */
    @Test
    void testCountTasksByStatus() throws Exception {
        // モックの動作を定義
        when(taskService.countTasksByUserIdAndStatus(1L, TaskStatus.TODO)).thenReturn(10L);

        // HTTPリクエストを送信し、レスポンスを検証
        mockMvc.perform(get("/api/tasks/count/status/TODO")
                .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }
}