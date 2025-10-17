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
import org.springframework.security.test.context.support.WithMockUser;
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
 * TaskControllerのテスト（Phase 2-5版）
 * 
 * Phase 2-5での更新：
 * - @AutoConfigureMockMvc(addFilters = false)を追加しました
 * - これにより、テスト時にSpring Securityのフィルタが無効化されます
 * - 認証なしでAPIにアクセスできるため、テストが簡潔になります
 * 
 * なぜaddFilters = falseが必要か：
 * - Phase 2-5でSpring Securityを有効化しました
 * - SecurityConfigで、/api/auth/**以外のすべてのエンドポイントに認証が必要になりました
 * - テストでは認証情報を提供する代わりに、フィルタを無効化することでテストを簡素化します
 * 
 * 実務でのポイント：
 * - テスト時にSpring Securityのフィルタを無効化することは、一般的な手法です
 * - Controller層のテストでは、ビジネスロジックに焦点を当てるため、認証はスキップします
 * - 認証のテストは、別途統合テストで行います（Phase 2-6で実装予定）
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
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testCreateTask() throws Exception {
                TaskRequestDto requestDto = new TaskRequestDto();
                requestDto.setTitle("買い物に行く");
                requestDto.setDescription("スーパーで食材を買う");
                requestDto.setDueDate(LocalDate.now().plusDays(1));
                requestDto.setStatus(TaskStatus.TODO);
                requestDto.setPriority(TaskPriority.HIGH);

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

                when(taskService.createTask(any(TaskRequestDto.class), eq(1L))).thenReturn(responseDto);

                mockMvc.perform(post("/api/tasks")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("買い物に行く"));
        }

        /**
         * IDでタスクを取得するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetTaskById() throws Exception {
                TaskResponseDto responseDto = new TaskResponseDto();
                responseDto.setId(1L);
                responseDto.setTitle("買い物に行く");
                responseDto.setStatus(TaskStatus.TODO);
                responseDto.setPriority(TaskPriority.HIGH);

                when(taskService.findById(1L, 1L)).thenReturn(responseDto);

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
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetAllTasks() throws Exception {
                TaskResponseDto task1 = new TaskResponseDto();
                task1.setId(1L);
                task1.setTitle("買い物に行く");
                task1.setStatus(TaskStatus.TODO);

                TaskResponseDto task2 = new TaskResponseDto();
                task2.setId(2L);
                task2.setTitle("資料作成");
                task2.setStatus(TaskStatus.IN_PROGRESS);

                List<TaskResponseDto> tasks = Arrays.asList(task1, task2);

                when(taskService.findAllByUserId(1L)).thenReturn(tasks);

                mockMvc.perform(get("/api/tasks")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1))
                                .andExpect(jsonPath("$[0].title").value("買い物に行く"));
        }

        /**
         * ステータスでフィルタするテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetTasksByStatus() throws Exception {
                TaskResponseDto task1 = new TaskResponseDto();
                task1.setId(1L);
                task1.setTitle("買い物に行く");
                task1.setStatus(TaskStatus.TODO);

                List<TaskResponseDto> tasks = Arrays.asList(task1);

                when(taskService.findByStatus(1L, TaskStatus.TODO)).thenReturn(tasks);

                mockMvc.perform(get("/api/tasks/status/TODO")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].status").value("TODO"));
        }

        /**
         * 優先度でフィルタするテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testGetTasksByPriority() throws Exception {
                TaskResponseDto task1 = new TaskResponseDto();
                task1.setId(1L);
                task1.setTitle("買い物に行く");
                task1.setPriority(TaskPriority.HIGH);

                List<TaskResponseDto> tasks = Arrays.asList(task1);

                when(taskService.findByPriority(1L, TaskPriority.HIGH)).thenReturn(tasks);

                mockMvc.perform(get("/api/tasks/priority/HIGH")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].priority").value("HIGH"));
        }

        /**
         * キーワード検索のテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testSearchTasks() throws Exception {
                TaskResponseDto task1 = new TaskResponseDto();
                task1.setId(1L);
                task1.setTitle("買い物に行く");

                List<TaskResponseDto> tasks = Arrays.asList(task1);

                when(taskService.searchByKeyword(1L, "買い物")).thenReturn(tasks);

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
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testFilterTasks() throws Exception {
                TaskResponseDto task1 = new TaskResponseDto();
                task1.setId(1L);
                task1.setTitle("買い物に行く");
                task1.setStatus(TaskStatus.TODO);
                task1.setPriority(TaskPriority.HIGH);

                List<TaskResponseDto> tasks = Arrays.asList(task1);

                when(taskService.findWithFilters(1L, TaskStatus.TODO, TaskPriority.HIGH, "買い物"))
                                .thenReturn(tasks);

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
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testUpdateTask() throws Exception {
                TaskRequestDto requestDto = new TaskRequestDto();
                requestDto.setTitle("更新されたタスク");
                requestDto.setDescription("更新されました");
                requestDto.setDueDate(LocalDate.now().plusDays(5));
                requestDto.setStatus(TaskStatus.IN_PROGRESS);
                requestDto.setPriority(TaskPriority.MEDIUM);

                TaskResponseDto responseDto = new TaskResponseDto();
                responseDto.setId(1L);
                responseDto.setTitle("更新されたタスク");
                responseDto.setStatus(TaskStatus.IN_PROGRESS);

                when(taskService.updateTask(eq(1L), any(TaskRequestDto.class), eq(1L)))
                                .thenReturn(responseDto);

                mockMvc.perform(put("/api/tasks/1")
                                .param("userId", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("更新されたタスク"));
        }

        /**
         * ステータスのみを更新するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testUpdateTaskStatus() throws Exception {
                TaskResponseDto responseDto = new TaskResponseDto();
                responseDto.setId(1L);
                responseDto.setTitle("買い物に行く");
                responseDto.setStatus(TaskStatus.DONE);

                when(taskService.updateTaskStatus(1L, TaskStatus.DONE, 1L)).thenReturn(responseDto);

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
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testDeleteTask() throws Exception {
                mockMvc.perform(delete("/api/tasks/1")
                                .param("userId", "1"))
                                .andExpect(status().isNoContent());
        }

        /**
         * タスク総数を取得するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testCountTasks() throws Exception {
                when(taskService.countTasksByUserId(1L)).thenReturn(42L);

                mockMvc.perform(get("/api/tasks/count")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("42"));
        }

        /**
         * ステータス別タスク数を取得するテスト
         */
        @Test
        @WithMockUser(username = "test@example.com", roles = "USER")
        void testCountTasksByStatus() throws Exception {
                when(taskService.countTasksByUserIdAndStatus(1L, TaskStatus.TODO)).thenReturn(10L);

                mockMvc.perform(get("/api/tasks/count/status/TODO")
                                .param("userId", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("10"));
        }
}